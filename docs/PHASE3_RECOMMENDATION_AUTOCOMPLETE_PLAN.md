# Phase 3: Recommendation 자동완성 구현 계획

**작성일**: 2025-11-10
**상태**: 🚀 **시작**

---

## 🎯 목표

Phase 2에서 구현한 Group 자동완성 패턴을 Recommendation 모듈에 확장하여:
1. **Store + Food 통합 검색** 지원
2. **도메인 타입별 라벨링** (Store/Food 구분)
3. **재사용 가능한 자동완성 인프라** 활용

---

## 📋 작업 범위

### ✅ 이미 완료된 작업 (Phase 1 & 2)
- [x] KoreanSearchUtil (초성 추출, 편집 거리)
- [x] ChosungIndexBuilder (Prefix 매칭)
- [x] SearchCacheService (캐시 인프라)
- [x] Group 자동완성 API (템플릿 패턴)

### 🚀 Phase 3 작업 목록

#### 3.1 Domain Layer
- [ ] StoreRepository에 자동완성용 메서드 추가
  - `findByNameStartsWith(String prefix, int limit)`
  - `findAllByIdIn(List<Long> storeIds)`
  - `count()` (캐시 워밍용)
  - `findAll(int page, int size)` (배치 로딩용)

- [ ] FoodRepository에 자동완성용 메서드 추가
  - `findByNameStartsWith(String prefix, int limit)`
  - `findAllByIdIn(List<Long> foodIds)`
  - `count()` (캐시 워밍용)
  - `findAll(int page, int size)` (배치 로딩용)

#### 3.2 Storage Layer (QueryDSL)
- [ ] StoreQueryDslRepository 확장
  - `findByNameStartingWith(String prefix, int limit)` 구현
  - `findByStoreIdIn(List<Long> storeIds)` 구현
  - Food LEFT JOIN 추가 (대표 메뉴 정보 포함)

- [ ] FoodQueryDslRepository 확장
  - `findByNameStartingWith(String prefix, int limit)` 구현
  - `findByFoodIdIn(List<Long> foodIds)` 구현
  - Store LEFT JOIN 추가 (가게 정보 포함)

#### 3.3 API Layer
- [ ] StoreAutocompleteService 구현
  - 3단계 검색 전략 (Group 패턴 재사용)
  - Stage 1: Prefix 캐시 검색
  - Stage 2: 초성 역인덱스 검색
  - Stage 3: DB Fallback (편집 거리)

- [ ] FoodAutocompleteService 구현
  - 동일한 3단계 검색 전략
  - Food → Store 조인 정보 포함

- [ ] DTO 생성
  - `StoreAutocompleteResponse`
  - `FoodAutocompleteResponse`
  - `StoreSuggestion` (도메인 타입: STORE)
  - `FoodSuggestion` (도메인 타입: FOOD)

#### 3.4 Controller
- [ ] StoreController 확장
  - `GET /api/v1/stores/autocomplete`
  - `GET /api/v1/stores/trending` (인기 검색어)

- [ ] FoodController 확장
  - `GET /api/v1/foods/autocomplete`
  - `GET /api/v1/foods/trending` (인기 검색어)

---

## 🏗️ 아키텍처 설계

### 도메인 타입 열거형
```java
public enum SearchDomain {
    GROUP("group"),
    STORE("store"),
    FOOD("food");
    
    private final String domain;
    
    // ...
}
```

### 자동완성 응답 구조
```json
{
  "suggestions": [
    {
      "id": 123,
      "name": "서울 맛집",
      "type": "STORE",
      "category": "한식",
      "address": "서울시 강남구",
      "popularity": 150.0,
      "metadata": {
        "distance": "1.2km",
        "isOpen": true
      }
    },
    {
      "id": 456,
      "name": "김치찌개",
      "type": "FOOD",
      "storeId": 123,
      "storeName": "서울 맛집",
      "category": "한식",
      "popularity": 120.0,
      "metadata": {
        "averagePrice": 12000
      }
    }
  ],
  "count": 2
}
```

---

## 📊 데이터베이스 인덱스

### Store 테이블
```sql
-- 자동완성용 Prefix 인덱스
CREATE INDEX idx_store_name_prefix ON store(name(10));

-- 삭제되지 않은 가게 필터링
CREATE INDEX idx_store_deleted_at ON store(deleted_at);
```

### Food 테이블
```sql
-- 자동완성용 Prefix 인덱스
CREATE INDEX idx_food_name_prefix ON food(food_name(10));

-- 삭제되지 않은 음식 필터링
CREATE INDEX idx_food_deleted_at ON food(deleted_at);

-- Store 조인용
CREATE INDEX idx_food_store_id ON food(store_id);
```

