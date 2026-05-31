package com.music.song.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsResponse {
    private long totalSongs;
    private long approvedSongs;
    private long pendingSongs;
    private long totalPlays;
    private long totalLikes;
}
