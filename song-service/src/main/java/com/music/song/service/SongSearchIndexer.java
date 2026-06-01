package com.music.song.service;

import com.music.song.client.SearchClient;
import com.music.song.client.SongIndexRequest;
import com.music.song.entity.Album;
import com.music.song.entity.Song;
import com.music.song.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

// Keeps the search index in sync with song lifecycle. Best-effort: a search-service
// outage must not break song operations (the index can be rebuilt later).
@Slf4j
@Component
@RequiredArgsConstructor
public class SongSearchIndexer {

    private final SearchClient searchClient;
    private final AlbumRepository albumRepository;

    public void index(Song song) {
        try {
            String albumName = song.getAlbumId() == null ? null
                    : albumRepository.findById(song.getAlbumId()).map(Album::getName).orElse(null);
            Long createdAt = song.getCreatedAt() == null ? null
                    : song.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli();
            searchClient.index(SongIndexRequest.builder()
                    .id(song.getId())
                    .title(song.getTitle())
                    .artistUsername(song.getArtistUsername())
                    .albumName(albumName)
                    .coverImage(song.getCoverImage())
                    .playCount(song.getPlayCount())
                    .likeCount(song.getLikeCount())
                    .createdAt(createdAt)
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to index song {} in search-service", song.getId(), ex);
        }
    }

    public void delete(Long songId) {
        try {
            searchClient.delete(songId);
        } catch (Exception ex) {
            log.warn("Failed to remove song {} from search-service", songId, ex);
        }
    }
}
