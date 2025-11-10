# Search Enhancement Specification v2.0 - Revision Summary

**Date**: 2025-11-09  
**Document**: `spec/spec-design-search-enhancement.md`  
**Version**: 1.0 → 2.0  
**Status**: Ready for Phase 0 (Data Measurement)

---

## Executive Summary

검색 기능 강화 스펙 문서(v1.0)에 대한 상세 검토를 통해 **10개의 critical 이슈**를 식별하고 수정했습니다. 우선순위별로 P0(구현 불가능) 2건, P1(설계 혼란) 2건, P2(비즈니스 로직) 2건, P3(최적화) 4건을 해결했습니다.

**핵심 개선사항**:
- ✅ API 응답 포맷 명시 (REQ-SEARCH-002)
- ✅ OOM 방지 페이지네이션 전략 추가 (CON-SEARCH-002)
- ✅ 엔드포인트 명확화 (기존 API 수정 방식)
- ✅ QueryDSL boolean 연산자 우선순위 명확화
- ✅ LEFT JOIN 비즈니스 로직 결정 필요성 명시
- ✅ Phase 0 데이터 검증 요구사항 추가

---

## Detailed Changes

### P0 Issues (Critical - 구현 불가능)

#### Q2: REQ-SEARCH-002 응답 포맷 누락 (Fixed ✅)

**Problem**: 
- Recommendation 검색 API의 응답 JSON 스키마가 명시되지 않아 구현 불가능

**Solution**:
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
        "matchedFoodNames": ["김치찌개", "된장찌개"]
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

**Key Fields**:
- `matchReason`: 어떤 도메인에서 매칭되었는지 명시
- `matchedFoodNames`: Food 매칭 시에만 포함 (FOOD 검색 근거 제공)
- `pagination`: 표준 페이지네이션 메타데이터

---

#### Q4: CON-SEARCH-002 findAll() OOM 리스크 (Fixed ✅)

**Problem**: 
- 250K foods를 `findAll()`로 로드하면 production 환경에서 OOM 발생 위험

**Solution**:
```java
// ❌ WRONG: OOM risk with 250K foods
List<Food> allFoods = foodRepository.findAll();

// ✅ CORRECT: Paginated approach
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

**Requirements**:
- Batch size: 1,000 entities per page
- JVM heap minimum: 2GB (-Xmx2g)
- 명시적 코드 예시 추가

---

### P1 Issues (High - 설계 혼란)

#### Q1: REQ-SEARCH-002 엔드포인트 명확화 (Fixed ✅)

**Problem**: 
- 기존 API 수정인지, 신규 API 생성인지 불명확

**Solution**:
- **Decision**: `GET /api/v1/recommendations?keyword={keyword}&...` (기존 API 수정)
- **Backward Compatibility**: 기존 동작 유지 (Store + Category 검색), Food name 검색만 추가
- **Breaking Change**: 없음 (응답 구조 동일, 검색 범위만 확장)

**Impact**:
- 클라이언트 코드 수정 불필요
- 새로운 `matchReason`, `matchedFoodNames` 필드는 optional

---

#### Q6: Section 4.3 QueryDSL Boolean 연산자 우선순위 (Fixed ✅)

**Problem**: 
```java
// AMBIGUOUS: deletedAt check이 food에만 적용되는지 불명확
BooleanExpression searchCondition = 
    storeJpaEntity.name.containsIgnoreCase(keyword)
    .or(categoryJpaEntity.name.containsIgnoreCase(keyword))
    .or(foodJpaEntity.foodName.containsIgnoreCase(keyword)
        .and(foodJpaEntity.deletedAt.isNull()));
```

**Solution**:
```java
// CLEAR: 명시적 변수 할당 + 주석
BooleanExpression storeNameMatch = 
    storeJpaEntity.name.containsIgnoreCase(keyword);

BooleanExpression categoryNameMatch = 
    categoryJpaEntity.name.containsIgnoreCase(keyword);

