---
title: Search Enhancement System Specification
version: 2.0
date_created: 2025-11-09
last_updated: 2025-11-09
owner: Backend Team
tags: [search, autocomplete, redis, recommendation, design]
status: ready-for-phase-0
---

# Search Enhancement System Specification

## 1. Purpose & Scope

### Purpose
This specification defines the requirements, constraints, and interfaces for enhancing the search functionality in the SmartMealTable system. The enhancement includes:
1. Real-time autocomplete for Group search
2. Multi-domain search for Recommendation module (Store name + Category name + Food name)
3. Korean-specific search features (Chosung/initial consonant search, typo tolerance)
4. Search keyword recommendation based on popularity

### Scope
- **In Scope**:
  - Group (member_group) autocomplete and search
  - Recommendation multi-domain search (Store, Category, Food)
  - Redis-based caching for performance
  - Korean text processing utilities
  - Search analytics and trending keywords
  - Admin API cache synchronization

- **Out of Scope**:
  - Full-text search using Elasticsearch (future migration path defined)
  - Store API autocomplete (exists, no changes)
  - Voice search / STT integration
  - Machine learning-based personalization

### Intended Audience
- Backend developers implementing the search system
- QA engineers writing test cases
- DevOps engineers deploying Redis infrastructure
- Product managers validating requirements

## 2. Definitions

| Term | Definition |
|------|------------|
| **Chosung (초성)** | Initial consonant of Korean syllables (e.g., "ㄱㅇㄷ" for "고려대") |
| **Autocomplete** | Real-time search suggestion as user types (p95 < 100ms) |
| **Cache Warming** | Pre-loading data into Redis cache via scheduled batch job |
| **Chosung Reverse Index** | Redis Set mapping chosung patterns to entity IDs for O(1) lookup |
| **Prefix Limitation** | Restricting cache keys to 1-2 character prefixes to prevent key explosion |
| **Edit Distance** | Levenshtein distance algorithm for typo tolerance (max distance = 2) |
| **Domain** | Search context: "group" (member_group), "recommendation" (store+category+food) |
| **TTL** | Time To Live - Redis key expiration time (24 hours for search cache) |

## 3. Requirements, Constraints & Guidelines

### Functional Requirements

#### REQ-SEARCH-001: Group Autocomplete API
- **Description**: Provide real-time autocomplete for member_group search
- **Endpoint**: `GET /api/v1/groups/autocomplete?keyword={keyword}&limit={limit}`
- **Request Parameters**:
  - `keyword` (required, String, 1-50 chars): Search keyword
  - `limit` (optional, Integer, default=10, max=20): Number of results
- **Response Format**:
  ```json
  {
    "result": "SUCCESS",
    "data": {
      "groups": [
        {
          "groupId": 1,
          "name": "서울대학교",
          "type": "UNIVERSITY",
          "matchType": "EXACT|PARTIAL|CHOSUNG|FUZZY"
        }
      ]
    }
  }
  ```
- **Performance**: p95 latency < 100ms

#### REQ-SEARCH-002: Recommendation Multi-Domain Search
- **Description**: Search across Store name, Category name, and Food name
- **Endpoint**: `GET /api/v1/recommendations?keyword={keyword}&...` (기존 API 수정)
  - **Backward Compatibility**: 기존 동작 유지 (Store + Category 검색), Food name 검색 추가
  - **Breaking Change**: 없음 (응답 구조 동일, 검색 범위만 확장)
- **Search Targets**:
  1. `store.name` (음식점 이름)
  2. `category.name` (카테고리명: 한식, 중식, 분식 등)
  3. `food.food_name` (음식 이름: 김치찌개, 된장찌개 등)
- **Query Implementation**: LEFT JOIN food table, use DISTINCT to prevent duplicates
- **Filter**: Exclude soft-deleted foods (`food.deleted_at IS NULL`)
- **Response Format**:
  ```json
  {
    "result": "SUCCESS",
    "data": {
      "stores": [
        {
          "storeId": 123,
          "name": "맛있는식당",
          "categoryName": "한식",
          "distanceKm": 0.5,
          "rating": 4.5,
          "matchReason": "STORE_NAME|CATEGORY|FOOD",
          "matchedFoodNames": ["김치찌개", "된장찌개"]  // matchReason=FOOD일 때만
        }
      ],
      "pagination": {
        "currentPage": 1,
        "totalPages": 5,
        "totalCount": 100
      }
    }
  }
  ```
- **Performance**: p95 latency < 300ms

