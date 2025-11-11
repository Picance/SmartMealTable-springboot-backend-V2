package com.stdev.smartmealtable.batch.crawler.job.config;

import com.stdev.smartmealtable.batch.crawler.dto.GroupDataDto;
import com.stdev.smartmealtable.batch.crawler.job.processor.GroupDataProcessor;
import com.stdev.smartmealtable.batch.crawler.job.reader.JsonGroupDataItemReader;
import com.stdev.smartmealtable.batch.crawler.job.writer.GroupDataWriter;
import com.stdev.smartmealtable.batch.crawler.service.GroupDataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 그룹 데이터 Import 배치 작업 설정
 * JSON 파일에서 그룹 데이터를 읽어 처리하는 배치 작업
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GroupDataBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * 그룹 데이터 Import Job
     */
    @Bean
    public Job importGroupDataJob(Step importGroupDataStep) {
        return new JobBuilder("importGroupDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importGroupDataStep)
                .build();
    }

    /**
     * 그룹 데이터 Import Step
     */
    @Bean
    public Step importGroupDataStep(
            ItemReader<GroupDataDto> groupDataItemReader,
            ItemProcessor<GroupDataDto, GroupDataDto> groupDataProcessor,
            ItemWriter<GroupDataDto> groupDataWriter) {

        return new StepBuilder("importGroupDataStep", jobRepository)
                .<GroupDataDto, GroupDataDto>chunk(100, transactionManager) // 100개씩 청크 처리
                .reader(groupDataItemReader)
                .processor(groupDataProcessor)
                .writer(groupDataWriter)
                .build();
    }

    /**
     * JSON 파일에서 그룹 데이터를 읽는 ItemReader
     * @StepScope를 사용하여 Job Parameter를 런타임에 주입
     */
    @Bean
    @StepScope
    public ItemReader<GroupDataDto> groupDataItemReader(
            @Value("#{jobParameters['inputFilePath']}") String inputFilePath) {

        log.info("Creating JsonGroupDataItemReader with inputFilePath: {}", inputFilePath);

        // file: 스킴 제거
        String filePath = inputFilePath;
        if (filePath.startsWith("file:")) {
            filePath = filePath.substring(5);
        }

        return new JsonGroupDataItemReader(new FileSystemResource(filePath));
    }

    /**
     * 그룹 데이터를 처리하는 Processor
     */
    @Bean
    public ItemProcessor<GroupDataDto, GroupDataDto> groupDataProcessor() {
        return new GroupDataProcessor();
    }

    /**
     * 그룹 데이터를 저장하는 Writer
     */
    @Bean
    public ItemWriter<GroupDataDto> groupDataWriter(GroupDataImportService groupDataImportService) {
        return new GroupDataWriter(groupDataImportService);
    }
}
