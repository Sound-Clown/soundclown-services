package com.music.song.dto.request;

import lombok.Getter;
import lombok.Setter;

// Partial update — only non-null fields are applied.
@Getter
@Setter
public class UpdateSongRequest {

    private String title;

    private String coverImage;

    private Long albumId;
}
