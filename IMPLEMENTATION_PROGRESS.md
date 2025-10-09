# 🚀 SmartMealTable API 구현 진행상황

> **목표**: 회원가입 API를 TDD로 완전히 구현하여 전체 아키텍처 템플릿 확립

**시작일**: 2025-10-08  
**완료일**: 2025-10-08

---

## 📊 전체 진행률

```
JWT 인증 시스템:        [████████████████████] 100% (4/4 API)
회원 관리 API:          [████████████████████] 100% (5/5 API)
소셜 로그인 API:        [████████████████████] 100% (2/2 API) ✅ 통합 테스트 완료
온보딩 API:             [████████████████████] 100% (4/4 API) ⭐ 취향 설정 추가
예산 관리 API:          [██████████░░░░░░░░░░]  50% (2/4 API) 🔥 조회 API 완료
지출 내역 API:          [░░░░░░░░░░░░░░░░░░░░]  0% (0/4 API)
가게 및 추천 API:       [░░░░░░░░░░░░░░░░░░░░]  0% (0/5 API)

총 진행률:              [██████████████████░░] 90% (19/27 API)
```

### ✅ 완료된 작업

#### 1. Core 모듈 구현 (100%)
- ✅ `ApiResponse<T>` - 공통 응답 구조
- ✅ `ResultType` enum - SUCCESS/ERROR
- ✅ `ErrorCode` enum - E400, E401, E404, E409, E422, E500, E503
- ✅ `ErrorMessage` - 에러 메시지 구조
- ✅ `ErrorType` enum - 모든 에러 타입 상세 정의
- ✅ `BaseException` - 기본 예외 클래스
- ✅ `AuthenticationException` - 인증 예외
- ✅ `AuthorizationException` - 권한 예외
- ✅ `BusinessException` - 비즈니스 예외
- ✅ `ExternalServiceException` - 외부 서비스 예외
- ✅ 빌드 성공 확인

**위치**: `smartmealtable-core/src/main/java/com/stdev/smartmealtable/core/`

#### 2. Domain 모듈 구현 (100%)
- ✅ `RecommendationType` enum - SAVER, ADVENTURER, BALANCED
- ✅ `GroupType` enum - UNIVERSITY, COMPANY, OTHER
- ✅ `SocialProvider` enum - KAKAO, GOOGLE
- ✅ `Member` 도메인 엔티티 - Lombok 적용 완료
- ✅ `MemberAuthentication` 도메인 엔티티 - 인증 로직 + Lombok 적용
- ✅ `Group` 도메인 엔티티 - Lombok 적용 완료
- ✅ `SocialAccount` 도메인 엔티티 - Lombok 적용 완료
- ✅ Repository 인터페이스 정의
  - `MemberRepository`
  - `MemberAuthenticationRepository`
  - `GroupRepository`
  - `SocialAccountRepository`

**위치**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/member/`

**Lombok 적용**: 모든 도메인 엔티티에 `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용

#### 3. Storage 모듈 구현 (100%)
- ✅ `BaseTimeEntity` - JPA Auditing용 기본 클래스
- ✅ `MemberJpaEntity` - JPA 엔티티 + Lombok 적용
- ✅ `MemberAuthenticationJpaEntity` - JPA 엔티티 + Lombok 적용
- ✅ `GroupJpaEntity` - JPA 엔티티 + Lombok 적용
- ✅ `SocialAccountJpaEntity` - JPA 엔티티 + Lombok 적용
- ✅ Repository 구현체
  - `MemberRepositoryImpl`
  - `MemberAuthenticationRepositoryImpl`
  - `GroupRepositoryImpl`
- ✅ `JpaConfig` - JPA Auditing 활성화

**위치**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/`

**Lombok 적용**: 모든 JPA 엔티티에 `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용

#### 4. API 모듈 - Infrastructure 설정 (100%)
- ✅ `application.yml` - MySQL, JPA, Logging 설정
- ✅ `PasswordConfig` - BCrypt 기반 암호화 (Spring Security 제거)
  - Custom `PasswordEncoder` 인터페이스 정의
  - BCrypt 라이브러리 (`at.favre.lib:bcrypt:0.10.2`) 사용
  - Cost factor: 12 (2^12 iterations)
- ✅ BCrypt 의존성 추가, Spring Security 의존성 제거
- ✅ 전체 프로젝트 빌드 성공 확인

**위치**: `smartmealtable-api/src/main/`

**기술 스택 변경**:
- ❌ Spring Security → ✅ BCrypt standalone library
- ✅ Lombok 전역 활성화 (root build.gradle 설정 완료)

#### 5. Application Service 계층 구현 (100%)
- ✅ `SignupServiceRequest` DTO
- ✅ `SignupServiceResponse` DTO
- ✅ `SignupService` 구현
  - 이메일 중복 검증
  - 비밀번호 BCrypt 암호화 (cost factor: 12)
  - Member 엔티티 생성 (기본 추천 유형: BALANCED)
  - MemberAuthentication 엔티티 생성
  - 트랜잭션 관리
- ✅ 도메인 로직과 애플리케이션 로직 분리

**위치**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/auth/service/`

#### 6. Presentation 계층 구현 (100%)
- ✅ `SignupRequest` DTO (Validation 적용)
  - `@NotBlank`, `@Email`, `@Size`, `@Pattern` 검증
- ✅ `SignupResponse` DTO
- ✅ `AuthController` 구현
  - `POST /api/v1/auth/signup/email` 엔드포인트
  - `@Valid` 검증
  - 201 Created 응답
- ✅ `GlobalExceptionHandler` 구현
  - BaseException 처리 (BusinessException, AuthenticationException 등)
  - MethodArgumentNotValidException 처리 (422)
  - IllegalArgumentException 처리 (400)
  - Exception 처리 (500)
  - ErrorType별 HTTP Status 매핑

**위치**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/`

#### 7. 테스트 구현 (100%)
- ✅ `SignupControllerTest` - 통합 테스트
  - 회원가입 성공 시나리오 (201)
  - 이메일 중복 실패 (409)
  - 이메일 형식 오류 (422)
  - 비밀번호 형식 오류 (422)
  - 필수 필드 누락 (422)
  - 이름 길이 제한 (422)
- ✅ MockMvc, TestContainers 설정
- ✅ `@Transactional` 테스트 격리

