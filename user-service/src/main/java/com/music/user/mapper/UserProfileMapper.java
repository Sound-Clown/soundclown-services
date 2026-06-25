package com.music.user.mapper;

import com.music.user.dto.response.UserProfileResponse;
import com.music.user.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfile profile) {
        LocalDateTime premiumUntil = profile.getPremiumUntil();
        boolean premium = premiumUntil != null && premiumUntil.isAfter(LocalDateTime.now());
        return UserProfileResponse.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .role(profile.getRole())
                .active(profile.isActive())
                .premium(premium)
                .premiumUntil(premiumUntil)
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
