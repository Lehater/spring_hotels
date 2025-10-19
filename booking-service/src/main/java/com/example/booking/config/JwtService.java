package com.example.booking.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final Key key;

  public JwtService(@Value("${auth.jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String issue(String subject, String role, long ttlSeconds) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(subject)
        .addClaims(Map.of("role", role))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }
}
