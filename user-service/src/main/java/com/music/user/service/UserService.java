package com.music.user.service;

import com.music.common.dto.PageResponse;
import com.music.user.dto.request.UpsertProfileRequest;
import com.music.user.dto.response.PremiumStatusResponse;
import com.music.user.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long id);

    PageResponse<UserProfileResponse> listUsers(int page, int size, String sortBy, String sortDir);

    UserProfileResponse toggleLock(Long id);

    void upsertProfile(UpsertProfileRequest request);

    // Extends the user's premium by durationDays (stacks on remaining time). Idempotency is
    // the caller's concern (payment-service finalizes a paid order only once).
    void activatePremium(Long userId, int durationDays);

    PremiumStatusResponse getPremiumStatus(Long id);
}
