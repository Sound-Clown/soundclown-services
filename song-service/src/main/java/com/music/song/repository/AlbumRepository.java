package com.music.song.repository;

import com.music.song.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Page<Album> findByArtistId(Long artistId, Pageable pageable);

    Page<Album> findByArtistUsername(String artistUsername, Pageable pageable);
}
