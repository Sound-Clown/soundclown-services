package com.music.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PremiumStatusResponse {
    private boolean premium;          // true if premiumUntil is in the future
    private LocalDateTime premiumUntil;
}
