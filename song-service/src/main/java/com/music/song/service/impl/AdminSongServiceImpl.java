package com.music.song.service.impl;

import com.music.common.dto.PageResponse;
import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.song.dto.request.ReviewSongRequest;
import com.music.song.dto.response.SongResponse;
import com.music.song.entity.Song;
import com.music.song.entity.SongStatus;
import com.music.song.repository.SongRepository;
import com.music.song.service.AdminSongService;
import com.music.song.service.Pageables;
import com.music.song.service.SongResponseAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSongServiceImpl implements AdminSongService {

    private final SongRepository songRepository;
    private final SongResponseAssembler assembler;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SongResponse> getPendingSongs(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = Pageables.of(page, size, sortBy, sortDir);
        return assembler.toPageResponse(songRepository.findByStatus(SongStatus.PENDING, pageable));
    }

    @Override
    public SongResponse reviewSong(Long id, ReviewSongRequest request) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));

        if (Boolean.TRUE.equals(request.getApproved())) {
            song.setStatus(SongStatus.APPROVED);
            song.setRejectReason(null);
        } else {
            song.setStatus(SongStatus.REJECTED);
            song.setRejectReason(request.getRejectReason());
        }
        songRepository.save(song);

        // TODO(search-service): index/refresh this song in Elasticsearch on approval (Phase 2).
        return assembler.toResponse(song);
    }
}
