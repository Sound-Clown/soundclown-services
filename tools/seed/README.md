# Seed real music into song-service

`seed_songs.py` pulls **real Creative-Commons tracks** from Jamendo + Internet Archive,
uploads the audio/cover to _this project's_ Cloudinary, and generates a Flyway migration
`song-service/src/main/resources/db/migration/V2__seed_songs.sql` (albums + songs, all
`APPROVED`, owned by the seed `artist` account, id = 2).

All sources are CC-licensed; `artist_username` carries the required attribution. This is
legal, real music — not random placeholder audio.

## Why this design

- **Generate once, commit the SQL.** Flyway then loads the same catalog on every fresh DB —
  reproducible, no network/API dependency at runtime.
- **Cloudinary fetches by URL** (server-side), so nothing is downloaded to your machine.
- Audio is stored under `music-app/audio` (resource type `video`), covers under `music-app/covers`.

## Prerequisites

1. **Jamendo client id** (free, ~5 min): register an app at https://devportal.jamendo.com → copy `Client ID`.
   (Internet Archive needs no key.)
2. **Cloudinary credentials** of the project (Dashboard → cloud name / API key / API secret).

## Run

```bash
cd tools/seed
python3 -m pip install -r requirements.txt          # only for the real (upload) run

# dry run — no Cloudinary, stores source URLs; good to preview the catalog:
python3 seed_songs.py --count 60 --sources archive --no-upload

# real run — uploads to Cloudinary and writes V2__seed_songs.sql:
JAMENDO_CLIENT_ID=your_id \
CLOUDINARY_CLOUD_NAME=your_cloud \
CLOUDINARY_API_KEY=your_key \
CLOUDINARY_API_SECRET=your_secret \
python3 seed_songs.py --count 1000 --sources jamendo,archive
```

Options: `--count` (default 1000), `--sources jamendo,archive`, `--no-upload`, `--out <path>`.

Then apply it:

```bash
docker compose up --build      # Flyway runs V1 then V2 on a fresh song_db
# (already running? recreate song_db: docker compose down -v, or drop the volume)
```

## Notes / caveats

- **Re-generating after V2 is already applied** changes the file's checksum → Flyway will refuse.
  For a dev reset, drop the DB (`docker compose down -v`) and bring it up again.
- **Full-text search**: seeded songs are inserted straight into `song_db`, bypassing the
  index-on-approve flow, so they aren't in Elasticsearch yet. After seeding, push them all in
  with one call (idempotent):

  ```bash
  curl -X POST http://localhost:8082/internal/songs/reindex   # -> {"code":1000,"result":934}
  ```

  Browsing/album/detail/play/like work without this; only `/api/search` needs it.

- Cloudinary free tier: ~25 GB storage/bandwidth. 1000 tracks ≈ 3–5 GB storage; heavy playback
  will consume bandwidth quota. Use `--count` to scale down, or `--no-upload` to keep source URLs.
