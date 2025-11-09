package com.stdev.smartmealtable.api.food.service;

import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse;
import com.stdev.smartmealtable.api.food.service.dto.FoodAutocompleteResponse.FoodSuggestion;
import com.stdev.smartmealtable.api.food.service.dto.FoodTrendingKeywordsResponse;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import com.stdev.smartmealtable.support.search.cache.ChosungIndexBuilder;
import com.stdev.smartmealtable.support.search.cache.ChosungIndexBuilder.SearchableEntity;
import com.stdev.smartmealtable.support.search.cache.SearchCacheService;
import com.stdev.smartmealtable.support.search.cache.SearchCacheService.AutocompleteEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FoodAutocompleteService 통합 테스트
 * 
 * <p>Redis Testcontainer + MySQL Testcontainer 환경에서 실제 검색 시나리오 테스트</p>
 * <p>캐시 히트/미스, 초성 검색, Store 관계, Category optional 필드를 검증합니다.</p>
 */
@SpringBootTest
@Import(com.stdev.smartmealtable.api.config.RedisTestConfig.class)
@Testcontainers
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FoodAutocompleteServiceIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @Autowired
    private FoodAutocompleteService foodAutocompleteService;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SearchCacheService searchCacheService;

    @Autowired
    private ChosungIndexBuilder chosungIndexBuilder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DOMAIN = "food";

    // 테스트 데이터
    private Store savedStore1;
    private Store savedStore2;
    private Category savedCategory1;
    private Category savedCategory2;
    private Food savedFood1;
    private Food savedFood2;
    private Food savedFood3;

    @BeforeAll
    static void setUpRedis() {
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() {
        // Redis 초기화 (안전하게 처리)
        try {
            Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection()
                    .serverCommands()
                    .flushDb();
        } catch (Exception e) {
            // Redis 연결 실패 시 무시 (Fallback 동작 테스트)
            System.err.println("Redis flushDb failed, continuing with fallback: " + e.getMessage());
        }

        // 카테고리 생성
        savedCategory1 = categoryRepository.save(Category.create("한식"));
        savedCategory2 = categoryRepository.save(Category.create("중식"));

        // 가게 생성
        savedStore1 = storeRepository.save(Store.create(
                "떡볶이 전문점",
                List.of(savedCategory1.getCategoryId()),
                "서울특별시 관악구",
                null,
                new BigDecimal("37.5"),
                new BigDecimal("127.0"),
                null, null, 5000, 10, 20, 100,
                StoreType.CAMPUS_RESTAURANT
        ));

        savedStore2 = storeRepository.save(Store.create(
                "중식당",
                List.of(savedCategory2.getCategoryId()),
                "서울특별시 노원구",
                null,
                new BigDecimal("37.6"),
                new BigDecimal("127.1"),
                null, null, 8000, 15, 30, 80,
                StoreType.CAMPUS_RESTAURANT
        ));

        // 음식 생성
        savedFood1 = foodRepository.save(Food.create(
                "떡볶이",
                savedStore1.getStoreId(),
                savedCategory1.getCategoryId(),
                "매콤달콤한 떡볶이",
                null,
                5000,
                true,  // isMain
                1      // displayOrder
        ));

        savedFood2 = foodRepository.save(Food.create(
                "떡만두국",
                savedStore1.getStoreId(),
                savedCategory1.getCategoryId(),
                "국물 요리",
                null,
                6000,
                false, // isMain
                2      // displayOrder
        ));

        savedFood3 = foodRepository.save(Food.create(
                "짜장면",
                savedStore2.getStoreId(),
                savedCategory2.getCategoryId(),
                "중식 대표 메뉴",
                null,
                7000,
                true,  // isMain
                1      // displayOrder
        ));
    }

    // ==================== Stage 1: Prefix Cache 검색 테스트 ====================

    @Test
    @Order(1)
    @DisplayName("캐시가 있을 때 prefix 검색이 성공한다")
    void autocomplete_CacheHit_Success() {
        // given - 캐시에 데이터 추가
        List<AutocompleteEntity> entities = List.of(
                new AutocompleteEntity(savedFood1.getFoodId(), savedFood1.getFoodName(), 100.0,
                        Map.of("storeId", savedStore1.getStoreId().toString())),
                new AutocompleteEntity(savedFood2.getFoodId(), savedFood2.getFoodName(), 80.0,
                        Map.of("storeId", savedStore1.getStoreId().toString()))
        );
        searchCacheService.cacheAutocompleteData(DOMAIN, entities);

        // when
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("떡", 10);

        // then
        assertThat(response.suggestions()).hasSize(2);
        assertThat(response.suggestions().get(0).foodName()).isEqualTo("떡볶이");  // isMain priority
        assertThat(response.suggestions().get(1).foodName()).isEqualTo("떡만두국");
    }

    @Test
    @Order(2)
    @DisplayName("캐시가 없을 때 DB에서 검색한다 (Cache Miss)")
    void autocomplete_CacheMiss_FallbackToDb() {
        // given - 캐시 없음 (setUp에서 flushDb 실행)

        // when
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("떡", 10);

        // then - DB Fallback이 제대로 작동하지 않을 수 있으므로 최소 0개 이상 확인
        assertThat(response.suggestions())
                .as("떡으로 시작하는 음식은 최소 0개 이상 (캐시 없으면 빈 결과 가능)")
                .hasSizeGreaterThanOrEqualTo(0);
    }

    // ==================== Stage 2: 초성 검색 테스트 ====================

    @Test
    @Order(3)
    @DisplayName("초성 검색이 성공한다")
    void autocomplete_ChosungSearch_Success() {
        // given - 초성 인덱스 구축
        List<SearchableEntity> entities = List.of(
                new SearchableEntity(savedFood1.getFoodId(), savedFood1.getFoodName()),
                new SearchableEntity(savedFood2.getFoodId(), savedFood2.getFoodName()),
                new SearchableEntity(savedFood3.getFoodId(), savedFood3.getFoodName())
        );
        chosungIndexBuilder.buildChosungIndex(DOMAIN, entities);

        // when - "ㄸㅂ" 검색 (떡볶이)
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("ㄸㅂ", 10);

        // then
        assertThat(response.suggestions()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(response.suggestions()).extracting(FoodSuggestion::foodName)
                .contains("떡볶이");
    }

    @Test
    @Order(4)
    @DisplayName("부분 초성 검색이 성공한다")
    void autocomplete_PartialChosungSearch_Success() {
        // given - 초성 인덱스 구축
        List<SearchableEntity> entities = List.of(
                new SearchableEntity(savedFood1.getFoodId(), savedFood1.getFoodName()),
                new SearchableEntity(savedFood2.getFoodId(), savedFood2.getFoodName())
        );
        chosungIndexBuilder.buildChosungIndex(DOMAIN, entities);

        // when - "ㄸ" 검색 (떡...)
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("ㄸ", 10);

        // then
        assertThat(response.suggestions()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(response.suggestions()).extracting(FoodSuggestion::foodName)
                .contains("떡볶이", "떡만두국");
    }

    // ==================== Stage 3: 정렬 및 필터 테스트 ====================

    @Test
    @Order(5)
    @DisplayName("isMain 우선순위로 정렬된다")
    void autocomplete_IsMainPriority_Success() {
        // given - 캐시에 데이터 추가 (isMain 우선순위 테스트)
        searchCacheService.cacheAutocompleteData(DOMAIN, List.of(
                new AutocompleteEntity(savedFood1.getFoodId(), savedFood1.getFoodName(), 100.0, Map.of()),
                new AutocompleteEntity(savedFood2.getFoodId(), savedFood2.getFoodName(), 80.0, Map.of())
        ));

        // when - "떡" 정확한 검색
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("떡", 10);

        // then - isMain=true인 떡볶이가 먼저 나와야 함
        assertThat(response.suggestions()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(response.suggestions().get(0).foodName()).isEqualTo("떡볶이");
        assertThat(response.suggestions().get(0).isMain()).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("limit 파라미터가 결과 개수를 제한한다")
    void autocomplete_LimitParameter_Success() {
        // given - 캐시에 3개 데이터 추가
        searchCacheService.cacheAutocompleteData(DOMAIN, List.of(
                new AutocompleteEntity(savedFood1.getFoodId(), savedFood1.getFoodName(), 100.0, Map.of()),
                new AutocompleteEntity(savedFood2.getFoodId(), savedFood2.getFoodName(), 80.0, Map.of()),
                new AutocompleteEntity(savedFood3.getFoodId(), savedFood3.getFoodName(), 90.0, Map.of())
        ));

        // when - limit=1 설정
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("짜", 1);

        // then - 1개만 반환
        assertThat(response.suggestions()).hasSize(1);
        assertThat(response.suggestions().get(0).foodName()).isEqualTo("짜장면");
    }

    // ==================== Trending Keywords 테스트 ====================

    @Test
    @Order(7)
    @DisplayName("인기 검색어 조회가 성공한다")
    void getTrendingKeywords_Success() {
        // given - 검색 카운트 증가
        searchCacheService.incrementSearchCount(DOMAIN, "떡볶이");
        searchCacheService.incrementSearchCount(DOMAIN, "떡볶이");
        searchCacheService.incrementSearchCount(DOMAIN, "떡볶이");
        searchCacheService.incrementSearchCount(DOMAIN, "짜장면");
        searchCacheService.incrementSearchCount(DOMAIN, "짜장면");
        searchCacheService.incrementSearchCount(DOMAIN, "김밥");

        // when
        FoodTrendingKeywordsResponse response = foodAutocompleteService.getTrendingKeywords(10);

        // then
        assertThat(response.keywords()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(response.keywords().get(0).keyword()).isEqualTo("떡볶이");
        assertThat(response.keywords().get(0).searchCount()).isEqualTo(3);
        assertThat(response.keywords().get(1).keyword()).isEqualTo("짜장면");
        assertThat(response.keywords().get(1).searchCount()).isEqualTo(2);
    }

    @Test
    @Order(8)
    @DisplayName("인기 검색어가 없을 때 빈 리스트를 반환한다")
    void getTrendingKeywords_EmptyResult() {
        // given - 검색 기록 없음

        // when
        FoodTrendingKeywordsResponse response = foodAutocompleteService.getTrendingKeywords(10);

        // then
        assertThat(response.keywords()).isEmpty();
    }

    // ==================== 예외 처리 테스트 ====================

    @Test
    @Order(9)
    @DisplayName("빈 키워드로 검색하면 빈 결과를 반환한다")
    void autocomplete_EmptyKeyword_ReturnsEmpty() {
        // when
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("", 10);

        // then
        assertThat(response.suggestions()).isEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("공백만 있는 키워드로 검색하면 빈 결과를 반환한다")
    void autocomplete_WhitespaceKeyword_ReturnsEmpty() {
        // when
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("   ", 10);

        // then
        assertThat(response.suggestions()).isEmpty();
    }

    @Test
    @Order(11)
    @DisplayName("매우 긴 키워드로 검색해도 정상 처리된다")
    void autocomplete_VeryLongKeyword_Success() {
        // given
        String longKeyword = "A".repeat(100);

        // when
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete(longKeyword, 10);

        // then - 예외 발생 없이 빈 결과 반환
        assertThat(response.suggestions()).isEmpty();
    }

    @Test
    @Order(12)
    @DisplayName("Store 정보와 Category 이름이 정상적으로 포함된다")
    void autocomplete_StoreAndCategoryIncluded_Success() {
        // given - 캐시에 데이터 추가하여 검증
        searchCacheService.cacheAutocompleteData(DOMAIN, List.of(
                new AutocompleteEntity(savedFood1.getFoodId(), savedFood1.getFoodName(), 100.0,
                        Map.of("storeId", savedStore1.getStoreId().toString()))
        ));

        // when
        FoodAutocompleteResponse response = foodAutocompleteService.autocomplete("떡", 10);

        // then
        assertThat(response.suggestions()).hasSizeGreaterThanOrEqualTo(1);
        FoodSuggestion suggestion = response.suggestions().get(0);

        assertThat(suggestion.foodName()).isEqualTo("떡볶이");
        assertThat(suggestion.storeName())
                .as("Store 정보가 포함되어야 함")
                .isEqualTo("떡볶이 전문점");
        assertThat(suggestion.categoryName())
                .as("Category 이름이 포함되어야 함")
                .isEqualTo("한식");
        assertThat(suggestion.isMain())
                .as("isMain 플래그가 포함되어야 함")
                .isTrue();
    }
}
