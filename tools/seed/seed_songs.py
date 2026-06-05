#!/usr/bin/env python3
"""
Seed real Creative-Commons music into song-service.

Pipeline (run once, offline):
  1. Fetch track metadata from CC sources (Jamendo, Internet Archive) — diverse genres,
     ordered by popularity so the catalog is real music, not random filler.
  2. Optionally upload each audio + cover to *this project's* Cloudinary (Cloudinary fetches
     the remote URL server-side, so nothing is downloaded locally).
  3. Emit a Flyway migration: song-service/.../db/migration/V2__seed_songs.sql
     (INSERTs into `albums` and `songs`, all APPROVED, owned by the seed ARTIST account id=2).

All sources are Creative-Commons licensed; `artist_username` carries the attribution.

Usage:
    # dry run (no Cloudinary, stores source URLs) — good for testing:
    python3 seed_songs.py --count 60 --sources archive --no-upload

    # real run (uploads to your Cloudinary, needs creds in env / ../../.env):
    JAMENDO_CLIENT_ID=xxx \\
    CLOUDINARY_CLOUD_NAME=.. CLOUDINARY_API_KEY=.. CLOUDINARY_API_SECRET=.. \\
    python3 seed_songs.py --count 1000 --sources jamendo,archive

Env:
    JAMENDO_CLIENT_ID            required if 'jamendo' in --sources
    CLOUDINARY_CLOUD_NAME/_API_KEY/_API_SECRET   required unless --no-upload
"""
import argparse
import json
import os
import sys
import time
import urllib.parse
import urllib.request
from dataclasses import dataclass, field

# the seeded ARTIST account (see auth-service DataInitializer)
SEED_ARTIST_ID = 2
OUT_DEFAULT = os.path.join(
    os.path.dirname(__file__), "..", "..",
    "song-service", "src", "main", "resources", "db", "migration", "V2__seed_songs.sql",
)
JAMENDO_GENRES = [
    "rock", "pop", "electronic", "jazz", "classical", "hiphop", "folk",
    "ambient", "metal", "reggae", "funk", "blues", "soul", "punk", "lounge",
]
UA = {"User-Agent": "soundclown-seed/1.0"}


@dataclass
class Track:
    title: str
    artist: str
    album: str | None
    audio_url: str
    cover_url: str | None
    source: str
    license: str = ""
    genre: str = ""


def _get_json(url: str, retries: int = 3) -> dict:
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers=UA)
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.load(r)
        except Exception as e:  # noqa: BLE001 - one bad request shouldn't kill the run
            if attempt == retries - 1:
                print(
                    f"  ! request failed: {url[:80]}... ({e})", file=sys.stderr)
                return {}
            time.sleep(1.5 * (attempt + 1))
    return {}


