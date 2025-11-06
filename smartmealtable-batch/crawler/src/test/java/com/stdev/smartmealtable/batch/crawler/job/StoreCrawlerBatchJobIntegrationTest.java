package com.stdev.smartmealtable.batch.crawler.job;

import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import org.junit.jupiter.api.*;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StoreCrawlerBatchJob 통합 테스트
 * TestContainer를 사용하여 실제 DB 연동 테스트
 */
@SpringBootTest
@SpringBatchTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("StoreCrawlerBatchJob 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StoreCrawlerBatchJobIntegrationTest {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private FoodRepository foodRepository;

    private File testJsonFile;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 JSON 파일 생성
        testJsonFile = File.createTempFile("test-store-", ".json");
        writeTestJson(testJsonFile);
    }

    @AfterEach
    void tearDown() {
        if (testJsonFile != null && testJsonFile.exists()) {
            testJsonFile.delete();
        }
    }

    @Test
    @Order(1)
    @DisplayName("크롤링 데이터 Import 배치 작업이 성공한다")
    void it_imports_crawled_store_data_successfully() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFilePath", "file:" + testJsonFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
    }

    @Test
    @Order(2)
    @DisplayName("Import 후 Store 데이터가 DB에 저장된다")
    void it_saves_store_to_database() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFilePath", "file:" + testJsonFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        Optional<Store> store = storeRepository.findByExternalId("test_external_001");
        assertThat(store).isPresent();
        assertThat(store.get().getName()).isEqualTo("테스트 음식점");
        assertThat(store.get().getExternalId()).isEqualTo("test_external_001");
    }

    @Test
    @Order(3)
    @DisplayName("Import 후 Food 데이터가 DB에 저장된다")
    void it_saves_food_to_database() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFilePath", "file:" + testJsonFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        Optional<Store> store = storeRepository.findByExternalId("test_external_001");
        assertThat(store).isPresent();

        List<Food> foods = foodRepository.findByStoreId(store.get().getStoreId());
        assertThat(foods).isNotEmpty();
        assertThat(foods).anyMatch(f -> f.getFoodName().equals("김치찌개"));
        assertThat(foods).anyMatch(f -> f.getFoodName().equals("된장찌개"));
    }

    @Test
    @Order(4)
    @DisplayName("Upsert - 기존 데이터가 있으면 업데이트한다")
    void it_updates_existing_store() throws Exception {
        // Given - 첫 번째 Import
        JobParameters firstJobParameters = new JobParametersBuilder()
                .addString("inputFilePath", "file:" + testJsonFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncherTestUtils.launchJob(firstJobParameters);

        Optional<Store> firstStore = storeRepository.findByExternalId("test_external_001");
        assertThat(firstStore).isPresent();
        Long firstStoreId = firstStore.get().getStoreId();

        // 수정된 JSON 파일 생성
        File updatedJsonFile = File.createTempFile("test-store-updated-", ".json");
        writeUpdatedTestJson(updatedJsonFile);

        // When - 두 번째 Import (업데이트)
        JobParameters secondJobParameters = new JobParametersBuilder()
                .addString("inputFilePath", "file:" + updatedJsonFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis() + 1000)
                .toJobParameters();
        jobLauncherTestUtils.launchJob(secondJobParameters);

        // Then
        Optional<Store> updatedStore = storeRepository.findByExternalId("test_external_001");
        assertThat(updatedStore).isPresent();
        assertThat(updatedStore.get().getStoreId()).isEqualTo(firstStoreId); // ID는 동일
        assertThat(updatedStore.get().getName()).isEqualTo("업데이트된 음식점"); // 이름은 변경

        // Cleanup
        updatedJsonFile.delete();
    }

    @Test
    @Order(5)
    @DisplayName("잘못된 파일 경로로 실행 시 실패한다")
    void it_fails_with_invalid_file_path() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFilePath", "file:/nonexistent/path/test.json")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    /**
     * 테스트용 JSON 데이터 생성
     */
    private void writeTestJson(File file) throws IOException {
        String json = """
                [
                  {
                    "id": "test_external_001",
                    "name": "테스트 음식점",
                    "category": "한식",
                    "address": "서울특별시 노원구 공릉동 123",
                    "latitude": 37.6250,
                    "longitude": 127.0757,
                    "phone": "02-1234-5678",
                    "averagePrice": 8000,
                    "reviewCount": 100,
                    "images": [
                      {
                        "url": "https://example.com/main.jpg",
                        "isMain": true,
                        "order": 1
                      }
                    ],
                    "openingHours": [
                      {
                        "day": "월요일",
                        "openTime": "09:00",
                        "closeTime": "21:00",
                        "isHoliday": false
                      }
                    ],
                    "menus": [
                      {
                        "name": "김치찌개",
                        "price": 7000,
                        "isMain": true,
                        "order": 1
                      },
                      {
                        "name": "된장찌개",
                        "price": 7000,
                        "isMain": false,
                        "order": 2
                      }
                    ]
                  }
                ]
                """;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }

    /**
     * 업데이트된 테스트용 JSON 데이터 생성
     */
    private void writeUpdatedTestJson(File file) throws IOException {
        String json = """
                [
                  {
                    "id": "test_external_001",
                    "name": "업데이트된 음식점",
                    "category": "한식",
                    "address": "서울특별시 노원구 공릉동 456",
                    "latitude": 37.6250,
                    "longitude": 127.0757,
                    "phone": "02-1234-5678",
                    "averagePrice": 9000,
                    "reviewCount": 150,
                    "images": [
                      {
                        "url": "https://example.com/updated.jpg",
                        "isMain": true,
                        "order": 1
                      }
                    ],
                    "openingHours": [
                      {
                        "day": "월요일",
                        "openTime": "09:00",
                        "closeTime": "22:00",
                        "isHoliday": false
                      }
                    ],
                    "menus": [
                      {
                        "name": "김치찌개",
                        "price": 8000,
                        "isMain": true,
                        "order": 1
                      },
                      {
                        "name": "제육볶음",
                        "price": 9000,
                        "isMain": false,
                        "order": 2
                      }
                    ]
                  }
                ]
                """;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }
}
