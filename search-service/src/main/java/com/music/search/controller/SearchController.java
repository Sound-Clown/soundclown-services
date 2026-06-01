package com.music.search.controller;

import com.music.common.dto.ApiResponse;
import com.music.common.dto.PageResponse;
import com.music.search.dto.response.SongSearchResponse;
import com.music.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ApiResponse<PageResponse<SongSearchResponse>> search(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<SongSearchResponse>>builder()
                .result(searchService.search(query, page, size))
                .build();
    }
}
