package com.music.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "AUTH_INVALID_CREDENTIALS")
    private String identifier;   // username hoặc email

    @NotBlank(message = "AUTH_INVALID_CREDENTIALS")
    private String password;
}