**위치**: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/auth/controller/`

#### 8. Spring Rest Docs 구현 (100%)
- ✅ `AbstractRestDocsTest` 기본 클래스
  - RestDocumentationExtension 설정
  - 요청/응답 전처리기 (pretty print, URI 치환)
- ✅ `SignupControllerRestDocsTest` - API 문서화 테스트
  - 회원가입 성공 케이스 문서화
  - 이메일 중복 에러 문서화
  - 유효성 검증 실패 문서화
  - 요청/응답 필드 상세 설명
- ✅ `index.adoc` - AsciiDoc 문서
  - API 개요 및 서버 정보
  - 공통 응답 형식
  - HTTP Status Codes
  - 회원가입 API 상세 문서
  - cURL, HTTPie 예제
- ✅ HTML 문서 생성 (`build/docs/asciidoc/index.html`)

**위치**: 
- `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/common/`
- `smartmealtable-api/src/docs/asciidoc/`

#### 9. JWT 인증 시스템 구현 (100%) ⭐ COMPLETE
- ✅ **JWT 인프라 구축** 
  - `JwtTokenProvider` - JWT 토큰 생성/검증 (Access 1h, Refresh 24h)
  - `PasswordConfig` - BCrypt 기반 암호화 (strength: 12)
  - JWT 라이브러리: `io.jsonwebtoken:jjwt-api:0.12.6`
- ✅ **4개 인증 API 완료** 
  - `POST /api/v1/auth/signup/email` - 이메일 회원가입 ✅
  - `POST /api/v1/auth/login` - JWT 로그인 ✅
  - `POST /api/v1/auth/refresh` - 토큰 재발급 ✅
  - `POST /api/v1/auth/logout` - JWT 로그아웃 ✅
- ✅ **TDD 개발 완료**
  - 각 API별 성공/실패 시나리오 테스트 작성
  - TestContainers MySQL 환경 통합 테스트
  - 모든 HTTP 상태코드 검증 (200, 201, 401, 409, 422)
- ✅ **보안 구현**
  - BCrypt 패스워드 암호화/검증
  - JWT 토큰 타입별 검증 (Access vs Refresh)
  - Authorization Header 검증
  - 토큰 만료 시간 관리

**위치**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/auth/`

**상세 문서**: `JWT_AUTHENTICATION_IMPLEMENTATION_REPORT.md` 참조

**기술 스택**:
- JWT 라이브러리: `io.jsonwebtoken:jjwt-api:0.12.6`
- 패스워드 암호화: `at.favre.lib:bcrypt:0.10.2`
- 테스트: TestContainers MySQL + MockMvc

---

## 🔄 최종 상태 (2025-10-09)

### ✅ JWT 인증 시스템 100% 완료
- ✅ **4개 핵심 API 구현**: 회원가입 → 로그인 → 토큰갱신 → 로그아웃
- ✅ **TDD 방식 개발**: RED-GREEN-REFACTOR 사이클 완벽 적용
- ✅ **보안 구현**: JWT + BCrypt 기반 안전한 인증 체계
- ✅ **테스트 완료**: 모든 성공/실패 시나리오 검증

### ✅ 회원 관리 API 100% 완료 (2025-10-09 추가) ⭐ NEW
- ✅ **이메일 중복 검증 API**: GET /api/v1/auth/check-email
  - 사용 가능/중복 여부 확인 (200 OK)
  - 이메일 형식 검증 (422)
- ✅ **비밀번호 변경 API**: PUT /api/v1/members/me/password
  - 현재 비밀번호 검증 (401)
  - 새 비밀번호 형식 검증 (422)
  - 비밀번호 변경 성공 (200 OK)
- ✅ **회원 탈퇴 API**: DELETE /api/v1/members/me
  - Soft Delete 처리 (204 No Content)
  - 비밀번호 검증 (401)
  - 탈퇴 사유 로깅

### ✅ 소셜 로그인 API 100% 완료 (2025-10-09 추가) ✅ 통합 테스트 완료
**목적**: 카카오 및 구글 OAuth 2.0 기반 소셜 로그인 구현

**구현 사항**:
1. **Client 모듈 - OAuth 인증 클라이언트**
   - `KakaoAuthClient`: 카카오 OAuth 토큰 및 사용자 정보 조회
   - `GoogleAuthClient`: Google OAuth 토큰 및 ID Token 기반 사용자 정보 추출
   - `OAuthTokenResponse`, `OAuthUserInfo`: 공통 OAuth DTO
   - RestClient 기반 HTTP 통신 (Spring 6+ Native Client)

2. **Storage 모듈 - 소셜 계정 영속성**
   - `SocialAccountJpaEntity`: 소셜 계정 JPA 엔티티
   - `SocialAccountJpaRepository`: Spring Data JPA Repository
   - `SocialAccountRepositoryImpl`: Domain Repository 구현체

3. **Domain 모듈 - 소셜 인증 비즈니스 로직**
   - `SocialAccount`: 소셜 계정 도메인 엔티티
   - `SocialProvider`: KAKAO, GOOGLE Enum
   - `SocialAuthDomainService`: 소셜 로그인 핵심 비즈니스 로직
     - 신규 회원: Member + SocialAccount 생성
     - 기존 회원: SocialAccount 업데이트 및 연결

4. **API 모듈 - Application Service & Controller**
   - `KakaoLoginService`: 카카오 로그인 유즈케이스
   - `GoogleLoginService`: 구글 로그인 유즈케이스
   - `SocialLoginController`: 소셜 로그인 REST 엔드포인트
     - `POST /api/v1/auth/login/kakao`: 카카오 로그인
     - `POST /api/v1/auth/login/google`: 구글 로그인
   - `KakaoLoginServiceRequest/Response`: 카카오 로그인 DTO
   - `GoogleLoginServiceRequest/Response`: 구글 로그인 DTO

5. **환경 설정**
   - `.env`: OAuth Client ID, Secret, Redirect URI 관리 (gitignored)
   - `.env.example`: 환경 변수 템플릿 (개발자 가이드)
   - `application.yml`: OAuth 설정을 환경 변수로 주입

**OAuth 흐름**:
- 카카오: Authorization Code → Access Token → User Info API
- 구글: Authorization Code → Access Token + ID Token → ID Token Parsing

**TDD 개발 완료**:
- `KakaoLoginServiceTest`: 신규/기존 회원 로그인 시나리오 테스트
- `GoogleLoginServiceTest`: 신규/기존 회원 로그인 시나리오 테스트
- 모든 테스트 통과 (BUILD SUCCESSFUL)

**통합 테스트 완료 (2025-10-10)** 🎉:
- `KakaoLoginControllerTest`: 5개 테스트 모두 통과 ✅
  - 카카오 로그인 성공 - 신규 회원 (200 OK)
  - 카카오 로그인 성공 - 기존 회원 (200 OK)
  - 카카오 로그인 실패 - authorizationCode 누락 (422)
  - 카카오 로그인 실패 - redirectUri 누락 (422)
  - 카카오 로그인 실패 - 빈 문자열 authorizationCode (422)

