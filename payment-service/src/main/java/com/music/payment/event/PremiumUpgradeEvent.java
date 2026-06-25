package com.music.payment.event;

// Published to Redis when a payment succeeds; user-service grants premium, notification can email.
public record PremiumUpgradeEvent(Long userId, int durationDays, String txnRef) {
}
