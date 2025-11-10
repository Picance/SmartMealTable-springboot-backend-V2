package com.stdev.smartmealtable.api.search.service;

import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.storage.cache.ChosungIndexBuilder;
import com.stdev.smartmealtable.storage.cache.SearchCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * SearchCacheWarmingService 단위 테스트
 * <p>
 * Mockist 스타일로 작성되어 외부 의존성(Repository, Cache Service)을 모두 Mocking합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCacheWarmingService 단위 테스트")
class SearchCacheWarmingServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SearchCacheService searchCacheService;

    @Mock
    private ChosungIndexBuilder chosungIndexBuilder;

    @InjectMocks
    private SearchCacheWarmingService searchCacheWarmingService;

    @Test
    @DisplayName("전체 캐시 워밍 - 성공")
    void warmAllCaches_Success() {
        // given
        Store store = createMockStore(1L, "교촌치킨 강남점");
        Food food = createMockFood(1L, "후라이드치킨");
        Group group = createMockGroup(1L, "서울대학교 컴퓨터공학부");

        // Store
        given(storeRepository.count()).willReturn(1L);
        given(storeRepository.findAll(0, 100)).willReturn(List.of(store));

        // Food
        given(foodRepository.count()).willReturn(1L);
        given(foodRepository.findAll(0, 500)).willReturn(List.of(food));

        // Group
        given(groupRepository.count()).willReturn(1L);
        given(groupRepository.findAll(0, 50)).willReturn(List.of(group));

        // when
        searchCacheWarmingService.warmAllCaches();

        // then
        then(storeRepository).should(times(1)).count();
        then(storeRepository).should(times(1)).findAll(anyInt(), anyInt());
        then(foodRepository).should(times(1)).count();
        then(foodRepository).should(times(1)).findAll(anyInt(), anyInt());
        then(groupRepository).should(times(1)).count();
        then(groupRepository).should(times(1)).findAll(anyInt(), anyInt());

        then(searchCacheService).should(times(3)).cacheAutocompleteData(anyString(), anyList());
        then(chosungIndexBuilder).should(times(3)).buildChosungIndex(anyString(), anyList());
    }

    @Test
    @DisplayName("Store 캐시 워밍 - 성공")
    void warmStoreCache_Success() {
        // given
        Store store = createMockStore(1L, "교촌치킨 강남점");
        given(storeRepository.count()).willReturn(1L);
        given(storeRepository.findAll(0, 100)).willReturn(List.of(store));

        // when
        searchCacheWarmingService.warmStoreCache(100);

        // then
        then(storeRepository).should(times(1)).count();
        then(storeRepository).should(times(1)).findAll(0, 100);
        then(searchCacheService).should(times(1)).cacheAutocompleteData(eq("store"), anyList());
        then(chosungIndexBuilder).should(times(1)).buildChosungIndex(eq("store"), anyList());
    }

    @Test
    @DisplayName("Food 캐시 워밍 - 성공")
    void warmFoodCache_Success() {
        // given
        Food food = createMockFood(1L, "후라이드치킨");
        given(foodRepository.count()).willReturn(1L);
        given(foodRepository.findAll(0, 500)).willReturn(List.of(food));

        // when
        searchCacheWarmingService.warmFoodCache(500);

        // then
        then(foodRepository).should(times(1)).count();
        then(foodRepository).should(times(1)).findAll(0, 500);
        then(searchCacheService).should(times(1)).cacheAutocompleteData(eq("food"), anyList());
        then(chosungIndexBuilder).should(times(1)).buildChosungIndex(eq("food"), anyList());
    }

    @Test
    @DisplayName("Group 캐시 워밍 - 성공")
    void warmGroupCache_Success() {
        // given
        Group group = createMockGroup(1L, "서울대학교 컴퓨터공학부");
        given(groupRepository.count()).willReturn(1L);
        given(groupRepository.findAll(0, 50)).willReturn(List.of(group));

        // when
        searchCacheWarmingService.warmGroupCache(50);

        // then
        then(groupRepository).should(times(1)).count();
        then(groupRepository).should(times(1)).findAll(anyInt(), anyInt());
        then(searchCacheService).should(times(1)).cacheAutocompleteData(eq("group"), anyList());
        then(chosungIndexBuilder).should(times(1)).buildChosungIndex(eq("group"), anyList());
    }

    @Test
    @DisplayName("Store 캐시 워밍 - 데이터 없음 (스킵)")
    void warmStoreCache_NoData_Skip() {
        // given
        given(storeRepository.count()).willReturn(0L);

        // when
        searchCacheWarmingService.warmStoreCache(100);

        // then
        then(storeRepository).should(times(1)).count();
        then(storeRepository).should(times(0)).findAll(anyInt(), anyInt());
        then(searchCacheService).should(times(0)).cacheAutocompleteData(anyString(), anyList());
        then(chosungIndexBuilder).should(times(0)).buildChosungIndex(anyString(), anyList());
    }

    @Test
    @DisplayName("Food 캐시 워밍 - 데이터 없음 (스킵)")
    void warmFoodCache_NoData_Skip() {
        // given
        given(foodRepository.count()).willReturn(0L);

        // when
        searchCacheWarmingService.warmFoodCache(500);

        // then
        then(foodRepository).should(times(1)).count();
        then(foodRepository).should(times(0)).findAll(anyInt(), anyInt());
        then(searchCacheService).should(times(0)).cacheAutocompleteData(anyString(), anyList());
        then(chosungIndexBuilder).should(times(0)).buildChosungIndex(anyString(), anyList());
    }

    @Test
    @DisplayName("Group 캐시 워밍 - 데이터 없음 (스킵)")
    void warmGroupCache_NoData_Skip() {
        // given
        given(groupRepository.count()).willReturn(0L);

        // when
        searchCacheWarmingService.warmGroupCache(50);

        // then
        then(groupRepository).should(times(1)).count();
        then(groupRepository).should(times(0)).findAll(anyInt(), anyInt());
        then(searchCacheService).should(times(0)).cacheAutocompleteData(anyString(), anyList());
        then(chosungIndexBuilder).should(times(0)).buildChosungIndex(anyString(), anyList());
    }

    // ===== Helper Methods =====

    private Store createMockStore(Long id, String name) {
        return Store.builder()
                .storeId(id)
                .name(name)
                .phoneNumber("02-1234-5678")
                .address("서울특별시 강남구 테헤란로 123")
                .latitude(java.math.BigDecimal.valueOf(37.5665))
                .longitude(java.math.BigDecimal.valueOf(126.9780))
                .categoryIds(List.of(1L))  // 카테고리 ID 추가
                .build();
    }

    private Food createMockFood(Long id, String name) {
        return Food.builder()
                .foodId(id)
                .foodName(name)
                .description("맛있는 후라이드치킨")
                .price(18000)
                .categoryId(1L)  // 카테고리 ID 추가
                .storeId(1L)
                .build();
    }

    private Group createMockGroup(Long id, String name) {
        return Group.create(name, com.stdev.smartmealtable.domain.member.entity.GroupType.UNIVERSITY, "서울특별시 관악구");
    }
}