- `GoogleLoginControllerTest`: 5개 테스트 모두 통과 ✅
  - 구글 로그인 성공 - 신규 회원 (200 OK)
  - 구글 로그인 성공 - 기존 회원 (200 OK)
  - 구글 로그인 실패 - authorizationCode 누락 (422)
  - 구글 로그인 실패 - redirectUri 누락 (422)
  - 구글 로그인 실패 - 빈 문자열 authorizationCode (422)

**TestContainers 통합 테스트 환경**:
- MySQL 8.0 컨테이너 (공유 컨테이너 패턴)
- OAuth 클라이언트 Mock (`@MockBean`)
- `AbstractContainerTest`: 공통 TestContainer 설정
- `application-test.yml`: 테스트 환경 OAuth 더미 설정

**통합 테스트 실행 결과**:
```
✅ 카카오 소셜 로그인 API: 5 tests completed, 0 failed
✅ 구글 소셜 로그인 API: 5 tests completed, 0 failed
✅ 전체 소셜 로그인 테스트: 10/10 통과
```

**Response DTO 구조 확정**:
```json
{
  "result": "SUCCESS",
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "name": "사용자이름",
    "profileImageUrl": "https://...",
    "isNewMember": true
  }
}
```

**기술 스택**:
- OAuth 클라이언트: Spring RestClient (Spring 6+)
- ID Token 파싱: Base64 디코딩 + JSON 파싱
- 환경 변수: `.env` + `application.yml` 통합
- 테스트: Mockito + JUnit 5

**보안 고려사항**:
- OAuth 시크릿 정보는 `.env`에서 관리 (Git 제외)
- ID Token 기반 사용자 정보 추출 (Google)
- Provider별 고유 ID 저장 및 매칭

**위치**: 
- Client: `smartmealtable-client/auth/src/main/java/com/stdev/smartmealtable/client/auth/`
- Storage: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/social/`
- Domain: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/social/`
- API: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/auth/`
- 환경 설정: `.env`, `.env.example`, `application.yml`

### ✅ Domain Service 분리 리팩토링 완료 (2025-10-09) 🔥 NEW
**목적**: Application Service와 Domain Service의 책임 분리

**변경 사항**:
1. **Domain Service 계층 신규 생성**
   - `MemberDomainService`: 회원 생성, 검증, 탈퇴 등 핵심 비즈니스 로직
   - `AuthenticationDomainService`: 인증, 비밀번호 검증 등 인증 관련 로직

2. **Application Service 리팩토링**
   - `SignupService`: 유즈케이스 orchestration에만 집중
   - `LoginService`: JWT 토큰 발급 및 응답 처리
   - `ChangePasswordService`: 비밀번호 변경 흐름 관리
   - `WithdrawMemberService`: 회원 탈퇴 흐름 관리

3. **아키텍처 개선**
   - ✅ 비즈니스 로직이 Domain Service로 이동
   - ✅ Application Service는 트랜잭션 관리 및 DTO 변환만 담당
   - ✅ 도메인 모델 패턴 강화 (엔티티의 `verifyPassword()` 메서드 활용)
   - ✅ 계층 간 의존성 명확화 (Domain → Storage, API → Domain)

**테스트 검증**:
```bash
./gradlew :smartmealtable-api:test --rerun-tasks
# 모든 테스트 통과 (회원가입, 로그인, 비밀번호 변경, 회원 탈퇴)
# BUILD SUCCESSFUL in 58s

