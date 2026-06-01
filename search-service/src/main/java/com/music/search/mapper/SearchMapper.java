package com.music.search.mapper;

import com.music.search.document.SongDocument;
import com.music.search.dto.request.SongIndexRequest;
import com.music.search.dto.response.SongSearchResponse;
import org.springframework.stereotype.Component;

@Component
public class SearchMapper {

    public SongDocument toDocument(SongIndexRequest request) {
        return SongDocument.builder()
                .id(request.getId())
                .title(request.getTitle())
                .artistUsername(request.getArtistUsername())
                .albumName(request.getAlbumName())
                .coverImage(request.getCoverImage())
                .playCount(request.getPlayCount())
                .likeCount(request.getLikeCount())
                .createdAt(request.getCreatedAt())
                .build();
    }

    public SongSearchResponse toResponse(SongDocument document) {
        return SongSearchResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .artistUsername(document.getArtistUsername())
                .albumName(document.getAlbumName())
                .coverImage(document.getCoverImage())
                .playCount(document.getPlayCount())
                .likeCount(document.getLikeCount())
                .build();
    }
}
