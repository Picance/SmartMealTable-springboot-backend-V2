package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.FoodPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FoodPreferenceRepositoryImplTest {

    @Mock
    private FoodPreferenceJpaRepository jpaRepository;

    private FoodPreferenceRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new FoodPreferenceRepositoryImpl(jpaRepository);
    }

    @Test
    void save_converts_and_returns_domain() {
        FoodPreference domain = FoodPreference.create(100L, 200L);
        FoodPreferenceJpaEntity saved = FoodPreferenceJpaEntity.fromDomain(
                FoodPreference.reconstitute(5L, 100L, 200L, true, LocalDateTime.now())
        );
        when(jpaRepository.save(any(FoodPreferenceJpaEntity.class))).thenReturn(saved);

        FoodPreference result = repository.save(domain);

        assertThat(result).isNotNull();
        assertThat(result.getFoodPreferenceId()).isEqualTo(5L);
    }

    @Test
    void findByMemberId_maps_list() {
        FoodPreferenceJpaEntity e1 = FoodPreferenceJpaEntity.fromDomain(FoodPreference.reconstitute(1L, 10L, 20L, true, LocalDateTime.now()));
        FoodPreferenceJpaEntity e2 = FoodPreferenceJpaEntity.fromDomain(FoodPreference.reconstitute(2L, 10L, 21L, false, LocalDateTime.now()));
        when(jpaRepository.findByMemberId(10L)).thenReturn(List.of(e1, e2));

        List<FoodPreference> result = repository.findByMemberId(10L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(FoodPreference::getMemberId).containsOnly(10L);
    }

    @Test
    void findByMemberIdAndFoodId_maps_optional() {
        FoodPreferenceJpaEntity e = FoodPreferenceJpaEntity.fromDomain(FoodPreference.reconstitute(3L, 11L, 22L, true, LocalDateTime.now()));
        when(jpaRepository.findByMemberIdAndFoodId(11L, 22L)).thenReturn(Optional.of(e));

        Optional<FoodPreference> result = repository.findByMemberIdAndFoodId(11L, 22L);

        assertThat(result).isPresent();
        assertThat(result.get().getFoodId()).isEqualTo(22L);
    }

    @Test
    void findById_maps_optional() {
        FoodPreferenceJpaEntity e = FoodPreferenceJpaEntity.fromDomain(FoodPreference.reconstitute(9L, 12L, 23L, false, LocalDateTime.now()));
        when(jpaRepository.findById(9L)).thenReturn(Optional.of(e));

        Optional<FoodPreference> result = repository.findById(9L);

        assertThat(result).isPresent();
        assertThat(result.get().getFoodPreferenceId()).isEqualTo(9L);
    }

    @Test
    void findByMemberIdAndIsPreferred_maps_list() {
        FoodPreferenceJpaEntity e = FoodPreferenceJpaEntity.fromDomain(FoodPreference.reconstitute(7L, 13L, 24L, true, LocalDateTime.now()));
        when(jpaRepository.findByMemberIdAndIsPreferred(13L, true)).thenReturn(List.of(e));

        List<FoodPreference> result = repository.findByMemberIdAndIsPreferred(13L, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsPreferred()).isTrue();
    }

    @Test
    void delete_variants_delegate_to_jpa() {
        doNothing().when(jpaRepository).deleteByMemberId(15L);
        doNothing().when(jpaRepository).deleteByMemberIdAndFoodId(15L, 25L);
        doNothing().when(jpaRepository).deleteById(100L);

        repository.deleteByMemberId(15L);
        repository.deleteByMemberIdAndFoodId(15L,25L);
        repository.deleteById(100L);

        verify(jpaRepository).deleteByMemberId(15L);
        verify(jpaRepository).deleteByMemberIdAndFoodId(15L,25L);
        verify(jpaRepository).deleteById(100L);
    }

    @Test
    void countByMemberIdAndIsPreferred_delegates() {
        when(jpaRepository.countByMemberIdAndIsPreferred(20L, true)).thenReturn(6L);

        long cnt = repository.countByMemberIdAndIsPreferred(20L, true);
        assertThat(cnt).isEqualTo(6L);
    }
}