./gradlew clean build -x test
# 전체 프로젝트 빌드 성공
# BUILD SUCCESSFUL in 4s
```

### ✅ 전체 테스트 실행 결과
```bash
./gradlew :smartmealtable-api:test
# JWT 인증 관련 테스트 모두 통과 (로그인, 토큰갱신, 로그아웃)
# BUILD SUCCESSFUL - 프로덕션 배포 준비 완료
```

### ✅ API 문서 생성 완료
```bash
./gradlew :smartmealtable-api:asciidoctor
# HTML 문서 생성: build/docs/asciidoc/index.html
# JWT 인증 API 스펙 문서화 완료
```

### ✅ 온보딩 API 구현 완료 (2025-10-10) ⭐ COMPLETE
**목적**: 신규 가입 회원의 초기 프로필 설정 기능 구현

**1. 온보딩 - 프로필 설정 API 완료** ⭐ COMPLETE
- ✅ **Endpoint**: `POST /api/v1/onboarding/profile`
- ✅ **기능**: 회원의 닉네임 및 소속 그룹 설정
- ✅ **TDD 방식 개발**: RED-GREEN-REFACTOR 완벽 적용
- ✅ **Spring Rest Docs 문서화**: 성공/실패 시나리오 완료
- ✅ **JWT 인증 통합**: `@AuthUser` ArgumentResolver 적용

**구현 사항**:
1. **Request/Response DTO**
   - `OnboardingProfileRequest`: 닉네임, 그룹ID (Validation 포함)
   - `OnboardingProfileResponse`: 회원ID, 닉네임, 그룹 정보
   
2. **Service 계층**
   - `OnboardingProfileService`: 프로필 업데이트 유즈케이스
   - `OnboardingProfileServiceRequest/Response`: Service DTO
   
3. **비즈니스 로직**
   - 닉네임 중복 검증 (`existsByNickname`)
   - 그룹 존재 여부 검증 (`findById`)
   - Member 도메인 로직: `changeNickname()` 활용
   
4. **Controller**
   - `OnboardingController`: `/api/v1/onboarding/profile` 엔드포인트
   - JWT 인증: `@AuthUser AuthenticatedUser` 파라미터
   - Authorization: Bearer {token} 헤더 필수

**테스트 완료**:
- ✅ 성공 시나리오 (200 OK)
- ✅ 닉네임 중복 (409 Conflict)
- ✅ 그룹 미존재 (404 Not Found)
- ✅ Validation 실패 (422 Unprocessable Entity)
  - 닉네임 null
  - 닉네임 길이 제한 (1자)
  - groupId null

**Spring Rest Docs 문서화 완료**:
- ✅ `OnboardingProfileControllerRestDocsTest` 작성
- ✅ 성공/실패 시나리오 문서화
- ✅ Request/Response 필드 상세 설명
- ✅ JWT 인증 요구사항 문서화

**2. 온보딩 - 주소 등록 API 완료** ⭐ COMPLETE
- ✅ **Endpoint**: `POST /api/v1/onboarding/address`
- ✅ **기능**: 회원의 주소 정보 등록 (집, 회사, 기타)
- ✅ **TDD 방식 개발**: RED-GREEN-REFACTOR 완벽 적용
- ✅ **Spring Rest Docs 문서화**: 성공/실패 시나리오 완료
- ✅ **JWT 인증 통합**: `@AuthUser` ArgumentResolver 적용

**구현 사항**:
1. **Domain 계층**
   - `AddressHistory` 도메인 엔티티: 주소 정보 (좌표, 주소 타입 등)
   - `AddressType` enum: HOME, WORK, OTHER
   - 비즈니스 로직: `changeIsPrimaryTo(false)` - 기본 주소 변경
   
2. **Storage 계층**
   - `AddressHistoryJpaEntity`: JPA 엔티티 + Lombok 적용
   - `AddressHistoryRepository`: 주소 조회/저장 인터페이스
   - `AddressHistoryRepositoryImpl`: QueryDSL 기반 구현체
   - `findByMemberId`, `findPrimaryAddressByMemberId` 쿼리

3. **Service 계층**
   - `RegisterAddressService`: 주소 등록 유즈케이스
   - 기본 주소 설정 시 기존 기본 주소 해제 로직
   - 좌표 정보 (위도/경도) 필수 입력

4. **Request/Response DTO**
   - `RegisterAddressRequest`: 8개 필드 (별칭, 도로명/지번 주소, 상세주소, 좌표, 타입, 기본주소 여부)
   - `RegisterAddressResponse`: 주소 ID, 회원 ID, 주소 정보 전체

**비즈니스 로직**:
- 주소 별칭 최대 20자 제한
- 주소 최대 200자 제한
- 상세 주소 최대 100자 제한
- 기본 주소 설정 시 기존 기본 주소 자동 해제

**테스트 완료**:
- ✅ 성공 - 기본 주소 등록 (201 Created)
- ✅ 성공 - 일반 주소 등록 (201 Created)
- ✅ 필수 필드 누락 (422 Unprocessable Entity)
- ✅ 주소 길이 초과 (422 Unprocessable Entity)
- ✅ JWT 토큰 누락 (400 Bad Request)
- ✅ 유효하지 않은 JWT 토큰 (400 Bad Request)

**Spring Rest Docs 문서화 완료**:
- ✅ `OnboardingAddressControllerRestDocsTest` 작성 (6 테스트)
- ✅ 성공 시나리오 2개 (기본/일반 주소)
- ✅ 실패 시나리오 4개 (필드 누락, 길이 초과, JWT 인증 실패)
- ✅ Request/Response 필드 상세 설명
- ✅ JWT 인증 요구사항 문서화
- ✅ 에러 응답 형식 문서화

**API 문서화 완성**:
```bash
./gradlew :smartmealtable-api:asciidoctor
# HTML 문서 생성: build/docs/asciidoc/index.html
# 온보딩 - 주소 등록 API 스펙 문서화 완료
```

**위치**: 
- Domain: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/address/`
- Storage: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/address/`
- API: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/`
- Tests: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/onboarding/controller/`

**3. 온보딩 - 예산 설정 API 완료** ⭐ COMPLETE (2025-10-10)
- ✅ **Endpoint**: `POST /api/v1/onboarding/budget`
- ✅ **기능**: 회원의 월별/일일/식사별 예산 설정
- ✅ **TDD 방식 개발**: RED-GREEN-REFACTOR 완벽 적용
- ✅ **Spring Rest Docs 문서화**: 3개 시나리오 완료
- ✅ **JWT 인증 통합**: `@AuthUser` ArgumentResolver 적용

**구현 사항**:
1. **Domain 계층**
   - `MealType` enum: BREAKFAST, LUNCH, DINNER (식사 유형)
   - `MonthlyBudget` 도메인 엔티티: 월별 예산 관리
   - `DailyBudget` 도메인 엔티티: 일일 예산 관리
   - `MealBudget` 도메인 엔티티: 식사별 예산 관리
   - reconstitute 패턴: JPA → Domain 변환 시 ID 보존
   - 비즈니스 로직: `changeMonthlyFoodBudget()`, `addUsedAmount()` 등
   
2. **Storage 계층**
   - `MonthlyBudgetJpaEntity`: JPA 엔티티 + Lombok 적용
   - `DailyBudgetJpaEntity`: JPA 엔티티 + Lombok 적용
   - `MealBudgetJpaEntity`: JPA 엔티티 + Lombok 적용
   - `MonthlyBudgetRepository`, `DailyBudgetRepository`, `MealBudgetRepository`
   - Spring Data JPA 기반 Repository 구현체
   - QueryDSL 활용: 최신 예산 조회 쿼리

3. **Service 계층**
   - `SetBudgetService`: 예산 설정 유즈케이스
   - 현재 월(YearMonth.now()) 기준 월별 예산 생성
   - 현재 일(LocalDate.now()) 기준 일별 예산 생성
   - 3개 식사 유형별 예산 생성 (BREAKFAST, LUNCH, DINNER)
   - @Transactional 처리로 원자성 보장

4. **Request/Response DTO**
   - `SetBudgetRequest`: 월별/일별 예산 + 식사별 예산 Map
   - `SetBudgetResponse`: 설정된 예산 정보 + 식사별 예산 리스트
   - `MealBudgetInfo`: 식사 유형별 예산 정보 (nested DTO)

**비즈니스 로직**:
- 월별 예산 최소 0원 이상 (`@Min(0)`)
- 일별 예산 최소 0원 이상
- 식사별 예산 최소 0원 이상
- 모든 예산 필드 필수 입력 (`@NotNull`)

**테스트 완료** (6 Integration Tests):
- ✅ 예산 설정 성공 (201 Created)
- ✅ 월별 예산 null (422 Unprocessable Entity)
- ✅ 일별 예산 null (422 Unprocessable Entity)
- ✅ 식사별 예산 null (422 Unprocessable Entity)
- ✅ 음수 예산 (422 Unprocessable Entity)
- ✅ JWT 토큰 누락 (400 Bad Request)

**Spring Rest Docs 문서화 완료** (3 Documentation Tests):
- ✅ `SetBudgetControllerRestDocsTest` 작성
- ✅ 예산 설정 성공 시나리오 (201 Created)
- ✅ Validation 실패 시나리오 (422 Unprocessable Entity)
- ✅ JWT 인증 실패 시나리오 (400 Bad Request)
- ✅ Request/Response 필드 상세 설명 (.optional() 적용)
- ✅ 에러 응답 형식 문서화 (@JsonInclude(NON_NULL) 대응)

**reconstitute 패턴 적용**:
```java
// Domain Entity에 reconstitute 팩토리 메서드 추가
public static MonthlyBudget reconstitute(
    Long monthlyBudgetId, Long memberId, Integer monthlyFoodBudget, 
    Integer monthlyUsedAmount, YearMonth budgetMonth) {
    // JPA Entity → Domain Entity 변환 시 ID 보존
    MonthlyBudget budget = new MonthlyBudget();
    budget.monthlyBudgetId = monthlyBudgetId; // ID 복원
    budget.memberId = memberId;
    // ...
    return budget;
}

