package com.music.search.controller;

import com.music.common.dto.ApiResponse;
import com.music.search.dto.request.SongIndexRequest;
import com.music.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Internal, service-to-service only — not routed by the API Gateway.
@RestController
@RequestMapping("/internal/search/songs")
@RequiredArgsConstructor
public class InternalSearchController {

    private final SearchService searchService;

    @PostMapping
    public ApiResponse<Void> index(@RequestBody SongIndexRequest request) {
        searchService.index(request);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        searchService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
