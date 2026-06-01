package com.music.song.client;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongIndexRequest {
    private Long id;
    private String title;
    private String artistUsername;
    private String albumName;
    private String coverImage;
    private int playCount;
    private int likeCount;
    private Long createdAt;
}
