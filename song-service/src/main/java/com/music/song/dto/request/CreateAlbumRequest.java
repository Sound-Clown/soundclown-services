package com.music.song.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAlbumRequest {

    @NotBlank(message = "ALBUM_NAME_BLANK")
    private String name;

    private String coverImage;
}
