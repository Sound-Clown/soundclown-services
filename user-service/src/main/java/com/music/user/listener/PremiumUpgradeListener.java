package com.music.user.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.music.user.event.PremiumUpgradeEvent;
import com.music.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumUpgradeListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            PremiumUpgradeEvent event = objectMapper.readValue(message.getBody(), PremiumUpgradeEvent.class);
            log.info("Received premium-upgrade for user {} ({} days, txnRef {})",
                    event.userId(), event.durationDays(), event.txnRef());
            userService.activatePremium(event.userId(), event.durationDays());
        } catch (Exception ex) {
            log.error("Failed to process premium-upgrade event", ex);
        }
    }
}
