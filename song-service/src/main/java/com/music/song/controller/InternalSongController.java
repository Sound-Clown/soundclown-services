package com.music.song.controller;

import com.music.common.dto.ApiResponse;
import com.music.song.service.AdminSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Internal, service-to-service / ops only — not routed by the API Gateway.
@RestController
@RequestMapping("/internal/songs")
@RequiredArgsConstructor
public class InternalSongController {

    private final AdminSongService adminSongService;

    // Pushes every APPROVED song into search-service. Useful after bulk-seeding songs
    // straight into the DB (which bypasses the index-on-approve flow). Idempotent.
    @PostMapping("/reindex")
    public ApiResponse<Long> reindex() {
        return ApiResponse.<Long>builder()
                .result(adminSongService.reindexApprovedSongs())
                .build();
    }
}
