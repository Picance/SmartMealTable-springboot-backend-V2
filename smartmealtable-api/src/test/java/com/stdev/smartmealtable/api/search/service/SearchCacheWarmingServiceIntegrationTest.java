package com.stdev.smartmealtable.api.search.service;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.storage.cache.ChosungIndexBuilder;
import com.stdev.smartmealtable.storage.cache.SearchCacheService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

/**
 * SearchCacheWarmingService 통합 테스트
 *
 * <p>실제 DB 환경과 Mock Redis를 사용하여 캐시 워밍 동작을 검증합니다.</p>
 *
 * <h3>테스트 시나리오</h3>
 * <ul>
 *   <li>전체 캐시 워밍 성공 (warmAllCaches)</li>
 *   <li>Store 캐시 워밍 성공</li>
 *   <li>Food 캐시 워밍 성공</li>
 *   <li>Group 캐시 워밍 성공</li>
 *   <li>빈 데이터 상황에서 캐시 워밍</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchCacheWarmingServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private SearchCacheWarmingService searchCacheWarmingService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Mock Redis 서비스
    @MockBean
    private SearchCacheService searchCacheService;

    @MockBean
    private ChosungIndexBuilder chosungIndexBuilder;

    // 테스트 데이터
    private Category savedCategory;

    @BeforeEach
    void setUp() {
        // Redis Mock 설정
        doNothing().when(searchCacheService).cacheAutocompleteData(anyString(), anyList());
        doNothing().when(chosungIndexBuilder).buildChosungIndex(anyString(), anyList());

        // 테스트 카테고리 생성
        savedCategory = categoryRepository.save(Category.create("한식"));
    }

    /**
     * 테스트 1: 전체 캐시 워밍 성공
     *
     * <p>Store, Food, Group 데이터를 로드하고 정상 동작하는지 검증합니다.</p>
     */
    @Test
    @Order(1)
    @DisplayName("전체 캐시 워밍 성공 - Store/Food/Group 모두 로드")
    void warmAllCaches_Success() {
        // Given: 테스트 데이터 생성
        storeRepository.save(createTestStore("떡볶이집", 1));
        storeRepository.save(createTestStore("김밥천국", 2));

        foodRepository.save(createTestFood("떡볶이", 1L));
        foodRepository.save(createTestFood("김밥", 2L));

        groupRepository.save(createTestGroup("맛집탐방단", 1L));
        groupRepository.save(createTestGroup("점심메뉴고민단", 2L));

        // When & Then: 전체 캐시 워밍 실행 - 예외 없이 정상 동작
        assertThatCode(() -> searchCacheWarmingService.warmAllCaches())
                .doesNotThrowAnyException();
    }

    /**
     * 테스트 2: Store 캐시 워밍 성공
     */
    @Test
    @Order(2)
    @DisplayName("Store 캐시 워밍 성공")
    void warmStoreCache_Success() {
        // Given: Store 데이터 생성
        for (int i = 1; i <= 5; i++) {
            storeRepository.save(createTestStore("가게" + i, i));
        }

        // When & Then: Store 캐시 워밍 실행 - 예외 없이 정상 동작
        assertThatCode(() -> searchCacheWarmingService.warmStoreCache(2))
                .doesNotThrowAnyException();
    }

    /**
     * 테스트 3: Food 캐시 워밍 성공
     */
    @Test
    @Order(3)
    @DisplayName("Food 캐시 워밍 성공")
    void warmFoodCache_Success() {
        // Given: Food 데이터 생성
        for (int i = 1; i <= 7; i++) {
            foodRepository.save(createTestFood("음식" + i, (long) i));
        }

        // When & Then: Food 캐시 워밍 실행 - 예외 없이 정상 동작
        assertThatCode(() -> searchCacheWarmingService.warmFoodCache(3))
                .doesNotThrowAnyException();
    }

    /**
     * 테스트 4: Group 캐시 워밍 성공
     */
    @Test
    @Order(4)
    @DisplayName("Group 캐시 워밍 성공")
    void warmGroupCache_Success() {
        // Given: Group 데이터 생성
        for (int i = 1; i <= 4; i++) {
            groupRepository.save(createTestGroup("그룹" + i, (long) i));
        }

        // When & Then: Group 캐시 워밍 실행 - 예외 없이 정상 동작
        assertThatCode(() -> searchCacheWarmingService.warmGroupCache(2))
                .doesNotThrowAnyException();
    }

    /**
     * 테스트 5: 빈 데이터 상황에서 캐시 워밍
     *
     * <p>DB에 데이터가 없을 때 예외 없이 정상 동작하는지 검증합니다.</p>
     */
    @Test
    @Order(5)
    @DisplayName("빈 데이터 상황에서 캐시 워밍 - 예외 없이 정상 동작")
    void warmAllCaches_EmptyData() {
        // Given: DB에 데이터 없음 (setUp에서 생성한 카테고리만 존재)

        // When & Then: 전체 캐시 워밍 실행 - 예외 없이 정상 동작
        assertThatCode(() -> searchCacheWarmingService.warmAllCaches())
                .doesNotThrowAnyException();
    }

    /**
     * 테스트 6: 대량 데이터 페이징 처리
     *
     * <p>대량의 데이터(50개 이상)를 페이징으로 나누어 처리하고, 메모리 오버플로우 없이 정상 동작하는지 검증합니다.</p>
     */
    @Test
    @Order(6)
    @DisplayName("대량 데이터 페이징 처리 - 50개 Store 데이터")
    void warmStoreCache_LargeDataset() {
        // Given: 50개의 Store 데이터 생성
        for (int i = 1; i <= 50; i++) {
            storeRepository.save(createTestStore("대형가게" + i, i));
        }

        // When & Then: Store 캐시 워밍 실행 - 예외 없이 정상 동작 (batchSize=10)
        assertThatCode(() -> searchCacheWarmingService.warmStoreCache(10))
                .doesNotThrowAnyException();
    }

    // ========== 테스트 헬퍼 메서드 ==========

    /**
     * 테스트용 Store 생성
     */
    private Store createTestStore(String name, int seed) {
        return Store.create(
                name,
                Collections.singletonList(savedCategory.getCategoryId()),
                "서울특별시 관악구",
                null,
                new BigDecimal("37.5"),
                new BigDecimal("127.0"),
                null,
                null,
                5000 + seed * 1000,
                seed * 10,
                seed * 20,
                seed * 100,
                StoreType.CAMPUS_RESTAURANT
        );
    }

    /**
     * 테스트용 Food 생성
     */
    private Food createTestFood(String foodName, Long storeId) {
        return Food.create(
                foodName,
                storeId,
                savedCategory.getCategoryId(),
                "맛있는 " + foodName,
                null,
                5000,
                false,
                1
        );
    }

    /**
     * 테스트용 Group 생성
     */
    private Group createTestGroup(String name, Long memberId) {
        return Group.create(name, GroupType.UNIVERSITY, Address.of(name, null, "서울특별시", null, null, null, null));
    }
}