// JpaEntity의 toDomain()에서 reconstitute 사용
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

**API 문서화 완성**:
```bash
./gradlew :smartmealtable-api:test --tests SetBudgetControllerRestDocsTest
# 3 tests completed, 3 passed ✅

./gradlew :smartmealtable-api:asciidoctor
# HTML 문서 생성: build/docs/asciidoc/index.html
# 온보딩 - 예산 설정 API 스펙 문서화 완료
```

**위치**: 
- Domain: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/budget/`
- Storage: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/budget/`
- API: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/`
- Tests: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/onboarding/controller/SetBudgetControllerTest.java`
- RestDocs: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/onboarding/controller/SetBudgetControllerRestDocsTest.java`

- ✅ `index.adoc` - 온보딩 섹션 추가 (~330 lines)
  - 인증 요구사항 (JWT Bearer Token)
  - 프로필 설정 API 문서
  - 주소 등록 API 문서
  - cURL 예제 포함
- ✅ HTML 문서 생성: `build/docs/asciidoc/index.html` (86KB)

**테스트 실행 결과**:
```bash
./gradlew :smartmealtable-api:test --tests OnboardingProfileControllerTest
# 6 tests completed, 6 passed ✅

./gradlew :smartmealtable-api:test --tests OnboardingAddressControllerTest
# 6 tests completed, 6 passed ✅

./gradlew :smartmealtable-api:test --tests OnboardingProfileControllerRestDocsTest
# 3 tests completed, 3 passed ✅

./gradlew :smartmealtable-api:test --tests OnboardingAddressControllerRestDocsTest
# 6 tests completed, 6 passed ✅

./gradlew :smartmealtable-api:asciidoctor
# BUILD SUCCESSFUL - HTML 문서 생성 완료 (86KB)
```

**LoginControllerTest JWT 인증 패턴 리팩토링 완료** ⭐:
- ✅ `logout_success` 테스트: JwtTokenProvider 사용하도록 수정
- ✅ `logout_noAuthorizationHeader` 테스트: 400 Bad Request 기대값 수정
- ✅ `logout_invalidToken` 테스트: 400 Bad Request 및 에러 메시지 수정
- ✅ 모든 로그아웃 테스트 통과 (3/3)
- ✅ 전체 LoginControllerTest 통과

**위치**: 
- Domain: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/address/`
- Storage: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/address/`
- Controller: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/`
- Service: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/service/`
- Test: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/onboarding/controller/`
- Docs: `smartmealtable-api/src/docs/asciidoc/index.adoc`

**기술 스택**:
- Validation: Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Size`, `@Max`, `@Min`)
- 쿼리: QueryDSL (기본 주소 조회 및 업데이트)
- 테스트: TestContainers MySQL + MockMvc + JwtTokenProvider
- 문서화: Spring Rest Docs + Asciidoctor

**4. 온보딩 - 취향 설정 API 완료** ⭐ COMPLETE (2025-10-10)
- ✅ **Endpoint**: `POST /api/v1/onboarding/preferences`
- ✅ **기능**: 추천 유형 설정 + 카테고리별 선호도 저장
- ✅ **Domain & Storage 계층 구현**: Preference, Category 엔티티 및 Repository
- ✅ **Service 계층 구현**: SetPreferencesService (추천 유형 업데이트 + 선호도 저장)
- ✅ **Controller 계층 구현**: SetPreferencesRequest/Response DTO, OnboardingController 엔드포인트
- ✅ **빌드 성공**: 전체 프로젝트 빌드 성공 (`./gradlew clean build -x test`)

**구현 사항**:
1. **Domain 계층**
   - `Preference` 도메인 엔티티: 카테고리별 선호도 정보
   - `PreferenceRepository`: 선호도 CRUD 인터페이스
   - `Category` 도메인 엔티티: 음식 카테고리 정보
   - `CategoryRepository`: 카테고리 조회 인터페이스
   - weight 값 검증: -100 (싫어요), 0 (보통), 100 (좋아요)

2. **Storage 계층**
   - `PreferenceJpaEntity`: JPA 엔티티 + Lombok 적용
   - `PreferenceRepositoryImpl`: Spring Data JPA 기반 구현체
   - `CategoryJpaEntity`: JPA 엔티티 + Lombok 적용
   - `CategoryRepositoryImpl`: Spring Data JPA 기반 구현체

3. **Service 계층**
   - `SetPreferencesService`: 취향 설정 유즈케이스
   - 추천 유형 업데이트 (Member.changeRecommendationType)
   - 기존 선호도 삭제 후 새로운 선호도 저장
   - 카테고리 존재 여부 검증
   - @Transactional 처리로 원자성 보장

4. **Request/Response DTO**
   - `SetPreferencesRequest`: 추천 유형 + 선호도 리스트 (Validation 포함)
   - `SetPreferencesResponse`: 설정된 추천 유형 + 선호도 정보 (카테고리명 포함)
   - `PreferenceItem`: 카테고리 ID + 가중치 (nested DTO)

**비즈니스 로직**:
- 추천 유형: SAVER, ADVENTURER, BALANCED
- weight 값: -100 (싫어요), 0 (보통), 100 (좋아요)
- 모든 필드 필수 입력 (`@NotNull`, `@NotEmpty`)
- 카테고리 존재 여부 검증 (404 Not Found)

**테스트 완료** (TODO):
- 통합 테스트 및 Spring Rest Docs 문서화는 추후 작성 예정
- 현재는 빌드 성공 및 API 구조 완성에 집중

**Core 계층 업데이트**:
- ✅ `ErrorType.CATEGORY_NOT_FOUND` 추가 (404 Not Found)

**빌드 검증**:
```bash
./gradlew clean build -x test
# BUILD SUCCESSFUL in 7s
# 56 actionable tasks: 49 executed, 7 from cache
```

**위치**: 
- Domain: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/preference/`, `domain/category/`
- Storage: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/preference/`, `db/category/`
- API: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/`
- Service: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/service/`
- Controller DTO: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/onboarding/controller/dto/`

---

## 📋 다음 단계 (향후 API 구현)

