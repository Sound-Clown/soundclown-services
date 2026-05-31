package com.music.song.controller;

import com.music.common.dto.ApiResponse;
import com.music.common.dto.PageResponse;
import com.music.song.dto.request.CreateAlbumRequest;
import com.music.song.dto.request.UpdateAlbumRequest;
import com.music.song.dto.response.AlbumResponse;
import com.music.song.service.AlbumService;
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
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ARTIST')")
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping("/my")
    public ApiResponse<PageResponse<AlbumResponse>> getMyAlbums(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<PageResponse<AlbumResponse>>builder()
                .result(albumService.getMyAlbums(page, size, sortBy, sortDir))
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AlbumResponse> createAlbum(@Valid @RequestBody CreateAlbumRequest request) {
        return ApiResponse.<AlbumResponse>builder()
                .result(albumService.createAlbum(request))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<AlbumResponse> updateAlbum(
            @PathVariable Long id,
            @RequestBody UpdateAlbumRequest request) {
        return ApiResponse.<AlbumResponse>builder()
                .result(albumService.updateAlbum(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/{albumId}/songs")
    public ApiResponse<Void> addSongToAlbum(
            @PathVariable Long albumId,
            @RequestParam Long songId) {
        albumService.addSongToAlbum(albumId, songId);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/{albumId}/songs/{songId}")
    public ApiResponse<Void> removeSongFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long songId) {
        albumService.removeSongFromAlbum(albumId, songId);
        return ApiResponse.<Void>builder().build();
    }
}
