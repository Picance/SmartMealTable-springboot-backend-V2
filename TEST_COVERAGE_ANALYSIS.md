# 테스트 커버리지 분석 및 미작성 테스트 목록

**생성일**: 2025-10-11  
**분석 범위**: SmartMealTable API 전체

---

## 📊 전체 분석 요약

### ✅ 완료된 작업
1. **MemberController 통합 테스트 작성 완료** (9개 테스트 모두 통과 ✅)
   - 내 프로필 조회 성공/실패 (2개)
   - 프로필 수정 성공 (3개: 닉네임만/그룹만/둘 다)
   - 프로필 수정 실패 (4개: 회원없음/그룹없음/닉네임길이초과/빈닉네임)

---

## 🔍 미작성 테스트 상세 분석

### 1. 통합 테스트 (Integration Tests) 부족

#### 1-1. AddressController 통합 테스트 (미작성)
**파일**: `AddressControllerTest.java` (생성 필요)
**테스트 대상 API**:
- `GET /api/v1/members/me/addresses` - 주소 목록 조회
- `POST /api/v1/members/me/addresses` - 주소 추가
- `PUT /api/v1/members/me/addresses/{addressHistoryId}` - 주소 수정
- `DELETE /api/v1/members/me/addresses/{addressHistoryId}` - 주소 삭제
- `PUT /api/v1/members/me/addresses/{addressHistoryId}/primary` - 기본 주소 설정

**필요한 테스트 시나리오**:
```
✅ 주소 목록 조회 성공 - 200 OK
✅ 주소 목록 조회 - 주소 없음 (빈 배열 반환)
✅ 주소 추가 성공 - 201 Created
✅ 주소 추가 실패 - 유효하지 않은 주소 형식 (422)
✅ 주소 수정 성공 - 200 OK
✅ 주소 수정 실패 - 존재하지 않는 주소 (404)
✅ 주소 수정 실패 - 다른 회원의 주소 (403 or 404)
✅ 주소 삭제 성공 - 204 No Content
✅ 주소 삭제 실패 - 존재하지 않는 주소 (404)
✅ 기본 주소 설정 성공 - 200 OK
✅ 기본 주소 설정 실패 - 존재하지 않는 주소 (404)
```

---

#### 1-2. PasswordExpiryController 통합 테스트 (미작성)
**파일**: `PasswordExpiryControllerTest.java` (생성 필요)
**테스트 대상 API**:
- `GET /api/v1/members/me/password/expiry-status` - 비밀번호 만료 상태 조회
- `POST /api/v1/members/me/password/extend-expiry` - 비밀번호 만료일 연장

**필요한 테스트 시나리오**:
```
✅ 비밀번호 만료 상태 조회 성공 - 만료되지 않음 (200 OK)
✅ 비밀번호 만료 상태 조회 성공 - 만료 임박 (7일 이내)
✅ 비밀번호 만료 상태 조회 성공 - 이미 만료됨
✅ 비밀번호 만료일 연장 성공 - 200 OK (90일 연장)
✅ 비밀번호 만료일 연장 실패 - 소셜 로그인 회원 (비밀번호 없음) (400)
```

---

#### 1-3. SocialAccountController 통합 테스트 보완 (부분 작성됨)
**기존 파일**: `GetSocialAccountListControllerTest.java` (조회만 구현)
**추가 필요 파일**: 기존 파일 확장 또는 새 파일 생성

**테스트 대상 API**:
- ✅ `GET /api/v1/members/me/social-accounts` - 소셜 계정 목록 조회 (작성 완료)
- ❌ `POST /api/v1/members/me/social-accounts` - 소셜 계정 추가 연동 (미작성)
- ❌ `DELETE /api/v1/members/me/social-accounts/{socialAccountId}` - 소셜 계정 연동 해제 (미작성)

**필요한 테스트 시나리오**:
```
✅ 소셜 계정 추가 연동 성공 - 카카오 (201 Created)
✅ 소셜 계정 추가 연동 성공 - 구글 (201 Created)
✅ 소셜 계정 추가 연동 실패 - 이미 연동된 계정 (409 Conflict)
✅ 소셜 계정 추가 연동 실패 - 유효하지 않은 authorizationCode (401)
✅ 소셜 계정 연동 해제 성공 - 204 No Content
✅ 소셜 계정 연동 해제 실패 - 존재하지 않는 소셜 계정 (404)
✅ 소셜 계정 연동 해제 실패 - 마지막 인증 수단 (비밀번호 없고 소셜 계정 1개) (400)
```

---

### 2. REST Docs 테스트 (Documentation Tests) 부족

