package com.stdev.smartmealtable.batch.crawler.job.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.batch.crawler.dto.CrawledStoreDto;
import com.stdev.smartmealtable.batch.crawler.job.processor.CrawledStoreProcessor;
import com.stdev.smartmealtable.batch.crawler.job.processor.CrawledStoreProcessor.ProcessedStoreData;
import com.stdev.smartmealtable.batch.crawler.job.reader.JsonStoreItemReader;
import com.stdev.smartmealtable.batch.crawler.job.writer.StoreDataWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 크롤링 데이터 배치 Job 설정
 * JSON 파일에서 가게 데이터를 읽어와 DB에 저장/업데이트
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StoreCrawlerBatchJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper;
    private final CrawledStoreProcessor processor;
    private final StoreDataWriter writer;
    
    /**
     * 크롤링 데이터 Import Job
     */
    @Bean
    public Job importCrawledStoreJob() {
        return new JobBuilder("importCrawledStoreJob", jobRepository)
                .start(importCrawledStoreStep())
                .build();
    }
    
    /**
     * JSON 파일에서 가게 데이터를 읽어와 처리하는 Step
     */
    @Bean
    public Step importCrawledStoreStep() {
        return new StepBuilder("importCrawledStoreStep", jobRepository)
                .<CrawledStoreDto, ProcessedStoreData>chunk(10, transactionManager) // 10개씩 처리
                .reader(jsonStoreItemReader(null))
                .processor(processor)
                .writer(writer)
                .build();
    }
    
    /**
     * JSON 파일 Reader (StepScope)
     * Job Parameter로 파일 경로 전달
     */
    @Bean
    @StepScope
    public ItemReader<CrawledStoreDto> jsonStoreItemReader(
            @Value("#{jobParameters['inputFilePath']}") Resource inputFile) {
        try {
            log.info("Creating JsonStoreItemReader for file: {}", 
                    inputFile != null ? inputFile.getFilename() : "null");
            return new JsonStoreItemReader(inputFile, objectMapper);
        } catch (Exception e) {
            log.error("Failed to create JsonStoreItemReader", e);
            throw new RuntimeException("Failed to create JsonStoreItemReader", e);
        }
    }
}
