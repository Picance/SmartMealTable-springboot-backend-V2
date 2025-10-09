# 🎉 온보딩 - 예산 설정 API 완료 보고서

**완료일**: 2025-10-10  
**작업자**: GitHub Copilot  
**작업 방식**: TDD (Test-Driven Development)

---

## 📋 Overview

온보딩 과정의 세 번째 API인 "예산 설정" 기능을 완전히 구현했습니다.  
사용자가 회원가입 후 월별/일별/식사별 예산을 설정할 수 있는 기능입니다.

---

## ✅ 구현 완료 항목

### 1. Domain Layer (100%)

#### 1.1 MealType Enum
**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/budget/MealType.java`

```java
public enum MealType {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁");

    private final String description;
}
```

**특징**:
- 3가지 식사 유형 정의 (아침, 점심, 저녁)
- 한글 설명 포함

#### 1.2 MonthlyBudget Domain Entity
**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/budget/MonthlyBudget.java`

**핵심 필드**:
- `monthlyBudgetId`: 월별 예산 ID
- `memberId`: 회원 ID
- `monthlyFoodBudget`: 월별 식비 예산
- `monthlyUsedAmount`: 월별 사용 금액
- `budgetMonth`: 예산 월 (YearMonth)

**팩토리 메서드**:
- `create()`: 새로운 예산 생성 (ID는 null)
- `reconstitute()`: JPA Entity → Domain Entity 변환 (ID 보존)

**비즈니스 로직**:
- `changeMonthlyFoodBudget(Integer newBudget)`: 예산 변경
- `addUsedAmount(Integer amount)`: 사용 금액 추가

#### 1.3 DailyBudget Domain Entity
**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/budget/DailyBudget.java`

**핵심 필드**:
- `dailyBudgetId`: 일별 예산 ID
- `memberId`: 회원 ID
- `dailyFoodBudget`: 일별 식비 예산
- `dailyUsedAmount`: 일별 사용 금액
- `budgetDate`: 예산 날짜 (LocalDate)

**팩토리 메서드**:
- `create()`: 새로운 일별 예산 생성
- `reconstitute()`: JPA Entity → Domain Entity 변환 (ID 보존)

#### 1.4 MealBudget Domain Entity
**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/budget/MealBudget.java`

**핵심 필드**:
- `mealBudgetId`: 식사별 예산 ID
- `memberId`: 회원 ID
- `mealType`: 식사 유형 (MealType enum)
- `mealBudget`: 식사별 예산 금액
- `mealUsedAmount`: 식사별 사용 금액
- `budgetDate`: 예산 날짜 (LocalDate)

**팩토리 메서드**:
- `create()`: 새로운 식사별 예산 생성
- `reconstitute()`: JPA Entity → Domain Entity 변환 (ID 보존)

#### 1.5 Repository Interfaces
**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/budget/`

- `MonthlyBudgetRepository`: 월별 예산 조회/저장
- `DailyBudgetRepository`: 일별 예산 조회/저장
- `MealBudgetRepository`: 식사별 예산 조회/저장

---

### 2. Storage Layer (100%)

#### 2.1 MonthlyBudgetJpaEntity
**위치**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/budget/MonthlyBudgetJpaEntity.java`

**테이블**: `monthly_budget`

**칼럼**:
```sql
monthly_budget_id BIGINT PRIMARY KEY AUTO_INCREMENT,
member_id BIGINT NOT NULL,
monthly_food_budget INT NOT NULL,
monthly_used_amount INT DEFAULT 0,
budget_month VARCHAR(7) NOT NULL,  -- YYYY-MM 형식
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

**특징**:
- `@Id @GeneratedValue(strategy = IDENTITY)`: Auto Increment
- `toDomain()`: reconstitute 패턴 사용하여 ID 보존
- `from(MonthlyBudget)`: Domain → JPA Entity 변환

#### 2.2 DailyBudgetJpaEntity
**위치**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/budget/DailyBudgetJpaEntity.java`