---

## 🔄 3단계 검색 전략

### Stage 1: Prefix Cache
```
키워드: "서울"
→ Redis Key: "autocomplete:store:서", "autocomplete:store:서울"
→ Sorted Set 조회 (popularity 기준 정렬)
→ 10개 이상 결과 → 반환
→ 10개 미만 → Stage 2로 이동
```

### Stage 2: 초성 역인덱스
```
키워드: "ㅅㅇ"
→ Redis Key: "chosung:store:ㅅㅇ"
→ Set에서 ID 목록 조회
→ DB에서 실제 데이터 조회
→ 10개 이상 결과 → 반환
→ 10개 미만 → Stage 3로 이동
```

### Stage 3: DB Fallback (편집 거리)
```
키워드: "셔울"
→ DB에서 LIKE '%셔울%' 검색
→ 편집 거리 계산 (Levenshtein Distance ≤ 2)
→ 결과 반환
```

---

## 🧪 테스트 계획

### 단위 테스트
- [x] KoreanSearchUtil (37개 - 완료)
- [ ] StoreAutocompleteService (12개)
- [ ] FoodAutocompleteService (12개)

### 통합 테스트
- [ ] StoreAutocompleteServiceIntegrationTest (Testcontainers)
  - 캐시 히트/미스
  - 초성 검색
  - DB Fallback
  - 인기 검색어
  - Limit 파라미터
  
- [ ] FoodAutocompleteServiceIntegrationTest
  - 동일한 시나리오

### 성능 테스트 (Phase 4)
- [ ] Gatling 부하 테스트
- [ ] p95 레이턴시 < 100ms 검증
- [ ] 캐시 히트율 > 80% 목표

---

## 📝 구현 순서

### Step 1: Store Repository 확장 (Domain + Storage)
1. StoreRepository 인터페이스에 메서드 추가
2. StoreQueryDslRepository에 구현
3. StoreRepositoryImpl에 연결

### Step 2: Food Repository 확장 (Domain + Storage)
1. FoodRepository 인터페이스에 메서드 추가
2. FoodQueryDslRepository 생성 및 구현
3. FoodRepositoryImpl에 연결

### Step 3: Store 자동완성 Service
1. StoreAutocompleteService 구현
2. DTO 생성 (StoreAutocompleteResponse, StoreSuggestion)
3. SearchCacheService 재사용

### Step 4: Food 자동완성 Service
1. FoodAutocompleteService 구현
2. DTO 생성 (FoodAutocompleteResponse, FoodSuggestion)
3. SearchCacheService 재사용

### Step 5: Controller 확장
1. StoreController에 /autocomplete, /trending 추가
2. FoodController에 /autocomplete, /trending 추가

### Step 6: 통합 테스트
1. StoreAutocompleteServiceIntegrationTest
2. FoodAutocompleteServiceIntegrationTest
3. 12개 시나리오 각각 구현 및 검증

---

## ⚠️ 주의사항

### 1. N+1 쿼리 방지
- Store → Food JOIN (대표 메뉴)
- Food → Store JOIN (가게 정보)
- QueryDSL에서 명시적 LEFT JOIN 사용

### 2. 중복 제거
- Food 검색 시 같은 Store에 여러 Food가 있을 수 있음
- `DISTINCT` 또는 Java Stream `distinct()` 사용

### 3. 캐시 키 전략
```
autocomplete:{domain}:{prefix}
chosung:{domain}:{chosung}
trending:{domain}
search-count:{domain}:{keyword}
```

### 4. 도메인별 분리
- Group, Store, Food는 독립적인 캐시 도메인
- 각각의 인기 검색어 관리
- 각각의 캐시 워밍 필요

---

## 🎯 성공 기준

- [ ] Store 자동완성 12개 테스트 통과
- [ ] Food 자동완성 12개 테스트 통과
- [ ] 캐시 히트 시 응답 시간 < 50ms
- [ ] DB Fallback 시 응답 시간 < 200ms
- [ ] 한글/초성/오타 모두 정확히 처리
- [ ] 도메인 타입별 라벨링 정확

---

## 📚 참고 문서

- `PHASE2_INTEGRATION_TESTS_COMPLETE.md` - Phase 2 완료 보고서
- `SEARCH_ENHANCEMENT_IMPLEMENTATION_GUIDE.md` - 전체 구현 가이드
- `docs/API_SPECIFICATION.md` - API 스펙 (업데이트 필요)

---

**다음 작업**: Step 1 - Store Repository 확장부터 시작
