package com.music.user.controller;

import com.music.common.dto.ApiResponse;
import com.music.user.dto.request.UpsertProfileRequest;
import com.music.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Internal, service-to-service only — not routed by the API Gateway.
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<Void> upsert(@RequestBody UpsertProfileRequest request) {
        userService.upsertProfile(request);
        return ApiResponse.<Void>builder().build();
    }
}
