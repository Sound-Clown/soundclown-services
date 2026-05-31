package com.music.song.repository;

import com.music.song.entity.Like;
import com.music.song.entity.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @Query("select l.id.songId from Like l where l.id.userId = :userId and l.id.songId in :songIds")
    List<Long> findLikedSongIds(@Param("userId") Long userId, @Param("songIds") Collection<Long> songIds);

    @Modifying
    @Query("delete from Like l where l.id.songId = :songId")
    void deleteBySongId(@Param("songId") Long songId);
}
