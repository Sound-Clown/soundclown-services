package com.music.auth.config;

import com.music.auth.entity.User;
import com.music.auth.repository.UserRepository;
import com.music.common.security.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds three demo accounts (idempotent) so the system is usable right after a fresh start.
// These own the credentials — login works against them. Profiles are seeded separately in
// user-service. On a fresh DB the insertion order gives admin=1, artist=2, listener=3.
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seed("admin", "admin@soundclown.dev", Role.ADMIN);
        seed("artist", "artist@soundclown.dev", Role.ARTIST);
        seed("listener", "listener@soundclown.dev", Role.LISTENER);
    }

    private void seed(String username, String email, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        userRepository.save(User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(role)
                .active(true)
                .build());
        log.info("Seeded {} account: {} / {}", role, username, DEFAULT_PASSWORD);
    }
}
