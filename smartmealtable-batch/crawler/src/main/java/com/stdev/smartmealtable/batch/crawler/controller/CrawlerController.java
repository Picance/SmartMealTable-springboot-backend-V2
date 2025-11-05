package com.stdev.smartmealtable.batch.crawler.controller;

import com.stdev.smartmealtable.batch.crawler.scheduler.CafeteriaCrawlerScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 크롤러 수동 실행을 위한 컨트롤러
 * 
 * <p>테스트 및 수동 실행 목적</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class CrawlerController {
    
    private final CafeteriaCrawlerScheduler scheduler;
    
    /**
     * 학식 데이터 크롤링 수동 실행
     * 
     * <p>POST /api/crawler/execute</p>
     */
    @PostMapping("/execute")
    public Map<String, String> executeCrawler() {
        log.info("학식 데이터 크롤링 수동 실행 요청 받음");
        
        try {
            scheduler.executeManually();
            return Map.of(
                    "status", "success",
                    "message", "학식 데이터 크롤링이 완료되었습니다."
            );
        } catch (Exception e) {
            log.error("학식 데이터 크롤링 수동 실행 중 오류 발생", e);
            return Map.of(
                    "status", "error",
                    "message", "학식 데이터 크롤링 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }
}