#### REQ-SEARCH-003: Recommendation Autocomplete API
- **Description**: Provide autocomplete with domain type labels
- **Endpoint**: `GET /api/v1/recommendations/autocomplete?keyword={keyword}&limit={limit}`
- **Response Format**:
  ```json
  {
    "result": "SUCCESS",
    "data": {
      "items": [
        {
          "id": 123,
          "name": "김치찌개",
          "type": "FOOD|STORE|CATEGORY",
          "popularity": 1500
        }
      ]
    }
  }
  ```

#### REQ-SEARCH-004: Korean Chosung Search
- **Description**: Support initial consonant search for Korean text
- **Examples**:
  - "ㄱㅇㄷ" → "고려대학교", "강원대학교"
  - "ㄱㅊㅉㄱ" → "김치찌개"
- **Implementation**: 
  - Extract chosung using Unicode ranges (0xAC00-0xD7A3)
  - Build chosung reverse index in Redis: `chosung_index:{domain}:{chosung}` → Set of IDs
- **Performance**: O(1) lookup using pre-computed index

#### REQ-SEARCH-005: Typo Tolerance
- **Description**: Allow edit distance ≤ 2 for short keywords
- **Constraints**:
  - Only applied when results < 5
  - Only for keywords length ≤ 10 characters
  - Use Levenshtein distance algorithm
- **Performance**: Pre-filter by 2-char prefix before edit distance calculation

#### REQ-SEARCH-006: Trending Keywords
- **Description**: Show popular search keywords
- **Endpoints**:
  - `GET /api/v1/groups/trending?limit={limit}`
  - `GET /api/v1/recommendations/trending?limit={limit}`
- **Data Source**: Redis Sorted Set ordered by search count

### Non-Functional Requirements

#### NFR-SEARCH-001: Performance
- Autocomplete API p95 latency < 100ms
- Main search API p95 latency < 300ms
- Support 1,000 concurrent users
- Cache hit rate > 80%

#### NFR-SEARCH-002: Scalability
- Support up to 50,000 groups
- Support up to 50,000 stores + 250,000 foods
  - **Note**: Food count assumes 5:1 ratio (Store:Food). **Phase 0 requirement**: Measure actual ratio from production database before implementation.
  - **Validation Query**: `SELECT COUNT(*) / (SELECT COUNT(*) FROM store) AS avg_food_per_store FROM food WHERE deleted_at IS NULL`
- Redis memory usage < 100MB total
- Horizontal scaling via Redis clustering (future)

#### NFR-SEARCH-003: Availability
- Fallback to DB on Redis failure (latency degrades to ~500ms)
- Graceful degradation: disable advanced features (chosung, typo tolerance) on high load
- Alert on Redis connection failure within 1 minute

#### NFR-SEARCH-004: Data Consistency
- Real-time cache updates on Admin API writes (create/update/delete)
- Cache TTL: 24 hours
- Daily cache warming at 3:00 AM KST
- Eventual consistency acceptable (max 1-hour lag on bulk changes)

### Security Requirements

#### SEC-SEARCH-001: Input Validation
- Sanitize keyword input to prevent injection attacks
- Maximum keyword length: 50 characters
- Reject special characters except Korean, alphanumeric, spaces
- Rate limiting: 100 requests/minute per user

### Constraints

#### CON-SEARCH-001: Redis Key Design
- Prefix limited to 1-2 characters to prevent key explosion
- Key pattern: `autocomplete:{domain}:{prefix}` (max 1,100 keys per domain)
- ID-based storage instead of full names to save memory

#### CON-SEARCH-002: Database Impact
- **Real-time search**: No findAll() calls allowed
- **Cache warming**: Use pagination to prevent OOM
  - **Batch size**: 1,000 entities per page
  - **JVM heap requirement**: Minimum 2GB (-Xmx2g)
  - **Implementation**:
    ```java
    // WRONG: OOM risk with 250K foods
    List<Food> allFoods = foodRepository.findAll(); // ❌
    
    // CORRECT: Paginated approach
    int pageSize = 1000;
    int pageNumber = 0;
    Page<Food> page;
    do {
        page = foodRepository.findAll(
            PageRequest.of(pageNumber++, pageSize)
        );
        warmupCache(page.getContent());
    } while (page.hasNext());
    ```
- Use B-Tree index for prefix search: `findByNameStartsWith()`
- Use IN query for bulk ID retrieval: `findAllByIdIn()`

#### CON-SEARCH-003: Technology Stack
- Redis 7.x (Sorted Set, Hash, Set, Pipeline)
- Spring Data Redis with Lettuce client
- QueryDSL for dynamic query building
- Testcontainers for integration tests

#### CON-SEARCH-004: Migration Path
- Migrate to Elasticsearch when:
  - Data exceeds 100,000 entities
  - Complex search requirements (geo-search, range filters)
  - Search quality becomes business-critical

### Guidelines

