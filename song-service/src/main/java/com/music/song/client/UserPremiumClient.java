package com.music.song.client;

import com.music.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Checks a user's premium status to gate premium-only playback. Internal, not gateway-routed.
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8083}")
public interface UserPremiumClient {

    @GetMapping("/internal/users/{id}/premium")
    ApiResponse<PremiumStatus> getPremiumStatus(@PathVariable("id") Long id);
}
