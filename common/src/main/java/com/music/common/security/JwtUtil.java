package com.music.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiryDays;

    // jwt.secret must be at least 32 chars (HS256 requires a 256-bit key)
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiry-days:7}") long expiryDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryDays = expiryDays;
    }

    public String generateToken(Long userId, String username, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiryDays, ChronoUnit.DAYS)))
                .signWith(key)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
