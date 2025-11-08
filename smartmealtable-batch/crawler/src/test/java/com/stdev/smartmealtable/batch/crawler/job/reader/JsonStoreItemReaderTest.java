package com.stdev.smartmealtable.batch.crawler.job.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.batch.crawler.dto.CrawledStoreDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * JsonStoreItemReader 테스트
 * JSON 파일 읽기 및 메뉴 정보 파싱 검증
 */
@DisplayName("JsonStoreItemReader 테스트")
class JsonStoreItemReaderTest {

    @Nested
    @DisplayName("JSON 파싱 테스트")
    class JsonParsingTest {

        @Test
        @DisplayName("정상 JSON 파일 파싱 성공")
        void testParsingValidJsonFile() throws IOException {
            // Arrange
            String jsonContent = createValidStoreJson();
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);

            // Assert
            assertThat(reader.getTotalCount()).isGreaterThan(0);
            CrawledStoreDto store = reader.read();
            assertThat(store).isNotNull();
            assertThat(store.getName()).isEqualTo("테스트 가게");
            assertThat(store.getMenus()).isNotEmpty();
        }

        @Test
        @DisplayName("isMain 필드가 Boolean 타입으로 정상 파싱")
        void testParsingIsMainField() throws IOException {
            // Arrange
            String jsonContent = createValidStoreJson();
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);
            CrawledStoreDto store = reader.read();

