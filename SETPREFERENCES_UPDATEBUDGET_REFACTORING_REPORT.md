# SetPreferencesService 및 UpdateBudgetService 리팩토링 보고서

**작업일**: 2025-10-11  
**작업자**: GitHub Copilot  
**목적**: Domain Service 분리 패턴 적용을 통한 코드 품질 향상

---

## 📋 목차

1. [개요](#개요)
2. [리팩토링 완료 항목](#리팩토링-완료-항목)
3. [변경 사항 상세](#변경-사항-상세)
4. [성과 및 효과](#성과-및-효과)
5. [다음 단계](#다음-단계)

---

## 개요

### 배경

REFACTORING_CANDIDATE_ANALYSIS.md에서 분석한 결과를 바탕으로 우선순위가 높은 2개의 Application Service를 리팩토링했습니다:

1. **SetPreferencesService** (우선순위 1위)
2. **UpdateBudgetService** (우선순위 2위)

### 목표

- Domain Service 분리를 통한 비즈니스 로직 재사용성 향상
- Application Service의 orchestration 역할 명확화
- 코드량 감소 및 유지보수성 개선

---

## 리팩토링 완료 항목

### 1. PreferenceDomainService 신규 생성 ✅

**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/preference/service/PreferenceDomainService.java`

**책임**:
- 선호도 재설정 (기존 선호도 삭제 후 새로운 선호도 생성)
- 선호도 일괄 생성
- 카테고리 존재 여부 검증

**주요 메서드**:
```java
// 기존 선호도 삭제 후 새로운 선호도 설정
public List<Preference> resetPreferences(Long memberId, List<PreferenceItem> items);

// 선호도 일괄 생성
public List<Preference> createPreferences(Long memberId, List<PreferenceItem> items);

// 카테고리 검증
public void validateCategories(List<Long> categoryIds);
```

**사용된 패턴**:
- Record 타입 DTO (`PreferenceItem`)
- Stream API를 활용한 함수형 프로그래밍

---

### 2. ProfileDomainService 확장 ✅

**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/member/service/ProfileDomainService.java`

**추가된 메서드**:
```java
/**
 * 추천 유형 업데이트
 */
public Member updateRecommendationType(Long memberId, RecommendationType recommendationType);
```

**목적**:
- SetPreferencesService에서 추천 유형 업데이트 로직 재사용
- 도메인 로직 중앙화

---

### 3. SetPreferencesService 리팩토링 ✅

**Before (79줄)**:
```java
@Service
@RequiredArgsConstructor
@Transactional
public class SetPreferencesService {
    private final MemberRepository memberRepository;
    private final PreferenceRepository preferenceRepository;
    private final CategoryRepository categoryRepository;

    public SetPreferencesServiceResponse setPreferences(Long memberId, SetPreferencesServiceRequest request) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 추천 유형 업데이트
        member.changeRecommendationType(recommendationType);
        memberRepository.save(member);

        // 3. 기존 선호도 삭제
        preferenceRepository.deleteByMemberId(memberId);

        // 4. 새로운 선호도 저장 (카테고리 검증 포함)
        // ... 복잡한 로직 ...

        // 5. 응답 생성
        // ...
    }
}
```

**After (66줄, -16% 감소)**:
```java
@Service
@RequiredArgsConstructor
@Transactional
public class SetPreferencesService {
    private final ProfileDomainService profileDomainService;
    private final PreferenceDomainService preferenceDomainService;
    private final CategoryRepository categoryRepository;

    public SetPreferencesServiceResponse setPreferences(Long memberId, SetPreferencesServiceRequest request) {
        // 1. 추천 유형 업데이트 (ProfileDomainService)
        RecommendationType recommendationType = request.getRecommendationType();
        profileDomainService.updateRecommendationType(memberId, recommendationType);

        // 2. 선호도 재설정 (PreferenceDomainService)
        List<PreferenceDomainService.PreferenceItem> preferenceItems = request.getPreferences().stream()
                .map(item -> new PreferenceDomainService.PreferenceItem(item.getCategoryId(), item.getWeight()))
                .toList();
        List<Preference> preferences = preferenceDomainService.resetPreferences(memberId, preferenceItems);

        // 3. 응답 생성 (orchestration)
        return buildResponse(recommendationType, preferences);
    }
}
```

**개선 사항**:
- ✅ Repository 직접 의존 제거 (MemberRepository, PreferenceRepository)
- ✅ 비즈니스 로직을 Domain Service로 위임
- ✅ Orchestration 역할에만 집중
- ✅ 코드량 16% 감소 (79줄 → 66줄)

---

### 4. BudgetDomainService 확장 ✅

**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/budget/service/BudgetDomainService.java`

**추가된 메서드**:
```java
/**
 * 월별 예산 수정
 */
public MonthlyBudget updateMonthlyBudget(Long memberId, String month, Integer newAmount);

/**
 * 특정 월의 모든 일일 예산 일괄 수정
 */
public List<DailyBudget> updateDailyBudgetsInMonth(Long memberId, String month, Integer newAmount);
```

**특징**:
- 신규 Domain Service 생성이 아닌 기존 BudgetDomainService 확장
- 일관성 유지 및 효율성 개선

---

### 5. UpdateBudgetService 리팩토링 ✅

**Before (63줄)**:
```java
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateBudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final DailyBudgetRepository dailyBudgetRepository;

    public UpdateBudgetServiceResponse updateBudget(Long memberId, UpdateBudgetServiceRequest request) {
        // 1. 현재 월의 예산 조회
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByMemberIdAndBudgetMonth(...)
                .orElseThrow(...);

        // 2. 월별 예산 금액 수정
        monthlyBudget.changeMonthlyFoodBudget(request.getMonthlyFoodBudget());
        monthlyBudgetRepository.save(monthlyBudget);

        // 3. 해당 월의 모든 일일 예산 금액 수정
        LocalDate startOfMonth = ...;
        LocalDate endOfMonth = ...;
        List<DailyBudget> dailyBudgets = dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(...);
        for (DailyBudget dailyBudget : dailyBudgets) {
            if (!dailyBudget.getBudgetDate().isAfter(endOfMonth)) {
                dailyBudget.changeDailyFoodBudget(request.getDailyFoodBudget());
                dailyBudgetRepository.save(dailyBudget);
            }
        }

        // 4. 응답 생성
        return new UpdateBudgetServiceResponse(...);
    }
}
```

**After (51줄, -19% 감소)**:
```java
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateBudgetService {
    private final BudgetDomainService budgetDomainService;

    public UpdateBudgetServiceResponse updateBudget(Long memberId, UpdateBudgetServiceRequest request) {
        String currentMonth = YearMonth.now().toString();

        // 1. 월별 예산 수정 (BudgetDomainService)
        MonthlyBudget monthlyBudget = budgetDomainService.updateMonthlyBudget(
                memberId,
                currentMonth,
                request.getMonthlyFoodBudget()
        );

        // 2. 일일 예산 일괄 수정 (BudgetDomainService)
        budgetDomainService.updateDailyBudgetsInMonth(
                memberId,
                currentMonth,
                request.getDailyFoodBudget()
        );

        // 3. 응답 생성
        return new UpdateBudgetServiceResponse(
                monthlyBudget.getMonthlyBudgetId(),
                monthlyBudget.getMonthlyFoodBudget(),
                request.getDailyFoodBudget(),
                monthlyBudget.getBudgetMonth()
        );
    }
}
```

**개선 사항**:
- ✅ Repository 직접 의존 제거 (MonthlyBudgetRepository, DailyBudgetRepository)
- ✅ 비즈니스 로직을 BudgetDomainService로 위임
- ✅ 기간 계산 로직 Domain Service로 이동
- ✅ 코드량 19% 감소 (63줄 → 51줄)

---

## 성과 및 효과

### 코드량 감소

| Service | Before | After | 감소율 |
|---------|--------|-------|--------|
| SetPreferencesService | 79줄 | 66줄 | **-16%** |
| UpdateBudgetService | 63줄 | 51줄 | **-19%** |
| **평균** | **71줄** | **58.5줄** | **-17.6%** |

### 책임 명확화

**Before**:
- Application Service가 비즈니스 로직과 orchestration을 모두 담당
- Repository에 직접 의존하여 영속성 계층과 강하게 결합
- 검증 로직, 도메인 로직, 영속성 로직이 혼재

**After**:
- **Application Service**: Orchestration에만 집중
- **Domain Service**: 비즈니스 로직 전담
- **Repository**: Domain Service에서만 접근
- 각 계층의 역할이 명확히 구분됨

### 재사용성 향상

**PreferenceDomainService**:
- 향후 프로필 설정의 선호도 수정 API에서 재사용 가능
- 다른 온보딩 플로우에서도 활용 가능

**ProfileDomainService.updateRecommendationType()**:
- SetPreferencesService에서 사용
- 향후 추천 유형 변경 API에서도 재사용 가능

**BudgetDomainService 확장**:
- 월별/일일 예산 수정 로직 중앙화
- 예산 관련 다른 API에서도 재사용 가능

### 테스트 용이성 개선

**Application Service 테스트**:
- Domain Service Mock만으로 테스트 가능
- 복잡한 Repository Mock 불필요

**Domain Service 테스트**:
- 순수 비즈니스 로직 단위 테스트 가능
- 테스트 범위와 책임 명확화

---

## 다음 단계

### 1. 테스트 검증 ⏳

- [ ] 전체 API 테스트 실행 (현재 진행 중)
- [ ] 151+ 테스트 모두 통과 확인
- [ ] BUILD SUCCESSFUL 검증

### 2. 문서 업데이트 📄

- [ ] APPLICATION_DOMAIN_SERVICE_REFACTORING_REPORT.md 업데이트
  - SetPreferencesService 리팩토링 내역 추가
  - UpdateBudgetService 리팩토링 내역 추가
  - PreferenceDomainService 신규 생성 내역 추가
  
- [ ] IMPLEMENTATION_PROGRESS.md 업데이트
  - 추가 리팩토링 완료 내역 반영
  
- [ ] REFACTORING_CANDIDATE_ANALYSIS.md 업데이트
  - 완료 항목 체크 표시

### 3. 추가 리팩토링 검토 🔍

**PolicyAgreementService**:
- 현재 보류 상태 (재사용 가능성 낮음)
- 필요 시 향후 검토

**기타 후보**:
- Query Service들 (MonthlyBudgetQueryService, DailyBudgetQueryService)
- 추가 분석 필요

---

## 결론

이번 리팩토링을 통해 **SetPreferencesService**와 **UpdateBudgetService**의 코드 품질을 크게 향상시켰습니다.

**주요 성과**:
- ✅ **PreferenceDomainService 신규 생성** (선호도 관련 비즈니스 로직 중앙화)
- ✅ **ProfileDomainService 확장** (추천 유형 업데이트 메서드 추가)
- ✅ **BudgetDomainService 확장** (예산 수정 메서드 추가)
- ✅ **2개 Application Service 리팩토링 완료**
- ✅ **평균 코드량 17.6% 감소**
- ✅ **책임 명확화 및 재사용성 향상**

이제 **7개의 Application Service**가 Domain Service 패턴을 적용하여 리팩토링 완료되었습니다:
1. MemberProfileService → ProfileDomainService
2. AddressService → AddressDomainService
3. OnboardingProfileService → ProfileDomainService
4. SetBudgetService → BudgetDomainService
5. CreateExpenditureService → ExpenditureDomainService
6. **SetPreferencesService → PreferenceDomainService + ProfileDomainService** (NEW)
7. **UpdateBudgetService → BudgetDomainService** (NEW)

향후 모든 테스트가 통과하면 문서를 업데이트하고, 필요 시 추가 리팩토링을 진행할 예정입니다.

---

**작성일**: 2025-10-11  
**작성자**: GitHub Copilot  
**버전**: 1.0
