package com.music.auth.dto.response;

import com.music.common.security.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
}
