package com.music.media.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadResponse {
    private String url;
    private String publicId;
}
