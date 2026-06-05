package com.music.song.service;

import com.music.common.dto.PageResponse;
import com.music.song.dto.response.ArtistResponse;

public interface ArtistService {

    PageResponse<ArtistResponse> listArtists(int page, int size, String sortBy, String sortDir);
}
