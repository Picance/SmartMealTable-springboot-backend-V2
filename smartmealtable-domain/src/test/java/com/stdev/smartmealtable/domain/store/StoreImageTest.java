package com.stdev.smartmealtable.domain.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StoreImage 도메인 엔티티 단위 테스트
 */
@DisplayName("StoreImage 도메인 엔티티 테스트")
class StoreImageTest {

    @Nested
    @DisplayName("Builder를 사용하면")
    class Describe_builder {

        @Test
        @DisplayName("모든 필드를 포함한 StoreImage 객체를 생성할 수 있다")
        void it_creates_store_image_with_all_fields() {
            // Given
            Long storeImageId = 1L;
            Long storeId = 100L;
            String imageUrl = "https://example.com/store1.jpg";
            boolean isMain = true;
            Integer displayOrder = 1;

            // When
            StoreImage storeImage = StoreImage.builder()
                    .storeImageId(storeImageId)
                    .storeId(storeId)
                    .imageUrl(imageUrl)
                    .isMain(isMain)
                    .displayOrder(displayOrder)
                    .build();

            // Then
            assertThat(storeImage).isNotNull();
            assertThat(storeImage.getStoreImageId()).isEqualTo(storeImageId);
            assertThat(storeImage.getStoreId()).isEqualTo(storeId);
            assertThat(storeImage.getImageUrl()).isEqualTo(imageUrl);
            assertThat(storeImage.isMain()).isTrue();
            assertThat(storeImage.getDisplayOrder()).isEqualTo(displayOrder);
        }

        @Test
        @DisplayName("ID 없이 새로운 StoreImage 객체를 생성할 수 있다")
        void it_creates_new_store_image_without_id() {
            // Given
            Long storeId = 101L;
            String imageUrl = "https://example.com/store2.jpg";
            boolean isMain = false;
            Integer displayOrder = 2;

            // When
            StoreImage storeImage = StoreImage.builder()
                    .storeId(storeId)
                    .imageUrl(imageUrl)
                    .isMain(isMain)
                    .displayOrder(displayOrder)
                    .build();

            // Then
            assertThat(storeImage).isNotNull();
            assertThat(storeImage.getStoreImageId()).isNull();
            assertThat(storeImage.getStoreId()).isEqualTo(storeId);
            assertThat(storeImage.getImageUrl()).isEqualTo(imageUrl);
            assertThat(storeImage.isMain()).isFalse();
            assertThat(storeImage.getDisplayOrder()).isEqualTo(displayOrder);
        }

        @Test
        @DisplayName("필수 필드만으로 StoreImage 객체를 생성할 수 있다")
        void it_creates_store_image_with_required_fields_only() {
            // Given
            Long storeId = 102L;
            String imageUrl = "https://example.com/store3.jpg";

            // When
            StoreImage storeImage = StoreImage.builder()
                    .storeId(storeId)
                    .imageUrl(imageUrl)
                    .build();

            // Then
            assertThat(storeImage).isNotNull();
            assertThat(storeImage.getStoreId()).isEqualTo(storeId);
            assertThat(storeImage.getImageUrl()).isEqualTo(imageUrl);
        }

        @Test
        @DisplayName("대표 이미지로 StoreImage 객체를 생성할 수 있다")
        void it_creates_main_store_image() {
            // Given
            Long storeId = 103L;
            String imageUrl = "https://example.com/main.jpg";

            // When
            StoreImage storeImage = StoreImage.builder()
                    .storeId(storeId)
                    .imageUrl(imageUrl)
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            // Then
            assertThat(storeImage.isMain()).isTrue();
            assertThat(storeImage.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("일반 이미지로 StoreImage 객체를 생성할 수 있다")
        void it_creates_non_main_store_image() {
            // Given
            Long storeId = 104L;
            String imageUrl = "https://example.com/sub.jpg";

            // When
            StoreImage storeImage = StoreImage.builder()
                    .storeId(storeId)
                    .imageUrl(imageUrl)
                    .isMain(false)
                    .displayOrder(3)
                    .build();

            // Then
            assertThat(storeImage.isMain()).isFalse();
            assertThat(storeImage.getDisplayOrder()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("isValid 메서드는")
    class Describe_isValid {

        @Test
        @DisplayName("imageUrl과 storeId가 모두 있으면 유효하다고 판단한다")
        void it_returns_true_with_required_fields() {
            // Given
            StoreImage storeImage = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl("https://example.com/valid.jpg")
                    .build();

            // When
            boolean result = storeImage.isValid();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("imageUrl이 null이면 유효하지 않다")
        void it_returns_false_with_null_image_url() {
            // Given
            StoreImage storeImage = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl(null)
                    .build();

            // When
            boolean result = storeImage.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("imageUrl이 빈 문자열이면 유효하지 않다")
        void it_returns_false_with_empty_image_url() {
            // Given
            StoreImage storeImage = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl("   ")
                    .build();

            // When
            boolean result = storeImage.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("storeId가 null이면 유효하지 않다")
        void it_returns_false_with_null_store_id() {
            // Given
            StoreImage storeImage = StoreImage.builder()
                    .storeId(null)
                    .imageUrl("https://example.com/image.jpg")
                    .build();

            // When
            boolean result = storeImage.isValid();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("모든 필드가 유효한 값이면 유효하다")
        void it_returns_true_with_all_valid_fields() {
            // Given
            StoreImage storeImage = StoreImage.builder()
                    .storeImageId(1L)
                    .storeId(100L)
                    .imageUrl("https://example.com/complete.jpg")
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            // When
            boolean result = storeImage.isValid();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("displayOrder가 null이어도 유효하다")
        void it_returns_true_with_null_display_order() {
            // Given
            StoreImage storeImage = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl("https://example.com/image.jpg")
                    .displayOrder(null)
                    .build();

            // When
            boolean result = storeImage.isValid();

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class Describe_edge_cases {

        @Test
        @DisplayName("displayOrder가 0인 경우 정상 처리된다")
        void it_handles_zero_display_order() {
            // Given
            StoreImage storeImage = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl("https://example.com/first.jpg")
                    .displayOrder(0)
                    .build();

            // When & Then
            assertThat(storeImage.getDisplayOrder()).isEqualTo(0);
            assertThat(storeImage.isValid()).isTrue();
        }

        @Test
        @DisplayName("매우 긴 URL도 정상 처리된다")
        void it_handles_long_url() {
            // Given
            String longUrl = "https://example.com/very/long/path/to/image/file/name/that/is/extremely/long/image.jpg";
            StoreImage storeImage = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl(longUrl)
                    .build();

            // When & Then
            assertThat(storeImage.getImageUrl()).isEqualTo(longUrl);
            assertThat(storeImage.isValid()).isTrue();
        }

        @Test
        @DisplayName("isMain이 명시되지 않아도 정상 처리된다")
        void it_handles_unspecified_is_main() {
            // Given
            StoreImage storeImage = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl("https://example.com/image.jpg")
                    .build();

            // When & Then
            assertThat(storeImage.isValid()).isTrue();
        }
    }
}