#### GUD-SEARCH-001: Cache Strategy
- Use cache-aside pattern
- Write-through for Admin API updates
- Pipeline operations for bulk cache writes
  - **Initial batch size**: 100 (to be validated in Phase 1)
  - **Benchmark requirement**: Measure optimal batch size (50/100/200/500) during implementation
  - **Metrics to measure**: Total warmup time, network round-trips, Redis CPU usage
- Atomic operations using Redis transactions

#### GUD-SEARCH-002: Error Handling
- Specific exception handling:
  - `RedisConnectionException` → DB fallback + alert
  - `RedisTimeoutException` → Retry once + DB fallback
  - `BusinessException` → Re-throw without fallback
- Log all fallback events with context

#### GUD-SEARCH-003: Testing Strategy
- Unit tests: 20+ test cases covering edge cases
- Integration tests: Testcontainers with Redis
- Performance tests: Gatling with 1,000 concurrent users
- Coverage requirement: 80% line coverage

#### GUD-SEARCH-004: Code Organization
- Korean utilities in `smartmealtable-support/search/korean/`
- Cache services in `smartmealtable-support/search/cache/`
- Domain-specific services in respective API modules
- Repository interfaces in domain layer

### Patterns

#### PAT-SEARCH-001: Repository Method Naming
- Prefix search: `findBy{Entity}NameStartsWith(String prefix)`
- Partial search: `findBy{Entity}NameContaining(String keyword)`
- Bulk retrieval: `findAllBy{Entity}IdIn(List<Long> ids)`

#### PAT-SEARCH-002: Redis Key Naming
- Autocomplete: `autocomplete:{domain}:{prefix}` (Sorted Set)
- Detail data: `{domain}:detail:{id}` (Hash)
- Chosung index: `chosung_index:{domain}:{chosung}` (Set)
- Trending: `trending:{domain}` (Sorted Set)

#### PAT-SEARCH-003: Service Method Signature
```java
public {Response}DTO autocomplete(String keyword, int limit) {
    // 1. Input validation
    // 2. Cache lookup
    // 3. DB fallback if cache miss
    // 4. Relevance scoring
    // 5. Result transformation
}
```

## 4. Interfaces & Data Contracts

### 4.1 REST API Interfaces

#### Group Autocomplete API
```http
GET /api/v1/groups/autocomplete HTTP/1.1
Host: api.smartmealtable.com
Authorization: Bearer {jwt_token}
Content-Type: application/json

Query Parameters:
- keyword: string (required, 1-50 chars)
- limit: integer (optional, default=10, max=20)
```

**Success Response (200 OK)**:
```json
{
  "result": "SUCCESS",
  "data": {
    "groups": [
      {
        "groupId": 1,
        "name": "서울대학교",
        "type": "UNIVERSITY",
        "matchType": "EXACT",
        "address": "서울특별시 관악구",
        "memberCount": 15000
      },
      {
        "groupId": 2,
        "name": "서울시립대학교",
        "type": "UNIVERSITY",
        "matchType": "PARTIAL",
        "address": "서울특별시 동대문구",
        "memberCount": 8000
      }
    ]
  },
  "error": null
}
```

**Error Response (400 Bad Request)**:
```json
{
  "result": "FAIL",
  "data": null,
  "error": {
    "code": "INVALID_KEYWORD",
    "message": "검색어는 1-50자 이내여야 합니다",
    "timestamp": "2025-11-09T10:30:00Z"
  }
}
```

#### Recommendation Autocomplete API
```http
GET /api/v1/recommendations/autocomplete HTTP/1.1
Host: api.smartmealtable.com
Authorization: Bearer {jwt_token}
Content-Type: application/json

Query Parameters:
- keyword: string (required, 1-50 chars)
- limit: integer (optional, default=10, max=20)
```

**Success Response (200 OK)**:
```json
{
  "result": "SUCCESS",
  "data": {
    "items": [
      {
        "id": 123,
        "name": "김치찌개",
        "type": "FOOD",
        "popularity": 1500,
        "storeName": "맛있는식당" // Only for type=FOOD
      },
      {
        "id": 456,
        "name": "맛있는식당",
        "type": "STORE",
        "popularity": 2000,
        "categoryName": "한식"
      },
      {
        "id": null,
        "name": "한식",
        "type": "CATEGORY",
        "popularity": 5000
      }
    ]
  },
  "error": null
}
```

### 4.2 Redis Data Structures

#### Autocomplete Sorted Set
```redis
# Key pattern: autocomplete:{domain}:{prefix}
# Type: Sorted Set
# Score: popularity (view count, search count)
# Members: entity IDs (strings)

ZADD autocomplete:group:ㅅ 1000 "1"
ZADD autocomplete:group:ㅅ 950 "2"
ZADD autocomplete:group:서 1000 "1"

# Query by prefix
ZREVRANGE autocomplete:group:서 0 9 WITHSCORES
```

