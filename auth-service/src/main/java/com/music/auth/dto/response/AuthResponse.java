package com.music.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private UserInfoDto user;
}