**테이블**: `daily_budget`

**칼럼**:
```sql
daily_budget_id BIGINT PRIMARY KEY AUTO_INCREMENT,
member_id BIGINT NOT NULL,
daily_food_budget INT NOT NULL,
daily_used_amount INT DEFAULT 0,
budget_date DATE NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

#### 2.3 MealBudgetJpaEntity
**위치**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/budget/MealBudgetJpaEntity.java`

**테이블**: `meal_budget`

**칼럼**:
```sql
meal_budget_id BIGINT PRIMARY KEY AUTO_INCREMENT,
member_id BIGINT NOT NULL,
meal_type VARCHAR(20) NOT NULL,  -- BREAKFAST, LUNCH, DINNER
meal_budget INT NOT NULL,
meal_used_amount INT DEFAULT 0,
budget_date DATE NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

**특징**:
- `@Enumerated(EnumType.STRING)`: MealType을 문자열로 저장

#### 2.4 Spring Data JPA Repositories
**위치**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/budget/`

- `MonthlyBudgetJpaRepository`: Spring Data JPA 인터페이스
- `DailyBudgetJpaRepository`: Spring Data JPA 인터페이스
- `MealBudgetJpaRepository`: Spring Data JPA 인터페이스

**커스텀 쿼리**:
```java
@Query("SELECT m FROM MonthlyBudgetJpaEntity m WHERE m.memberId = :memberId ORDER BY m.budgetMonth DESC LIMIT 1")
Optional<MonthlyBudgetJpaEntity> findLatestByMemberId(@Param("memberId") Long memberId);
```

#### 2.5 Repository Implementations
**위치**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/budget/`

- `MonthlyBudgetRepositoryImpl`: Domain Repository 구현체
- `DailyBudgetRepositoryImpl`: Domain Repository 구현체
- `MealBudgetRepositoryImpl`: Domain Repository 구현체

**특징**:
- `save()`: Domain → JPA Entity → DB 저장 → Domain 반환
- `findById()`: JPA Entity 조회 → Domain 변환
- reconstitute 패턴 적용하여 ID 보존

---

### 3. Service Layer (100%)

#### 3.1 SetBudgetService
**위치**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/service/SetBudgetService.java`

**메서드**: `SetBudgetServiceResponse setBudget(Long memberId, SetBudgetServiceRequest request)`

**로직**:
1. 월별 예산 생성 (현재 월 기준: `YearMonth.now()`)
2. 일별 예산 생성 (현재 일 기준: `LocalDate.now()`)
3. 식사별 예산 생성 (BREAKFAST, LUNCH, DINNER)
4. 모든 예산 정보를 응답으로 반환

**트랜잭션**:
- `@Transactional`: 원자성 보장 (전체 성공 또는 전체 실패)

**Service DTO**:
- `SetBudgetServiceRequest`: 월별/일별 예산 + 식사별 예산 Map
- `SetBudgetServiceResponse`: 설정된 예산 정보

---

### 4. Controller Layer (100%)

#### 4.1 OnboardingController - setBudget Endpoint
**위치**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/controller/OnboardingController.java`

**Endpoint**: `POST /api/v1/onboarding/budget`

**Request**:
```json
{
  "monthlyBudget": 300000,
  "dailyBudget": 10000,
  "mealBudgets": {
    "BREAKFAST": 3000,
    "LUNCH": 5000,
    "DINNER": 7000
  }
}
```

**Response** (201 Created):
```json
{
  "result": "SUCCESS",
  "data": {
    "monthlyBudget": 300000,
    "dailyBudget": 10000,
    "mealBudgets": [
      {
        "mealType": "BREAKFAST",
        "budget": 3000
      },
      {
        "mealType": "LUNCH",
        "budget": 5000
      },
      {
        "mealType": "DINNER",
        "budget": 7000
      }
    ]
  },
  "error": null
}
```

**Validation**:
- `@NotNull`: 모든 필드 필수
- `@Min(0)`: 예산은 0원 이상

**Authentication**:
- JWT 토큰 필수 (`Authorization: Bearer {token}`)
- `@AuthUser` ArgumentResolver 사용

#### 4.2 Request/Response DTOs
**위치**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/controller/dto/`