#### Detail Data Hash
```redis
# Key pattern: {domain}:detail:{id}
# Type: Hash
# Fields: entity attributes

HSET group:detail:1 
  "name" "서울대학교" 
  "type" "UNIVERSITY" 
  "address" "서울특별시 관악구"
  "memberCount" "15000"

# Query by ID
HGETALL group:detail:1
```

#### Chosung Reverse Index Set
```redis
# Key pattern: chosung_index:{domain}:{chosung}
# Type: Set
# Members: entity IDs matching the chosung pattern

SADD chosung_index:group:ㅅㅇㄷㅎㄱ "1"  # 서울대학교
SADD chosung_index:group:ㅅㅅㄷㅎㄱ "2"  # 서울시립대학교

# Query by chosung
SMEMBERS chosung_index:group:ㅅㅇㄷㅎㄱ
```

#### Trending Keywords Sorted Set
```redis
# Key pattern: trending:{domain}
# Type: Sorted Set
# Score: search count
# Members: keyword strings

ZADD trending:group 1500 "서울대학교"
ZADD trending:group 1200 "연세대학교"
ZINCRBY trending:group 1 "고려대학교"

# Get top N
ZREVRANGE trending:group 0 9 WITHSCORES
```

### 4.3 Database Schema Changes

#### New Indexes
```sql
-- Group table
CREATE INDEX idx_group_name_prefix 
  ON member_group(name(10));

CREATE INDEX idx_group_type_name_prefix 
  ON member_group(type, name(10));

-- Food table (NEW)
CREATE INDEX idx_food_name_prefix 
  ON food(food_name(10));

CREATE INDEX idx_food_store_deleted 
  ON food(store_id, deleted_at);
```

#### QueryDSL Changes
```java
// StoreQueryDslRepository.java
public StoreSearchResult searchStoresWithFood(
    String keyword,
    BigDecimal userLatitude,
    BigDecimal userLongitude,
    // ... other parameters
) {
    // Step 1: Build individual conditions with explicit variables
    BooleanExpression storeNameMatch = 
        storeJpaEntity.name.containsIgnoreCase(keyword);
    
    BooleanExpression categoryNameMatch = 
        categoryJpaEntity.name.containsIgnoreCase(keyword);
    
    // CRITICAL: deletedAt check ONLY applies to food matches
    BooleanExpression foodNameMatch = 
        foodJpaEntity.foodName.containsIgnoreCase(keyword)
            .and(foodJpaEntity.deletedAt.isNull());
    
    // Step 2: Combine conditions with OR
    // Parentheses ensure: (A OR B OR (C AND D))
    BooleanExpression searchCondition = 
        storeNameMatch
            .or(categoryNameMatch)
            .or(foodNameMatch);
    
    // Use DISTINCT to prevent Store duplication
    List<Tuple> tuples = queryFactory
        .select(storeJpaEntity, distanceExpression)
        .distinct()  // CRITICAL!
        .from(storeJpaEntity)
        .leftJoin(storeCategoryJpaEntity).on(...)
        .leftJoin(categoryJpaEntity).on(...)
        .leftJoin(foodJpaEntity).on(  // NEW!
            storeJpaEntity.storeId.eq(foodJpaEntity.storeId)
        )
        .where(searchCondition)
        // ...
        .fetch();
}
```

### 4.4 Service Interfaces

#### KoreanSearchUtil Interface
```java
public class KoreanSearchUtil {
    /**
     * Extract chosung (initial consonants) from Korean text
     * @param text Korean text (e.g., "서울대학교")
     * @return Chosung string (e.g., "ㅅㅇㄷㅎㄱ")
     */
    public static String extractChosung(String text);
    
    /**
     * Check if keyword matches target's chosung
     * @param keyword User input (may be chosung)
     * @param target Entity name
     * @return true if matches
     */
    public static boolean matchesChosung(String keyword, String target);
    
    /**
     * Calculate Levenshtein edit distance
     * @param s1 First string
     * @param s2 Second string
     * @return Edit distance (0 = identical)
     */
    public static int calculateEditDistance(String s1, String s2);
}
```

