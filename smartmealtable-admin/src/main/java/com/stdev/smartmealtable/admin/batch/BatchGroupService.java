package com.stdev.smartmealtable.admin.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 배치 서버와 통신하는 서비스
 * 그룹 데이터 배치 작업을 위한 REST API 호출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchGroupService {

    private final RestTemplate restTemplate;

    @Value("${batch.crawler.base-url}")
    private String batchBaseUrl;

    /**
     * 배치 서버로 파일을 업로드하고 그룹 데이터 배치 작업 시작
     *
     * @param file 업로드할 JSON 파일
     * @return 배치 작업 응답
     */
    public BatchJobResponse uploadAndStartBatch(MultipartFile file) throws IOException {
        String url = batchBaseUrl + "/api/v1/batch/group/upload-and-import";

        log.info("Uploading group data file to batch server: {} (size: {} bytes)",
                file.getOriginalFilename(), file.getSize());

        // MultipartFile을 ByteArrayResource로 변환
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        // Multipart 요청 준비
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        try {
            // 배치 서버로 POST 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("Group batch job started successfully: {}", responseBody);

                // 응답에서 Job ID 추출 (예: "Batch job started with id: 123 for file: ...")
                Long jobId = extractJobId(responseBody);

                return BatchJobResponse.builder()
                        .success(true)
                        .message(responseBody != null ? responseBody : "그룹 데이터 배치 작업이 시작되었습니다.")
                        .jobId(jobId)
                        .build();
            } else {
                String errorMessage = String.format("배치 서버 응답 오류: %s", response.getStatusCode());
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }

        } catch (Exception e) {
            log.error("Failed to communicate with batch server for group data", e);
            throw new RuntimeException("배치 서버 통신 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 응답 메시지에서 Job ID 추출
     * 예: "Batch job started with id: 123 for file: ..." -> 123
     */
    private Long extractJobId(String responseBody) {
        if (responseBody == null) {
            return null;
        }

        try {
            // "id: " 다음의 숫자 추출
            int idIndex = responseBody.indexOf("id: ");
            if (idIndex != -1) {
                int startIndex = idIndex + 4;
                int endIndex = responseBody.indexOf(" ", startIndex);
                if (endIndex == -1) {
                    endIndex = responseBody.length();
                }
                String idStr = responseBody.substring(startIndex, endIndex);
                return Long.parseLong(idStr);
            }
        } catch (Exception e) {
            log.warn("Failed to extract job ID from response: {}", responseBody, e);
        }

        return null;
    }
}
