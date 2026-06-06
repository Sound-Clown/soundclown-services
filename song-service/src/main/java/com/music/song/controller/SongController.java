package com.music.song.controller;

import com.music.common.dto.ApiResponse;
import com.music.common.dto.PageResponse;
import com.music.song.dto.request.CreateSongRequest;
import com.music.song.dto.request.ReviewSongRequest;
import com.music.song.dto.request.UpdateSongRequest;
import com.music.song.dto.response.LikeResponse;
import com.music.song.dto.response.SongResponse;
import com.music.song.dto.response.StatsResponse;
import com.music.song.service.AdminSongService;
import com.music.song.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final AdminSongService adminSongService;

    @GetMapping
    public ApiResponse<PageResponse<SongResponse>> getApprovedSongs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String artist) {
        return ApiResponse.<PageResponse<SongResponse>>builder()
                .result(songService.getApprovedSongs(page, size, sortBy, sortDir, artist))
                .build();
    }

    // Bài user hiện tại đã like (mọi role có token). Auth enforced trong service qua CurrentUserProvider.
    @GetMapping("/liked")
    public ApiResponse<PageResponse<SongResponse>> getLikedSongs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<PageResponse<SongResponse>>builder()
                .result(songService.getLikedSongs(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ARTIST')")
    public ApiResponse<PageResponse<SongResponse>> getMySongs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<PageResponse<SongResponse>>builder()
                .result(songService.getMySongs(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/my/stats")
    @PreAuthorize("hasRole('ARTIST')")
    public ApiResponse<StatsResponse> getMyStats() {
        return ApiResponse.<StatsResponse>builder()
                .result(songService.getMyStats())
                .build();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<SongResponse>> getPendingSongs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<PageResponse<SongResponse>>builder()
                .result(adminSongService.getPendingSongs(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SongResponse> getSongById(@PathVariable Long id) {
        return ApiResponse.<SongResponse>builder()
                .result(songService.getSongById(id))
                .build();
    }

    @PostMapping("/{id}/play")
    public ApiResponse<Void> play(@PathVariable Long id) {
        songService.play(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/{id}/like")
    public ApiResponse<LikeResponse> toggleLike(@PathVariable Long id) {
        return ApiResponse.<LikeResponse>builder()
                .result(songService.toggleLike(id))
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ARTIST')")
    public ApiResponse<SongResponse> createSong(@Valid @RequestBody CreateSongRequest request) {
        return ApiResponse.<SongResponse>builder()
                .result(songService.createSong(request))
                .build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ARTIST')")
    public ApiResponse<SongResponse> updateSong(
            @PathVariable Long id,
            @RequestBody UpdateSongRequest request) {
        return ApiResponse.<SongResponse>builder()
                .result(songService.updateSong(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ARTIST')")
    public ApiResponse<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SongResponse> reviewSong(
            @PathVariable Long id,
            @Valid @RequestBody ReviewSongRequest request) {
        return ApiResponse.<SongResponse>builder()
                .result(adminSongService.reviewSong(id, request))
                .build();
    }
}
