package com.music.user.dto.response;

import com.music.common.security.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean active;
    private boolean premium;          // derived: premiumUntil is in the future
    private LocalDateTime premiumUntil;
    private LocalDateTime createdAt;
}