            // Assert
            assertThat(store.getMenus()).isNotEmpty();
            CrawledStoreDto.MenuInfo menu = store.getMenus().get(0);
            assertThat(menu.getIsMain()).isNotNull();
            assertThat(menu.getIsMain()).isEqualTo(true);
        }

        @Test
        @DisplayName("Integer 범위 초과하는 가격은 메뉴 스킵")
        void testSkippingMenuWithIntegerOverflow() throws IOException {
            // Arrange
            String jsonContent = createStoreJsonWithIntegerOverflow();
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);
            CrawledStoreDto store = reader.read();

            // Assert - Integer 오버플로우 메뉴는 제거되고 정상 메뉴만 남음
            assertThat(store.getMenus()).isNotEmpty();
            assertThat(store.getMenus()).hasSize(1);
            assertThat(store.getMenus().get(0).getName()).isEqualTo("정상가격메뉴");
            assertThat(store.getMenus().get(0).getPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("복합 메뉴 정보 파싱")
        void testParsingComplexMenuInfo() throws IOException {
            // Arrange
            String jsonContent = createComplexStoreJson();
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);
            CrawledStoreDto store = reader.read();

            // Assert
            assertThat(store.getId()).isEqualTo("external-id-123");
            assertThat(store.getName()).isEqualTo("복잡한 가게");
            assertThat(store.getCategory()).isEqualTo("한식");
            assertThat(store.getAddress()).isEqualTo("서울시 강남구");
            assertThat(store.getPhoneNumber()).isEqualTo("02-1234-5678");
            assertThat(store.getReviewCount()).isEqualTo(100);
            
            assertThat(store.getMenus()).hasSize(2);
            CrawledStoreDto.MenuInfo mainMenu = store.getMenus().get(0);
            assertThat(mainMenu.getIsMain()).isTrue();
            assertThat(mainMenu.getName()).isEqualTo("메인메뉴");
            assertThat(mainMenu.getPrice()).isEqualTo(15000);
            assertThat(mainMenu.getIntroduce()).isEqualTo("대표메뉴");
        }

        @Test
        @DisplayName("null 메뉴 제거")
        void testRemovingNullMenus() throws IOException {
            // Arrange
            String jsonContent = """
                    [{
                        "id": "test-id",
                        "name": "테스트 가게",
                        "category": "카테고리",
                        "address": "주소",
                        "menus": [
                            {"isMain": true, "name": "메뉴1", "price": 10000},
                            null,
                            {"isMain": false, "name": "메뉴2", "price": 20000}
                        ]
                    }]
                    """;
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);
            CrawledStoreDto store = reader.read();

            // Assert
            assertThat(store.getMenus()).hasSize(2);
            assertThat(store.getMenus().stream()
                    .allMatch(menu -> menu != null))
                    .isTrue();
        }

        @Test
        @DisplayName("isMain 필드가 숫자 0/1로 표현될 경우 파싱")
        void testParsingIsMainAsNumber() throws IOException {
            // Arrange
            String jsonContent = """
                    [{
                        "id": "test-id",
                        "name": "테스트 가게",
                        "category": "카테고리",
                        "address": "주소",
                        "menus": [
                            {"isMain": 1, "name": "메뉴1", "price": 10000},
                            {"isMain": 0, "name": "메뉴2", "price": 20000}
                        ]
                    }]
                    """;
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);
            CrawledStoreDto store = reader.read();

            // Assert
            assertThat(store.getMenus()).hasSize(2);
            assertThat(store.getMenus().get(0).getIsMain()).isTrue();
            assertThat(store.getMenus().get(1).getIsMain()).isFalse();
        }

        @Test
        @DisplayName("좌표 정보 파싱")
        void testParsingCoordinates() throws IOException {
            // Arrange
            String jsonContent = """
                    [{
                        "id": "test-id",
                        "name": "테스트 가게",
                        "category": "카테고리",
                        "address": "주소",
                        "coordinates": {
                            "latitude": 37.123456,
                            "longitude": 127.123456
                        },
                        "menus": []
                    }]
                    """;
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);
            CrawledStoreDto store = reader.read();

            // Assert
            assertThat(store.getCoordinates()).isNotNull();
            assertThat(store.getCoordinates().getLatitude())
                    .isEqualByComparingTo(new BigDecimal("37.123456"));
            assertThat(store.getCoordinates().getLongitude())
                    .isEqualByComparingTo(new BigDecimal("127.123456"));
        }

        @Test
        @DisplayName("빈 JSON 배열 처리")
        void testParsingEmptyJsonArray() throws IOException {
            // Arrange
            String jsonContent = "[]";
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);

            // Assert
            assertThat(reader.getTotalCount()).isZero();
            assertThat(reader.read()).isNull();
        }

        @Test
        @DisplayName("CrawledStoreDto Integer 오버플로우 필드 처리")
        void testHandlingStoreIntegerOverflow() throws IOException {
            // Arrange - menu_average가 Integer.MAX_VALUE를 초과
            String jsonContent = """
                    [{
                        "id": "test-overflow-id",
                        "name": "오버플로우 가게",
                        "category": "한식",
                        "address": "서울시 강남구",
                        "menu_average": 26054019911,
                        "review_count": 500,
                        "menus": [
                            {"isMain": true, "name": "메뉴1", "price": 10000}
                        ]
                    }]
                    """;
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);
            CrawledStoreDto store = reader.read();

            // Assert - 오버플로우 필드는 null로 처리되고 가게는 정상적으로 파싱됨
            assertThat(store).isNotNull();
            assertThat(store.getName()).isEqualTo("오버플로우 가게");
            assertThat(store.getMenuAverage()).isNull(); // 오버플로우 필드는 null
            assertThat(store.getReviewCount()).isNotNull(); // review_count는 정상 범위 내
            assertThat(store.getMenus()).isNotEmpty();
        }

        @Test
        @DisplayName("여러 Integer 오버플로우 필드 처리")
        void testHandlingMultipleStoreIntegerOverflows() throws IOException {
            // Arrange - menu_average와 review_count 모두 오버플로우
            String jsonContent = """
                    [{
                        "id": "test-multi-overflow",
                        "name": "멀티 오버플로우 가게",
                        "category": "중식",
                        "address": "서울시 종로구",
                        "menu_average": 99999999999,
                        "review_count": 99999999999,
                        "menus": [
                            {"isMain": true, "name": "메뉴", "price": 12000}
                        ]
                    }]
                    """;
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);
            CrawledStoreDto store = reader.read();

            // Assert - 모든 오버플로우 필드는 null이지만 가게는 정상 파싱됨
            assertThat(store).isNotNull();
            assertThat(store.getName()).isEqualTo("멀티 오버플로우 가게");
            assertThat(store.getMenuAverage()).isNull();
            assertThat(store.getReviewCount()).isNull();
            assertThat(store.getMenus()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("멀티 아이템 읽기 테스트")
    class MultiItemReadingTest {

        @Test
        @DisplayName("여러 가게 순차 읽기")
        void testReadingMultipleStores() throws IOException {
            // Arrange
            String jsonContent = """
                    [
                        {
                            "id": "store-1",
                            "name": "가게1",
                            "category": "카테고리1",
                            "address": "주소1",
                            "menus": [{"isMain": true, "name": "메뉴1", "price": 10000}]
                        },
                        {
                            "id": "store-2",
                            "name": "가게2",
                            "category": "카테고리2",
                            "address": "주소2",
                            "menus": [{"isMain": false, "name": "메뉴2", "price": 20000}]
                        }
                    ]
                    """;
            Resource resource = createTempJsonResource(jsonContent);

            // Act
            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);

            // Assert
            CrawledStoreDto store1 = reader.read();
            CrawledStoreDto store2 = reader.read();
            CrawledStoreDto store3 = reader.read();

            assertThat(store1).isNotNull();
            assertThat(store1.getName()).isEqualTo("가게1");
            assertThat(store2).isNotNull();
            assertThat(store2.getName()).isEqualTo("가게2");
            assertThat(store3).isNull();
        }
    }

    // Helper methods

    private String createValidStoreJson() {
        return """
                [{
                    "id": "test-id",
                    "name": "테스트 가게",
                    "category": "카테고리",
                    "address": "주소",
                    "menus": [
                        {"isMain": true, "name": "메뉴", "price": 10000}
                    ]
                }]
                """;
    }

    private String createStoreJsonWithIntegerOverflow() {
        return """
                [{
                    "id": "test-id",
                    "name": "테스트 가게",
                    "category": "카테고리",
                    "address": "주소",
                    "menus": [
                        {"isMain": true, "name": "오버플로우메뉴", "price": 9999999999},
                        {"isMain": false, "name": "정상가격메뉴", "price": 10000}
                    ]
                }]
                """;
    }

    private String createComplexStoreJson() {
        return """
                [{
                    "id": "external-id-123",
                    "name": "복잡한 가게",
                    "category": "한식",
                    "address": "서울시 강남구",
                    "phone_number": "02-1234-5678",
                    "review_count": 100,
                    "menus": [
                        {
                            "isMain": true,
                            "name": "메인메뉴",
                            "introduce": "대표메뉴",
                            "price": 15000,
                            "imgUrl": "http://example.com/menu1.jpg"
                        },
                        {
                            "isMain": false,
                            "name": "사이드메뉴",
                            "price": 5000
                        }
                    ]
                }]
                """;
    }

    private Resource createTempJsonResource(String jsonContent) throws IOException {
        File tempFile = File.createTempFile("test-store", ".json");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(jsonContent);
        }

        return new org.springframework.core.io.FileSystemResource(tempFile);
    }
}
