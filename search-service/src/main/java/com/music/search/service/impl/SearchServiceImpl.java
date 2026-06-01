package com.music.search.service.impl;

import com.music.common.dto.PageResponse;
import com.music.search.dto.request.SongIndexRequest;
import com.music.search.dto.response.SongSearchResponse;
import com.music.search.mapper.SearchMapper;
import com.music.search.repository.SongSearchRepository;
import com.music.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SongSearchRepository songSearchRepository;
    private final SearchMapper searchMapper;

    @Override
    public PageResponse<SongSearchResponse> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        Page<?> result = StringUtils.hasText(query)
                ? songSearchRepository.searchByKeyword(query, pageable)
                : songSearchRepository.findAll(pageable);
        @SuppressWarnings("unchecked")
        Page<com.music.search.document.SongDocument> docs =
                (Page<com.music.search.document.SongDocument>) result;
        return PageResponse.of(docs, searchMapper::toResponse);
    }

    @Override
    public void index(SongIndexRequest request) {
        songSearchRepository.save(searchMapper.toDocument(request));
    }

    @Override
    public void delete(Long id) {
        songSearchRepository.deleteById(id);
    }
}
