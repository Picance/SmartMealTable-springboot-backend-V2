package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.Food;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FoodRepositoryImplTest {

    @Mock
    private FoodJpaRepository foodJpaRepository;

    private FoodRepositoryImpl foodRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        foodRepository = new FoodRepositoryImpl(foodJpaRepository);
    }

    @Test
    void save_converts_domain_and_returns_saved_domain() {

        // input domain simulates a new entity (no id)
        Food domain = Food.reconstitute(null, "Pizza", 1L, 10L, "desc", "url", 12000);

        // saved entity returned from JPA would have an id assigned
        FoodJpaEntity savedEntity = FoodJpaEntity.fromDomain(Food.reconstitute(1L, "Pizza", 1L, 10L, "desc", "url", 12000));
        when(foodJpaRepository.save(any(FoodJpaEntity.class))).thenReturn(savedEntity);

        Food result = foodRepository.save(domain);

        ArgumentCaptor<FoodJpaEntity> captor = ArgumentCaptor.forClass(FoodJpaEntity.class);
        verify(foodJpaRepository, times(1)).save(captor.capture());

        FoodJpaEntity passed = captor.getValue();
        assertThat(passed.getFoodName()).isEqualTo("Pizza");

        assertThat(result).isNotNull();
        assertThat(result.getFoodId()).isEqualTo(1L);

    }

    @Test
    void findById_maps_entity_to_domain() {
        FoodJpaEntity entity = new FoodJpaEntity();
        entity = FoodJpaEntity.fromDomain(Food.reconstitute(2L, "Pasta", 11L, 1L, "d", "u", 8000));
        when(foodJpaRepository.findById(2L)).thenReturn(Optional.of(entity));

        Optional<Food> found = foodRepository.findById(2L);

        assertThat(found).isPresent();
        assertThat(found.get().getFoodId()).isEqualTo(2L);
        assertThat(found.get().getFoodName()).isEqualTo("Pasta");
    }

    @Test
    void findByIdIn_maps_list_to_domain_list() {
        FoodJpaEntity e1 = FoodJpaEntity.fromDomain(Food.reconstitute(1L, "A", 1L, 1L, null, null, 1000));
        FoodJpaEntity e2 = FoodJpaEntity.fromDomain(Food.reconstitute(2L, "B", 1L, 1L, null, null, 2000));
        when(foodJpaRepository.findByFoodIdIn(List.of(1L,2L))).thenReturn(List.of(e1, e2));

        List<Food> result = foodRepository.findByIdIn(List.of(1L,2L));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Food::getFoodId).containsExactly(1L,2L);
    }

    @Test
    void findAll_uses_paging_and_maps() {
        FoodJpaEntity e1 = FoodJpaEntity.fromDomain(Food.reconstitute(1L, "A", 1L, 1L, null, null, 1000));
        when(foodJpaRepository.findAll(PageRequest.of(0,2))).thenReturn(new PageImpl<>(List.of(e1)));

        List<Food> result = foodRepository.findAll(0,2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFoodId()).isEqualTo(1L);
    }

    @Test
    void findByCategoryId_uses_paging_and_maps() {
        FoodJpaEntity e1 = FoodJpaEntity.fromDomain(Food.reconstitute(5L, "X", 1L, 99L, null, null, 500));
        when(foodJpaRepository.findByCategoryId(99L, PageRequest.of(1,3))).thenReturn(new PageImpl<>(List.of(e1)));

        List<Food> result = foodRepository.findByCategoryId(99L,1,3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(99L);
    }

    @Test
    void count_and_countByCategory_delegate() {
        when(foodJpaRepository.count()).thenReturn(42L);
        when(foodJpaRepository.countByCategoryId(7L)).thenReturn(3L);

        assertThat(foodRepository.count()).isEqualTo(42L);
        assertThat(foodRepository.countByCategoryId(7L)).isEqualTo(3L);
    }

    @Test
    void findRandom_delegates_and_maps() {
        FoodJpaEntity e1 = FoodJpaEntity.fromDomain(Food.reconstitute(10L, "랜덤", 1L, 2L, null, "url", 5000));
        when(foodJpaRepository.findRandom(0, 5)).thenReturn(List.of(e1));

        List<Food> result = foodRepository.findRandom(0, 5);

        verify(foodJpaRepository).findRandom(0, 5);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFoodId()).isEqualTo(10L);
        assertThat(result.get(0).getImageUrl()).isEqualTo("url");
    }

    @Test
    void countOnboardingCandidates_delegates_to_jpa() {
        when(foodJpaRepository.countForOnboarding()).thenReturn(15L);

        long count = foodRepository.countOnboardingCandidates();

        verify(foodJpaRepository).countForOnboarding();
        assertThat(count).isEqualTo(15L);
    }

    @Test
    void deleteByStoreId_removes_all_foods_for_store() {
        // Given
        Long storeId = 100L;

        // When
        foodRepository.deleteByStoreId(storeId);

        // Then
        verify(foodJpaRepository, times(1)).deleteByStoreId(storeId);
    }

    @Test
    void softDelete_removes_search_keywords() {
        Long foodId = 55L;
        FoodJpaEntity entity = FoodJpaEntity.fromDomain(
                Food.reconstitute(foodId, "Taco", 10L, 3L, "desc", "url", 1000)
        );
        when(foodJpaRepository.findById(foodId)).thenReturn(Optional.of(entity));

        foodRepository.softDelete(foodId);
    }
}
