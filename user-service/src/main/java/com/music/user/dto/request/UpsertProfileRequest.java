package com.music.user.dto.request;

import com.music.common.security.Role;
import lombok.Getter;
import lombok.Setter;

// Body of the internal sync endpoint called by auth-service.
@Getter
@Setter
public class UpsertProfileRequest {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean active;
}
