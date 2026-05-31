package com.music.media.service;

import com.music.media.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    UploadResponse upload(MultipartFile file, String type);
}
