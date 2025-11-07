package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StoreImage 도메인 ↔ JPA Entity 변환 테스트
 */
@DisplayName("StoreImageEntityMapping 테스트")
class StoreImageEntityMappingTest {

    @Nested
    @DisplayName("기본 필드 변환은")
    class Describe_basic_field_mapping {

        @Test
        @DisplayName("도메인 → Entity → 도메인 양방향 변환이 정상 동작한다")
        void roundtrip_fromDomain_toDomain() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeImageId(1L)
                    .storeId(100L)
                    .imageUrl("https://example.com/store1.jpg")
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(mapped.getStoreImageId()).isEqualTo(1L);
            assertThat(mapped.getStoreId()).isEqualTo(100L);
            assertThat(mapped.getImageUrl()).isEqualTo("https://example.com/store1.jpg");
            assertThat(mapped.isMain()).isTrue();
            assertThat(mapped.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("ID 없이 생성된 도메인도 변환이 정상 동작한다")
        void roundtrip_without_id() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeId(101L)
                    .imageUrl("https://example.com/store2.jpg")
                    .isMain(false)
                    .displayOrder(2)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(mapped.getStoreImageId()).isNull();
            assertThat(mapped.getStoreId()).isEqualTo(101L);
            assertThat(mapped.getImageUrl()).isEqualTo("https://example.com/store2.jpg");
            assertThat(mapped.isMain()).isFalse();
            assertThat(mapped.getDisplayOrder()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("대표 이미지 필드 변환은")
    class Describe_main_image_mapping {

        @Test
        @DisplayName("대표 이미지(isMain=true) 변환이 정상 동작한다")
        void roundtrip_main_image() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeImageId(2L)
                    .storeId(102L)
                    .imageUrl("https://example.com/main.jpg")
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(entity.getIsMain()).isTrue();
            assertThat(mapped.isMain()).isTrue();
        }

        @Test
        @DisplayName("일반 이미지(isMain=false) 변환이 정상 동작한다")
        void roundtrip_non_main_image() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeImageId(3L)
                    .storeId(103L)
                    .imageUrl("https://example.com/sub.jpg")
                    .isMain(false)
                    .displayOrder(3)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(entity.getIsMain()).isFalse();
            assertThat(mapped.isMain()).isFalse();
        }
    }

    @Nested
    @DisplayName("displayOrder 필드 변환은")
    class Describe_display_order_mapping {

        @Test
        @DisplayName("displayOrder가 설정된 경우 변환이 정상 동작한다")
        void roundtrip_with_display_order() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeImageId(4L)
                    .storeId(104L)
                    .imageUrl("https://example.com/image1.jpg")
                    .displayOrder(5)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(mapped.getDisplayOrder()).isEqualTo(5);
        }

        @Test
        @DisplayName("displayOrder가 null인 경우 변환이 정상 동작한다")
        void roundtrip_with_null_display_order() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeImageId(5L)
                    .storeId(105L)
                    .imageUrl("https://example.com/image2.jpg")
                    .displayOrder(null)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(mapped.getDisplayOrder()).isNull();
        }

        @Test
        @DisplayName("displayOrder가 0인 경우 변환이 정상 동작한다")
        void roundtrip_with_zero_display_order() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeImageId(6L)
                    .storeId(106L)
                    .imageUrl("https://example.com/first.jpg")
                    .displayOrder(0)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(mapped.getDisplayOrder()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class Describe_edge_cases {

        @Test
        @DisplayName("매우 긴 URL도 변환이 정상 동작한다")
        void roundtrip_with_long_url() {
            // Given
            String longUrl = "https://example.com/very/long/path/to/image/file/name/that/is/extremely/long/for/testing/purposes/image.jpg";
            StoreImage original = StoreImage.builder()
                    .storeImageId(7L)
                    .storeId(107L)
                    .imageUrl(longUrl)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(mapped.getImageUrl()).isEqualTo(longUrl);
        }

        @Test
        @DisplayName("모든 필드가 설정된 경우 변환이 정상 동작한다")
        void roundtrip_with_all_fields() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeImageId(8L)
                    .storeId(108L)
                    .imageUrl("https://example.com/complete.jpg")
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(mapped.getStoreImageId()).isEqualTo(8L);
            assertThat(mapped.getStoreId()).isEqualTo(108L);
            assertThat(mapped.getImageUrl()).isEqualTo("https://example.com/complete.jpg");
            assertThat(mapped.isMain()).isTrue();
            assertThat(mapped.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("최소 필드만 설정된 경우 변환이 정상 동작한다")
        void roundtrip_with_minimal_fields() {
            // Given
            StoreImage original = StoreImage.builder()
                    .storeId(109L)
                    .imageUrl("https://example.com/minimal.jpg")
                    .build();

            // When
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(original);
            StoreImage mapped = StoreImageEntityMapper.toDomain(entity);

            // Then
            assertThat(mapped.getStoreId()).isEqualTo(109L);
            assertThat(mapped.getImageUrl()).isEqualTo("https://example.com/minimal.jpg");
        }
    }
}
