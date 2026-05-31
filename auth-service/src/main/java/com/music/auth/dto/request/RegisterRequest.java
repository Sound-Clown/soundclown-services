package com.music.auth.dto.request;

import com.music.common.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "AUTH_USERNAME_INVALID")
    @Size(min = 3, max = 50, message = "AUTH_USERNAME_INVALID")
    private String username;

    @NotBlank(message = "AUTH_EMAIL_INVALID")
    @Email(message = "AUTH_EMAIL_INVALID")
    private String email;

    @NotBlank(message = "AUTH_PASSWORD_INVALID")
    @Size(min = 6, message = "AUTH_PASSWORD_INVALID")
    private String password;

    // Optional. ADMIN không thể tự đăng ký — service ép về LISTENER nếu thiếu/không hợp lệ.
    private Role role;
}
