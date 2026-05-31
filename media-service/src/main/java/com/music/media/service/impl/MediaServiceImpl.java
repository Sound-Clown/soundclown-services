package com.music.media.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.media.dto.UploadResponse;
import com.music.media.service.MediaService;
import com.music.media.upload.FileTypeValidator;
import com.music.media.upload.UploadType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;
    private final FileTypeValidator fileTypeValidator;

    @Override
    public UploadResponse upload(MultipartFile file, String type) {
        UploadType uploadType = UploadType.from(type);
        fileTypeValidator.validate(file, uploadType);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", uploadType.getFolder(),
                    "resource_type", uploadType.getCloudinaryResourceType()));
            return UploadResponse.builder()
                    .url((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .build();
        } catch (Exception ex) {
            // Cloudinary surfaces auth/network failures as IOException or RuntimeException.
            log.error("Cloudinary upload failed", ex);
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }
}
