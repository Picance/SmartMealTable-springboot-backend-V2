# 🏠 홈 화면 API 구현 가이드

> **작성일**: 2025-10-14
> **상태**: 구현 가이드 문서
> **목표**: 3개 홈 화면 API 구현으로 프로젝트 100% 완료

---

## 📋 개요

홈 화면 API 3개를 구현하여 SmartMealTable 프로젝트를 100% 완료합니다.

**구현 대상 API**:
1. `GET /api/v1/home/dashboard` - 홈 대시보드 조회
2. `GET /api/v1/members/me/onboarding-status` - 온보딩 상태 조회
3. `POST /api/v1/members/me/monthly-budget-confirmed` - 월별 예산 확인 처리

---

## 🎯 API 상세 스펙

### 1. 홈 대시보드 조회

**Endpoint**: `GET /api/v1/home/dashboard`

**설명**: 
- 사용자의 기본 주소를 기준으로 홈 화면에 필요한 모든 정보 제공
- 오늘의 예산/지출, 추천 메뉴/가게 포함

**Response 구조**:
```json
{
  "result": "SUCCESS",
  "data": {
    "location": {
      "addressHistoryId": 456,
      "addressAlias": "우리집",
      "fullAddress": "서울특별시 강남구 테헤란로 123 101동 101호",
      "roadAddress": "서울특별시 강남구 테헤란로 123",
      "latitude": 37.497942,
      "longitude": 127.027621,
      "isPrimary": true
    },
    "budget": {
      "todaySpent": 12500,
      "todayBudget": 15000,
      "remaining": 2500,
      "utilizationRate": 83.33,
      "mealBudgets": [
        {
          "mealType": "BREAKFAST",
          "budget": 3000,
          "spent": 0,
          "remaining": 3000
        },
        {
          "mealType": "LUNCH",
          "budget": 5000,
          "spent": 5500,
          "remaining": -500
        },
        {
          "mealType": "DINNER",
          "budget": 7000,
          "spent": 7000,
          "remaining": 0
        }
      ]
    },
    "recommendedMenus": [
      {
        "foodId": 201,
        "foodName": "김치찌개",
        "price": 7000,
        "storeId": 101,
        "storeName": "맛있는집",
        "distance": 0.3,
        "tags": ["인기메뉴", "예산적합"],
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg"
      }
    ],
    "recommendedStores": [
      {
        "storeId": 101,
        "storeName": "맛있는집",
        "categoryName": "한식",
        "distance": 0.3,
        "distanceText": "도보 5분 거리",
        "contextInfo": "학교 근처",
        "averagePrice": 7500,
        "reviewCount": 523,
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg"
      }
    ]
  },
  "error": null
}
```

**Error Cases**:
- `404`: 등록된 주소가 없음 (`ADDRESS_NOT_FOUND`)

---

### 2. 온보딩 상태 조회

**Endpoint**: `GET /api/v1/members/me/onboarding-status`

**설명**: 
- 온보딩 완료 여부 및 모달 표시 여부 확인
- 최초 온보딩 후 추천 유형 선택 모달
- 매월 1일 이후 월별 예산 확인 모달

**Response 구조**:
```json
{
  "result": "SUCCESS",
  "data": {
    "isOnboardingComplete": true,
    "hasSelectedRecommendationType": false,
    "hasConfirmedMonthlyBudget": false,
    "currentMonth": "2025-10",
    "showRecommendationTypeModal": true,
    "showMonthlyBudgetModal": true
  },
  "error": null
}
```

---

### 3. 월별 예산 확인 처리

**Endpoint**: `POST /api/v1/members/me/monthly-budget-confirmed`

**설명**: 
- 매월 첫 방문 시 예산 확인 모달에서 사용자 선택 처리
- "기존 유지" 또는 "변경" 액션 기록

**Request 구조**:
```json
{
  "year": 2025,
  "month": 10,
  "action": "KEEP"
}
```

**Enum Values**:
- `action`: `KEEP` (기존 유지), `CHANGE` (변경하러 가기)

