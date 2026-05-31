package com.music.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "AUTH_EMAIL_INVALID")
    @Email(message = "AUTH_EMAIL_INVALID")
    private String email;
}
