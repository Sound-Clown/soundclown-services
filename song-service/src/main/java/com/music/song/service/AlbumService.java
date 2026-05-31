package com.music.song.service;

import com.music.common.dto.PageResponse;
import com.music.song.dto.request.CreateAlbumRequest;
import com.music.song.dto.request.UpdateAlbumRequest;
import com.music.song.dto.response.AlbumResponse;

public interface AlbumService {

    PageResponse<AlbumResponse> getMyAlbums(int page, int size, String sortBy, String sortDir);

    AlbumResponse createAlbum(CreateAlbumRequest request);

    AlbumResponse updateAlbum(Long id, UpdateAlbumRequest request);

    void deleteAlbum(Long id);

    void addSongToAlbum(Long albumId, Long songId);

    void removeSongFromAlbum(Long albumId, Long songId);
}
