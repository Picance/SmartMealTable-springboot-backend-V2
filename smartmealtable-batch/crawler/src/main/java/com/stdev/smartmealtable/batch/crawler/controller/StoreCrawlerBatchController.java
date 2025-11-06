package com.stdev.smartmealtable.batch.crawler.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 크롤링 데이터 배치 작업 실행 컨트롤러
 * JSON 파일 경로를 받아 배치 작업 실행
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/batch/crawler")
@RequiredArgsConstructor
public class StoreCrawlerBatchController {
    
    private final JobLauncher jobLauncher;
    private final Job importCrawledStoreJob;
    
    /**
     * 크롤링 데이터 Import 배치 실행
     * 
     * @param filePath JSON 파일 경로 (예: file:/path/to/노원구_공릉동.json)
     */
    @PostMapping("/import-stores")
    public ResponseEntity<String> importStores(@RequestParam String filePath) {
        try {
            log.info("Starting importCrawledStoreJob with filePath: {}", filePath);
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFilePath", filePath)
                    .addLong("timestamp", System.currentTimeMillis()) // 중복 실행 방지
                    .toJobParameters();
            
            jobLauncher.run(importCrawledStoreJob, jobParameters);
            
            log.info("Successfully completed importCrawledStoreJob");
            return ResponseEntity.ok("Batch job started successfully");
            
        } catch (Exception e) {
            log.error("Failed to run importCrawledStoreJob", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to start batch job: " + e.getMessage());
        }
    }
}
