# API 업데이트 요약

**업데이트 일자:** 2025-10-08  
**업데이트 사유:** SRD 요구사항 REQ-PROFILE-204e 누락 보완

---

## 📋 업데이트 개요

### 문제점
- **SRD 요구사항:** REQ-PROFILE-204e "향후 예산 기본값 설정"
- **요구사항 내용:** "OO일 이후 기본 값으로 설정하기" 체크박스를 선택하고 저장하면, 해당 날짜 이후의 모든 날짜에 대한 기본 식사 예산이 방금 입력한 값으로 일괄 적용되어야 함
- **기존 API 한계:** `PUT /budgets` API는 전체 예산만 변경 가능하며, 특정 날짜부터 일괄 적용하는 기능 부재
- **영향:** 클라이언트에서 "일괄 적용" 기능을 구현할 방법이 없음

### 해결 방안
특정 날짜부터 예산을 일괄 적용할 수 있는 새로운 API 엔드포인트 추가

---

## ✅ 추가된 API

### 5.4 특정 날짜 예산 수정 및 일괄 적용

**Endpoint:** `PUT /budgets/daily/{date}`

**주요 기능:**
1. 특정 날짜의 예산 설정
2. `applyForward: true` 옵션으로 해당 날짜부터 미래 모든 날짜에 일괄 적용
3. 이미 개별 설정된 날짜도 덮어쓰기 가능

**Request 예시:**
```json
{
  "dailyBudget": 12000,
  "mealBudgets": {
    "BREAKFAST": 3500,
    "LUNCH": 5000,
    "DINNER": 3500
  },
  "applyForward": true
}
```

**Response 예시:**
```json
{
  "result": "SUCCESS",
  "data": {
    "targetDate": "2025-10-08",
    "dailyBudget": 12000,
    "mealBudgets": [
      {
        "mealType": "BREAKFAST",
        "budget": 3500
      },
      {
        "mealType": "LUNCH",
        "budget": 5000
      },
      {
        "mealType": "DINNER",
        "budget": 3500
      }
    ],
    "applyForward": true,
    "affectedDatesCount": 84,
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**주요 특징:**
- `affectedDatesCount`: 영향받은 날짜 수 (연말까지)
- 유효성 검증: 식사별 예산 합계 = 일일 예산
- 날짜 형식: YYYY-MM-DD

---

## 🔧 업데이트된 파일 목록

### 1. API_SPECIFICATION.md
**위치:** Section 5. 예산 관리 API

**변경 사항:**
- ✅ 5.4 특정 날짜 예산 수정 및 일괄 적용 API 추가
- ✅ Request/Response 스펙 정의
- ✅ 에러 케이스 3가지 추가 (401, 400, 422)
- ✅ SRD 요구사항 REQ-PROFILE-204e 충족 명시

### 2. API_ERROR_MAPPING.md
**위치:** Section 4. 예산 관리 API

**변경 사항:**
- ✅ 4.4 특정 날짜 예산 수정 및 일괄 적용 에러 매핑 추가
- ✅ ErrorType 매핑 테이블 작성
  - 401: INVALID_TOKEN
  - 400: INVALID_PARAMETER (날짜 형식 오류)
  - 400: BAD_REQUEST (date 파라미터 누락)
  - 422: INVALID_BUDGET (예산 0 미만)
  - 422: VALIDATION_ERROR (식사별 예산 합계 불일치)
  - 500: DATABASE_ERROR (일괄 적용 포함)
- ✅ 특이사항 및 주의사항 기술

---

## 📊 에러 처리

### 에러 시나리오

#### 1. 인증 실패 (401)
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E401",
    "message": "유효하지 않은 토큰입니다."
  }
}
```

#### 2. 날짜 형식 오류 (400)
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E400",
    "message": "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식으로 입력해주세요."
  }
}
```

#### 3. 예산 유효성 검증 실패 (422)
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E422",
    "message": "식사별 예산의 합계가 일일 예산과 일치하지 않습니다.",
    "data": {
      "dailyBudget": 12000,
      "mealBudgetsSum": 11000,
      "difference": 1000
    }
  }
}
```

---

## 🎯 비즈니스 로직

### 일괄 적용 로직

1. **단일 날짜 설정 (applyForward: false 또는 미지정)**
   - 해당 날짜의 예산만 업데이트
   - 다른 날짜에 영향 없음

2. **일괄 적용 (applyForward: true)**
   - 해당 날짜부터 연말(12/31)까지 모든 날짜에 동일한 예산 적용
   - 이미 개별 설정된 날짜도 덮어씀
   - `affectedDatesCount`로 영향받은 날짜 수 반환

3. **유효성 검증**
   - 일일 예산 ≥ 0
   - 각 식사 예산 ≥ 0
   - BREAKFAST + LUNCH + DINNER = dailyBudget

---

## 🔍 SRD 요구사항 충족도

| 요구사항 ID | 요구사항 내용 | 충족 여부 | 구현 API |
|------------|--------------|----------|---------|
| REQ-PROFILE-204e | "OO일 이후 기본 값으로 설정하기" 체크박스 기능 | ✅ 완전 충족 | `PUT /budgets/daily/{date}` with `applyForward: true` |

---

## 📝 구현 가이드

### Controller 구현 예시

