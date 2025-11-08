package com.stdev.smartmealtable.batch.crawler.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * StoreCrawlerBatchController 테스트
 * 배치 작업 실행 컨트롤러의 각 엔드포인트 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StoreCrawlerBatchController 테스트")
class StoreCrawlerBatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job importCrawledStoreJob;
    
    @InjectMocks
    private StoreCrawlerBatchController controller;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("JSON 파일 업로드 및 배치 작업 시작 - 성공")
    void uploadAndImportStores_Success() throws Exception {
        // given
        String jsonContent = """
                [{
                    "id": "test-store-1",
                    "name": "테스트 가게",
                    "category": "한식",
                    "address": "서울특별시 노원구 공릉동",
                    "coordinates": {
                        "latitude": 37.6254,
                        "longitude": 127.0728
                    },
                    "images": ["https://example.com/image1.jpg"],
                    "openingHours": [],
                    "menus": [],
                    "menu_average": 10000,
                    "phone_number": "02-1234-5678",
                    "review_count": 100
                }]
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-stores.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonContent.getBytes()
        );

        JobExecution jobExecution = new JobExecution(1L);
        when(jobLauncher.run(eq(importCrawledStoreJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        // when & then
        mockMvc.perform(multipart("/api/v1/batch/crawler/upload-and-import")
                        .file(file))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Batch job started with id: 1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("test-stores.json")));
    }

    @Test
    @DisplayName("빈 파일 업로드 시 400 Bad Request")
    void uploadAndImportStores_EmptyFile() throws Exception {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.json",
                MediaType.APPLICATION_JSON_VALUE,
                new byte[0]
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/batch/crawler/upload-and-import")
                        .file(emptyFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }

    @Test
    @DisplayName("JSON이 아닌 파일 업로드 시 400 Bad Request")
    void uploadAndImportStores_NotJsonFile() throws Exception {
        // given
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/batch/crawler/upload-and-import")
                        .file(textFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only JSON files are allowed"));
    }

    @Test
    @DisplayName("배치 작업 실행 실패 시 500 Internal Server Error")
    void uploadAndImportStores_JobExecutionFailed() throws Exception {
        // given
        String jsonContent = """
                [{
                    "id": "test-store-1",
                    "name": "테스트 가게"
                }]
                """;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-stores.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonContent.getBytes()
        );

        when(jobLauncher.run(eq(importCrawledStoreJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Job execution failed"));

        // when & then
        mockMvc.perform(multipart("/api/v1/batch/crawler/upload-and-import")
                        .file(file))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Failed to start batch job")));
    }

    @Test
    @DisplayName("파일 경로로 배치 작업 시작 - 성공")
    void importStores_Success() throws Exception {
        // given
        String filePath = "file:/tmp/test-stores.json";
        JobExecution jobExecution = new JobExecution(2L);
        
        when(jobLauncher.run(eq(importCrawledStoreJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        // when & then
        mockMvc.perform(post("/api/v1/batch/crawler/import-stores")
                        .param("filePath", filePath))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(content().string("Batch job started with id: 2"));
    }

    @Test
    @DisplayName("잘못된 파일 경로로 배치 작업 시작 시 400 Bad Request")
    void importStores_InvalidFilePath() throws Exception {
        // given
        String invalidFilePath = "/tmp/../etc/passwd";

        // when & then
        mockMvc.perform(post("/api/v1/batch/crawler/import-stores")
                        .param("filePath", invalidFilePath))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid file path")));
    }

    @Test
    @DisplayName("file: 스킴이 없는 경로로 배치 작업 시작 시 400 Bad Request")
    void importStores_PathWithoutFileScheme() throws Exception {
        // given
        String pathWithoutScheme = "/tmp/test-stores.json";

        // when & then
        mockMvc.perform(post("/api/v1/batch/crawler/import-stores")
                        .param("filePath", pathWithoutScheme))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid file path")));
    }
}
