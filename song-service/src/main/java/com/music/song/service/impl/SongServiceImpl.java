package com.music.song.service.impl;

import com.music.common.dto.PageResponse;
import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import com.music.common.security.AuthPrincipal;
import com.music.common.security.CurrentUserProvider;
import com.music.common.security.Role;
import com.music.song.dto.request.CreateSongRequest;
import com.music.song.dto.request.UpdateSongRequest;
import com.music.song.dto.response.LikeResponse;
import com.music.song.dto.response.SongResponse;
import com.music.song.dto.response.StatsResponse;
import com.music.song.entity.Album;
import com.music.song.entity.Like;
import com.music.song.entity.LikeId;
import com.music.song.entity.Song;
import com.music.song.entity.SongStatus;
import com.music.song.repository.AlbumRepository;
import com.music.song.repository.LikeRepository;
import com.music.song.repository.SongRepository;
import com.music.song.service.Pageables;
import com.music.song.service.SongResponseAssembler;
import com.music.song.service.SongSearchIndexer;
import com.music.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final LikeRepository likeRepository;
    private final SongResponseAssembler assembler;
    private final CurrentUserProvider currentUserProvider;
    private final SongSearchIndexer searchIndexer;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SongResponse> getApprovedSongs(int page, int size, String sortBy, String sortDir, String artist) {
        Pageable pageable = Pageables.of(page, size, sortBy, sortDir);
        Page<Song> songs = (artist == null || artist.isBlank())
                ? songRepository.findByStatus(SongStatus.APPROVED, pageable)
                : songRepository.findByStatusAndArtistUsername(SongStatus.APPROVED, artist, pageable);
        return assembler.toPageResponse(songs);
    }

    @Override
    @Transactional(readOnly = true)
    public SongResponse getSongById(Long id) {
        return assembler.toResponse(loadSong(id));
    }

    @Override
    public void play(Long id) {
        if (!songRepository.existsById(id)) {
            throw new AppException(ErrorCode.SONG_NOT_FOUND);
        }
        songRepository.incrementPlayCount(id);
    }

    @Override
    public LikeResponse toggleLike(Long id) {
        Song song = loadSong(id);
        Long userId = currentUserProvider.getCurrentUserId();
        LikeId likeId = new LikeId(userId, id);

        boolean liked;
        if (likeRepository.existsById(likeId)) {
            likeRepository.deleteById(likeId);
            songRepository.decrementLikeCount(id);
            liked = false;
        } else {
            likeRepository.save(Like.builder().id(likeId).build());
            songRepository.incrementLikeCount(id);
            liked = true;
        }
        int newLikeCount = liked ? song.getLikeCount() + 1 : Math.max(0, song.getLikeCount() - 1);
        return LikeResponse.builder().liked(liked).likeCount(newLikeCount).build();
    }

    @Override
    public SongResponse createSong(CreateSongRequest request) {
        AuthPrincipal me = currentUserProvider.getCurrentUser();
        if (request.getAlbumId() != null) {
            requireOwnedAlbum(request.getAlbumId(), me.userId());
        }
        Song song = Song.builder()
                .title(request.getTitle())
                .audioFile(request.getAudioFile())
                .coverImage(request.getCoverImage())
                .artistId(me.userId())
                .artistUsername(me.username())
                .albumId(request.getAlbumId())
                .status(SongStatus.PENDING)
                .build();
        songRepository.save(song);
        return assembler.toResponse(song);
    }

    @Override
    public SongResponse updateSong(Long id, UpdateSongRequest request) {
        Song song = loadSong(id);
        ensureOwnsSong(song);

        if (request.getTitle() != null) {
            song.setTitle(request.getTitle());
        }
        if (request.getCoverImage() != null) {
            song.setCoverImage(request.getCoverImage());
        }
        if (request.getAlbumId() != null) {
            requireOwnedAlbum(request.getAlbumId(), song.getArtistId());
            song.setAlbumId(request.getAlbumId());
        }
        songRepository.save(song);
        if (song.getStatus() == SongStatus.APPROVED) {
            searchIndexer.index(song);
        }
        return assembler.toResponse(song);
    }

    @Override
    public void deleteSong(Long id) {
        Song song = loadSong(id);
        ensureOwnsSong(song);
        likeRepository.deleteBySongId(id);
        songRepository.delete(song);
        searchIndexer.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SongResponse> getMySongs(int page, int size, String sortBy, String sortDir) {
        Long artistId = currentUserProvider.getCurrentUserId();
        Pageable pageable = Pageables.of(page, size, sortBy, sortDir);
        return assembler.toPageResponse(songRepository.findByArtistId(artistId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SongResponse> getLikedSongs(int page, int size, String sortBy, String sortDir) {
        Long userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = Pageables.of(page, size, sortBy, sortDir);
        return assembler.toPageResponse(
                songRepository.findLikedSongs(userId, SongStatus.APPROVED, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public StatsResponse getMyStats() {
        Long artistId = currentUserProvider.getCurrentUserId();
        return StatsResponse.builder()
                .totalSongs(songRepository.countByArtistId(artistId))
                .approvedSongs(songRepository.countByArtistIdAndStatus(artistId, SongStatus.APPROVED))
                .pendingSongs(songRepository.countByArtistIdAndStatus(artistId, SongStatus.PENDING))
                .totalPlays(songRepository.sumPlayCountByArtistId(artistId))
                .totalLikes(songRepository.sumLikeCountByArtistId(artistId))
                .build();
    }

    private Song loadSong(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND));
    }

    private void ensureOwnsSong(Song song) {
        if (currentUserProvider.getCurrentRole() == Role.ADMIN) {
            return;
        }
        if (!song.getArtistId().equals(currentUserProvider.getCurrentUserId())) {
            throw new AppException(ErrorCode.SONG_NOT_OWNED);
        }
    }

    private void requireOwnedAlbum(Long albumId, Long artistId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new AppException(ErrorCode.ALBUM_NOT_FOUND));
        if (!album.getArtistId().equals(artistId)) {
            throw new AppException(ErrorCode.ALBUM_NOT_OWNED);
        }
    }
}
