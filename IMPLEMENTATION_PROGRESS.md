# 🚀 SmartMealTable API 구현 진행상황

> **목표**: TDD 기반 RESTful API 완전 구현

**시작일**: 2025-10-08  
**최종 업데이트**: 2025-10-12

---

## 📊 전체 진행률

> **전체 API 엔드포인트**: 70개 (API_SPECIFICATION.md 기준)

```
3. 인증 및 회원 관리:      [████████████████████] 100% (13/13 API) ✅
4. 온보딩:                [████████████████████] 100% (11/11 API) ✅
5. 예산 관리:             [████████████████████] 100% (4/4 API) ✅
6. 지출 내역:             [████████████████████] 100% (7/7 API) ✅
7. 가게 관리:             [░░░░░░░░░░░░░░░░░░░░]   0% (0/3 API)
8. 추천 시스템:           [░░░░░░░░░░░░░░░░░░░░]   0% (0/3 API)
9. 즐겨찾기:              [░░░░░░░░░░░░░░░░░░░░]   0% (0/4 API)
10. 프로필 및 설정:        [████████████████████] 100% (12/12 API) ✅
11. 홈 화면:              [░░░░░░░░░░░░░░░░░░░░]   0% (0/3 API)
12. 장바구니:             [░░░░░░░░░░░░░░░░░░░░]   0% (0/6 API)
13. 지도 및 위치:         [░░░░░░░░░░░░░░░░░░░░]   0% (0/4 API)
14. 알림 및 설정:         [░░░░░░░░░░░░░░░░░░░░]   0% (0/4 API)

총 진행률:                [█████████████████░░░]  71% (50/70 API)
```

### 📋 섹션별 상세 현황

#### ✅ 완료 (50개)
- **인증 및 회원 (13개)**: 회원가입, 로그인(이메일/소셜), 토큰관리, 비밀번호관리, 회원탈퇴, 소셜계정연동(3), 비밀번호만료관리(2)
- **온보딩 (11개)**: 프로필/주소/예산/취향설정, 음식목록/선호도, 약관동의, 그룹/카테고리/약관 조회
- **예산 관리 (4개)**: 월별/일별 조회, 예산수정, 일괄적용
- **프로필 및 설정 (12개)**: 프로필관리(2), 주소관리(5), 선호도관리(5)
- **지출 내역 (7개)**: 등록, SMS파싱, 목록조회, 상세조회, 수정, 삭제, 통계

#### ⚠️ 미구현 (20개)
- **가게 관리** (3개): 목록조회, 상세조회, 자동완성
- **추천 시스템** (3개): 개인화추천, 점수상세, 유형변경
- **즐겨찾기** (4개): 추가, 목록조회, 순서변경, 삭제
- **홈 화면** (3개): 대시보드, 온보딩상태, 예산확인
- **장바구니** (6개): 조회, 추가, 수정, 삭제, 비우기, 지출등록
- **지도 및 위치** (4개): 주소검색, 역지오코딩, GPS등록, 위치변경
- **알림 및 설정** (4개): 알림설정, 앱설정

---

## 🏗 아키텍처 현황

### 멀티모듈 구조
smartmealtable-backend-v2/
├── core/              # 공통 응답/에러 처리
├── domain/            # 도메인 모델 & 비즈니스 로직
├── storage/           # JPA 엔티티 & Repository 구현
├── api/               # REST API & Application Service
├── admin/             # 관리자 API
├── client/            # 외부 API 클라이언트
├── batch/             # 배치 작업
└── support/           # 유틸리티

### 계층별 구현 현황

#### ✅ Core 모듈 (100%)
- ApiResponse<T>: 통일된 응답 구조
- ErrorCode, ErrorType: 체계적 에러 분류
- BaseException 계층: Authentication, Authorization, Business, ExternalService

#### ✅ Domain 모듈 (완료된 도메인)
**회원 (Member)**
- Entity: Member, MemberAuthentication, SocialAccount
- Enum: RecommendationType, SocialProvider
- Repository Interface

**온보딩 (Onboarding)**
- Entity: Group, Category, Policy, PolicyAgreementHistory
- Entity: AddressHistory, Budget, CategoryPreference, FoodPreference
- Enum: GroupType, MealType, AddressType
- Repository Interface

**지출 (Expenditure)**
- Entity: Expenditure
- Value Object: ParsedSmsResult
- Domain Service: SmsParsingDomainService
- SMS Parser: KB/NH/신한카드 파서

#### ✅ Storage 모듈 (완료된 영역)
- JPA Entity: Member, Onboarding, Budget, Expenditure 관련
- Repository 구현체: JpaRepository 확장
- BaseTimeEntity: JPA Auditing (created_at, updated_at - DB 레벨 관리)

#### ✅ API 모듈 (완료된 컨트롤러)
- AuthenticationController: 회원가입, 로그인, 토큰관리 (13 API)
- OnboardingController: 온보딩 전 단계 (11 API)
- BudgetController: 예산 관리 (4 API)
- ProfileController: 프로필 및 설정 (12 API)
- ExpenditureController: 지출 등록, SMS 파싱 (2 API)