**Response 구조**:
```json
{
  "result": "SUCCESS",
  "data": {
    "year": 2025,
    "month": 10,
    "confirmedAt": "2025-10-08T12:34:56.789Z",
    "monthlyBudget": 300000
  },
  "error": null
}
```

---

## 🏗 구현 아키텍처

### 레이어드 아키텍처 구조

```
smartmealtable-api/
└── src/main/java/com/stdev/smartmealtable/api/home/
    ├── controller/
    │   ├── HomeController.java                     # REST Controller
    │   ├── dto/
    │   │   ├── HomeDashboardResponse.java          # 홈 대시보드 응답 DTO
    │   │   ├── OnboardingStatusResponse.java       # 온보딩 상태 응답 DTO
    │   │   ├── MonthlyBudgetConfirmRequest.java    # 월별 예산 확인 요청 DTO
    │   │   └── MonthlyBudgetConfirmResponse.java   # 월별 예산 확인 응답 DTO
    │   
    └── service/
        ├── HomeDashboardQueryService.java          # 홈 대시보드 조회 서비스
        ├── OnboardingStatusQueryService.java       # 온보딩 상태 조회 서비스
        ├── MonthlyBudgetConfirmService.java        # 월별 예산 확인 처리 서비스
        └── dto/
            ├── HomeDashboardServiceResponse.java   # 서비스 응답 DTO
            ├── OnboardingStatusServiceResponse.java
            └── MonthlyBudgetConfirmServiceResponse.java

smartmealtable-domain/
└── src/main/java/com/stdev/smartmealtable/domain/
    └── member/
        ├── entity/
        │   └── MonthlyBudgetConfirmation.java      # 월별 예산 확인 이력 엔티티
        └── repository/
            └── MonthlyBudgetConfirmationRepository.java
```

---

## 💾 데이터베이스 스키마

### monthly_budget_confirmation 테이블

```sql
CREATE TABLE monthly_budget_confirmation (
    monthly_budget_confirmation_id  BIGINT      NOT NULL AUTO_INCREMENT COMMENT '월별 예산 확인의 고유 식별자',
    member_id                       BIGINT      NOT NULL COMMENT '회원 ID (FK)',
    year                            INT         NOT NULL COMMENT '연도',
    month                           INT         NOT NULL COMMENT '월 (1-12)',
    action                          VARCHAR(20) NOT NULL COMMENT '사용자 액션 (KEEP: 유지, CHANGE: 변경)',
    confirmed_at                    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '확인 시각',
    
    PRIMARY KEY (monthly_budget_confirmation_id),
    UNIQUE KEY uq_member_year_month (member_id, year, month),
    INDEX idx_member_id (member_id),
    INDEX idx_confirmed_at (confirmed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='월별 예산 확인 이력 테이블';
```

---

## 🔧 핵심 구현 로직

### 1. HomeDashboardQueryService 핵심 로직

```java
public HomeDashboardServiceResponse getHomeDashboard(Long memberId) {
    // 1. 기본 주소 조회 (필수)
    AddressHistory primaryAddress = addressHistoryRepository.findPrimaryByMemberId(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorType.ADDRESS_NOT_FOUND));

    // 2. 오늘의 예산/지출 정보 수집
    LocalDate today = LocalDate.now();
    DailyBudget dailyBudget = dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, today)
            .orElse(null);
    List<MealBudget> mealBudgets = mealBudgetRepository.findByMemberIdAndBudgetDate(memberId, today);
    
    // 3. 오늘의 지출 계산
    Long todaySpentLong = expenditureRepository.getTotalAmountByPeriod(memberId, today, today);
    BigDecimal todaySpent = todaySpentLong != null ? BigDecimal.valueOf(todaySpentLong) : BigDecimal.ZERO;
    
    // 4. 끼니별 지출 계산
    Map<MealType, Long> mealTypeAmounts = expenditureRepository.getAmountByMealTypeForPeriod(memberId, today, today);
    
    // 5. 추천 메뉴/가게 조회 (기본 주소 기준)
    List<Food> recommendedFoods = foodRepository.findTopByDistance(
            primaryAddress.getLatitude(), 
            primaryAddress.getLongitude(), 
            5
    );
    List<Store> recommendedStores = storeRepository.findTopByDistance(
            primaryAddress.getLatitude(), 
            primaryAddress.getLongitude(), 
            5
    );
    
    // 6. 응답 생성
    return HomeDashboardServiceResponse.of(...);
}
```