# ---------------- Jamendo ----------------
def fetch_jamendo(client_id: str, want: int) -> list[Track]:
    per_genre = max(1, want // len(JAMENDO_GENRES)) + 1
    out: list[Track] = []
    for genre in JAMENDO_GENRES:
        params = {
            "client_id": client_id,
            "format": "json",
            "limit": str(min(per_genre, 200)),
            "order": "popularity_total",
            "include": "licenses musicinfo",
            "audioformat": "mp31",
            "tags": genre,
            "groupby": "artist_id",  # variety: avoid many tracks from one artist
        }
        url = "https://api.jamendo.com/v3.0/tracks/?" + \
            urllib.parse.urlencode(params)
        data = _get_json(url)
        results = data.get("results", []) if isinstance(data, dict) else []
        for t in results:
            audio = t.get("audio") or t.get("audiodownload")
            if not audio or not t.get("name"):
                continue
            out.append(Track(
                title=t.get("name", "").strip(),
                artist=(t.get("artist_name") or "Unknown Artist").strip(),
                album=(t.get("album_name") or "").strip() or None,
                audio_url=audio,
                cover_url=t.get("album_image") or t.get("image") or None,
                source="jamendo",
                license=t.get("license_ccurl", ""),
                genre=genre,
            ))
        print(f"  jamendo[{genre}]: {len(results)} tracks")
    return out


# ---------------- Internet Archive ----------------
def fetch_archive(want: int) -> list[Track]:
    q = "collection:(netlabels) AND mediatype:(audio)"
    params = {
        "q": q, "fl[]": ["identifier", "title", "creator"],
        "rows": str(want), "page": "1", "output": "json", "sort[]": "downloads desc",
    }
    url = "https://archive.org/advancedsearch.php?" + \
        urllib.parse.urlencode(params, doseq=True)
    data = _get_json(url)
    docs = data.get("response", {}).get(
        "docs", []) if isinstance(data, dict) else []
    out: list[Track] = []
    for d in docs:
        ident = d.get("identifier")
        if not ident:
            continue
        meta = _get_json(f"https://archive.org/metadata/{ident}")
        files = meta.get("files", []) if isinstance(meta, dict) else []
        mp3s = [f for f in files if str(
            f.get("name", "")).lower().endswith(".mp3")]
        if not mp3s:
            continue
        creator = d.get("creator")
        if isinstance(creator, list):
            creator = creator[0] if creator else None
        release = str(d.get("title") or ident).strip()
        # take up to 3 tracks per release -> real per-track titles + album grouping
        for mp3 in mp3s[:3]:
            track_title = str(mp3.get("title") or mp3.get("name", "")).strip()
            track_title = track_title.rsplit(".mp3", 1)[0]
            artist = str(mp3.get("artist") or mp3.get("creator")
                         or creator or "Internet Archive").strip()
            out.append(Track(
                title=track_title or release,
                artist=artist,
                album=release,
                audio_url=f"https://archive.org/download/{ident}/{urllib.parse.quote(mp3['name'])}",
                cover_url=f"https://archive.org/services/img/{ident}",
                source="archive",
                license="https://archive.org",
            ))
        print(f"  archive: {release[:45]} (+{min(len(mp3s), 3)} tracks)")
    return out


# ---------------- Cloudinary upload ----------------
def make_uploader(no_upload: bool):
    if no_upload:
        return lambda url, kind: url  # passthrough: store source URL
    import cloudinary
    import cloudinary.uploader
    cloudinary.config(
        cloud_name=os.environ["CLOUDINARY_CLOUD_NAME"],
        api_key=os.environ["CLOUDINARY_API_KEY"],
        api_secret=os.environ["CLOUDINARY_API_SECRET"],
        secure=True,
    )

    def upload(url: str, kind: str) -> str | None:
        try:
            res_type = "video" if kind == "audio" else "image"
            folder = "music-app/audio" if kind == "audio" else "music-app/covers"
            res = cloudinary.uploader.upload(
                url, resource_type=res_type, folder=folder)
            return res["secure_url"]
        except Exception as e:  # noqa: BLE001
            print(
                f"  ! cloudinary upload failed ({kind}): {e}", file=sys.stderr)
            return None

    return upload


# ---------------- SQL generation ----------------
def sql_str(v: str | None, maxlen: int) -> str:
    if v is None:
        return "NULL"
    return "'" + v[:maxlen].replace("\\", "\\\\").replace("'", "''") + "'"


def generate_sql(tracks: list[Track], out_path: str) -> None:
    # distinct albums -> explicit ids (albums table is empty on a fresh DB)
    album_ids: dict[tuple[str, str], int] = {}
    albums: list[tuple[int, Track]] = []
    for t in tracks:
        if t.album:
            key = (t.artist, t.album)
            if key not in album_ids:
                album_ids[key] = len(album_ids) + 1
                albums.append((album_ids[key], t))

    lines = [
        "-- Auto-generated by tools/seed/seed_songs.py — real Creative-Commons music.",
        f"-- {len(tracks)} songs, {len(albums)} albums. All APPROVED, owned by seed ARTIST (id=2).",
        "",
    ]
    if albums:
        lines.append(
            "INSERT INTO albums (id, name, cover_image, artist_id, artist_username, created_at) VALUES")
        rows = [
            f"  ({aid}, {sql_str(t.album, 255)}, {sql_str(t.cover_url, 500)}, "
            f"{SEED_ARTIST_ID}, {sql_str(t.artist, 50)}, NOW(6))"
            for aid, t in albums
        ]
        lines.append(",\n".join(rows) + ";")
        lines.append("")

    lines.append(
        "INSERT INTO songs (id, title, audio_file, cover_image, artist_id, artist_username, "
        "album_id, status, play_count, like_count, created_at, updated_at) VALUES"
    )
    rows = []
    for i, t in enumerate(tracks, start=1):
        aid = album_ids.get((t.artist, t.album)) if t.album else None
        album_sql = str(aid) if aid else "NULL"
        # deterministic pseudo-random, not all zero
        play = (i * 9301 + 49297) % 50000
        like = (i * 233 + 17) % 4000
        rows.append(
            f"  ({i}, {sql_str(t.title, 255)}, {sql_str(t.audio_url, 500)}, "
            f"{sql_str(t.cover_url, 500)}, {SEED_ARTIST_ID}, {sql_str(t.artist, 50)}, "
            f"{album_sql}, 'APPROVED', {play}, {like}, NOW(6), NOW(6))"
        )
    lines.append(",\n".join(rows) + ";")
    lines.append("")

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"\nWrote {len(tracks)} songs + {len(albums)} albums -> {out_path}")


