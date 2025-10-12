# 남은 REST Docs 작업 상세 계획

**문서 작성일:** 2025-10-12  
**현재 완료 상태:** 13개 파일, 49개 테스트 케이스 완료
**남은 작업:** 8개 Controller

---

## 📋 작업 우선순위 및 개요

### P1 - 높은 우선순위 (ArgumentResolver 인프라 구축 후)
1. **PreferenceController** - X-Member-Id 헤더 사용 (즉시 작업 가능)
2. **BudgetController** - JWT 토큰 필요, 응답 구조 수정 필요

### P2 - 중간 우선순위 (JWT 인증 확인됨)
3. ~~**PasswordExpiryController**~~ - ✅ 완료 (2025-10-12)
4. **AddressController** - 주소 관리 CRUD
5. **SocialAccountController** - 소셜 계정 연동/해제

### P3 - 낮은 우선순위
6. **ExpenditureController** - 지출 내역 관리
7. **PolicyController** - 약관 관리
8. **CategoryController** - 카테고리 조회
9. **GroupController** - 그룹 관리

---

## ✅ 완료된 작업

### PasswordExpiryController REST Docs (2025-10-12)
- **파일:** `PasswordExpiryControllerRestDocsTest.java`
- **테스트 케이스:** 4개 (모두 통과)
  - 비밀번호 만료 상태 조회 성공 (GET /api/v1/members/me/password/expiry-status)
  - 비밀번호 만료 상태 조회 실패 - 회원 없음 (404)
  - 비밀번호 만료일 연장 성공 (POST /api/v1/members/me/password/extend-expiry)
  - 비밀번호 만료일 연장 실패 - 회원 없음 (404)
- **인증 방식:** JWT Authorization Bearer Token
- **생성된 Snippet:** 4개 API 문서 (get-status-success, get-status-not-found, extend-success, extend-not-found)
- **소요 시간:** 약 20분
- **주요 이슈:** X-Member-Id 헤더 대신 Authorization Bearer Token 사용 필요

---

## 🔧 공통 인프라 작업

### ArgumentResolver 테스트 환경 구축
**필요성:** 대부분의 Controller가 `@AuthUser` 사용  
**예상 소요 시간:** 2-3시간

#### 작업 내용
1. **JWT 토큰 생성 헬퍼 메서드 작성**
   ```java
   // AbstractRestDocsTest에 추가
   protected String createAccessToken(Long memberId) {
       return jwtTokenProvider.createToken(memberId);
   }
   ```

2. **@AuthUser ArgumentResolver Mock 설정**
   - 실제 JWT 토큰 검증 로직 활용
   - TestContainers 환경에서 동작 보장

3. **기존 UpdateBudgetControllerTest 패턴 재사용**
   - 이미 JWT 토큰 기반 테스트 성공 사례 존재
   - 패턴을 REST Docs 테스트에 적용

#### 참고 코드
```java
// UpdateBudgetControllerTest.java 참조
@Autowired
private JwtTokenProvider jwtTokenProvider;

@BeforeEach
void setUp() {
    // 회원 생성 후
    accessToken = jwtTokenProvider.createToken(member.getMemberId());
}

// 테스트에서
mockMvc.perform(put("/api/v1/budgets")
    .header("Authorization", "Bearer " + accessToken)
    ...
```

---

## 📝 Controller별 상세 작업 계획

### 1. PreferenceController ⭐ (우선 작업 가능)

**엔드포인트:**
- GET `/api/v1/members/me/preferences` - 선호도 조회
- PUT `/api/v1/members/me/preferences/categories` - 카테고리 선호도 수정
- POST `/api/v1/members/me/preferences/foods` - 음식 선호도 추가
- PUT `/api/v1/members/me/preferences/foods/{id}` - 음식 선호도 변경
- DELETE `/api/v1/members/me/preferences/foods/{id}` - 음식 선호도 삭제

**인증 방식:** `@RequestHeader("X-Member-Id")` ✅ 즉시 작업 가능

**예상 테스트 케이스:** 6-8개
1. ✅ 선호도 조회 성공
2. ✅ 선호도 조회 실패 - 회원 없음 (404)
3. ✅ 카테고리 선호도 수정 성공
4. ✅ 카테고리 선호도 수정 실패 - 유효성 검증 (422)
5. ✅ 음식 선호도 추가 성공 (201)
6. ✅ 음식 선호도 변경 성공
7. ✅ 음식 선호도 삭제 성공 (204)
8. ✅ 음식 선호도 삭제 실패 - 존재하지 않음 (404)