#### 2-1. 인증 API REST Docs (미작성)
**파일들**: 
- `LoginControllerRestDocsTest.java` (생성 필요)
- `RefreshTokenControllerRestDocsTest.java` (생성 필요)
- `LogoutControllerRestDocsTest.java` (생성 필요)
- `CheckEmailControllerRestDocsTest.java` (생성 필요)
- `KakaoLoginControllerRestDocsTest.java` (생성 필요)
- `GoogleLoginControllerRestDocsTest.java` (생성 필요)

**문서화 필요 API**:
```
❌ POST /api/v1/auth/login/email - 이메일 로그인
❌ POST /api/v1/auth/refresh - 토큰 갱신
❌ POST /api/v1/auth/logout - 로그아웃
❌ GET /api/v1/auth/check-email - 이메일 중복 검증
❌ POST /api/v1/auth/login/kakao - 카카오 소셜 로그인
❌ POST /api/v1/auth/login/google - 구글 소셜 로그인
```

**REST Docs 구현 우선순위**: ⭐⭐⭐⭐⭐ (매우 높음)
- 인증 API는 모든 클라이언트가 가장 먼저 사용하는 API
- API 명세 문서화가 필수

---

#### 2-2. 회원 관리 API REST Docs (미작성)
**파일들**:
- `MemberControllerRestDocsTest.java` (생성 필요)
- `ChangePasswordControllerRestDocsTest.java` (생성 필요)
- `WithdrawMemberControllerRestDocsTest.java` (생성 필요)
- `SocialAccountControllerRestDocsTest.java` (생성 필요)
- `PasswordExpiryControllerRestDocsTest.java` (생성 필요)

**문서화 필요 API**:
```
❌ GET /api/v1/members/me - 내 프로필 조회
❌ PUT /api/v1/members/me - 프로필 수정
❌ PUT /api/v1/members/me/password - 비밀번호 변경
❌ DELETE /api/v1/members/me - 회원 탈퇴
❌ GET /api/v1/members/me/social-accounts - 소셜 계정 목록 조회
❌ POST /api/v1/members/me/social-accounts - 소셜 계정 추가 연동
❌ DELETE /api/v1/members/me/social-accounts/{socialAccountId} - 소셜 계정 연동 해제
❌ GET /api/v1/members/me/password/expiry-status - 비밀번호 만료 상태 조회
❌ POST /api/v1/members/me/password/extend-expiry - 비밀번호 만료일 연장
```

**REST Docs 구현 우선순위**: ⭐⭐⭐⭐ (높음)

---

#### 2-3. 예산 관리 API REST Docs (미작성)
**파일들**:
- `BudgetControllerRestDocsTest.java` (생성 필요)

**문서화 필요 API**:
```
❌ GET /api/v1/budget/monthly - 월별 예산 조회
❌ GET /api/v1/budget/daily - 일별 예산 조회
❌ PUT /api/v1/budget - 예산 수정
❌ PUT /api/v1/budget/daily/{date} - 특정 일자 예산 일괄 적용
```

**기존 통합 테스트**: ✅ 모두 작성 완료
- `MonthlyBudgetQueryControllerTest.java`
- `DailyBudgetQueryControllerTest.java`
- `UpdateBudgetControllerTest.java`

**REST Docs 구현 우선순위**: ⭐⭐⭐ (중간)

---

#### 2-4. 선호도 관리 API REST Docs (미작성)
**파일들**:
- `PreferenceControllerRestDocsTest.java` (생성 필요)

**문서화 필요 API**:
```
❌ GET /api/v1/members/me/preferences - 선호도 조회
❌ PUT /api/v1/members/me/preferences/categories - 카테고리 선호도 수정
❌ POST /api/v1/members/me/preferences/foods - 음식 선호도 추가
❌ PUT /api/v1/members/me/preferences/foods/{foodPreferenceId} - 음식 선호도 변경
❌ DELETE /api/v1/members/me/preferences/foods/{foodPreferenceId} - 음식 선호도 삭제
```

**기존 통합 테스트**: ✅ 모두 작성 완료
- `PreferenceControllerTest.java`
- `UpdateCategoryPreferencesControllerTest.java`
- `FoodPreferenceControllerTest.java`

**REST Docs 구현 우선순위**: ⭐⭐⭐ (중간)

---

#### 2-5. 주소 관리 API REST Docs (미작성)
**파일들**:
- `AddressControllerRestDocsTest.java` (생성 필요)

**문서화 필요 API**:
```
❌ GET /api/v1/members/me/addresses - 주소 목록 조회
❌ POST /api/v1/members/me/addresses - 주소 추가
❌ PUT /api/v1/members/me/addresses/{addressHistoryId} - 주소 수정
❌ DELETE /api/v1/members/me/addresses/{addressHistoryId} - 주소 삭제
❌ PUT /api/v1/members/me/addresses/{addressHistoryId}/primary - 기본 주소 설정
```

