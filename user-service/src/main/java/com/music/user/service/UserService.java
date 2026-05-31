package com.music.user.service;

import com.music.common.dto.PageResponse;
import com.music.user.dto.request.UpsertProfileRequest;
import com.music.user.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long id);

    PageResponse<UserProfileResponse> listUsers(int page, int size, String sortBy, String sortDir);

    UserProfileResponse toggleLock(Long id);

    void upsertProfile(UpsertProfileRequest request);
}
