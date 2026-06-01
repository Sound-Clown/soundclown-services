package com.music.notification.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cross-service contract published by auth-service over Redis Pub/Sub.
@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordEmailEvent {
    private String email;
    private String token;
}
