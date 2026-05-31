package com.music.media.upload;

import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// Validates the real content type via magic bytes — never trusts the file extension
// or the client-supplied Content-Type header.
@Component
public class FileTypeValidator {

    private static final int HEADER_BYTES = 12;

    public void validate(MultipartFile file, UploadType type) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (file.getSize() > type.getMaxSizeBytes()) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String detected = detectMimeType(readHeader(file));
        if (detected == null || !type.getAllowedMimeTypes().contains(detected)) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private byte[] readHeader(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            int len = Math.min(content.length, HEADER_BYTES);
            byte[] header = new byte[len];
            System.arraycopy(content, 0, header, 0, len);
            return header;
        } catch (IOException ex) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    private String detectMimeType(byte[] h) {
        if (h.length >= 3 && h[0] == 'I' && h[1] == 'D' && h[2] == '3') {
            return "audio/mpeg";   // MP3 with ID3 tag
        }
        if (h.length >= 2 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xE0) == 0xE0) {
            return "audio/mpeg";   // MP3 frame sync (no ID3 tag)
        }
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && (h[4] & 0xFF) == 0x0D && (h[5] & 0xFF) == 0x0A && (h[6] & 0xFF) == 0x1A && (h[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
