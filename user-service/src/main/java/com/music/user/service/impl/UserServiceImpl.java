package com.music.user.service.impl;

import com.music.common.dto.PageResponse;
import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.common.security.CurrentUserProvider;
import com.music.user.client.AuthLockClient;
import com.music.user.dto.request.UpsertProfileRequest;
import com.music.user.dto.response.PremiumStatusResponse;
import com.music.user.dto.response.UserProfileResponse;
import com.music.user.entity.UserProfile;
import com.music.user.mapper.UserProfileMapper;
import com.music.user.repository.UserProfileRepository;
import com.music.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
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
    public void activatePremium(Long userId, int durationDays) {
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        if (profile == null) {
            // Profile should exist (synced at register); log and skip rather than fail the event.
            log.warn("activatePremium: no profile for user {} — skipping", userId);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        // Stack on remaining premium if still active, otherwise start from now.
        LocalDateTime base = (profile.getPremiumUntil() != null && profile.getPremiumUntil().isAfter(now))
                ? profile.getPremiumUntil()
                : now;
        profile.setPremiumUntil(base.plusDays(durationDays));
        userProfileRepository.save(profile);
        log.info("Premium activated for user {} until {}", userId, profile.getPremiumUntil());
    }

    @Override
    @Transactional(readOnly = true)
    public PremiumStatusResponse getPremiumStatus(Long id) {
        LocalDateTime premiumUntil = load(id).getPremiumUntil();
        boolean premium = premiumUntil != null && premiumUntil.isAfter(LocalDateTime.now());
        return PremiumStatusResponse.builder()
                .premium(premium)
                .premiumUntil(premiumUntil)
                .build();
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
