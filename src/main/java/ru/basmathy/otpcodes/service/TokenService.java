package ru.basmathy.otpcodes.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ru.basmathy.otpcodes.exception.UnauthorizedException;
import ru.basmathy.otpcodes.model.User;
import ru.basmathy.otpcodes.model.UserRole;
import ru.basmathy.otpcodes.security.CurrentUser;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class TokenService {
    private final SecretKey secretKey;
    private final long expiresInSeconds;

    public TokenService(String secret, long expiresInSeconds) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresInSeconds = expiresInSeconds;
    }

    public AuthToken createToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expiresInSeconds);
        String token = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("login", user.getLogin())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new AuthToken(token, expiresInSeconds);
    }

    public CurrentUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            long userId = Long.parseLong(claims.getSubject());
            String login = claims.get("login", String.class);
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            return new CurrentUser(userId, login, role);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid token");
        }
    }
}
