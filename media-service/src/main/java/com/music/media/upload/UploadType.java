package com.music.media.upload;

import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import lombok.Getter;

import java.util.Set;

@Getter
public enum UploadType {

    AUDIO(Set.of("audio/mpeg"), 10L * 1024 * 1024, "music-app/audio", "video"),
    IMAGE(Set.of("image/jpeg", "image/png", "image/webp"), 2L * 1024 * 1024, "music-app/covers", "image");

    private final Set<String> allowedMimeTypes;
    private final long maxSizeBytes;
    private final String folder;
    private final String cloudinaryResourceType;   // Cloudinary treats audio as "video"

    UploadType(Set<String> allowedMimeTypes, long maxSizeBytes, String folder, String cloudinaryResourceType) {
        this.allowedMimeTypes = allowedMimeTypes;
        this.maxSizeBytes = maxSizeBytes;
        this.folder = folder;
        this.cloudinaryResourceType = cloudinaryResourceType;
    }

    public static UploadType from(String value) {
        if (value == null) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
        try {
            return UploadType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}
