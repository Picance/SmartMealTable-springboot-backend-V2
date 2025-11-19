package com.stdev.smartmealtable.performance.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Small utility that prints a signed JWT so load-testing tools can call authenticated APIs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCliService {

    private final JwtCliProperties properties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public void printToken(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("jwt-member-id must be a positive number");
        }
        String token = createToken(memberId);
        log.info("Generated JWT for member {}: {}", memberId, token);
        System.out.println(token);
    }

    private String createToken(Long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getValidityInMilliseconds());
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
