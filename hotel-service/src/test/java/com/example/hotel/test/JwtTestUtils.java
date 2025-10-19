package com.example.hotel.test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public final class JwtTestUtils {
    private JwtTestUtils() {}

    public static String issueHs256(String secret, String sub, String role, long ttlSeconds) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(sub)
                .addClaims(Map.of("role", role))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
