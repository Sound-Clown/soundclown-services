package com.music.song.client;

import lombok.Getter;
import lombok.Setter;

// Subset of user-service's premium-status response needed to gate playback.
@Getter
@Setter
public class PremiumStatus {
    private boolean premium;
}
