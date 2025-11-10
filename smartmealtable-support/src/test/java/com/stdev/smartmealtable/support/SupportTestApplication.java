package com.stdev.smartmealtable.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Support 모듈 테스트용 Application
 * 
 * <p>통합 테스트를 위한 최소 구성의 Spring Boot Application입니다.</p>
 */
@SpringBootApplication
public class SupportTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupportTestApplication.class, args);
    }
}
