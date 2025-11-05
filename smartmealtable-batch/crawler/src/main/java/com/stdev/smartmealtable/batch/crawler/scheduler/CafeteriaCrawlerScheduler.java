package com.stdev.smartmealtable.batch.crawler.scheduler;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData;
import com.stdev.smartmealtable.batch.crawler.service.CafeteriaDataImportService;
import com.stdev.smartmealtable.batch.crawler.service.SeleniumCrawlerService;
import com.stdev.smartmealtable.batch.crawler.service.SeoulTechCafeteriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 학식 데이터 크롤링 스케줄러
 * 
 * <p>매주 월요일 오전 2시에 서울과학기술대학교 학식 데이터를 크롤링하여 데이터베이스를 업데이트합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CafeteriaCrawlerScheduler {
    
    private final SeoulTechCafeteriaService crawlerService;
    private final CafeteriaDataImportService importService;
    
    /**
     * 매주 월요일 오전 2시에 학식 데이터 크롤링 및 저장
     * 
     * <p>Cron 표현식: "0 0 2 * * MON" = 매주 월요일 02:00:00</p>
     */
    @Scheduled(cron = "0 0 2 * * MON", zone = "Asia/Seoul")
    public void crawlAndUpdateCafeteriaData() {
        log.info("========================================");
        log.info("학식 데이터 크롤링 작업 시작");
        log.info("========================================");
        
        try {
            // 1. 노션 페이지에서 학식 데이터 크롤링
            List<CampusCafeteriaData> cafeteriaDataList = crawlerService.crawlCafeteriaData();
            
            // 2. 크롤링한 데이터를 데이터베이스에 저장
            importService.importCafeteriaData(cafeteriaDataList);
            
            log.info("========================================");
            log.info("학식 데이터 크롤링 작업 완료");
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("학식 데이터 크롤링 작업 중 오류 발생", e);
            log.error("========================================");
            log.error("학식 데이터 크롤링 작업 실패");
            log.error("========================================");
            
            // 실패해도 예외를 던지지 않아 다음 스케줄 실행에 영향을 주지 않음
        }
    }
    
    /**
     * 수동 실행을 위한 메서드
     * 
     * <p>애플리케이션 시작 시 또는 수동으로 크롤링을 실행할 때 사용</p>
     */
    public void executeManually() {
        log.info("학식 데이터 크롤링 작업 수동 실행");
        crawlAndUpdateCafeteriaData();
    }

    public static void main(String[] args) {
        SeleniumCrawlerService crawler = new SeleniumCrawlerService();
        List<CampusCafeteriaData> result = crawler.crawlCafeteriaData();

        // 결과 확인
        System.out.println("크롤링 완료: " + result.size() + "개 건물");
    }
}

