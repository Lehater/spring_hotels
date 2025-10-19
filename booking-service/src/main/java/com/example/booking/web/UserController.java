package com.example.booking.web;

import com.example.booking.config.JwtService;
import com.example.booking.domain.UserAccount;
import com.example.booking.repo.UserAccountRepository;
import com.example.booking.web.dto.*;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
  private static final long TTL = Duration.ofHours(1).toSeconds();
  private final UserAccountRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
    users
        .findByUsername(req.username())
        .ifPresent(
            u -> {
              throw new RuntimeException("USERNAME_TAKEN");
            });
    String role = req.admin() ? "ROLE_ADMIN" : "ROLE_USER";
    UserAccount saved =
        users.save(
            UserAccount.builder()
                .username(req.username())
                .passwordHash(encoder.encode(req.password()))
                .role(role)
                .build());
    String token = jwt.issue(saved.getUsername(), saved.getRole(), TTL);
    return ResponseEntity.ok(new AuthResponse(token, TTL));
  }

  @PostMapping("/auth")
  public ResponseEntity<AuthResponse> auth(@RequestBody AuthRequest req) {
    var u =
        users.findByUsername(req.username()).orElseThrow(() -> new RuntimeException("NOT_FOUND"));
    if (!encoder.matches(req.password(), u.getPasswordHash()))
      throw new RuntimeException("BAD_CREDENTIALS");
    String token = jwt.issue(u.getUsername(), u.getRole(), TTL);
    return ResponseEntity.ok(new AuthResponse(token, TTL));
  }
}