**예상 소요 시간:** 1시간

**참고 파일:**
- `PreferenceController.java`
- `GetPreferencesServiceResponse.java`
- `UpdateCategoryPreferencesServiceResponse.java`
- `AddFoodPreferenceServiceResponse.java`

---

### 2. BudgetController ⚠️ (응답 구조 수정 필요)

**엔드포인트:**
- GET `/api/v1/budgets/monthly?year={year}&month={month}` - 월별 예산 조회
- GET `/api/v1/budgets/daily?date={date}` - 일별 예산 조회
- PUT `/api/v1/budgets` - 월별 예산 수정
- PUT `/api/v1/budgets/daily/{date}` - 일별 예산 수정

**인증 방식:** `@AuthUser` ⚠️ ArgumentResolver 필요

**이슈:**
- 실제 응답 구조와 예상 응답 구조 불일치
- DailyBudgetQueryResponse: `totalBudget` (실제) vs `totalDailyBudget` (예상)
- UpdateBudgetResponse: `monthlyBudgetId`, `budgetMonth`, `message` (실제) vs `effectiveDate`, `daysAffected` (예상)

**해결 방안:**
1. 실제 응답 DTO 구조 정확히 파악
2. REST Docs 필드 정의 수정
3. 기존 BudgetControllerRestDocsTest.java 삭제 후 재작성

**예상 테스트 케이스:** 8-10개
1. ✅ 월별 예산 조회 성공
2. ✅ 일별 예산 조회 성공
3. ✅ 월별 예산 수정 성공
4. ✅ 일별 예산 수정 성공
5. ✅ 월별 예산 조회 실패 - 잘못된 파라미터 (422)
6. ✅ 일별 예산 조회 실패 - 예산 없음 (404)
7. ✅ 월별 예산 수정 실패 - 유효성 검증 (422)
8. ✅ 일별 예산 수정 실패 - 유효성 검증 (422)

**예상 소요 시간:** 1.5시간

**참고 파일:**
- `BudgetController.java`
- `MonthlyBudgetQueryResponse.java` (record 타입)
- `DailyBudgetQueryResponse.java` (record 타입)
- `UpdateBudgetResponse.java`
- `UpdateDailyBudgetResponse.java`
- `UpdateBudgetControllerTest.java` (JWT 토큰 패턴 참고)

---

### 3. PasswordExpiryController

**엔드포인트:**
- GET `/api/v1/members/me/password/expiry-status` - 비밀번호 만료 상태 조회
- POST `/api/v1/members/me/password/extend-expiry` - 비밀번호 만료일 연장

**인증 방식:** `@AuthUser` ⚠️ ArgumentResolver 필요

**예상 테스트 케이스:** 4-5개
1. ✅ 비밀번호 만료 상태 조회 성공
2. ✅ 비밀번호 만료일 연장 성공
3. ✅ 비밀번호 만료 상태 조회 실패 - 회원 없음 (404)
4. ✅ 비밀번호 만료일 연장 실패 - 이미 연장됨 (400)

**예상 소요 시간:** 40분

**참고 파일:**
- `PasswordExpiryController.java`
- `PasswordExpiryStatusResponse.java`
- `ExtendPasswordExpiryResponse.java`

**이미 작성된 파일:**
- `PasswordExpiryControllerRestDocsTest.java` (401 에러로 실패, ArgumentResolver 수정 필요)

---

### 4. AddressController

**엔드포인트:**
- GET `/api/v1/members/me/addresses` - 주소 목록 조회
- POST `/api/v1/members/me/addresses` - 주소 추가 (201)
- PUT `/api/v1/members/me/addresses/{id}` - 주소 수정
- DELETE `/api/v1/members/me/addresses/{id}` - 주소 삭제 (204)
- PUT `/api/v1/members/me/addresses/{id}/primary` - 기본 주소 설정

**인증 방식:** `@AuthUser` ⚠️ ArgumentResolver 필요

