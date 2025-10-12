# 남은 REST Docs 작업 상세 계획

**문서 작성일:** 2025-10-12  
**현재 완료 상태:** 21개 파일, 100개 테스트 케이스 완료
**보류 작업:** 2개 Controller (기능 미완성으로 인한 보류)

---

## 📋 작업 우선순위 및 개요

### P1 - 높은 우선순위 ✅ 모두 완료
1. ~~**PreferenceController**~~ - ✅ 완료 (2025-10-12)
2. ~~**BudgetController**~~ - ✅ 완료 (2025-10-12)

### P2 - 중간 우선순위 ✅ 모두 완료
3. ~~**PasswordExpiryController**~~ - ✅ 완료 (2025-10-12)
4. ~~**AddressController**~~ - ✅ 완료 (2025-10-12)
5. ~~**SocialAccountController**~~ - ✅ 완료 (2025-10-12)

### P3 - 낮은 우선순위 ✅ 부분 완료
6. ~~**CategoryController**~~ - ✅ 완료 (2025-10-12)
7. ~~**PolicyController**~~ - ✅ 완료 (2025-10-12)
8. **ExpenditureController** - ⏸️ 보류 (등록 API만 구현됨, CRUD 전체 구현 후 작업)
9. **GroupController** - ⏸️ 보류 (검색 API만 구현됨, CRUD 전체 구현 후 작업)

---

## ✅ 완료된 작업

### PreferenceController REST Docs (2025-10-12)
- **파일:** `PreferenceControllerRestDocsTest.java`
- **테스트 케이스:** 6개 (모두 통과)
  - 선호도 조회 성공 (GET /api/v1/members/me/preferences)
  - 카테고리 선호도 수정 성공 (PUT /api/v1/members/me/preferences/categories)
  - 음식 선호도 추가 성공 (POST /api/v1/members/me/preferences/foods)
  - 음식 선호도 변경 성공 (PUT /api/v1/members/me/preferences/foods/{id})
  - 음식 선호도 삭제 성공 (DELETE /api/v1/members/me/preferences/foods/{id})
  - 선호도 조회 실패 - 회원 없음 (404)
- **인증 방식:** JWT Authorization Bearer Token
- **생성된 Snippet:** 6개 API 문서
- **소요 시간:** 약 5분 (기존 테스트 검증)
- **특이사항:** 카테고리 선호도 + 음식 선호도 통합 관리

### BudgetController REST Docs (2025-10-12)
- **파일:** `BudgetControllerRestDocsTest.java`
- **테스트 케이스:** 8개 (모두 통과)
  - 월별 예산 조회 성공, 일별 예산 조회 성공
  - 월별 예산 수정 성공, 일별 예산 수정 성공
  - 월별 예산 수정 실패 - 유효성 검증 실패 (422)
  - 월별 예산 조회 실패 - 잘못된 파라미터 (422)
  - 월별 예산 조회 실패 - 인증되지 않은 요청 (401)
  - 월별 예산 수정 실패 - 인증되지 않은 요청 (401)
- **인증 방식:** JWT Authorization Bearer Token
- **생성된 Snippet:** 8개 API 문서
- **소요 시간:** 약 1.5시간

### SocialAccountController REST Docs (2025-10-12)
- **파일:** `SocialAccountControllerRestDocsTest.java`
- **테스트 케이스:** 7개 (모두 통과)
  - 소셜 계정 목록 조회 성공 (GET /api/v1/members/me/social-accounts)
  - 소셜 계정 추가 연동 성공 (POST /api/v1/members/me/social-accounts)
  - 소셜 계정 연동 해제 성공 (DELETE /api/v1/members/me/social-accounts/{socialAccountId})
  - 소셜 계정 추가 실패 - 중복 연동 (409)
  - 소셜 계정 추가 실패 - 유효성 검증 실패 (422)
  - 소셜 계정 연동 해제 실패 - 존재하지 않는 계정 (404)
  - 소셜 계정 연동 해제 실패 - 유일한 로그인 수단 (409)
- **인증 방식:** JWT Authorization Bearer Token
- **생성된 Snippet:** 7개 API 문서
- **소요 시간:** 약 10분 (기존 테스트 검증)
- **특이사항:** OAuth 클라이언트 MockBean 설정 (KakaoAuthClient, GoogleAuthClient)

