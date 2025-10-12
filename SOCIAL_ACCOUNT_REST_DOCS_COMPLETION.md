# SocialAccountController REST Docs 작성 완료 보고서

## 📋 작업 개요

**작업명:** SocialAccountController REST Docs 테스트 작성  
**작업일:** 2025-10-12  
**작업 시간:** 약 30분  
**작업 상태:** ✅ **완료 (100%)**

---

## 🎯 작업 목표

소셜 계정 관리 API (연동 목록 조회, 추가 연동, 연동 해제)의 Spring REST Docs 테스트 코드 작성 및 API 문서 자동 생성

---

## ✅ 완료된 작업

### 1. 테스트 파일 작성
**파일명:** `SocialAccountControllerRestDocsTest.java`  
**경로:** `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/member/controller/`

### 2. 작성된 테스트 케이스 (7개)

#### 성공 시나리오 (3개)
1. **`getSocialAccountList_success_docs`**
   - 엔드포인트: `GET /api/v1/members/me/social-accounts`
   - 상태 코드: 200 OK
   - 응답 구조:
     ```json
     {
       "result": "SUCCESS",
       "data": {
         "connectedAccounts": [
           {
             "socialAccountId": 1,
             "provider": "KAKAO",
             "providerEmail": "test@example.com",
             "connectedAt": "2025-10-12T14:00:14.080172"
           }
         ],
         "hasPassword": true
       }
     }
     ```
   - 주요 특징: hasPassword 필드로 이메일 비밀번호 설정 여부 확인

2. **`addSocialAccount_success_docs`**
   - 엔드포인트: `POST /api/v1/members/me/social-accounts`
   - 상태 코드: 201 CREATED
   - 요청 구조:
     ```json
     {
       "provider": "GOOGLE",
       "authorizationCode": "new_google_auth_code_12345"
     }
     ```
   - 응답 구조:
     ```json
     {
       "result": "SUCCESS",
       "data": {
         "socialAccountId": 5,
         "provider": "GOOGLE",
         "providerEmail": "newuser@gmail.com",
         "connectedAt": "2025-10-12T14:00:14.010489"
       }
     }
     ```
   - 주요 특징: OAuth 인증 코드를 사용한 소셜 계정 추가 연동

3. **`removeSocialAccount_success_docs`**
   - 엔드포인트: `DELETE /api/v1/members/me/social-accounts/{socialAccountId}`
   - 상태 코드: 204 NO_CONTENT
   - 응답: 없음 (성공 시 본문 없음)
   - 주요 특징: 비밀번호가 있거나 다른 소셜 계정이 있을 때만 해제 가능

#### 실패 시나리오 (4개)

4. **`addSocialAccount_duplicate_docs`**
   - 상태 코드: 409 CONFLICT
   - 에러 메시지: "이미 다른 계정에 연동된 소셜 계정입니다."
   - 시나리오: 같은 providerId의 소셜 계정이 이미 다른 회원에게 연동된 경우

5. **`addSocialAccount_validation_docs`**
   - 상태 코드: 422 UNPROCESSABLE_ENTITY
   - 에러 메시지: "인증 코드를 입력해주세요."
   - 에러 상세:
     ```json
     {
       "result": "ERROR",
       "error": {
         "code": "E422",
         "message": "인증 코드를 입력해주세요.",
         "data": {
           "field": "authorizationCode",
           "reason": "인증 코드를 입력해주세요."
         }
       }
     }
     ```
   - 시나리오: authorizationCode가 빈 문자열인 경우

6. **`removeSocialAccount_notFound_docs`**
   - 상태 코드: 404 NOT_FOUND
   - 에러 메시지: "존재하지 않는 소셜 계정입니다."
   - 시나리오: 존재하지 않는 socialAccountId로 해제 시도

7. **`removeSocialAccount_lastLoginMethod_docs`**
   - 상태 코드: 409 CONFLICT
   - 에러 메시지: "유일한 로그인 수단입니다. 연동 해제하려면 먼저 비밀번호를 설정해주세요."
   - 시나리오: 비밀번호가 없고 소셜 계정이 1개만 있어서 해제 시 로그인 불가능한 경우
   - 비즈니스 로직: 마지막 로그인 수단 보호

---

## 🔧 기술적 구현 사항

### 1. OAuth 클라이언트 MockBean 설정
```java
@MockBean private KakaoAuthClient kakaoAuthClient;
@MockBean private GoogleAuthClient googleAuthClient;

// OAuth Token Response Mock
OAuthTokenResponse tokenResponse = new OAuthTokenResponse(
    "google_access_token",
    "google_refresh_token",
    3600,
    "bearer",
    "id_token_value"
);

// OAuth User Info Mock
OAuthUserInfo userInfo = OAuthUserInfo.of(
    "google_87654321",
    "newuser@gmail.com",
    "New User",
    null
);

given(googleAuthClient.getAccessToken(anyString())).willReturn(tokenResponse);
given(googleAuthClient.extractUserInfo(anyString())).willReturn(userInfo);
```