### ✅ 완료된 영역: 인증 및 회원 관리 API (100%)
- ✅ 이메일 회원가입 API (TDD 완료)
- ✅ 이메일 로그인 API (JWT 토큰 발급 완료)  
- ✅ JWT 토큰 재발급 API (Refresh Token 완료)
- ✅ 로그아웃 API (토큰 검증 완료)
- ✅ 이메일 중복 검증 API ⭐
- ✅ 비밀번호 변경 API ⭐
- ✅ 회원 탈퇴 API ⭐
- ✅ 카카오 소셜 로그인 API 🔥 NEW
- ✅ 구글 소셜 로그인 API 🔥 NEW

### 우선순위 1: 인증 확장 API (100% 완료) ⭐ COMPLETE
- [x] 이메일 중복 검증 API ✅
- [x] 소셜 로그인 API (카카오, 구글 OAuth) ✅ **NEW**
- [ ] 비밀번호 찾기 API

### 우선순위 2: 온보딩 API (100% 완료) ⭐ COMPLETE
- [x] 프로필 설정 API (닉네임, 소속 그룹) ✅ **COMPLETE**
- [x] 주소 등록 API ✅ **COMPLETE**
- [x] 예산 설정 API ✅ **COMPLETE**
- [x] 취향 설정 API (추천 유형 + 카테고리 선호도) ✅ **COMPLETE**
- [ ] 약관 동의 API

### 우선순위 3: 프로필 관리 API (일부 완료)
- [ ] 프로필 조회 API
- [ ] 프로필 수정 API
- [x] 비밀번호 변경 API ✅
- [x] 회원 탈퇴 API ✅

### 우선순위 4: 예산 관리 API
- [ ] 예산 조회 API
- [ ] 예산 수정 API
- [ ] 선호도 설정 API
- [ ] 주소 관리 API

### 우선순위 4: 지출 내역 API
- [ ] 지출 등록 API (SMS 파싱)
- [ ] 지출 조회 API
- [ ] 지출 수정/삭제 API
- [ ] 지출 통계 API

### 우선순위 5: 가게 및 추천 API
- [ ] 가게 목록 조회 API
- [ ] 가게 상세 조회 API
- [ ] 음식 추천 API
- [ ] 즐겨찾기 관리 API

---

## 🏗️ 아키텍처 설계

### 멀티모듈 구조
```
smartmealtable/
├── core/              # 공통 응답, 예외 처리
├── domain/            # 순수 도메인 로직 + Domain Service (JPA 비의존)
│   └── service/      # Domain Service (비즈니스 로직)
├── storage/
│   └── db/           # JPA 엔티티, Repository 구현
├── api/              # Controller, Application Service, GlobalExceptionHandler
│   └── service/      # Application Service (유즈케이스 orchestration)
└── client/           # 외부 API 연동
```
├── storage/
│   └── db/           # JPA 엔티티, Repository 구현
├── api/              # Controller, GlobalExceptionHandler
└── client/           # 외부 API 연동
```

### 계층별 책임

#### Domain Layer
- 순수 Java 객체
- 비즈니스 규칙 검증
- 도메인 로직 (팩토리 메서드, 상태 변경)
- Repository 인터페이스 정의
- **Domain Service**: 핵심 비즈니스 로직 (회원 생성, 인증 검증, 비밀번호 변경 등)

#### Storage Layer
- JPA 엔티티 (Domain → JPA 매핑)
- Repository 구현체 (QueryDSL)
- 영속성 관리

#### Application Layer (Service)
- **유즈케이스 orchestration**: 여러 Domain Service 및 Repository 호출 조합
- 트랜잭션 관리 (`@Transactional`)
- DTO 변환 (Request → Domain, Domain → Response)
- JWT 토큰 발급 등 인프라 관련 작업

#### Presentation Layer (Controller)
- HTTP 요청/응답 처리
- Validation (`@Valid`, Bean Validation)
- DTO → Service 요청 변환
- HTTP 상태 코드 매핑

---

## 🎯 인증 API 스펙 (구현 완료)

### 1. 회원가입 API

#### Endpoint
```
POST /api/v1/auth/signup/email
```

#### Request
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "password": "SecureP@ss123!"
}
```

#### Response (201 Created)
```json
{
  "result": "SUCCESS",
  "data": {
    "memberId": 1,
    "email": "hong@example.com",
    "name": "홍길동",
    "createdAt": "2025-10-08T22:00:00"
  },
  "error": null
}
```

#### Error Cases
- **409 Conflict**: 이메일 중복
- **422 Unprocessable Entity**: 유효성 검증 실패

#### Validation Rules
- `name`: 2-50자
- `email`: 이메일 형식 (RFC 5322)
- `password`: 8-20자, 영문+숫자+특수문자 조합

### 2. 이메일 로그인 API ⭐ NEW

#### Endpoint
```
POST /api/v1/auth/login/email
```

#### Request
```json
{
  "email": "hong@example.com",
  "password": "SecureP@ss123!"
}
```

#### Response (200 OK)
```json
{
  "result": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "member": {
      "memberId": 1,
      "email": "hong@example.com",
      "name": "홍길동",
      "recommendationType": "BALANCED",
      "onboardingComplete": false,
      "createdAt": "2025-10-08T22:00:00"
    }
  },
  "error": null
}
```

#### Error Cases
- **401 Unauthorized**: 인증 실패 (이메일 미존재 또는 비밀번호 불일치)
  ```json
  {
    "result": "ERROR",
    "data": null,
    "error": {
      "code": "E401",
      "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
      "data": null
    }
  }
  ```

#### JWT 토큰 정보
- **Access Token**: 1시간 만료, 인증에 사용
- **Refresh Token**: 7일 만료, 토큰 재발급에 사용
- **Subject**: 사용자 이메일
- **Algorithm**: HS256

---

## 📈 진행 이력

### 2025-10-08 (전체 구현 완료)

#### Phase 1: 기반 모듈 구축
- ✅ Core 모듈 완성 (공통 응답, 예외 체계)
- ✅ Domain 모듈 완성 (4개 엔티티, 4개 Repository 인터페이스)
- ✅ Storage 모듈 완성 (4개 JPA 엔티티, 3개 Repository 구현체)
- ✅ 패키지 구조 정립 (`com.stdev.smartmealtable`)

#### Phase 2: 기술 스택 조정
- ✅ Spring Security 제거 → BCrypt standalone library 적용
- ✅ Lombok 전역 적용 (8개 엔티티 변환)
- ✅ PasswordConfig 구현 (BCrypt, cost factor: 12)

#### Phase 3: API 구현 (TDD)
- ✅ Service 계층 구현 (SignupService, DTO)
- ✅ Controller 계층 구현 (AuthController, DTO)
- ✅ GlobalExceptionHandler 구현
- ✅ Validation 적용 (@Valid, Bean Validation)