**SetBudgetRequest.java**:
```java
public record SetBudgetRequest(
    @NotNull(message = "월별 예산은 필수입니다.")
    @Min(value = 0, message = "월별 예산은 0원 이상이어야 합니다.")
    Integer monthlyBudget,
    
    @NotNull(message = "일별 예산은 필수입니다.")
    @Min(value = 0, message = "일별 예산은 0원 이상이어야 합니다.")
    Integer dailyBudget,
    
    @NotNull(message = "식사별 예산은 필수입니다.")
    Map<String, Integer> mealBudgets
) {}
```

**SetBudgetResponse.java**:
```java
public record SetBudgetResponse(
    Integer monthlyBudget,
    Integer dailyBudget,
    List<MealBudgetInfo> mealBudgets
) {
    public record MealBudgetInfo(
        String mealType,
        Integer budget
    ) {}
}
```

---

### 5. 통합 테스트 (100%)

#### 5.1 SetBudgetControllerTest
**위치**: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/onboarding/controller/SetBudgetControllerTest.java`

**테스트 결과**: ✅ 6/6 통과

**테스트 케이스**:
1. ✅ `setBudget_success` - 예산 설정 성공 (201 Created)
2. ✅ `setBudget_monthlyBudgetNull` - 월별 예산 null (422)
3. ✅ `setBudget_dailyBudgetNull` - 일별 예산 null (422)
4. ✅ `setBudget_mealBudgetsNull` - 식사별 예산 null (422)
5. ✅ `setBudget_negativeBudget` - 음수 예산 (422)
6. ✅ `setBudget_noToken` - JWT 토큰 누락 (400)

**테스트 환경**:
- TestContainers MySQL 8.0
- MockMvc
- `@SpringBootTest`
- `@AutoConfigureMockMvc`

**실행 결과**:
```bash
./gradlew :smartmealtable-api:test --tests SetBudgetControllerTest
# BUILD SUCCESSFUL in 15s
# 6 tests completed, 0 failed
```

---

### 6. Spring Rest Docs 문서화 (100%)

#### 6.1 SetBudgetControllerRestDocsTest
**위치**: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/onboarding/controller/SetBudgetControllerRestDocsTest.java`

**테스트 결과**: ✅ 3/3 통과

**문서화 테스트**:
1. ✅ `setBudget_Success_Docs` - 성공 시나리오 (201 Created)
2. ✅ `setBudget_MonthlyBudgetNull_Docs` - Validation 실패 (422)
3. ✅ `setBudget_NoToken_Docs` - JWT 인증 실패 (400)

**생성된 Snippets**:
- `onboarding-budget-set-success/`
  - http-request.adoc
  - http-response.adoc
  - request-headers.adoc
  - request-fields.adoc
  - response-fields.adoc
- `onboarding-budget-set-validation-error/`
  - http-request.adoc
  - http-response.adoc
  - request-fields.adoc
  - response-fields.adoc
- `onboarding-budget-set-auth-error/`
  - http-request.adoc
  - http-response.adoc
  - response-fields.adoc

**실행 결과**:
```bash
./gradlew :smartmealtable-api:test --tests SetBudgetControllerRestDocsTest
# BUILD SUCCESSFUL in 11s
# 3 tests completed, 0 failed
```

#### 6.2 index.adoc 업데이트
**위치**: `smartmealtable-api/src/docs/asciidoc/index.adoc`