def main() -> None:
    ap = argparse.ArgumentParser(description="Seed CC music into song-service")
    ap.add_argument("--count", type=int, default=1000)
    ap.add_argument("--sources", default="jamendo,archive",
                    help="comma list: jamendo,archive")
    ap.add_argument("--no-upload", action="store_true",
                    help="store source URLs instead of Cloudinary")
    ap.add_argument("--workers", type=int, default=8,
                    help="parallel Cloudinary uploads")
    ap.add_argument("--out", default=OUT_DEFAULT)
    args = ap.parse_args()

    sources = [s.strip() for s in args.sources.split(",") if s.strip()]
    tracks: list[Track] = []

    if "jamendo" in sources:
        cid = os.environ.get("JAMENDO_CLIENT_ID")
        if not cid:
            print("! JAMENDO_CLIENT_ID not set — skipping Jamendo", file=sys.stderr)
        else:
            print("Fetching Jamendo...")
            tracks += fetch_jamendo(cid, args.count)
    if "archive" in sources:
        print("Fetching Internet Archive...")
        # use archive to top up toward the target
        tracks += fetch_archive(max(20, args.count - len(tracks)))

    # dedupe by (artist, title), keep order
    seen, unique = set(), []
    for t in tracks:
        key = (t.artist.lower(), t.title.lower())
        if key in seen or not t.title or not t.audio_url:
            continue
        seen.add(key)
        unique.append(t)
    tracks = unique[: args.count]
    if not tracks:
        print("No tracks fetched — check sources/keys.", file=sys.stderr)
        sys.exit(1)

    upload = make_uploader(args.no_upload)

    def process(t: Track) -> Track | None:
        audio = upload(t.audio_url, "audio")
        if not audio:
            return None
        t.audio_url = audio
        if t.cover_url:
            t.cover_url = upload(t.cover_url, "cover")
        return t

    if args.no_upload:
        kept = [t for t in (process(t) for t in tracks) if t]
    else:
        from concurrent.futures import ThreadPoolExecutor
        print(
            f"\nUploading {len(tracks)} tracks to Cloudinary ({args.workers} workers)...")
        with ThreadPoolExecutor(max_workers=args.workers) as ex:
            results = list(ex.map(process, tracks))  # preserves order
        kept = [t for t in results if t]
        print(
            f"  {len(kept)}/{len(tracks)} uploaded ({len(tracks) - len(kept)} failed/skipped)")

    generate_sql(kept, args.out)


if __name__ == "__main__":
    main()
