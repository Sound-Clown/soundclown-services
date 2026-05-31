package com.music.song.service;

import com.music.common.dto.PageResponse;
import com.music.song.dto.request.CreateSongRequest;
import com.music.song.dto.request.UpdateSongRequest;
import com.music.song.dto.response.LikeResponse;
import com.music.song.dto.response.SongResponse;
import com.music.song.dto.response.StatsResponse;

public interface SongService {

    PageResponse<SongResponse> getApprovedSongs(int page, int size, String sortBy, String sortDir);

    SongResponse getSongById(Long id);

    void play(Long id);

    LikeResponse toggleLike(Long id);

    SongResponse createSong(CreateSongRequest request);

    SongResponse updateSong(Long id, UpdateSongRequest request);

    void deleteSong(Long id);

    PageResponse<SongResponse> getMySongs(int page, int size, String sortBy, String sortDir);

    StatsResponse getMyStats();
}
