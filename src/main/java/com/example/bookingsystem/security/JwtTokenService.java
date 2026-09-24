package com.example.bookingsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenService {

    private static final int MINIMUM_SECRET_LENGTH = 32;

    private final SecretKey signingKey;
    private final long lifetime;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long lifetime) {

        if (secret == null ||
                secret.getBytes(StandardCharsets.UTF_8).length
                        < MINIMUM_SECRET_LENGTH) {

            throw new IllegalArgumentException(
                    "JWT secret must contain at least "
                            + MINIMUM_SECRET_LENGTH
                            + " bytes");
        }

        if (lifetime <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero");
        }

        this.signingKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(StandardCharsets.UTF_8));

        this.lifetime = lifetime;
    }

    public String create(
            String username,
            String role) {

        Date issuedAt = new Date();

        Date expiresAt =
                new Date(
                        issuedAt.getTime()
                                + lifetime);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public String username(String token) {

        return parse(token)
                .getPayload()
                .getSubject();
    }

    public boolean valid(String token) {

        try {

            parse(token);
            return true;

        } catch (JwtException |
                 IllegalArgumentException ex) {

            return false;
        }
    }

    private Jws<Claims> parse(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
    }
}