#### SearchCacheService Interface
```java
@Service
public class SearchCacheService {
    /**
     * Get autocomplete suggestions from cache
     * @param domain "group" or "recommendation"
     * @param keyword User input
     * @param limit Max results
     * @return List of entity IDs (sorted by popularity)
     */
    public List<String> getAutocomplete(String domain, String keyword, int limit);
    
    /**
     * Add entity to autocomplete cache
     * @param domain "group" or "recommendation"
     * @param prefix 1-2 character prefix
     * @param entityId Entity ID
     * @param score Popularity score
     */
    public void addToAutocomplete(String domain, String prefix, 
                                   String entityId, double score);
    
    /**
     * Update entity in cache (real-time sync)
     * @param domain "group" or "recommendation"
     * @param entityId Entity ID
     * @param name Entity name
     * @param score Popularity score
     */
    public void updateEntityInCache(String domain, Long entityId, 
                                     String name, double score);
    
    /**
     * Remove entity from cache
     * @param domain "group" or "recommendation"
     * @param entityId Entity ID
     */
    public void removeEntityFromCache(String domain, Long entityId);
}
```

## 5. Acceptance Criteria

### AC-SEARCH-001: Group Autocomplete Functionality
**Given** a user types "서울" in the group search box  
**When** the autocomplete API is called  
**Then** the system shall return relevant groups starting with "서울" within 100ms (p95)

**And** the results shall include:
- Exact matches first (서울대학교, 서울시립대학교)
- Partial matches next (서울과학기술대학교)
- Chosung matches if applicable (ㅅㅇ → 서울대학교)

### AC-SEARCH-002: Recommendation Multi-Domain Search
**Given** a user searches for "김치" in recommendations  
**When** the search is executed  
**Then** the system shall return results from all three domains:
1. Stores with "김치" in name (김치찌개전문점)
2. Foods with "김치" in name (김치찌개, 김치볶음밥)
3. Categories containing "김치" (not applicable in this case)

**And** no duplicate stores shall appear in results due to multiple food matches

### AC-SEARCH-003: Cache Hit Performance
**Given** a keyword exists in Redis cache  
**When** autocomplete API is called 100 times  
**Then** at least 80 requests shall hit cache (cache hit rate ≥ 80%)

**And** p95 latency shall be < 100ms

### AC-SEARCH-004: Cache Miss Fallback
**Given** Redis is down or cache misses  
**When** autocomplete API is called  
**Then** the system shall fall back to DB query within 500ms

**And** an alert shall be triggered within 1 minute

### AC-SEARCH-005: Real-Time Cache Update
**Given** an admin creates a new group "카이스트"  
**When** the group creation API succeeds  
**Then** the autocomplete cache shall be updated immediately

**And** searching "ㅋㅇㅅㅌ" shall return "카이스트" within 1 second

### AC-SEARCH-006: Chosung Search Accuracy
**Given** a user types "ㄱㅊㅉㄱ"  
**When** autocomplete is triggered  
**Then** the system shall return "김치찌개" as the top result

**And** the result shall be retrieved from pre-computed chosung reverse index

### AC-SEARCH-007: Typo Tolerance
**Given** a user types "김치찌게" (typo: 게 instead of 개)  
**When** autocomplete finds fewer than 5 results  
**Then** the system shall apply edit distance calculation

**And** "김치찌개" (edit distance = 1) shall be included in results

### AC-SEARCH-008: Concurrent User Load
**Given** 1,000 concurrent users  
**When** all users perform autocomplete searches simultaneously  
**Then** error rate shall be < 1%

**And** p95 latency shall remain < 150ms

## 6. Test Automation Strategy

### Test Levels

#### Unit Tests
- **Framework**: JUnit 5 + AssertJ
- **Target**: Utility classes, service methods
- **Coverage**: 80% line coverage minimum
- **Examples**:
  - `KoreanSearchUtilTest`: Chosung extraction, edit distance
  - `SearchCacheServiceTest`: Redis operations (mocked)
  - Relevance scoring algorithms

#### Integration Tests
- **Framework**: Spring Boot Test + Testcontainers
- **Target**: End-to-end API flows with real Redis
- **Database**: Testcontainers MySQL + Redis
- **Examples**:
  - `GroupAutocompleteIntegrationTest`: Full autocomplete flow
  - `RecommendationSearchIntegrationTest`: Multi-domain search
  - Cache warming job execution

#### Performance Tests
- **Framework**: Gatling 3.9.5
- **Scenarios**:
  1. **Load Test**: 1,000 concurrent users, 5 minutes duration
  2. **Stress Test**: Ramp up to 2,000 users, measure breaking point
  3. **Soak Test**: 500 users, 30 minutes duration
- **Metrics**:
  - p50, p95, p99 latency
  - Throughput (requests/second)
  - Error rate
  - Redis memory usage

### Test Data Management
- Use fixtures for consistent test data
- Reset Redis between test classes
- Use `@Sql` scripts for database setup
- Avoid shared mutable state

### CI/CD Integration
- Run unit tests on every commit
- Run integration tests on PR creation
- Run performance tests nightly
- Block merge if coverage < 80%

## 7. Rationale & Context

### Why Redis Instead of Elasticsearch?

**Decision**: Use Redis + Application Layer for initial implementation