### 2. JWT 인증 패턴 사용
```java
private String accessToken;

@BeforeEach
void setUpTestData() {
    // ... 회원 생성
    accessToken = createAccessToken(member.getMemberId());
}

// 모든 요청에 Authorization 헤더 추가
mockMvc.perform(get("/api/v1/members/me/social-accounts")
        .header("Authorization", accessToken))
```

### 3. 엔티티 생성 패턴
```java
// 이메일 + 비밀번호 회원
MemberAuthentication auth = MemberAuthentication.createEmailAuth(
    member.getMemberId(),
    "test@example.com",
    "hashedPasswordForTest",
    "테스트유저"
);

// 소셜 로그인만 가능한 회원
MemberAuthentication socialOnlyAuth = MemberAuthentication.createSocialAuth(
    savedSocialOnlyMember.getMemberId(),
    "social@example.com",
    "소셜전용유저"
);

// 소셜 계정 생성
SocialAccount kakao = SocialAccount.create(
    memberAuth.getMemberAuthenticationId(),
    SocialProvider.KAKAO,
    "kakao_12345678",
    "kakao_access_token",
    "kakao_refresh_token",
    "bearer",
    LocalDateTime.now().plusDays(30)
);
```

### 4. 응답 검증 패턴
```java
// 성공 응답 ($.error 필드 없음)
.andExpect(status().isOk())
.andExpect(jsonPath("$.result").value("SUCCESS"))
.andExpect(jsonPath("$.data").exists())
.andExpect(jsonPath("$.data.connectedAccounts").isArray())

// 에러 응답 ($.data 필드 없음)
.andExpect(status().isConflict())
.andExpect(jsonPath("$.result").value("ERROR"))
.andExpect(jsonPath("$.error").exists())
.andExpect(jsonPath("$.error.code").value("E409"))
.andExpect(jsonPath("$.error.message").value("이미 다른 계정에 연동된 소셜 계정입니다."))
```

---

## 📊 테스트 실행 결과

### 빌드 결과
```bash
./gradlew :smartmealtable-api:test --tests "SocialAccountControllerRestDocsTest"

BUILD SUCCESSFUL in 9s
7 tests completed, 0 failed ✅
```

### 생성된 REST Docs Snippets
```
smartmealtable-api/build/generated-snippets/social-account/
├── get-list-success/
├── add-success/
├── add-duplicate/
├── add-validation/
├── remove-success/
├── remove-not-found/
└── remove-last-login-method/
```

각 디렉토리에는 다음 파일들이 생성됨:
- `curl-request.adoc` - cURL 예제
- `http-request.adoc` - HTTP 요청 예제
- `http-response.adoc` - HTTP 응답 예제
- `httpie-request.adoc` - HTTPie 예제
- `request-body.adoc` - 요청 본문 (POST만)
- `request-fields.adoc` - 요청 필드 문서 (POST만)
- `path-parameters.adoc` - 경로 파라미터 문서 (DELETE만)
- `request-headers.adoc` - 요청 헤더 문서
- `response-body.adoc` - 응답 본문
- `response-fields.adoc` - 응답 필드 문서

---

## 🐛 해결한 이슈

### 1. 에러 메시지 불일치
**문제:**
- 예상 메시지: "소셜 계정을 찾을 수 없습니다."
- 실제 메시지: "존재하지 않는 소셜 계정입니다."

**해결:**
- ErrorType.java에서 실제 에러 메시지 확인
- 테스트 코드의 예상 메시지를 실제 메시지로 수정

### 2. OAuth 클라이언트 패키지 경로
**문제:**
- 초기 시도 시 client 모듈 의존성 문제 발생

**해결:**
- `smartmealtable-client:auth` 모듈이 이미 build.gradle에 포함되어 있음 확인
- 정확한 패키지 경로 사용: `com.stdev.smartmealtable.client.auth.oauth.*`

### 3. 복잡한 시나리오 테스트 (유일한 로그인 수단)
**문제:**
- 비밀번호 없는 회원 + 소셜 계정 1개만 있는 상황 구성 필요

**해결:**
- 별도의 회원/그룹/인증 정보/소셜 계정 생성
- `MemberAuthentication.createSocialAuth()` 사용하여 비밀번호 없는 인증 정보 생성
- 해당 회원의 토큰으로 연동 해제 시도하여 409 에러 검증

---

## 📈 성과 및 영향

### 1. API 문서화 완성도 향상
- 소셜 계정 관리 API 3개 엔드포인트 완전 문서화
- 7개 시나리오 (성공 3개 + 실패 4개) 커버
- 마지막 로그인 수단 보호 로직까지 상세히 문서화

### 2. OAuth 통합 테스트 패턴 확립
- KakaoAuthClient, GoogleAuthClient MockBean 패턴
- OAuthTokenResponse + OAuthUserInfo 객체 모킹 방법
- 향후 다른 OAuth 연동 기능 테스트에 재사용 가능

