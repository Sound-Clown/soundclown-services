package com.music.song.service.impl;

import com.music.common.dto.PageResponse;
import com.music.song.dto.response.ArtistResponse;
import com.music.song.repository.ArtistQueryRepository;
import com.music.song.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistServiceImpl implements ArtistService {

    private final ArtistQueryRepository artistQueryRepository;

    @Override
    public PageResponse<ArtistResponse> listArtists(int page, int size, String sortBy, String sortDir) {
        int offset = Math.max(0, page - 1) * size;
        List<ArtistResponse> content = artistQueryRepository.list(offset, size, sortBy, sortDir);
        long total = artistQueryRepository.countArtists();
        return PageResponse.<ArtistResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / Math.max(1, size)))
                .build();
    }
}
