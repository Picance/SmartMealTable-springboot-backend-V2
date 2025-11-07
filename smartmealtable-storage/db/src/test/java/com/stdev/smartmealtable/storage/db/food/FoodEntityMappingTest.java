package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.Food;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Food 도메인 ↔ JPA Entity 변환 테스트
 */
@DisplayName("FoodEntityMapping 테스트")
class FoodEntityMappingTest {

    @Nested
    @DisplayName("기본 필드 변환은")
    class Describe_basic_field_mapping {

        @Test
        @DisplayName("도메인 → Entity → 도메인 양방향 변환이 정상 동작한다")
        void roundtrip_fromDomain_toDomain() {
            // Given
            Food original = Food.reconstitute(10L, "Sushi", 1L, 3L, "d", "i", 7000);

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(mapped.getFoodId()).isEqualTo(10L);
            assertThat(mapped.getFoodName()).isEqualTo("Sushi");
            assertThat(mapped.getCategoryId()).isEqualTo(3L);
            assertThat(mapped.getAveragePrice()).isEqualTo(7000);
        }
    }

    @Nested
    @DisplayName("크롤러 필드 변환은")
    class Describe_crawler_field_mapping {

        @Test
        @DisplayName("모든 크롤러 필드를 포함한 양방향 변환이 정상 동작한다")
        void roundtrip_with_all_crawler_fields() {
            // Given
            LocalDateTime registeredDt = LocalDateTime.of(2025, 11, 7, 10, 0);
            Food original = Food.builder()
                    .foodId(1L)
                    .foodName("김치찌개")
                    .storeId(100L)
                    .categoryId(1L)
                    .description("맛있는 김치찌개")
                    .imageUrl("https://example.com/kimchi.jpg")
                    .price(8000)
                    .averagePrice(8000)
                    .isMain(true)
                    .displayOrder(1)
                    .registeredDt(registeredDt)
                    .deletedAt(null)
                    .build();

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(mapped.getFoodId()).isEqualTo(1L);
            assertThat(mapped.getFoodName()).isEqualTo("김치찌개");
            assertThat(mapped.getStoreId()).isEqualTo(100L);
            assertThat(mapped.getCategoryId()).isEqualTo(1L);
            assertThat(mapped.getDescription()).isEqualTo("맛있는 김치찌개");
            assertThat(mapped.getImageUrl()).isEqualTo("https://example.com/kimchi.jpg");
            assertThat(mapped.getPrice()).isEqualTo(8000);
            assertThat(mapped.getAveragePrice()).isEqualTo(8000);
            assertThat(mapped.getIsMain()).isTrue();
            assertThat(mapped.getDisplayOrder()).isEqualTo(1);
            assertThat(mapped.getRegisteredDt()).isEqualTo(registeredDt);
            assertThat(mapped.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("price만 있고 averagePrice가 null인 경우 변환이 정상 동작한다")
        void roundtrip_with_price_only() {
            // Given
            Food original = Food.builder()
                    .foodId(2L)
                    .foodName("된장찌개")
                    .storeId(101L)
                    .categoryId(1L)
                    .price(7000)
                    .isMain(false)
                    .displayOrder(2)
                    .build();

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(mapped.getPrice()).isEqualTo(7000);
            assertThat(mapped.getAveragePrice()).isEqualTo(7000); // price를 averagePrice로도 매핑
            assertThat(mapped.getIsMain()).isFalse();
            assertThat(mapped.getDisplayOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("averagePrice만 있고 price가 null인 경우 변환이 정상 동작한다")
        void roundtrip_with_average_price_only() {
            // Given
            Food original = Food.builder()
                    .foodId(3L)
                    .foodName("비빔밥")
                    .storeId(102L)
                    .categoryId(1L)
                    .averagePrice(9000)
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(mapped.getPrice()).isEqualTo(9000);
            assertThat(mapped.getAveragePrice()).isEqualTo(9000);
        }

        @Test
        @DisplayName("isMain이 null인 경우 false로 변환된다")
        void isMain_null_converts_to_false() {
            // Given
            Food original = Food.builder()
                    .foodId(4L)
                    .foodName("제육볶음")
                    .storeId(103L)
                    .categoryId(1L)
                    .price(10000)
                    .isMain(null)
                    .build();

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(entity.getIsMain()).isFalse();
            assertThat(mapped.getIsMain()).isFalse();
        }

        @Test
        @DisplayName("deletedAt이 설정된 경우 변환이 정상 동작한다")
        void roundtrip_with_deleted_at() {
            // Given
            LocalDateTime deletedAt = LocalDateTime.of(2025, 11, 7, 15, 30);
            Food original = Food.builder()
                    .foodId(5L)
                    .foodName("삭제된 메뉴")
                    .storeId(104L)
                    .categoryId(1L)
                    .price(8000)
                    .deletedAt(deletedAt)
                    .build();

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(mapped.getDeletedAt()).isEqualTo(deletedAt);
            assertThat(mapped.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class Describe_edge_cases {

        @Test
        @DisplayName("displayOrder가 null인 경우 변환이 정상 동작한다")
        void roundtrip_with_null_display_order() {
            // Given
            Food original = Food.builder()
                    .foodId(6L)
                    .foodName("순서 없는 메뉴")
                    .storeId(105L)
                    .categoryId(1L)
                    .price(5000)
                    .displayOrder(null)
                    .build();

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(mapped.getDisplayOrder()).isNull();
        }

        @Test
        @DisplayName("description과 imageUrl이 null인 경우 변환이 정상 동작한다")
        void roundtrip_with_null_optional_fields() {
            // Given
            Food original = Food.builder()
                    .foodId(7L)
                    .foodName("최소 정보 메뉴")
                    .storeId(106L)
                    .categoryId(1L)
                    .price(6000)
                    .description(null)
                    .imageUrl(null)
                    .build();

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(mapped.getDescription()).isNull();
            assertThat(mapped.getImageUrl()).isNull();
        }

        @Test
        @DisplayName("price가 0인 경우 변환이 정상 동작한다")
        void roundtrip_with_zero_price() {
            // Given
            Food original = Food.builder()
                    .foodId(8L)
                    .foodName("무료 음식")
                    .storeId(107L)
                    .categoryId(1L)
                    .price(0)
                    .build();

            // When
            FoodJpaEntity entity = FoodJpaEntity.fromDomain(original);
            Food mapped = entity.toDomain();

            // Then
            assertThat(mapped.getPrice()).isEqualTo(0);
            assertThat(mapped.getAveragePrice()).isEqualTo(0);
        }
    }
}
