package com.stdev.smartmealtable.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smartmealtable Admin Application
 * 관리자용 API를 제공하는 애플리케이션
 */
@SpringBootApplication(scanBasePackages = "com.stdev.smartmealtable")
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
