package com.stdev.smartmealtable.batch.crawler.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 그룹 데이터 배치 작업 실행 컨트롤러 (REST API only)
 * Admin 서버에서 호출하는 내부 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/batch/group")
@RequiredArgsConstructor
public class GroupDataBatchController {

    private final JobLauncher jobLauncher;
    private final Job importGroupDataJob;

    /**
     * 그룹 데이터 Import 배치 실행
     *
     * @param filePath JSON 파일 경로 (예: file:/path/to/korea-univ-data.json)
     */
    @PostMapping("/import-group-data")
    public ResponseEntity<String> importGroupData(@RequestParam String filePath) {
        try {
            // 파일 경로 검증
            if (!isValidFilePath(filePath)) {
                return ResponseEntity.badRequest()
                        .body("Invalid file path: must start with file: and not contain ..");
            }

            log.info("Starting importGroupDataJob with filePath: {}", filePath);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFilePath", filePath)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(importGroupDataJob, jobParameters);

            log.info("Group data batch job started with execution id: {}", jobExecution.getId());
            return ResponseEntity.accepted()
                    .body("Batch job started with id: " + jobExecution.getId());
        } catch (Exception e) {
            log.error("Failed to start group data batch job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start batch job: " + e.getMessage());
        }
    }

    /**
     * JSON 파일을 업로드하여 그룹 데이터 Import 배치 실행
     * Admin 서버에서 호출하는 내부 API
     *
     * @param file JSON 파일 (MultipartFile)
     */
    @PostMapping("/upload-and-import")
    public ResponseEntity<String> uploadAndImportGroupData(@RequestParam("file") MultipartFile file) {
        // 파일 검증
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("File is empty");
        }

        // JSON 파일 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".json")) {
            return ResponseEntity.badRequest()
                    .body("Only JSON files are allowed");
        }

        Path tempFile = null;
        try {
            // 임시 디렉토리에 파일 저장
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "group-batch");
            Files.createDirectories(tempDir);

            // 고유한 파일명 생성 (타임스탬프 포함)
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            tempFile = tempDir.resolve(filename);

            // 파일 복사
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("Group data file uploaded and saved to: {}", tempFile.toAbsolutePath());

            // 배치 작업 실행
            String filePath = "file:" + tempFile.toAbsolutePath();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFilePath", filePath)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(importGroupDataJob, jobParameters);

            log.info("Group data batch job started with execution id: {} for file: {}",
                    jobExecution.getId(), originalFilename);

            return ResponseEntity.accepted()
                    .body(String.format("Batch job started with id: %d for file: %s",
                            jobExecution.getId(), originalFilename));

        } catch (IOException e) {
            log.error("Failed to save uploaded group data file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save uploaded file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to start group data batch job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start batch job: " + e.getMessage());
        }
    }

    /**
     * 파일 경로 유효성 검증
     */
    private boolean isValidFilePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        // file: 스킴으로 시작하고, 경로 순회(path traversal) 방지
        return filePath.startsWith("file:") && !filePath.contains("..");
    }
}
