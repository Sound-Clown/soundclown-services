package com.music.auth.service.impl;

import com.music.auth.dto.request.ChangePasswordRequest;
import com.music.auth.dto.request.ForgotPasswordRequest;
import com.music.auth.dto.request.LoginRequest;
import com.music.auth.dto.request.RegisterRequest;
import com.music.auth.dto.request.ResetPasswordRequest;
import com.music.auth.client.UserProfileSyncRequest;
import com.music.auth.client.UserSyncClient;
import com.music.auth.dto.response.AuthResponse;
import com.music.auth.dto.response.UserInfoDto;
import com.music.auth.entity.PasswordResetToken;
import com.music.auth.entity.User;
import com.music.auth.event.ResetEmailPublisher;
import com.music.auth.mapper.UserMapper;
import com.music.auth.repository.PasswordResetTokenRepository;
import com.music.auth.repository.UserRepository;
import com.music.auth.service.AuthService;
import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.common.security.CurrentUserProvider;
import com.music.common.security.JwtUtil;
import com.music.common.security.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;
    private final UserSyncClient userSyncClient;
    private final ResetEmailPublisher resetEmailPublisher;

    @Value("${password-reset.token-ttl-minutes:30}")
    private long resetTokenTtlMinutes;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.DUPLICATE_USERNAME);
        }

        Role role = (request.getRole() == null || request.getRole() == Role.ADMIN)
                ? Role.LISTENER
                : request.getRole();

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();
        userRepository.save(user);
        syncProfile(user);

        return buildAuthResponse(user);
    }

    @Override
    public void setActive(Long id, boolean active) {
        User user = loadUser(id);
        user.setActive(active);
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByUsernameOrEmail(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (!user.isActive()) {
            throw new AppException(ErrorCode.AUTH_ACCOUNT_LOCKED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDto getCurrentUser() {
        return userMapper.toUserInfo(loadUser(currentUserProvider.getCurrentUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDto getUserById(Long id) {
        return userMapper.toUserInfo(loadUser(id));
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = loadUser(currentUserProvider.getCurrentUserId());
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.AUTH_OLD_PASSWORD_INCORRECT);
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Không tiết lộ email có tồn tại hay không (chống user enumeration) — luôn trả
        // thành công.
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userId(user.getId())
                    .token(UUID.randomUUID().toString())
                    .expiresAt(LocalDateTime.now().plusMinutes(resetTokenTtlMinutes))
                    .createdAt(LocalDateTime.now())
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // Hand off to notification-service asynchronously via Redis Pub/Sub.
            resetEmailPublisher.publish(user.getEmail(), resetToken.getToken());
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_RESET_TOKEN_INVALID));
        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new AppException(ErrorCode.AUTH_RESET_TOKEN_INVALID);
        }

        User user = loadUser(resetToken.getUserId());
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    private User loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // Best-effort mirror to user-service. Registration must not fail if user-service is down, the profile can be re-synced later (an event bus would make this reliable — Phase 3).
    private void syncProfile(User user) {
        try {
            userSyncClient.upsert(UserProfileSyncRequest.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .active(user.isActive())
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to sync profile to user-service for userId={}", user.getId(), ex);
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        return AuthResponse.builder()
                .accessToken(token)
                .user(userMapper.toUserInfo(user))
                .build();
    }
}
