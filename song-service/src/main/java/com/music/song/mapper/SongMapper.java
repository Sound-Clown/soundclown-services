package com.music.song.mapper;

import com.music.song.dto.response.SongResponse;
import com.music.song.entity.Song;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {

    public SongResponse toResponse(Song song, String albumName, boolean liked) {
        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .audioFile(song.getAudioFile())
                .coverImage(song.getCoverImage())
                .artistId(song.getArtistId())
                .artistUsername(song.getArtistUsername())
                .albumId(song.getAlbumId())
                .albumName(albumName)
                .status(song.getStatus())
                .rejectReason(song.getRejectReason())
                .playCount(song.getPlayCount())
                .likeCount(song.getLikeCount())
                .liked(liked)
                .createdAt(song.getCreatedAt())
                .build();
    }
}
