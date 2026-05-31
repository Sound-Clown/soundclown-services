package com.music.common.security;

import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    private static final String ROLE_PREFIX = "ROLE_";

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        return userId;
    }

    public Role getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .map(Role::valueOf)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
    }
}