### 2. OnboardingStatusQueryService 핵심 로직

```java
public OnboardingStatusServiceResponse getOnboardingStatus(Long memberId) {
    // 1. 회원 정보 조회
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorType.MEMBER_NOT_FOUND));
    
    // 2. 추천 유형 선택 여부
    boolean hasSelectedRecommendationType = member.getRecommendationType() != null;
    
    // 3. 이번 달 예산 확인 여부
    LocalDate now = LocalDate.now();
    boolean hasConfirmedMonthlyBudget = monthlyBudgetConfirmationRepository
            .existsByMemberIdAndYearAndMonth(memberId, now.getYear(), now.getMonthValue());
    
    // 4. 모달 표시 여부 결정
    boolean showRecommendationTypeModal = member.isOnboardingComplete() && !hasSelectedRecommendationType;
    boolean showMonthlyBudgetModal = !hasConfirmedMonthlyBudget;
    
    return OnboardingStatusServiceResponse.of(...);
}
```

### 3. MonthlyBudgetConfirmService 핵심 로직

```java
@Transactional
public MonthlyBudgetConfirmServiceResponse confirmMonthlyBudget(
        Long memberId, 
        int year, 
        int month, 
        String action
) {
    // 1. 중복 확인 체크
    if (monthlyBudgetConfirmationRepository.existsByMemberIdAndYearAndMonth(memberId, year, month)) {
        throw new BusinessException(ErrorType.ALREADY_CONFIRMED);
    }
    
    // 2. 월별 예산 조회
    MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByMemberIdAndYearMonth(memberId, year, month)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorType.MONTHLY_BUDGET_NOT_FOUND));
    
    // 3. 확인 이력 생성
    MonthlyBudgetConfirmation confirmation = MonthlyBudgetConfirmation.builder()
            .memberId(memberId)
            .year(year)
            .month(month)
            .action(BudgetConfirmAction.valueOf(action))
            .build();
    
    monthlyBudgetConfirmationRepository.save(confirmation);
    
    return MonthlyBudgetConfirmServiceResponse.of(confirmation, monthlyBudget);
}
```

---

## 📝 테스트 전략

### 1. 단위 테스트 (Service Layer)

**HomeDashboardQueryServiceTest**:
- ✅ 홈 대시보드 정상 조회
- ❌ 기본 주소 없음 (404 에러)
- ✅ 오늘 예산 없는 경우 (기본값 0)
- ✅ 오늘 지출 없는 경우 (기본값 0)

**OnboardingStatusQueryServiceTest**:
- ✅ 온보딩 완료, 추천 유형 미선택 (모달 표시)
- ✅ 온보딩 완료, 추천 유형 선택됨 (모달 숨김)
- ✅ 이번 달 예산 미확인 (모달 표시)
- ✅ 이번 달 예산 확인됨 (모달 숨김)

**MonthlyBudgetConfirmServiceTest**:
- ✅ 월별 예산 확인 정상 처리 (KEEP)
- ✅ 월별 예산 확인 정상 처리 (CHANGE)
- ❌ 중복 확인 시도 (409 에러)
- ❌ 월별 예산 없음 (404 에러)

### 2. 통합 테스트 (Controller Layer)

**HomeControllerTest**:
- ✅ 홈 대시보드 조회 성공
- ❌ 인증 없음 (401 에러)
- ❌ 기본 주소 없음 (404 에러)
- ✅ 온보딩 상태 조회 성공
- ✅ 월별 예산 확인 처리 성공
- ❌ 잘못된 요청 (422 에러)

### 3. REST Docs 테스트

