package com.music.search.service;

import com.music.common.dto.PageResponse;
import com.music.search.dto.request.SongIndexRequest;
import com.music.search.dto.response.SongSearchResponse;

public interface SearchService {

    PageResponse<SongSearchResponse> search(String query, int page, int size);

    void index(SongIndexRequest request);

    void delete(Long id);
}
