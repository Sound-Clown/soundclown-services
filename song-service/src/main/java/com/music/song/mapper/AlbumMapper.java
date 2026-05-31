package com.music.song.mapper;

import com.music.song.dto.response.AlbumResponse;
import com.music.song.entity.Album;
import org.springframework.stereotype.Component;

@Component
public class AlbumMapper {

    public AlbumResponse toResponse(Album album) {
        return AlbumResponse.builder()
                .id(album.getId())
                .name(album.getName())
                .coverImage(album.getCoverImage())
                .artistId(album.getArtistId())
                .createdAt(album.getCreatedAt())
                .build();
    }
}
