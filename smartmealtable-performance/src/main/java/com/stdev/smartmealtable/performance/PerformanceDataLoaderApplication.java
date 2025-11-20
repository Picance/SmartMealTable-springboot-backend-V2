package com.stdev.smartmealtable.performance;

import com.stdev.smartmealtable.performance.config.PerformanceLoadProperties;
import com.stdev.smartmealtable.performance.jwt.JwtCliProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the performance data loader CLI application.
 */
@SpringBootApplication
@EnableConfigurationProperties({PerformanceLoadProperties.class, JwtCliProperties.class})
public class PerformanceDataLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerformanceDataLoaderApplication.class, args);
    }
}
 
