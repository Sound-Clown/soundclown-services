package com.music.song.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AlbumDetailResponse {
    private Long id;
    private String name;
    private String coverImage;
    private Long artistId;
    private String artistUsername;
    private LocalDateTime createdAt;
    private List<SongResponse> songs;   // chỉ bài APPROVED
}