**Rationale**:
1. **Current Scale**: 1,000-5,000 groups, 1,000-3,000 stores (Redis sufficient)
2. **Existing Infrastructure**: Redis already deployed for caching
3. **Development Speed**: 4.5 weeks vs 8+ weeks for Elasticsearch
4. **Operational Simplicity**: No additional cluster management
5. **Cost**: Redis memory +65MB vs Elasticsearch cluster ~2GB RAM minimum

**Trade-offs Accepted**:
- Less sophisticated relevance scoring (manual algorithm vs ES BM25)
- No built-in Korean morphological analysis (custom chosung logic)
- Manual scaling logic (vs ES automatic sharding)

**Migration Path**: Defined threshold at 100,000 entities or complex search requirements

### Why Prefix Limitation (1-2 Characters)?

**Problem**: Naive approach generates 500,000+ Redis keys (50,000 entities × average 5-char names × 2 for chosung)

**Solution**: Limit prefixes to 1-2 characters

**Rationale**:
- 99.8% key reduction: 500,000 → 1,100 keys
- Autocomplete typically triggers after 1-2 characters anyway
- Longer keywords use chosung reverse index + IN queries
- Memory savings: ~500MB → ~12MB per domain

### Why LEFT JOIN for Food Table?

**Problem**: Current implementation only searches Store + Category, missing Food names

**Solution**: Add LEFT JOIN to food table with DISTINCT

**Business Logic Decision**:
- **When user searches by food name** (e.g., "김치찌개"):
  - Show stores that have matching foods
  - Do NOT show stores without any foods (use INNER JOIN behavior via WHERE clause)
- **When user searches by store/category name** (e.g., "맛있는"):
  - Show all matching stores (including those without foods)
  - LEFT JOIN allows this flexibility

**Conditional JOIN Strategy**:
```java
// Pseudocode
if (matchesFoodName(keyword)) {
    // INNER JOIN behavior via WHERE foodMatch
    searchCondition = storeMatch OR categoryMatch OR foodMatch;
} else {
    // LEFT JOIN preserves stores without foods
    searchCondition = storeMatch OR categoryMatch;
}
```

**Rationale**:
- 1:N relationship (Store → Foods) causes Store duplication without DISTINCT
- LEFT JOIN preserves stores with no foods for store/category searches
- `food.deleted_at IS NULL` filter is essential for data quality
- **PM approval required**: Confirm stores without foods should NOT appear in food-name-only searches

**Performance Impact**: 
- Index on `food.food_name` + `food.store_id` mitigates JOIN cost
- DISTINCT adds ~10-20ms overhead (acceptable for p95 < 300ms target)

### Why Real-Time Cache Updates in Admin API?

**Problem**: Daily cache warming causes 24-hour lag for new entities

**Solution**: Immediate cache update on create/update/delete operations

**Rationale**:
- User expectation: New group searchable immediately
- Minimal performance impact: Single Redis write (~1ms)
- Eventual consistency acceptable for bulk changes

**Implementation**: Try-catch around cache update to avoid blocking business transaction

## 8. Dependencies & External Integrations

### External Systems

**EXT-001**: Redis 7.x Cluster
- **Purpose**: Primary caching layer for search data
- **Required Capabilities**: Sorted Sets, Hash, Set, Transactions, Pipeline
- **SLA Requirements**: 99.9% uptime, < 5ms p95 latency
- **Failover**: Sentinel configuration with automatic failover

### Third-Party Services

**SVC-001**: None (fully self-contained)

### Infrastructure Dependencies

**INF-001**: Docker Compose for Local Development
- **Requirements**: Redis container, MySQL container
- **Configuration**: `docker-compose.local.yml`

**INF-002**: Kubernetes (Production)
- **Requirements**: Redis StatefulSet, ConfigMaps for cache keys
- **Monitoring**: Prometheus metrics, Grafana dashboards

### Data Dependencies

**DAT-001**: MySQL Database
- **Tables**: member_group, store, category, food, store_category
- **Access Pattern**: Read-heavy (95% reads, 5% writes)
- **Backup**: Daily snapshots, point-in-time recovery

### Technology Platform Dependencies

**PLT-001**: Java 21
- **Rationale**: Project standard, LTS support until 2029
- **Features Used**: Records, Pattern matching, Virtual threads (future)

**PLT-002**: Spring Boot 3.x
- **Modules**: Spring Data Redis, Spring Data JPA, Spring Scheduler
- **Rationale**: Framework standard across project

**PLT-003**: QueryDSL 5.x
- **Purpose**: Type-safe dynamic query building
- **Rationale**: Complex search criteria with multiple optional filters

### Compliance Dependencies

