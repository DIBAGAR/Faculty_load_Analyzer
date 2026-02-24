package com.example.facultyload.security;

import com.example.facultyload.config.JwtProperties;
import com.example.facultyload.entity.RoleName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    private SecretKey key() {
        // Derive a 256-bit key from the configured secret (supports short secrets safely).
        byte[] raw = props.getSecret().getBytes(StandardCharsets.UTF_8);
        byte[] digest = sha256(raw);
        return Keys.hmacShaKeyFor(digest);
    }

    public String generateToken(String email, RoleName role) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(props.getExpiration());

        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("role", role.name())
                .signWith(key())
                .compact();
    }

    public Optional<Claims> parseClaims(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token);
            return Optional.of(jws.getPayload());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static byte[] sha256(byte[] in) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(in);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

