package com.music.search.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongSearchResponse {
    private Long id;
    private String title;
    private String artistUsername;
    private String albumName;
    private String coverImage;
    private int playCount;
    private int likeCount;
}
