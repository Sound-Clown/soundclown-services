package com.music.user.config;

import com.music.user.listener.PremiumUpgradeListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    public static final String PREMIUM_UPGRADE_CHANNEL = "premium-upgrade";

    @Bean
    public ChannelTopic premiumUpgradeTopic() {
        return new ChannelTopic(PREMIUM_UPGRADE_CHANNEL);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            PremiumUpgradeListener premiumUpgradeListener,
            ChannelTopic premiumUpgradeTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(premiumUpgradeListener, premiumUpgradeTopic);
        return container;
    }
}
