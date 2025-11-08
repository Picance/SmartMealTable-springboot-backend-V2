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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 크롤링 데이터 배치 작업 실행 컨트롤러
 * JSON 파일 경로를 받아 배치 작업 실행
 */
@Slf4j
@Controller
@RequestMapping("/batch/crawler")
@RequiredArgsConstructor
public class StoreCrawlerBatchController {
    
    private final JobLauncher jobLauncher;
    private final Job importCrawledStoreJob;
    
    /**
     * 업로드 폼 페이지 표시
     */
    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "crawler/upload";
    }
    
    /**
     * 크롤링 데이터 Import 배치 실행 (API)
     * 
     * @param filePath JSON 파일 경로 (예: file:/path/to/노원구_공릉동.json)
     */
    @PostMapping("/api/import-stores")
    public ResponseEntity<String> importStores(@RequestParam String filePath) {
        try {
            // 파일 경로 검증
            if (!isValidFilePath(filePath)) {
                return ResponseEntity.badRequest()
                        .body("Invalid file path: must start with file: and not contain ..");
            }
            
            log.info("Starting importCrawledStoreJob with filePath: {}", filePath);
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFilePath", filePath)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(importCrawledStoreJob, jobParameters);
            
            log.info("Batch job started with execution id: {}", jobExecution.getId());
            return ResponseEntity.accepted()
                    .body("Batch job started with id: " + jobExecution.getId());
        } catch (Exception e) {
            log.error("Failed to start batch job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start batch job: " + e.getMessage());
        }
    }
    
    /**
     * JSON 파일을 업로드하여 크롤링 데이터 Import 배치 실행 (웹 폼)
     * 
     * @param file JSON 파일 (MultipartFile)
     */
    @PostMapping("/upload-and-import")
    public String uploadAndImportStores(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        // 파일 검증
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "파일이 비어있습니다.");
            return "redirect:/batch/crawler/upload";
        }
        
        // JSON 파일 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".json")) {
            redirectAttributes.addFlashAttribute("error", "JSON 파일만 업로드 가능합니다.");
            return "redirect:/batch/crawler/upload";
        }
        
        Path tempFile = null;
        try {
            // 임시 디렉토리에 파일 저장
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "crawler-batch");
            Files.createDirectories(tempDir);
            
            // 고유한 파일명 생성 (타임스탬프 포함)
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            tempFile = tempDir.resolve(filename);
            
            // 파일 복사
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Uploaded file saved to: {}", tempFile.toAbsolutePath());
            
            // 배치 작업 실행
            String filePath = "file:" + tempFile.toAbsolutePath();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFilePath", filePath)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(importCrawledStoreJob, jobParameters);
            
            log.info("Batch job started with execution id: {} for file: {}", 
                    jobExecution.getId(), originalFilename);
            
            redirectAttributes.addFlashAttribute("success", 
                    String.format("배치 작업이 시작되었습니다. 작업 ID: %d, 파일: %s", 
                            jobExecution.getId(), originalFilename));
            redirectAttributes.addFlashAttribute("jobId", jobExecution.getId());
            return "redirect:/batch/crawler/upload";
            
        } catch (IOException e) {
            log.error("Failed to save uploaded file", e);
            redirectAttributes.addFlashAttribute("error", 
                    "파일 저장 실패: " + e.getMessage());
            return "redirect:/batch/crawler/upload";
        } catch (Exception e) {
            log.error("Failed to start batch job", e);
            redirectAttributes.addFlashAttribute("error", 
                    "배치 작업 시작 실패: " + e.getMessage());
            return "redirect:/batch/crawler/upload";
        }
    }
    
    /**
     * JSON 파일을 업로드하여 크롤링 데이터 Import 배치 실행 (REST API)
     * 
     * @param file JSON 파일 (MultipartFile)
     */
    @PostMapping("/api/upload-and-import")
    public ResponseEntity<String> uploadAndImportStoresApi(@RequestParam("file") MultipartFile file) {
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
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "crawler-batch");
            Files.createDirectories(tempDir);
            
            // 고유한 파일명 생성 (타임스탬프 포함)
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            tempFile = tempDir.resolve(filename);
            
            // 파일 복사
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Uploaded file saved to: {}", tempFile.toAbsolutePath());
            
            // 배치 작업 실행
            String filePath = "file:" + tempFile.toAbsolutePath();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFilePath", filePath)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            JobExecution jobExecution = jobLauncher.run(importCrawledStoreJob, jobParameters);
            
            log.info("Batch job started with execution id: {} for file: {}", 
                    jobExecution.getId(), originalFilename);
            
            return ResponseEntity.accepted()
                    .body(String.format("Batch job started with id: %d for file: %s", 
                            jobExecution.getId(), originalFilename));
            
        } catch (IOException e) {
            log.error("Failed to save uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save uploaded file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to start batch job", e);
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
