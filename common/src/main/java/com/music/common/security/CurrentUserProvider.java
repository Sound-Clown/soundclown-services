package com.music.common.security;

import com.music.common.exception.AppException;
import com.music.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public AuthPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        return principal;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().userId();
    }

    public String getCurrentUsername() {
        return getCurrentUser().username();
    }

    public Role getCurrentRole() {
        return getCurrentUser().role();
    }

    // For public endpoints that personalize when a token is present, but do not require it.
    public Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof AuthPrincipal principal)
                ? principal.userId()
                : null;
    }
}
