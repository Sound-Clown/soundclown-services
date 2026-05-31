package com.music.song.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSongRequest {

    @NotBlank(message = "SONG_TITLE_BLANK")
    private String title;

    @NotBlank(message = "SONG_AUDIO_REQUIRED")
    private String audioFile;

    private String coverImage;

    private Long albumId;
}
