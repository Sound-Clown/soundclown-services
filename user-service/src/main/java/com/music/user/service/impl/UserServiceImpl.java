package com.music.user.service.impl;

import com.music.common.dto.PageResponse;
import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.common.security.CurrentUserProvider;
import com.music.user.client.AuthLockClient;
import com.music.user.dto.request.UpsertProfileRequest;
import com.music.user.dto.response.UserProfileResponse;
import com.music.user.entity.UserProfile;
import com.music.user.mapper.UserProfileMapper;
import com.music.user.repository.UserProfileRepository;
import com.music.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final CurrentUserProvider currentUserProvider;
    private final AuthLockClient authLockClient;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long id) {
        return userProfileMapper.toResponse(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserProfileResponse> listUsers(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(direction, sortBy));
        return PageResponse.of(userProfileRepository.findAll(pageable), userProfileMapper::toResponse);
    }

    @Override
    public UserProfileResponse toggleLock(Long id) {
        if (id.equals(currentUserProvider.getCurrentUserId())) {
            throw new AppException(ErrorCode.CANNOT_LOCK_SELF);
        }
        UserProfile profile = load(id);
        boolean newActive = !profile.isActive();

        // auth-service is the source of truth for login; update it first, then mirror locally.
        try {
            authLockClient.setActive(id, newActive);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.USER_LOCK_SYNC_FAILED);
        }
        profile.setActive(newActive);
        userProfileRepository.save(profile);
        return userProfileMapper.toResponse(profile);
    }

    @Override
    public void upsertProfile(UpsertProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(request.getId())
                .orElseGet(UserProfile::new);
        profile.setId(request.getId());
        profile.setUsername(request.getUsername());
        profile.setEmail(request.getEmail());
        profile.setRole(request.getRole());
        profile.setActive(request.isActive());
        userProfileRepository.save(profile);
    }

    private UserProfile load(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
