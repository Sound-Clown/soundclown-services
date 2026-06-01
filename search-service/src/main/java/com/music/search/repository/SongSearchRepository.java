package com.music.search.repository;

import com.music.search.document.SongDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SongSearchRepository extends ElasticsearchRepository<SongDocument, Long> {

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title\", \"artistUsername\", \"albumName\"], \"fuzziness\": \"AUTO\"}}")
    Page<SongDocument> searchByKeyword(String keyword, Pageable pageable);
}
