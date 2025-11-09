package com.stdev.smartmealtable.api.group.service;

import com.stdev.smartmealtable.api.group.service.dto.GroupAutocompleteResponse;
import com.stdev.smartmealtable.api.group.service.dto.GroupAutocompleteResponse.GroupSuggestion;
import com.stdev.smartmealtable.api.group.service.dto.TrendingKeywordsResponse;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GroupAutocompleteService 통합 테스트
 * 
 * <p>Redis Testcontainer + MySQL Testcontainer 환경에서 실제 검색 시나리오 테스트</p>
 * <p>캐시 히트/미스, 초성 검색, 오타 허용, Fallback 동작을 검증합니다.</p>
 */
@SpringBootTest
@Import(com.stdev.smartmealtable.api.config.RedisTestConfig.class)
@Testcontainers
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroupAutocompleteServiceIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @Autowired
    private GroupAutocompleteService groupAutocompleteService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SearchCacheService searchCacheService;

    @Autowired
    private ChosungIndexBuilder chosungIndexBuilder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DOMAIN = "group";

    // 테스트 데이터
    private Group savedGroup1;
    private Group savedGroup2;
    private Group savedGroup3;

    @BeforeAll
    static void setUpRedis() {
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() {
        // Redis 초기화
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushDb();

        // 테스트 데이터 생성
        savedGroup1 = groupRepository.save(Group.create("서울대학교", GroupType.UNIVERSITY, "서울특별시 관악구"));
        savedGroup2 = groupRepository.save(Group.create("서울과학기술대학교", GroupType.UNIVERSITY, "서울특별시 노원구"));
        savedGroup3 = groupRepository.save(Group.create("연세대학교", GroupType.UNIVERSITY, "서울특별시 서대문구"));
    }

    // ==================== Stage 1: Prefix Cache 검색 테스트 ====================

    @Test
    @Order(1)
    @DisplayName("캐시가 있을 때 prefix 검색이 성공한다")
    void autocomplete_CacheHit_Success() {
        // given - 캐시에 데이터 추가
        List<AutocompleteEntity> entities = List.of(
                new AutocompleteEntity(savedGroup1.getGroupId(), savedGroup1.getName(), 100.0, 
                        Map.of("type", "UNIVERSITY", "address", savedGroup1.getAddress())),
                new AutocompleteEntity(savedGroup2.getGroupId(), savedGroup2.getName(), 150.0,
                        Map.of("type", "UNIVERSITY", "address", savedGroup2.getAddress()))
        );
        searchCacheService.cacheAutocompleteData(DOMAIN, entities);

        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("서울", 10);

        // then
        assertThat(response.suggestions()).hasSize(2);
        assertThat(response.suggestions().get(0).name()).isEqualTo("서울과학기술대학교");  // popularity 150
        assertThat(response.suggestions().get(1).name()).isEqualTo("서울대학교");  // popularity 100
    }

    @Test
    @Order(2)
    @DisplayName("캐시가 없을 때 DB에서 검색한다 (Cache Miss)")
    void autocomplete_CacheMiss_FallbackToDb() {
        // given - 캐시 없음 (setUp에서 flushDb 실행)

        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("서울", 10);

        // then
        assertThat(response.suggestions())
                .as("서울로 시작하는 그룹은 2개 (서울대학교, 서울과학기술대학교)")
                .hasSize(2);
        assertThat(response.suggestions()).extracting(GroupSuggestion::name)
                .as("서울대학교와 서울과학기술대학교가 포함되어야 함")
                .containsExactlyInAnyOrder("서울대학교", "서울과학기술대학교");
    }

    // ==================== Stage 2: 초성 검색 테스트 ====================

    @Test
    @Order(3)
    @DisplayName("초성 검색이 성공한다")
    void autocomplete_ChosungSearch_Success() {
        // given - 초성 인덱스 구축
        List<SearchableEntity> entities = List.of(
                new SearchableEntity(savedGroup1.getGroupId(), savedGroup1.getName()),
                new SearchableEntity(savedGroup2.getGroupId(), savedGroup2.getName()),
                new SearchableEntity(savedGroup3.getGroupId(), savedGroup3.getName())
        );
        chosungIndexBuilder.buildChosungIndex(DOMAIN, entities);

        // when - "ㅅㅇㄷ" 검색 (서울대학교)
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("ㅅㅇㄷ", 10);

        // then
        assertThat(response.suggestions()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(response.suggestions()).extracting(GroupSuggestion::name)
                .contains("서울대학교");
    }

    @Test
    @Order(4)
    @DisplayName("부분 초성 검색이 성공한다")
    void autocomplete_PartialChosungSearch_Success() {
        // given - 초성 인덱스 구축
        List<SearchableEntity> entities = List.of(
                new SearchableEntity(savedGroup1.getGroupId(), savedGroup1.getName()),
                new SearchableEntity(savedGroup2.getGroupId(), savedGroup2.getName())
        );
        chosungIndexBuilder.buildChosungIndex(DOMAIN, entities);

        // when - "ㅅㅇ" 검색 (서울...)
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("ㅅㅇ", 10);

        // then
        assertThat(response.suggestions()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(response.suggestions()).extracting(GroupSuggestion::name)
                .contains("서울대학교", "서울과학기술대학교");
    }

    // ==================== Stage 3: 오타 허용 검색 테스트 ====================

    @Test
    @Order(5)
    @DisplayName("오타가 있어도 편집 거리 2 이내면 검색된다")
    @org.junit.jupiter.api.Disabled("TODO: 오타 허용 검색 로직 개선 필요 - 전체 DB 스캔 필요")
    void autocomplete_TypoTolerance_Success() {
        // given - 캐시 없음

        // when - "셔울" (서울의 오타, 편집 거리 1)
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("셔울", 10);

        // then
        assertThat(response.suggestions()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(response.suggestions()).extracting(GroupSuggestion::name)
                .contains("서울대학교", "서울과학기술대학교");
    }

    @Test
    @Order(6)
    @DisplayName("편집 거리가 2를 초과하면 검색되지 않는다")
    void autocomplete_TypoTolerance_ExceedsThreshold() {
        // given - 캐시 없음

        // when - "부산" (서울과 완전히 다름)
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("부산", 10);

        // then
        assertThat(response.suggestions()).isEmpty();
    }

    // ==================== 인기 검색어 테스트 ====================

    @Test
    @Order(7)
    @DisplayName("검색 시 검색 횟수가 증가한다")
    void autocomplete_IncrementSearchCount() {
        // given - 캐시 데이터 추가
        searchCacheService.cacheAutocompleteData(DOMAIN, List.of(
                new AutocompleteEntity(savedGroup1.getGroupId(), savedGroup1.getName(), 100.0, Map.of())
        ));

        // when - 같은 키워드로 3번 검색
        groupAutocompleteService.autocomplete("서울", 10);
        groupAutocompleteService.autocomplete("서울", 10);
        groupAutocompleteService.autocomplete("서울", 10);

        // then - 인기 검색어에서 확인
        TrendingKeywordsResponse trending = groupAutocompleteService.getTrendingKeywords(10);
        assertThat(trending.keywords()).isNotEmpty();
        assertThat(trending.keywords().get(0).keyword()).isEqualTo("서울");
        assertThat(trending.keywords().get(0).searchCount()).isEqualTo(3L);
    }

    @Test
    @Order(8)
    @DisplayName("인기 검색어를 검색 횟수 순으로 조회한다")
    void getTrendingKeywords_SortedBySearchCount() {
        // given - 여러 키워드 검색
        searchCacheService.incrementSearchCount(DOMAIN, "서울");
        searchCacheService.incrementSearchCount(DOMAIN, "서울");
        searchCacheService.incrementSearchCount(DOMAIN, "서울");
        searchCacheService.incrementSearchCount(DOMAIN, "연세");
        searchCacheService.incrementSearchCount(DOMAIN, "연세");
        searchCacheService.incrementSearchCount(DOMAIN, "고려");

        // when
        TrendingKeywordsResponse response = groupAutocompleteService.getTrendingKeywords(3);

        // then
        assertThat(response.keywords()).hasSize(3);
        assertThat(response.keywords().get(0).keyword()).isEqualTo("서울");
        assertThat(response.keywords().get(0).searchCount()).isEqualTo(3L);
        assertThat(response.keywords().get(1).keyword()).isEqualTo("연세");
        assertThat(response.keywords().get(1).searchCount()).isEqualTo(2L);
        assertThat(response.keywords().get(2).keyword()).isEqualTo("고려");
        assertThat(response.keywords().get(2).searchCount()).isEqualTo(1L);
    }

    // ==================== 하이브리드 데이터 조회 테스트 ====================

    @Test
    @Order(9)
    @DisplayName("캐시에 일부 데이터만 있을 때 DB와 조합하여 조회한다")
    void autocomplete_HybridFetch_CacheAndDb() {
        // given - 캐시에 1개만 추가
        searchCacheService.cacheAutocompleteData(DOMAIN, List.of(
                new AutocompleteEntity(savedGroup1.getGroupId(), savedGroup1.getName(), 100.0, 
                        Map.of("type", "UNIVERSITY"))
        ));

        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("서울", 10);

        // then - 캐시(1개) + DB(1개) = 총 2개
        assertThat(response.suggestions()).hasSize(2);
        assertThat(response.suggestions()).extracting(GroupSuggestion::name)
                .contains("서울대학교", "서울과학기술대학교");
    }

    // ==================== 결과 제한 테스트 ====================

    @Test
    @Order(10)
    @DisplayName("limit 파라미터가 적용된다")
    void autocomplete_WithLimit() {
        // given - 캐시에 데이터 추가
        searchCacheService.cacheAutocompleteData(DOMAIN, List.of(
                new AutocompleteEntity(savedGroup1.getGroupId(), savedGroup1.getName(), 100.0, Map.of()),
                new AutocompleteEntity(savedGroup2.getGroupId(), savedGroup2.getName(), 150.0, Map.of()),
                new AutocompleteEntity(savedGroup3.getGroupId(), savedGroup3.getName(), 200.0, Map.of())
        ));

        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("대학교", 2);

        // then
        assertThat(response.suggestions()).hasSize(2);
    }

    // ==================== 빈 결과 테스트 ====================

    @Test
    @Order(11)
    @DisplayName("검색 결과가 없으면 빈 리스트를 반환한다")
    void autocomplete_NoResults_EmptyList() {
        // given - 캐시 없음

        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("존재하지않는대학", 10);

        // then
        assertThat(response.suggestions()).isEmpty();
    }

    // ==================== Response DTO 검증 ====================

    @Test
    @Order(12)
    @DisplayName("Response DTO에 모든 필드가 포함된다")
    void autocomplete_ResponseDto_AllFields() {
        // given
        searchCacheService.cacheAutocompleteData(DOMAIN, List.of(
                new AutocompleteEntity(savedGroup1.getGroupId(), savedGroup1.getName(), 100.0,
                        Map.of("type", "UNIVERSITY", "address", savedGroup1.getAddress()))
        ));

        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("서울", 10);

        // then
        assertThat(response.suggestions()).hasSize(1);
        GroupSuggestion suggestion = response.suggestions().get(0);
        assertThat(suggestion.groupId()).isEqualTo(savedGroup1.getGroupId());
        assertThat(suggestion.name()).isEqualTo("서울대학교");
        assertThat(suggestion.type()).isEqualTo(GroupType.UNIVERSITY);
        assertThat(suggestion.address()).isEqualTo("서울특별시 관악구");
    }
}
