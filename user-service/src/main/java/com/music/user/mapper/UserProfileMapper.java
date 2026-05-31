package com.music.user.mapper;

import com.music.user.dto.response.UserProfileResponse;
import com.music.user.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .role(profile.getRole())
                .active(profile.isActive())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
