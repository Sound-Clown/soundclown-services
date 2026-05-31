package com.music.song.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAlbumRequest {

    private String name;

    private String coverImage;
}
