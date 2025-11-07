package com.stdev.smartmealtable.domain.food;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Food 도메인 엔티티 단위 테스트
 */
@DisplayName("Food 도메인 엔티티 테스트")
class FoodTest {

    @Nested
    @DisplayName("reconstitute 메서드는")
    class Describe_reconstitute {

        @Test
        @DisplayName("기본 필드로 Food 객체를 재구성한다")
        void it_reconstitutes_food_with_basic_fields() {
            // Given
            Long foodId = 1L;
            String foodName = "김치찌개";
            Long storeId = 100L;
            Long categoryId = 1L;
            String description = "맛있는 김치찌개";
            String imageUrl = "https://example.com/kimchi.jpg";
            Integer averagePrice = 8000;

            // When
            Food food = Food.reconstitute(foodId, foodName, storeId, categoryId, description, imageUrl, averagePrice);

            // Then
            assertThat(food).isNotNull();
            assertThat(food.getFoodId()).isEqualTo(foodId);
            assertThat(food.getFoodName()).isEqualTo(foodName);
            assertThat(food.getStoreId()).isEqualTo(storeId);
            assertThat(food.getCategoryId()).isEqualTo(categoryId);
            assertThat(food.getDescription()).isEqualTo(description);
            assertThat(food.getImageUrl()).isEqualTo(imageUrl);
            assertThat(food.getAveragePrice()).isEqualTo(averagePrice);
        }
    }