**추가된 섹션**:
```asciidoc
[[onboarding-budget]]
== 예산 설정

온보딩 과정에서 사용자의 월별/일일/식사별 예산을 설정합니다.

=== 요청
include::{snippets}/onboarding-budget-set-success/http-request.adoc[]

=== 성공 응답 (201 Created)
include::{snippets}/onboarding-budget-set-success/http-response.adoc[]

=== 에러 응답
==== 필수 필드 누락 (422 Unprocessable Entity)
include::{snippets}/onboarding-budget-set-validation-error/http-response.adoc[]

==== JWT 토큰 누락 (400 Bad Request)
include::{snippets}/onboarding-budget-set-auth-error/http-response.adoc[]

=== 예제
==== cURL
[source,bash]
----
curl -X POST https://api.smartmealtable.com/api/v1/onboarding/budget \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "monthlyBudget": 300000,
    "dailyBudget": 10000,
    "mealBudgets": {
      "BREAKFAST": 3000,
      "LUNCH": 5000,
      "DINNER": 7000
    }
  }'
----
```

#### 6.3 HTML 문서 생성
**실행 명령**:
```bash
./gradlew :smartmealtable-api:asciidoctor
# BUILD SUCCESSFUL in 2m 27s
```

**생성된 파일**:
- `smartmealtable-api/build/docs/asciidoc/index.html`

---

## 🔍 주요 기술 이슈 및 해결 방법

### Issue 1: SnippetException - Fields not found in payload

**문제 상황**:
```
org.springframework.restdocs.snippet.SnippetException: 
The following parts of the payload were not documented:
{
  "error" : null
}
```

**원인**:
- `ApiResponse` 클래스에 `@JsonInclude(NON_NULL)` 적용
- null 필드가 JSON에서 제외되어 RestDocs가 필드를 찾지 못함

**해결 방법**:
```java
// 성공 응답에서 error 필드
fieldWithPath("error").type(JsonFieldType.NULL).optional()
    .description("에러 정보 (성공 시 null, @JsonInclude(NON_NULL)로 제외될 수 있음)")

// 에러 응답에서 data 필드
fieldWithPath("data").type(JsonFieldType.NULL).optional()
    .description("응답 데이터 (에러 시 null, @JsonInclude(NON_NULL)로 제외될 수 있음)")
```

**핵심**: `.optional()` 메서드를 추가하여 필드가 없을 수 있음을 명시

---

### Issue 2: Domain Entity ID가 null로 반환되는 문제

**문제 상황**:
```java
MonthlyBudget savedBudget = monthlyBudgetRepository.save(budget);
// savedBudget.getMonthlyBudgetId() == null 😱
```

**원인**:
- JPA Entity에서 Domain Entity로 변환 시 `create()` 팩토리 메서드 사용
- `create()`는 새 객체를 생성하므로 ID가 설정되지 않음

**기존 코드** (문제):
```java
// MonthlyBudgetJpaEntity.java
public MonthlyBudget toDomain() {
    return MonthlyBudget.create(
        this.memberId,
        this.monthlyFoodBudget,
        this.budgetMonth
    );  // ID가 null로 초기화됨
}
```

**해결 방법**: reconstitute 패턴 도입

```java
// 1. Domain Entity에 reconstitute 팩토리 메서드 추가
public static MonthlyBudget reconstitute(
    Long monthlyBudgetId,  // ID를 파라미터로 받음
    Long memberId,
    Integer monthlyFoodBudget,
    Integer monthlyUsedAmount,
    YearMonth budgetMonth
) {
    MonthlyBudget budget = new MonthlyBudget();
    budget.monthlyBudgetId = monthlyBudgetId;  // ID 복원
    budget.memberId = memberId;
    budget.monthlyFoodBudget = monthlyFoodBudget;
    budget.monthlyUsedAmount = monthlyUsedAmount;
    budget.budgetMonth = budgetMonth;
    return budget;
}

// 2. JpaEntity의 toDomain()에서 reconstitute 사용
public MonthlyBudget toDomain() {
    return MonthlyBudget.reconstitute(
        this.monthlyBudgetId,  // ID 전달
        this.memberId,
        this.monthlyFoodBudget,
        this.monthlyUsedAmount,
        this.budgetMonth
    );
}
```