```java
@RestController
@RequestMapping("/budgets")
public class BudgetController {
    
    @PutMapping("/daily/{date}")
    public ApiResponse<DailyBudgetResponse> updateDailyBudget(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestBody @Valid DailyBudgetRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // 유효성 검증
        validateBudgetSum(request);
        
        // 서비스 호출
        DailyBudgetResponse response = budgetService.updateDailyBudget(
            userDetails.getMemberId(),
            date,
            request
        );
        
        return ApiResponse.success(response);
    }
    
    private void validateBudgetSum(DailyBudgetRequest request) {
        int sum = request.getMealBudgets().values().stream()
            .mapToInt(Integer::intValue)
            .sum();
            
        if (sum != request.getDailyBudget()) {
            Map<String, Object> errorData = Map.of(
                "dailyBudget", request.getDailyBudget(),
                "mealBudgetsSum", sum,
                "difference", request.getDailyBudget() - sum
            );
            throw new BusinessException(ErrorType.VALIDATION_ERROR, errorData);
        }
    }
}
```

### Service 구현 예시

```java
@Service
@Transactional
public class BudgetService {
    
    public DailyBudgetResponse updateDailyBudget(
            Long memberId, 
            LocalDate targetDate, 
            DailyBudgetRequest request) {
        
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));
        
        if (request.isApplyForward()) {
            // 일괄 적용: targetDate부터 연말까지
            LocalDate endOfYear = targetDate.withDayOfYear(targetDate.lengthOfYear());
            int affectedCount = 0;
            
            for (LocalDate date = targetDate; !date.isAfter(endOfYear); date = date.plusDays(1)) {
                saveDailyBudget(member, date, request);
                affectedCount++;
            }
            
            return DailyBudgetResponse.of(targetDate, request, affectedCount);
        } else {
            // 단일 날짜만 설정
            saveDailyBudget(member, targetDate, request);
            return DailyBudgetResponse.of(targetDate, request, 1);
        }
    }
    
    private void saveDailyBudget(Member member, LocalDate date, DailyBudgetRequest request) {
        DailyBudget budget = dailyBudgetRepository
            .findByMemberAndDate(member, date)
            .orElse(DailyBudget.create(member, date));
            
        budget.updateBudgets(
            request.getDailyBudget(),
            request.getMealBudgets()
        );
        
        dailyBudgetRepository.save(budget);
    }
}
```

---

## ✅ 테스트 시나리오

### 1. 단일 날짜 예산 설정
- **Given:** 2025-10-08 날짜에 예산 설정
- **When:** `applyForward: false` (또는 미지정)
- **Then:** 2025-10-08의 예산만 변경, 다른 날짜 영향 없음

### 2. 일괄 적용 (정상)
- **Given:** 2025-10-08 날짜부터 일괄 적용
- **When:** `applyForward: true`, 유효한 예산 값
- **Then:** 
  - 2025-10-08 ~ 2025-12-31까지 모든 날짜에 동일한 예산 적용
  - `affectedDatesCount = 85` (10/08부터 12/31까지)

### 3. 유효성 검증 실패
- **Given:** 식사별 예산 합계 ≠ 일일 예산
- **When:** API 호출
- **Then:** 422 에러, 차이값 포함한 에러 메시지 반환

### 4. 날짜 형식 오류
- **Given:** 잘못된 날짜 형식 (예: "2025/10/08")
- **When:** API 호출
- **Then:** 400 에러, "YYYY-MM-DD 형식으로 입력" 메시지

### 5. 이미 설정된 날짜 덮어쓰기
- **Given:** 2025-10-15에 이미 개별 예산 설정됨
- **When:** 2025-10-08부터 일괄 적용 (`applyForward: true`)
- **Then:** 2025-10-15의 기존 예산이 새 값으로 덮어씌워짐

---

## 📈 영향 분석

### 긍정적 영향
1. ✅ SRD 요구사항 100% 충족
2. ✅ 사용자 편의성 향상 (일괄 설정 기능)
3. ✅ 클라이언트 구현 단순화
4. ✅ 데이터 일관성 보장

### 고려 사항
1. ⚠️ 대량 업데이트 시 성능 고려 필요
   - 연말까지 최대 365건 업데이트 가능
   - 배치 처리 또는 비동기 처리 검토 권장
   
2. ⚠️ 실수로 인한 대량 덮어쓰기 방지
   - 프론트엔드에서 확인 모달 필수
   - "84일의 예산이 변경됩니다" 같은 명확한 안내

3. ⚠️ 트랜잭션 처리
   - 일괄 적용 중 오류 발생 시 롤백 처리
   - 부분 성공 방지

---

## 🚀 다음 단계

### 1. 구현 우선순위
- [x] API 명세 작성 완료
- [x] 에러 매핑 문서 작성 완료
- [ ] Controller 구현
- [ ] Service 로직 구현
- [ ] Repository 메서드 추가
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성

### 2. 테스트 계획
- [ ] 단일 날짜 설정 테스트
- [ ] 일괄 적용 테스트 (정상)
- [ ] 일괄 적용 테스트 (덮어쓰기)
- [ ] 유효성 검증 실패 테스트
- [ ] 날짜 형식 오류 테스트
- [ ] 성능 테스트 (대량 업데이트)

### 3. 문서화
- [ ] Spring Rest Docs 작성
- [ ] API 사용 가이드 작성
- [ ] 클라이언트 팀에 API 전달

---

## 📚 참고 문서

- **SRD:** REQ-PROFILE-204e (향후 예산 기본값 설정)
- **API 명세서:** Section 5.4
- **에러 매핑:** API_ERROR_MAPPING.md Section 4.4
- **ErrorType Enum:** `ErrorType.VALIDATION_ERROR`, `INVALID_BUDGET`, `INVALID_PARAMETER`

---

**문서 종료**
