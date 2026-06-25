package com.music.user.event;

// Mirror of payment-service's event payload (deserialized from the premium-upgrade channel).
public record PremiumUpgradeEvent(Long userId, int durationDays, String txnRef) {
}