**기존 통합 테스트**: ❌ 미작성 (통합 테스트부터 작성 필요)

**REST Docs 구현 우선순위**: ⭐⭐⭐ (중간)

---

#### 2-6. 지출 내역 API REST Docs (미작성)
**파일들**:
- `CreateExpenditureControllerRestDocsTest.java` (생성 필요)

**문서화 필요 API**:
```
❌ POST /api/v1/expenditures - 지출 내역 등록
```

**기존 통합 테스트**: ✅ 작성 완료
- `CreateExpenditureControllerTest.java`

**REST Docs 구현 우선순위**: ⭐⭐⭐ (중간)

---

## 📋 테스트 작성 우선순위 요약

### 🔴 최우선 (P0) - 즉시 작성 필요
1. **인증 API REST Docs** (6개 파일)
   - 모든 클라이언트가 첫 번째로 구현하는 API
   - API 명세 문서 필수

### 🟠 높음 (P1) - 빠른 시일 내 작성
2. **회원 관리 API REST Docs** (5개 파일)
   - 핵심 기능 API 문서화
3. **AddressController 통합 테스트** (1개 파일)
   - 통합 테스트 미작성 API

### 🟡 중간 (P2) - 순차적 작성
4. **PasswordExpiryController 통합 테스트** (1개 파일)
5. **SocialAccountController 통합 테스트 보완** (기존 파일 확장)
6. **예산 관리 API REST Docs** (1개 파일)
7. **선호도 관리 API REST Docs** (1개 파일)
8. **주소 관리 API REST Docs** (1개 파일)
9. **지출 내역 API REST Docs** (1개 파일)

---

## 📝 테스트 작성 가이드

### 통합 테스트 작성 패턴
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("컨트롤러명 API 테스트")
class XxxControllerTest extends AbstractContainerTest {
    
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    
    // 테스트 시나리오: 성공/실패/예외 모두 포함
    // HTTP 상태코드 검증 (200, 201, 400, 401, 404, 422)
    // 응답 구조 검증 ($.result, $.data, $.error)
}
```

### REST Docs 작성 패턴
```java
class XxxControllerRestDocsTest extends AbstractRestDocsTest {
    
    @Test
    @DisplayName("API명 성공 케이스 문서화")
    void apiName_success_docs() throws Exception {
        mockMvc.perform(...)
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(document("api-endpoint-name",
                requestFields(...),
                responseFields(...)
            ));
    }
}
```

---

## ✅ 완료된 테스트 현황

### 통합 테스트 (✅ 47개 파일)
- ✅ Auth: SignupControllerTest, LoginControllerTest, CheckEmailControllerTest, KakaoLoginControllerTest, GoogleLoginControllerTest
- ✅ Member: MemberControllerTest (신규), ChangePasswordControllerTest, WithdrawMemberControllerTest, GetSocialAccountListControllerTest
- ✅ Member Preferences: PreferenceControllerTest, UpdateCategoryPreferencesControllerTest, FoodPreferenceControllerTest
- ✅ Budget: MonthlyBudgetQueryControllerTest, DailyBudgetQueryControllerTest, UpdateBudgetControllerTest
- ✅ Onboarding: OnboardingProfileControllerTest, OnboardingAddressControllerTest, SetBudgetControllerTest, FoodPreferenceControllerTest, PolicyAgreementControllerTest
- ✅ Support: GroupControllerTest, CategoryControllerTest, PolicyControllerTest
- ✅ Expenditure: CreateExpenditureControllerTest

### REST Docs (✅ 6개 파일)
- ✅ SignupControllerRestDocsTest
- ✅ OnboardingProfileControllerRestDocsTest
- ✅ OnboardingAddressControllerRestDocsTest
- ✅ SetBudgetControllerRestDocsTest
- ✅ FoodPreferenceControllerRestDocsTest (온보딩)

---

## 🎯 다음 단계 권장사항

1. **인증 API REST Docs 작성** (최우선)
   - 6개 API 문서화 완료
   - index.adoc에 인증 섹션 추가

2. **회원 관리 API 테스트 완성**
   - AddressController 통합 테스트 작성
   - PasswordExpiryController 통합 테스트 작성
   - 모든 회원 관리 API REST Docs 작성

3. **나머지 REST Docs 순차 작성**
   - 예산 관리, 선호도 관리, 지출 내역 등

4. **CI/CD 통합**
   - 모든 테스트 통과 여부 자동 검증
   - REST Docs HTML 자동 생성 및 배포

---

**작성자**: GitHub Copilot  
**마지막 업데이트**: 2025-10-11
