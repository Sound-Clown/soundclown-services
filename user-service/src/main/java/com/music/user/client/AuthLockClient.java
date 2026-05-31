package com.music.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// auth-service owns the account's active state (it gates login), so lock/unlock is
// applied there first via this internal endpoint. PUT (not PATCH) because Feign's default
// HttpURLConnection client cannot send PATCH.
@FeignClient(name = "auth-service", url = "${auth-service.url:http://localhost:8081}")
public interface AuthLockClient {

    @PutMapping("/internal/users/{id}/lock")
    void setActive(@PathVariable("id") Long id, @RequestParam("active") boolean active);
}
