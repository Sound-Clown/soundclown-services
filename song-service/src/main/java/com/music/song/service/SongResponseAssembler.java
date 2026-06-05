package com.music.song.service;

import com.music.common.dto.PageResponse;
import com.music.common.security.CurrentUserProvider;
import com.music.song.dto.response.SongResponse;
import com.music.song.entity.Album;
import com.music.song.entity.LikeId;
import com.music.song.entity.Song;
import com.music.song.mapper.SongMapper;
import com.music.song.repository.AlbumRepository;
import com.music.song.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// Builds SongResponse objects, resolving albumName and the current user's "liked" flag
// in batch to avoid N+1 queries when assembling a page.
@Component
@RequiredArgsConstructor
public class SongResponseAssembler {

    private final AlbumRepository albumRepository;
    private final LikeRepository likeRepository;
    private final SongMapper songMapper;
    private final CurrentUserProvider currentUserProvider;

    public SongResponse toResponse(Song song) {
        String albumName = song.getAlbumId() == null ? null
                : albumRepository.findById(song.getAlbumId()).map(Album::getName).orElse(null);
        Long userId = currentUserProvider.getCurrentUserIdOrNull();
        boolean liked = userId != null
                && likeRepository.existsById(new LikeId(userId, song.getId()));
        return songMapper.toResponse(song, albumName, liked);
    }

    // Batch-assemble a list of songs (albumName + liked resolved once for the whole list).
    public List<SongResponse> toResponses(List<Song> songs) {
        Map<Long, String> albumNames = resolveAlbumNames(songs);
        Set<Long> likedSongIds = resolveLikedSongIds(songs);
        return songs.stream()
                .map(s -> songMapper.toResponse(
                        s,
                        s.getAlbumId() == null ? null : albumNames.get(s.getAlbumId()),
                        likedSongIds.contains(s.getId())))
                .toList();
    }

    public PageResponse<SongResponse> toPageResponse(Page<Song> page) {
        return PageResponse.<SongResponse>builder()
                .content(toResponses(page.getContent()))
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private Map<Long, String> resolveAlbumNames(List<Song> songs) {
        Set<Long> albumIds = songs.stream()
                .map(Song::getAlbumId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (albumIds.isEmpty()) {
            return Map.of();
        }
        return albumRepository.findAllById(albumIds).stream()
                .collect(Collectors.toMap(Album::getId, Album::getName));
    }

    private Set<Long> resolveLikedSongIds(List<Song> songs) {
        Long userId = currentUserProvider.getCurrentUserIdOrNull();
        if (userId == null || songs.isEmpty()) {
            return Set.of();
        }
        List<Long> songIds = songs.stream().map(Song::getId).toList();
        return new HashSet<>(likeRepository.findLikedSongIds(userId, songIds));
    }
}
