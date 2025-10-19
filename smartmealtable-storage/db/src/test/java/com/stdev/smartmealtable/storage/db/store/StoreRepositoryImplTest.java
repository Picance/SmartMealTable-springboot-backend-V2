package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreRepositoryImplTest {

    @Mock
    private StoreJpaRepository jpaRepository;

    @Mock
    private StoreQueryDslRepository queryDslRepository;

    private StoreRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new StoreRepositoryImpl(jpaRepository, queryDslRepository);
    }

    @Test
    void findById_maps_optional() {
    // Create a simple Store via builder
    Store store = Store.builder().storeId(1L).name("S").build();
    when(jpaRepository.findById(1L)).thenReturn(Optional.of(StoreEntityMapper.toJpaEntity(store)));

        Optional<Store> found = repository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getStoreId()).isEqualTo(1L);
    }

    @Test
    void findByIdAndDeletedAtIsNull_maps_optional() {
    Store store = Store.builder().storeId(2L).name("S2").build();
    when(jpaRepository.findByStoreIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(StoreEntityMapper.toJpaEntity(store)));

        Optional<Store> found = repository.findByIdAndDeletedAtIsNull(2L);
        assertThat(found).isPresent();
        assertThat(found.get().getStoreId()).isEqualTo(2L);
    }

    @Test
    void findByIdIn_maps_list() {
    Store s1 = Store.builder().storeId(3L).name("A").build();
    Store s2 = Store.builder().storeId(4L).name("B").build();
        when(jpaRepository.findByStoreIdInAndDeletedAtIsNull(List.of(3L,4L))).thenReturn(List.of(StoreEntityMapper.toJpaEntity(s1), StoreEntityMapper.toJpaEntity(s2)));

        List<Store> result = repository.findByIdIn(List.of(3L,4L));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Store::getStoreId).containsExactly(3L,4L);
    }

    @Test
    void save_converts_and_returns_domain() {
    Store store = Store.builder().name("New").build();
        when(jpaRepository.save(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            return StoreEntityMapper.toJpaEntity(Store.builder().storeId(10L).name("New").build());
        });

        Store saved = repository.save(store);
        assertThat(saved.getStoreId()).isEqualTo(10L);
    }

    @Test
    void searchByKeywordForAutocomplete_maps_list() {
    Store s = Store.builder().storeId(5L).name("X").build();
    when(jpaRepository.searchByKeywordForAutocomplete("k", 5)).thenReturn(List.of(StoreEntityMapper.toJpaEntity(s)));

        List<Store> result = repository.searchByKeywordForAutocomplete("k",5);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreId()).isEqualTo(5L);
    }

    @Test
    void searchStores_delegates_to_querydsl() {
    StoreRepository.StoreSearchResult empty = new StoreRepository.StoreSearchResult(List.of(), 0L);
    when(queryDslRepository.searchStores(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(empty);

    StoreRepository.StoreSearchResult res = repository.searchStores("k", BigDecimal.ONE, BigDecimal.ONE, 1.0, null, null, StoreType.RESTAURANT, "", 0, 10);
    assertThat(res.totalCount()).isZero();
    }
}
