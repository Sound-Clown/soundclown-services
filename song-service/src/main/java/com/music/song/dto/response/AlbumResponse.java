package com.music.song.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlbumResponse {
    private Long id;
    private String name;
    private String coverImage;
    private Long artistId;
    private LocalDateTime createdAt;
}
