package com.music.auth.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetEmailPublisher {

    public static final String RESET_EMAIL_CHANNEL = "reset-password-email";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Best-effort: a Redis/notification outage must not break the forgot-password flow.
    public void publish(String email, String token) {
        try {
            String payload = objectMapper.writeValueAsString(new ResetPasswordEmailEvent(email, token));
            redisTemplate.convertAndSend(RESET_EMAIL_CHANNEL, payload);
        } catch (Exception ex) {
            log.warn("Failed to publish reset-password event for {}", email, ex);
        }
    }
}
