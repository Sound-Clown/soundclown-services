package com.music.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.music.notification.event.ResetPasswordEmailEvent;
import com.music.notification.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetEmailListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final MailService mailService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ResetPasswordEmailEvent event =
                    objectMapper.readValue(message.getBody(), ResetPasswordEmailEvent.class);
            log.info("Received reset-password event for {}", event.getEmail());
            mailService.sendResetPasswordEmail(event.getEmail(), event.getToken());
        } catch (Exception ex) {
            // Swallow: a bad message must not kill the listener container.
            log.error("Failed to process reset-password event", ex);
        }
    }
}
