package com.music.song.service;

import com.music.common.dto.PageResponse;
import com.music.song.dto.request.ReviewSongRequest;
import com.music.song.dto.response.SongResponse;

public interface AdminSongService {

    PageResponse<SongResponse> getPendingSongs(int page, int size, String sortBy, String sortDir);

    SongResponse reviewSong(Long id, ReviewSongRequest request);
}
