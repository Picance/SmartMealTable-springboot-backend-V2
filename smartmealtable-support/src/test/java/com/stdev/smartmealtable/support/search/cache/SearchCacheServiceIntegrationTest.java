package com.stdev.smartmealtable.support.search.cache;

import com.stdev.smartmealtable.support.search.cache.SearchCacheService.AutocompleteEntity;
import com.stdev.smartmealtable.support.search.cache.SearchCacheService.TrendingKeyword;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SearchCacheService 통합 테스트
 * 
 * <p>Redis Testcontainer를 사용한 실제 Redis 환경 테스트</p>
 * <p>캐시 CRUD 작업의 정상 동작을 검증합니다.</p>
 */
@SpringBootTest
@Import(RedisTestContainerConfig.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchCacheServiceIntegrationTest {

    @Autowired
    private SearchCacheService searchCacheService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TEST_DOMAIN = "test_group";

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 Redis 초기화
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    @Test
    @DisplayName("캐시에 autocomplete 데이터를 추가하고 조회할 수 있다")
    void cacheAutocompleteData_Success() {
        // given
        Map<String, String> additionalData = Map.of(
                "type", "UNIVERSITY",
                "address", "서울특별시 관악구"
        );
        AutocompleteEntity entity = new AutocompleteEntity(
                1L,
                "서울대학교",
                100.0,
                additionalData
        );

        // when
        searchCacheService.cacheAutocompleteData(TEST_DOMAIN, List.of(entity));

        // then
        List<Long> results = searchCacheService.getAutocompleteResults(
                TEST_DOMAIN, "서", 10
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(1L);
    }

    @Test
    @DisplayName("prefix가 여러 개일 때 모든 prefix에서 조회 가능하다")
    void cacheAutocompleteData_MultiplePrefix() {
        // given
        AutocompleteEntity entity = new AutocompleteEntity(
                1L,
                "서울대학교",
                100.0,
                Map.of()
        );
        searchCacheService.cacheAutocompleteData(TEST_DOMAIN, List.of(entity));

        // when & then
        assertThat(searchCacheService.getAutocompleteResults(TEST_DOMAIN, "서", 10))
                .hasSize(1);
        assertThat(searchCacheService.getAutocompleteResults(TEST_DOMAIN, "서울", 10))
                .hasSize(1);
    }

    @Test
    @DisplayName("popularity가 높은 순으로 정렬되어 조회된다")
    void getAutocompleteResults_SortedByPopularity() {
        // given
        List<AutocompleteEntity> entities = List.of(
                new AutocompleteEntity(1L, "서울대학교", 100.0, Map.of()),
                new AutocompleteEntity(2L, "서울과학기술대학교", 150.0, Map.of()),
                new AutocompleteEntity(3L, "서울시립대학교", 200.0, Map.of())
        );
        searchCacheService.cacheAutocompleteData(TEST_DOMAIN, entities);

        // when
        List<Long> results = searchCacheService.getAutocompleteResults(
                TEST_DOMAIN, "서", 10
        );

        // then
        assertThat(results).hasSize(3);
        assertThat(results.get(0)).isEqualTo(3L);  // 서울시립대학교 (200.0)
        assertThat(results.get(1)).isEqualTo(2L);  // 서울과학기술대학교 (150.0)
        assertThat(results.get(2)).isEqualTo(1L);  // 서울대학교 (100.0)
    }

    @Test
    @DisplayName("limit을 지정하면 해당 개수만큼만 조회된다")
    void getAutocompleteResults_WithLimit() {
        // given
        List<AutocompleteEntity> entities = List.of(
                new AutocompleteEntity(1L, "서울대학교", 100.0, Map.of()),
                new AutocompleteEntity(2L, "서울과학기술대학교", 150.0, Map.of()),
                new AutocompleteEntity(3L, "서울시립대학교", 200.0, Map.of())
        );
        searchCacheService.cacheAutocompleteData(TEST_DOMAIN, entities);

        // when
        List<Long> results = searchCacheService.getAutocompleteResults(
                TEST_DOMAIN, "서", 2
        );

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("캐시에서 특정 항목을 제거할 수 있다")
    void removeFromAutocompleteCache_Success() {
        // given
        AutocompleteEntity entity = new AutocompleteEntity(
                1L,
                "서울대학교",
                100.0,
                Map.of()
        );
        searchCacheService.cacheAutocompleteData(TEST_DOMAIN, List.of(entity));

        // when
        searchCacheService.removeFromAutocompleteCache(TEST_DOMAIN, 1L, "서울대학교");

        // then
        List<Long> results = searchCacheService.getAutocompleteResults(
                TEST_DOMAIN, "서", 10
        );
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("검색 횟수를 증가시키고 trending keywords를 조회할 수 있다")
    void incrementSearchCount_AndGetTrendingKeywords() {
        // given
        String keyword1 = "서울대학교";
        String keyword2 = "연세대학교";
        String keyword3 = "고려대학교";

        // when
        searchCacheService.incrementSearchCount(TEST_DOMAIN, keyword1);
        searchCacheService.incrementSearchCount(TEST_DOMAIN, keyword1);
        searchCacheService.incrementSearchCount(TEST_DOMAIN, keyword1);
        searchCacheService.incrementSearchCount(TEST_DOMAIN, keyword2);
        searchCacheService.incrementSearchCount(TEST_DOMAIN, keyword2);
        searchCacheService.incrementSearchCount(TEST_DOMAIN, keyword3);

        // then
        List<TrendingKeyword> trending = searchCacheService.getTrendingKeywords(TEST_DOMAIN, 3);
        assertThat(trending).hasSize(3);
        assertThat(trending.get(0).keyword()).isEqualTo("서울대학교");  // 3회
        assertThat(trending.get(0).searchCount()).isEqualTo(3L);
        assertThat(trending.get(1).keyword()).isEqualTo("연세대학교");  // 2회
        assertThat(trending.get(1).searchCount()).isEqualTo(2L);
        assertThat(trending.get(2).keyword()).isEqualTo("고려대학교");  // 1회
        assertThat(trending.get(2).searchCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("trending keywords limit이 실제 개수보다 많으면 전체가 조회된다")
    void getTrendingKeywords_LimitExceedsActualCount() {
        // given
        searchCacheService.incrementSearchCount(TEST_DOMAIN, "서울대학교");
        searchCacheService.incrementSearchCount(TEST_DOMAIN, "연세대학교");

        // when
        List<TrendingKeyword> trending = searchCacheService.getTrendingKeywords(TEST_DOMAIN, 10);

        // then
        assertThat(trending).hasSize(2);
    }

    @Test
    @DisplayName("캐시에 추가 항목을 개별적으로 추가할 수 있다")
    void addToAutocompleteCache_Single() {
        // given
        AutocompleteEntity entity = new AutocompleteEntity(
                1L,
                "서울대학교",
                100.0,
                Map.of("type", "UNIVERSITY")
        );

        // when
        searchCacheService.addToAutocompleteCache(TEST_DOMAIN, entity);

        // then
        List<Long> results = searchCacheService.getAutocompleteResults(
                TEST_DOMAIN, "서", 10
        );
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 prefix로 조회하면 빈 리스트를 반환한다")
    void getAutocompleteResults_NonExistentPrefix() {
        // given
        AutocompleteEntity entity = new AutocompleteEntity(
                1L,
                "서울대학교",
                100.0,
                Map.of()
        );
        searchCacheService.cacheAutocompleteData(TEST_DOMAIN, List.of(entity));

        // when
        List<Long> results = searchCacheService.getAutocompleteResults(
                TEST_DOMAIN, "부", 10
        );

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("캐시 데이터는 24시간 TTL이 설정된다")
    void cacheData_HasTTL() {
        // given
        AutocompleteEntity entity = new AutocompleteEntity(
                1L,
                "서울대학교",
                100.0,
                Map.of()
        );
        searchCacheService.cacheAutocompleteData(TEST_DOMAIN, List.of(entity));

        // when
        Long ttl = redisTemplate.getExpire("autocomplete:" + TEST_DOMAIN + ":서");

        // then
        assertThat(ttl).isNotNull();
        assertThat(ttl).isGreaterThan(0);  // TTL이 설정되어 있음
        assertThat(ttl).isLessThanOrEqualTo(86400L);  // 24시간 이내
    }
}