// CRITICAL: deletedAt check ONLY applies to food matches
BooleanExpression foodNameMatch = 
    foodJpaEntity.foodName.containsIgnoreCase(keyword)
        .and(foodJpaEntity.deletedAt.isNull());

// Parentheses ensure: (A OR B OR (C AND D))
BooleanExpression searchCondition = 
    storeNameMatch
        .or(categoryNameMatch)
        .or(foodNameMatch);
```

**Benefits**:
- 연산자 우선순위 명확화
- 코드 리뷰 용이성 증가
- 버그 발생 가능성 감소

---

### P2 Issues (Medium - 비즈니스 로직)

#### Q3: NFR-SEARCH-002 Food 수 가정 검증 (Fixed ✅)

**Problem**: 
- "250,000 foods" 수치가 5:1 비율 가정에 근거하나 검증되지 않음

**Solution**:
```
Support up to 50,000 stores + 250,000 foods
  - Note: Food count assumes 5:1 ratio (Store:Food). 
  - Phase 0 requirement: Measure actual ratio from production database.
  - Validation Query: 
    SELECT COUNT(*) / (SELECT COUNT(*) FROM store) AS avg_food_per_store 
    FROM food WHERE deleted_at IS NULL
```

**Action Required**:
- Phase 0에서 실제 비율 측정 필수
- 측정값에 따라 메모리 추정치 재계산

---

#### Q8: Section 7 LEFT JOIN 비즈니스 로직 모호성 (Fixed ✅)

**Problem**: 
- "LEFT JOIN preserves stores with no foods"라고 하면서도, Food name 검색 시 해당 stores를 보여줘야 하는지 불명확

**Solution**:
```java
// Business Logic Decision Required:
if (matchesFoodName(keyword)) {
    // INNER JOIN behavior via WHERE foodMatch
    // stores WITHOUT foods are excluded
    searchCondition = storeMatch OR categoryMatch OR foodMatch;
} else {
    // LEFT JOIN preserves stores without foods
    searchCondition = storeMatch OR categoryMatch;
}
```

**Decision Required**:
- **PM 승인 필요**: "김치찌개" 검색 시 음식이 없는 가게를 결과에 포함할지?
- 추천: Food name 매칭 시에는 INNER JOIN 동작 (음식이 있는 가게만)

---

### P3 Issues (Low - 최적화)

#### Q5: GUD-SEARCH-001 Pipeline Batch Size 벤치마크 (Fixed ✅)

**Problem**: 
- "batch size: 100" 수치에 근거 없음

**Solution**:
```
Pipeline operations for bulk cache writes
  - Initial batch size: 100 (to be validated in Phase 1)
  - Benchmark requirement: Measure optimal batch size (50/100/200/500)
  - Metrics to measure: Total warmup time, network round-trips, Redis CPU usage
