package com.music.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Auth 1001-1100
    AUTH_INVALID_CREDENTIALS(1002, "Sai thông tin đăng nhập", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_LOCKED(1003, "Tài khoản bị khóa", HttpStatus.FORBIDDEN),
    AUTH_TOKEN_EXPIRED(1004, "Token hết hạn", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID(1005, "Invalid token", HttpStatus.UNAUTHORIZED),
    AUTH_UNAUTHORIZED(1006, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    AUTH_FORBIDDEN(1007, "Không đủ quyền truy cập", HttpStatus.FORBIDDEN),
    AUTH_USERNAME_INVALID(1010, "Username phải từ 3-50 ký tự", HttpStatus.BAD_REQUEST),
    AUTH_EMAIL_INVALID(1011, "Email không hợp lệ", HttpStatus.BAD_REQUEST),
    AUTH_PASSWORD_INVALID(1012, "Mật khẩu phải có ít nhất 6 ký tự", HttpStatus.BAD_REQUEST),
    AUTH_TOKEN_REQUIRED(1013, "Thiếu token", HttpStatus.BAD_REQUEST),
    AUTH_RESET_TOKEN_INVALID(1014, "Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    AUTH_OLD_PASSWORD_INCORRECT(1015, "Mật khẩu hiện tại không đúng", HttpStatus.BAD_REQUEST),

    // User 1201-1300
    USER_NOT_FOUND(1201, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL(1205, "Email đã được sử dụng", HttpStatus.CONFLICT),
    DUPLICATE_USERNAME(1206, "Username đã được sử dụng", HttpStatus.CONFLICT),
    CANNOT_LOCK_SELF(1207, "Không thể tự khóa tài khoản của mình", HttpStatus.BAD_REQUEST),
    USER_LOCK_SYNC_FAILED(1210, "Không thể đồng bộ trạng thái tài khoản với auth-service", HttpStatus.BAD_GATEWAY),

    // Song 1301-1400
    SONG_NOT_FOUND(1301, "Không tìm thấy bài hát", HttpStatus.NOT_FOUND),
    SONG_NOT_OWNED(1303, "Bạn không có quyền thao tác bài hát này", HttpStatus.FORBIDDEN),
    SONG_NOT_APPROVED(1304, "Bài hát chưa được duyệt", HttpStatus.FORBIDDEN),
    SONG_PREMIUM_REQUIRED(1305, "Bài hát này chỉ dành cho tài khoản Premium", HttpStatus.FORBIDDEN),
    SONG_TITLE_BLANK(1310, "Tên bài hát không được để trống", HttpStatus.BAD_REQUEST),
    SONG_AUDIO_REQUIRED(1311, "File audio là bắt buộc", HttpStatus.BAD_REQUEST),

    // Album 1401-1500
    ALBUM_NOT_FOUND(1401, "Không tìm thấy album", HttpStatus.NOT_FOUND),
    ALBUM_NOT_OWNED(1402, "Bạn không có quyền thao tác album này", HttpStatus.FORBIDDEN),
    ALBUM_NAME_BLANK(1410, "Tên album không được để trống", HttpStatus.BAD_REQUEST),

    // Media 1601-1700
    INVALID_FILE_TYPE(1601, "Định dạng file không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1602, "File quá dung lượng cho phép", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(1603, "Tải file lên thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    // Payment 1701-1800
    PAYMENT_NOT_FOUND(1701, "Không tìm thấy giao dịch", HttpStatus.NOT_FOUND),
    PAYMENT_INVALID_SIGNATURE(1702, "Chữ ký thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_FINALIZED(1703, "Giao dịch đã được xử lý", HttpStatus.CONFLICT),

    // Validation / request 1900-1999
    VALIDATION_ERROR(1900, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    ENUM_INVALID_VALUE(1901, "Giá trị không hợp lệ", HttpStatus.BAD_REQUEST),

    // System / DB 9xxx
    DB_ERROR(9001, "Database error", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_INTERNAL_ERROR(9000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
