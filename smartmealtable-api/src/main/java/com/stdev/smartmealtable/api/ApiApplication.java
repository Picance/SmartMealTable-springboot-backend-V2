package com.stdev.smartmealtable.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Smartmealtable API Application
 * REST API를 제공하는 메인 애플리케이션
 */
@SpringBootApplication(scanBasePackages = "com.stdev.smartmealtable")
@EntityScan(basePackages = "com.stdev.smartmealtable.storage.db")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