**결과**:
- ✅ DB 저장 후 ID가 정상적으로 반환됨
- ✅ Domain Entity와 JPA Entity 간 변환 시 ID 보존
- ✅ 모든 통합 테스트 통과

---

### Issue 3: Validation Error Response 필드 문서화

**문제 상황**:
```
SnippetException: The following parts of the payload were not documented:
{
  "error" : {
    "data" : {
      "field" : "monthlyBudget",
      "reason" : "월별 예산은 필수입니다."
    }
  }
}
```

**원인**:
- `error.data` 객체 내부의 `field`, `reason` 필드 미문서화

**해결 방법**:
```java
responseFields(
    fieldWithPath("result").type(JsonFieldType.STRING)
        .description("응답 결과 (ERROR)"),
    fieldWithPath("data").type(JsonFieldType.NULL).optional()
        .description("응답 데이터 (에러 시 null, @JsonInclude(NON_NULL)로 제외될 수 있음)"),
    fieldWithPath("error").type(JsonFieldType.OBJECT)
        .description("에러 정보"),
    fieldWithPath("error.code").type(JsonFieldType.STRING)
        .description("에러 코드 (E422)"),
    fieldWithPath("error.message").type(JsonFieldType.STRING)
        .description("에러 메시지"),
    fieldWithPath("error.data").type(JsonFieldType.OBJECT).optional()
        .description("상세 에러 정보 (필드별 검증 오류)"),
    // 🔥 중첩 필드 문서화
    fieldWithPath("error.data.field").type(JsonFieldType.STRING).optional()
        .description("검증 실패한 필드명"),
    fieldWithPath("error.data.reason").type(JsonFieldType.STRING).optional()
        .description("검증 실패 사유")
)
```

---

## 📚 아키텍처 패턴

### 1. reconstitute 패턴

**목적**: JPA Entity → Domain Entity 변환 시 ID 보존

**구현**:
```java
// Domain Entity
public static MonthlyBudget reconstitute(
    Long monthlyBudgetId,  // 저장된 ID
    Long memberId,
    Integer monthlyFoodBudget,
    Integer monthlyUsedAmount,
    YearMonth budgetMonth
) {
    MonthlyBudget budget = new MonthlyBudget();
    budget.monthlyBudgetId = monthlyBudgetId;  // ID 복원
    budget.memberId = memberId;
    budget.monthlyFoodBudget = monthlyFoodBudget;
    budget.monthlyUsedAmount = monthlyUsedAmount;
    budget.budgetMonth = budgetMonth;
    return budget;
}

// JPA Entity
public MonthlyBudget toDomain() {
    return MonthlyBudget.reconstitute(
        this.monthlyBudgetId,
        this.memberId,
        this.monthlyFoodBudget,
        this.monthlyUsedAmount,
        this.budgetMonth
    );
}
```

**장점**:
- ID가 보존되어 영속성 컨텍스트 추적 가능
- Domain Entity가 JPA에 의존하지 않음
- `create()`와 `reconstitute()` 의도 명확히 분리

---

### 2. Domain-Driven Design

**계층 분리**:
1. **Domain Layer**: 비즈니스 로직 (`MonthlyBudget.changeMonthlyFoodBudget()`)
2. **Storage Layer**: 영속성 처리 (`MonthlyBudgetRepositoryImpl`)
3. **Service Layer**: 유즈케이스 orchestration (`SetBudgetService`)
4. **Controller Layer**: HTTP 요청/응답 처리 (`OnboardingController`)

**의존성 방향**:
```
Controller → Service → Domain → Storage (Interface)
                                  ↑
                            Storage (Implementation)
```

---

## 📊 최종 결과

### 통합 테스트 결과
```bash
./gradlew :smartmealtable-api:test --tests SetBudgetControllerTest
# ✅ BUILD SUCCESSFUL in 15s
# ✅ 6 tests completed, 0 failed
```

