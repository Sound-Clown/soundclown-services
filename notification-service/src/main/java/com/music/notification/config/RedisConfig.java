package com.music.notification.config;

import com.music.notification.listener.ResetEmailListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    public static final String RESET_EMAIL_CHANNEL = "reset-password-email";

    @Bean
    public ChannelTopic resetEmailTopic() {
        return new ChannelTopic(RESET_EMAIL_CHANNEL);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ResetEmailListener resetEmailListener,
            ChannelTopic resetEmailTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(resetEmailListener, resetEmailTopic);
        return container;
    }
}
