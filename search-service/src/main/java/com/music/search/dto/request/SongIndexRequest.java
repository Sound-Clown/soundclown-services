package com.music.search.dto.request;

import lombok.Getter;
import lombok.Setter;

// Body sent by song-service to (re)index a song.
@Getter
@Setter
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
