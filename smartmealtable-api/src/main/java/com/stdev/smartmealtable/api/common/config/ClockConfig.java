package com.stdev.smartmealtable.api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Time-related common beans.
 */
@Configuration
public class ClockConfig {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock systemClock() {
        return Clock.system(DEFAULT_ZONE_ID);
    }
}
