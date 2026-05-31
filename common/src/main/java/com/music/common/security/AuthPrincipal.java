package com.music.common.security;

// Authenticated principal built from JWT claims and stored in the SecurityContext.
public record AuthPrincipal(Long userId, String username, Role role) {
}