### CategoryController REST Docs (2025-10-12)
- **파일:** `CategoryControllerRestDocsTest.java`
- **테스트 케이스:** 2개 (모두 통과)
  - 카테고리 목록 조회 성공 (GET /api/v1/categories)
  - 카테고리 목록 조회 - 빈 목록 (200)
- **인증 방식:** 인증 불필요 (공개 API)
- **생성된 Snippet:** 2개 API 문서 (get-categories-success, get-categories-empty)
- **소요 시간:** 즉시 (기존 테스트 존재)
- **특이사항:** 가장 간단한 REST API, 인증 불필요

### PolicyController REST Docs (2025-10-12)
- **파일:** `PolicyControllerRestDocsTest.java`
- **테스트 케이스:** 3개 (모두 통과)
  - 약관 목록 조회 성공 (GET /api/v1/policies)
  - 약관 상세 조회 성공 (GET /api/v1/policies/{policyId})
  - 약관 상세 조회 실패 - 존재하지 않는 약관 (404)
- **인증 방식:** 인증 불필요 (공개 API)
- **생성된 Snippet:** 3개 API 문서 (get-policies-success, get-policy-success, get-policy-not-found)
- **소요 시간:** 즉시 (기존 테스트 존재)
- **특이사항:** 약관 동의 API는 별도 Controller에 없음 (온보딩 프로세스에 통합된 것으로 보임)

---

## ⏸️ 보류된 작업

### ExpenditureController REST Docs (보류 사유: 기능 미완성)
- **현재 상태:** 등록 API만 구현됨 (POST /api/v1/expenditures)
- **미구현 기능:**
  - 지출 내역 목록 조회 (GET /api/v1/expenditures) - 필터링, 페이징
  - 지출 내역 상세 조회 (GET /api/v1/expenditures/{id})
  - 지출 내역 수정 (PUT /api/v1/expenditures/{id})
  - 지출 내역 삭제 (DELETE /api/v1/expenditures/{id})
  - 지출 통계 조회 (GET /api/v1/expenditures/statistics)
- **작업 시도 내용:**
  - ExpenditureControllerRestDocsTest.java 작성 시도
  - 등록 API에 대한 4개 테스트 케이스 작성 (성공 2개, 실패 2개)
  - 인증 및 응답 구조 이슈로 테스트 실패
- **다음 단계:** CRUD 전체 기능 구현 후 REST Docs 작업 재개

### GroupController REST Docs (보류 사유: 기능 미완성)
- **현재 상태:** 검색 API만 구현됨 (GET /api/v1/groups)
- **미구현 기능:**
  - 그룹 상세 조회 (GET /api/v1/groups/{id})
  - 그룹 생성 (POST /api/v1/groups) - Admin 전용
  - 그룹 수정 (PUT /api/v1/groups/{id}) - Admin 전용
  - 그룹 삭제 (DELETE /api/v1/groups/{id}) - Admin 전용
  - 그룹 멤버 추가 (POST /api/v1/groups/{id}/members) - Admin 전용
  - 그룹 멤버 제거 (DELETE /api/v1/groups/{id}/members/{memberId}) - Admin 전용
- **다음 단계:** CRUD 전체 기능 구현 후 REST Docs 작업 진행 (Admin 모듈에서 작업 가능성)

### AddressController REST Docs (2025-10-12)
- **파일:** `AddressControllerRestDocsTest.java`
- **테스트 케이스:** 11개 (모두 통과)
  - 주소 목록 조회 성공 (GET /api/v1/members/me/addresses)
  - 주소 추가 성공 (POST /api/v1/members/me/addresses)
  - 주소 수정 성공 (PUT /api/v1/members/me/addresses/{addressHistoryId})
  - 주소 삭제 성공 (DELETE /api/v1/members/me/addresses/{addressHistoryId})
  - 기본 주소 설정 성공 (PUT /api/v1/members/me/addresses/{addressHistoryId}/primary)
  - 주소 목록 조회 - 빈 배열 (200)
  - 주소 추가 실패 - 유효성 검증 실패 (422)
  - 주소 수정 실패 - 존재하지 않는 주소 (404)
  - 주소 삭제 실패 - 존재하지 않는 주소 (404)
  - 기본 주소 설정 실패 - 존재하지 않는 주소 (404)
- **인증 방식:** JWT Authorization Bearer Token
- **생성된 Snippet:** 11개 API 문서
- **소요 시간:** 약 1.5시간

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
