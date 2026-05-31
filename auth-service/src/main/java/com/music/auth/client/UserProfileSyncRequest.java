package com.music.auth.client;

import com.music.common.security.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileSyncRequest {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean active;
}
