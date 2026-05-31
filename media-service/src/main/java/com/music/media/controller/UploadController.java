package com.music.media.controller;

import com.music.common.dto.ApiResponse;
import com.music.media.dto.UploadResponse;
import com.music.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final MediaService mediaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        return ApiResponse.<UploadResponse>builder()
                .result(mediaService.upload(file, type))
                .build();
    }
}