**COM-001**: GDPR - Personal Data Processing
- **Impact**: Search keywords may contain user names
- **Requirement**: Anonymize search logs after 90 days
- **Implementation**: Batch job to purge old trending data

## 9. Examples & Edge Cases

### Example 1: Successful Group Autocomplete
```java
@Test
void autocomplete_exactMatch_returnsTopResult() {
    // Given
    String keyword = "서울대";
    int limit = 10;
    
    // Setup cache
    searchCacheService.addToAutocomplete("group", "서", "1", 1000.0);
    searchCacheService.addToAutocomplete("group", "서울", "1", 1000.0);
    
    // When
    GroupAutocompleteResponse response = 
        groupAutocompleteService.autocomplete(keyword, limit);
    
    // Then
    assertThat(response.getGroups()).isNotEmpty();
    assertThat(response.getGroups().get(0).getName())
        .isEqualTo("서울대학교");
    assertThat(response.getGroups().get(0).getMatchType())
        .isEqualTo(MatchType.EXACT);
}
```

### Example 2: Chosung Search
```java
@Test
void autocomplete_chosungMatch_returnsResults() {
    // Given: User types initial consonants
    String keyword = "ㄱㅊㅉㄱ";
    
    // Setup chosung index
    String chosung = KoreanSearchUtil.extractChosung("김치찌개");
    chosungIndexBuilder.addToIndex("recommendation", chosung, "food:123");
    
    // When
    RecommendationAutocompleteResponse response = 
        recommendationAutocompleteService.autocomplete(keyword, 10);
    
    // Then
    assertThat(response.getItems())
        .extracting("name")
        .contains("김치찌개");
}
```

### Example 3: Multi-Domain Search (Food Join)
```sql
-- Expected query with DISTINCT
SELECT DISTINCT s.store_id, s.name, distance_in_km
FROM store s
LEFT JOIN store_category sc ON s.store_id = sc.store_id
LEFT JOIN category c ON sc.category_id = c.category_id
LEFT JOIN food f ON s.store_id = f.store_id 
                 AND f.deleted_at IS NULL
WHERE s.deleted_at IS NULL
  AND (
    LOWER(s.name) LIKE LOWER('%김치%')
    OR LOWER(c.name) LIKE LOWER('%김치%')
    OR LOWER(f.food_name) LIKE LOWER('%김치%')
  )
ORDER BY distance_in_km ASC
LIMIT 20;
```

### Edge Case 1: Empty Keyword
```java
@Test
void autocomplete_emptyKeyword_returnsEmptyList() {
    // Given
    String keyword = "";
    
    // When
    GroupAutocompleteResponse response = 
        groupAutocompleteService.autocomplete(keyword, 10);
    
    // Then
    assertThat(response.getGroups()).isEmpty();
}
```

### Edge Case 2: Redis Connection Failure
```java
@Test
void autocomplete_redisDown_fallbacksToDb() {
    // Given
    String keyword = "서울";
    mockRedisFailure(); // Simulate Redis down
    
    // When
    GroupAutocompleteResponse response = 
        groupAutocompleteService.autocomplete(keyword, 10);
    
    // Then
    assertThat(response.getGroups()).isNotEmpty(); // DB fallback works
    verify(alertService).sendAlert(
        AlertType.REDIS_CONNECTION_FAILURE, anyString()
    );
}
```

### Edge Case 3: Food Table Duplication Prevention
```java
@Test
void searchStores_multipleFoodMatches_returnsUniqueStores() {
    // Given: Store "맛있는식당" has 3 foods containing "김치"
    Long storeId = 1L;
    createFoods(storeId, "김치찌개", "김치볶음밥", "김치전");
    
    // When: Search with DISTINCT (correct implementation)
    List<Store> results = storeRepository.searchStoresWithFood("김치");
    
    // Then: Store appears exactly once despite multiple food matches
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getStoreId()).isEqualTo(1L);
    assertThat(results.get(0).getMatchedFoodNames())
        .containsExactlyInAnyOrder("김치찌개", "김치볶음밥", "김치전");
}

@Test
void searchStores_withoutDistinct_demonstratesProblem() {
    // This test demonstrates WHY DISTINCT is required
    // For documentation and educational purposes only
    
    // Given: Store "맛있는식당" has 3 foods containing "김치"
    Long storeId = 1L;
    createFoods(storeId, "김치찌개", "김치볶음밥", "김치전");
    
    // When: Query without DISTINCT (intentionally wrong)
    List<Tuple> rawResults = queryFactory
        .select(storeJpaEntity, foodJpaEntity)
        .from(storeJpaEntity)
        .leftJoin(foodJpaEntity).on(storeJpaEntity.storeId.eq(foodJpaEntity.storeId))
        .where(foodJpaEntity.foodName.contains("김치"))
        .fetch();
    
    // Then: Same store appears 3 times in raw results (one per food)
    assertThat(rawResults).hasSize(3);
    assertThat(rawResults)
        .extracting(tuple -> tuple.get(storeJpaEntity).getStoreId())
        .containsOnly(1L, 1L, 1L);
    
    // Conclusion: This is why DISTINCT is MANDATORY in the actual query
}
```

