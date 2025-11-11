package com.stdev.smartmealtable.batch.crawler.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.batch.crawler.dto.GroupDataDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 그룹 데이터 배치 작업 통합 테스트
 */
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class GroupDataBatchJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job importGroupDataJob;

    @Test
    @DisplayName("그룹 데이터 JSON 파일을 읽어서 배치 작업을 성공적으로 수행한다")
    void it_imports_group_data_successfully() throws Exception {
        // Given: 테스트용 JSON 파일 생성
        Path tempFile = createTestGroupDataJsonFile();

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFilePath", "file:" + tempFile.toAbsolutePath())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            // When: 배치 작업 실행
            jobLauncherTestUtils.setJob(importGroupDataJob);
            JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

            // Then: 작업이 성공적으로 완료되었는지 확인
            assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        } finally {
            // 임시 파일 삭제
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 테스트용 그룹 데이터 JSON 파일 생성
     */
    private Path createTestGroupDataJsonFile() throws Exception {
        // 테스트 데이터 생성
        GroupDataDto testData1 = GroupDataDto.builder()
                .schoolName("테스트대학교")
                .schoolNameEn("Test University")
                .campusType("본교")
                .universityType("대학원")
                .schoolType("일반대학원")
                .establishmentType("사립")
                .regionCode("11")
                .regionName("서울특별시")
                .roadAddress("서울특별시 종로구 테스트로 123")
                .jibunAddress("서울특별시 종로구 테스트동 123")
                .roadPostalCode("03083")
                .postalCode("03083")
                .website("www.test.ac.kr")
                .phoneNumber("02-1234-5678")
                .faxNumber("02-1234-5679")
                .establishmentDate("2020-03-01")
                .baseYear("2024")
                .dataReferenceDate("2025-01-08")
                .providerCode("B340014")
                .providerName("한국대학교육협의회")
                .build();

        GroupDataDto testData2 = GroupDataDto.builder()
                .schoolName("샘플대학교")
                .schoolNameEn("Sample University")
                .campusType("본교")
                .universityType("대학원")
                .schoolType("특수대학원")
                .establishmentType("국립")
                .regionCode("41")
                .regionName("경기도")
                .roadAddress("경기도 수원시 팔달구 샘플로 456")
                .jibunAddress("경기도 수원시 팔달구 샘플동 456")
                .roadPostalCode("16499")
                .postalCode("16499")
                .website("sample.ac.kr")
                .phoneNumber("031-9876-5432")
                .faxNumber("031-9876-5433")
                .establishmentDate("2018-03-01")
                .baseYear("2024")
                .dataReferenceDate("2025-01-08")
                .providerCode("B340014")
                .providerName("한국대학교육협의회")
                .build();

        // JSON 구조 생성 (fields + records 형식)
        Map<String, Object> jsonData = new HashMap<>();

        // fields 배열 생성
        List<Map<String, String>> fields = List.of(
                Map.of("id", "학교명"),
                Map.of("id", "학교 영문명"),
                Map.of("id", "본분교구분명"),
                Map.of("id", "대학구분명"),
                Map.of("id", "학교구분명"),
                Map.of("id", "설립형태구분명"),
                Map.of("id", "시도코드"),
                Map.of("id", "시도명"),
                Map.of("id", "소재지도로명주소"),
                Map.of("id", "소재지지번주소"),
                Map.of("id", "도로명우편번호"),
                Map.of("id", "소재지우편번호"),
                Map.of("id", "홈페이지주소"),
                Map.of("id", "대표전화번호"),
                Map.of("id", "대표팩스번호"),
                Map.of("id", "설립일자"),
                Map.of("id", "기준연도"),
                Map.of("id", "데이터기준일자"),
                Map.of("id", "제공기관코드"),
                Map.of("id", "제공기관명")
        );

        jsonData.put("fields", fields);
        jsonData.put("records", List.of(testData1, testData2));

        // 임시 파일에 JSON 작성
        File tempFile = Files.createTempFile("test-group-data", ".json").toFile();
        ObjectMapper objectMapper = new ObjectMapper();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(objectMapper.writeValueAsString(jsonData));
        }

        return tempFile.toPath();
    }
}