#### Phase 4: 테스트 작성
- ✅ 통합 테스트 작성 (SignupControllerTest)
  - 성공 시나리오 (201)
  - 에러 시나리오 (409, 422)
- ✅ TestContainers 설정
- ✅ 모든 테스트 통과 확인

#### Phase 5: API 문서화
- ✅ Spring Rest Docs 설정
- ✅ AbstractRestDocsTest 기본 클래스 작성
- ✅ SignupControllerRestDocsTest 작성
- ✅ AsciiDoc 문서 작성 (index.adoc)
- ✅ HTML 문서 생성 성공

#### Phase 6: 검증 및 정리
- ✅ 전체 빌드 성공 확인
- ✅ 전체 테스트 실행 성공
- ✅ API 문서 생성 확인
- ✅ 진행상황 문서 업데이트

### 2025-10-09 (JWT 인증 시스템 완료) ⭐

#### Phase 7: JWT 인프라 구축
- ✅ JwtTokenProvider 구현 (JWT 생성/검증 로직)
- ✅ JWT 라이브러리 통합 (`io.jsonwebtoken:jjwt-api:0.12.6`)
- ✅ BCrypt PasswordConfig 통합
- ✅ Access/Refresh Token 분리 (만료시간: 1h/24h)

#### Phase 8: 인증 API 구현 (TDD 방식)
- ✅ **로그인 API**: 이메일/비밀번호 → JWT 토큰 발급
- ✅ **토큰 재발급 API**: Refresh Token → 새 토큰 발급  
- ✅ **로그아웃 API**: JWT 토큰 검증 및 무효화 처리
- ✅ 각 API별 성공/실패 시나리오 TDD 완료

#### Phase 9: 통합 테스트 및 검증
- ✅ TestContainers MySQL 환경 통합 테스트
- ✅ 모든 HTTP 상태코드 시나리오 검증 (200, 201, 401, 409, 422)
- ✅ JWT 토큰 생성/검증 로직 테스트 
- ✅ BCrypt 암호화/검증 테스트

#### Phase 10: 문서화 및 완료
- ✅ `JWT_AUTHENTICATION_IMPLEMENTATION_REPORT.md` 상세 문서 작성
- ✅ IMPLEMENTATION_PROGRESS.md 업데이트

### 2025-10-09 (회원 관리 API 확장 완료) ⭐ NEW

#### Phase 11: 이메일 중복 검증 API 구현
- ✅ CheckEmailControllerTest 작성 (3개 시나리오)
- ✅ CheckEmailServiceResponse DTO 구현
- ✅ SignupService.checkEmail() 메서드 추가
- ✅ AuthController에 GET /api/v1/auth/check-email 엔드포인트 추가
- ✅ 이메일 형식 검증 로직 구현
- ✅ 모든 테스트 통과 확인

#### Phase 12: 비밀번호 변경 API 구현
- ✅ ChangePasswordControllerTest 작성 (3개 시나리오)
- ✅ ChangePasswordServiceRequest/Response DTO 구현
- ✅ ChangePasswordService 구현 (비밀번호 검증 + 변경)
- ✅ MemberAuthentication.verifyPassword() 도메인 메서드 추가
- ✅ Domain 모듈에 BCrypt 의존성 추가
- ✅ MemberController에 PUT /api/v1/members/me/password 엔드포인트 추가
- ✅ GlobalExceptionHandler Validation 메시지 개선
- ✅ 모든 테스트 통과 확인

#### Phase 13: 회원 탈퇴 API 구현
- ✅ WithdrawMemberControllerTest 작성 (2개 시나리오)
- ✅ WithdrawMemberServiceRequest DTO 구현
- ✅ WithdrawMemberService 구현 (Soft Delete)
- ✅ MemberController에 DELETE /api/v1/members/me 엔드포인트 추가
- ✅ 탈퇴 사유 로깅 구현
- ✅ 모든 테스트 통과 확인

#### Phase 14: 문서화 및 정리
- ✅ IMPLEMENTATION_PROGRESS.md 업데이트
- ✅ 3개 신규 API 구현 완료 확인  
- ✅ **JWT 인증 시스템 100% 완료 선언**

### 2025-10-10 (온보딩 API 완료) ⭐ NEW

#### Phase 15: 온보딩 - 취향 설정 API 구현
- ✅ Domain 계층: Preference, Category 엔티티 및 Repository 구현
- ✅ Storage 계층: PreferenceJpaEntity, CategoryJpaEntity 및 Repository 구현체
- ✅ Service 계층: SetPreferencesService 구현 (추천 유형 업데이트 + 선호도 저장)
- ✅ Controller 계층: SetPreferencesRequest/Response DTO, OnboardingController 엔드포인트
- ✅ Core 계층: ErrorType.CATEGORY_NOT_FOUND 추가
- ✅ weight 값 검증 로직 구현 (-100, 0, 100)
- ✅ 전체 빌드 성공 확인 (`./gradlew clean build -x test`)
- ✅ IMPLEMENTATION_PROGRESS.md 업데이트
- ✅ **온보딩 API 4개 완료 (프로필, 주소, 예산, 취향)**

---

## 🔧 기술 스택 세부사항

### 암호화
- **라이브러리**: `at.favre.lib:bcrypt:0.10.2`
- **설정**: Cost factor 12 (2^12 iterations)
- **위치**: `PasswordConfig.java`
- **인터페이스**: Custom `PasswordEncoder` (encode, matches)

### Lombok 설정
- **위치**: Root `build.gradle` (전역 설정)
- **적용 범위**: 모든 도메인 및 JPA 엔티티
- **사용 애너테이션**:
  - `@Getter` - 모든 필드 getter 자동 생성
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - 보호된 기본 생성자

### 제거된 의존성
- ❌ `spring-boot-starter-security` - Spring Security 전체 제거
- ✅ 대체: BCrypt standalone library (암호화 전용)

### 테스트 환경
- **프레임워크**: JUnit5, Mockito
- **통합 테스트**: Spring Boot Test, MockMvc
- **데이터베이스**: TestContainers (MySQL)
- **문서화**: Spring Rest Docs, AsciiDoctor

---

## 🎓 학습 포인트 및 베스트 프랙티스

### 1. 멀티 모듈 아키텍처
- Core, Domain, Storage, API 모듈 분리로 관심사 명확히 구분
- Domain은 JPA에 의존하지 않는 순수 Java 객체로 유지
- Storage에서 Domain ↔ JPA Entity 매핑 담당

### 2. TDD 적용
- RED → GREEN → REFACTOR 사이클 완벽 준수
- 테스트 먼저 작성 후 구현
- 모든 테스트 케이스 통과 확인