### 3. 비즈니스 로직 검증
- hasPassword 상태에 따른 연동 해제 가능 여부 확인
- 소셜 계정 개수에 따른 연동 해제 가능 여부 확인
- 중복 연동 방지 로직 검증

### 4. 전체 REST Docs 진행률 향상
- **15개 파일, 62개 테스트 케이스 완료** (이전: 14개 파일, 55개 테스트)
- Profile & Preference API 카테고리 **17개 테스트** 완료

---

## 📝 문서화된 API 엔드포인트

### 1. GET /api/v1/members/me/social-accounts
- **기능:** 연동된 소셜 계정 목록 조회
- **인증:** JWT 필수
- **응답:** connectedAccounts 배열 + hasPassword 상태
- **문서:** `get-list-success/`

### 2. POST /api/v1/members/me/social-accounts
- **기능:** 소셜 계정 추가 연동
- **인증:** JWT 필수
- **요청:** provider (KAKAO/GOOGLE), authorizationCode
- **응답:** socialAccountId, provider, providerEmail, connectedAt
- **문서:** `add-success/`, `add-duplicate/`, `add-validation/`

### 3. DELETE /api/v1/members/me/social-accounts/{socialAccountId}
- **기능:** 소셜 계정 연동 해제
- **인증:** JWT 필수
- **응답:** 204 NO_CONTENT (성공 시)
- **비즈니스 규칙:** 
  - 비밀번호가 없고 소셜 계정이 1개만 있으면 해제 불가 (409)
  - 존재하지 않는 소셜 계정 해제 시도 시 404
- **문서:** `remove-success/`, `remove-not-found/`, `remove-last-login-method/`

---

## 🎓 교훈 및 개선 사항

### 배운 점
1. **OAuth 클라이언트 모킹 패턴**
   - Token Response와 User Info를 각각 모킹해야 함
   - `given(...).willReturn(...)` 패턴으로 명확한 동작 정의

2. **복잡한 비즈니스 로직 테스트**
   - 여러 엔티티 상태 조합을 통한 시나리오 구성
   - 별도 회원 생성으로 독립적인 테스트 환경 구축

3. **에러 메시지 정확성**
   - ErrorType enum을 먼저 확인하고 테스트 작성
   - 실제 에러 메시지와 예상 메시지 일치 필수

### 재사용 가능한 패턴
```java
// OAuth 클라이언트 Mock 패턴
OAuthTokenResponse tokenResponse = new OAuthTokenResponse(...);
OAuthUserInfo userInfo = OAuthUserInfo.of(...);
given(client.getAccessToken(anyString())).willReturn(tokenResponse);
given(client.extractUserInfo(anyString())).willReturn(userInfo);

// 소셜 전용 회원 생성 패턴
MemberAuthentication socialAuth = MemberAuthentication.createSocialAuth(
    memberId, email, name
);

// 마지막 로그인 수단 검증 패턴
// hasPassword = false AND 소셜 계정 개수 = 1 → 409 에러
```

---

## 📊 통계 요약

### 작업 시간 분석
- 파일 작성: 15분
- 테스트 실행 및 디버깅: 10분
- 문서 업데이트: 5분
- **총 작업 시간:** 30분

### 코드 통계
- 테스트 코드 라인 수: 약 380줄
- 테스트 케이스: 7개
- Mock 객체: 2개 (KakaoAuthClient, GoogleAuthClient)
- 생성된 Snippet 디렉토리: 7개

### 테스트 커버리지
- 엔드포인트: 3/3 (100%)
- 성공 시나리오: 3/3 (100%)
- 실패 시나리오: 4/4 (100%)
  - 409 CONFLICT: 2개 (중복, 마지막 로그인 수단)
  - 404 NOT_FOUND: 1개
  - 422 UNPROCESSABLE_ENTITY: 1개

---

## 🚀 다음 작업 추천

### 우선순위 P1: BudgetController REST Docs
- **예상 시간:** 40-50분
- **예상 테스트:** 6-10개
- **엔드포인트:** 4개 (조회, 수정, 초기화, 이력)
- **이유:** JWT 인증 패턴 재사용 가능, 예산 관리는 핵심 기능

### 우선순위 P2: AddressController REST Docs
- **예상 시간:** 50-60분
- **예상 테스트:** 8-12개
- **엔드포인트:** 5개 (CRUD + 기본 주소 설정)

---

## ✅ 체크리스트

- [x] 테스트 파일 작성 완료
- [x] 7개 테스트 케이스 모두 통과
- [x] REST Docs Snippets 생성 확인 (7개 디렉토리)
- [x] JWT 인증 패턴 적용
- [x] OAuth 클라이언트 MockBean 설정
- [x] 에러 메시지 정확성 검증
- [x] 마지막 로그인 수단 보호 로직 테스트
- [x] REST_DOCS_PROGRESS_REPORT.md 업데이트
- [x] 완료 보고서 작성

---

**작성일:** 2025-10-12  
**작성자:** GitHub Copilot  
**최종 상태:** ✅ **100% 완료**  
**다음 작업:** BudgetController REST Docs 작성 권장
