package com.stdev.smartmealtable.batch.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 학식 데이터 크롤러 애플리케이션
 * 
 * <p>서울과학기술대학교 학식 데이터를 주기적으로 크롤링하여 데이터베이스에 저장합니다.</p>
 * <p>JPA 설정(Repository, Entity 스캔)은 storage:db 모듈의 JpaConfig에서 처리됩니다.</p>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.stdev.smartmealtable.batch.crawler",
    "com.stdev.smartmealtable.storage.db"
})
@EnableScheduling
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }
}

