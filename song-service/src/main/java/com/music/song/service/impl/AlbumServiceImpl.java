package com.music.song.service.impl;

import com.music.common.dto.PageResponse;
import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.common.security.CurrentUserProvider;
import com.music.common.security.Role;
import com.music.song.dto.request.CreateAlbumRequest;
import com.music.song.dto.request.UpdateAlbumRequest;
import com.music.song.dto.response.AlbumDetailResponse;
import com.music.song.dto.response.AlbumResponse;
import com.music.song.entity.Album;
import com.music.song.entity.Song;
import com.music.song.entity.SongStatus;
import com.music.song.mapper.AlbumMapper;
import com.music.song.repository.AlbumRepository;
import com.music.song.repository.SongRepository;
import com.music.song.service.AlbumService;
import com.music.song.service.Pageables;
import com.music.song.service.SongResponseAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final AlbumMapper albumMapper;
    private final SongResponseAssembler songResponseAssembler;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlbumResponse> getMyAlbums(int page, int size, String sortBy, String sortDir) {
        Long artistId = currentUserProvider.getCurrentUserId();
        Pageable pageable = Pageables.of(page, size, sortBy, sortDir);
        return PageResponse.of(albumRepository.findByArtistId(artistId, pageable), albumMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumDetailResponse getAlbumDetail(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));
        var songs = songResponseAssembler.toResponses(
                songRepository.findByAlbumIdAndStatusOrderByCreatedAtDesc(id, SongStatus.APPROVED));
        return albumMapper.toDetailResponse(album, songs);
    }

    @Override
    public AlbumResponse createAlbum(CreateAlbumRequest request) {
        Album album = Album.builder()
                .name(request.getName())
                .coverImage(request.getCoverImage())
                .artistId(currentUserProvider.getCurrentUserId())
                .artistUsername(currentUserProvider.getCurrentUsername())
                .build();
        albumRepository.save(album);
        return albumMapper.toResponse(album);
    }

    @Override
    public AlbumResponse updateAlbum(Long id, UpdateAlbumRequest request) {
        Album album = loadOwnedAlbum(id);
        if (request.getName() != null) {
            album.setName(request.getName());
        }
        if (request.getCoverImage() != null) {
            album.setCoverImage(request.getCoverImage());
        }
        albumRepository.save(album);
        return albumMapper.toResponse(album);
    }

    @Override
    public void deleteAlbum(Long id) {
        Album album = loadOwnedAlbum(id);
        songRepository.clearAlbum(id);
        albumRepository.delete(album);
    }

    @Override
    public void addSongToAlbum(Long albumId, Long songId) {
        loadOwnedAlbum(albumId);
        Song song = loadOwnedSong(songId);
        song.setAlbumId(albumId);
        songRepository.save(song);
    }

    @Override
    public void removeSongFromAlbum(Long albumId, Long songId) {
        loadOwnedAlbum(albumId);
        Song song = loadOwnedSong(songId);
        if (albumId.equals(song.getAlbumId())) {
            song.setAlbumId(null);
            songRepository.save(song);
        }
    }

    private Album loadOwnedAlbum(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));
        if (currentUserProvider.getCurrentRole() != Role.ADMIN
                && !album.getArtistId().equals(currentUserProvider.getCurrentUserId())) {
            throw new AppException(ErrorCode.ALBUM_NOT_OWNED);
        }
        return album;
    }

    private Song loadOwnedSong(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
        if (currentUserProvider.getCurrentRole() != Role.ADMIN
                && !song.getArtistId().equals(currentUserProvider.getCurrentUserId())) {
            throw new AppException(ErrorCode.SONG_NOT_OWNED);
        }
        return song;
    }
}