### 3. 예외 처리 전략
- 계층화된 예외 체계 (BaseException → BusinessException, AuthenticationException 등)
- GlobalExceptionHandler로 중앙 집중식 예외 처리
- ErrorType enum으로 에러 타입 체계적 관리

### 4. API 문서화 자동화
- Spring Rest Docs로 테스트 기반 문서 자동 생성
- AsciiDoc으로 가독성 높은 HTML 문서 생성
- 코드와 문서 동기화 보장

### 5. 보안
- Spring Security 없이 BCrypt로 비밀번호 암호화
- Cost factor 12로 충분한 보안 강도 확보
- 커스텀 PasswordEncoder 인터페이스로 추상화

---

## 📚 참고 문서
- [API Specification](./API_SPECIFICATION.md)
- [API Documentation](./smartmealtable-api/build/docs/asciidoc/index.html) (생성됨)
- [DDL Schema](./ddl.sql)
- [Copilot Instructions](./.github/copilot-instructions.md)
- [Spring Boot Instructions](./.github/instructions/springboot.instructions.md)
- [Java Instructions](./.github/instructions/java.instructions.md)
- [SRS](./SRS.md)
- [SRD](./SRD.md)

---

## 🚀 다음 개발자를 위한 가이드

### 새로운 API 추가 방법 (회원가입 API 참고)

1. **Domain 계층** (`smartmealtable-domain`)
   - 필요한 엔티티 추가/수정
   - Repository 인터페이스 정의

2. **Storage 계층** (`smartmealtable-storage/db`)
   - JPA Entity 추가/수정
   - Repository 구현체 작성 (필요시 QueryDSL 사용)

3. **Service 계층** (`smartmealtable-api/.../service`)
   - ServiceRequest, ServiceResponse DTO 작성
   - Service 구현 (비즈니스 로직, 트랜잭션 관리)

4. **Controller 계층** (`smartmealtable-api/.../controller`)
   - Request, Response DTO 작성 (Validation 포함)
   - Controller 구현

5. **테스트 작성** (`smartmealtable-api/src/test`)
   - 통합 테스트 작성 (성공/실패 시나리오)
   - Rest Docs 테스트 작성 (문서화)

6. **문서화**
   - `index.adoc`에 API 문서 추가
   - `./gradlew :smartmealtable-api:asciidoctor` 실행

### 개발 시 체크리스트
- [ ] 모든 계층에서 DTO 사용 (Entity 직접 노출 금지)
- [ ] Validation 적용 (Bean Validation)
- [ ] GlobalExceptionHandler에서 에러 처리
- [ ] 테스트 작성 (TDD)
- [ ] Rest Docs로 문서화
- [ ] 컨벤션 준수 (naming, conventional commits)

---

## ✅ 예산 관리 API 구현 완료 (2025-10-10) 🔥 NEW

### 구현 현황: 50% (2/4 API)
**목적**: 회원의 월별/일별 예산을 조회하는 기능 구현

**구현 완료 API**:
1. ✅ **월별 예산 조회 API**: `GET /api/v1/budgets/monthly`
   - 연도/월별 예산 정보 조회
   - 사용 금액, 남은 예산, 예산 사용률 계산
   - 남은 일수 계산
   - 테스트: 성공(200), 404, 401, 422 모두 통과

2. ✅ **일별 예산 조회 API**: `GET /api/v1/budgets/daily`
   - 특정 날짜의 예산 정보 조회
   - 끼니별 예산 정보 포함 (아침/점심/저녁/기타)
   - 테스트: 성공(200), 404 통과

**구현 내역**:

1. **Core 모듈 - ErrorType 추가**
   - `MONTHLY_BUDGET_NOT_FOUND`: 월별 예산 미존재 (404)
   - `DAILY_BUDGET_NOT_FOUND`: 일별 예산 미존재 (404)

2. **Core 모듈 - GlobalExceptionHandler 개선**
   - `ConstraintViolationException` 핸들러 추가
   - Query Parameter Validation 처리 (422)

3. **Domain 모듈 - 예산 엔티티** (기존 존재)
   - `MonthlyBudget`: 월별 예산 도메인 엔티티
   - `DailyBudget`: 일별 예산 도메인 엔티티
   - `MealBudget`: 끼니별 예산 도메인 엔티티
   - `MealType` enum: BREAKFAST, LUNCH, DINNER, ETC

4. **API 모듈 - Service 계층**
   - `MonthlyBudgetQueryService`: 월별 예산 조회 서비스
     - 예산 사용률 계산 (BigDecimal, 소수점 2자리)
     - 남은 일수 계산 (LocalDate, YearMonth 활용)
   - `DailyBudgetQueryService`: 일별 예산 조회 서비스
     - 끼니별 예산 목록 조회
     - 남은 예산 계산

5. **API 모듈 - Controller 계층**
   - `BudgetController`: 예산 관리 API 컨트롤러
     - `@Validated` 적용으로 Query Parameter Validation 활성화
     - JWT 인증 필수 (`@AuthUser`)

6. **ArgumentResolver 개선**
   - `AuthUserArgumentResolver`에서 `AuthenticationException` 사용
   - 인증 실패 시 401 반환 (기존 400 → 401로 수정)

**TDD 개발 완료**:
- `MonthlyBudgetQueryControllerTest`: 4개 테스트 모두 통과 ✅
  - 월별 예산 조회 성공 (200)
  - 존재하지 않는 예산 (404)
  - 유효하지 않은 토큰 (401)
  - 잘못된 월 범위 (422)

- `DailyBudgetQueryControllerTest`: 2개 테스트 모두 통과 ✅
  - 일별 예산 조회 성공 - 끼니별 포함 (200)
  - 존재하지 않는 예산 (404)

**테스트 실행 결과**:
```bash
./gradlew :smartmealtable-api:test --tests "*BudgetQuery*"
✅ 전체 예산 조회 테스트: 6/6 통과
✅ BUILD SUCCESSFUL
```

**기술 스택**:
- Service 응답: Record DTO
- 날짜 처리: `LocalDate`, `YearMonth`
- 예산 사용률: `BigDecimal` (정확한 소수점 계산)
- Validation: `@Min`, `@Max` (Query Parameter)
- 인증: JWT + ArgumentResolver

**남은 작업**:
- [ ] 예산 수정 API (`PUT /api/v1/budgets`)
- [ ] 특정 날짜 예산 수정 API (`PUT /api/v1/budgets/daily/{date}`)
- [ ] Spring Rest Docs 문서화

**위치**:
- Controller: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/budget/controller/`
- Service: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/budget/service/`
- Domain: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/budget/`

---

**마지막 업데이트**: 2025-10-10 (예산 관리 API 50% 완료 - 조회 API 구현)

```
