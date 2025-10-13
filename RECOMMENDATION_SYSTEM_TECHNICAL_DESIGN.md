# 추천 시스템 기술 설계 문서 (Technical Design Document)

**작성일**: 2025-10-13  
**버전**: 1.0  
**담당**: SmartMealTable Backend Team

---

## 📋 목차

1. [개요](#1-개요)
2. [아키텍처 설계](#2-아키텍처-설계)
3. [핵심 컴포넌트 설계](#3-핵심-컴포넌트-설계)
4. [알고리즘 구현 전략](#4-알고리즘-구현-전략)
5. [성능 최적화 전략](#5-성능-최적화-전략)
6. [데이터 흐름](#6-데이터-흐름)
7. [테스트 전략](#7-테스트-전략)
8. [API 설계](#8-api-설계)

---

## 1. 개요

### 1.1 목적
사용자의 **목표 식비**, **활동 지역**, **음식 선호도**를 기반으로 개인화된 음식점 추천을 제공하는 시스템 개발

### 1.2 핵심 요구사항
- **3가지 사용자 유형**: 절약형, 모험형, 균형형
- **4가지 속성 점수**: 안정성, 탐험성, 예산 효율성, 접근성
- **다양한 필터 옵션**: 위치, 영업시간, 불호 음식, 가게 타입
- **정렬 옵션**: 추천 점수, 거리, 리뷰, 가격, 즐겨찾기, 관심도

### 1.3 기술 스택
- **Backend**: Spring Boot 3.x, Java 21
- **Database**: MySQL (Primary), Redis (Caching)
- **ORM**: Spring Data JPA, QueryDSL
- **Testing**: JUnit5, Mockito, TestContainers

---

## 2. 아키텍처 설계

### 2.1 멀티모듈 구조

```
recommendation/                      # 추천 시스템 전용 모듈
├── domain/
│   ├── model/
│   │   ├── RecommendationRequest.java
│   │   ├── RecommendationResult.java
│   │   ├── ScoreDetail.java
│   │   └── UserProfile.java
│   ├── service/
│   │   ├── RecommendationDomainService.java
│   │   ├── ScoreCalculator.java
│   │   ├── StabilityScoreCalculator.java
│   │   ├── ExplorationScoreCalculator.java
│   │   ├── BudgetEfficiencyScoreCalculator.java
│   │   └── AccessibilityScoreCalculator.java
│   └── repository/
│       └── RecommendationDataRepository.java (interface)
├── storage/
│   ├── entity/
│   │   └── (기존 Store, Food, Category 활용)
│   └── repository/
│       └── RecommendationDataRepositoryImpl.java
└── api/
    ├── controller/
    │   └── RecommendationController.java
    ├── service/
    │   └── RecommendationApplicationService.java
    └── dto/
        ├── RecommendationRequestDto.java
        └── RecommendationResponseDto.java
```

### 2.2 계층별 책임

#### Domain Layer (핵심 비즈니스 로직)
- **RecommendationDomainService**: 추천 점수 계산 오케스트레이션
- **ScoreCalculator (Interface)**: 점수 계산 전략 인터페이스
- **구체적 Calculator들**: 4가지 속성별 점수 계산 구현

#### Application Layer (유즈케이스)
- **RecommendationApplicationService**: 필터링, 정렬, 페이징 처리
- DTO 변환 및 캐싱 로직

#### Persistence Layer (데이터 접근)
- **RecommendationDataRepository**: 추천에 필요한 집계 데이터 조회
- QueryDSL을 활용한 복잡한 쿼리 처리

---

## 3. 핵심 컴포넌트 설계

### 3.1 점수 계산 파이프라인

```java
// 점수 계산 흐름
1. 데이터 수집 (UserProfile, StoreList)
   ↓
2. 필터링 (위치, 영업시간, 불호 음식)
   ↓
3. 속성별 점수 계산 (4개 Calculator 병렬 실행)
   - StabilityScoreCalculator
   - ExplorationScoreCalculator
   - BudgetEfficiencyScoreCalculator
   - AccessibilityScoreCalculator
   ↓
4. 가중합 계산 (사용자 유형별 가중치 적용)
   ↓
5. 정규화 (0~100 스케일)
   ↓
6. 정렬 및 페이징
   ↓
7. 응답 반환
```

### 3.2 ScoreCalculator 인터페이스 설계

```java
public interface ScoreCalculator {
    /**
     * 특정 가게에 대한 점수를 계산합니다.
     * 
     * @param store 점수를 계산할 가게
     * @param userProfile 사용자 프로필
     * @param context 계산에 필요한 추가 컨텍스트 (전체 가게 리스트, 통계 등)
     * @return 0~100 사이의 정규화된 점수
     */
    double calculate(Store store, UserProfile userProfile, CalculationContext context);
    
    /**
     * 계산에 필요한 데이터를 미리 로드합니다 (성능 최적화).
     */
    void preload(List<Store> stores, UserProfile userProfile);
}
```

### 3.3 StabilityScoreCalculator 상세 설계

```java
public class StabilityScoreCalculator implements ScoreCalculator {
    
    private static final double PREFERENCE_WEIGHT = 0.4;      // 40%
    private static final double EXPENDITURE_WEIGHT = 0.4;     // 40%
    private static final double REVIEW_WEIGHT = 0.2;          // 20%
    private static final double TIME_DECAY_LAMBDA = 0.01;
    
    @Override
    public double calculate(Store store, UserProfile userProfile, CalculationContext context) {
        double preferenceScore = calculatePreferenceScore(store, userProfile);
        double expenditureScore = calculateExpenditureScore(store, userProfile);
        double reviewScore = calculateReviewScore(store, context);
        
        return (preferenceScore * PREFERENCE_WEIGHT) +
               (expenditureScore * EXPENDITURE_WEIGHT) +
               (reviewScore * REVIEW_WEIGHT);
    }
    
    private double calculatePreferenceScore(Store store, UserProfile userProfile) {
        // 카테고리 선호도: 100 (좋아요) / 0 (보통) / -100 (싫어요)
        Integer categoryWeight = userProfile.getCategoryPreference(store.getCategoryId());
        return normalize(categoryWeight, -100, 100);  // -100~100 → 0~100
    }
    
    private double calculateExpenditureScore(Store store, UserProfile userProfile) {
        // 과거 6개월 지출 내역에서 해당 카테고리 비중 계산 (시간 감쇠 적용)
        List<Expenditure> expenditures = userProfile.getRecentExpenditures(180); // 6개월
        
        if (expenditures.size() < 3) {
            return 0.0; // 신규 사용자는 이 가중치 0으로 처리
        }
        
        double totalWeightedAmount = 0.0;
        double categoryWeightedAmount = 0.0;
        
        for (Expenditure exp : expenditures) {
            long daysAgo = ChronoUnit.DAYS.between(exp.getExpendedAt(), LocalDate.now());
            double weight = Math.exp(-TIME_DECAY_LAMBDA * daysAgo);
            double weightedAmount = exp.getAmount() * weight;
            
            totalWeightedAmount += weightedAmount;
            if (exp.getCategoryId().equals(store.getCategoryId())) {
                categoryWeightedAmount += weightedAmount;
            }
        }
        
        double categoryProportion = categoryWeightedAmount / totalWeightedAmount;
        return categoryProportion * 100; // 비율을 0~100 점수로 변환
    }
    
    private double calculateReviewScore(Store store, CalculationContext context) {
        // 전체 가게들의 리뷰 수 중 상대적 위치
        double minReviews = context.getMinReviews();
        double maxReviews = context.getMaxReviews();
        return normalizeMinMax(store.getReviewCount(), minReviews, maxReviews);
    }
}
```

### 3.4 ExplorationScoreCalculator 상세 설계

```java
public class ExplorationScoreCalculator implements ScoreCalculator {
    
    private static final double CATEGORY_FRESHNESS_WEIGHT = 0.4;  // 40%
    private static final double STORE_NEWNESS_WEIGHT = 0.3;       // 30%
    private static final double RECENT_INTEREST_WEIGHT = 0.3;     // 30%
    
    @Override
    public double calculate(Store store, UserProfile userProfile, CalculationContext context) {
        double freshnessScore = calculateCategoryFreshnessScore(store, userProfile);
        double newnessScore = calculateStoreNewnessScore(store, userProfile);
        double interestScore = calculateRecentInterestScore(store, context);
        
        return (freshnessScore * CATEGORY_FRESHNESS_WEIGHT) +
               (newnessScore * STORE_NEWNESS_WEIGHT) +
               (interestScore * RECENT_INTEREST_WEIGHT);
    }
    
    private double calculateCategoryFreshnessScore(Store store, UserProfile userProfile) {
        // 최근 30일간 해당 카테고리 방문 비중의 역수
        List<Expenditure> recentExpenditures = userProfile.getRecentExpenditures(30);
        
        if (recentExpenditures.isEmpty()) {
            return 50.0; // 신규 사용자는 모든 카테고리 동일 점수
        }
        
        long categoryCount = recentExpenditures.stream()
            .filter(exp -> exp.getCategoryId().equals(store.getCategoryId()))
            .count();
        
        double categoryProportion = (double) categoryCount / recentExpenditures.size();
        return (1 - categoryProportion) * 100; // 역수 → 안 간 카테고리일수록 높은 점수
    }
    
    private double calculateStoreNewnessScore(Store store, UserProfile userProfile) {
        // 마지막 방문 후 경과일 (60%) + 가게 신규성 (40%)
        
        // 1) 마지막 방문 후 경과일
        LocalDate lastVisit = userProfile.getLastVisitDate(store.getId());
        double visitScore;
        if (lastVisit == null) {
            visitScore = 100.0; // 한 번도 안 간 가게
        } else {
            long daysAgo = ChronoUnit.DAYS.between(lastVisit, LocalDate.now());
            visitScore = Math.min(daysAgo / 180.0 * 100, 100); // 180일 이상이면 100점
        }
        
        // 2) 가게 등록일 (신규 가게일수록 높은 점수)
        long daysSinceRegistered = ChronoUnit.DAYS.between(store.getRegisteredAt(), LocalDate.now());
        double registeredScore = Math.max(100 - (daysSinceRegistered / 30.0 * 10), 0); // 30일 = 100점, 300일+ = 0점
        
        return visitScore * 0.6 + registeredScore * 0.4;
    }
    
    private double calculateRecentInterestScore(Store store, CalculationContext context) {
        // 최근 7일 조회수 (60%) + 조회수 증가율 (40%)
        
        long views7Days = store.getViewCountLast7Days();
        double viewScore = normalizeLog(views7Days, context.getMinViews7Days(), context.getMaxViews7Days());
        
        // 증가율 = (최근 7일 조회수 - 이전 7일 조회수) / 이전 7일 조회수
        long viewsPrevious7Days = store.getViewCountPrevious7Days();
        double growthRate = viewsPrevious7Days > 0 
            ? ((double) views7Days - viewsPrevious7Days) / viewsPrevious7Days 
            : 0.0;
        
        double growthScore = Math.max(0, Math.min(100, growthRate * 100)); // 음수는 0, 최대 100
        
        return viewScore * 0.6 + growthScore * 0.4;
    }
}
```

### 3.5 BudgetEfficiencyScoreCalculator 상세 설계

```java
public class BudgetEfficiencyScoreCalculator implements ScoreCalculator {
    
    private static final double VALUE_FOR_MONEY_WEIGHT = 0.6;     // 60%
    private static final double BUDGET_FIT_WEIGHT = 0.4;          // 40%
    
    @Override
    public double calculate(Store store, UserProfile userProfile, CalculationContext context) {
        double valueScore = calculateValueForMoneyScore(store, context);
        double budgetFitScore = calculateBudgetFitScore(store, userProfile);
        
        return (valueScore * VALUE_FOR_MONEY_WEIGHT) +
               (budgetFitScore * BUDGET_FIT_WEIGHT);
    }
    
    private double calculateValueForMoneyScore(Store store, CalculationContext context) {
        // 가성비 = log(1 + reviews) / avg_price
        double valueForMoney = Math.log(1 + store.getReviewCount()) / store.getAveragePrice();
        
        double minValue = context.getMinValueForMoney();
        double maxValue = context.getMaxValueForMoney();
        
        return normalizeMinMax(valueForMoney, minValue, maxValue);
    }
    
    private double calculateBudgetFitScore(Store store, UserProfile userProfile) {
        // 현재 시간대에 맞는 예산 가져오기
        MealType currentMealType = getCurrentMealType();
        double userBudget = userProfile.getBudgetForMeal(currentMealType);
        
        if (userBudget <= 0) {
            return 50.0; // 예산 미설정 시 중립 점수
        }
        
        double expectedPrice = store.getAveragePrice();
        double diff = Math.abs(expectedPrice - userBudget);
        double fitScore = 1 - (diff / userBudget);
        
        return Math.max(0, fitScore * 100); // 음수 방지
    }
    
    private MealType getCurrentMealType() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.of(10, 0))) return MealType.BREAKFAST;
        if (now.isBefore(LocalTime.of(15, 0))) return MealType.LUNCH;
        if (now.isBefore(LocalTime.of(21, 0))) return MealType.DINNER;
        return MealType.OTHER;
    }
}
```

### 3.6 AccessibilityScoreCalculator 상세 설계

```java
public class AccessibilityScoreCalculator implements ScoreCalculator {
    
    @Override
    public double calculate(Store store, UserProfile userProfile, CalculationContext context) {
        // 사용자 현재 위치와 가게 사이의 거리 계산
        double distance = calculateDistance(
            userProfile.getCurrentLatitude(),
            userProfile.getCurrentLongitude(),
            store.getLatitude(),
            store.getLongitude()
        );
        
        double minDistance = context.getMinDistance();
        double maxDistance = context.getMaxDistance();
        
        // 거리가 가까울수록 높은 점수
        double normalizedDistance = normalizeMinMax(distance, minDistance, maxDistance);
        return 100 - normalizedDistance; // 역수 변환
    }
    
    /**
     * Haversine 공식을 사용한 두 좌표 간 거리 계산 (km 단위)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // km
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
```

---

## 4. 알고리즘 구현 전략

### 4.1 정규화 유틸리티

```java
public class NormalizationUtil {
    
    /**
     * Min-Max 정규화 (0~100)
     */
    public static double normalizeMinMax(double value, double min, double max) {
        if (max == min) return 50.0; // 모든 값이 같을 경우 중립 점수
        return 100 * (value - min) / (max - min);
    }
    
    /**
     * 로그 스케일 정규화 (지출 내역 등 편차가 작은 데이터)
     */
    public static double normalizeLog(double value, double min, double max) {
        double logValue = Math.log(1 + value);
        double logMin = Math.log(1 + min);
        double logMax = Math.log(1 + max);
        return normalizeMinMax(logValue, logMin, logMax);
    }
    
    /**
     * 선형 정규화 (-100~100 → 0~100)
     */
    public static double normalize(double value, double min, double max) {
        return normalizeMinMax(value, min, max);
    }
}
```

### 4.2 CalculationContext 설계

```java
/**
 * 점수 계산에 필요한 전체 컨텍스트 정보
 * (정규화를 위한 min/max 값들)
 */
public class CalculationContext {
    
    // Review 관련
    private final double minReviews;
    private final double maxReviews;
    
    // 조회수 관련
    private final long minViews7Days;
    private final long maxViews7Days;
    
    // 가성비 관련
    private final double minValueForMoney;
    private final double maxValueForMoney;
    
    // 거리 관련
    private final double minDistance;
    private final double maxDistance;
    
    // 전체 가게 리스트 (참조용)
    private final List<Store> allStores;
    
    /**
     * 필터링된 가게 리스트로부터 Context 생성
     */
    public static CalculationContext from(List<Store> stores, UserProfile userProfile) {
        // 각 속성의 min/max 계산
        DoubleSummaryStatistics reviewStats = stores.stream()
            .mapToDouble(Store::getReviewCount)
            .summaryStatistics();
        
        LongSummaryStatistics viewStats = stores.stream()
            .mapToLong(Store::getViewCountLast7Days)
            .summaryStatistics();
        
        // ... (나머지 통계 계산)
        
        return new CalculationContext(
            reviewStats.getMin(),
            reviewStats.getMax(),
            viewStats.getMin(),
            viewStats.getMax(),
            // ... 
            stores
        );
    }
}
```

### 4.3 사용자 유형별 가중치 관리

```java
public enum RecommendationType {
    
    SAVER(0.30, 0.15, 0.50, 0.05),      // 절약형
    ADVENTURER(0.30, 0.50, 0.10, 0.10), // 모험형
    BALANCED(0.30, 0.25, 0.30, 0.15);   // 균형형
    
    private final double stabilityWeight;
    private final double explorationWeight;
    private final double budgetEfficiencyWeight;
    private final double accessibilityWeight;
    
    RecommendationType(double stability, double exploration, 
                       double budgetEfficiency, double accessibility) {
        this.stabilityWeight = stability;
        this.explorationWeight = exploration;
        this.budgetEfficiencyWeight = budgetEfficiency;
        this.accessibilityWeight = accessibility;
    }
    
    /**
     * 최종 추천 점수 = 4가지 속성 점수의 가중합
     */
    public double calculateFinalScore(double stability, double exploration,
                                     double budgetEfficiency, double accessibility) {
        return (stability * stabilityWeight) +
               (exploration * explorationWeight) +
               (budgetEfficiency * budgetEfficiencyWeight) +
               (accessibility * accessibilityWeight);
    }
}
```

---

## 5. 성능 최적화 전략

### 5.1 데이터 로딩 최적화

#### 5.1.1 Batch Fetch Join
```java
// Store 조회 시 Category, Food를 한 번에 로딩
@EntityGraph(attributePaths = {"category", "foods"})
List<Store> findAllWithDetails(Specification<Store> spec);
```

#### 5.1.2 DTO Projection
```java
// 필요한 컬럼만 조회하는 DTO
public interface StoreRecommendationProjection {
    Long getId();
    String getName();
    Long getCategoryId();
    Double getAveragePrice();
    Integer getReviewCount();
    Long getViewCountLast7Days();
    // ...
}
```

### 5.2 캐싱 전략

#### 5.2.1 Redis 캐싱 계층

```yaml
# Cache Key 전략
- recommendation:user:{memberId}:type:{type}:filter:{hash}  # 추천 결과 (TTL: 10분)
- recommendation:context:{date}                             # CalculationContext (TTL: 1시간)
- recommendation:user-profile:{memberId}                    # UserProfile (TTL: 30분)
```

#### 5.2.2 Cache-Aside 패턴

```java
@Service
public class RecommendationApplicationService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public List<RecommendationResult> getRecommendations(RecommendationRequest request) {
        String cacheKey = generateCacheKey(request);
        
        // 1. 캐시 조회
        List<RecommendationResult> cached = (List<RecommendationResult>) 
            redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        // 2. 캐시 미스 → 계산
        List<RecommendationResult> results = calculateRecommendations(request);
        
        // 3. 캐시 저장 (10분)
        redisTemplate.opsForValue().set(cacheKey, results, 10, TimeUnit.MINUTES);
        
        return results;
    }
}
```

### 5.3 쿼리 최적화

#### 5.3.1 최근 조회수 집계 (배치 작업)

```sql
-- store_view_history 테이블에서 최근 7일/14일 조회수를 Store 테이블에 역정규화
UPDATE store s
SET 
    view_count_last_7_days = (
        SELECT COUNT(*)
        FROM store_view_history svh
        WHERE svh.store_id = s.store_id
          AND svh.viewed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
    ),
    view_count_previous_7_days = (
        SELECT COUNT(*)
        FROM store_view_history svh
        WHERE svh.store_id = s.store_id
          AND svh.viewed_at >= DATE_SUB(NOW(), INTERVAL 14 DAY)
          AND svh.viewed_at < DATE_SUB(NOW(), INTERVAL 7 DAY)
    );
```

**실행 주기**: 매일 새벽 2시 (Spring Batch)

#### 5.3.2 인덱스 전략

```sql
-- 위치 기반 검색 최적화
CREATE INDEX idx_store_location ON store(latitude, longitude);

-- 조회수 정렬 최적화
CREATE INDEX idx_store_view_count ON store(view_count_last_7_days DESC);

-- 가격 정렬 최적화
CREATE INDEX idx_store_avg_price ON store(average_price);

-- 리뷰 수 정렬 최적화
CREATE INDEX idx_store_review_count ON store(review_count DESC);
```

---

## 6. 데이터 흐름

### 6.1 추천 요청 처리 흐름

```
[Client]
   ↓ GET /api/v1/recommendations?radius=1.0&sortBy=SCORE
[RecommendationController]
   ↓ RecommendationRequestDto
[RecommendationApplicationService]
   ↓ 1. 캐시 확인 (Redis)
   ↓ 2. UserProfile 조회 (Member, Preference, Expenditure, Address)
   ↓ 3. Store 필터링 (위치, 영업시간, 불호 음식)
   ↓ 4. CalculationContext 생성
[RecommendationDomainService]
   ↓ 5. 4가지 속성 점수 계산 (병렬)
   │    ├─ StabilityScoreCalculator
   │    ├─ ExplorationScoreCalculator
   │    ├─ BudgetEfficiencyScoreCalculator
   │    └─ AccessibilityScoreCalculator
   ↓ 6. 가중합 계산 (사용자 유형별)
   ↓ 7. 정렬 (sortBy 옵션)
[RecommendationApplicationService]
   ↓ 8. DTO 변환
   ↓ 9. 캐시 저장 (Redis)
   ↓ RecommendationResponseDto
[Client]
```

### 6.2 점수 상세 조회 흐름

```
[Client]
   ↓ GET /api/v1/recommendations/{storeId}/score-detail
[RecommendationController]
   ↓
[RecommendationApplicationService]
   ↓ 1. Store 조회 (존재 여부 확인)
   ↓ 2. UserProfile 조회
   ↓ 3. CalculationContext 생성 (해당 Store만)
[RecommendationDomainService]
   ↓ 4. 4가지 속성 점수 개별 계산
   ↓ 5. ScoreDetail 생성 (각 점수 + 가중치 + 최종 점수)
[RecommendationApplicationService]
   ↓ 6. DTO 변환
[Client]
```

---

## 7. 테스트 전략

### 7.1 단위 테스트 (Unit Test)

#### 7.1.1 점수 계산 로직 테스트

```java
@ExtendWith(MockitoExtension.class)
class StabilityScoreCalculatorTest {
    
    @InjectMocks
    private StabilityScoreCalculator calculator;
    
    @Test
    @DisplayName("선호 카테고리 점수 계산 - 좋아요(100) → 100점")
    void calculatePreferenceScore_Liked() {
        // given
        Store store = createStore(1L, "한식집", 1L);
        UserProfile userProfile = createUserProfileWithPreference(1L, 100); // 카테고리 1번 좋아요
        CalculationContext context = createMockContext();
        
        // when
        double score = calculator.calculate(store, userProfile, context);
        
        // then
        assertThat(score).isGreaterThanOrEqualTo(80.0); // 선호도 40% 영향
    }
    
    @Test
    @DisplayName("신규 사용자 (지출 내역 3건 미만) - 지출 점수 0 처리")
    void calculateExpenditureScore_NewUser() {
        // given
        Store store = createStore(1L, "한식집", 1L);
        UserProfile userProfile = createUserProfileWithLowExpenditures(2); // 지출 2건
        CalculationContext context = createMockContext();
        
        // when
        double score = calculator.calculate(store, userProfile, context);
        
        // then
        // 지출 가중치(40%)가 0이므로, 선호도(40%) + 리뷰(20%)만 반영
        assertThat(score).isLessThan(80.0);
    }
}
```

#### 7.1.2 정규화 유틸리티 테스트

```java
class NormalizationUtilTest {
    
    @Test
    @DisplayName("Min-Max 정규화 - 중간값")
    void normalizeMinMax_MiddleValue() {
        double result = NormalizationUtil.normalizeMinMax(50, 0, 100);
        assertThat(result).isEqualTo(50.0);
    }
    
    @Test
    @DisplayName("Min-Max 정규화 - 모든 값 동일 시 50 반환")
    void normalizeMinMax_SameValues() {
        double result = NormalizationUtil.normalizeMinMax(10, 10, 10);
        assertThat(result).isEqualTo(50.0);
    }
    
    @Test
    @DisplayName("로그 정규화 - 큰 편차 완화")
    void normalizeLog_LargeDifference() {
        double result1 = NormalizationUtil.normalizeLog(10, 1, 1000);
        double result2 = NormalizationUtil.normalizeLog(100, 1, 1000);
        
        // 로그 스케일이므로 10배 차이가 정규화 후에는 더 작은 차이
        assertThat(result2 - result1).isLessThan(20.0);
    }
}
```

### 7.2 통합 테스트 (Integration Test)

```java
@SpringBootTest
@Transactional
@Testcontainers
class RecommendationApplicationServiceIntegrationTest {
    
    @Autowired
    private RecommendationApplicationService recommendationService;
    
    @Autowired
    private TestDataBuilder testDataBuilder;
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    
    @Test
    @DisplayName("절약형 사용자 - 예산 효율성 높은 가게 우선 추천")
    void recommend_SaverType_PrioritizeBudgetEfficiency() {
        // given
        Member member = testDataBuilder.createMember("saver@test.com", RecommendationType.SAVER);
        testDataBuilder.createMonthlyBudget(member, 5000); // 점심 예산 5000원
        
        Store cheapStore = testDataBuilder.createStore("저렴한 가게", 4500, 100); // 가성비 좋음
        Store expensiveStore = testDataBuilder.createStore("비싼 가게", 12000, 150);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .memberId(member.getId())
            .latitude(37.5665)
            .longitude(126.9780)
            .radius(2.0)
            .build();
        
        // when
        List<RecommendationResult> results = recommendationService.getRecommendations(request);
        
        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStoreId()).isEqualTo(cheapStore.getId()); // 저렴한 가게 1위
        assertThat(results.get(0).getScore()).isGreaterThan(results.get(1).getScore());
    }
    
    @Test
    @DisplayName("모험형 사용자 - 신규 가게 및 안 가본 카테고리 우선 추천")
    void recommend_AdventurerType_PrioritizeExploration() {
        // given
        Member member = testDataBuilder.createMember("adventurer@test.com", RecommendationType.ADVENTURER);
        
        // 최근 30일간 한식만 5번 방문
        testDataBuilder.createExpenditures(member, CategoryType.KOREAN, 5, 30);
        
        Store koreanStore = testDataBuilder.createStore("한식집", CategoryType.KOREAN);
        Store chineseStore = testDataBuilder.createStore("중식집", CategoryType.CHINESE); // 안 가본 카테고리
        
        RecommendationRequest request = createRequest(member);
        
        // when
        List<RecommendationResult> results = recommendationService.getRecommendations(request);
        
        // then
        assertThat(results.get(0).getStoreId()).isEqualTo(chineseStore.getId()); // 중식 우선
    }
}
```

### 7.3 REST Docs 테스트

```java
@WebMvcTest(RecommendationController.class)
@AutoConfigureRestDocs
class RecommendationControllerRestDocsTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RecommendationApplicationService recommendationService;
    
    @Test
    @DisplayName("추천 목록 조회 - 성공")
    void getRecommendations_Success() throws Exception {
        // given
        List<RecommendationResult> mockResults = createMockResults();
        when(recommendationService.getRecommendations(any())).thenReturn(mockResults);
        
        // when & then
        mockMvc.perform(
            get("/api/v1/recommendations")
                .header("Authorization", "Bearer " + JWT_TOKEN)
                .param("latitude", "37.5665")
                .param("longitude", "126.9780")
                .param("radius", "1.0")
                .param("sortBy", "SCORE")
                .param("includeDisliked", "false")
        )
        .andExpect(status().isOk())
        .andDo(document("recommendation-list",
            requestHeaders(
                headerWithName("Authorization").description("JWT 인증 토큰")
            ),
            queryParameters(
                parameterWithName("latitude").description("현재 위도"),
                parameterWithName("longitude").description("현재 경도"),
                parameterWithName("radius").description("검색 반경 (km)").optional(),
                parameterWithName("sortBy").description("정렬 기준 (SCORE, DISTANCE, REVIEW, ...)").optional(),
                parameterWithName("includeDisliked").description("불호 음식 포함 여부").optional()
            ),
            responseFields(
                fieldWithPath("success").description("성공 여부"),
                fieldWithPath("data[].storeId").description("가게 ID"),
                fieldWithPath("data[].storeName").description("가게 이름"),
                fieldWithPath("data[].score").description("추천 점수 (0~100)"),
                fieldWithPath("data[].distance").description("거리 (km)"),
                // ...
            )
        ));
    }
}
```

---

## 8. API 설계

### 8.1 추천 목록 조회

```http
GET /api/v1/recommendations
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| latitude | double | ❌ | (기본 주소) | 현재 위도 |
| longitude | double | ❌ | (기본 주소) | 현재 경도 |
| radius | double | ❌ | 0.5 | 검색 반경 (km) |
| sortBy | enum | ❌ | SCORE | 정렬 기준 (SCORE, DISTANCE, REVIEW, PRICE_LOW, PRICE_HIGH, FAVORITE, INTEREST_HIGH, INTEREST_LOW) |
| includeDisliked | boolean | ❌ | false | 불호 음식 포함 여부 |
| openNow | boolean | ❌ | false | 영업 중인 가게만 |
| storeType | enum | ❌ | ALL | 가게 타입 (ALL, CAFETERIA, RESTAURANT) |
| page | int | ❌ | 0 | 페이지 번호 |
| size | int | ❌ | 20 | 페이지 크기 |

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "storeId": 123,
      "storeName": "맛있는 한식당",
      "categoryName": "한식",
      "score": 87.5,
      "distance": 0.35,
      "averagePrice": 8000,
      "reviewCount": 245,
      "isFavorite": false,
      "imageUrl": "https://...",
      "openingStatus": "OPEN"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

### 8.2 점수 상세 조회

```http
GET /api/v1/recommendations/{storeId}/score-detail
Authorization: Bearer {JWT_TOKEN}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "storeId": 123,
    "storeName": "맛있는 한식당",
    "finalScore": 87.5,
    "userType": "BALANCED",
    "scoreBreakdown": {
      "stability": {
        "score": 85.0,
        "weight": 0.30,
        "weightedScore": 25.5,
        "details": {
          "preferenceScore": 100.0,
          "expenditureScore": 75.0,
          "reviewScore": 80.0
        }
      },
      "exploration": {
        "score": 60.0,
        "weight": 0.25,
        "weightedScore": 15.0,
        "details": {
          "categoryFreshnessScore": 70.0,
          "storeNewnessScore": 50.0,
          "recentInterestScore": 60.0
        }
      },
      "budgetEfficiency": {
        "score": 92.0,
        "weight": 0.30,
        "weightedScore": 27.6,
        "details": {
          "valueForMoneyScore": 95.0,
          "budgetFitScore": 88.0
        }
      },
      "accessibility": {
        "score": 95.0,
        "weight": 0.15,
        "weightedScore": 14.25,
        "details": {
          "distance": 0.35
        }
      }
    }
  }
}
```

### 8.3 추천 유형 변경

```http
PUT /api/v1/recommendations/type
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "recommendationType": "ADVENTURER"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "recommendationType": "ADVENTURER",
    "updatedAt": "2025-10-13T15:30:00"
  }
}
```

---

## 9. 구현 우선순위 및 일정

### Phase 1: 핵심 점수 계산 로직 (1-2일)
- [ ] NormalizationUtil 구현 및 테스트
- [ ] CalculationContext 구현
- [ ] 4가지 ScoreCalculator 구현 및 단위 테스트
- [ ] RecommendationDomainService 구현

### Phase 2: 필터링 및 정렬 (1일)
- [ ] RecommendationDataRepository 인터페이스 정의
- [ ] QueryDSL 기반 Repository 구현 (위치, 영업시간, 불호 음식 필터)
- [ ] 다양한 정렬 옵션 구현

### Phase 3: Application Service 및 API (1일)
- [ ] RecommendationApplicationService 구현
- [ ] RecommendationController 구현
- [ ] DTO 변환 로직

### Phase 4: 통합 테스트 및 REST Docs (1일)
- [ ] 통합 테스트 작성 (시나리오별)
- [ ] REST Docs 문서화
- [ ] 성능 테스트 (1000개 가게, 100명 사용자)

### Phase 5: 캐싱 및 최적화 (1일)
- [ ] Redis 캐싱 적용
- [ ] Spring Batch 작업 (조회수 집계)
- [ ] 인덱스 최적화

**총 예상 소요 시간**: 5-6일

---

## 10. 참고 사항

### 10.1 제약사항
- 최근 6개월 지출 내역 < 3건인 경우 → 신규 사용자로 간주
- 정규화는 필터링된 결과 집합 내에서만 수행 (전체 가게 기준 아님)
- 시간 감쇠 λ = 0.01 (약 100일 후 37%로 감소)

### 10.2 향후 개선 방향
- **머신러닝 모델 도입**: 사용자 행동 패턴 학습
- **A/B 테스트**: 추천 알고리즘 효과 측정
- **실시간 스트림 처리**: Kafka를 활용한 조회수 실시간 집계
- **개인화 강화**: 시간대별, 날씨별, 요일별 선호도 반영

---

**문서 버전**: 1.0  
**작성일**: 2025-10-13  
**다음 리뷰 예정일**: 2025-10-20
