# 검색 기능 강화 계획서 (Search Enhancement Plan)

## 📋 목차
1. [현황 분석](#1-현황-분석)
2. [요구사항 정의](#2-요구사항-정의)
3. [기술 선택 및 아키텍처](#3-기술-선택-및-아키텍처)
4. [구현 계획](#4-구현-계획)
5. [성능 고려사항](#5-성능-고려사항)
6. [마이그레이션 전략](#6-마이그레이션-전략)

---

## 1. 현황 분석

### 1.1 현재 검색 구현 상태

#### 그룹(소속) 검색
- **위치**: `smartmealtable-api/group`
- **엔드포인트**: `GET /api/v1/groups?type={type}&name={name}&page={page}&size={size}`
- **현재 구현**:
  ```java
  // GroupJpaRepository.java
  @Query("SELECT g FROM GroupJpaEntity g " +
         "WHERE (:type IS NULL OR g.type = :type) " +
         "AND (:name IS NULL OR g.name LIKE %:name%)")
  Page<GroupJpaEntity> searchGroups(@Param("type") GroupType type, 
                                     @Param("name") String name, 
                                     Pageable pageable);
  ```
- **문제점**:
  - `LIKE %:name%` 사용으로 Full Table Scan 발생 가능
  - 중간 일치 검색만 지원 (초성 검색, 오타 허용 미지원)
  - 자동완성 기능 없음
  - 인덱스 활용 불가

#### Recommendation 모듈의 키워드 검색
- **위치**: `smartmealtable-recommendation`, `smartmealtable-api/recommendation`
- **엔드포인트**: 
  - `GET /api/v1/recommendations?keyword={keyword}...` (추천 목록에서 검색)
- **현재 구현**:
  ```java
  // RecommendationDataRepositoryImpl.java
  public List<Store> findStoresInRadiusWithKeyword(..., String keyword, ...) {
      // StoreQueryDslRepository를 사용하여 검색
      StoreSearchResult result = storeQueryDslRepository.searchStores(
          keyword,  // 가게명 또는 카테고리명만 검색
          ...
      );
  }
  ```
  
  ```java
  // StoreQueryDslRepository.java
  if (keyword != null && !keyword.isBlank()) {
      // 가게명 또는 카테고리명 검색
      conditions.add(storeJpaEntity.name.containsIgnoreCase(keyword)
          .or(categoryJpaEntity.name.containsIgnoreCase(keyword)));
  }
  ```
- **검색 대상 (현재)**:
  - ✅ 음식점 이름 (Store.name)
  - ✅ 카테고리명 (Category.name)
  - ❌ **음식 이름 (Food.foodName)** - **누락!**
  
- **문제점**:
  - `LIKE` 패턴 사용으로 성능 저하
  - **음식 이름(Food.foodName) 검색 미지원** - "김치찌개", "된장찌개" 등 메뉴명으로 검색 불가
  - 자동완성 기능 없음
  - 한글 초성 검색 미지원
  - 검색어 추천 기능 없음

### 1.2 데이터 규모 예상 및 검증 필요성

#### 현재 데이터 규모
- **그룹(member_group)**: 
  - 대학교: 약 200-300개
  - 회사: 수천-수만 개 (지속 증가)
  - **현재 추정: 1,000 ~ 5,000 건** (초기)
  - **3년 후 예상: 10,000 ~ 50,000 건**
  
- **가게(store)**:
  - **현재 추정: 1,000 ~ 3,000 건** (초기)
  - **1년 후 예상: 수만 건**
  - **3년 후 예상: 10만+ 건**
  - 검색 빈도: 매우 높음

#### ⚠️ 검증 필요 사항
1. **실제 데이터 규모 확인**: 구현 전 현재 DB의 정확한 데이터 수 측정 필요
2. **메모리 사용량 계산**: Redis 키 개수 × 평균 키/값 크기로 필요 메모리 산출
3. **성능 프로토타입**: 1만 건 데이터로 실제 응답 시간 측정

---

## 2. 요구사항 정의

### 2.1 기능 요구사항

#### FR-1: 실시간 자동완성 (Autocomplete)
- **설명**: 사용자가 검색어를 입력하는 즉시 관련 검색어를 실시간으로 제안
- **대상**: 
  - 그룹 검색 (member_group)
  - Recommendation 검색 (store, category, **food**)
- **요구사항**:
  - 입력 즉시 응답 (최대 100ms 이내)
  - 최대 10개의 추천 검색어 제공
  - **검색 결과 타입 구분**: "음식점", "카테고리", "음식" 라벨 표시
  - 인기도, 관련성 기반 정렬
  - 검색어 하이라이팅 지원

#### FR-2: 검색 품질 개선
- **설명**: 단순 LIKE 검색을 넘어선 고급 검색 기능
- **요구사항**:
  - **한글 초성 검색**: 
    - 그룹: "ㄱㅇㄷ" → "고려대", "강원대"
    - 음식: "ㄱㅊㅉㄱ" → "김치찌개", "ㄷㅈㅉㄱ" → "된장찌개"
  - **오타 허용**: "셔울" → "서울", "삼섬" → "삼성", "김치찌게" → "김치찌개"
  - **동의어 처리**: 
    - 그룹: "회사" ≈ "기업", "대학" ≈ "학교"
    - 음식: "짜장면" ≈ "자장면", "라면" ≈ "ramen"
  - **부분 일치**: "서울대" → "서울대학교", "김치" → "김치찌개", "김치볶음밥"
  - **정확도 순 정렬**: 관련성 높은 결과 우선
  - **다중 도메인 검색**: 음식점, 카테고리, 음식 이름 통합 검색

#### FR-3: 검색어 추천 (Related Keywords)
- **설명**: 입력한 검색어와 관련된 다른 검색어 추천
- **요구사항**:
  - **도메인별 인기 검색어**: 
    - 그룹 인기 검색어 (예: "서울대학교", "삼성전자")
    - 음식 인기 검색어 (예: "김치찌개", "된장찌개", "비빔밥")
  - 사용자 검색 이력 기반 추천 (선택)
  - 카테고리별 인기 검색어 (예: "한식" → "김치찌개", "된장찌개")

### 2.2 비기능 요구사항

#### NFR-1: 성능
- 자동완성 API 응답 시간: p95 < 100ms
- 메인 검색 API 응답 시간: p95 < 300ms
- 동시 사용자 1000명 지원

#### NFR-2: 확장성
- 데이터 10만 건까지 성능 저하 없이 처리
- 캐시 활용으로 DB 부하 최소화

#### NFR-3: 가용성
- 검색 기능 장애 시에도 기본 검색은 동작 (fallback)
- 캐시 장애 시 DB로 fallback

---

## 3. 기술 선택 및 아키텍처

### 3.1 검색 기술 비교

#### 옵션 1: **Redis + Application Layer 처리** ⭐ **추천**

##### 장점
- ✅ **빠른 구현**: 기존 인프라 활용, 학습 곡선 낮음
- ✅ **낮은 운영 비용**: 별도 검색 엔진 불필요
- ✅ **충분한 성능**: 현재 데이터 규모(수만 건)에 적합
- ✅ **유연한 커스터마이징**: 한글 초성, 오타 허용 로직 직접 구현 가능
- ✅ **기존 Redis 활용**: 현재 프로젝트에 Redis 이미 사용 중

##### 단점
- ⚠️ 대용량 데이터(수십만 건 이상) 시 성능 한계
- ⚠️ 복잡한 검색 로직 직접 구현 필요

##### 아키텍처
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ 1. 검색 요청 (keyword)
       ▼
┌─────────────────────┐
│   API Layer         │
│  (Controller)       │
└──────┬──────────────┘
       │ 2. Service 호출
       ▼
┌─────────────────────┐
│ Application Service │
│ - 한글 초성 변환    │
│ - 오타 허용 로직    │
└──────┬──────────────┘
       │
       ├──── 3a. 캐시 조회 ────┐
       │                       │
       │                       ▼
       │              ┌──────────────┐
       │              │   Redis      │
       │              │ - ZSet (인기) │
       │              │ - Hash (데이터)│
       │              │ - List (추천)  │
       │              └──────────────┘
       │                       │
       │    4a. Cache Hit      │
       │◄──────────────────────┘
       │
       └──── 3b. DB 조회 (Cache Miss) ────┐
                                          │
                                          ▼
                                 ┌──────────────┐
                                 │   MySQL      │
                                 │ (RDB)        │
                                 └──────┬───────┘
                                        │
                         4b. DB Result  │
                     ┌──────────────────┘
                     │
                     ▼
              ┌─────────────┐
              │ 캐시 업데이트│
              └─────────────┘
```

##### Redis 데이터 구조 설계 (최적화 버전)

**핵심 원칙**: 메모리 효율성을 위해 **Prefix 길이를 1-2자로 제한**

```redis
# ==================== 1. 자동완성용 Sorted Set ====================
# 전략: prefix 길이를 1-2자로 제한하여 키 폭발 방지
# Key: autocomplete:group:{1-2글자 prefix}
# Value: group_id (이름 대신 ID 저장으로 메모리 절약)
# Score: popularity (조회수 기반 점수)

# 1글자 prefix (초성 포함)
ZADD autocomplete:group:ㅅ 1000 "1"  # 서울대학교 (ID=1)
ZADD autocomplete:group:ㅅ 950 "2"   # 서울시립대 (ID=2)
ZADD autocomplete:group:서 1000 "1"
ZADD autocomplete:group:서 950 "2"

# 2글자 prefix (선택적 - 자주 검색되는 것만)
ZADD autocomplete:group:서울 1000 "1"
ZADD autocomplete:group:ㅅㅇ 1000 "1"

# 메모리 절약 계산:
# - 기존 방식: 5만 그룹 × 평균 이름 5자 × 2(초성/일반) = 50만 키
# - 최적화 방식: 19(초성) × 2(일반+초성 1글자) + 인기 2글자 1000개 = 약 1,100 키 (99.8% 감소)

# ==================== 2. 그룹 ID → 상세 데이터 매핑 Hash ====================
# 실제 데이터는 별도 Hash에 저장
HSET group:detail:1 "name" "서울대학교" "type" "UNIVERSITY" "address" "서울특별시 관악구"
HSET group:detail:2 "name" "서울시립대학교" "type" "UNIVERSITY" "address" "서울특별시 동대문구"

# ==================== 3. 초성 역인덱스 (캐시 워밍용) ====================
# 초성 검색 최적화를 위한 미리 계산된 인덱스
# Key: chosung_index:group:ㅅㅇㄷㅎㄱ
# Value: Set of group_ids
SADD chosung_index:group:ㅅㅇㄷㅎㄱ "1"  # 서울대학교
SADD chosung_index:group:ㅅㅅㄷㅎㄱ "2"  # 서울시립대학교

# ==================== 4. 인기 검색어 Sorted Set ====================
ZADD trending:group 1500 "서울대학교"
ZADD trending:group 1200 "연세대학교"
ZADD trending:group 1000 "고려대학교"

# ==================== 5. 검색어 추천 (Related Keywords) ====================
SADD related:서울대 "서울대학교" "서울시립대" "서울과기대"

# ==================== 6. 사용자별 최근 검색어 (선택) ====================
LPUSH recent_search:user:123 "서울대학교"
LTRIM recent_search:user:123 0 9  # 최근 10개만 유지
```

**메모리 사용량 추정**:
```
그룹 (5만 건 기준):
- 자동완성 키: ~1,100개 × (100 byte key + 50 byte value) = 165 KB
- 상세 데이터: 50,000개 × 200 byte = 10 MB
- 초성 인덱스: ~10,000개 × 150 byte = 1.5 MB
- 인기 검색어: 100개 × 50 byte = 5 KB
소계: ~12 MB

음식점 + 음식 (5만 음식점 + 25만 음식 기준):
- 음식점 자동완성: ~1,100개 × 150 byte = 165 KB
- 음식 자동완성: ~1,100개 × 150 byte = 165 KB (음식명도 prefix 1-2자 제한)
- 상세 데이터 (음식점): 50,000개 × 250 byte = 12.5 MB
- 상세 데이터 (음식): 250,000개 × 150 byte = 37.5 MB
- 초성 인덱스 (통합): ~15,000개 × 200 byte = 3 MB
- 인기 검색어: 200개 × 50 byte = 10 KB
소계: ~53 MB

총합계: ~65 MB (여전히 매우 효율적!)
```

---

#### 옵션 2: **Elasticsearch**

##### 장점
- ✅ 강력한 Full-Text 검색 기능 (형태소 분석, N-gram)
- ✅ 대용량 데이터 처리 최적화
- ✅ 한글 검색 플러그인 (nori 분석기)
- ✅ 복잡한 검색 쿼리 지원 (bool, fuzzy, wildcard 등)
- ✅ RESTful API 제공

##### 단점
- ⚠️ **높은 학습 곡선**: 새로운 기술 스택 도입
- ⚠️ **운영 복잡도**: 별도 클러스터 관리 필요
- ⚠️ **인프라 비용**: 추가 서버 리소스 필요 (메모리, CPU)
- ⚠️ **오버엔지니어링**: 현재 데이터 규모에 과한 솔루션
- ⚠️ **데이터 동기화**: MySQL ↔ Elasticsearch 동기화 로직 필요

##### 언제 고려할까?
- 데이터가 **10만 건 이상**으로 증가
- **복잡한 검색 요구사항** 추가 (범위 검색, 지리 검색 강화 등)
- **검색 품질**이 비즈니스의 핵심일 때

---

#### 옵션 3: **MySQL Full-Text Index**

##### 장점
- ✅ 별도 인프라 불필요
- ✅ 트랜잭션 일관성 보장
- ✅ 간단한 구현

##### 단점
- ⚠️ **한글 지원 약함**: InnoDB Full-Text는 한글 형태소 분석 미흡
- ⚠️ **성능 한계**: 대용량 데이터에서 느림
- ⚠️ **초성 검색 불가**: 커스텀 로직 필요
- ⚠️ **제한적인 랭킹**: 정교한 relevance scoring 어려움

---

### 3.2 최종 선택: **Redis + Application Layer** ⭐

#### 선택 이유
1. **프로젝트 현황 고려**:
   - 현재 Redis 이미 사용 중 (캐싱)
   - **현재 데이터 규모: 수천 건** (Redis로 충분)
   - **3년 후 5만 건까지** 대응 가능 (최적화된 키 설계로)
   - 빠른 MVP 구축 필요

2. **기술적 적합성**:
   - Redis의 빠른 읽기 성능 (O(log N))
   - 한글 초성, 오타 허용 로직을 Application Layer에서 구현 가능
   - **메모리 효율성**: 5만 건 기준 약 12MB만 사용
   - 충분한 성능 보장 (p95 < 100ms)

3. **확장 가능성**:
   - 향후 Elasticsearch로 마이그레이션 용이
   - 검색 로직을 Service Layer에 캡슐화하여 변경 영향 최소화
   - **명확한 마이그레이션 시점 정의**: 10만 건 이상 또는 복잡한 검색 요구사항 발생 시

#### 핵심 문제 해결 방안

| 문제 | 해결책 |
|------|--------|
| **1. findAll() 성능 문제** | 캐시 워밍 시에만 사용, 실시간 검색에서는 초성/오타 인덱스 활용 |
| **2. Redis 키 폭발** | Prefix 1-2자로 제한, ID 기반 저장으로 99.8% 키 감소 |
| **3. 캐시 일관성** | 그룹 생성/수정 시 실시간 캐시 업데이트 + 24시간 TTL |
| **4. 편집 거리 성능** | 결과 5개 미만 시에만 사용, 사전 계산된 초성 인덱스 우선 활용 |
| **5. 동시성 제어** | Redis 원자성 보장 + 낙관적 동시성 제어 |
| **6. Fallback 전략** | Redis 장애 시 DB 직접 조회, 구체적인 Exception 처리 |
| **7. 테스트 부족** | 성능 벤치마크 포함한 체계적인 테스트 계획 수립 |
| **8. 인덱스 미활용** | 시작 일치 검색용 인덱스 추가, FULLTEXT INDEX 고려 |

---

## 4. 구현 계획

### 4.1 아키텍처 설계

#### 모듈 구조
```
smartmealtable-support/
└── search/
    ├── korean/
    │   ├── KoreanSearchUtil.java          # 한글 초성, 자모 분리
    │   └── KoreanFuzzyMatcher.java        # 오타 허용 로직 (Phase 1에서 제외 가능)
    ├── cache/
    │   ├── SearchCacheService.java        # Redis 캐시 관리
    │   ├── SearchCacheWarmer.java         # 캐시 워밍
    │   └── ChosungIndexBuilder.java       # 초성 역인덱스 빌더 (신규)
    └── ranking/
        └── RelevanceScorer.java           # 검색 결과 랭킹

smartmealtable-api/
├── group/
│   ├── controller/
│   │   └── GroupController.java (기존 + 자동완성 엔드포인트 추가)
│   └── service/
│       ├── SearchGroupsService.java (기존)
│       └── GroupAutocompleteService.java (신규)
└── recommendation/
    ├── controller/
    │   └── RecommendationController.java (기존 + 자동완성 엔드포인트 추가)
    └── service/
        ├── RecommendationApplicationService.java (기존 - 검색 로직 개선)
        └── RecommendationAutocompleteService.java (신규)

smartmealtable-domain/
├── member/
│   └── repository/
│       └── GroupRepository.java (시작 일치 검색 메서드 추가)
└── search/
    └── entity/
        ├── SearchKeyword.java             # 검색어 히스토리
        └── TrendingKeyword.java           # 인기 검색어

smartmealtable-admin/ (신규 추가)
└── group/
    └── service/
        └── GroupApplicationService.java (캐시 업데이트 로직 추가)
```

---

### 4.2 상세 구현 단계

#### Phase 1: 기반 구조 및 성능 검증 (3일)

##### 1.0 사전 작업: 데이터 규모 확인 및 성능 벤치마크 (0.5일)
```java
/**
 * 실제 데이터 규모 확인 스크립트
 */
@Test
void checkCurrentDataSize() {
    long groupCount = groupRepository.count();
    long storeCount = storeRepository.count();
    
    log.info("현재 그룹 수: {}", groupCount);
    log.info("현재 가게 수: {}", storeCount);
    
    // 평균 이름 길이 측정
    List<Group> sampleGroups = groupRepository.findAll(PageRequest.of(0, 100));
    double avgNameLength = sampleGroups.stream()
        .mapToInt(g -> g.getName().length())
        .average()
        .orElse(0);
    
    log.info("평균 그룹 이름 길이: {}", avgNameLength);
    
    // 예상 Redis 메모리 계산
    long estimatedKeys = calculateExpectedRedisKeys(groupCount, avgNameLength);
    long estimatedMemory = calculateExpectedMemory(groupCount);
    
    log.info("예상 Redis 키 개수: {}", estimatedKeys);
    log.info("예상 Redis 메모리 사용량: {} MB", estimatedMemory / 1024 / 1024);
}

/**
 * findAll() 성능 측정
 */
@Test
void benchmarkFindAll() {
    long start = System.currentTimeMillis();
    List<Group> groups = groupRepository.findAll();
    long end = System.currentTimeMillis();
    
    long executionTime = end - start;
    long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    
    log.info("findAll() 실행 시간: {} ms", executionTime);
    log.info("메모리 사용량: {} MB", memoryUsed / 1024 / 1024);
    
    // 기준: 1,000ms 이상이면 캐싱 필수
    assertThat(executionTime).isLessThan(1000);
}
```

##### 1.1 한글 검색 유틸리티 구현 (1일)
**파일**: `smartmealtable-support/search/korean/KoreanSearchUtil.java`

```java
package com.stdev.smartmealtable.support.search.korean;

/**
 * 한글 검색 유틸리티
 * - 초성 추출
 * - 자모 분리
 * - 유사도 계산
 */
public class KoreanSearchUtil {
    
    // 한글 유니코드 상수
    private static final int HANGUL_BASE = 0xAC00;  // '가'
    private static final int CHOSUNG_BASE = 0x1100; // 'ㄱ'
    private static final int JONGSUNG_BASE = 0x11A8; // 'ㄱ' (종성)
    
    // 초성 리스트
    private static final char[] CHOSUNG_LIST = {
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
        'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    
    /**
     * 한글 문자열에서 초성 추출
     * 예: "서울대학교" → "ㅅㅇㄷㅎㄱ"
     */
    public static String extractChosung(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (isHangul(ch)) {
                int unicode = ch - HANGUL_BASE;
                int chosungIndex = unicode / (21 * 28);
                result.append(CHOSUNG_LIST[chosungIndex]);
            } else if (isChosung(ch)) {
                result.append(ch);
            } else if (Character.isWhitespace(ch)) {
                // 공백 유지
            } else {
                // 영문, 숫자는 그대로
                result.append(ch);
            }
        }
        return result.toString();
    }
    
    /**
     * 한글 자모 완전 분리
     * 예: "서" → "ㅅㅓ"
     */
    public static String decomposeHangul(String text) {
        // 구현...
    }
    
    /**
     * 초성 매칭 여부 확인
     * 예: "ㅅㅇㄷ" matches "서울대학교"
     */
    public static boolean matchesChosung(String keyword, String target) {
        String targetChosung = extractChosung(target);
        String keywordChosung = extractChosung(keyword);
        return targetChosung.contains(keywordChosung);
    }
    
    /**
     * 편집 거리 (Levenshtein Distance) 계산
     * 오타 허용에 사용
     */
    public static int calculateEditDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[s1.length()][s2.length()];
    }
    
    private static boolean isHangul(char ch) {
        return ch >= 0xAC00 && ch <= 0xD7A3;
    }
    
    private static boolean isChosung(char ch) {
        return ch >= 0x1100 && ch <= 0x1112;
    }
    
    /**
     * 한글 여부 확인
     */
    public static boolean isKorean(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.chars().anyMatch(ch -> isHangul((char) ch) || isChosung((char) ch));
    }
}
```

**테스트 코드**:
```java
@Test
void extractChosung_Korean() {
    assertThat(KoreanSearchUtil.extractChosung("서울대학교")).isEqualTo("ㅅㅇㄷㅎㄱ");
    assertThat(KoreanSearchUtil.extractChosung("삼성전자")).isEqualTo("ㅅㅅㅈㅈ");
}

@Test
void matchesChosung_Success() {
    assertThat(KoreanSearchUtil.matchesChosung("ㅅㅇㄷ", "서울대학교")).isTrue();
    assertThat(KoreanSearchUtil.matchesChosung("ㅅㅅ", "삼성전자")).isTrue();
}

@Test
void calculateEditDistance_Performance() {
    // 1만 번 실행 시간 측정
    long start = System.nanoTime();
    for (int i = 0; i < 10000; i++) {
        KoreanSearchUtil.calculateEditDistance("서울", "서울대학교");
    }
    long end = System.nanoTime();
    
    long avgTime = (end - start) / 10000;
    log.info("평균 편집 거리 계산 시간: {} ns", avgTime);
    
    // 기준: 평균 100μs(100,000ns) 이하
    assertThat(avgTime).isLessThan(100_000);
}
```

---

##### 1.2 초성 역인덱스 빌더 구현 (0.5일)
**파일**: `smartmealtable-support/search/cache/ChosungIndexBuilder.java`

```java
package com.stdev.smartmealtable.support.search.cache;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 초성 역인덱스 빌더
 * findAll() 없이 초성 검색을 빠르게 처리하기 위한 인덱스 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChosungIndexBuilder {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String INDEX_PREFIX = "chosung_index:group:";
    private static final int INDEX_TTL_HOURS = 24;
    
    /**
     * 초성 인덱스 빌드 (캐시 워밍 시 호출)
     * 
     * @param groups 모든 그룹 목록
     */
    public void buildIndex(List<Group> groups) {
        log.info("초성 인덱스 빌드 시작: {} 건", groups.size());
        
        long start = System.currentTimeMillis();
        
        for (Group group : groups) {
            String chosung = KoreanSearchUtil.extractChosung(group.getName());
            
            // 1~5글자 초성 조합에 대해 인덱스 생성
            for (int len = 1; len <= Math.min(5, chosung.length()); len++) {
                String prefix = chosung.substring(0, len);
                String key = INDEX_PREFIX + prefix;
                
                // Set에 그룹 ID 저장
                redisTemplate.opsForSet().add(key, String.valueOf(group.getGroupId()));
                redisTemplate.expire(key, INDEX_TTL_HOURS, TimeUnit.HOURS);
            }
        }
        
        long end = System.currentTimeMillis();
        log.info("초성 인덱스 빌드 완료: {} ms", end - start);
    }
    
    /**
     * 초성으로 그룹 ID 조회 (O(1) 성능)
     * 
     * @param chosungPrefix 초성 접두사 (예: "ㅅㅇㄷ")
     * @return 해당하는 그룹 ID 목록
     */
    public List<Long> findGroupIdsByChosung(String chosungPrefix) {
        String key = INDEX_PREFIX + chosungPrefix;
        
        try {
            return redisTemplate.opsForSet().members(key).stream()
                .map(Long::parseLong)
                .toList();
        } catch (Exception e) {
            log.error("초성 인덱스 조회 실패: prefix={}", chosungPrefix, e);
            return List.of();
        }
    }
}
```

---

##### 1.3 검색 캐시 서비스 구현 (1일)
**파일**: `smartmealtable-support/search/cache/SearchCacheService.java`

```java
package com.stdev.smartmealtable.support.search.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 검색 캐시 서비스
 * Redis를 활용한 자동완성 및 검색어 추천
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchCacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String AUTOCOMPLETE_PREFIX = "autocomplete:";
    private static final String TRENDING_PREFIX = "trending:";
    private static final String RELATED_PREFIX = "related:";
    private static final int DEFAULT_TTL_HOURS = 24;
    
    /**
     * 자동완성 데이터 저장
     * 
     * @param domain 도메인 (group, store)
     * @param keyword 키워드
     * @param items 자동완성 항목들
     * @param scores 각 항목의 점수 (인기도)
     */
    public void saveAutocomplete(String domain, String keyword, 
                                   List<String> items, List<Double> scores) {
        String key = buildAutocompleteKey(domain, keyword);
        
        try {
            // Sorted Set에 저장
            for (int i = 0; i < items.size(); i++) {
                redisTemplate.opsForZSet().add(key, items.get(i), scores.get(i));
            }
            
            // TTL 설정
            redisTemplate.expire(key, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
            
            log.debug("자동완성 캐시 저장 완료: domain={}, keyword={}, count={}", 
                     domain, keyword, items.size());
        } catch (Exception e) {
            log.error("자동완성 캐시 저장 실패: domain={}, keyword={}", domain, keyword, e);
        }
    }
    
    /**
     * 자동완성 데이터 조회
     * 
     * @param domain 도메인
     * @param keyword 키워드
     * @param limit 최대 개수
     * @return 자동완성 결과 (인기도 순)
     */
    public List<String> getAutocomplete(String domain, String keyword, int limit) {
        String key = buildAutocompleteKey(domain, keyword);
        
        try {
            // 높은 점수(인기도) 순으로 조회
            Set<String> results = redisTemplate.opsForZSet()
                .reverseRange(key, 0, limit - 1);
            
            if (results == null || results.isEmpty()) {
                log.debug("자동완성 캐시 미스: domain={}, keyword={}", domain, keyword);
                return Collections.emptyList();
            }
            
            log.debug("자동완성 캐시 히트: domain={}, keyword={}, count={}", 
                     domain, keyword, results.size());
            return new ArrayList<>(results);
        } catch (Exception e) {
            log.error("자동완성 캐시 조회 실패: domain={}, keyword={}", domain, keyword, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 인기 검색어 저장
     */
    public void saveTrendingKeywords(String domain, Map<String, Double> keywordScores) {
        String key = buildTrendingKey(domain);
        
        try {
            keywordScores.forEach((keyword, score) -> 
                redisTemplate.opsForZSet().add(key, keyword, score));
            
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
            log.info("인기 검색어 업데이트: domain={}, count={}", domain, keywordScores.size());
        } catch (Exception e) {
            log.error("인기 검색어 저장 실패: domain={}", domain, e);
        }
    }
    
    /**
     * 인기 검색어 조회
     */
    public List<String> getTrendingKeywords(String domain, int limit) {
        String key = buildTrendingKey(domain);
        
        try {
            Set<String> results = redisTemplate.opsForZSet()
                .reverseRange(key, 0, limit - 1);
            return results != null ? new ArrayList<>(results) : Collections.emptyList();
        } catch (Exception e) {
            log.error("인기 검색어 조회 실패: domain={}", domain, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 검색 카운트 증가 (인기도 업데이트)
     * Redis의 ZINCRBY는 원자성이 보장되므로 동시성 제어 불필요
     */
    public void incrementSearchCount(String domain, String keyword) {
        String key = buildTrendingKey(domain);
        
        try {
            // Redis ZINCRBY는 원자적(atomic) 연산이므로 Race Condition 걱정 없음
            redisTemplate.opsForZSet().incrementScore(key, keyword, 1.0);
        } catch (Exception e) {
            log.error("검색 카운트 증가 실패: domain={}, keyword={}", domain, keyword, e);
        }
    }
    
    /**
     * 특정 그룹의 자동완성 캐시만 업데이트 (실시간 반영)
     * 
     * @param domain 도메인
     * @param group 업데이트할 그룹
     */
    public void updateGroupInCache(String domain, Group group) {
        String name = group.getName();
        Long groupId = group.getGroupId();
        
        try {
            // 1-2글자 prefix 키에 추가
            for (int i = 1; i <= Math.min(2, name.length()); i++) {
                String prefix = name.substring(0, i).toLowerCase();
                String key = buildAutocompleteKey(domain, prefix);
                
                // 기본 인기도 점수로 추가
                redisTemplate.opsForZSet().add(key, String.valueOf(groupId), 0.0);
                redisTemplate.expire(key, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
            }
            
            // 초성도 추가
            String chosung = KoreanSearchUtil.extractChosung(name);
            for (int i = 1; i <= Math.min(2, chosung.length()); i++) {
                String prefix = chosung.substring(0, i);
                String key = buildAutocompleteKey(domain, prefix);
                
                redisTemplate.opsForZSet().add(key, String.valueOf(groupId), 0.0);
                redisTemplate.expire(key, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
            }
            
            log.debug("그룹 캐시 업데이트: groupId={}, name={}", groupId, name);
        } catch (Exception e) {
            log.error("그룹 캐시 업데이트 실패: groupId={}", groupId, e);
        }
    }
    
    /**
     * 특정 그룹을 캐시에서 제거
     */
    public void removeGroupFromCache(String domain, Long groupId) {
        try {
            // 모든 자동완성 키에서 해당 그룹 ID 제거
            Set<String> keys = redisTemplate.keys(AUTOCOMPLETE_PREFIX + domain + ":*");
            if (keys != null) {
                for (String key : keys) {
                    redisTemplate.opsForZSet().remove(key, String.valueOf(groupId));
                }
            }
            log.debug("그룹 캐시 삭제: groupId={}", groupId);
        } catch (Exception e) {
            log.error("그룹 캐시 삭제 실패: groupId={}", groupId, e);
        }
    }
    
    /**
     * 연관 검색어 저장
     */
    public void saveRelatedKeywords(String keyword, List<String> relatedKeywords) {
        String key = buildRelatedKey(keyword);
        
        try {
            relatedKeywords.forEach(related -> 
                redisTemplate.opsForSet().add(key, related));
            
            redisTemplate.expire(key, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("연관 검색어 저장 실패: keyword={}", keyword, e);
        }
    }
    
    /**
     * 연관 검색어 조회
     */
    public List<String> getRelatedKeywords(String keyword, int limit) {
        String key = buildRelatedKey(keyword);
        
        try {
            Set<String> results = redisTemplate.opsForSet().members(key);
            if (results == null) return Collections.emptyList();
            
            return results.stream().limit(limit).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("연관 검색어 조회 실패: keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 캐시 무효화
     */
    public void invalidateCache(String domain) {
        try {
            Set<String> keys = redisTemplate.keys(AUTOCOMPLETE_PREFIX + domain + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("캐시 무효화 완료: domain={}, count={}", domain, keys.size());
            }
        } catch (Exception e) {
            log.error("캐시 무효화 실패: domain={}", domain, e);
        }
    }
    
    // Key 생성 헬퍼 메서드
    private String buildAutocompleteKey(String domain, String keyword) {
        return AUTOCOMPLETE_PREFIX + domain + ":" + keyword.toLowerCase();
    }
    
    private String buildTrendingKey(String domain) {
        return TRENDING_PREFIX + domain;
    }
    
    private String buildRelatedKey(String keyword) {
        return RELATED_PREFIX + keyword.toLowerCase();
    }
}
```

---

#### Phase 2: 그룹 검색 자동완성 (4일)

##### 2.0 Repository 레이어 개선 (0.5일)
**파일**: `smartmealtable-domain/member/repository/GroupRepository.java`

```java
public interface GroupRepository {
    // 기존 메서드...
    
    /**
     * 이름이 특정 문자열로 시작하는 그룹 조회 (인덱스 활용 가능)
     * 
     * @param prefix 검색 prefix
     * @return 매칭되는 그룹 목록
     */
    List<Group> findByNameStartsWith(String prefix);
    
    /**
     * 여러 ID로 그룹 일괄 조회 (IN query)
     */
    List<Group> findAllByIdIn(List<Long> groupIds);
}
```

**파일**: `smartmealtable-storage/db/member/repository/GroupJpaRepository.java`

```java
public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, Long> {
    // 기존 메서드...
    
    /**
     * 시작 일치 검색 (인덱스 활용 가능)
     */
    @Query("SELECT g FROM GroupJpaEntity g WHERE g.name LIKE :prefix%")
    List<GroupJpaEntity> findByNameStartsWith(@Param("prefix") String prefix);
    
    /**
     * IN query로 일괄 조회
     */
    List<GroupJpaEntity> findByGroupIdIn(List<Long> groupIds);
}
```

**DB 인덱스 추가**:
```sql
-- 시작 일치 검색을 위한 인덱스 (B-Tree 인덱스 활용 가능)
CREATE INDEX idx_group_name_prefix ON member_group(name(10));

-- 타입 + 이름 복합 인덱스
CREATE INDEX idx_group_type_name_prefix ON member_group(type, name(10));
```

---

##### 2.1 API 엔드포인트 추가 (0.5일)
**파일**: `smartmealtable-api/group/controller/GroupController.java`

```java
/**
 * 그룹 검색 자동완성
 * GET /api/v1/groups/autocomplete?keyword={keyword}&limit={limit}
 * 
 * @param keyword 검색 키워드 (최소 1글자)
 * @param limit 결과 개수 (기본값: 10, 최대: 20)
 * @return 자동완성 추천 목록
 */
@GetMapping("/autocomplete")
public ResponseEntity<ApiResponse<GroupAutocompleteResponse>> autocomplete(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "10") @Max(20) int limit
) {
    log.info("그룹 자동완성 요청: keyword={}, limit={}", keyword, limit);
    
    GroupAutocompleteResponse response = groupAutocompleteService
        .autocomplete(keyword, limit);
    
    return ResponseEntity.ok(ApiResponse.success(response));
}

/**
 * 인기 검색어 조회
 * GET /api/v1/groups/trending?limit={limit}
 */
@GetMapping("/trending")
public ResponseEntity<ApiResponse<TrendingKeywordsResponse>> getTrendingKeywords(
        @RequestParam(defaultValue = "10") int limit
) {
    TrendingKeywordsResponse response = groupAutocompleteService
        .getTrendingKeywords(limit);
    
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

---

##### 2.2 자동완성 서비스 구현
**파일**: `smartmealtable-api/group/service/GroupAutocompleteService.java`

```java
package com.stdev.smartmealtable.api.group.service;

import com.stdev.smartmealtable.api.group.service.dto.*;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.support.search.cache.SearchCacheService;
import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 그룹 자동완성 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupAutocompleteService {
    
    private final GroupRepository groupRepository;
    private final SearchCacheService searchCacheService;
    
    private static final String DOMAIN = "group";
    private static final int MAX_EDIT_DISTANCE = 2; // 오타 허용 범위
    
    /**
     * 그룹 자동완성
     * 
     * @param keyword 검색 키워드
     * @param limit 결과 개수
     * @return 자동완성 결과
     */
    public GroupAutocompleteResponse autocomplete(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return GroupAutocompleteResponse.empty();
        }
        
        keyword = keyword.trim();
        
        // 1. 캐시 조회
        List<String> cachedResults = searchCacheService
            .getAutocomplete(DOMAIN, keyword, limit);
        
        if (!cachedResults.isEmpty()) {
            List<GroupAutocompleteItem> items = cachedResults.stream()
                .map(name -> new GroupAutocompleteItem(name, "CACHED"))
                .collect(Collectors.toList());
            return new GroupAutocompleteResponse(items);
        }
        
        // 2. DB에서 검색
        List<Group> groups = searchGroups(keyword);
        
        // 3. 관련도 점수 계산 및 정렬
        List<ScoredGroup> scoredGroups = groups.stream()
            .map(group -> new ScoredGroup(group, calculateRelevance(keyword, group)))
            .sorted(Comparator.comparingDouble(ScoredGroup::score).reversed())
            .limit(limit)
            .collect(Collectors.toList());
        
        // 4. 캐시 저장
        if (!scoredGroups.isEmpty()) {
            List<String> names = scoredGroups.stream()
                .map(sg -> sg.group.getName())
                .collect(Collectors.toList());
            List<Double> scores = scoredGroups.stream()
                .map(ScoredGroup::score)
                .collect(Collectors.toList());
            
            searchCacheService.saveAutocomplete(DOMAIN, keyword, names, scores);
        }
        
        // 5. 검색 카운트 증가 (비동기 처리 권장)
        searchCacheService.incrementSearchCount(DOMAIN, keyword);
        
        // 6. 응답 생성
        List<GroupAutocompleteItem> items = scoredGroups.stream()
            .map(sg -> new GroupAutocompleteItem(
                sg.group.getName(),
                sg.group.getType().name()
            ))
            .collect(Collectors.toList());
        
        return new GroupAutocompleteResponse(items);
    }
    
    /**
     * 다양한 전략으로 그룹 검색 (최적화 버전)
     * findAll() 제거하고 초성 인덱스 활용
     */
    private List<Group> searchGroups(String keyword) {
        Set<Group> results = new HashSet<>();
        
        // 전략 1: 시작 일치 (인덱스 활용, 가장 빠름)
        List<Group> startsWithMatches = groupRepository.findByNameStartsWith(keyword);
        results.addAll(startsWithMatches);
        
        // 전략 2: 부분 일치 (LIKE 검색)
        if (results.size() < 10) {
            List<Group> containsMatches = groupRepository.findByNameContaining(keyword);
            results.addAll(containsMatches);
        }
        
        // 전략 3: 초성 검색 (초성 인덱스 활용 - findAll() 제거!)
        if (KoreanSearchUtil.isKorean(keyword) && results.size() < 10) {
            String chosung = KoreanSearchUtil.extractChosung(keyword);
            
            // 초성 인덱스에서 그룹 ID 조회 (O(1))
            List<Long> groupIds = chosungIndexBuilder.findGroupIdsByChosung(chosung);
            
            // ID로 일괄 조회 (IN query, 효율적)
            if (!groupIds.isEmpty()) {
                List<Group> chosungMatches = groupRepository.findAllByIdIn(groupIds);
                results.addAll(chosungMatches);
            }
        }
        
        // 전략 4: 오타 허용 (편집 거리) - 최후의 수단
        // 결과가 5개 미만이고, 키워드가 짧을 때만 (성능 보호)
        if (results.size() < 5 && keyword.length() <= 10) {
            // 시작 일치하는 그룹 중에서만 편집 거리 계산 (범위 축소)
            List<Group> candidates = groupRepository.findByNameStartsWith(
                keyword.substring(0, Math.min(2, keyword.length()))
            );
            
            List<Group> fuzzyMatches = candidates.stream()
                .filter(g -> {
                    int distance = KoreanSearchUtil.calculateEditDistance(
                        keyword.toLowerCase(),
                        g.getName().toLowerCase()
                    );
                    return distance <= MAX_EDIT_DISTANCE;
                })
                .limit(10) // 최대 10개로 제한
                .collect(Collectors.toList());
            
            results.addAll(fuzzyMatches);
        }
        
        return new ArrayList<>(results);
    }
    
    /**
     * 검색어와 그룹명의 관련도 점수 계산
     * 
     * @return 0.0 ~ 1.0 사이의 점수 (높을수록 관련성 높음)
     */
    private double calculateRelevance(String keyword, Group group) {
        String target = group.getName().toLowerCase();
        String query = keyword.toLowerCase();
        
        double score = 0.0;
        
        // 1. 정확한 일치 (가장 높은 점수)
        if (target.equals(query)) {
            score += 1.0;
        }
        // 2. 시작 일치
        else if (target.startsWith(query)) {
            score += 0.9;
        }
        // 3. 부분 일치
        else if (target.contains(query)) {
            score += 0.7;
        }
        // 4. 초성 일치
        else if (KoreanSearchUtil.matchesChosung(query, target)) {
            score += 0.6;
        }
        // 5. 오타 허용 (편집 거리)
        else {
            int distance = KoreanSearchUtil.calculateEditDistance(query, target);
            if (distance <= MAX_EDIT_DISTANCE) {
                score += Math.max(0, 0.5 - (distance * 0.15));
            }
        }
        
        // 보정: 짧은 이름에 가산점 (가독성)
        if (target.length() <= 10) {
            score += 0.05;
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * 인기 검색어 조회
     */
    public TrendingKeywordsResponse getTrendingKeywords(int limit) {
        List<String> trending = searchCacheService
            .getTrendingKeywords(DOMAIN, limit);
        
        return new TrendingKeywordsResponse(trending);
    }
    
    // 내부 DTO
    private record ScoredGroup(Group group, double score) {}
}
```

---

##### 2.3 Admin API 캐시 연동 (0.5일)
**파일**: `smartmealtable-admin/group/service/GroupApplicationService.java`

```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GroupApplicationService {
    
    private final GroupRepository groupRepository;
    private final SearchCacheService searchCacheService;
    private final ChosungIndexBuilder chosungIndexBuilder;
    
    /**
     * 그룹 생성 (캐시 즉시 업데이트)
     */
    public GroupCreateServiceResponse createGroup(GroupCreateServiceRequest request) {
        // 1. 그룹 생성
        Group group = Group.create(
            request.name(),
            request.type(),
            request.address()
        );
        Group savedGroup = groupRepository.save(group);
        
        // 2. 캐시에 즉시 반영 (실시간 업데이트)
        try {
            searchCacheService.updateGroupInCache("group", savedGroup);
            log.info("그룹 생성 후 캐시 업데이트 완료: groupId={}", savedGroup.getGroupId());
        } catch (Exception e) {
            // 캐시 업데이트 실패해도 그룹 생성은 성공
            log.error("캐시 업데이트 실패 (무시)", e);
        }
        
        return GroupCreateServiceResponse.from(savedGroup);
    }
    
    /**
     * 그룹 수정 (캐시 갱신)
     */
    public void updateGroup(Long groupId, GroupUpdateServiceRequest request) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        // 1. 그룹 업데이트
        group.update(request.name(), request.address());
        groupRepository.save(group);
        
        // 2. 캐시 갱신
        try {
            // 기존 캐시 삭제 후 재등록
            searchCacheService.removeGroupFromCache("group", groupId);
            searchCacheService.updateGroupInCache("group", group);
        } catch (Exception e) {
            log.error("캐시 갱신 실패 (무시)", e);
        }
    }
    
    /**
     * 그룹 삭제 (캐시 제거)
     */
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new GroupNotFoundException(groupId));
        
        // 1. Soft Delete
        group.delete();
        groupRepository.save(group);
        
        // 2. 캐시에서 제거
        try {
            searchCacheService.removeGroupFromCache("group", groupId);
        } catch (Exception e) {
            log.error("캐시 삭제 실패 (무시)", e);
        }
    }
}
```

**결과**: 그룹 생성/수정/삭제 시 캐시가 **즉시 반영**되어, 24시간 기다리지 않고 최신 데이터 검색 가능!

---

#### Phase 3: Recommendation 검색 개선 (음식점 + 카테고리 + 음식) (4일)

##### 3.1 음식 검색용 Repository 개선 (1일)
**파일**: `smartmealtable-storage/db/store/StoreQueryDslRepository.java`

**핵심 변경: Food 테이블 JOIN 추가**
```java
/**
 * Recommendation용 통합 검색 (음식점 + 카테고리 + 음식 이름)
 */
public StoreSearchResult searchStoresWithFood(
        String keyword,
        BigDecimal userLatitude,
        BigDecimal userLongitude,
        Double radiusKm,
        Long categoryId,
        Boolean isOpenOnly,
        StoreType storeType,
        String sortBy,
        int page,
        int size
) {
    // Haversine 거리 계산
    NumberExpression<Double> distanceExpression = calculateDistance(
            userLatitude.doubleValue(),
            userLongitude.doubleValue()
    );
    
    // 조건 생성
    List<BooleanExpression> conditions = new ArrayList<>();
    conditions.add(storeJpaEntity.deletedAt.isNull());
    
    if (keyword != null && !keyword.isBlank()) {
        // 🔥 핵심: 세 가지 모두 검색
        BooleanExpression searchCondition = storeJpaEntity.name.containsIgnoreCase(keyword)
            .or(categoryJpaEntity.name.containsIgnoreCase(keyword))
            .or(foodJpaEntity.foodName.containsIgnoreCase(keyword));  // Food 추가!
        
        conditions.add(searchCondition);
    }
    
    if (radiusKm != null) {
        conditions.add(distanceExpression.loe(radiusKm));
    }
    
    if (categoryId != null) {
        conditions.add(storeCategoryJpaEntity.categoryId.eq(categoryId));
    }
    
    if (isOpenOnly != null && isOpenOnly) {
        conditions.add(createOpenNowCondition());
    }
    
    if (storeType != null) {
        conditions.add(storeJpaEntity.storeType.eq(storeType));
    }
    
    BooleanExpression finalCondition = conditions.stream()
            .reduce(BooleanExpression::and)
            .orElse(null);
    
    // 총 개수 조회
    Long totalCount = queryFactory
            .select(storeJpaEntity.countDistinct())  // DISTINCT 중요!
            .from(storeJpaEntity)
            .leftJoin(storeCategoryJpaEntity).on(storeJpaEntity.storeId.eq(storeCategoryJpaEntity.storeId))
            .leftJoin(categoryJpaEntity).on(storeCategoryJpaEntity.categoryId.eq(categoryJpaEntity.categoryId))
            .leftJoin(foodJpaEntity).on(storeJpaEntity.storeId.eq(foodJpaEntity.storeId)
                                        .and(foodJpaEntity.deletedAt.isNull()))  // 삭제 안된 음식만
            .where(finalCondition)
            .fetchOne();
    
    if (totalCount == null || totalCount == 0) {
        return new StoreSearchResult(List.of(), 0);
    }
    
    // 정렬 기준 결정
    OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortBy, distanceExpression);
    
    // 데이터 조회 (Store + Distance) - DISTINCT로 중복 제거
    List<Tuple> tuples = queryFactory
            .select(storeJpaEntity, distanceExpression)
            .distinct()  // Food JOIN으로 인한 중복 제거
            .from(storeJpaEntity)
            .leftJoin(storeCategoryJpaEntity).on(storeJpaEntity.storeId.eq(storeCategoryJpaEntity.storeId))
            .leftJoin(categoryJpaEntity).on(storeCategoryJpaEntity.categoryId.eq(categoryJpaEntity.categoryId))
            .leftJoin(foodJpaEntity).on(storeJpaEntity.storeId.eq(foodJpaEntity.storeId)
                                        .and(foodJpaEntity.deletedAt.isNull()))
            .where(finalCondition)
            .orderBy(orderSpecifier)
            .offset((long) page * size)
            .limit(size)
            .fetch();
    
    List<StoreWithDistance> stores = tuples.stream()
            .map(tuple -> {
                StoreJpaEntity entity = tuple.get(0, StoreJpaEntity.class);
                Double distance = tuple.get(1, Double.class);
                
                List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                Store store = StoreEntityMapper.toDomain(entity, categoryIds);
                
                return new StoreWithDistance(store, distance);
            })
            .collect(Collectors.toList());
    
    return new StoreSearchResult(stores, totalCount.intValue());
}
```

**주의사항**:
- `DISTINCT` 필수: Food 1:N 관계로 인한 Store 중복 방지
- `food.deletedAt IS NULL` 조건 추가: 삭제된 음식 제외
- 기존 `searchStores()` 메서드는 유지 (Store API용)

---

##### 3.2 Recommendation 자동완성 서비스 구현 (1.5일)
**파일**: `smartmealtable-api/recommendation/service/RecommendationAutocompleteService.java`

```java
package com.stdev.smartmealtable.api.recommendation.service;

import com.stdev.smartmealtable.api.recommendation.dto.*;
import com.stdev.smartmealtable.domain.store.entity.Store;
import com.stdev.smartmealtable.domain.food.entity.Food;
import com.stdev.smartmealtable.support.search.cache.SearchCacheService;
import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommendation 자동완성 서비스
 * 음식점, 카테고리, 음식 이름 통합 검색
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationAutocompleteService {
    
    private final SearchCacheService searchCacheService;
    private final StoreRepository storeRepository;
    private final FoodRepository foodRepository;
    
    /**
     * 통합 자동완성 (음식점 + 카테고리 + 음식)
     */
    public RecommendationAutocompleteResponse autocomplete(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return RecommendationAutocompleteResponse.empty();
        }
        
        log.debug("Recommendation 자동완성 요청: keyword={}, limit={}", keyword, limit);
        
        // 1. 캐시 조회 시도
        List<AutocompleteItem> cachedResults = searchCacheService
            .getAutocomplete("recommendation", keyword, limit);
        
        if (!cachedResults.isEmpty()) {
            log.debug("캐시 히트: {} 건", cachedResults.size());
            return RecommendationAutocompleteResponse.of(cachedResults);
        }
        
        // 2. 캐시 미스 시 DB 검색
        log.debug("캐시 미스, DB 검색 실행");
        List<AutocompleteItem> results = searchFromDatabase(keyword, limit);
        
        return RecommendationAutocompleteResponse.of(results);
    }
    
    /**
     * DB에서 직접 검색 (캐시 미스 시)
     */
    private List<AutocompleteItem> searchFromDatabase(String keyword, int limit) {
        List<AutocompleteItem> results = new ArrayList<>();
        
        // 1️⃣ 음식점 검색 (시작 일치 우선)
        List<Store> stores = storeRepository.findByNameStartsWith(keyword);
        if (stores.isEmpty()) {
            stores = storeRepository.findByNameContaining(keyword);
        }
        stores.stream()
            .limit(limit / 3)  // 전체의 1/3
            .forEach(store -> results.add(
                AutocompleteItem.of(
                    store.getStoreId(),
                    store.getName(),
                    "STORE",
                    store.getViewCount()
                )
            ));
        
        // 2️⃣ 음식 검색
        List<Food> foods = foodRepository.findByFoodNameStartsWith(keyword);
        if (foods.isEmpty()) {
            foods = foodRepository.findByFoodNameContaining(keyword);
        }
        foods.stream()
            .limit(limit / 3)  // 전체의 1/3
            .forEach(food -> results.add(
                AutocompleteItem.of(
                    food.getFoodId(),
                    food.getFoodName(),
                    "FOOD",
                    0  // 음식은 조회수 없음
                )
            ));
        
        // 3️⃣ 카테고리 검색 (인기 검색어에서)
        List<String> trendingKeywords = searchCacheService
            .getTrendingKeywords("recommendation", limit / 3);
        
        trendingKeywords.stream()
            .filter(kw -> kw.contains(keyword))
            .forEach(kw -> results.add(
                AutocompleteItem.of(
                    null,
                    kw,
                    "CATEGORY",
                    0
                )
            ));
        
        // 정확도 순 정렬
        results.sort(Comparator
            .comparing((AutocompleteItem item) -> calculateRelevance(keyword, item.getName()))
            .reversed()
            .thenComparing(AutocompleteItem::getPopularity, Comparator.reverseOrder())
        );
        
        return results.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * 관련성 점수 계산
     */
    private double calculateRelevance(String keyword, String target) {
        if (target.startsWith(keyword)) return 3.0;  // 시작 일치
        if (target.contains(keyword)) return 2.0;    // 부분 일치
        
        // 초성 매칭
        if (KoreanSearchUtil.isKorean(keyword) && 
            KoreanSearchUtil.matchesChosung(keyword, target)) {
            return 1.5;
        }
        
        // 편집 거리 (짧은 키워드만)
        if (keyword.length() <= 5) {
            int distance = KoreanSearchUtil.calculateEditDistance(keyword, target);
            if (distance <= 2) return 1.0 / (distance + 1);
        }
        
        return 0.0;
    }
}
```

**DTO**:
```java
@Getter
@AllArgsConstructor
public class RecommendationAutocompleteResponse {
    private List<AutocompleteItem> items;
    
    public static RecommendationAutocompleteResponse of(List<AutocompleteItem> items) {
        return new RecommendationAutocompleteResponse(items);
    }
    
    public static RecommendationAutocompleteResponse empty() {
        return new RecommendationAutocompleteResponse(List.of());
    }
}

@Getter
@AllArgsConstructor
public class AutocompleteItem {
    private Long id;
    private String name;
    private String type;  // "STORE", "FOOD", "CATEGORY"
    private Integer popularity;
    
    public static AutocompleteItem of(Long id, String name, String type, Integer popularity) {
        return new AutocompleteItem(id, name, type, popularity);
    }
}
```

---

##### 3.3 Food Repository 메서드 추가 (0.5일)
**파일**: `smartmealtable-domain/food/FoodRepository.java`

```java
public interface FoodRepository {
    // 기존 메서드...
    
    /**
     * 음식 이름으로 시작 일치 검색 (B-Tree 인덱스 활용)
     */
    List<Food> findByFoodNameStartsWith(String foodName);
    
    /**
     * 음식 이름 부분 일치 검색
     */
    List<Food> findByFoodNameContaining(String foodName);
    
    /**
     * 음식 ID 리스트로 조회 (IN query)
     */
    List<Food> findAllByIdIn(List<Long> foodIds);
}
```

**DB 인덱스 추가**:
```sql
-- 음식 이름 시작 일치 검색용 인덱스
CREATE INDEX idx_food_name_prefix ON food(food_name(10));

-- 음식점 ID + 삭제 여부 복합 인덱스
CREATE INDEX idx_food_store_deleted ON food(store_id, deleted_at);
```

---

##### 3.4 API 엔드포인트 추가 (0.5일)
**파일**: `smartmealtable-api/recommendation/controller/RecommendationController.java`

```java
/**
 * Recommendation 자동완성
 * 음식점, 카테고리, 음식 이름 통합 검색
 */
@GetMapping("/autocomplete")
public ResponseEntity<ApiResponse<RecommendationAutocompleteResponse>> autocomplete(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "10") @Max(20) int limit
) {
    log.info("Recommendation 자동완성 요청: keyword={}, limit={}", keyword, limit);
    
    RecommendationAutocompleteResponse response = recommendationAutocompleteService
        .autocomplete(keyword, limit);
    
    return ResponseEntity.ok(ApiResponse.success(response));
}

/**
 * 인기 검색어 조회
 */
@GetMapping("/trending")
public ResponseEntity<ApiResponse<TrendingKeywordsResponse>> getTrendingKeywords(
        @RequestParam(defaultValue = "10") int limit
) {
    TrendingKeywordsResponse response = recommendationAutocompleteService
        .getTrendingKeywords(limit);
    
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

---

##### 3.5 캐시 워밍에 Food 추가 (0.5일)
**파일**: `smartmealtable-scheduler/search/SearchCacheWarmer.java`

```java
/**
 * Recommendation 캐시 워밍 (음식점 + 음식)
 */
@Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시
public void warmRecommendationCache() {
    log.info("Recommendation 캐시 워밍 시작");
    Instant start = Instant.now();
    
    try {
        // 1️⃣ 음식점 데이터 캐싱
        List<Store> stores = storeRepository.findAll();
        log.info("총 {} 개 음식점 캐싱 시작", stores.size());
        
        stores.forEach(store -> {
            // Prefix 1-2자만 저장
            String name = store.getName();
            if (name.length() >= 1) {
                String prefix1 = name.substring(0, 1);
                searchCacheService.addToAutocomplete("recommendation", prefix1, 
                                                     String.valueOf(store.getStoreId()), 
                                                     store.getViewCount());
                
                // 초성도 추가
                String chosung = KoreanSearchUtil.extractChosung(name);
                if (!chosung.isEmpty()) {
                    searchCacheService.addToAutocomplete("recommendation", 
                                                         chosung.substring(0, 1), 
                                                         String.valueOf(store.getStoreId()), 
                                                         store.getViewCount());
                }
            }
            
            if (name.length() >= 2) {
                String prefix2 = name.substring(0, 2);
                searchCacheService.addToAutocomplete("recommendation", prefix2, 
                                                     String.valueOf(store.getStoreId()), 
                                                     store.getViewCount());
            }
            
            // 상세 데이터 저장
            searchCacheService.saveDetail("recommendation", 
                                          String.valueOf(store.getStoreId()), 
                                          Map.of(
                                              "name", store.getName(),
                                              "type", "STORE",
                                              "viewCount", String.valueOf(store.getViewCount())
                                          ));
        });
        
        // 2️⃣ 음식 데이터 캐싱
        List<Food> foods = foodRepository.findAllNotDeleted();  // 삭제 안된 것만
        log.info("총 {} 개 음식 캐싱 시작", foods.size());
        
        foods.forEach(food -> {
            String foodName = food.getFoodName();
            if (foodName.length() >= 1) {
                String prefix1 = foodName.substring(0, 1);
                searchCacheService.addToAutocomplete("recommendation", prefix1, 
                                                     "food:" + food.getFoodId(),  // 음식은 "food:" prefix
                                                     0);  // 음식은 조회수 없음
                
                String chosung = KoreanSearchUtil.extractChosung(foodName);
                if (!chosung.isEmpty()) {
                    searchCacheService.addToAutocomplete("recommendation", 
                                                         chosung.substring(0, 1), 
                                                         "food:" + food.getFoodId(), 
                                                         0);
                }
            }
            
            if (foodName.length() >= 2) {
                String prefix2 = foodName.substring(0, 2);
                searchCacheService.addToAutocomplete("recommendation", prefix2, 
                                                     "food:" + food.getFoodId(), 
                                                     0);
            }
            
            // 상세 데이터 저장
            searchCacheService.saveDetail("recommendation", 
                                          "food:" + food.getFoodId(), 
                                          Map.of(
                                              "name", food.getFoodName(),
                                              "type", "FOOD",
                                              "storeId", String.valueOf(food.getStoreId())
                                          ));
        });
        
        // 3️⃣ 초성 역인덱스 빌드
        chosungIndexBuilder.buildIndex("recommendation", stores, foods);
        
        Duration duration = Duration.between(start, Instant.now());
        log.info("Recommendation 캐시 워밍 완료 - {}ms (음식점: {}, 음식: {})", 
                 duration.toMillis(), stores.size(), foods.size());
        
    } catch (Exception e) {
        log.error("Recommendation 캐시 워밍 실패", e);
    }
}
```

---

#### Phase 4: 캐시 워밍 & 스케줄러 (2일)

##### 4.1 캐시 워밍 배치 작업
**파일**: `smartmealtable-scheduler/search/SearchCacheWarmer.java`

```java
package com.stdev.smartmealtable.scheduler.search;

import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.support.search.cache.SearchCacheService;
import com.stdev.smartmealtable.support.search.korean.KoreanSearchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 검색 캐시 워밍
 * 주기적으로 인기 검색어의 자동완성 데이터를 미리 캐싱
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchCacheWarmer {
    
    private final GroupRepository groupRepository;
    private final SearchCacheService searchCacheService;
    
    /**
     * 매일 새벽 3시에 그룹 검색 캐시 워밍
     * findAll()은 여기서만 사용 (배치 작업이므로 성능 영향 최소화)
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void warmGroupSearchCache() {
        log.info("그룹 검색 캐시 워밍 시작");
        
        try {
            // 1. 모든 그룹 조회 (배치 작업에서만 허용)
            List<Group> allGroups = groupRepository.findAll();
            log.info("총 {} 개 그룹 로드", allGroups.size());
            
            // 2. 초성 역인덱스 빌드
            chosungIndexBuilder.buildIndex(allGroups);
            
            // 3. 자동완성 캐시 빌드 (1-2글자 prefix만)
            Map<String, List<GroupCacheEntry>> prefixMap = new HashMap<>();
            
            for (Group group : allGroups) {
                String name = group.getName();
                Long groupId = group.getGroupId();
                
                // 1-2글자 prefix만 생성 (키 폭발 방지)
                for (int i = 1; i <= Math.min(2, name.length()); i++) {
                    String prefix = name.substring(0, i).toLowerCase();
                    
                    prefixMap.computeIfAbsent(prefix, k -> new ArrayList<>())
                             .add(new GroupCacheEntry(groupId, name, group.getType()));
                    
                    // 초성도 함께 캐싱
                    String chosung = KoreanSearchUtil.extractChosung(prefix);
                    if (!chosung.equals(prefix)) {
                        prefixMap.computeIfAbsent(chosung, k -> new ArrayList<>())
                                 .add(new GroupCacheEntry(groupId, name, group.getType()));
                    }
                }
            }
            
            // 4. Redis에 일괄 저장 (Pipeline 사용)
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) {
                    prefixMap.forEach((prefix, entries) -> {
                        String key = "autocomplete:group:" + prefix;
                        
                        // ID만 저장 (메모리 절약)
                        for (GroupCacheEntry entry : entries) {
                            double score = calculatePopularity(entry);
                            operations.opsForZSet().add(key, String.valueOf(entry.groupId()), score);
                        }
                        
                        operations.expire(key, 24, TimeUnit.HOURS);
                    });
                    return null;
                }
            });
            
            log.info("그룹 검색 캐시 워밍 완료: {} prefixes, {} groups", 
                     prefixMap.size(), allGroups.size());
        } catch (Exception e) {
            log.error("그룹 검색 캐시 워밍 실패", e);
        }
    }
    
    /**
     * 인기도 점수 계산
     * TODO: 실제 조회수, 회원 가입 수 등을 반영하도록 개선
     */
    private double calculatePopularity(GroupCacheEntry entry) {
        // 임시: 대학교에 가중치 부여
        double baseScore = 0.0;
        if (entry.type() == GroupType.UNIVERSITY) {
            baseScore = 100.0;
        } else if (entry.type() == GroupType.COMPANY) {
            baseScore = 50.0;
        }
        
        return baseScore;
    }
    
    // 내부 DTO
    private record GroupCacheEntry(Long groupId, String name, GroupType type) {}
}
}
```

---

#### Phase 5: 테스트 & 성능 검증 (5일)

##### 5.1 단위 테스트 (1.5일)

**파일**: `KoreanSearchUtilTest.java`
```java
@DisplayName("한글 검색 유틸리티 테스트")
class KoreanSearchUtilTest {
    
    @Test
    @DisplayName("초성 추출 - 한글")
    void extractChosung_Korean() {
        assertThat(KoreanSearchUtil.extractChosung("서울대학교")).isEqualTo("ㅅㅇㄷㅎㄱ");
        assertThat(KoreanSearchUtil.extractChosung("삼성전자")).isEqualTo("ㅅㅅㅈㅈ");
    }
    
    @Test
    @DisplayName("초성 추출 - 한글+영문 혼합")
    void extractChosung_Mixed() {
        assertThat(KoreanSearchUtil.extractChosung("KAIST")).isEqualTo("KAIST");
        assertThat(KoreanSearchUtil.extractChosung("서울IT고등학교")).isEqualTo("ㅅㅇITㄱㄷㅎㄱ");
    }
    
    @Test
    @DisplayName("초성 매칭 - 성공")
    void matchesChosung_Success() {
        assertThat(KoreanSearchUtil.matchesChosung("ㅅㅇㄷ", "서울대학교")).isTrue();
        assertThat(KoreanSearchUtil.matchesChosung("ㅅㅅ", "삼성전자")).isTrue();
    }
    
    @Test
    @DisplayName("초성 매칭 - 실패")
    void matchesChosung_Fail() {
        assertThat(KoreanSearchUtil.matchesChosung("ㄱㅇㄷ", "서울대학교")).isFalse();
    }
    
    @Test
    @DisplayName("편집 거리 계산 - 정확한 일치")
    void calculateEditDistance_Exact() {
        assertThat(KoreanSearchUtil.calculateEditDistance("서울", "서울")).isEqualTo(0);
    }
    
    @Test
    @DisplayName("편집 거리 계산 - 1글자 차이")
    void calculateEditDistance_OneChar() {
        assertThat(KoreanSearchUtil.calculateEditDistance("서울", "서을")).isEqualTo(1);
        assertThat(KoreanSearchUtil.calculateEditDistance("삼성", "상성")).isEqualTo(1);
    }
    
    @Test
    @DisplayName("편집 거리 성능 테스트 - 1만번 실행")
    void calculateEditDistance_Performance() {
        int iterations = 10000;
        long start = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            KoreanSearchUtil.calculateEditDistance("서울대학교", "서울대");
        }
        
        long end = System.nanoTime();
        long avgTimeNs = (end - start) / iterations;
        
        log.info("평균 편집 거리 계산 시간: {} ns ({} μs)", avgTimeNs, avgTimeNs / 1000);
        
        // 기준: 평균 100μs 이하
        assertThat(avgTimeNs).isLessThan(100_000);
    }
}
```

**파일**: `SearchCacheServiceTest.java`
```java
@SpringBootTest
@Testcontainers
class SearchCacheServiceTest extends AbstractContainerTest {
    
    @Autowired
    private SearchCacheService searchCacheService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @AfterEach
    void cleanup() {
        // Redis 캐시 전체 삭제
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
    
    @Test
    @DisplayName("자동완성 저장 및 조회")
    void saveAndGetAutocomplete() {
        // given
        List<String> items = List.of("1", "2", "3");  // Group IDs
        List<Double> scores = List.of(100.0, 90.0, 80.0);
        
        // when
        searchCacheService.saveAutocomplete("group", "서울", items, scores);
        List<String> results = searchCacheService.getAutocomplete("group", "서울", 10);
        
        // then
        assertThat(results).hasSize(3);
        assertThat(results).containsExactly("1", "2", "3"); // 점수 순
    }
    
    @Test
    @DisplayName("캐시 TTL 검증")
    void cacheExpiration() throws InterruptedException {
        // given
        searchCacheService.saveAutocomplete("group", "test", List.of("1"), List.of(100.0));
        
        // when: 24시간 경과 시뮬레이션 (실제로는 짧게)
        String key = "autocomplete:group:test";
        redisTemplate.expire(key, 1, TimeUnit.SECONDS);
        Thread.sleep(1100);
        
        // then
        List<String> results = searchCacheService.getAutocomplete("group", "test", 10);
        assertThat(results).isEmpty();
    }
    
    @Test
    @DisplayName("인기 검색어 증가 - 동시성 테스트")
    void incrementSearchCount_Concurrency() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // when: 100개 스레드가 동시에 같은 검색어 카운트 증가
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    searchCacheService.incrementSearchCount("group", "서울대학교");
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        // then: 정확히 100이어야 함 (Race Condition 없음)
        Double score = redisTemplate.opsForZSet().score("trending:group", "서울대학교");
        assertThat(score).isEqualTo(100.0);
    }
}
```

**파일**: `GroupAutocompleteServiceTest.java`
```java
@SpringBootTest
@Transactional
class GroupAutocompleteServiceTest extends AbstractContainerTest {
    
    @Autowired
    private GroupAutocompleteService groupAutocompleteService;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        groupRepository.save(Group.create("서울대학교", GroupType.UNIVERSITY, "서울시 관악구"));
        groupRepository.save(Group.create("서울시립대학교", GroupType.UNIVERSITY, "서울시 동대문구"));
        groupRepository.save(Group.create("고려대학교", GroupType.UNIVERSITY, "서울시 성북구"));
        groupRepository.save(Group.create("삼성전자", GroupType.COMPANY, "경기도 수원시"));
    }
    
    @Test
    @DisplayName("자동완성 - 정확한 일치")
    void autocomplete_Exact() {
        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("서울대", 10);
        
        // then
        assertThat(response.items()).isNotEmpty();
        assertThat(response.items().get(0).name()).contains("서울대");
    }
    
    @Test
    @DisplayName("자동완성 - 초성 검색")
    void autocomplete_Chosung() {
        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("ㅅㅇㄷ", 10);
        
        // then
        assertThat(response.items()).hasSize(2);
        assertThat(response.items())
            .extracting("name")
            .contains("서울대학교", "서울시립대학교");
    }
    
    @Test
    @DisplayName("자동완성 - 빈 결과")
    void autocomplete_Empty() {
        // when
        GroupAutocompleteResponse response = groupAutocompleteService.autocomplete("존재하지않는학교", 10);
        
        // then
        assertThat(response.items()).isEmpty();
    }
}
```

---

##### 5.2 통합 테스트 (1일)

**파일**: `GroupControllerAutocompleteTest.java`
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupControllerAutocompleteTest extends AbstractContainerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("자동완성 API - 성공")
    void autocomplete_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/groups/autocomplete")
                .param("keyword", "서울")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.items").isArray());
    }
    
    @Test
    @DisplayName("자동완성 API - 파라미터 검증 실패")
    void autocomplete_InvalidParam() throws Exception {
        mockMvc.perform(get("/api/v1/groups/autocomplete")
                .param("keyword", "")
                .param("limit", "10"))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("인기 검색어 API - 성공")
    void getTrendingKeywords_Success() throws Exception {
        mockMvc.perform(get("/api/v1/groups/trending")
                .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("SUCCESS"))
            .andExpect(jsonPath("$.data.keywords").isArray());
    }
}
```

---

##### 5.3 성능 테스트 (2일)

**부하 테스트 (JMeter 또는 Gatling)**
```groovy
// build.gradle에 추가
dependencies {
    testImplementation 'io.gatling.highcharts:gatling-charts-highcharts:3.9.5'
}
```

**파일**: `SearchLoadTest.scala`
```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class SearchLoadTest extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
  
  val keywords = List("서울", "ㅅㅇ", "고려", "삼성", "ㄱㄹ")
  
  val scn = scenario("자동완성 부하 테스트")
    .repeat(100) {
      exec(
        http("자동완성 요청")
          .get("/api/v1/groups/autocomplete")
          .queryParam("keyword", keywords(scala.util.Random.nextInt(keywords.length)))
          .queryParam("limit", "10")
          .check(status.is(200))
          .check(responseTimeInMillis.lte(100)) // p95 < 100ms 검증
      )
    }
  
  setUp(
    scn.inject(
      rampUsers(1000).during(10.seconds) // 10초 동안 1000명 유저
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.percentile3.lt(100), // p95 < 100ms
     global.successfulRequests.percent.gt(99)  // 성공률 99% 이상
   )
}
```

**성능 벤치마크 체크리스트**:
- [ ] 자동완성 API p95 응답 시간 < 100ms
- [ ] 메인 검색 API p95 응답 시간 < 300ms
- [ ] 1,000명 동시 접속 시 에러율 < 1%
- [ ] Redis 메모리 사용량 < 50MB (5만 건 기준)
- [ ] 캐시 히트율 > 80%
- [ ] DB 쿼리 횟수 (캐시 미스 시에만 발생)

---

##### 5.4 API 문서화 (Spring Rest Docs) (0.5일)

**파일**: `GroupControllerRestDocsTest.java`
```java
@AutoConfigureRestDocs
class GroupControllerRestDocsTest extends AbstractRestDocsTest {
    
    @Test
    @DisplayName("그룹 자동완성 API 문서화")
    void autocomplete_Documentation() throws Exception {
        mockMvc.perform(get("/api/v1/groups/autocomplete")
                .param("keyword", "서울")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andDo(document("group-autocomplete",
                queryParameters(
                    parameterWithName("keyword").description("검색 키워드"),
                    parameterWithName("limit").description("결과 개수 (기본값: 10, 최대: 20)")
                ),
                responseFields(
                    fieldWithPath("result").description("결과 코드"),
                    fieldWithPath("data.items[]").description("자동완성 결과 목록"),
                    fieldWithPath("data.items[].name").description("그룹명"),
                    fieldWithPath("data.items[].type").description("그룹 타입")
                )
            ));
    }
}
```

---

## 5. 성능 고려사항

### 5.1 성능 목표
| 지표 | 목표 |
|------|------|
| 자동완성 API 응답 시간 (p95) | < 100ms |
| 메인 검색 API 응답 시간 (p95) | < 300ms |
| 캐시 히트율 | > 80% |
| 동시 사용자 | 1,000명 |

### 5.2 최적화 전략

#### 5.2.1 Redis 최적화
```java
// 1. Pipeline 사용 (여러 명령 일괄 처리)
redisTemplate.executePipelined(new SessionCallback<Object>() {
    @Override
    public Object execute(RedisOperations operations) {
        operations.opsForZSet().add(key1, value1, score1);
        operations.opsForZSet().add(key2, value2, score2);
        // ...
        return null;
    }
});

// 2. Connection Pool 설정
spring.data.redis.lettuce.pool.max-active=20
spring.data.redis.lettuce.pool.max-idle=10
spring.data.redis.lettuce.pool.min-idle=5
```

#### 5.2.2 DB 최적화
```sql
-- 인덱스 추가 (그룹 테이블)
CREATE INDEX idx_group_name ON member_group(name);
CREATE INDEX idx_group_type_name ON member_group(type, name);

-- 인덱스 추가 (가게 테이블)
CREATE INDEX idx_store_name ON store(name);
CREATE FULLTEXT INDEX idx_store_name_fulltext ON store(name);
```

#### 5.2.3 애플리케이션 최적화
```java
// 1. 로컬 캐시 (Caffeine) 추가 - 초고속 조회
@Cacheable(value = "groupAutocomplete", key = "#keyword")
public List<String> getAutocompleteLocal(String keyword) {
    // Redis 조회
}

// 2. 비동기 처리
@Async
public void incrementSearchCountAsync(String domain, String keyword) {
    searchCacheService.incrementSearchCount(domain, keyword);
}

// 3. Bulk 조회
List<Group> groups = groupRepository.findAllById(ids); // IN query
```

---

## 6. 마이그레이션 전략

### 6.1 단계적 배포

#### Step 1: Canary Deployment (1주차)
- 10% 트래픽만 새 검색 기능 사용
- 모니터링: 응답 시간, 에러율, 캐시 히트율

#### Step 2: 점진적 확대 (2주차)
- 50% → 100% 트래픽 확대
- 성능 이슈 발생 시 즉시 롤백

#### Step 3: 레거시 제거 (3주차)
- 기존 LIKE 검색 로직 완전 제거

### 6.2 Fallback 전략 (개선 버전)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupAutocompleteService {
    
    private final SearchCacheService searchCacheService;
    private final GroupRepository groupRepository;
    private final AlertService alertService; // 알림 서비스
    
    public GroupAutocompleteResponse autocomplete(String keyword, int limit) {
        try {
            // 1차: 캐시 조회
            return autocompleteWithCache(keyword, limit);
        } catch (RedisConnectionException e) {
            // Redis 연결 실패 → 즉시 알림 + DB fallback
            log.error("Redis 연결 실패, DB fallback 실행", e);
            alertService.sendAlert(AlertType.REDIS_CONNECTION_FAILURE, 
                                   "검색 캐시 서버 장애 발생");
            return autocompleteFallback(keyword, limit);
        } catch (RedisTimeoutException e) {
            // Redis 타임아웃 → 알림 + DB fallback
            log.error("Redis 타임아웃, DB fallback 실행", e);
            alertService.sendAlert(AlertType.REDIS_TIMEOUT, 
                                   "검색 캐시 응답 지연");
            return autocompleteFallback(keyword, limit);
        } catch (BusinessException e) {
            // 비즈니스 로직 오류는 그대로 throw
            throw e;
        } catch (Exception e) {
            // 예상치 못한 오류 → 알림 + DB fallback
            log.error("검색 처리 중 예상치 못한 오류, DB fallback 실행", e);
            alertService.sendAlert(AlertType.SEARCH_UNEXPECTED_ERROR, 
                                   "검색 기능 오류: " + e.getMessage());
            return autocompleteFallback(keyword, limit);
        }
    }
    
    /**
     * 캐시 기반 자동완성
     */
    private GroupAutocompleteResponse autocompleteWithCache(String keyword, int limit) {
        // 캐시 조회
        List<String> cachedIds = searchCacheService.getAutocomplete("group", keyword, limit);
        
        if (!cachedIds.isEmpty()) {
            // 캐시 히트
            List<Long> groupIds = cachedIds.stream()
                .map(Long::parseLong)
                .toList();
            
            List<Group> groups = groupRepository.findAllByIdIn(groupIds);
            return GroupAutocompleteResponse.from(groups);
        }
        
        // 캐시 미스 → DB 검색
        return searchFromDb(keyword, limit);
    }
    
    /**
     * DB fallback (Redis 장애 시)
     */
    private GroupAutocompleteResponse autocompleteFallback(String keyword, int limit) {
        log.warn("DB fallback 모드로 검색 수행: keyword={}", keyword);
        
        try {
            return searchFromDb(keyword, limit);
        } catch (Exception e) {
            log.error("DB fallback도 실패", e);
            // 최후의 수단: 빈 결과 반환
            return GroupAutocompleteResponse.empty();
        }
    }
    
    /**
     * DB 직접 검색
     */
    private GroupAutocompleteResponse searchFromDb(String keyword, int limit) {
        // 시작 일치 검색 (가장 효율적)
        List<Group> groups = groupRepository.findByNameStartsWith(keyword);
        
        if (groups.isEmpty()) {
            // 부분 일치 검색
            groups = groupRepository.findByNameContaining(keyword);
        }
        
        // 결과 제한
        List<Group> limitedGroups = groups.stream()
            .limit(limit)
            .toList();
        
        return GroupAutocompleteResponse.from(limitedGroups);
    }
}
```

**알림 서비스 인터페이스**:
```java
public interface AlertService {
    void sendAlert(AlertType type, String message);
}

public enum AlertType {
    REDIS_CONNECTION_FAILURE,
    REDIS_TIMEOUT,
    SEARCH_UNEXPECTED_ERROR
}
```

---

## 7. 모니터링 & 알림

### 7.1 모니터링 지표 (확장 버전)
```yaml
# Prometheus Metrics

# 1. API 응답 시간
- name: search_autocomplete_duration_seconds
  help: 자동완성 API 응답 시간
  type: histogram
  labels: [domain, status]  # domain=group/store, status=success/failure

# 2. 캐시 성능
- name: search_cache_hit_rate
  help: 캐시 히트율
  type: gauge
  labels: [domain]
  
- name: search_cache_hits_total
  help: 캐시 히트 총 횟수
  type: counter
  labels: [domain]
  
- name: search_cache_misses_total
  help: 캐시 미스 총 횟수
  type: counter
  labels: [domain]

# 3. Redis 상태
- name: redis_memory_usage_bytes
  help: Redis 메모리 사용량
  type: gauge
  
- name: redis_keys_total
  help: Redis 키 총 개수
  type: gauge
  labels: [key_pattern]  # autocomplete:*, trending:*, chosung_index:*

# 4. 에러 추적
- name: search_error_total
  help: 검색 오류 총 개수
  type: counter
  labels: [error_type]  # redis_timeout, redis_connection, db_error
  
- name: search_fallback_total
  help: Fallback 실행 총 횟수
  type: counter
  labels: [reason]  # redis_failure, cache_miss

# 5. 검색 품질
- name: search_result_count
  help: 검색 결과 개수
  type: histogram
  labels: [domain, strategy]  # strategy=exact/partial/chosung/fuzzy
  
- name: search_keyword_popularity
  help: 검색어별 조회수
  type: counter
  labels: [domain, keyword]

# 6. DB 성능
- name: db_query_duration_seconds
  help: DB 쿼리 실행 시간
  type: histogram
  labels: [query_type]  # findAll, findByNameStartsWith, etc.
  
- name: db_query_total
  help: DB 쿼리 총 실행 횟수
  type: counter
  labels: [query_type]
```

**Micrometer를 이용한 메트릭 수집 코드**:
```java
@Component
@RequiredArgsConstructor
public class SearchMetrics {
    
    private final MeterRegistry meterRegistry;
    
    /**
     * 캐시 히트 기록
     */
    public void recordCacheHit(String domain) {
        meterRegistry.counter("search_cache_hits_total", 
                              "domain", domain).increment();
    }
    
    /**
     * 캐시 미스 기록
     */
    public void recordCacheMiss(String domain) {
        meterRegistry.counter("search_cache_misses_total", 
                              "domain", domain).increment();
    }
    
    /**
     * API 응답 시간 기록
     */
    public void recordApiDuration(String domain, String status, Duration duration) {
        meterRegistry.timer("search_autocomplete_duration_seconds",
                            "domain", domain,
                            "status", status)
                     .record(duration);
    }
    
    /**
     * Fallback 실행 기록
     */
    public void recordFallback(String reason) {
        meterRegistry.counter("search_fallback_total", 
                              "reason", reason).increment();
    }
}
```

### 7.2 알림 설정
```yaml
# AlertManager Rules
- alert: SearchHighLatency
  expr: search_autocomplete_duration_seconds{quantile="0.95"} > 0.1
  annotations:
    summary: "검색 응답 시간 증가"
    
- alert: SearchCacheLowHitRate
  expr: search_cache_hit_rate < 0.8
  annotations:
    summary: "캐시 히트율 저하"
```

---

## 8. 예상 일정 (현실적 버전)

| Phase | 작업 | 세부 내용 | 기간 | 담당 |
|-------|------|-----------|------|------|
| **Phase 0** | **사전 조사** | 데이터 규모 확인, 성능 벤치마크 | 0.5일 | Backend |
| **Phase 1** | **기반 구조** | 한글 유틸리티, 초성 인덱스, 캐시 서비스 | 3일 | Backend |
| **Phase 2** | **그룹 검색 자동완성** | Repository 개선, API, Service, Admin 연동 | 4일 | Backend |
| **Phase 3** | **Recommendation 검색 개선** | 음식점 + 카테고리 + **음식 이름** 통합 검색 | 4일 | Backend |
| **Phase 4** | **캐시 워밍 & 스케줄러** | 배치 작업, 초성 인덱스 빌드 (음식 포함) | 2일 | Backend |
| **Phase 5** | **테스트 & 성능 검증** | 단위/통합 테스트, 부하 테스트, 문서화 | 5일 | Backend |
| **Buffer** | **예비 시간** | 예상치 못한 이슈 대응 | 3일 | Backend |
| **총계** | | | **21.5일 (~4.5주)** | |

### 상세 일정표

#### Week 1: 기반 구조 (5.5일)
- Day 1: 데이터 규모 확인 (0.5일) + 한글 유틸리티 구현 (1일)
- Day 2-3: 초성 인덱스 빌더 (0.5일) + 캐시 서비스 (1.5일)
- Day 4-5: Repository 개선 (0.5일) + 그룹 자동완성 API (1.5일)

#### Week 2: 자동완성 및 Recommendation 검색 (8일)
- Day 6-7: 그룹 자동완성 Service (2일)
- Day 8: Admin 캐시 연동 (0.5일) + Recommendation QueryDSL 개선 (0.5일)
- Day 9: Food JOIN 추가 및 테스트 (1일)
- Day 10-11: Recommendation 자동완성 Service (1.5일)
#### Week 3: 캐시 워밍 및 테스트 시작 (7일)
- Day 14-15: 캐시 워밍 & 스케줄러 완성 (2일)
- Day 16-17: 단위 테스트 (1.5일) + 통합 테스트 (1일)
- Day 18-19: 성능 테스트 (2일)
- Day 20: API 문서화 (0.5일)러 (2일)
- Day 13-14: 단위 테스트 (1.5일) + 통합 테스트 (1일)
#### Week 4: 성능 튜닝 및 배포 (4.5일 + Buffer 3일)
- Day 21: 성능 튜닝 (1일)
- Day 22: 최종 검증 (1일)
- Day 23: 배포 준비 (0.5일)
- Day 24-26: **예비 시간** (버퍼 3일)
- Day 19: 최종 검증 (1일)
- Day 20: 배포 준비 (1일)
- Day 21-23: **예비 시간** (버퍼)

### 마일스톤

- ✅ **M1 (Day 5)**: 기반 구조 완성, 한글 검색 가능
- ✅ **M2 (Day 8)**: 그룹 자동완성 API 완성
- ✅ **M3 (Day 13)**: Recommendation 검색 개선 완료 (음식점 + 카테고리 + 음식)
- ✅ **M4 (Day 15)**: 캐시 워밍 완료
- ✅ **M5 (Day 20)**: 테스트 및 문서화 완료
- ✅ **M6 (Day 23)**: 프로덕션 배포 준비 완료

---

## 9. 향후 확장 고려사항

### 9.1 Elasticsearch 마이그레이션 시점
- 데이터가 **10만 건 이상** 증가
- **복잡한 검색 요구사항** 추가 (범위 검색, 지리 검색 강화)
- **검색 품질**이 비즈니스 핵심일 때

### 9.2 추가 기능 아이디어
- 🔍 **개인화 검색**: 사용자 검색 이력 기반 추천
- 🎯 **검색어 자동 수정**: "셔울대" → "서울대" (Did you mean?)
- 📊 **검색 분석 대시보드**: 인기 검색어, 검색 트렌드 시각화
- 🔊 **음성 검색**: STT 연동

---

## 10. 참고 자료

### 10.1 기술 문서
- [Redis Sorted Sets Documentation](https://redis.io/docs/data-types/sorted-sets/)
- [Spring Data Redis Reference](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Levenshtein Distance Algorithm](https://en.wikipedia.org/wiki/Levenshtein_distance)

### 10.2 한글 검색 관련
- [한글 자모 분리 알고리즘](https://en.wikipedia.org/wiki/Hangul)
- [네이버 Nori 형태소 분석기](https://www.elastic.co/guide/en/elasticsearch/plugins/current/analysis-nori.html)

---

## 부록: API 명세서

### A.1 그룹 자동완성 API

#### 요청
```http
GET /api/v1/groups/autocomplete?keyword=서울&limit=10 HTTP/1.1
```

#### 응답
```json
{
  "result": "SUCCESS",
  "data": {
    "items": [
      {
        "name": "서울대학교",
        "type": "UNIVERSITY"
      },
      {
        "name": "서울시립대학교",
        "type": "UNIVERSITY"
      },
      {
        "name": "서울과학기술대학교",
        "type": "UNIVERSITY"
      }
    ]
  },
  "error": null
}
```

### A.2 인기 검색어 API

#### 요청
```http
GET /api/v1/groups/trending?limit=5 HTTP/1.1
```

#### 응답
```json
{
  "result": "SUCCESS",
  "data": {
    "keywords": [
      "서울대학교",
      "연세대학교",
      "고려대학교",
      "삼성전자",
      "네이버"
    ]
  },
  "error": null
}
```

---

## 결론

이 계획서는 **Redis + Application Layer 처리** 방식을 채택하되, 검토 과정에서 발견된 모든 잠재적 문제에 대한 구체적인 해결책을 제시합니다.

### 핵심 포인트
1. ✅ **검증된 아키텍처**: 
   - 메모리 효율성 99.8% 개선 (50만 키 → 1,100 키)
   - findAll() 제거하여 실시간 검색 성능 보장
   - 초성 역인덱스로 O(1) 검색 달성

2. ✅ **충분한 성능**: 
   - p95 < 100ms 목표 (성능 테스트로 검증)
   - 동시 사용자 1,000명 지원
   - 5만 건 데이터에서도 12MB 메모리만 사용

3. ✅ **실시간 캐시 일관성**: 
   - 그룹 생성/수정/삭제 시 즉시 캐시 업데이트
   - 24시간 기다리지 않고 최신 데이터 검색 가능

4. ✅ **강력한 Fallback**: 
   - Redis 장애 시 DB로 자동 전환
   - 구체적인 Exception 처리로 정확한 원인 파악
   - 알림 서비스 연동으로 즉시 대응 가능

5. ✅ **체계적인 테스트**: 
   - 단위/통합/성능 테스트 모두 포함
   - Gatling 부하 테스트로 p95 응답 시간 검증
   - 동시성 테스트로 Race Condition 방지 확인

6. ✅ **확장 가능**: 
   - 향후 Elasticsearch로 마이그레이션 용이
   - 명확한 마이그레이션 시점 정의 (10만 건 이상)

7. ✅ **한글 검색 최적화**: 
   - 초성 검색 (O(1) 성능)
   - 오타 허용 (편집 거리 기반, 성능 최적화)
   - 시작/부분 일치 검색

### 개선된 사항 요약

| 기존 우려사항 | 해결책 |
| findAll() 성능 문제 | 캐시 워밍 시에만 사용, 실시간은 초성 인덱스 활용 |
| Redis 키 폭발 (50만 개) | Prefix 1-2자 제한으로 1,100개로 감소 (99.8% 절감) |
| 캐시 일관성 (24시간 지연) | Admin API에서 실시간 캐시 업데이트 |
| 편집 거리 성능 (250만 연산) | 결과 5개 미만 + 짧은 키워드일 때만 실행, 범위 축소 |
| 동시성 제어 | Redis 원자성 보장 + 테스트 검증 |
| 모호한 Fallback | 구체적인 Exception 처리 + 알림 연동 |
| 테스트 부족 | 5일간 체계적 테스트 (단위/통합/성능) |
| 인덱스 미활용 | 시작 일치용 B-Tree 인덱스 추가 (Store + Food) |
| 음식 이름 검색 누락 | Food 테이블 LEFT JOIN 추가, DISTINCT로 중복 제거 |
| 일정 낙관적 (12일) | 현실적으로 21.5일 (버퍼 3일 포함) |
| 일정 낙관적 (12일) | 현실적으로 20.5일 (버퍼 3일 포함) |

### 비용 대비 효과
**개발 비용**: 약 4.5주 (1명 개발자 기준)
**운영 비용**: Redis 메모리 +65MB (음식 데이터 포함)
**기대 효과**:
- 사용자 경험 대폭 개선 (자동완성, 초성 검색, **음식명 검색**)
- DB 부하 80% 감소 (캐시 히트율 80% 가정)
- 검색 응답 시간 70% 단축 (300ms → 100ms)
- **검색 커버리지 확대**: 음식점 이름뿐 아니라 **메뉴명**으로도 검색 가능
- 검색 응답 시간 70% 단축 (300ms → 100ms)

### 리스크 관리

| 리스크 | 가능성 | 영향도 | 대응 방안 |
|--------|--------|--------|----------|
| Redis 메모리 부족 | 낮음 | 중간 | 메모리 모니터링 + TTL 단축 |
| 성능 목표 미달 | 낮음 | 높음 | Phase 0 벤치마크로 조기 검증 |
| 일정 지연 | 중간 | 중간 | 3일 버퍼 확보, 주간 진행 리뷰 |
| Elasticsearch 조기 필요 | 낮음 | 높음 | 마이그레이션 경로 사전 정의 |

### Next Steps

#### 즉시 실행 (1주 내)
1. ✅ 이 계획서 리뷰 및 승인
2. ✅ Phase 0: 현재 데이터 규모 측정
3. ✅ Phase 0: findAll() 성능 벤치마크
4. ✅ Redis 메모리 사용량 예측 검증

#### 구현 시작 (승인 후)
1. Phase 1부터 순차 구현 시작
2. 매주 금요일 진행 상황 리뷰
3. 각 Phase 완료 후 코드 리뷰 및 테스트
4. M1(Day 5) 마일스톤에서 기술적 타당성 재검증

#### 장기 계획
- 데이터 5만 건 도달 시: 성능 재측정
- 데이터 10만 건 예상 시: Elasticsearch 마이그레이션 검토
- 복잡한 검색 요구사항 발생 시: 기술 스택 재평가

---

**문서 버전**: v2.0 (문제점 개선 완료)  
**작성일**: 2025-11-09  
**최종 리뷰**: 모든 기술적 우려사항 해결 완료 ✅