**HomeControllerRestDocsTest**:
1. **홈 대시보드 조회** (5개 시나리오)
   - ✅ 성공: 전체 정보 조회
   - ❌ 실패: 인증 없음 (401)
   - ❌ 실패: 기본 주소 없음 (404)

2. **온보딩 상태 조회** (3개 시나리오)
   - ✅ 성공: 모달 모두 표시
   - ✅ 성공: 모달 모두 숨김
   - ❌ 실패: 인증 없음 (401)

3. **월별 예산 확인 처리** (5개 시나리오)
   - ✅ 성공: KEEP 액션
   - ✅ 성공: CHANGE 액션
   - ❌ 실패: 중복 확인 (409)
   - ❌ 실패: 유효성 검증 실패 (422)
   - ❌ 실패: 인증 없음 (401)

**총 REST Docs 시나리오**: 13개

---

## 🚀 구현 단계

### Phase 1: Domain Layer (30분)
1. `MonthlyBudgetConfirmation` 엔티티 생성
2. `MonthlyBudgetConfirmationRepository` 인터페이스 생성
3. Storage 모듈에 JPA 엔티티 및 Repository 구현

### Phase 2: Application Service Layer (45분)
1. `HomeDashboardQueryService` 구현
2. `OnboardingStatusQueryService` 구현
3. `MonthlyBudgetConfirmService` 구현
4. 서비스 응답 DTO 생성
5. 단위 테스트 작성 (Mockist 스타일)

### Phase 3: API Layer (30분)
1. `HomeController` 구현
2. Controller DTO 생성 (Request/Response)
3. 전역 예외 처리 추가

### Phase 4: Integration Test (45분)
1. `HomeControllerTest` 작성
2. 모든 성공/실패 케이스 테스트
3. Test Container 활용

### Phase 5: REST Docs (60분)
1. `HomeControllerRestDocsTest` 작성
2. 13개 시나리오 문서화
3. 요청/응답 필드 상세 문서화
4. 에러 케이스 문서화

### Phase 6: 최종 검증 (30분)
1. 전체 빌드 및 테스트 실행
2. REST Docs 문서 생성 확인
3. IMPLEMENTATION_PROGRESS.md 업데이트

**총 예상 소요 시간**: 약 4시간

---

## 📚 참고 문서

- **API 명세**: `API_SPECIFICATION.md` (Line 2586-2800)
- **요구사항**: `SRD.md` (Line 284-350)
- **데이터베이스 스키마**: `ddl.sql`
- **기존 구현 패턴**: 
  - `BudgetController` - 예산 관리 API 패턴
  - `ExpenditureController` - 지출 내역 API 패턴
  - `RecommendationController` - 추천 시스템 API 패턴

---

## ✅ 완료 체크리스트

### Domain Layer
- [ ] MonthlyBudgetConfirmation 엔티티 생성
- [ ] MonthlyBudgetConfirmationRepository 인터페이스
- [ ] JPA 엔티티 및 QueryDSL 구현

### Application Layer
- [ ] HomeDashboardQueryService 구현
- [ ] OnboardingStatusQueryService 구현
- [ ] MonthlyBudgetConfirmService 구현
- [ ] 서비스 DTO 생성
- [ ] 단위 테스트 작성

### API Layer
- [ ] HomeController 구현
- [ ] Controller DTO 생성
- [ ] 통합 테스트 작성

### Documentation
- [ ] REST Docs 테스트 13개 시나리오
- [ ] API 문서 생성 확인
- [ ] IMPLEMENTATION_PROGRESS.md 업데이트
- [ ] 완료 보고서 작성

---

## 🎉 완료 후 예상 결과

```
전체 API 엔드포인트:  70개
구현 완료:            70개
진행률:               100% ████████████████████ 

REST Docs 테스트:     150개 (137개 기존 + 13개 신규)
테스트 통과율:        100%

프로젝트 상태:        🎉 100% 완료!
```

---

**다음 작업**: 위 가이드를 따라 홈 화면 API 3개를 완전히 구현하여 프로젝트를 100% 완료합니다.