    @Nested
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("ID 없이 새로운 Food 객체를 생성한다")
        void it_creates_new_food_without_id() {
            // Given
            String foodName = "된장찌개";
            Long storeId = 101L;
            Long categoryId = 1L;
            String description = "구수한 된장찌개";
            String imageUrl = "https://example.com/doenjang.jpg";
            Integer averagePrice = 7000;

            // When
            Food food = Food.create(foodName, storeId, categoryId, description, imageUrl, averagePrice, false, null);

            // Then
            assertThat(food).isNotNull();
            assertThat(food.getFoodId()).isNull();
            assertThat(food.getFoodName()).isEqualTo(foodName);
            assertThat(food.getStoreId()).isEqualTo(storeId);
            assertThat(food.getCategoryId()).isEqualTo(categoryId);
            assertThat(food.getDescription()).isEqualTo(description);
            assertThat(food.getImageUrl()).isEqualTo(imageUrl);
            assertThat(food.getAveragePrice()).isEqualTo(averagePrice);
            assertThat(food.getIsMain()).isFalse();
            assertThat(food.getDisplayOrder()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder를 사용하면")
    class Describe_builder {

        @Test
        @DisplayName("모든 필드를 포함한 Food 객체를 생성할 수 있다")
        void it_creates_food_with_all_fields() {
            // Given
            Long foodId = 1L;
            String foodName = "비빔밥";
            Long storeId = 100L;
            Long categoryId = 1L;
            String description = "건강한 비빔밥";
            String imageUrl = "https://example.com/bibimbap.jpg";
            Integer averagePrice = 9000;
            Integer price = 9000;
            Boolean isMain = true;
            Integer displayOrder = 1;
            LocalDateTime registeredDt = LocalDateTime.of(2025, 11, 7, 10, 0);
            LocalDateTime deletedAt = null;

            // When
            Food food = Food.builder()
                    .foodId(foodId)
                    .foodName(foodName)
                    .storeId(storeId)
                    .categoryId(categoryId)
                    .description(description)
                    .imageUrl(imageUrl)
                    .averagePrice(averagePrice)
                    .price(price)
                    .isMain(isMain)
                    .displayOrder(displayOrder)
                    .registeredDt(registeredDt)
                    .deletedAt(deletedAt)
                    .build();

            // Then
            assertThat(food).isNotNull();
            assertThat(food.getFoodId()).isEqualTo(foodId);
            assertThat(food.getFoodName()).isEqualTo(foodName);
            assertThat(food.getStoreId()).isEqualTo(storeId);
            assertThat(food.getCategoryId()).isEqualTo(categoryId);
            assertThat(food.getDescription()).isEqualTo(description);
            assertThat(food.getImageUrl()).isEqualTo(imageUrl);
            assertThat(food.getAveragePrice()).isEqualTo(averagePrice);
            assertThat(food.getPrice()).isEqualTo(price);
            assertThat(food.getIsMain()).isEqualTo(isMain);
            assertThat(food.getDisplayOrder()).isEqualTo(displayOrder);
            assertThat(food.getRegisteredDt()).isEqualTo(registeredDt);
            assertThat(food.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("크롤러용 필드만으로 Food 객체를 생성할 수 있다")
        void it_creates_food_with_crawler_fields() {
            // Given
            String foodName = "제육볶음";
            Long storeId = 102L;
            Long categoryId = 1L;
            Integer price = 10000;
            Boolean isMain = false;
            Integer displayOrder = 3;
            LocalDateTime registeredDt = LocalDateTime.now();

            // When
            Food food = Food.builder()
                    .foodName(foodName)
                    .storeId(storeId)
                    .categoryId(categoryId)
                    .price(price)
                    .isMain(isMain)
                    .displayOrder(displayOrder)
                    .registeredDt(registeredDt)
                    .build();

            // Then
            assertThat(food).isNotNull();
            assertThat(food.getFoodName()).isEqualTo(foodName);
            assertThat(food.getPrice()).isEqualTo(price);
            assertThat(food.getIsMain()).isEqualTo(isMain);
            assertThat(food.getDisplayOrder()).isEqualTo(displayOrder);
            assertThat(food.getRegisteredDt()).isEqualTo(registeredDt);
            assertThat(food.getAveragePrice()).isNull();
        }
    }

    @Nested
    @DisplayName("isValid 메서드는")
    class Describe_isValid {

        @Test
        @DisplayName("averagePrice가 있으면 유효하다고 판단한다")
        void it_returns_true_with_average_price() {
            // Given
            Food food = Food.builder()
                    .foodName("김치찌개")
                    .storeId(1L)
                    .categoryId(1L)
                    .averagePrice(8000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("price가 있으면 유효하다고 판단한다")
        void it_returns_true_with_price() {
            // Given
            Food food = Food.builder()
                    .foodName("된장찌개")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(7000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("price와 averagePrice 둘 다 있어도 유효하다")
        void it_returns_true_with_both_prices() {
            // Given
            Food food = Food.builder()
                    .foodName("비빔밥")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(9000)
                    .averagePrice(9000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("foodName이 null이면 유효하지 않다")
        void it_returns_false_with_null_food_name() {
            // Given
            Food food = Food.builder()
                    .foodName(null)
                    .storeId(1L)
                    .categoryId(1L)
                    .averagePrice(8000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("foodName이 빈 문자열이면 유효하지 않다")
        void it_returns_false_with_empty_food_name() {
            // Given
            Food food = Food.builder()
                    .foodName("   ")
                    .storeId(1L)
                    .categoryId(1L)
                    .averagePrice(8000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("price와 averagePrice가 둘 다 null이면 유효하지 않다")
        void it_returns_false_without_any_price() {
            // Given
            Food food = Food.builder()
                    .foodName("김치찌개")
                    .storeId(1L)
                    .categoryId(1L)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("price가 음수면 유효하지 않다")
        void it_returns_false_with_negative_price() {
            // Given
            Food food = Food.builder()
                    .foodName("김치찌개")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(-1000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("averagePrice가 음수면 유효하지 않다")
        void it_returns_false_with_negative_average_price() {
            // Given
            Food food = Food.builder()
                    .foodName("김치찌개")
                    .storeId(1L)
                    .categoryId(1L)
                    .averagePrice(-1000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("storeId가 null이면 유효하지 않다")
        void it_returns_false_without_store_id() {
            // Given
            Food food = Food.builder()
                    .foodName("김치찌개")
                    .categoryId(1L)
                    .averagePrice(8000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("categoryId가 null이면 유효하지 않다")
        void it_returns_false_without_category_id() {
            // Given
            Food food = Food.builder()
                    .foodName("김치찌개")
                    .storeId(1L)
                    .averagePrice(8000)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("price가 0이면 유효하다")
        void it_returns_true_with_zero_price() {
            // Given
            Food food = Food.builder()
                    .foodName("무료 음식")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(0)
                    .build();

            // When
            boolean result = food.isValid();

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("isMainFood 메서드는")
    class Describe_isMainFood {

        @Test
        @DisplayName("isMain이 true이면 true를 반환한다")
        void it_returns_true_when_is_main_is_true() {
            // Given
            Food food = Food.builder()
                    .foodName("대표메뉴")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(10000)
                    .isMain(true)
                    .build();

            // When
            boolean result = food.isMainFood();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isMain이 false이면 false를 반환한다")
        void it_returns_false_when_is_main_is_false() {
            // Given
            Food food = Food.builder()
                    .foodName("일반메뉴")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(8000)
                    .isMain(false)
                    .build();

            // When
            boolean result = food.isMainFood();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isMain이 null이면 false를 반환한다")
        void it_returns_false_when_is_main_is_null() {
            // Given
            Food food = Food.builder()
                    .foodName("메뉴")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(8000)
                    .build();

            // When
            boolean result = food.isMainFood();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isDeleted 메서드는")
    class Describe_isDeleted {

        @Test
        @DisplayName("deletedAt이 null이면 false를 반환한다")
        void it_returns_false_when_deleted_at_is_null() {
            // Given
            Food food = Food.builder()
                    .foodName("활성 메뉴")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(8000)
                    .deletedAt(null)
                    .build();

            // When
            boolean result = food.isDeleted();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("deletedAt이 설정되어 있으면 true를 반환한다")
        void it_returns_true_when_deleted_at_is_set() {
            // Given
            LocalDateTime deletedAt = LocalDateTime.of(2025, 11, 7, 15, 30);
            Food food = Food.builder()
                    .foodName("삭제된 메뉴")
                    .storeId(1L)
                    .categoryId(1L)
                    .price(8000)
                    .deletedAt(deletedAt)
                    .build();

            // When
            boolean result = food.isDeleted();

            // Then
            assertThat(result).isTrue();
        }
    }
}