**예상 테스트 케이스:** 10-12개
1. ✅ 주소 목록 조회 성공
2. ✅ 주소 목록 조회 실패 - 회원 없음 (404)
3. ✅ 주소 추가 성공 (201)
4. ✅ 주소 추가 실패 - 유효성 검증 (422)
5. ✅ 주소 수정 성공
6. ✅ 주소 수정 실패 - 존재하지 않음 (404)
7. ✅ 주소 수정 실패 - 유효성 검증 (422)
8. ✅ 주소 삭제 성공 (204)
9. ✅ 주소 삭제 실패 - 존재하지 않음 (404)
10. ✅ 기본 주소 설정 성공
11. ✅ 기본 주소 설정 실패 - 존재하지 않음 (404)

**예상 소요 시간:** 1.5시간

**참고 파일:**
- `AddressController.java`
- `AddressResponse.java`
- `PrimaryAddressResponse.java`
- `AddressRequest.java`

---

### 5. SocialAccountController

**엔드포인트:**
- GET `/api/v1/members/me/social-accounts` - 연동된 소셜 계정 목록 조회
- POST `/api/v1/members/me/social-accounts` - 소셜 계정 추가 연동 (201)
- DELETE `/api/v1/members/me/social-accounts/{id}` - 소셜 계정 연동 해제 (204)

**인증 방식:** `@AuthUser` ⚠️ ArgumentResolver 필요

**추가 이슈:** OAuth 클라이언트 MockBean 설정 필요

**예상 테스트 케이스:** 6-8개
1. ✅ 소셜 계정 목록 조회 성공
2. ✅ 소셜 계정 추가 연동 성공 - 카카오 (201)
3. ✅ 소셜 계정 추가 연동 성공 - 구글 (201)
4. ✅ 소셜 계정 추가 연동 실패 - 이미 연동됨 (400)
5. ✅ 소셜 계정 추가 연동 실패 - OAuth 인증 실패 (401)
6. ✅ 소셜 계정 연동 해제 성공 (204)
7. ✅ 소셜 계정 연동 해제 실패 - 존재하지 않음 (404)
8. ✅ 소셜 계정 연동 해제 실패 - 마지막 로그인 수단 (400)

**예상 소요 시간:** 2시간

**참고 파일:**
- `SocialAccountController.java`
- `SocialAccountListServiceResponse.java`
- `AddSocialAccountServiceResponse.java`
- `ConnectedSocialAccountResponse.java`

---

### 6. ExpenditureController

**엔드포인트:**
- GET `/api/v1/expenditures` - 지출 내역 목록 조회 (필터링, 페이징)
- GET `/api/v1/expenditures/{id}` - 지출 내역 상세 조회
- POST `/api/v1/expenditures` - 지출 내역 등록 (201)
- PUT `/api/v1/expenditures/{id}` - 지출 내역 수정
- DELETE `/api/v1/expenditures/{id}` - 지출 내역 삭제 (204)
- GET `/api/v1/expenditures/statistics` - 지출 통계 조회

**인증 방식:** `@AuthUser` ⚠️ ArgumentResolver 필요

**예상 테스트 케이스:** 12-15개
1. ✅ 지출 내역 목록 조회 성공
2. ✅ 지출 내역 목록 조회 - 기간 필터링
3. ✅ 지출 내역 목록 조회 - 카테고리 필터링
4. ✅ 지출 내역 목록 조회 - 페이징
5. ✅ 지출 내역 상세 조회 성공
6. ✅ 지출 내역 상세 조회 실패 - 존재하지 않음 (404)
7. ✅ 지출 내역 등록 성공 (201)
8. ✅ 지출 내역 등록 실패 - 유효성 검증 (422)
9. ✅ 지출 내역 수정 성공
10. ✅ 지출 내역 수정 실패 - 존재하지 않음 (404)
11. ✅ 지출 내역 삭제 성공 (204)
12. ✅ 지출 내역 삭제 실패 - 존재하지 않음 (404)
13. ✅ 지출 통계 조회 성공

**예상 소요 시간:** 2-2.5시간

---

### 7. PolicyController

**엔드포인트:**
- GET `/api/v1/policies` - 약관 목록 조회
- GET `/api/v1/policies/{id}` - 약관 상세 조회
- POST `/api/v1/policies/{id}/agree` - 약관 동의

**인증 방식:** 조회는 인증 불필요, 동의는 `@AuthUser` 필요

