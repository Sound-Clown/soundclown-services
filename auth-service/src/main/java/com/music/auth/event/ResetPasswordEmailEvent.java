package com.music.auth.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Published over Redis Pub/Sub; consumed by notification-service.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordEmailEvent {
    private String email;
    private String token;
}