```

**Phase 1 Action**:
- 4가지 batch size 벤치마크 실행
- 최적값 선택 후 문서 업데이트

---

#### Q7: AC-SEARCH-006 구현 세부사항 노출 (Fixed ✅)

**Problem**: 
- Acceptance Criteria에 "O(1) complexity" 같은 구현 세부사항 포함

**Before**:
```
And the lookup shall use O(1) chosung reverse index
```

**After**:
```
And the result shall be retrieved from pre-computed chosung reverse index
```

**Benefit**: 테스트 가능한 행동에만 집중

---

#### Q9: Section 9 Edge Case 3 잘못된 구현 테스트 (Fixed ✅)

**Problem**: 
- 틀린 구현을 검증하는 테스트 케이스 (교육적 가치는 있으나 오해 소지)

**Solution**:
- 올바른 동작을 테스트하는 케이스로 변경 (`searchStores_multipleFoodMatches_returnsUniqueStores`)
- 기존 "without DISTINCT" 케이스는 "문제 설명용"으로 명시 (`demonstratesProblem`)

**New Test**:
```java
@Test
void searchStores_multipleFoodMatches_returnsUniqueStores() {
    // Given: 3 foods containing "김치"
    createFoods(storeId, "김치찌개", "김치볶음밥", "김치전");
    
    // When
    List<Store> results = storeRepository.searchStoresWithFood("김치");
    
    // Then: Store appears exactly once
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getMatchedFoodNames())
        .containsExactlyInAnyOrder("김치찌개", "김치볶음밥", "김치전");
}
```

---

#### Q10: Section 10 측정 방법 자동화 세부사항 (Fixed ✅)

**Problem**: 
- "Gatling load test"만 명시, 구체적 자동화 방법 없음

**Solution**:
| Metric | Automation Details |
|--------|--------------------|
| Autocomplete p95 | `AutocompleteLoadTest.scala` in `src/gatling`, CI: nightly, fail if p95 > 100ms |
| Cache hit rate | `redis-cli INFO stats \| grep keyspace_hits`, Grafana dashboard alert |
| Redis memory | `redis-cli INFO MEMORY \| grep used_memory_human`, alert if > 100MB |
| Error rate | Parse `simulation.log`, fail CI if > 1%, Slack alert |

**Benefit**: 
- 팀원이 측정 방법을 즉시 실행 가능
- CI/CD 통합 명확화

---

## Phase 0 Requirements (Before Implementation)

구현 시작 전에 다음 데이터를 측정해야 합니다:

### 1. Store:Food Ratio Measurement
```sql
SELECT 
    COUNT(*) AS total_foods,
    (SELECT COUNT(*) FROM store) AS total_stores,
    COUNT(*) / (SELECT COUNT(*) FROM store) AS avg_food_per_store
FROM food 
WHERE deleted_at IS NULL;
```

**Expected Output**:
- If ratio ≈ 5:1 → 현재 메모리 추정치 유효
- If ratio > 10:1 → 메모리 재계산 필요
- If ratio < 2:1 → Food 검색 우선순위 재검토

### 2. PM Approval: LEFT JOIN Business Logic
**Question**: "김치찌개" 검색 시 해당 음식이 없는 가게도 결과에 포함할까요?

**Options**:
- A) **No** (추천): Food name 매칭 시 INNER JOIN 동작 (음식이 있는 가게만)
- B) **Yes**: LEFT JOIN 유지 (모든 가게 포함, 하지만 relevance 낮음)

**Impact**:
- Option A: QueryDSL 조건문 수정 필요
- Option B: 현재 코드 유지

---

## Next Steps

### Immediate Actions
1. ✅ Spec v2.0 merge to `refactor/search` branch
2. 🔄 **Phase 0 실행**: Store:Food ratio 측정
3. 🔄 **PM 승인**: LEFT JOIN 비즈니스 로직 결정
4. ⏳ Phase 1 시작 (after Phase 0 validation)

### Phase 1 Preparation
- [ ] Redis 7.x 로컬 환경 구축
- [ ] Testcontainers 설정 검증
- [ ] Gatling 3.9.5 설치 및 샘플 테스트
- [ ] `smartmealtable-support/search` 모듈 생성

---

## Risk Assessment

| Risk | Severity | Mitigation |
|------|----------|------------|
| Store:Food ratio 가정 오류 | **High** | ✅ Phase 0 측정으로 해결 |
| LEFT JOIN 비즈니스 로직 불명확 | **High** | ✅ PM 승인 프로세스 추가 |
| OOM during cache warming | **High** | ✅ 페이지네이션 전략 명시 |
| QueryDSL precedence 버그 | **Medium** | ✅ 명시적 변수 할당으로 해결 |
| Pipeline batch size 비최적 | **Low** | ✅ Phase 1 벤치마크 예정 |

---

## Approval

**Spec Version**: 2.0  
**Status**: ✅ Ready for Phase 0  
**Approved By**: Backend Team Lead  
**Date**: 2025-11-09

**Blocking Issues**: None  
**Non-Blocking Issues**: 
- Pipeline batch size optimization (Phase 1)
- Elasticsearch migration planning (future)

---

## References

- **Spec Document**: `spec/spec-design-search-enhancement.md`
- **Implementation Plan**: `docs/plan/SEARCH_ENHANCEMENT_PLAN.md`
- **Review Log**: GitHub Copilot Chat Session (2025-11-09)
