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
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.batch.jdbc.initialize-schema", () -> "always");
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job importCrawledStoreJob;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private FoodRepository foodRepository;

    private File testJsonFile;

    @BeforeEach
    void setUp() throws IOException {
        // Job 설정
        jobLauncherTestUtils.setJob(importCrawledStoreJob);

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
    @DisplayName("CSV 형식의 카테고리를 분리해서 저장한다")
    void it_splits_csv_categories() throws Exception {
        // Given - CSV 카테고리를 가진 JSON 파일
        File csvJsonFile = File.createTempFile("test-store-csv-", ".json");
        writeTestJsonWithCsvCategories(csvJsonFile);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFilePath", "file:" + csvJsonFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Store 조회
        Optional<Store> store = storeRepository.findByExternalId("test_external_csv_001");
        assertThat(store).isPresent();

        // 카테고리가 여러 개로 분리되어 저장되었는지 확인
        List<Long> categoryIds = store.get().getCategoryIds();
        assertThat(categoryIds).isNotEmpty();
        assertThat(categoryIds.size()).isGreaterThanOrEqualTo(2); // 최소 2개 이상의 카테고리

        // Cleanup
        csvJsonFile.delete();
    }

    @Test
    @Order(6)
    @DisplayName("공백을 포함한 CSV 카테고리를 제대로 분리해서 저장한다")
    void it_splits_csv_categories_with_spaces() throws Exception {
        // Given - 공백을 포함한 CSV 카테고리
        File csvJsonFile = File.createTempFile("test-store-csv-spaces-", ".json");
        try {
            writeTestJsonWithCsvCategoriesWithSpaces(csvJsonFile);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFilePath", "file:" + csvJsonFile.getAbsolutePath())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            // When
            JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

            // Then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

            // Store 조회
            Optional<Store> store = storeRepository.findByExternalId("test_external_csv_spaces_001");
            assertThat(store).isPresent();
            
            // 공백이 제거되고 정확히 3개의 카테고리로 분리되었는지 확인
            assertThat(store.get().getCategoryIds()).hasSize(3); // "한식, 일식, 양식" = 3개
        } finally {
            csvJsonFile.delete();
        }
    }

    @Test
    @Order(7)
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
                    "coordinates": {
                      "latitude": 37.6250,
                      "longitude": 127.0757
                    },
                    "phone_number": "02-1234-5678",
                    "menu_average": 8000,
                    "review_count": 100,
                    "images": [
                      "https://example.com/main.jpg"
                    ],
                    "openingHours": [
                      {
                        "dayOfWeek": "월",
                        "hours": {
                          "startTime": "09:00",
                          "endTime": "21:00"
                        }
                      }
                    ],
                    "menus": [
                      {
                        "name": "김치찌개",
                        "price": 7000
                      },
                      {
                        "name": "된장찌개",
                        "price": 7000
                      },
                      {
                        "name": "고가 메뉴",
                        "price": 3100036000
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
                    "coordinates": {
                      "latitude": 37.6250,
                      "longitude": 127.0757
                    },
                    "phone_number": "02-1234-5678",
                    "menu_average": 9000,
                    "review_count": 150,
                    "images": [
                      "https://example.com/updated.jpg"
                    ],
                    "openingHours": [
                      {
                        "dayOfWeek": "월요일",
                        "hours": {
                          "openTime": "09:00",
                          "closeTime": "22:00"
                        }
                      }
                    ],
                    "menus": [
                      {
                        "name": "김치찌개",
                        "price": 8000
                      },
                      {
                        "name": "제육볶음",
                        "price": 9000
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
     * CSV 형식의 카테고리를 포함한 테스트용 JSON 데이터 생성
     * 예: "한식,일식"
     */
    private void writeTestJsonWithCsvCategories(File file) throws IOException {
        String json = """
                [
                  {
                    "id": "test_external_csv_001",
                    "name": "CSV 카테고리 음식점",
                    "category": "한식,일식",
                    "address": "서울특별시 노원구 공릉동 789",
                    "coordinates": {
                      "latitude": 37.6250,
                      "longitude": 127.0757
                    },
                    "phone_number": "02-9999-9999",
                    "menu_average": 12000,
                    "review_count": 200,
                    "images": [
                      "https://example.com/csv-category.jpg"
                    ],
                    "openingHours": [
                      {
                        "dayOfWeek": "월",
                        "hours": {
                          "startTime": "10:00",
                          "endTime": "22:00"
                        }
                      }
                    ],
                    "menus": [
                      {
                        "name": "비빔밥",
                        "price": 9000
                      },
                      {
                        "name": "초밥",
                        "price": 15000
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
     * 공백을 포함한 CSV 형식의 카테고리를 포함한 테스트용 JSON 데이터 생성
     * 예: "한식, 일식, 양식"
     */
    private void writeTestJsonWithCsvCategoriesWithSpaces(File file) throws IOException {
        String json = """
                [
                  {
                    "id": "test_external_csv_spaces_001",
                    "name": "공백 포함 CSV 카테고리 음식점",
                    "category": "한식, 일식, 양식",
                    "address": "서울특별시 노원구 공릉동 999",
                    "coordinates": {
                      "latitude": 37.6250,
                      "longitude": 127.0757
                    },
                    "phone_number": "02-8888-8888",
                    "menu_average": 13000,
                    "review_count": 250,
                    "images": [
                      "https://example.com/csv-spaces.jpg"
                    ],
                    "openingHours": [
                      {
                        "dayOfWeek": "월",
                        "hours": {
                          "startTime": "11:00",
                          "endTime": "23:00"
                        }
                      }
                    ],
                    "menus": [
                      {
                        "name": "스테이크",
                        "price": 25000
                      },
                      {
                        "name": "함박스테이크",
                        "price": 18000
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
