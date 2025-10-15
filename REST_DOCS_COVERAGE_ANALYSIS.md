# REST Docs 문서 커버리지 분석 보고서

**분석일시**: 2025-10-15  
**분석 대상**: API 명세서 vs REST Docs 생성 문서

---

## 📊 요약

### 현재 상태
- **API 명세서 정의**: 79개 API 엔드포인트
- **REST Docs 테스트 파일**: 30개
- **현재 REST Docs 문서화 범위**: 약 14% (11개 섹션 중 3개만 문서화)

### 결론
❌ **REST Docs 문서가 실제 API 명세를 모두 반영하고 있지 않습니다.**

---

## 🔍 상세 분석

### 1. API 명세서 섹션별 정의 (총 14개 섹션, 79개 API)

| 섹션 | API 개수 | 비고 |
|------|----------|------|
| 3. 인증 및 회원 관리 API | 13개 | 회원가입, 로그인, 토큰 갱신, 비밀번호 변경 등 |
| 4. 온보딩 API | 11개 | 프로필, 주소, 예산, 취향 설정 등 |
| 5. 예산 관리 API | 4개 | 월별/일별 예산 조회 및 수정 |
| 6. 지출 내역 API | 7개 | SMS 파싱, 지출 내역 CRUD, 통계 |
| 7. 가게 관리 API | 3개 | 목록 조회, 상세 조회, 검색 |
| 8. 추천 시스템 API | 3개 | 개인화 추천, 점수 상세, 유형 변경 |
| 9. 즐겨찾기 API | 4개 | 추가, 조회, 순서 변경, 삭제 |
| 10. 프로필 및 설정 API | 12개 | 회원정보, 주소, 취향, 테마 등 |
| 11. 홈 화면 API | 3개 | 대시보드, 온보딩 상태, 예산 확인 |
| 12. 장바구니 API | 6개 | 장바구니 CRUD 및 관리 |
| 13. 지도 및 위치 API | 2개 | 주소 검색, 역지오코딩 |
| 14. 알림 및 설정 API | 4개 | 알림 설정, 앱 설정 |
| **기타 (중복 제거 후)** | 7개 | 약관, 그룹, 카테고리 등 |

### 2. 현재 REST Docs 문서화 범위

#### ✅ 문서화 완료된 섹션 (3개)

1. **인증 (Authentication)**
   - ✅ 이메일 회원가입 (POST /api/v1/auth/signup/email)
   
2. **온보딩 (Onboarding)**
   - ✅ 프로필 설정 (POST /api/v1/onboarding/profile)
   - ✅ 주소 등록 (POST /api/v1/onboarding/address)
   - ✅ 예산 설정 (POST /api/v1/onboarding/budget)

3. **가게 관리 (Store)**
   - ✅ 가게 목록 조회 (GET /api/v1/stores)
   - ✅ 가게 상세 조회 (GET /api/v1/stores/{storeId})
   - ✅ 가게 자동완성 검색 (GET /api/v1/stores/autocomplete)

#### ❌ 문서화 누락된 섹션 (11개)

1. **인증 및 회원 관리** (12개 API 누락)
   - ❌ 이메일 로그인
   - ❌ 소셜 로그인 (카카오, 구글)
   - ❌ 토큰 갱신
   - ❌ 로그아웃
   - ❌ 이메일 중복 검증
   - ❌ 비밀번호 변경
   - ❌ 회원 탈퇴
   - ❌ 소셜 계정 연동 관리 (3개 API)
   - ❌ 비밀번호 만료 관리 (2개 API)

2. **온보딩** (7개 API 누락)
   - ❌ 취향 설정
   - ❌ 약관 동의
   - ❌ 그룹 목록 조회
   - ❌ 카테고리 목록 조회
   - ❌ 약관 조회
   - ❌ 음식 목록 조회
   - ❌ 개별 음식 선호도 저장

3. **예산 관리** (4개 API 전체 누락)
   - ❌ 월별 예산 조회
   - ❌ 일별 예산 조회
   - ❌ 예산 수정
   - ❌ 특정 날짜 예산 수정

4. **지출 내역** (7개 API 전체 누락)
   - ❌ SMS 파싱
   - ❌ 지출 내역 등록
   - ❌ 지출 내역 조회 (목록)
   - ❌ 지출 내역 상세 조회
   - ❌ 지출 내역 수정
   - ❌ 지출 내역 삭제
   - ❌ 일별 지출 통계 조회

5. **추천 시스템** (3개 API 전체 누락)
   - ❌ 개인화 추천
   - ❌ 추천 점수 상세 조회
   - ❌ 추천 유형 변경

