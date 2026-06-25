package com.music.payment.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumUpgradePublisher {

    public static final String PREMIUM_UPGRADE_CHANNEL = "premium-upgrade";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(Long userId, int durationDays, String txnRef) {
        try {
            String payload = objectMapper.writeValueAsString(
                    new PremiumUpgradeEvent(userId, durationDays, txnRef));
            redisTemplate.convertAndSend(PREMIUM_UPGRADE_CHANNEL, payload);
            log.info("Published premium-upgrade for user {} ({} days, txnRef {})",
                    userId, durationDays, txnRef);
        } catch (Exception ex) {
            // Best-effort: payment is already PAID. Log loudly so premium can be re-granted if needed.
            log.error("Failed to publish premium-upgrade for user {} (txnRef {})", userId, txnRef, ex);
        }
    }
}
