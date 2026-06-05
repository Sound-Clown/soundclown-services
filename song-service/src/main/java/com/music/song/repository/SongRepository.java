package com.music.song.repository;

import com.music.song.entity.Song;
import com.music.song.entity.SongStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    Page<Song> findByStatus(SongStatus status, Pageable pageable);

    Page<Song> findByArtistId(Long artistId, Pageable pageable);

    List<Song> findByAlbumIdAndStatusOrderByCreatedAtDesc(Long albumId, SongStatus status);

    List<Song> findByStatus(SongStatus status);

    long countByArtistId(Long artistId);

    long countByArtistIdAndStatus(Long artistId, SongStatus status);

    @Query("select coalesce(sum(s.playCount), 0) from Song s where s.artistId = :artistId")
    long sumPlayCountByArtistId(@Param("artistId") Long artistId);

    @Query("select coalesce(sum(s.likeCount), 0) from Song s where s.artistId = :artistId")
    long sumLikeCountByArtistId(@Param("artistId") Long artistId);

    @Modifying
    @Query("update Song s set s.playCount = s.playCount + 1 where s.id = :id")
    void incrementPlayCount(@Param("id") Long id);

    @Modifying
    @Query("update Song s set s.likeCount = s.likeCount + 1 where s.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("update Song s set s.likeCount = s.likeCount - 1 where s.id = :id and s.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("update Song s set s.albumId = null where s.albumId = :albumId")
    void clearAlbum(@Param("albumId") Long albumId);
}
