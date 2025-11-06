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
            StoreImage result = storeImageRepository.findById(storeImageId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStoreImageId()).isEqualTo(storeImageId);
            assertThat(result.getStoreId()).isEqualTo(100L);
            verify(storeImageJpaRepository, times(1)).findById(storeImageId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 null을 반환한다")
        void it_returns_null_when_not_exists() {
            // Given
            Long storeImageId = 999L;
            when(storeImageJpaRepository.findById(storeImageId)).thenReturn(Optional.empty());

            // When
            StoreImage result = storeImageRepository.findById(storeImageId);

            // Then
            assertThat(result).isNull();
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
            StoreImage found = storeImageRepository.findById(saved.getStoreImageId());

            // Then
            assertThat(found).isNotNull();
            assertThat(found.getStoreImageId()).isEqualTo(saved.getStoreImageId());
            assertThat(found.getImageUrl()).isEqualTo(saved.getImageUrl());
        }
    }
}
