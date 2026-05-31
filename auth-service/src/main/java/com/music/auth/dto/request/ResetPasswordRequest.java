package com.music.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "AUTH_TOKEN_REQUIRED")
    private String token;

    @NotBlank(message = "AUTH_PASSWORD_INVALID")
    @Size(min = 6, message = "AUTH_PASSWORD_INVALID")
    private String newPassword;
}
