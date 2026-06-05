package com.music.user.config;

import com.music.common.security.Role;
import com.music.user.entity.UserProfile;
import com.music.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Seeds the public profiles for the three demo accounts (idempotent). The user_id values
// match the insertion order of auth-service's seed on a fresh database
// (admin=1, artist=2, listener=3), so profiles line up with the real accounts.
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void run(ApplicationArguments args) {
        seed(1L, "admin", "admin@soundclown.dev", Role.ADMIN);
        seed(2L, "artist", "artist@soundclown.dev", Role.ARTIST);
        seed(3L, "listener", "listener@soundclown.dev", Role.LISTENER);
    }

    private void seed(Long id, String username, String email, Role role) {
        if (userProfileRepository.existsById(id)) {
            return;
        }
        userProfileRepository.save(UserProfile.builder()
                .id(id)
                .username(username)
                .email(email)
                .role(role)
                .active(true)
                .build());
        log.info("Seeded profile: {} ({})", username, role);
    }
}