6. **즐겨찾기** (4개 API 전체 누락)
   - ❌ 즐겨찾기 추가
   - ❌ 즐겨찾기 목록 조회
   - ❌ 즐겨찾기 순서 변경
   - ❌ 즐겨찾기 삭제

7. **프로필 및 설정** (12개 API 전체 누락)
   - ❌ 회원 정보 조회/수정
   - ❌ 닉네임 변경
   - ❌ 주소 관리 (CRUD)
   - ❌ 프로필 이미지 업로드
   - ❌ 음식 취향 관리
   - ❌ 테마 설정

8. **홈 화면** (3개 API 전체 누락)
   - ❌ 홈 대시보드
   - ❌ 온보딩 상태 조회
   - ❌ 월간 예산 확인

9. **장바구니** (6개 API 전체 누락)
   - ❌ 장바구니 추가
   - ❌ 장바구니 조회
   - ❌ 장바구니 수정
   - ❌ 장바구니 삭제
   - ❌ 장바구니 전체 삭제
   - ❌ 주문하기

10. **지도 및 위치** (2개 API 전체 누락)
    - ❌ 주소 검색 (Geocoding)
    - ❌ 좌표→주소 변환 (Reverse Geocoding)

11. **알림 및 설정** (4개 API 전체 누락)
    - ❌ 알림 설정 조회
    - ❌ 알림 설정 변경
    - ❌ 앱 설정 조회
    - ❌ 사용자 추적 설정

---

## 🔬 REST Docs 테스트 파일 vs 실제 문서화 상태

### 존재하는 REST Docs 테스트 파일 (30개)

| 테스트 파일 | 문서화 여부 | 비고 |
|------------|------------|------|
| SignupControllerRestDocsTest.java | ✅ | index.adoc에 포함 |
| OnboardingProfileControllerRestDocsTest.java | ✅ | index.adoc에 포함 |
| OnboardingAddressControllerRestDocsTest.java | ✅ | index.adoc에 포함 |
| SetBudgetControllerRestDocsTest.java | ✅ | index.adoc에 포함 |
| StoreControllerRestDocsTest.java | ✅ | index.adoc에 포함 |
| LoginControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| KakaoLoginControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| GoogleLoginControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| RefreshTokenControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| LogoutControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| CheckEmailControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| MemberControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| SocialAccountControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| PasswordExpiryControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| AddressControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| PreferenceControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| FoodPreferenceControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| GroupControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| CategoryControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| PolicyControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| BudgetControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| ExpenditureControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| RecommendationControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| FavoriteControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| HomeControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| CartControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| MapControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| NotificationSettingsControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |
| AppSettingsControllerRestDocsTest.java | ❌ | index.adoc에 미포함 |

### 문제점 분석

1. **테스트 파일은 존재하지만 문서화되지 않음**
   - 30개 테스트 파일 중 25개(83%)가 index.adoc에 포함되지 않음
   - 테스트는 작성되어 실행되지만, 최종 HTML 문서에 반영 안 됨

2. **index.adoc 구조 문제**
   - 현재 11개 앵커만 정의 ([[overview]], [[auth]], [[onboarding]], [[store]] 등)
   - 나머지 11개 섹션에 대한 include 문 누락

---

## 📋 개선 방안

### 1. 즉시 조치 사항 (High Priority)

#### A. index.adoc 완성
기존 테스트 snippets를 활용하여 문서 섹션 추가:

```asciidoc
[[auth-login]]
== 로그인

=== 이메일 로그인
include::{snippets}/auth/login/email/success/http-request.adoc[]
...

[[auth-social]]
== 소셜 로그인

=== 카카오 로그인
include::{snippets}/auth/login/kakao/success/http-request.adoc[]
...

[[budget]]
= 예산 관리

[[budget-monthly]]
== 월별 예산 조회
include::{snippets}/budget/monthly/success/http-request.adoc[]
...

[[expenditure]]
= 지출 내역

[[expenditure-list]]
== 지출 내역 조회
include::{snippets}/expenditure/list/success/http-request.adoc[]
...

[[recommendation]]
= 추천 시스템

[[recommendation-list]]
== 개인화 추천
include::{snippets}/recommendation/list/success/http-request.adoc[]
...

[[favorite]]
= 즐겨찾기

[[favorite-add]]
== 즐겨찾기 추가
include::{snippets}/favorite/add/success/http-request.adoc[]
...

[[home]]
= 홈 화면

[[home-dashboard]]
== 홈 대시보드
include::{snippets}/home/dashboard/success/http-request.adoc[]
...

[[cart]]
= 장바구니

[[cart-add]]
== 장바구니 추가
include::{snippets}/cart/add/success/http-request.adoc[]
...

[[map]]
= 지도 및 위치

[[map-geocoding]]
== 주소 검색
include::{snippets}/map/geocoding/success/http-request.adoc[]
...

[[notification]]
= 알림 및 설정

[[notification-settings]]
== 알림 설정 조회
include::{snippets}/notification/settings/get/success/http-request.adoc[]
...
```

