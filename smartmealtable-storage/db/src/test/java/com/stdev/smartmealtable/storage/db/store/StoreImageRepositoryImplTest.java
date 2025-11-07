package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StoreImageRepositoryImpl 단위 테스트 (Mockist 스타일)
 */
@DisplayName("StoreImageRepositoryImpl 테스트")
class StoreImageRepositoryImplTest {

    @Mock
    private StoreImageJpaRepository storeImageJpaRepository;

    private StoreImageRepositoryImpl storeImageRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeImageRepository = new StoreImageRepositoryImpl(storeImageJpaRepository);
    }

    @Nested
    @DisplayName("save 메서드는")
    class Describe_save {

        @Test
        @DisplayName("도메인을 Entity로 변환하여 저장하고 도메인으로 반환한다")
        void it_converts_and_saves_domain() {
            // Given
            StoreImage domain = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl("https://example.com/store.jpg")
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            StoreImageJpaEntity savedEntity = StoreImageEntityMapper.toJpaEntity(
                    StoreImage.builder()
                            .storeImageId(1L)
                            .storeId(100L)
                            .imageUrl("https://example.com/store.jpg")
                            .isMain(true)
                            .displayOrder(1)
                            .build()
            );

            when(storeImageJpaRepository.save(any(StoreImageJpaEntity.class))).thenReturn(savedEntity);

            // When
            StoreImage result = storeImageRepository.save(domain);

            // Then
            ArgumentCaptor<StoreImageJpaEntity> captor = ArgumentCaptor.forClass(StoreImageJpaEntity.class);
            verify(storeImageJpaRepository, times(1)).save(captor.capture());

            StoreImageJpaEntity capturedEntity = captor.getValue();
            assertThat(capturedEntity.getStoreId()).isEqualTo(100L);
            assertThat(capturedEntity.getImageUrl()).isEqualTo("https://example.com/store.jpg");

            assertThat(result).isNotNull();
            assertThat(result.getStoreImageId()).isEqualTo(1L);
            assertThat(result.getStoreId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("대표 이미지를 저장할 수 있다")
        void it_saves_main_image() {
            // Given
            StoreImage mainImage = StoreImage.builder()
                    .storeId(101L)
                    .imageUrl("https://example.com/main.jpg")
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            StoreImageJpaEntity savedEntity = StoreImageEntityMapper.toJpaEntity(
                    StoreImage.builder()
                            .storeImageId(2L)
                            .storeId(101L)
                            .imageUrl("https://example.com/main.jpg")
                            .isMain(true)
                            .displayOrder(1)
                            .build()
            );

            when(storeImageJpaRepository.save(any(StoreImageJpaEntity.class))).thenReturn(savedEntity);

            // When
            StoreImage result = storeImageRepository.save(mainImage);

            // Then
            assertThat(result.isMain()).isTrue();
            assertThat(result.getDisplayOrder()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findById 메서드는")
    class Describe_findById {

        @Test
        @DisplayName("존재하는 ID로 조회 시 도메인을 반환한다")
        void it_returns_domain_when_exists() {
            // Given
            Long storeImageId = 1L;
            StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(
                    StoreImage.builder()
                            .storeImageId(storeImageId)
                            .storeId(100L)
                            .imageUrl("https://example.com/image.jpg")
                            .isMain(true)
                            .displayOrder(1)
                            .build()
            );

            when(storeImageJpaRepository.findById(storeImageId)).thenReturn(Optional.of(entity));

            // When
            Optional<StoreImage> result = storeImageRepository.findById(storeImageId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getStoreImageId()).isEqualTo(storeImageId);
            assertThat(result.get().getStoreId()).isEqualTo(100L);
            verify(storeImageJpaRepository, times(1)).findById(storeImageId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 empty Optional을 반환한다")
        void it_returns_empty_optional_when_not_exists() {
            // Given
            Long storeImageId = 999L;
            when(storeImageJpaRepository.findById(storeImageId)).thenReturn(Optional.empty());

            // When
            Optional<StoreImage> result = storeImageRepository.findById(storeImageId);

            // Then
            assertThat(result).isEmpty();
            verify(storeImageJpaRepository, times(1)).findById(storeImageId);
        }
    }

    @Nested
    @DisplayName("deleteByStoreId 메서드는")
    class Describe_deleteByStoreId {

        @Test
        @DisplayName("가게 ID로 모든 이미지를 삭제한다")
        void it_deletes_all_images_for_store() {
            // Given
            Long storeId = 100L;

            // When
            storeImageRepository.deleteByStoreId(storeId);

            // Then
            verify(storeImageJpaRepository, times(1)).deleteByStoreId(storeId);
        }

        @Test
        @DisplayName("여러 번 호출해도 정상 동작한다")
        void it_works_when_called_multiple_times() {
            // Given
            Long storeId = 101L;

            // When
            storeImageRepository.deleteByStoreId(storeId);
            storeImageRepository.deleteByStoreId(storeId);

            // Then
            verify(storeImageJpaRepository, times(2)).deleteByStoreId(storeId);
        }
    }

    @Nested
    @DisplayName("Integration 시나리오 테스트")
    class Describe_integration_scenarios {

        @Test
        @DisplayName("저장 후 조회가 정상 동작한다")
        void it_saves_and_finds() {
            // Given
            StoreImage domain = StoreImage.builder()
                    .storeId(100L)
                    .imageUrl("https://example.com/test.jpg")
                    .isMain(true)
                    .displayOrder(1)
                    .build();

            StoreImageJpaEntity savedEntity = StoreImageEntityMapper.toJpaEntity(
                    StoreImage.builder()
                            .storeImageId(1L)
                            .storeId(100L)
                            .imageUrl("https://example.com/test.jpg")
                            .isMain(true)
                            .displayOrder(1)
                            .build()
            );

            when(storeImageJpaRepository.save(any(StoreImageJpaEntity.class))).thenReturn(savedEntity);
            when(storeImageJpaRepository.findById(1L)).thenReturn(Optional.of(savedEntity));

            // When
            StoreImage saved = storeImageRepository.save(domain);
            Optional<StoreImage> foundOptional = storeImageRepository.findById(saved.getStoreImageId());
            StoreImage found = foundOptional.orElseThrow();

            // Then
            assertThat(found).isNotNull();
            assertThat(found.getStoreImageId()).isEqualTo(saved.getStoreImageId());
            assertThat(found.getImageUrl()).isEqualTo(saved.getImageUrl());
        }
    }

    @Nested
    @DisplayName("findByStoreId 메서드는")
    class Describe_findByStoreId {

        @Test
        @DisplayName("가게의 모든 이미지를 isMain 우선, displayOrder 순으로 정렬하여 반환한다")
        void it_returns_images_sorted_by_isMain_and_displayOrder() {
            // Given
            Long storeId = 100L;

            List<StoreImageJpaEntity> entities = List.of(
                    createImageEntity(3L, storeId, "https://example.com/image3.jpg", false, 3),
                    createImageEntity(1L, storeId, "https://example.com/image1.jpg", true, 1),
                    createImageEntity(4L, storeId, "https://example.com/image4.jpg", false, 4),
                    createImageEntity(2L, storeId, "https://example.com/image2.jpg", true, 2)
            );

            when(storeImageJpaRepository.findByStoreIdOrderByIsMainDescDisplayOrderAsc(storeId))
                    .thenReturn(entities);

            // When
            List<StoreImage> result = storeImageRepository.findByStoreId(storeId);

            // Then
            assertThat(result).hasSize(4);
            verify(storeImageJpaRepository, times(1))
                    .findByStoreIdOrderByIsMainDescDisplayOrderAsc(storeId);
        }

        @Test
        @DisplayName("이미지가 없는 경우 빈 리스트를 반환한다")
        void it_returns_empty_list_when_no_images() {
            // Given
            Long storeId = 999L;
            when(storeImageJpaRepository.findByStoreIdOrderByIsMainDescDisplayOrderAsc(storeId))
                    .thenReturn(List.of());

            // When
            List<StoreImage> result = storeImageRepository.findByStoreId(storeId);

            // Then
            assertThat(result).isEmpty();
            verify(storeImageJpaRepository, times(1))
                    .findByStoreIdOrderByIsMainDescDisplayOrderAsc(storeId);
        }
    }

    @Nested
    @DisplayName("findByStoreIdAndIsMainTrue 메서드는")
    class Describe_findByStoreIdAndIsMainTrue {

        @Test
        @DisplayName("대표 이미지를 반환한다")
        void it_returns_main_image() {
            // Given
            Long storeId = 100L;
            StoreImageJpaEntity mainImageEntity = createImageEntity(
                    1L, storeId, "https://example.com/main.jpg", true, 1
            );

            when(storeImageJpaRepository.findByStoreIdAndIsMainTrue(storeId))
                    .thenReturn(Optional.of(mainImageEntity));

            // When
            Optional<StoreImage> result = storeImageRepository.findByStoreIdAndIsMainTrue(storeId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().isMainImage()).isTrue();
            assertThat(result.get().getImageUrl()).isEqualTo("https://example.com/main.jpg");
            verify(storeImageJpaRepository, times(1)).findByStoreIdAndIsMainTrue(storeId);
        }

        @Test
        @DisplayName("대표 이미지가 없으면 Optional.empty()를 반환한다")
        void it_returns_empty_when_no_main_image() {
            // Given
            Long storeId = 100L;
            when(storeImageJpaRepository.findByStoreIdAndIsMainTrue(storeId))
                    .thenReturn(Optional.empty());

            // When
            Optional<StoreImage> result = storeImageRepository.findByStoreIdAndIsMainTrue(storeId);

            // Then
            assertThat(result).isEmpty();
            verify(storeImageJpaRepository, times(1)).findByStoreIdAndIsMainTrue(storeId);
        }
    }

    @Nested
    @DisplayName("findFirstByStoreIdOrderByDisplayOrderAsc 메서드는")
    class Describe_findFirstByStoreIdOrderByDisplayOrderAsc {

        @Test
        @DisplayName("displayOrder가 가장 낮은 이미지를 반환한다")
        void it_returns_first_image_by_display_order() {
            // Given
            Long storeId = 100L;
            StoreImageJpaEntity firstImageEntity = createImageEntity(
                    5L, storeId, "https://example.com/first.jpg", false, 1
            );

            when(storeImageJpaRepository.findFirstByStoreIdOrderByDisplayOrderAsc(storeId))
                    .thenReturn(Optional.of(firstImageEntity));

            // When
            Optional<StoreImage> result = storeImageRepository.findFirstByStoreIdOrderByDisplayOrderAsc(storeId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getDisplayOrder()).isEqualTo(1);
            assertThat(result.get().getImageUrl()).isEqualTo("https://example.com/first.jpg");
            verify(storeImageJpaRepository, times(1))
                    .findFirstByStoreIdOrderByDisplayOrderAsc(storeId);
        }

        @Test
        @DisplayName("이미지가 없으면 Optional.empty()를 반환한다")
        void it_returns_empty_when_no_images() {
            // Given
            Long storeId = 999L;
            when(storeImageJpaRepository.findFirstByStoreIdOrderByDisplayOrderAsc(storeId))
                    .thenReturn(Optional.empty());

            // When
            Optional<StoreImage> result = storeImageRepository.findFirstByStoreIdOrderByDisplayOrderAsc(storeId);

            // Then
            assertThat(result).isEmpty();
            verify(storeImageJpaRepository, times(1))
                    .findFirstByStoreIdOrderByDisplayOrderAsc(storeId);
        }
    }

    /**
     * 테스트용 StoreImageJpaEntity 생성 헬퍼 메서드
     */
    private StoreImageJpaEntity createImageEntity(Long id, Long storeId, String imageUrl, boolean isMain, Integer displayOrder) {
        StoreImage domain = StoreImage.builder()
                .storeImageId(id)
                .storeId(storeId)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .displayOrder(displayOrder)
                .build();
        return StoreImageEntityMapper.toJpaEntity(domain);
    }
}
