# API-ErrorType 매핑 문서

**버전:** v1.0  
**작성일:** 2025-10-08  
**목적:** 각 API 엔드포인트별로 발생 가능한 ErrorType을 명시하여 일관된 에러 처리 보장

---

## 목차
1. [에러 타입 개요](#1-에러-타입-개요)
2. [인증 및 회원 관리 API](#2-인증-및-회원-관리-api)
3. [온보딩 API](#3-온보딩-api)
4. [예산 관리 API](#4-예산-관리-api)
5. [지출 내역 API](#5-지출-내역-api)
6. [가게 관리 API](#6-가게-관리-api)
7. [추천 시스템 API](#7-추천-시스템-api)
8. [즐겨찾기 API](#8-즐겨찾기-api)
9. [프로필 및 설정 API](#9-프로필-및-설정-api)
10. [홈 화면 API](#10-홈-화면-api)
11. [장바구니 API](#11-장바구니-api)
12. [지도 및 위치 API](#12-지도-및-위치-api)
13. [알림 및 설정 API](#13-알림-및-설정-api)

---

## 1. 에러 타입 개요

### 1.1 ErrorType Enum 정의

| ErrorType | HTTP Status | ErrorCode | 설명 | LogLevel |
|-----------|-------------|-----------|------|----------|
| **인증 관련 (401)** |
| INVALID_TOKEN | 401 | E401 | 유효하지 않은 토큰 | WARN |
| EXPIRED_TOKEN | 401 | E401 | 만료된 토큰 | WARN |
| INVALID_CREDENTIALS | 401 | E401 | 이메일/비밀번호 불일치 | WARN |
| INVALID_REFRESH_TOKEN | 401 | E401 | 유효하지 않은 Refresh Token | WARN |
| **권한 관련 (403)** |
| ACCOUNT_LOCKED | 403 | E403 | 계정 잠김 (5회 실패) | WARN |
| ACCESS_DENIED | 403 | E403 | 접근 권한 없음 | WARN |
| **리소스 없음 (404)** |
| RESOURCE_NOT_FOUND | 404 | E404 | 리소스 없음 | WARN |
| EMAIL_NOT_FOUND | 404 | E404 | 이메일 없음 | WARN |
| MEMBER_NOT_FOUND | 404 | E404 | 회원 없음 | WARN |
| STORE_NOT_FOUND | 404 | E404 | 가게 없음 | WARN |
| EXPENDITURE_NOT_FOUND | 404 | E404 | 지출 내역 없음 | WARN |
| **중복/충돌 (409)** |
| DUPLICATE_EMAIL | 409 | E409 | 이메일 중복 | WARN |
| DUPLICATE_RESOURCE | 409 | E409 | 리소스 중복 | WARN |
| DUPLICATE_FAVORITE | 409 | E409 | 즐겨찾기 중복 | WARN |
| DUPLICATE_SOCIAL_ACCOUNT | 409 | E409 | 소셜 계정 중복 | WARN |
| CANNOT_DELETE_PRIMARY_ADDRESS | 409 | E409 | 기본 주소 삭제 불가 | WARN |
| CANNOT_DELETE_LAST_LOGIN_METHOD | 409 | E409 | 유일한 로그인 수단 삭제 불가 | WARN |
| DIFFERENT_STORE_IN_CART | 409 | E409 | 장바구니에 다른 가게 상품 존재 | WARN |
| **유효성 검증 (422)** |
| VALIDATION_ERROR | 422 | E422 | 입력값 검증 실패 | WARN |
| INVALID_EMAIL_FORMAT | 422 | E422 | 이메일 형식 오류 | WARN |
| INVALID_PASSWORD_FORMAT | 422 | E422 | 비밀번호 형식 오류 | WARN |
| PASSWORD_MISMATCH | 422 | E422 | 비밀번호 불일치 | WARN |
| CURRENT_PASSWORD_INCORRECT | 422 | E422 | 현재 비밀번호 불일치 | WARN |
| REQUIRED_POLICY_NOT_AGREED | 422 | E422 | 필수 약관 미동의 | WARN |
| INVALID_ADDRESS | 422 | E422 | 주소 유효성 실패 | WARN |
| INVALID_BUDGET | 422 | E422 | 예산 유효성 실패 | WARN |
| SMS_PARSE_FAILED | 422 | E422 | SMS 파싱 실패 | WARN |
| **잘못된 요청 (400)** |
| BAD_REQUEST | 400 | E400 | 잘못된 요청 | WARN |
| INVALID_PARAMETER | 400 | E400 | 잘못된 파라미터 | WARN |
| **외부 서비스 (503)** |
| EXTERNAL_API_ERROR | 503 | E503 | 외부 API 오류 | ERROR |
| NAVER_MAP_API_ERROR | 503 | E503 | 네이버 지도 API 오류 | ERROR |
| OAUTH_PROVIDER_ERROR | 503 | E503 | 소셜 로그인 오류 | ERROR |
| AI_SERVICE_ERROR | 503 | E503 | AI 서비스 오류 | ERROR |
| **서버 에러 (500)** |
| INTERNAL_SERVER_ERROR | 500 | E500 | 서버 내부 오류 | ERROR |
| DATABASE_ERROR | 500 | E500 | 데이터베이스 오류 | ERROR |
| DEFAULT_ERROR | 500 | E500 | 예상치 못한 오류 | ERROR |

---

## 2. 인증 및 회원 관리 API

### 2.1 이메일 회원가입
**Endpoint:** `POST /auth/signup/email`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 409 | DUPLICATE_EMAIL | 이미 사용 중인 이메일 |
| 422 | INVALID_EMAIL_FORMAT | 이메일 형식 오류 |
| 422 | INVALID_PASSWORD_FORMAT | 비밀번호 형식 오류 (8-20자, 영문/숫자/특수문자) |
| 422 | VALIDATION_ERROR | 기타 입력값 검증 실패 |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 2.2 이메일 로그인
**Endpoint:** `POST /auth/login/email`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_CREDENTIALS | 이메일 또는 비밀번호 불일치 |
| 403 | ACCOUNT_LOCKED | 5회 연속 로그인 실패로 계정 잠김 |
| 404 | EMAIL_NOT_FOUND | 존재하지 않는 이메일 |
| 500 | INTERNAL_SERVER_ERROR | 토큰 생성 실패 등 서버 오류 |

---

### 2.3 소셜 로그인 (카카오/구글)
**Endpoint:** `POST /auth/login/kakao`, `POST /auth/login/google`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 인증 코드 만료 또는 유효하지 않음 |
| 503 | OAUTH_PROVIDER_ERROR | 카카오/구글 인증 서버 통신 오류 |
| 500 | INTERNAL_SERVER_ERROR | 회원 정보 저장 또는 토큰 생성 실패 |

---

### 2.4 토큰 갱신
**Endpoint:** `POST /auth/refresh`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_REFRESH_TOKEN | 유효하지 않거나 만료된 Refresh Token |
| 401 | EXPIRED_TOKEN | 토큰 만료 |
| 500 | INTERNAL_SERVER_ERROR | 새 토큰 생성 실패 |

---

### 2.5 로그아웃
**Endpoint:** `POST /auth/logout`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 Access Token |
| 401 | EXPIRED_TOKEN | 만료된 Access Token |
| 500 | INTERNAL_SERVER_ERROR | 토큰 무효화 실패 |

---

### 2.6 이메일 중복 검증
**Endpoint:** `GET /auth/check-email`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 422 | INVALID_EMAIL_FORMAT | 이메일 형식 오류 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 2.7 비밀번호 변경
**Endpoint:** `PUT /members/me/password`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | CURRENT_PASSWORD_INCORRECT | 현재 비밀번호 불일치 |
| 422 | INVALID_PASSWORD_FORMAT | 새 비밀번호 형식 오류 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 2.8 회원 탈퇴
**Endpoint:** `DELETE /members/me`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | PASSWORD_MISMATCH | 비밀번호 불일치 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 500 | DATABASE_ERROR | Soft Delete 실패 |

---

### 2.9 소셜 계정 연동 관리

#### 2.9.1 연동된 소셜 계정 목록 조회
**Endpoint:** `GET /members/me/social-accounts`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

#### 2.9.2 소셜 계정 추가 연동
**Endpoint:** `POST /members/me/social-accounts`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 Access Token |
| 409 | DUPLICATE_SOCIAL_ACCOUNT | 이미 다른 계정에 연동됨 |
| 503 | OAUTH_PROVIDER_ERROR | 소셜 로그인 인증 실패 |
| 500 | DATABASE_ERROR | DB 저장 실패 |

#### 2.9.3 소셜 계정 연동 해제
**Endpoint:** `DELETE /members/me/social-accounts/{socialAccountId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 소셜 계정 정보 없음 |
| 409 | CANNOT_DELETE_LAST_LOGIN_METHOD | 유일한 로그인 수단 삭제 시도 |
| 500 | DATABASE_ERROR | DB 삭제 실패 |

---

### 2.10 비밀번호 만료 관리

#### 2.10.1 비밀번호 만료 상태 조회
**Endpoint:** `GET /members/me/password/expiry-status`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

#### 2.10.2 비밀번호 만료일 연장
**Endpoint:** `POST /members/me/password/extend-expiry`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 422 | VALIDATION_ERROR | 연장 횟수 초과 (3회 제한) |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

## 3. 온보딩 API

### 3.1 온보딩 - 닉네임 및 소속 설정
**Endpoint:** `POST /onboarding/profile`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 그룹 ID |
| 409 | DUPLICATE_RESOURCE | 닉네임 중복 |
| 422 | VALIDATION_ERROR | 닉네임 형식 오류 (2-50자) |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 3.2 온보딩 - 주소 등록
**Endpoint:** `POST /onboarding/address`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | INVALID_ADDRESS | 네이버 지도 API 주소 검증 실패 |
| 422 | VALIDATION_ERROR | 필수 필드 누락 |
| 503 | NAVER_MAP_API_ERROR | 네이버 지도 API 호출 실패 |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 3.3 온보딩 - 예산 설정
**Endpoint:** `POST /onboarding/budget`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | INVALID_BUDGET | 예산 0 미만 입력 |
| 422 | VALIDATION_ERROR | 식사별 예산 합계 ≠ 일일 예산 |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 3.4 온보딩 - 취향 설정
**Endpoint:** `POST /onboarding/preferences`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | VALIDATION_ERROR | weight 값이 -100, 0, 100이 아님 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 카테고리 ID |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 3.5 온보딩 - 약관 동의
**Endpoint:** `POST /onboarding/policy-agreements`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | REQUIRED_POLICY_NOT_AGREED | 필수 약관 미동의 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 약관 ID |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 3.6 그룹 목록 조회
**Endpoint:** `GET /groups`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 400 | INVALID_PARAMETER | 잘못된 query parameter |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 3.7 카테고리 목록 조회
**Endpoint:** `GET /categories`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 3.8 약관 조회

#### 3.8.1 약관 목록 조회
**Endpoint:** `GET /policies`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 500 | DATABASE_ERROR | DB 조회 실패 |

#### 3.8.2 약관 상세 조회
**Endpoint:** `GET /policies/{policyId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 약관 ID |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

## 4. 예산 관리 API

### 4.1 월별 예산 조회
**Endpoint:** `GET /budgets/monthly`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 year/month 값 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 4.2 일별 예산 조회
**Endpoint:** `GET /budgets/daily`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 날짜 형식 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 4.3 예산 수정
**Endpoint:** `PUT /budgets`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | INVALID_BUDGET | 예산 0 미만 |
| 422 | VALIDATION_ERROR | 식사별 예산 합계 불일치 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 4.4 특정 날짜 예산 수정 및 일괄 적용
**Endpoint:** `PUT /budgets/daily/{date}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 날짜 형식 (YYYY-MM-DD 형식 필요) |
| 400 | BAD_REQUEST | date 파라미터 누락 |
| 422 | INVALID_BUDGET | 예산 0 미만 |
| 422 | VALIDATION_ERROR | 식사별 예산 합계 ≠ dailyBudget |
| 500 | DATABASE_ERROR | DB 업데이트 실패 (일괄 적용 포함) |

**특이사항:**
- `applyForward: true` 시 해당 날짜부터 연말까지 모든 날짜에 일괄 적용
- 이미 개별 설정된 날짜도 덮어씀
- SRD 요구사항 REQ-PROFILE-204e 충족

---

## 5. 지출 내역 API

### 5.1 SMS 파싱
**Endpoint:** `POST /expenditures/parse-sms`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | SMS_PARSE_FAILED | 파싱 실패 (지원하지 않는 형식) |
| 503 | AI_SERVICE_ERROR | Gemini AI 호출 실패 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

---

### 5.2 지출 내역 등록
**Endpoint:** `POST /expenditures`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | VALIDATION_ERROR | items 총액 ≠ amount |
| 422 | INVALID_BUDGET | amount < 0 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 카테고리 ID |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 5.3 지출 내역 조회 (목록)
**Endpoint:** `GET /expenditures`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 날짜 형식 또는 페이징 파라미터 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 5.4 지출 내역 상세 조회
**Endpoint:** `GET /expenditures/{expenditureId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | EXPENDITURE_NOT_FOUND | 존재하지 않는 지출 내역 |
| 403 | ACCESS_DENIED | 다른 사용자의 지출 내역 접근 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 5.5 지출 내역 수정
**Endpoint:** `PUT /expenditures/{expenditureId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | EXPENDITURE_NOT_FOUND | 존재하지 않는 지출 내역 |
| 403 | ACCESS_DENIED | 다른 사용자의 지출 내역 수정 시도 |
| 422 | VALIDATION_ERROR | items 총액 ≠ amount |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 5.6 지출 내역 삭제
**Endpoint:** `DELETE /expenditures/{expenditureId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | EXPENDITURE_NOT_FOUND | 존재하지 않는 지출 내역 |
| 403 | ACCESS_DENIED | 다른 사용자의 지출 내역 삭제 시도 |
| 500 | DATABASE_ERROR | Soft Delete 실패 |

---

### 5.7 일별 지출 통계 조회
**Endpoint:** `GET /expenditures/statistics/daily`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 year/month 값 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

## 6. 가게 관리 API

### 6.1 가게 목록 조회
**Endpoint:** `GET /stores`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 (선택적 인증) |
| 400 | INVALID_PARAMETER | 잘못된 위도/경도 또는 페이징 값 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 6.2 가게 상세 조회
**Endpoint:** `GET /stores/{storeId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 (선택적 인증) |
| 404 | STORE_NOT_FOUND | 존재하지 않는 가게 ID |
| 500 | DATABASE_ERROR | DB 조회 또는 view_count 증가 실패 |

---

### 6.3 가게 검색 (자동완성)
**Endpoint:** `GET /stores/autocomplete`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 400 | INVALID_PARAMETER | keyword 누락 또는 limit 범위 초과 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

## 7. 추천 시스템 API

### 7.1 개인화 추천 (기본)
**Endpoint:** `POST /recommendations`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 위도/경도 또는 radius 값 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 500 | INTERNAL_SERVER_ERROR | 추천 알고리즘 실행 실패 |

---

### 7.2 추천 점수 상세 조회
**Endpoint:** `GET /recommendations/{storeId}/scores`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | STORE_NOT_FOUND | 존재하지 않는 가게 ID |
| 500 | INTERNAL_SERVER_ERROR | 점수 계산 실패 |

---

### 7.3 추천 유형 변경
**Endpoint:** `PUT /members/me/recommendation-type`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | VALIDATION_ERROR | 유효하지 않은 recommendationType enum |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

## 8. 즐겨찾기 API

### 8.1 즐겨찾기 추가
**Endpoint:** `POST /favorites`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | STORE_NOT_FOUND | 존재하지 않는 가게 ID |
| 409 | DUPLICATE_FAVORITE | 이미 즐겨찾기에 추가됨 |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 8.2 즐겨찾기 목록 조회
**Endpoint:** `GET /favorites`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 8.3 즐겨찾기 순서 변경
**Endpoint:** `PUT /favorites/order`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 즐겨찾기 ID |
| 403 | ACCESS_DENIED | 다른 사용자의 즐겨찾기 수정 시도 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 8.4 즐겨찾기 삭제
**Endpoint:** `DELETE /favorites/{favoriteId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 즐겨찾기 ID |
| 403 | ACCESS_DENIED | 다른 사용자의 즐겨찾기 삭제 시도 |
| 500 | DATABASE_ERROR | DB 삭제 실패 |

---

## 9. 프로필 및 설정 API

### 9.1 내 프로필 조회
**Endpoint:** `GET /members/me`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 9.2 프로필 수정
**Endpoint:** `PUT /members/me`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 그룹 ID |
| 409 | DUPLICATE_RESOURCE | 닉네임 중복 |
| 422 | VALIDATION_ERROR | 닉네임 형식 오류 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 9.3 주소 목록 조회
**Endpoint:** `GET /members/me/addresses`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 9.4 주소 추가
**Endpoint:** `POST /members/me/addresses`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | INVALID_ADDRESS | 네이버 지도 API 주소 검증 실패 |
| 503 | NAVER_MAP_API_ERROR | 네이버 지도 API 호출 실패 |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 9.5 주소 수정
**Endpoint:** `PUT /members/me/addresses/{addressHistoryId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 주소 ID |
| 403 | ACCESS_DENIED | 다른 사용자의 주소 수정 시도 |
| 422 | INVALID_ADDRESS | 주소 검증 실패 |
| 503 | NAVER_MAP_API_ERROR | 네이버 지도 API 호출 실패 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 9.6 주소 삭제
**Endpoint:** `DELETE /members/me/addresses/{addressHistoryId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 주소 ID |
| 403 | ACCESS_DENIED | 다른 사용자의 주소 삭제 시도 |
| 409 | CANNOT_DELETE_PRIMARY_ADDRESS | 기본 주소 삭제 불가 (다른 주소 없음) |
| 500 | DATABASE_ERROR | DB 삭제 실패 |

---

### 9.7 기본 주소 설정
**Endpoint:** `PUT /members/me/addresses/{addressHistoryId}/primary`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 주소 ID |
| 403 | ACCESS_DENIED | 다른 사용자의 주소 수정 시도 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 9.8 선호도 조회
**Endpoint:** `GET /members/me/preferences`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 9.9 선호도 수정
**Endpoint:** `PUT /members/me/preferences`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | VALIDATION_ERROR | weight 값이 -100, 0, 100이 아님 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 카테고리 ID |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

## 10. 홈 화면 API

### 10.1 홈 대시보드 조회
**Endpoint:** `GET /home/dashboard`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 위도/경도 값 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 500 | INTERNAL_SERVER_ERROR | 대시보드 데이터 조합 실패 |

---

### 10.2 온보딩 상태 조회
**Endpoint:** `GET /members/me/onboarding-status`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 10.3 월별 예산 확인 처리
**Endpoint:** `POST /members/me/monthly-budget-confirmed`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 year/month 값 |
| 422 | VALIDATION_ERROR | action 값이 KEEP/CHANGE가 아님 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

## 11. 장바구니 API

### 11.1 장바구니 조회
**Endpoint:** `GET /cart`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 11.2 장바구니에 상품 추가
**Endpoint:** `POST /cart/items`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | STORE_NOT_FOUND | 존재하지 않는 가게 ID |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 음식 ID |
| 409 | DIFFERENT_STORE_IN_CART | 장바구니에 다른 가게 상품 존재 |
| 422 | VALIDATION_ERROR | 수량이 1 미만 |
| 500 | DATABASE_ERROR | DB 저장 실패 |

---

### 11.3 장바구니 상품 수량 변경
**Endpoint:** `PUT /cart/items/{cartItemId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 장바구니 아이템 ID |
| 403 | ACCESS_DENIED | 다른 사용자의 장바구니 수정 시도 |
| 422 | VALIDATION_ERROR | 수량이 1 미만 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 11.4 장바구니 상품 삭제
**Endpoint:** `DELETE /cart/items/{cartItemId}`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 장바구니 아이템 ID |
| 403 | ACCESS_DENIED | 다른 사용자의 장바구니 삭제 시도 |
| 500 | DATABASE_ERROR | DB 삭제 실패 |

---

### 11.5 장바구니 전체 비우기
**Endpoint:** `DELETE /cart`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 500 | DATABASE_ERROR | DB 삭제 실패 |

---

### 11.6 장바구니 → 지출 등록
**Endpoint:** `POST /cart/checkout`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | BAD_REQUEST | 빈 장바구니로 체크아웃 시도 |
| 422 | VALIDATION_ERROR | 잘못된 mealType 또는 날짜 형식 |
| 500 | DATABASE_ERROR | 지출 내역 생성 또는 장바구니 삭제 실패 |

---

## 12. 지도 및 위치 API

### 12.1 주소 검색 (Geocoding)
**Endpoint:** `GET /maps/search-address`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 400 | INVALID_PARAMETER | keyword 누락 또는 limit 범위 초과 |
| 503 | NAVER_MAP_API_ERROR | 네이버 지도 API 호출 실패 |
| 500 | INTERNAL_SERVER_ERROR | 응답 파싱 실패 |

---

### 12.2 좌표 → 주소 변환 (Reverse Geocoding)
**Endpoint:** `GET /maps/reverse-geocode`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 400 | INVALID_PARAMETER | 잘못된 위도/경도 값 |
| 503 | NAVER_MAP_API_ERROR | 네이버 지도 API 호출 실패 |
| 500 | INTERNAL_SERVER_ERROR | 응답 파싱 실패 |

---

### 12.3 현재 위치 기준 변경
**Endpoint:** `PUT /members/me/current-location`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 400 | INVALID_PARAMETER | 잘못된 type 또는 위도/경도 값 |
| 404 | RESOURCE_NOT_FOUND | 존재하지 않는 주소 ID (type=ADDRESS 시) |
| 403 | ACCESS_DENIED | 다른 사용자의 주소 접근 시도 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

## 13. 알림 및 설정 API

### 13.1 알림 설정 조회
**Endpoint:** `GET /members/me/notification-settings`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 404 | MEMBER_NOT_FOUND | 회원 정보 없음 |
| 500 | DATABASE_ERROR | DB 조회 실패 |

---

### 13.2 알림 설정 변경
**Endpoint:** `PUT /members/me/notification-settings`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | VALIDATION_ERROR | 유효하지 않은 boolean 값 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

### 13.3 앱 설정 조회
**Endpoint:** `GET /settings/app`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 500 | INTERNAL_SERVER_ERROR | 설정 로드 실패 |

---

### 13.4 사용자 추적 설정 변경
**Endpoint:** `PUT /settings/app/tracking`

| HTTP Status | ErrorType | 발생 조건 |
|-------------|-----------|----------|
| 401 | INVALID_TOKEN | 유효하지 않은 토큰 |
| 422 | VALIDATION_ERROR | 유효하지 않은 boolean 값 |
| 500 | DATABASE_ERROR | DB 업데이트 실패 |

---

## 부록

### A. ErrorType 사용 가이드

#### A.1 Controller에서 ErrorType 사용 예시

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @PostMapping("/login/email")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        Member member = memberService.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException(ErrorType.EMAIL_NOT_FOUND));
        
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorType.INVALID_CREDENTIALS);
        }
        
        if (member.isLocked()) {
            throw new BusinessException(ErrorType.ACCOUNT_LOCKED);
        }
        
        // 정상 처리
        return ApiResponse.success(loginResponse);
    }
}
```

#### A.2 Controller Advice에서 ErrorType 변환

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorType errorType = e.getErrorType();
        ErrorMessage errorMessage = ErrorMessage.of(
            errorType.getCode().name(),
            errorType.getMessage(),
            e.getData()
        );
        
        return ResponseEntity
            .status(errorType.getStatus())
            .body(ApiResponse.error(errorMessage));
    }
}
```

#### A.3 Custom BusinessException

```java
public class BusinessException extends RuntimeException {
    private final ErrorType errorType;
    private final Map<String, Object> data;
    
    public BusinessException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.data = null;
    }
    
    public BusinessException(ErrorType errorType, Map<String, Object> data) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.data = data;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
}
```

---

### B. 에러 응답 예시

#### B.1 인증 실패 (401)
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E401",
    "message": "이메일 또는 비밀번호가 일치하지 않습니다."
  }
}
```

#### B.2 유효성 검증 실패 (422) - 상세 데이터 포함
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E422",
    "message": "입력값 검증에 실패했습니다.",
    "data": {
      "field": "password",
      "reason": "비밀번호는 8-20자, 영문/숫자/특수문자 조합이어야 합니다."
    }
  }
}
```

#### B.3 리소스 중복 (409)
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E409",
    "message": "이미 사용 중인 이메일입니다.",
    "data": {
      "field": "email",
      "value": "hong@example.com"
    }
  }
}
```

#### B.4 외부 API 오류 (503)
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E503",
    "message": "네이버 지도 API 호출에 실패했습니다."
  }
}
```

---

### C. 에러 처리 체크리스트

- [ ] 모든 Controller 메서드에 적절한 ErrorType throw 구현
- [ ] Controller Advice에서 ErrorType을 ApiResponse로 변환
- [ ] 각 ErrorType의 LogLevel에 따른 로깅 전략 수립
- [ ] 외부 API 호출 시 503 에러 처리 (Circuit Breaker 고려)
- [ ] 유효성 검증 실패 시 상세 에러 데이터(`data` 필드) 제공
- [ ] 인증/권한 관련 에러에 대한 보안 로깅 강화
- [ ] 데이터베이스 오류 시 민감한 정보 노출 방지

---

**문서 종료**
