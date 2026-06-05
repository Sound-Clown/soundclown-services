package com.music.song.dto.response;

import lombok.Builder;
import lombok.Getter;

// An artist is identified by name (artist_username) — the catalog attributes songs by name,
// not by a per-artist user account.
@Getter
@Builder
public class ArtistResponse {
    private String name;
    private long songCount;
    private long albumCount;
    private long totalPlays;
}
