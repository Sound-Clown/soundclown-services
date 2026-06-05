package com.music.song.controller;

import com.music.common.dto.ApiResponse;
import com.music.common.dto.PageResponse;
import com.music.song.dto.response.ArtistResponse;
import com.music.song.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Public: browse artists (derived from the approved-songs catalog by artist name).
@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    public ApiResponse<PageResponse<ArtistResponse>> listArtists(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "totalPlays") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<PageResponse<ArtistResponse>>builder()
                .result(artistService.listArtists(page, size, sortBy, sortDir))
                .build();
    }
}