#### B. 문서 생성 및 배포 스크립트 수정

`deploy-docs.sh` 개선:
- 모든 REST Docs 테스트 실행 (현재는 일부만)
- snippets 디렉토리 검증 강화
- 누락된 snippets 자동 탐지

### 2. 중기 개선 사항 (Medium Priority)

#### A. 섹션별 문서화 작업 순서

**Phase 1: 핵심 기능 (2-3일)**
1. 인증 및 회원 관리 (13개 API)
2. 예산 관리 (4개 API)
3. 지출 내역 (7개 API)

**Phase 2: 사용자 경험 (2-3일)**
4. 추천 시스템 (3개 API)
5. 즐겨찾기 (4개 API)
6. 홈 화면 (3개 API)

**Phase 3: 부가 기능 (1-2일)**
7. 장바구니 (6개 API)
8. 지도 및 위치 (2개 API)
9. 알림 및 설정 (4개 API)

**Phase 4: 설정 및 관리 (1일)**
10. 프로필 및 설정 (12개 API)

#### B. 문서화 품질 개선

각 API마다 포함할 내용:
- ✅ 요청/응답 예시
- ✅ 필드 설명 (request-fields.adoc, response-fields.adoc)
- ✅ 에러 케이스 (최소 2-3개)
- ✅ cURL 예제
- ✅ 인증 헤더 정보
- ✅ Query/Path 파라미터 설명

### 3. 장기 개선 사항 (Low Priority)

#### A. 자동화 도구 도입
- Swagger/OpenAPI 병행 고려
- CI/CD 파이프라인에 문서 생성 자동화
- 문서 커버리지 리포트 자동 생성

#### B. 문서 구조 개선
- 버전별 문서 분리 (v1, v2)
- Postman Collection 자동 생성
- Interactive API 문서 (Swagger UI)

---

## 📈 작업 추정

### 소요 시간 예측

| 작업 | 예상 시간 | 우선순위 |
|------|-----------|----------|
| index.adoc 섹션 추가 (25개 API) | 4-6시간 | 🔴 High |
| 에러 케이스 문서화 보완 | 2-3시간 | 🔴 High |
| cURL 예제 추가 | 2-3시간 | 🟡 Medium |
| 문서 검토 및 QA | 2시간 | 🟡 Medium |
| deploy-docs.sh 개선 | 1시간 | 🟢 Low |

**총 예상 시간**: 11-15시간 (약 2일)

---

## ✅ 액션 아이템

### 즉시 착수
1. [ ] index.adoc에 누락된 25개 API 섹션 추가
2. [ ] 각 섹션별 include 문 작성
3. [ ] snippets 디렉토리 구조 확인

### 1주일 내
4. [ ] 에러 응답 문서화 보완
5. [ ] cURL 예제 추가
6. [ ] 문서 일관성 검토

### 2주일 내
7. [ ] deploy-docs.sh 스크립트 개선
8. [ ] GitHub Pages 자동 배포 설정
9. [ ] 문서 커버리지 리포트 자동화

---

## 📝 참고 자료

- API 명세서: `API_SPECIFICATION.md`
- 구현 진척도: `IMPLEMENTATION_PROGRESS.md`
- REST Docs 소스: `smartmealtable-api/src/docs/asciidoc/index.adoc`
- 생성된 HTML: `docs/api-docs.html`
- 배포 스크립트: `deploy-docs.sh`

---

## 🎯 결론

**현재 REST Docs 문서화율: 약 14%**

실제 API 구현은 100% 완료되었으나, REST Docs 문서화는 매우 초기 단계입니다.
- ✅ 테스트 코드: 30개 파일 존재 (100% 통과)
- ❌ 문서화: 3개 섹션만 포함 (14%)

**핵심 문제**: 
테스트 파일과 snippets는 존재하지만, `index.adoc`에 include 문이 누락되어 최종 HTML 문서에 반영되지 않음.

**해결 방법**: 
index.adoc에 나머지 11개 섹션과 68개 API에 대한 include 문을 추가하면, 
대부분의 문서화가 즉시 완료됨 (예상 소요시간: 4-6시간).
