package com.music.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Calls user-service's internal (non-gateway-routed) endpoint to mirror the account
// as a public profile. Used best-effort on registration.
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8083}")
public interface UserSyncClient {

    @PostMapping("/internal/users")
    void upsert(@RequestBody UserProfileSyncRequest request);
}