**예상 테스트 케이스:** 4-5개
1. ✅ 약관 목록 조회 성공
2. ✅ 약관 상세 조회 성공
3. ✅ 약관 동의 성공
4. ✅ 약관 동의 실패 - 이미 동의함 (400)

**예상 소요 시간:** 40분

---

### 8. CategoryController

**엔드포인트:**
- GET `/api/v1/categories` - 카테고리 목록 조회
- GET `/api/v1/categories/{id}` - 카테고리 상세 조회

**인증 방식:** 인증 불필요 (공개 API)

**예상 테스트 케이스:** 3-4개
1. ✅ 카테고리 목록 조회 성공
2. ✅ 카테고리 상세 조회 성공
3. ✅ 카테고리 상세 조회 실패 - 존재하지 않음 (404)

**예상 소요 시간:** 30분

---

### 9. GroupController

**엔드포인트:**
- GET `/api/v1/groups` - 그룹 목록 조회
- GET `/api/v1/groups/{id}` - 그룹 상세 조회
- POST `/api/v1/groups` - 그룹 생성 (201)
- PUT `/api/v1/groups/{id}` - 그룹 수정
- DELETE `/api/v1/groups/{id}` - 그룹 삭제 (204)
- POST `/api/v1/groups/{id}/members` - 그룹 멤버 추가
- DELETE `/api/v1/groups/{id}/members/{memberId}` - 그룹 멤버 제거

**인증 방식:** `@AuthUser` ⚠️ ArgumentResolver 필요

**예상 테스트 케이스:** 12-15개

**예상 소요 시간:** 2-2.5시간

---

## 📊 전체 작업 예상 시간

### ArgumentResolver 인프라 구축
- 소요 시간: 2-3시간
- 완료 후 대부분의 Controller 작업 가능

### Controller별 소요 시간
| Controller | 예상 시간 | 우선순위 | 전제 조건 |
|-----------|----------|---------|----------|
| Preference | 1h | P1 | 없음 (즉시 가능) |
| Budget | 1.5h | P1 | ArgumentResolver |
| PasswordExpiry | 40m | P2 | ArgumentResolver |
| Address | 1.5h | P2 | ArgumentResolver |
| SocialAccount | 2h | P2 | ArgumentResolver + OAuth Mock |
| Expenditure | 2.5h | P3 | ArgumentResolver |
| Policy | 40m | P3 | ArgumentResolver (일부) |
| Category | 30m | P3 | 없음 |
| Group | 2.5h | P3 | ArgumentResolver |

**총 예상 시간:** 약 15-17시간

---

## 🎯 권장 작업 순서

### Phase 1: 즉시 착수 가능 (2시간)
1. PreferenceController REST Docs 작성 (1h)
2. CategoryController REST Docs 작성 (30m)
3. PolicyController REST Docs 작성 - 조회 기능만 (30m)

### Phase 2: ArgumentResolver 인프라 구축 (2-3시간)
1. JWT 토큰 기반 테스트 헬퍼 작성
2. AbstractRestDocsTest에 통합
3. 기존 UpdateBudgetControllerTest 패턴 적용

### Phase 3: 핵심 기능 REST Docs (5시간)
1. BudgetController (1.5h)
2. PasswordExpiryController (40m)
3. AddressController (1.5h)
4. PolicyController - 동의 기능 (20m)
5. SocialAccountController (2h)

### Phase 4: 부가 기능 REST Docs (5시간)
1. ExpenditureController (2.5h)
2. GroupController (2.5h)

**총 예상 시간:** 약 14-16시간 (3-4일 작업)

---

## 💡 효율화 전략

### 1. 패턴 재사용
- 기존 12개 REST Docs 테스트 패턴 활용
- 성공/실패/검증 시나리오 템플릿 재사용

### 2. 우선순위 기반 작업
- 즉시 가능한 작업 먼저 완료 (빠른 성과)
- ArgumentResolver 인프라는 한 번만 구축
- 핵심 기능 우선, 부가 기능 후순위

### 3. 병렬 작업 가능
- 인증 불필요 Controller (Preference, Category) 독립 작업
- ArgumentResolver 인프라와 병렬 진행 가능

---

**문서 작성일:** 2025-10-11  
**예상 완료 일정:** 2025-10-14 (3-4일 작업 기준)  
**참고:** 본 문서는 계획서이며, 실제 작업 시 우선순위 및 일정 조정 가능