### RestDocs 테스트 결과
```bash
./gradlew :smartmealtable-api:test --tests SetBudgetControllerRestDocsTest
# ✅ BUILD SUCCESSFUL in 11s
# ✅ 3 tests completed, 0 failed
```

### HTML 문서 생성 결과
```bash
./gradlew :smartmealtable-api:asciidoctor
# ✅ BUILD SUCCESSFUL in 2m 27s
# ✅ Generated: build/docs/asciidoc/index.html
```

### 전체 API 테스트 결과
```bash
./gradlew :smartmealtable-api:test
# ✅ 모든 온보딩 API 테스트 통과
#    - 프로필 설정 (6 tests)
#    - 주소 등록 (6 tests)
#    - 예산 설정 (6 tests + 3 RestDocs tests)
# ✅ Total: 21 tests completed, 0 failed
```

---

## 🎯 다음 단계

### 온보딩 API 완료 현황
- ✅ 프로필 설정 (닉네임, 그룹) - 100%
- ✅ 주소 등록 (집, 회사, 기타) - 100%
- ✅ 예산 설정 (월별/일일/식사별) - 100%

### 다음 구현 예정
**온보딩 API 추가 기능**:
1. ⏳ 취향 설정 API (음식 카테고리 선호도)
2. ⏳ 약관 동의 API (개인정보, 서비스 이용약관)

**예산 관리 API**:
1. ⏳ 예산 조회 API (GET /api/v1/budgets)
2. ⏳ 예산 수정 API (PUT /api/v1/budgets/{id})
3. ⏳ 예산 삭제 API (DELETE /api/v1/budgets/{id})
4. ⏳ 예산 통계 API (GET /api/v1/budgets/statistics)

---

## 📝 교훈 및 Best Practices

### 1. RestDocs 필드 문서화
- `@JsonInclude(NON_NULL)` 사용 시 `.optional()` 필수
- 중첩 객체의 모든 필드 문서화 필요
- 에러 응답도 동일하게 상세 문서화

### 2. Domain Entity ID 관리
- `create()`: 새 객체 생성 (ID = null)
- `reconstitute()`: 기존 객체 복원 (ID 보존)
- JPA Entity → Domain 변환 시 reconstitute 사용

### 3. TDD 방식의 효과
- 통합 테스트 먼저 작성 → 요구사항 명확화
- RestDocs 테스트로 문서화 자동화
- 리팩토링 시 안전망 역할

### 4. 계층 간 책임 분리
- Domain: 비즈니스 로직
- Storage: 영속성 처리
- Service: 유즈케이스 orchestration
- Controller: HTTP 처리

---

## 🏆 성과

✅ **Domain Layer**: 3개 엔티티 + 3개 Repository  
✅ **Storage Layer**: 3개 JPA 엔티티 + 3개 구현체  
✅ **Service Layer**: 1개 Service + 2개 DTO  
✅ **Controller Layer**: 1개 Endpoint + 2개 DTO  
✅ **통합 테스트**: 6개 테스트 (100% 통과)  
✅ **RestDocs 문서화**: 3개 시나리오 (100% 통과)  
✅ **HTML 문서**: index.html 생성 완료  

**총 작업 파일**: 20개  
**총 테스트 케이스**: 9개 (6 Integration + 3 RestDocs)  
**테스트 통과율**: 100% (9/9)  
**문서화 완성도**: 100%  

---

## 📌 관련 문서

- [IMPLEMENTATION_PROGRESS.md](./IMPLEMENTATION_PROGRESS.md) - 전체 구현 진행상황
- [ONBOARDING_REST_DOCS_COMPLETION_REPORT.md](./ONBOARDING_REST_DOCS_COMPLETION_REPORT.md) - 온보딩 프로필/주소 API 문서화 보고서
- [API Specification](./API_SPECIFICATION.md) - API 스펙 정의
- [DDL](./ddl.sql) - 데이터베이스 스키마

---

**작성일**: 2025-10-10  
**작성자**: GitHub Copilot  
**프로젝트**: SmartMealTable SpringBoot Backend V2
