package com.music.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimiterConfig {

    // Rate-limit per client IP. Referenced from application.yml as #{@ipKeyResolver}.
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
            String key = remote != null ? remote.getAddress().getHostAddress() : "unknown";
            return Mono.just(key);
        };
    }
}
