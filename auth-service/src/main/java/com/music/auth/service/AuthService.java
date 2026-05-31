package com.music.auth.service;

import com.music.auth.dto.request.ChangePasswordRequest;
import com.music.auth.dto.request.ForgotPasswordRequest;
import com.music.auth.dto.request.LoginRequest;
import com.music.auth.dto.request.RegisterRequest;
import com.music.auth.dto.request.ResetPasswordRequest;
import com.music.auth.dto.response.AuthResponse;
import com.music.auth.dto.response.UserInfoDto;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserInfoDto getCurrentUser();

    UserInfoDto getUserById(Long id);

    void changePassword(ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
