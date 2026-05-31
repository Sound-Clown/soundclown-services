package com.music.auth.controller;

import com.music.auth.service.AuthService;
import com.music.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Internal, service-to-service only — not routed by the API Gateway.
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalAuthController {

    private final AuthService authService;

    @PutMapping("/{id}/lock")
    public ApiResponse<Void> setActive(@PathVariable Long id, @RequestParam boolean active) {
        authService.setActive(id, active);
        return ApiResponse.<Void>builder().build();
    }
}
