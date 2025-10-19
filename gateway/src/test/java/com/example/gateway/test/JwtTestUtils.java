// gateway/src/test/java/.../JwtTestUtils.java
package com.example.gateway.test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public final class JwtTestUtils {
    private JwtTestUtils(){}

    public static String issueHs256RawSecret(String secret, String sub, String role, long ttlSeconds){
        // ВАЖНО: тот же способ, что в SecurityConfig — сырые байты строки
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
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
