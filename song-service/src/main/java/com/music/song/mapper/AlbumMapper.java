package com.music.song.mapper;

import com.music.song.dto.response.AlbumDetailResponse;
import com.music.song.dto.response.AlbumResponse;
import com.music.song.dto.response.SongResponse;
import com.music.song.entity.Album;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public AlbumDetailResponse toDetailResponse(Album album, List<SongResponse> songs) {
        return AlbumDetailResponse.builder()
                .id(album.getId())
                .name(album.getName())
                .coverImage(album.getCoverImage())
                .artistId(album.getArtistId())
                .artistUsername(album.getArtistUsername())
                .createdAt(album.getCreatedAt())
                .songs(songs)
                .build();
    }
}
