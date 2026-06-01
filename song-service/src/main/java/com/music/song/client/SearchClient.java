package com.music.song.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Indexes approved songs into search-service. Internal, not gateway-routed.
@FeignClient(name = "search-service", url = "${search-service.url:http://localhost:8084}")
public interface SearchClient {

    @PostMapping("/internal/search/songs")
    void index(@RequestBody SongIndexRequest request);

    @DeleteMapping("/internal/search/songs/{id}")
    void delete(@PathVariable("id") Long id);
}
