package com.music.search.service.impl;

import com.music.common.dto.PageResponse;
import com.music.search.dto.request.SongIndexRequest;
import com.music.search.dto.response.SongSearchResponse;
import com.music.search.mapper.SearchMapper;
import com.music.search.repository.SongSearchRepository;
import com.music.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
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
        try {
            songSearchRepository.save(searchMapper.toDocument(request));
            log.info("Indexed song id={} title='{}' artist='{}'",
                    request.getId(), request.getTitle(), request.getArtistUsername());
        } catch (Exception ex) {
            log.error("Failed to index song id={} title='{}': {}",
                    request.getId(), request.getTitle(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void delete(Long id) {
        try {
            songSearchRepository.deleteById(id);
            log.info("Removed song id={} from index", id);
        } catch (Exception ex) {
            log.error("Failed to remove song id={} from index: {}", id, ex.getMessage(), ex);
            throw ex;
        }
    }
}