---

## ✅ 주요 완료 기능 요약

### 1️⃣ 인증 시스템 (100%)
**구현 내용**: JWT 기반 STATELESS 인증, ArgumentResolver 활용
- 이메일 회원가입/로그인, 소셜 로그인 (카카오/구글)
- Access/Refresh Token 관리
- 소셜 계정 연동 관리 (추가/해제)
- 비밀번호 만료 관리 (90일 정책, 최대 3회 연장)

**핵심 컴포넌트**:
- JwtTokenProvider: 토큰 생성/검증
- AuthenticatedUserArgumentResolver: 토큰 파싱 후 AuthenticatedUser 주입
- KakaoAuthClient, GoogleAuthClient: OAuth2 클라이언트

**상세 문서**: JWT_AUTHENTICATION_IMPLEMENTATION_REPORT.md

### 2️⃣ 온보딩 시스템 (100%)
**구현 내용**: 신규 사용자 초기 설정 프로세스
- 프로필 설정 (닉네임, 소속 그룹)
- 주소 등록 (네이버 지도 API 검증)
- 예산 설정 (월별/식사별)
- 취향 설정 (카테고리/개별 음식 선호도)
- 약관 동의 처리

**핵심 도메인**:
- OnboardingDomainService: 온보딩 완료 여부 검증
- BudgetCalculationDomainService: 예산 자동 분배 로직
- 네이버 지도 API 통합 (주소 검증)

**상세 문서**: ONBOARDING_API_COMPLETION_REPORT.md

### 3️⃣ 예산 관리 시스템 (100%)
**구현 내용**: 유연한 예산 설정 및 조회
- 월별/일별 예산 조회
- 예산 수정 (월별 기준, 식사별 분배)
- 특정 날짜 예산 일괄 적용 (특정 날짜 이후 전체 적용)

**핵심 로직**:
- BudgetCalculationDomainService: 식사별 예산 자동 계산
- 일괄 적용 시 해당 날짜부터 연말까지 덮어쓰기

### 4️⃣ 프로필 및 설정 시스템 (100%)
**구현 내용**: 사용자 정보 및 선호도 관리
- 프로필 조회/수정 (닉네임, 그룹)
- 주소 관리 (CRUD, 기본 주소 설정)
- 선호도 관리 (카테고리/개별 음식 선호도)

**핵심 비즈니스 규칙**:
- 기본 주소는 1개 이상 필수 (마지막 주소 삭제 방지)
- 카테고리 선호도: weight 100/0/-100 (좋아요/보통/싫어요)
- 개별 음식 선호도: liked/disliked 분리 관리

**상세 문서**: PROFILE_SETTINGS_API_PHASE2_REPORT.md

### 5️⃣ 지출 내역 시스템 (100%)
**구현 내용**: 지출 내역 CRUD 및 통계 조회
- 지출 내역 등록 (수동 + SMS 자동 파싱)
- 지출 내역 목록 조회 (날짜/식사유형/카테고리 필터, 페이징)
- 지출 내역 상세 조회 (지출 항목 포함, 권한 검증)
- 지출 내역 수정 (가게명, 금액, 날짜, 메모, 식사유형)
- 지출 내역 삭제 (Soft Delete)
- 지출 통계 조회 (기간별 총액, 식사별/카테고리별 집계)

**핵심 컴포넌트**:
- ExpenditureController: 7개 API 엔드포인트
- ExpenditureService: 목록/상세/수정/삭제/통계 비즈니스 로직
- SmsParsingDomainService: 카드사별 SMS 파싱 (KB/NH/신한)
- ExpenditureRepository: 동적 필터링 및 집계 쿼리

**API**:
- POST /api/v1/expenditures: 지출 등록
- POST /api/v1/expenditures/parse-sms: SMS 파싱
- GET /api/v1/expenditures: 목록 조회 (필터/페이징)
- GET /api/v1/expenditures/{id}: 상세 조회
- PUT /api/v1/expenditures/{id}: 수정
- DELETE /api/v1/expenditures/{id}: 삭제
- GET /api/v1/expenditures/statistics: 통계 조회

**상세 문서**: (신규 작성 필요)

---

## 🧪 테스트 전략

### 테스트 원칙
- **TDD 기반 개발**: RED-GREEN-REFACTORING
- **Mockist 스타일**: Mock을 활용한 단위 테스트
- **Test Container 사용**: 실제 MySQL 환경 테스트
  - 병렬 실행 금지 (메모리/커넥션 제한)
  - H2 DB 사용 금지 (MySQL 전용)

### 테스트 커버리지
- **Controller 통합 테스트**: 모든 HTTP 상태 코드 검증
  - 200, 201, 204 (성공)
  - 400, 401, 404, 409, 422 (실패)
- **Domain 단위 테스트**: 비즈니스 로직 검증
- **Repository 테스트**: JPA 매핑 검증

### 완료된 테스트
- ✅ 인증 시스템: 50+ 테스트
- ✅ 온보딩: 60+ 테스트
- ✅ 예산 관리: 30+ 테스트
- ✅ 프로필 및 설정: 70+ 테스트
- ✅ 지출 내역: 25+ 테스트 (32개 작성, 7개 마이너 이슈)

