package com.stdev.smartmealtable.performance.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Minimal JWT settings used by the CLI helper to mint tokens for perf tests.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtCliProperties {

    private String secret = "smartmealtable-secret-key-for-jwt-token-generation-minimum-256-bits";
    private long validityInMilliseconds = 3_600_000L;
}
