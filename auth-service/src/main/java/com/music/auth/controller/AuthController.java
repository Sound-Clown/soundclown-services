package com.music.auth.controller;

import com.music.auth.dto.request.ChangePasswordRequest;
import com.music.auth.dto.request.ForgotPasswordRequest;
import com.music.auth.dto.request.LoginRequest;
import com.music.auth.dto.request.RegisterRequest;
import com.music.auth.dto.request.ResetPasswordRequest;
import com.music.auth.dto.response.AuthResponse;
import com.music.auth.dto.response.UserInfoDto;
import com.music.auth.service.AuthService;
import com.music.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.login(request))
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoDto> me() {
        return ApiResponse.<UserInfoDto>builder()
                .result(authService.getCurrentUser())
                .build();
    }

    // Service-to-service lookup (vd. song-service denormalize artistUsername)
    @GetMapping("/users/{id}")
    public ApiResponse<UserInfoDto> getUserById(@PathVariable Long id) {
        return ApiResponse.<UserInfoDto>builder()
                .result(authService.getUserById(id))
                .build();
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.<Void>builder().build();
    }
}