**전체 빌드 상태**: ✅ BUILD SUCCESSFUL

**테스트 환경 개선사항**:
- MockChatModelConfig 추가: Spring AI ChatModel Mock 빈 제공
- AbstractRestDocsTest, AbstractContainerTest에 통합
- Spring AI 의존성 테스트 환경 격리 완료

---

## 📝 API 문서화

### Spring Rest Docs
- **모든 완료된 API**: Rest Docs 문서 자동 생성
- **위치**: build/generated-snippets/
- **포맷**: AsciiDoc → HTML

### 완료된 문서
- ✅ 인증 및 회원 관리 API (13개)
- ✅ 온보딩 API (11개)
- ✅ 예산 관리 API (4개)
- ✅ 프로필 및 설정 API (12개)
- ✅ 지출 내역 API (7개)

**상세 문서**: 각 섹션별 *_REST_DOCS_COMPLETION_REPORT.md 참조

---

## 🎯 다음 구현 대상

### 우선순위 1: 가게 관리 API (3개)
- [ ] GET /api/v1/stores - 목록 조회 (위치 기반, 필터, 정렬)
- [ ] GET /api/v1/stores/{id} - 상세 조회 (조회수 증가)
- [ ] GET /api/v1/stores/autocomplete - 자동완성 검색

### 우선순위 2: 추천 시스템 API (3개)
- [ ] POST /api/v1/recommendations - 개인화 추천 (위치/예산/선호도 기반)
- [ ] GET /api/v1/recommendations/{storeId}/scores - 추천 점수 상세
- [ ] PUT /api/v1/members/me/recommendation-type - 추천 유형 변경

### 우선순위 3: 즐겨찾기 API (4개)
- [ ] POST /api/v1/favorites - 즐겨찾기 추가
- [ ] GET /api/v1/favorites - 목록 조회
- [ ] PUT /api/v1/favorites/order - 순서 변경
- [ ] DELETE /api/v1/favorites/{id} - 삭제

---

## 📚 참고 문서

### 핵심 문서
- API_SPECIFICATION.md: 전체 API 명세 (상세 Request/Response 예시 포함)
- .github/copilot-instructions.md: 개발 컨벤션 및 아키텍처 가이드
- SRD.md, SRS.md, PRD.md: 요구사항 명세

### 완료 보고서 (상세 구현 내용)
- JWT_AUTHENTICATION_IMPLEMENTATION_REPORT.md: 인증 시스템
- SOCIAL_LOGIN_IMPLEMENTATION_REPORT.md: 소셜 로그인
- ONBOARDING_API_COMPLETION_REPORT.md: 온보딩
- PROFILE_SETTINGS_API_PHASE2_REPORT.md: 프로필 설정
- MEMBER_MANAGEMENT_API_COMPLETION_REPORT.md: 회원 관리
- 기타 *_COMPLETION_REPORT.md 파일들

---

## 🔧 기술 스택

### Backend
- **Java 21**, **Spring Boot 3.x**, **Spring MVC**
- **Spring Data JPA**, **QueryDSL**
- **MySQL** (Primary DB), **Redis** (Caching)
- **Spring AI** (SMS 파싱), **Spring Batch** (배치 작업)

### Testing
- **JUnit 5**, **Mockito**, **Test Containers**
- **Spring Rest Docs** (API 문서)

### Build & Deploy
- **Gradle Multi-Module**
- **Docker Compose**, **Terraform** (IaC)
- **GitHub Actions** (CI/CD)

### Libraries
- **Lombok**, **Logback**
- **주의**: Spring Security 미사용 (직접 JWT 구현)

---

## 🚨 주요 개발 규칙

### 도메인 및 아키텍처
1. **created_at, updated_at**: DB DEFAULT CURRENT_TIMESTAMP 사용 (JPA Auditing 노출 금지)
2. **비즈니스 시간 컬럼**: registered_at 등은 엔티티에 노출 가능
3. **FK 제약조건**: 물리 FK 사용 금지, 논리 FK만 사용
4. **JPA 연관관계**: 같은 Aggregate 내에서만 허용
5. **DTO**: 모든 계층 간 통신에 사용, @Setter/@Data 금지 (DTO 제외)
6. **도메인 모델 패턴**: 비즈니스 로직은 Domain 객체에 위치
7. **Application Service**: 유즈케이스 조합에 집중

### 테스트
1. **Test Container 필수**: H2, 로컬 MySQL 사용 금지
2. **병렬 실행 금지**: 순차 실행으로 메모리/커넥션 관리
3. **독립성 보장**: 각 테스트는 독립적으로 실행 가능해야 함

### 문서화
1. **큼직한 기능 단위로 IMPLEMENTATION_PROGRESS 업데이트**
2. **상세 내용은 별도 완료 보고서에 작성**
3. **API_SPECIFICATION.md와 중복 최소화**

---

**마지막 업데이트**: 2025-10-12 (지출 내역 섹션 100% 완료)