### Edge Case 4: Very Long Keyword (50+ characters)
```java
@Test
void autocomplete_longKeyword_returnsBadRequest() {
    // Given
    String keyword = "A".repeat(51); // 51 characters
    
    // When & Then
    assertThatThrownBy(() -> 
        groupAutocompleteService.autocomplete(keyword, 10)
    )
    .isInstanceOf(InvalidKeywordException.class)
    .hasMessageContaining("1-50자 이내");
}
```

## 10. Validation Criteria

### Performance Validation

| Metric | Target | Validation Method | Automation Details |
|--------|--------|-------------------|--------------------|
| Autocomplete p95 latency | < 100ms | Gatling load test with 1,000 users | `AutocompleteLoadTest.scala` in `src/gatling`, CI: nightly run, fail build if p95 > 100ms |
| Search p95 latency | < 300ms | Gatling load test with 1,000 users | `SearchLoadTest.scala`, report saved to `build/gatling/`, parse JSON results in CI |
| Cache hit rate | > 80% | Monitor Redis stats over 24 hours | `redis-cli INFO stats \| grep keyspace_hits`, calculate hit_rate = hits/(hits+misses), Grafana dashboard alert |
| Redis memory usage | < 100MB | Monitor `INFO MEMORY` after cache warming | `redis-cli INFO MEMORY \| grep used_memory_human`, automated in cache warming job, alert if > 100MB |
| Error rate (1,000 concurrent) | < 1% | Gatling stress test report | Parse `simulation.log`, fail CI if error_rate > 1%, alert to Slack |

### Functional Validation

- [ ] Group autocomplete returns results within 100ms (p95)
- [ ] Recommendation search includes Store + Category + Food
- [ ] Chosung search "ㄱㅊㅉㄱ" finds "김치찌개"
- [ ] Typo tolerance finds "김치찌개" for "김치찌게"
- [ ] DISTINCT prevents Store duplication in multi-food results
- [ ] Real-time cache update on Admin API create/update/delete
- [ ] Fallback to DB on Redis failure with alert
- [ ] Trending keywords API returns top 10 by search count

### Security Validation

- [ ] SQL injection attempts rejected with 400 Bad Request
- [ ] XSS payloads in keywords sanitized
- [ ] Rate limiting enforces 100 requests/minute per user
- [ ] Invalid authentication returns 401 Unauthorized

### Scalability Validation

- [ ] System handles 50,000 groups without performance degradation
- [ ] System handles 50,000 stores + 250,000 foods
- [ ] Cache warming completes within 5 minutes
- [ ] Redis pipeline batch operations reduce network calls by 90%

## 11. Related Specifications / Further Reading

### Internal Documents
- [Search Enhancement Plan](../docs/plan/SEARCH_ENHANCEMENT_PLAN.md) - High-level implementation plan
- [API Specification](../docs/API_SPECIFICATION.md) - Complete REST API documentation
- [Database Schema](../ddl.sql) - Current database structure

### External References
- [Redis Sorted Sets Documentation](https://redis.io/docs/data-types/sorted-sets/)
- [Levenshtein Distance Algorithm](https://en.wikipedia.org/wiki/Levenshtein_distance)
- [Korean Unicode Ranges](https://en.wikipedia.org/wiki/Korean_language_and_computers#Hangul_in_Unicode)
- [Spring Data Redis Reference](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [QueryDSL Documentation](http://querydsl.com/static/querydsl/latest/reference/html/)

---

## Revision History

| Version | Date | Changes | Reviewer |
|---------|------|---------|----------|
| 1.0 | 2025-11-09 | Initial specification | Backend Team |
| 2.0 | 2025-11-09 | Critical fixes: Added response format (Q2), pagination strategy (Q4), endpoint clarification (Q1), QueryDSL precedence fix (Q6), LEFT JOIN business logic (Q8), Phase 0 validation (Q3), benchmark requirements (Q5), AC simplification (Q7), edge case correction (Q9), automation details (Q10) | Backend Team Lead |

---

**Document Status**: ✅ Ready for Phase 0 (Data Measurement)  
**Review Date**: 2025-11-09  
**Next Steps**: 
1. Phase 0: Measure Store:Food ratio from production DB
2. PM approval: LEFT JOIN business logic for food-name-only searches
3. Proceed to implementation after Phase 0 validation

**Approved By**: Backend Team Lead
