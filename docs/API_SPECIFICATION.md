# API Specification - SmartMealTable (알뜰식탁)

**버전:** v1.0  
**작성일:** 2025-10-08  
**Base URL:** `/api/v1`

---

## 목차
1. [개요](#1-개요)
2. [공통 사항](#2-공통-사항)
3. [인증 및 회원 관리 API](#3-인증-및-회원-관리-api)
4. [온보딩 API](#4-온보딩-api)
5. [예산 관리 API](#5-예산-관리-api)
6. [지출 내역 API](#6-지출-내역-api)
7. [가게 관리 API](#7-가게-관리-api)
8. [음식 API](#8-음식-api)
9. [추천 시스템 API](#9-추천-시스템-api)
10. [즐겨찾기 API](#10-즐겨찾기-api)
11. [프로필 및 설정 API](#11-프로필-및-설정-api)
12. [홈 화면 API](#12-홈-화면-api)
13. [장바구니 API](#13-장바구니-api)
14. [지도 및 위치 API](#14-지도-및-위치-api)
15. [알림 및 설정 API](#15-알림-및-설정-api)

---

## 1. 개요

### 1.1 목적
본 문서는 알뜰식탁 서비스의 RESTful API 엔드포인트와 데이터 구조를 명세합니다.

### 1.2 인증 방식
- **JWT (JSON Web Token)** 기반 인증
- Access Token + Refresh Token 조합
- Access Token 유효기간: 2시간
- Refresh Token 유효기간: 14일

---

## 2. 공통 사항

### 2.1 공통 헤더
```http
Content-Type: application/json
Authorization: Bearer {access_token}
Accept-Language: ko-KR
```

### 2.2 공통 응답 구조

#### 성공 응답 (데이터 포함)
```json
{
  "result": "SUCCESS",
  "data": { ... },
  "error": null
}
```

#### 성공 응답 (데이터 없음)
```json
{
  "result": "SUCCESS",
  "data": null,
  "error": null
}
```

### 2.3 에러 응답 구조
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "사람이 읽을 수 있는 에러 메시지",
    "data": {
      "field": "email",
      "reason": "이미 사용 중인 이메일입니다."
    }
  }
}
```

**ErrorMessage 구조:**
- `code`: ErrorCode enum 값 (예: "E400", "E401", "E404", "E409", "E422", "E500")
- `message`: 사용자에게 표시할 에러 메시지
- `data`: 에러 상세 정보 (optional, 필드별 검증 오류 등)

**ResultType enum:**
- `SUCCESS`: 성공
- `ERROR`: 실패

### 2.4 HTTP 상태 코드

| HTTP Status | ErrorCode | 설명 |
|-------------|-----------|------|
| 200 | - | 성공 (OK) |
| 201 | - | 생성 성공 (Created) |
| 204 | - | 성공, 반환 데이터 없음 (No Content) |
| 400 | E400 | 잘못된 요청 (Bad Request) |
| 401 | E401 | 인증 실패 (Unauthorized) |
| 403 | E403 | 권한 없음 (Forbidden) |
| 404 | E404 | 리소스 없음 (Not Found) |
| 409 | E409 | 충돌 (Conflict) - 중복 데이터 |
| 422 | E422 | 유효성 검증 실패 (Unprocessable Entity) |
| 500 | E500 | 내부 서버 오류 (Internal Server Error) |
| 503 | E503 | 서비스 이용 불가 (Service Unavailable) - 외부 API 오류 |

#### 404 Not Found 에러 처리

리소스를 찾을 수 없을 때 발생하는 에러입니다. `ResourceNotFoundException`을 사용하여 처리합니다.

**사용 예시:**
```java
// Service Layer
public ExpenditureDetail getExpenditureDetail(Long memberId, Long expenditureId) {
    Expenditure expenditure = expenditureRepository.findById(expenditureId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorType.EXPENDITURE_NOT_FOUND));
    
    // 권한 확인
    if (!expenditure.getMemberId().equals(memberId)) {
        throw new AuthorizationException(ErrorType.ACCESS_DENIED);
    }
    
    return convertToDetail(expenditure);
}
```

**에러 응답 예시:**
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E404",
    "message": "존재하지 않는 지출 내역입니다.",
    "data": null
  }
}
```

**주요 404 에러 타입:**
- `MEMBER_NOT_FOUND`: 존재하지 않는 회원
- `EMAIL_NOT_FOUND`: 존재하지 않는 이메일
- `GROUP_NOT_FOUND`: 존재하지 않는 그룹
- `POLICY_NOT_FOUND`: 존재하지 않는 약관
- `CATEGORY_NOT_FOUND`: 존재하지 않는 카테고리
- `STORE_NOT_FOUND`: 존재하지 않는 가게
- `FOOD_NOT_FOUND`: 존재하지 않는 음식
- `EXPENDITURE_NOT_FOUND`: 존재하지 않는 지출 내역
- `MONTHLY_BUDGET_NOT_FOUND`: 해당 월의 예산 정보를 찾을 수 없음
- `DAILY_BUDGET_NOT_FOUND`: 해당 날짜의 예산 정보를 찾을 수 없음
- `ADDRESS_NOT_FOUND`: 존재하지 않는 주소
- `FAVORITE_NOT_FOUND`: 존재하지 않는 즐겨찾기
- `CART_NOT_FOUND`: 존재하지 않는 장바구니
- `CART_ITEM_NOT_FOUND`: 존재하지 않는 장바구니 아이템

### 2.5 페이징
```json
{
  "content": [...],
  "pageable": {
    "page": 0,
    "size": 20,
    "sort": "createdAt,desc"
  },
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

## 3. 인증 및 회원 관리 API

### 3.1 이메일 회원가입

**Endpoint:** `POST /api/v1/auth/signup/email`

**Request:**
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "password": "SecureP@ss123!"
}
```

**Validation Rules:**
- `name`: 2-50자, 필수
- `email`: 이메일 형식, 필수, 중복 불가
- `password`: 8-20자, 영문/숫자/특수문자 조합, 필수

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "memberId": 123,
    "email": "hong@example.com",
    "name": "홍길동",
    "createdAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Error Cases:**

*409 Conflict - 이메일 중복:*
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

*422 Unprocessable Entity - 유효성 검증 실패:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E422",
    "message": "입력값 검증에 실패했습니다.",
    "data": {
      "field": "password",
      "reason": "비밀번호는 8-20자의 영문, 숫자, 특수문자 조합이어야 합니다."
    }
  }
}
```

---

### 3.2 이메일 로그인

**Endpoint:** `POST /api/v1/auth/login/email`

**Request:**
```json
{
  "email": "hong@example.com",
  "password": "SecureP@ss123!"
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "member": {
      "memberId": 123,
      "nickname": "길동이",
      "email": "hong@example.com",
      "recommendationType": "BALANCED",
      "isOnboardingComplete": true
    }
  },
  "error": null
}
```

**Error Cases:**

*401 Unauthorized - 인증 실패:*
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

*403 Forbidden - 계정 잠김:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E403",
    "message": "5회 연속 로그인 실패로 계정이 잠겼습니다. 30분 후 다시 시도해주세요."
  }
}
```

*404 Not Found - 존재하지 않는 이메일:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E404",
    "message": "존재하지 않는 이메일입니다."
  }
}
```

---

### 3.3 소셜 로그인 (카카오)

**Endpoint:** `POST /api/v1/auth/login/kakao`

**Request:**
```json
{
  "authorizationCode": "asdf1234qwer5678zxcv"
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "member": {
      "memberId": 124,
      "nickname": "카카오사용자",
      "email": "kakao_user@kakao.com",
      "recommendationType": null,
      "isOnboardingComplete": false,
      "isNewMember": true
    }
  },
  "error": null
}
```

**Note:** 
- 신규 가입 시 `isNewMember: true`, `isOnboardingComplete: false`
- 기존 회원 시 `isNewMember: false`, `isOnboardingComplete: true`

**Error Cases:**

*503 Service Unavailable - 외부 API 오류:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E503",
    "message": "카카오 인증 서버와 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
  }
}
```

*401 Unauthorized - 인증 코드 만료:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E401",
    "message": "인증 코드가 만료되었습니다. 다시 로그인해주세요."
  }
}
```

---

### 3.4 소셜 로그인 (구글)

**Endpoint:** `POST /api/v1/auth/login/google`

**Request/Response:** 카카오 로그인과 동일한 구조

---

### 3.5 토큰 갱신

**Endpoint:** `POST /api/v1/auth/refresh`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  },
  "error": null
}
```

**Error Cases:**

*401 Unauthorized - 만료되거나 유효하지 않은 토큰:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E401",
    "message": "유효하지 않거나 만료된 Refresh Token입니다."
  }
}
```

---

### 3.6 로그아웃

**Endpoint:** `POST /api/v1/auth/logout`

**Headers:**
```
Authorization: Bearer {access_token}
```

**Response (204):** No Content

---

### 3.7 이메일 중복 검증

**Endpoint:** `GET /api/v1/auth/check-email?email={email}`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "available": true,
    "message": "사용 가능한 이메일입니다."
  },
  "error": null
}
```

**Response (200) - 중복:**
```json
{
  "result": "SUCCESS",
  "data": {
    "available": false,
    "message": "이미 사용 중인 이메일입니다."
  },
  "error": null
}
```

---

### 3.8 비밀번호 변경

**Endpoint:** `PUT /api/v1/members/me/password`

**Request:**
```json
{
  "currentPassword": "OldP@ss123!",
  "newPassword": "NewP@ss456!"
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "message": "비밀번호가 성공적으로 변경되었습니다.",
    "passwordChangedAt": "2025-10-08T12:34:56.789Z",
    "passwordExpiresAt": "2026-01-06T12:34:56.789Z"
  },
  "error": null
}
```

**Error Cases:**

*401 Unauthorized - 현재 비밀번호 불일치:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E401",
    "message": "현재 비밀번호가 일치하지 않습니다."
  }
}
```

*422 Unprocessable Entity - 새 비밀번호 형식 오류:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E422",
    "message": "비밀번호는 8-20자의 영문, 숫자, 특수문자 조합이어야 합니다."
  }
}
```

---

### 3.9 회원 탈퇴

**Endpoint:** `DELETE /api/v1/members/me`

**Request:**
```json
{
  "password": "SecureP@ss123!",
  "reason": "서비스 불만족"
}
```

**Response (204):** No Content

**Error Cases:**

*401 Unauthorized - 비밀번호 불일치:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E401",
    "message": "비밀번호가 일치하지 않습니다."
  }
}
```

**Note:** Soft Delete 처리, 1년 후 배치로 완전 삭제

---

### 3.10 소셜 계정 연동 관리

#### 3.10.1 연동된 소셜 계정 목록 조회

**Endpoint:** `GET /api/v1/members/me/social-accounts`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "connectedAccounts": [
      {
        "socialAccountId": 201,
        "provider": "KAKAO",
        "providerEmail": "user@kakao.com",
        "connectedAt": "2025-09-01T10:00:00.000Z"
      },
      {
        "socialAccountId": 202,
        "provider": "GOOGLE",
        "providerEmail": "user@gmail.com",
        "connectedAt": "2025-09-15T14:30:00.000Z"
      }
    ],
    "hasPassword": true
  },
  "error": null
}
```

---

#### 3.10.2 소셜 계정 추가 연동

**Endpoint:** `POST /api/v1/members/me/social-accounts`

**Request:**
```json
{
  "provider": "KAKAO",
  "authorizationCode": "asdf1234qwer5678zxcv"
}
```

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "socialAccountId": 203,
    "provider": "KAKAO",
    "providerEmail": "user@kakao.com",
    "connectedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Error Cases:**

*409 Conflict - 이미 연동된 소셜 계정:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E409",
    "message": "이미 다른 계정에 연동된 소셜 계정입니다."
  }
}
```

---

#### 3.10.3 소셜 계정 연동 해제

**Endpoint:** `DELETE /api/v1/members/me/social-accounts/{socialAccountId}`

**Response (204):** No Content

**Error Cases:**

*409 Conflict - 유일한 로그인 수단:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E409",
    "message": "유일한 로그인 수단입니다. 비밀번호를 설정한 후 연동을 해제할 수 있습니다."
  }
}
```

**Note:** 
- 소셜 계정이 유일한 로그인 수단일 경우 연동 해제 전 비밀번호 설정 필요
- 응답에서 `hasPassword: false`이고 연동된 계정이 1개뿐이면 해제 불가

---

### 3.11 비밀번호 만료 관리

#### 3.11.1 비밀번호 만료 상태 조회

**Endpoint:** `GET /api/v1/members/me/password/expiry-status`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "passwordChangedAt": "2025-07-10T10:00:00.000Z",
    "passwordExpiresAt": "2025-10-08T10:00:00.000Z",
    "daysRemaining": 0,
    "isExpired": true,
    "isExpiringSoon": true
  },
  "error": null
}
```

**Note:**
- `isExpiringSoon`: 만료 7일 전부터 true
- `daysRemaining < 0`: 이미 만료됨

---

#### 3.11.2 비밀번호 만료일 연장

**Endpoint:** `POST /api/v1/members/me/password/extend-expiry`

**Request:** (Body 없음)

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "previousExpiresAt": "2025-10-08T10:00:00.000Z",
    "newExpiresAt": "2026-01-06T10:00:00.000Z",
    "extendedDays": 90,
    "message": "비밀번호 만료일이 90일 연장되었습니다."
  },
  "error": null
}
```

**Note:** 90일 연장, 최대 3회까지 연장 가능

---

## 4. 온보딩 API

### 4.1 온보딩 - 닉네임 및 소속 설정

**Endpoint:** `POST /api/v1/onboarding/profile`

**Request:**
```json
{
  "nickname": "길동이",
  "groupId": 123
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "memberId": 123,
    "nickname": "길동이",
    "group": {
      "groupId": 123,
      "name": "서울대학교",
      "type": "UNIVERSITY",
      "address": "서울특별시 관악구 관악로 1"
    }
  },
  "error": null
}
```

**Error Cases:**

*409 Conflict - 닉네임 중복:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E409",
    "message": "이미 사용 중인 닉네임입니다."
  }
}
```

*404 Not Found - 존재하지 않는 그룹:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E404",
    "message": "존재하지 않는 소속 그룹입니다."
  }
}
```

---

### 4.2 온보딩 - 주소 등록

**Endpoint:** `POST /api/v1/onboarding/address`

**Request:**
```json
{
  "addressAlias": "우리집",
  "addressType": "HOME",
  "streetNameAddress": "서울특별시 강남구 테헤란로 123",
  "lotNumberAddress": "서울특별시 강남구 역삼동 456-78",
  "detailedAddress": "101동 1234호",
  "latitude": 37.497942,
  "longitude": 127.027621
}
```

**Validation:**
- 네이버 지도 API로 주소 검증 필수

**Note:**
- 첫 번째로 등록된 주소는 자동으로 기본 주소(primary address)로 설정됩니다.

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "addressHistoryId": 456,
    "addressAlias": "우리집",
    "streetNameAddress": "서울특별시 강남구 테헤란로 123",
    "isPrimary": true,
    "createdAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

### 4.3 온보딩 - 예산 설정

**Endpoint:** `POST /api/v1/onboarding/budget`

**Request:**
```json
{
  "monthlyBudget": 300000,
  "dailyBudget": 10000,
  "mealBudgets": {
    "BREAKFAST": 3000,
    "LUNCH": 4000,
    "DINNER": 3000,
    "OTHER": 3000
  }
}
```

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "monthlyBudget": 300000,
    "dailyBudget": 10000,
    "mealBudgets": [
      {
        "mealType": "BREAKFAST",
        "budget": 3000
      },
      {
        "mealType": "LUNCH",
        "budget": 4000
      },
      {
        "mealType": "DINNER",
        "budget": 3000
      },
      {
        "mealType": "OTHER",
        "budget": 3000
      }
    ]
  },
  "error": null
}
```

---

### 4.4 온보딩 - 취향 설정

**Endpoint:** `POST /api/v1/onboarding/preferences`

**Request:**
```json
{
  "recommendationType": "BALANCED",
  "preferences": [
    {
      "categoryId": 1,
      "weight": 100
    },
    {
      "categoryId": 2,
      "weight": 0
    },
    {
      "categoryId": 3,
      "weight": -100
    }
  ]
}
```

**Enum Values:**
- `recommendationType`: `SAVER`, `ADVENTURER`, `BALANCED`
- `weight`: `100` (좋아요), `0` (보통), `-100` (싫어요)

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "recommendationType": "BALANCED",
    "preferences": [
      {
        "categoryId": 1,
        "categoryName": "한식",
        "weight": 100
      },
      {
        "categoryId": 2,
        "categoryName": "중식",
        "weight": 0
      },
      {
        "categoryId": 3,
        "categoryName": "일식",
        "weight": -100
      }
    ]
  },
  "error": null
}
```

---

### 4.5 온보딩 - 약관 동의

**Endpoint:** `POST /api/v1/onboarding/policy-agreements`

**Request:**
```json
{
  "agreements": [
    {
      "policyId": 1,
      "isAgreed": true
    },
    {
      "policyId": 2,
      "isAgreed": true
    },
    {
      "policyId": 3,
      "isAgreed": false
    }
  ]
}
```

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "isOnboardingComplete": true,
    "agreedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Error Cases:**
- `422`: 필수 약관 미동의

---

### 4.6 그룹 목록 조회

**Endpoint:** `GET /api/v1/groups?type={type}&name={name}&page=0&size=20`

**Query Parameters:**
- `type` (optional): `UNIVERSITY`, `COMPANY`, `OTHER`
- `name` (optional): 그룹명 검색어
- `page`, `size`: 페이징

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "content": [
      {
        "groupId": 123,
        "name": "서울대학교",
        "type": "UNIVERSITY",
        "address": "서울특별시 관악구 관악로 1"
      }
    ],
    "pageable": { ... },
    "totalElements": 50
  },
  "error": null
}
```

---

### 4.7 카테고리 목록 조회

**Endpoint:** `GET /api/v1/categories`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": [
    {
      "categoryId": 1,
      "name": "한식",
      "description": "한국 전통 음식"
    },
    {
      "categoryId": 2,
      "name": "중식",
      "description": "중국 음식"
    }
  ],
  "error": null
}
```

---

### 4.8 약관 조회

#### 4.8.1 약관 목록 조회

**Endpoint:** `GET /api/v1/policies`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": [
    {
      "policyId": 1,
      "title": "서비스 이용 약관",
      "version": "1.0",
      "isRequired": true,
      "summary": "알뜰식탁 서비스 이용에 관한 약관",
      "contentUrl": "https://smartmealtable.com/policies/terms",
      "updatedAt": "2025-01-01T00:00:00.000Z"
    },
    {
      "policyId": 2,
      "title": "개인정보 수집 및 이용 동의",
      "version": "1.0",
      "isRequired": true,
      "summary": "개인정보 수집, 이용 및 제공에 관한 동의",
      "contentUrl": "https://smartmealtable.com/policies/privacy",
      "updatedAt": "2025-01-01T00:00:00.000Z"
    },
    {
      "policyId": 3,
      "title": "푸시 알림 수신 동의",
      "version": "1.0",
      "isRequired": false,
      "summary": "마케팅 및 서비스 알림 수신 동의",
      "contentUrl": "https://smartmealtable.com/policies/notification",
      "updatedAt": "2025-01-01T00:00:00.000Z"
    }
  ],
  "error": null
}
```

---

#### 4.11.2 약관 상세 조회

**Endpoint:** `GET /api/v1/policies/{policyId}`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "policyId": 1,
    "title": "서비스 이용 약관",
    "version": "1.0",
    "isRequired": true,
    "content": "제1장 총칙\n제1조 (목적)...",
    "updatedAt": "2025-01-01T00:00:00.000Z"
  },
  "error": null
}
```

**Error Cases:**
- `404`: 약관을 찾을 수 없음

---

### 4.9 온보딩용 음식 목록 조회 (REQ-ONBOARD-405)

**Endpoint:** `GET /api/v1/onboarding/foods?categoryId={categoryId}&page=0&size=20`

**설명:** 온보딩 과정에서 사용자가 선호하는 개별 음식을 이미지 그리드로 선택할 수 있도록 음식 목록을 제공합니다.

**Query Parameters:**
- `categoryId` (optional): 특정 카테고리의 음식만 조회 (선택한 카테고리 기반 필터링)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20, 최대: 100)

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "content": [
      {
        "foodId": 1,
        "foodName": "김치찌개",
        "categoryId": 1,
        "categoryName": "한식",
        "imageUrl": "https://cdn.smartmealtable.com/foods/kimchi-jjigae.jpg",
        "description": "얼큰한 김치찌개",
        "averagePrice": 8000
      },
      {
        "foodId": 2,
        "foodName": "된장찌개",
        "categoryId": 1,
        "categoryName": "한식",
        "imageUrl": "https://cdn.smartmealtable.com/foods/doenjang-jjigae.jpg",
        "description": "구수한 된장찌개",
        "averagePrice": 7500
      },
      {
        "foodId": 15,
        "foodName": "짜장면",
        "categoryId": 2,
        "categoryName": "중식",
        "imageUrl": "https://cdn.smartmealtable.com/foods/jjajangmyeon.jpg",
        "description": "고소한 짜장면",
        "averagePrice": 6000
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 150,
    "totalPages": 8,
    "last": false,
    "size": 20,
    "number": 0,
    "first": true,
    "numberOfElements": 20,
    "empty": false
  },
  "error": null
}
```

**Response Fields:**
- `foodId`: 음식 고유 식별자
- `foodName`: 음식 이름
- `categoryId`: 카테고리 ID
- `categoryName`: 카테고리 이름
- `imageUrl`: 음식 이미지 URL (그리드 뷰에서 표시)
- `description`: 음식 설명
- `averagePrice`: 평균 가격 (참고용)

**Error Cases:**
- `400`: 잘못된 쿼리 파라미터

---

### 4.10 개별 음식 선호도 저장 (REQ-ONBOARD-405)

**Endpoint:** `POST /api/v1/onboarding/food-preferences`

**설명:** 온보딩 과정에서 사용자가 이미지 그리드에서 선택한 개별 음식 선호도를 저장합니다.

**Request:**
```json
{
  "preferredFoodIds": [1, 2, 5, 7, 12, 15, 23, 28, 35, 42]
}
```

**Request Fields:**
- `preferredFoodIds`: 사용자가 선호하는 음식의 ID 배열 (최소 0개, 최대 50개 권장)

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "savedCount": 10,
    "preferredFoods": [
      {
        "foodId": 1,
        "foodName": "김치찌개",
        "categoryName": "한식",
        "imageUrl": "https://cdn.smartmealtable.com/foods/kimchi-jjigae.jpg"
      },
      {
        "foodId": 2,
        "foodName": "된장찌개",
        "categoryName": "한식",
        "imageUrl": "https://cdn.smartmealtable.com/foods/doenjang-jjigae.jpg"
      },
      {
        "foodId": 5,
        "foodName": "불고기",
        "categoryName": "한식",
        "imageUrl": "https://cdn.smartmealtable.com/foods/bulgogi.jpg"
      }
    ],
    "message": "선호 음식이 성공적으로 저장되었습니다."
  },
  "error": null
}
```

**Response Fields:**
- `savedCount`: 저장된 음식 선호도 개수
- `preferredFoods`: 저장된 선호 음식 목록 (최대 10개 반환, 나머지는 생략)
- `message`: 성공 메시지

**Error Cases:**
- `400`: 잘못된 요청 (예: 빈 배열, 너무 많은 항목)
- `404`: 존재하지 않는 음식 ID 포함
- `422`: 유효성 검증 실패

**비즈니스 규칙:**
- 이미 저장된 선호도가 있을 경우, 기존 데이터를 삭제하고 새로운 데이터로 대체합니다 (Upsert)
- 빈 배열(`[]`)을 보내면 모든 음식 선호도를 삭제합니다
- 중복된 `foodId`는 자동으로 제거됩니다
- 카테고리 선호도(`POST /onboarding/preferences`)와 독립적으로 저장됩니다

---

### 4.11 약관 조회 (기존 4.8에서 번호 변경)

#### 4.11.1 약관 목록 조회
    }
  ],
  "error": null
}
```

---

#### 4.8.2 약관 상세 조회

**Endpoint:** `GET /api/v1/policies/{policyId}`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "policyId": 1,
    "title": "서비스 이용 약관",
    "version": "1.0",
    "isRequired": true,
    "content": "제1조 (목적)\n본 약관은 알뜰식탁(이하 '회사')이 제공하는...",
    "contentHtml": "<h1>제1조 (목적)</h1><p>본 약관은...</p>",
    "contentUrl": "https://smartmealtable.com/policies/terms",
    "updatedAt": "2025-01-01T00:00:00.000Z"
  },
  "error": null
}
```

---

## 5. 예산 관리 API

### 5.1 월별 예산 조회

**Endpoint:** `GET /api/v1/budgets/monthly?year=2025&month=10`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "year": 2025,
    "month": 10,
    "totalBudget": 300000,
    "totalSpent": 125000,
    "remainingBudget": 175000,
    "utilizationRate": 41.67,
    "daysRemaining": 23
  },
  "error": null
}
```

---

### 5.2 일별 예산 조회

**Endpoint:** `GET /api/v1/budgets/daily?date=2025-10-08`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "date": "2025-10-08",
    "totalBudget": 10000,
    "totalSpent": 8500,
    "remainingBudget": 1500,
    "mealBudgets": [
      {
        "mealType": "BREAKFAST",
        "budget": 3000,
        "spent": 2500,
        "remaining": 500
      },
      {
        "mealType": "LUNCH",
        "budget": 4000,
        "spent": 4000,
        "remaining": 0
      },
      {
        "mealType": "DINNER",
        "budget": 3000,
        "spent": 2000,
        "remaining": 1000
      },
      {
        "mealType": "OTHER",
        "budget": 3000,
        "spent": 2000,
        "remaining": 1000
      }
    ]
  },
  "error": null
}
```

---

### 5.3 예산 수정

**Endpoint:** `PUT /api/v1/budgets`

**Request:**
```json
{
  "monthlyBudget": 350000,
  "dailyBudget": 12000,
  "mealBudgets": {
    "BREAKFAST": 3500,
    "LUNCH": 5000,
    "DINNER": 3500,
    "OTHER": 2000
  }
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "monthlyBudget": 350000,
    "dailyBudget": 12000,
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Error Cases:**

*401 Unauthorized - 유효하지 않은 토큰:*
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

*422 Unprocessable Entity - 예산 유효성 실패:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E422",
    "message": "예산은 0 이상이어야 합니다."
  }
}
```

---

### 5.4 특정 날짜 예산 수정 및 일괄 적용

**Endpoint:** `PUT /api/v1/budgets/daily/{date}`

**Request:**
```json
{
  "dailyBudget": 12000,
  "mealBudgets": {
    "BREAKFAST": 3500,
    "LUNCH": 5000,
    "DINNER": 3500,
    "OTHER": 2000
  },
  "applyForward": true
}
```

**Parameters:**
- `date`: 예산을 설정할 날짜 (YYYY-MM-DD 형식)
- `applyForward`: true일 경우 해당 날짜 이후 모든 날짜에 일괄 적용 (기본값: false)

**Validation Rules:**
- `date`: YYYY-MM-DD 형식, 필수
- `dailyBudget`: 0 이상, 필수
- `mealBudgets`: 각 식사 예산의 합계가 dailyBudget과 일치해야 함
- `applyForward`: boolean, 선택 (기본값: false)

**Response (200):**
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
      },
      {
        "mealType": "OTHER",
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

**Response Fields:**
- `targetDate`: 예산을 설정한 기준 날짜
- `affectedDatesCount`: 영향받은 날짜 수 (applyForward=true일 경우 해당 날짜부터 연말까지)

**Error Cases:**

*401 Unauthorized - 유효하지 않은 토큰:*
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

*400 Bad Request - 잘못된 날짜 형식:*
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

*422 Unprocessable Entity - 예산 유효성 실패:*
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

**Note:** 
- `applyForward: true` 선택 시, 해당 날짜부터 미래의 모든 날짜(연말까지)에 대해 동일한 예산이 일괄 적용됩니다.
- 이미 개별 설정된 날짜가 있더라도 덮어씌워집니다.
- SRD 요구사항 REQ-PROFILE-204e를 충족합니다.

---

## 6. 지출 내역 API

### 6.1 SMS 파싱

**Endpoint:** `POST /api/v1/expenditures/parse-sms`

**Request:**
```json
{
  "smsContent": "[국민카드] 10/08 12:30 맘스터치강남점 13,500원 일시불 승인"
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "storeName": "맘스터치강남점",
    "amount": 13500,
    "date": "2025-10-08",
    "time": "12:30:00",
    "isParsed": true
  },
  "error": null
}
```

**Error Cases:**
- `422`: 파싱 실패 (지원하지 않는 형식)

---

### 6.2 지출 내역 등록

**Endpoint:** `POST /api/v1/expenditures`

**Request:**
```json
{
  "storeName": "맘스터치강남점",
  "amount": 13500,
  "expendedDate": "2025-10-08",
  "expendedTime": "12:30:00",
  "categoryId": 5,
  "mealType": "LUNCH",
  "memo": "동료와 점심",
  "items": [
    {
      "foodName": "싸이버거 세트",
      "quantity": 1,
      "price": 6500
    },
    {
      "foodName": "치킨버거 세트",
      "quantity": 1,
      "price": 7000
    }
  ]
}
```

**Validation:**
- `amount` >= 0
- `items` 총액 = `amount`

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "expenditureId": 789,
    "storeId": null,
    "storeName": "맘스터치강남점",
    "amount": 13500,
    "expendedDate": "2025-10-08",
    "categoryId": 5,
    "categoryName": "패스트푸드",
    "mealType": "LUNCH",
    "items": [
      {
        "itemId": 1,
        "foodId": null,
        "foodName": "싸이버거 세트",
        "quantity": 1,
        "price": 6500,
        "hasFoodLink": false
      },
      {
        "itemId": 2,
        "foodId": null,
        "foodName": "치킨버거 세트",
        "quantity": 1,
        "price": 7000,
        "hasFoodLink": false
      }
    ],
    "createdAt": "2025-10-08T12:34:56.789Z",
    "hasStoreLink": false
  },
  "error": null
}
```

**Note:**
- `hasStoreLink`: `storeId`가 존재할 때만 `true` (가게 상세 페이지 링크 가능)
- 모든 항목의 `hasFoodLink`는 `false` (수기 입력이므로 `foodId` 없음)

---

### 6.3 장바구니 → 지출 내역 등록 (새 엔드포인트)

**Endpoint:** `POST /api/v1/expenditures/from-cart`

**설명:** 장바구니에서 직접 지출 내역을 등록합니다. 가게 ID(`storeId`)와 음식 ID(`foodId`)를 포함하여 저장합니다.

**Request:**
```json
{
  "storeId": 123,
  "storeName": "맘스터치강남점",
  "amount": 13500,
  "expendedDate": "2025-10-08",
  "expendedTime": "12:30:00",
  "categoryId": 5,
  "mealType": "LUNCH",
  "memo": "동료와 점심",
  "items": [
    {
      "foodId": 456,
      "foodName": "싸이버거 세트",
      "quantity": 1,
      "price": 6500
    },
    {
      "foodId": 789,
      "foodName": "치킨버거 세트",
      "quantity": 1,
      "price": 7000
    }
  ]
}
```

**Validation:**
- `storeId`: 필수
- `amount` >= 0
- `items` 총액 = `amount`

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "expenditureId": 790,
    "storeId": 123,
    "storeName": "맘스터치강남점",
    "amount": 13500,
    "expendedDate": "2025-10-08",
    "categoryId": 5,
    "categoryName": "패스트푸드",
    "mealType": "LUNCH",
    "items": [
      {
        "itemId": 1001,
        "foodId": 456,
        "foodName": "싸이버거 세트",
        "quantity": 1,
        "price": 6500,
        "hasFoodLink": true
      },
      {
        "itemId": 1002,
        "foodId": 789,
        "foodName": "치킨버거 세트",
        "quantity": 1,
        "price": 7000,
        "hasFoodLink": true
      }
    ],
    "createdAt": "2025-10-08T12:34:56.789Z",
    "hasStoreLink": true
  },
  "error": null
}
```

**Note:**
- `hasStoreLink`: `true` (storeId 존재)
- 모든 항목의 `hasFoodLink`: `true` (foodId 존재)
- 프론트엔드는 이 플래그들을 기반으로 상세 페이지 링크 표시 여부 결정

**Error Cases:**

*400 Bad Request - 필수 필드 누락:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E400",
    "message": "storeId는 필수입니다.",
    "data": null
  }
}
```

*422 Unprocessable Entity - 유효성 검증 실패:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E422",
    "message": "지출 항목의 총액이 지출 금액과 일치하지 않습니다.",
    "data": {
      "field": "items",
      "reason": "총액 13000 ≠ 금액 13500"
    }
  }
}
```

---

### 6.4 지출 내역 조회 (목록)

**Endpoint:** `GET /api/v1/expenditures?startDate=2025-10-01&endDate=2025-10-31&mealType=LUNCH&page=0&size=20`

**Query Parameters:**
- `startDate`, `endDate`: 조회 기간 (YYYY-MM-DD)
- `mealType` (optional): `BREAKFAST`, `LUNCH`, `DINNER`
- `categoryId` (optional): 카테고리 필터
- `page`, `size`: 페이징

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "summary": {
      "totalAmount": 125000,
      "totalCount": 28,
      "averageAmount": 4464
    },
    "expenditures": {
      "content": [
        {
          "expenditureId": 789,
          "storeName": "맘스터치강남점",
          "amount": 13500,
          "expendedDate": "2025-10-08",
          "categoryName": "패스트푸드",
          "mealType": "LUNCH"
        }
      ],
      "pageable": { ... },
      "totalElements": 28
    }
  },
  "error": null
}
```

---

### 6.5 지출 내역 상세 조회

**Endpoint:** `GET /api/v1/expenditures/{expenditureId}`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "expenditureId": 789,
    "storeName": "맘스터치강남점",
    "amount": 13500,
    "expendedDate": "2025-10-08",
    "expendedTime": "12:30:00",
    "categoryId": 5,
    "categoryName": "패스트푸드",
    "mealType": "LUNCH",
    "memo": "동료와 점심",
    "items": [
      {
        "expenditureItemId": 1001,
        "foodName": "싸이버거 세트",
        "quantity": 1,
        "price": 6500
      },
      {
        "expenditureItemId": 1002,
        "foodName": "치킨버거 세트",
        "quantity": 1,
        "price": 7000
      }
    ],
    "createdAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

### 6.6 지출 내역 수정

**Endpoint:** `PUT /api/v1/expenditures/{expenditureId}`

**Request:** 등록과 동일한 구조

**Response (200):** 상세 조회와 동일한 구조

---

### 6.7 지출 내역 삭제

**Endpoint:** `DELETE /api/v1/expenditures/{expenditureId}`

**Response (204):** No Content

**Note:** Soft Delete 처리

---

### 6.8 일별 지출 통계 조회

**Endpoint:** `GET /api/v1/expenditures/statistics/daily?year=2025&month=10`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "year": 2025,
    "month": 10,
    "dailyStatistics": [
      {
        "date": "2025-10-01",
        "totalAmount": 12500,
        "budget": 10000,
        "overBudget": true
      },
      {
        "date": "2025-10-02",
        "totalAmount": 8500,
        "budget": 10000,
        "overBudget": false
      }
    ]
  },
  "error": null
}
```

---

## 7. 가게 관리 API

### 7.1 가게 목록 조회

**Endpoint:** `GET /api/v1/stores`

**Query Parameters:**
커서 기반 페이징 (권장):
```
?keyword=치킨
&radius=0.5
&categoryId=5
&isOpen=true
&storeType=RESTAURANT
&sortBy=distance
&lastId=101
&limit=20
```

오프셋 기반 페이징 (하위 호환성):
```
?keyword=치킨
&radius=0.5
&categoryId=5
&isOpen=true
&storeType=RESTAURANT
&sortBy=distance
&page=0
&size=20
```

**Parameters:**
- `keyword` (optional): 가게명 또는 카테고리 검색어
- `radius` (optional): 반경 (0.5 ~ 50 km) - 기본값: 3.0
- `categoryId` (optional): 카테고리 필터
- `isOpen` (optional): true면 영업 중만 조회
- `storeType` (optional): `CAMPUS_RESTAURANT`, `RESTAURANT`, `ALL` (기본값: ALL)
- `sortBy` (optional): `distance`, `reviewCount`, `priceAsc`, `priceDesc`, `favoriteCount`, `viewCount`

**커서 기반 페이징 파라미터:**
- `lastId` (optional): 이전 응답의 마지막 가게 ID (첫 요청시 생략)
- `limit` (optional): 조회할 개수 (1-100, 기본값: 20)

**오프셋 기반 페이징 파라미터:**
- `page` (optional): 페이지 번호 (0부터 시작, 기본값: 0)
- `size` (optional): 페이지 크기 (1-100, 기본값: 20)

**Note:**
- 사용자의 **기본 주소(primary address)**를 기준으로 거리 계산
- 기본 주소가 없으면 404 에러 반환
- 커서 기반 페이징 사용 시 무한 스크롤 구현 가능

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "stores": [
      {
        "storeId": 101,
        "name": "교촌치킨 강남점",
        "categoryId": 5,
        "categoryName": "치킨",
        "address": "서울특별시 강남구 테헤란로 123",
        "latitude": 37.498123,
        "longitude": 127.028456,
        "distance": 0.45,
        "averagePrice": 18000,
        "reviewCount": 1523,
        "viewCount": 8945,
        "storeType": "RESTAURANT",
        "phoneNumber": "02-1234-5678",
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg"
      }
    ],
    "totalCount": 45,
    "currentPage": 0,
    "pageSize": 20,
    "totalPages": 3,
    "hasMore": true,
    "lastId": 101
  },
  "error": null
}
```

---

### 7.2 가게 상세 조회

**Endpoint:** `GET /api/v1/stores/{storeId}`

**설명:**
- 가게 상세 정보를 조회합니다.
- 조회 시 `store_view_history` 테이블에 조회 이력이 자동으로 기록됩니다.
- `view_count`가 1 증가합니다.

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "storeId": 101,
    "name": "교촌치킨 강남점",
    "categoryId": 5,
    "categoryName": "치킨",
    "address": "서울특별시 강남구 테헤란로 123",
    "lotNumberAddress": "서울특별시 강남구 역삼동 456-78",
    "latitude": 37.498123,
    "longitude": 127.028456,
    "phoneNumber": "02-1234-5678",
    "description": "1991년부터 이어온 전통의 맛",
    "averagePrice": 18000,
    "reviewCount": 1523,
    "viewCount": 8946,
    "favoriteCount": 234,
    "isCampusRestaurant": false,
    "isFavorite": true,
    "isTemporaryClosed": false,
    "imageUrls": [
      "https://cdn.smartmealtable.com/stores/101/main.jpg",
      "https://cdn.smartmealtable.com/stores/101/menu.jpg"
    ],
    "openingHours": [
      {
        "dayOfWeek": "MONDAY",
        "openTime": "11:00:00",
        "closeTime": "22:00:00",
        "breakStartTime": "15:00:00",
        "breakEndTime": "17:00:00",
        "isHoliday": false
      },
      {
        "dayOfWeek": "SUNDAY",
        "isHoliday": true
      }
    ],
    "temporaryClosures": [
      {
        "closureDate": "2025-10-15",
        "startTime": "11:00:00",
        "endTime": "22:00:00",
        "reason": "사정으로 인한 임시 휴무"
      }
    ],
    "recommendedMenus": [
      {
        "foodId": 201,
        "foodName": "교촌 오리지널",
        "price": 18000,
        "description": "교촌의 시그니처 메뉴",
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
        "isAvailable": true,
        "recommendationScore": 92.5,
        "budgetComparison": {
          "userMealBudget": 20000,
          "difference": 2000,
          "isOverBudget": false,
          "differenceText": "-2,000원"
        }
      },
      {
        "foodId": 202,
        "foodName": "교촌 레드",
        "price": 18000,
        "description": "매콤한 양념치킨",
        "imageUrl": "https://cdn.smartmealtable.com/foods/202.jpg",
        "isAvailable": true,
        "recommendationScore": 88.3,
        "budgetComparison": {
          "userMealBudget": 20000,
          "difference": 2000,
          "isOverBudget": false,
          "differenceText": "-2,000원"
        }
      }
    ],
    "menus": [
      {
        "foodId": 201,
        "foodName": "교촌 오리지널",
        "price": 18000,
        "description": "교촌의 시그니처 메뉴",
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
        "isAvailable": true,
        "budgetComparison": {
          "userMealBudget": 20000,
          "difference": 2000,
          "isOverBudget": false,
          "differenceText": "-2,000원"
        }
      },
      {
        "foodId": 202,
        "foodName": "교촌 레드",
        "price": 18000,
        "description": "매콤한 양념치킨",
        "imageUrl": "https://cdn.smartmealtable.com/foods/202.jpg",
        "isAvailable": true,
        "budgetComparison": {
          "userMealBudget": 20000,
          "difference": 2000,
          "isOverBudget": false,
          "differenceText": "-2,000원"
        }
      },
      {
        "foodId": 203,
        "foodName": "교촌 허니콤보",
        "price": 21000,
        "description": "달콤한 허니 소스",
        "imageUrl": "https://cdn.smartmealtable.com/foods/203.jpg",
        "isAvailable": true,
        "budgetComparison": {
          "userMealBudget": 20000,
          "difference": -1000,
          "isOverBudget": true,
          "differenceText": "+1,000원"
        }
      }
    ],
    "seller": {
      "sellerId": 501,
      "businessNumber": "123-45-67890",
      "ownerName": "김사장"
    },
    "createdAt": "2024-01-15T09:00:00.000Z"
  },
  "error": null
}
```

**Note:** 
- 조회 시 `store_view_history` 테이블에 자동 기록
- `view_count` 1 증가
- 조회 이력 기록 시점: 사용자가 가게 목록에서 가게 카드를 터치하여 상세 페이지로 진입한 시점

---

### 7.3 가게 검색 (자동완성)

**Endpoint:** `GET /api/v1/stores/autocomplete?keyword=치킨&limit=10`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": [
    {
      "storeId": 101,
      "name": "교촌치킨 강남점",
      "categoryName": "치킨"
    },
    {
      "storeId": 102,
      "name": "bhc치킨 역삼점",
      "categoryName": "치킨"
    }
  ],
  "error": null
}
```

---

### 7.4 메뉴 상세 조회

**Endpoint:** `GET /api/v1/foods/{foodId}`

**설명:**
- 특정 메뉴의 상세 정보를 조회합니다.
- 가게 정보, 메뉴 정보, 사용자의 현재 예산과의 비교 정보를 함께 반환합니다.

**Path Parameters:**
- `foodId`: 조회할 메뉴의 ID

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "foodId": 201,
    "foodName": "교촌 오리지널",
    "description": "교촌의 시그니처 메뉴",
    "price": 18000,
    "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
    "store": {
      "storeId": 101,
      "storeName": "교촌치킨 강남점",
      "categoryName": "치킨",
      "address": "서울특별시 강남구 테헤란로 123",
      "phoneNumber": "02-1234-5678",
      "averagePrice": 18000,
      "reviewCount": 1523,
      "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg"
    },
    "isAvailable": true,
    "budgetComparison": {
      "userMealBudget": 20000,
      "foodPrice": 18000,
      "difference": 2000,
      "isOverBudget": false,
      "differenceText": "-2,000원"
    }
  },
  "error": null
}
```

**Response Fields:**
- `foodId`: 메뉴 고유 식별자
- `foodName`: 메뉴 이름
- `description`: 메뉴 설명
- `price`: 메뉴 가격
- `imageUrl`: 메뉴 이미지 URL
- `store`: 이 메뉴를 판매하는 가게 정보
- `isAvailable`: 메뉴 판매 가능 여부
- `budgetComparison`: 사용자 예산과의 비교 정보

**Error Cases:**
- `404`: 메뉴를 찾을 수 없음

---

## 8. 음식 API

본 섹션은 개별 메뉴 조회 및 음식 관련 기능을 제공합니다.

### 8.1 메뉴 상세 조회

**Endpoint:** `GET /api/v1/foods/{foodId}`

**설명:**
- 특정 메뉴의 상세 정보를 조회합니다.
- 가게 정보, 메뉴 정보, 사용자의 현재 예산과의 비교 정보를 함께 반환합니다.
- JWT 인증이 필요합니다.

**Path Parameters:**
- `foodId`: 조회할 메뉴의 ID (필수)

**Query Parameters:**
- 없음

**Request Header:**
```http
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "foodId": 201,
    "foodName": "교촌 오리지널",
    "description": "교촌의 시그니처 메뉴",
    "price": 18000,
    "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
    "store": {
      "storeId": 101,
      "storeName": "교촌치킨 강남점",
      "categoryName": "치킨",
      "address": "서울특별시 강남구 테헤란로 123",
      "phoneNumber": "02-1234-5678",
      "averagePrice": 18000,
      "reviewCount": 1523,
      "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg"
    },
    "isAvailable": true,
    "budgetComparison": {
      "userMealBudget": 20000,
      "foodPrice": 18000,
      "difference": 2000,
      "isOverBudget": false,
      "differenceText": "-2,000원"
    }
  },
  "error": null
}
```

**Response Fields:**
- `foodId`: 메뉴 고유 식별자
- `foodName`: 메뉴 이름
- `description`: 메뉴 설명
- `price`: 메뉴 가격 (정수, 원 단위)
- `imageUrl`: 메뉴 이미지 URL
- `store`: 이 메뉴를 판매하는 가게 정보
  - `storeId`: 가게 고유 식별자
  - `storeName`: 가게 이름
  - `categoryName`: 음식 카테고리 이름 (예: 치킨, 피자, 한식)
  - `address`: 가게 주소
  - `phoneNumber`: 가게 전화번호
  - `averagePrice`: 가게의 평균 메뉴 가격
  - `reviewCount`: 리뷰 개수
  - `imageUrl`: 가게 이미지 URL
- `isAvailable`: 메뉴 판매 가능 여부 (boolean)
- `budgetComparison`: 사용자 예산과의 비교 정보
  - `userMealBudget`: 사용자의 현재 끼니 예산
  - `foodPrice`: 메뉴 가격
  - `difference`: 예산과의 차이 (음수면 남음, 양수면 초과)
  - `isOverBudget`: 예산 초과 여부
  - `differenceText`: 포맷팅된 차이 문자열 (예: "-2,000원", "+5,000원")

**Error Cases:**

**404 Not Found - 메뉴를 찾을 수 없음:**
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "FOOD_NOT_FOUND",
    "message": "존재하지 않는 메뉴입니다.",
    "data": null
  }
}
```

**401 Unauthorized - 인증 실패:**
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E401",
    "message": "인증 토큰이 유효하지 않습니다.",
    "data": null
  }
}
```

**인증 및 권한:**
- JWT 토큰 필수 (Authorization Header에 Bearer 토큰)
- 사용자의 현재 예산 정보를 기반으로 `budgetComparison` 계산

**예제:**

Request:
```bash
curl -X GET "https://api.smartmealtable.com/api/v1/foods/201" \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json"
```

Response:
```json
{
  "result": "SUCCESS",
  "data": {
    "foodId": 201,
    "foodName": "교촌 오리지널",
    "price": 18000,
    ...
  },
  "error": null
}
```

---

### 8.2 가게의 메뉴 목록 (가게 상세 조회에 포함)

**Endpoint:** `GET /api/v1/stores/{storeId}`

**설명:**
- 가게 상세 정보 조회 시 해당 가게의 모든 메뉴 목록이 포함됩니다.
- `menus` 배열에 해당 가게에서 판매하는 모든 메뉴 정보가 포함됩니다.

**Response (200) - 가게 상세 조회 응답 예시:**
```json
{
  "result": "SUCCESS",
  "data": {
    "storeId": 101,
    "storeName": "교촌치킨 강남점",
    "categoryId": 5,
    "categoryName": "치킨",
    "address": "서울특별시 강남구 테헤란로 123",
    "phoneNumber": "02-1234-5678",
    "averagePrice": 18000,
    "reviewCount": 1523,
    "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg",
    "menus": [
      {
        "foodId": 201,
        "name": "교촌 오리지널",
        "storeId": 101,
        "categoryId": 5,
        "description": "교촌의 시그니처 메뉴",
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
        "averagePrice": 18000
      },
      {
        "foodId": 202,
        "name": "교촌 반반",
        "storeId": 101,
        "categoryId": 5,
        "description": "순살과 뼈의 반반 구성",
        "imageUrl": "https://cdn.smartmealtable.com/foods/202.jpg",
        "averagePrice": 20000
      },
      {
        "foodId": 203,
        "name": "교촌 순살",
        "storeId": 101,
        "categoryId": 5,
        "description": "순살만 정성껏 튀겨낸 메뉴",
        "imageUrl": "https://cdn.smartmealtable.com/foods/203.jpg",
        "averagePrice": 22000
      }
    ]
  },
  "error": null
}
```

**메뉴 정보 필드:**
- `foodId`: 메뉴 고유 식별자
- `name`: 메뉴 이름
- `storeId`: 판매 가게 ID
- `categoryId`: 음식 카테고리 ID
- `description`: 메뉴 설명
- `imageUrl`: 메뉴 이미지 URL
- `averagePrice`: 메뉴 평균 가격

**주의사항:**
- 메뉴 목록은 가게 상세 조회 응답에 자동으로 포함됩니다.
- 별도의 메뉴 목록 조회 엔드포인트는 없습니다.
- 개별 메뉴 상세 정보는 8.1의 메뉴 상세 조회를 사용하세요.

---

## 9. 추천 시스템 API

### 9.1 개인화 추천 (기본)

**Endpoint:** `GET /api/v1/recommendations`

**Query Parameters:**
커서 기반 페이징 (권장):
```
?latitude=37.497942
&longitude=127.027621
&radius=0.5
&lastId=101
&limit=20
```

오프셋 기반 페이징 (하위 호환성):
```
?latitude=37.497942
&longitude=127.027621
&radius=0.5
&page=0
&size=20
```

**Parameters:**
- `latitude` (required): 현재 위도 (-90 ~ 90)
- `longitude` (required): 현재 경도 (-180 ~ 180)
- `radius` (optional): 반경 (0.1 ~ 10 km, 기본값: 0.5)
- `sortBy` (optional): `SCORE`, `reviewCount`, `distance` (기본값: SCORE)
- `includeDisliked` (optional): 불호 음식 포함 여부 (기본값: false)
- `openNow` (optional): 영업 중인 가게만 조회 (기본값: false)
- `storeType` (optional): `ALL`, `CAMPUS_RESTAURANT`, `RESTAURANT` (기본값: ALL)

**커서 기반 페이징 파라미터:**
- `lastId` (optional): 이전 응답의 마지막 항목 ID (첫 요청시 생략)
- `limit` (optional): 조회할 개수 (1-100, 기본값: 20)

**오프셋 기반 페이징 파라미터:**
- `page` (optional): 페이지 번호 (0부터 시작, 기본값: 0)
- `size` (optional): 페이지 크기 (1-100, 기본값: 20)

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": [
    {
      "storeId": 101,
      "name": "교촌치킨 강남점",
      "categoryName": "치킨",
      "address": "서울특별시 강남구 테헤란로 123",
      "latitude": 37.498123,
      "longitude": 127.028456,
      "distance": 0.45,
      "averagePrice": 18000,
      "reviewCount": 1523,
      "recommendationScore": 87.5,
      "scores": {
        "stability": 85.0,
        "exploration": 72.0,
        "budgetEfficiency": 90.0,
        "accessibility": 95.0
      },
      "isFavorite": false,
      "isOpen": true,
      "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg"
    }
  ],
  "totalCount": 45,
  "currentPage": 0,
  "pageSize": 20,
  "totalPages": 3,
  "hasMore": true,
  "lastId": 101,
  "error": null
}
```

---

### 9.2 추천 점수 상세 조회

**Endpoint:** `GET /api/v1/recommendations/{storeId}/scores`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "storeId": 101,
    "storeName": "교촌치킨 강남점",
    "recommendationScore": 87.5,
    "stabilityScore": {
      "total": 85.0,
      "details": {
        "preferenceScore": 100.0,
        "pastExpenditureScore": 75.0,
        "reviewTrustScore": 80.0
      }
    },
    "explorationScore": {
      "total": 72.0,
      "details": {
        "categoryFreshnessScore": 60.0,
        "storeNewnessScore": 70.0,
        "recentInterestScore": 85.0
      }
    },
    "budgetEfficiencyScore": {
      "total": 90.0,
      "details": {
        "valueForMoneyScore": 95.0,
        "budgetFitScore": 85.0
      }
    },
    "accessibilityScore": {
      "total": 95.0,
      "details": {
        "distance": 0.45,
        "distanceScore": 95.0
      }
    }
  },
  "error": null
}
```

---

### 9.3 추천 유형 변경

**Endpoint:** `PUT /api/v1/members/me/recommendation-type`

**Request:**
```json
{
  "recommendationType": "SAVER"
}
```

**Enum Values:**
- `SAVER`: 절약형 (예산효율성 50%, 안정성 30%, 탐험성 15%, 접근성 5%)
- `ADVENTURER`: 모험형 (탐험성 50%, 안정성 30%, 예산효율성 10%, 접근성 10%)
- `BALANCED`: 균형형 (예산효율성 30%, 안정성 30%, 탐험성 25%, 접근성 15%)

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "recommendationType": "SAVER",
    "weights": {
      "stability": 0.30,
      "exploration": 0.15,
      "budgetEfficiency": 0.50,
      "accessibility": 0.05
    },
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

## 10. 즐겨찾기 API

### 9.1 즐겨찾기 추가

**Endpoint:** `POST /api/v1/favorites`

**Request:**
```json
{
  "storeId": 101
}
```

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "favoriteId": 301,
    "storeId": 101,
    "storeName": "교촌치킨 강남점",
    "displayOrder": 5,
    "createdAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Error Cases:**
- `409`: 이미 즐겨찾기에 추가됨

---

### 9.2 즐겨찾기 목록 조회

**Endpoint:** `GET /api/v1/favorites`

**Query Parameters:**
```
?sortBy=displayOrder
&isOpenOnly=false
&categoryId=5
&page=0
&size=20
```

**Parameters:**
- `sortBy` (optional): 정렬 기준
  - `displayOrder`: 사용자 지정 순서 (기본값)
  - `name`: 이름 가나다순
  - `reviewCount`: 리뷰 많은 순
  - `distance`: 거리 순 (기본 주소 기준)
  - `createdAt`: 최근 추가 순
- `isOpenOnly` (optional): true면 현재 영업 중인 가게만 조회 (기본값: false)
- `categoryId` (optional): 특정 카테고리 필터링
- `page`, `size`: 페이징 (기본값: page=0, size=20)

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "favorites": [
      {
        "favoriteId": 301,
        "storeId": 101,
        "storeName": "교촌치킨 강남점",
        "categoryId": 5,
        "categoryName": "치킨",
        "address": "서울특별시 강남구 테헤란로 123",
        "distance": 0.8,
        "averagePrice": 18000,
        "reviewCount": 1523,
        "displayOrder": 1,
        "isOpenNow": true,
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg",
        "createdAt": "2025-09-15T10:20:30.000Z"
      }
    ],
    "totalCount": 15,
    "openCount": 12,
    "page": 0,
    "size": 20,
    "totalPages": 1
  },
  "error": null
}
```

**Note:**
- `distance`: 사용자의 기본 주소(primary address)를 기준으로 계산
- `isOpenNow`: 현재 시간 기준 영업 여부
- `openCount`: 전체 즐겨찾기 중 현재 영업 중인 가게 수

---

### 9.3 즐겨찾기 순서 변경

**Endpoint:** `PUT /api/v1/favorites/order`

**Request:**
```json
{
  "favoriteOrders": [
    {
      "favoriteId": 301,
      "displayOrder": 2
    },
    {
      "favoriteId": 302,
      "displayOrder": 1
    },
    {
      "favoriteId": 303,
      "displayOrder": 3
    }
  ]
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "message": "즐겨찾기 순서가 변경되었습니다.",
    "updatedCount": 3
  },
  "error": null
}
```

---

### 9.4 즐겨찾기 삭제

**Endpoint:** `DELETE /api/v1/favorites/{favoriteId}`

**Response (204):** No Content

---

## 11. 프로필 및 설정 API

### 10.1 내 프로필 조회

**Endpoint:** `GET /api/v1/members/me`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "memberId": 123,
    "nickname": "길동이",
    "email": "hong@example.com",
    "name": "홍길동",
    "recommendationType": "BALANCED",
    "group": {
      "groupId": 123,
      "name": "서울대학교",
      "type": "UNIVERSITY"
    },
    "socialAccounts": [
      {
        "provider": "KAKAO",
        "connectedAt": "2025-09-01T10:00:00.000Z"
      }
    ],
    "passwordExpiresAt": "2026-01-06T12:34:56.789Z",
    "createdAt": "2025-09-01T10:00:00.000Z"
  },
  "error": null
}
```

---

### 10.2 프로필 수정

**Endpoint:** `PUT /api/v1/members/me`

**Request:**
```json
{
  "nickname": "새로운닉네임",
  "groupId": 456
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "memberId": 123,
    "nickname": "새로운닉네임",
    "group": {
      "groupId": 456,
      "name": "고려대학교",
      "type": "UNIVERSITY"
    },
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

### 10.3 주소 목록 조회

**Endpoint:** `GET /api/v1/members/me/addresses`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": [
    {
      "addressHistoryId": 456,
      "addressAlias": "우리집",
      "addressType": "HOME",
      "streetNameAddress": "서울특별시 강남구 테헤란로 123",
      "detailedAddress": "101동 1234호",
      "latitude": 37.497942,
      "longitude": 127.027621,
      "isPrimary": true,
      "createdAt": "2025-09-01T10:00:00.000Z"
    }
  ],
  "error": null
}
```

---

### 10.4 주소 추가

**Endpoint:** `POST /api/v1/members/me/addresses`

**Request:**
```json
{
  "addressAlias": "회사",
  "addressType": "OFFICE",
  "streetNameAddress": "서울특별시 강남구 테헤란로 234",
  "lotNumberAddress": "서울특별시 강남구 역삼동 567-89",
  "detailedAddress": "200동 2345호",
  "latitude": 37.498500,
  "longitude": 127.029000
}
```

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "addressHistoryId": 457,
    "addressAlias": "회사",
    "addressType": "OFFICE",
    "streetNameAddress": "서울특별시 강남구 테헤란로 234",
    "detailedAddress": "200동 2345호",
    "latitude": 37.498500,
    "longitude": 127.029000,
    "isPrimary": false,
    "createdAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Note:**
- 첫 번째 주소 등록 시 자동으로 기본 주소로 설정됩니다.
- 추가 주소 등록 시 기본 주소 설정은 별도의 API를 이용합니다.

---

### 10.5 주소 수정

**Endpoint:** `PUT /api/v1/members/me/addresses/{addressHistoryId}`

**Request:**
```json
{
  "addressAlias": "회사 (강남)",
  "addressType": "OFFICE",
  "streetNameAddress": "서울특별시 강남구 테헤란로 234",
  "lotNumberAddress": "서울특별시 강남구 역삼동 567-89",
  "detailedAddress": "200동 2345호",
  "latitude": 37.498500,
  "longitude": 127.029000
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "addressHistoryId": 457,
    "addressAlias": "회사 (강남)",
    "addressType": "OFFICE",
    "streetNameAddress": "서울특별시 강남구 테헤란로 234",
    "detailedAddress": "200동 2345호",
    "latitude": 37.498500,
    "longitude": 127.029000,
    "isPrimary": false,
    "createdAt": "2025-10-08T12:34:56.789Z",
    "updatedAt": "2025-10-08T13:45:00.000Z"
  },
  "error": null
}
```

---

### 10.6 주소 삭제

**Endpoint:** `DELETE /api/v1/members/me/addresses/{addressHistoryId}`

**Response (204):** No Content

**Error Cases:**
- `409`: 기본 주소는 다른 주소가 있을 때만 삭제 가능

---

### 10.7 기본 주소 설정

**Endpoint:** `PUT /api/v1/members/me/addresses/{addressHistoryId}/primary`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "addressHistoryId": 456,
    "isPrimary": true,
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

### 10.8 선호도 조회

**Endpoint:** `GET /api/v1/members/me/preferences`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "recommendationType": "BALANCED",
    "categoryPreferences": [
      {
        "preferenceId": 701,
        "categoryId": 1,
        "categoryName": "한식",
        "weight": 100
      },
      {
        "preferenceId": 702,
        "categoryId": 3,
        "categoryName": "일식",
        "weight": -100
      }
    ],
    "foodPreferences": {
      "liked": [
        {
          "foodPreferenceId": 801,
          "foodId": 12,
          "foodName": "김치찌개",
          "categoryName": "한식",
          "createdAt": "2025-09-01T10:00:00.000Z"
        }
      ],
      "disliked": [
        {
          "foodPreferenceId": 802,
          "foodId": 35,
          "foodName": "생굴",
          "categoryName": "일식",
          "createdAt": "2025-09-01T10:00:00.000Z"
        }
      ]
    }
  },
  "error": null
}
```

**Note:**
- `categoryPreferences`: 카테고리 기반 선호도 (weight: 100=좋아요, 0=보통, -100=싫어요)
- `foodPreferences.liked`: 좋아하는 개별 음식 목록
- `foodPreferences.disliked`: 싫어하는 개별 음식 목록

---

### 10.9 카테고리 선호도 수정

**Endpoint:** `PUT /api/v1/members/me/preferences/categories`

**Request:**
```json
{
  "preferences": [
    {
      "categoryId": 1,
      "weight": 100
    },
    {
      "categoryId": 2,
      "weight": 0
    },
    {
      "categoryId": 3,
      "weight": -100
    }
  ]
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "updatedCount": 3,
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

### 10.10 개별 음식 선호도 추가

**Endpoint:** `POST /api/v1/members/me/preferences/foods`

**Request:**
```json
{
  "foodId": 12,
  "isPreferred": true
}
```

**Parameters:**
- `foodId`: 음식 ID (필수)
- `isPreferred`: true=좋아요, false=싫어요 (필수)

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "foodPreferenceId": 803,
    "foodId": 12,
    "foodName": "김치찌개",
    "categoryName": "한식",
    "isPreferred": true,
    "createdAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Error Cases:**
- `409`: 이미 해당 음식에 대한 선호도가 등록되어 있음

---

### 10.11 개별 음식 선호도 변경

**Endpoint:** `PUT /api/v1/members/me/preferences/foods/{foodPreferenceId}`

**Request:**
```json
{
  "isPreferred": false
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "foodPreferenceId": 803,
    "foodId": 12,
    "foodName": "김치찌개",
    "categoryName": "한식",
    "isPreferred": false,
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

### 10.12 개별 음식 선호도 삭제

**Endpoint:** `DELETE /api/v1/members/me/preferences/foods/{foodPreferenceId}`

**Response (204):** No Content

---

## 12. 홈 화면 API

### 11.1 홈 대시보드 조회

**Endpoint:** `GET /api/v1/home/dashboard`

**설명:** 
- 사용자의 **기본 주소(primary address)**를 기준으로 홈 대시보드 정보를 제공합니다.
- 기본 주소가 없는 경우 에러를 반환합니다.

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "location": {
      "addressHistoryId": 456,
      "addressAlias": "우리집",
      "fullAddress": "서울특별시 강남구 테헤란로 123 테헤란빌딩 101동 101호",
      "roadAddress": "서울특별시 강남구 테헤란로 123",
      "latitude": 37.497942,
      "longitude": 127.027621,
      "isPrimary": true
    },
    "budget": {
      "todaySpent": 12500,
      "todayBudget": 15000,
      "remaining": 2500,
      "utilizationRate": 83.33,
      "mealBudgets": [
        {
          "mealType": "BREAKFAST",
          "budget": 3000,
          "spent": 0,
          "remaining": 3000
        },
        {
          "mealType": "LUNCH",
          "budget": 5000,
          "spent": 5500,
          "remaining": -500
        },
        {
          "mealType": "DINNER",
          "budget": 7000,
          "spent": 7000,
          "remaining": 0
        }
      ]
    },
    "recommendedMenus": [
      {
        "foodId": 201,
        "foodName": "김치찌개",
        "price": 7000,
        "storeId": 101,
        "storeName": "맛있는집",
        "distance": 0.3,
        "tags": ["인기메뉴", "예산적합"],
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg"
      },
      {
        "foodId": 202,
        "foodName": "제육볶음",
        "price": 8000,
        "storeId": 102,
        "storeName": "학생식당",
        "distance": 0.5,
        "tags": ["추천", "학식"],
        "imageUrl": "https://cdn.smartmealtable.com/foods/202.jpg"
      }
    ],
    "recommendedStores": [
      {
        "storeId": 101,
        "storeName": "맛있는집",
        "categoryName": "한식",
        "distance": 0.3,
        "distanceText": "도보 5분 거리",
        "contextInfo": "학교 근처",
        "averagePrice": 7500,
        "reviewCount": 523,
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg"
      },
      {
        "storeId": 102,
        "storeName": "학생식당",
        "categoryName": "학식",
        "distance": 0.2,
        "distanceText": "도보 3분 거리",
        "contextInfo": "학교 내부",
        "averagePrice": 5000,
        "reviewCount": 1204,
        "imageUrl": "https://cdn.smartmealtable.com/stores/102/main.jpg"
      }
    ]
  },
  "error": null
}
```

**Error Cases:**

*404 Not Found - 등록된 주소가 없음:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "ADDRESS_002",
    "message": "등록된 주소가 없습니다. 주소를 먼저 등록해주세요.",
    "data": {
      "suggestion": "주소 등록 화면으로 이동"
    }
  }
}
```

**Note:**
- `location`: 사용자의 기본 주소(isPrimary=true) 정보
- `distance`: 기본 주소를 기준으로 계산
- `recommendedMenus`, `recommendedStores`: 기본 주소 기준 추천

---

### 11.2 온보딩 상태 조회

**Endpoint:** `GET /api/v1/members/me/onboarding-status`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "isOnboardingComplete": true,
    "hasSelectedRecommendationType": false,
    "hasConfirmedMonthlyBudget": false,
    "currentMonth": "2025-10",
    "showRecommendationTypeModal": true,
    "showMonthlyBudgetModal": true
  },
  "error": null
}
```

**Note:**
- `showRecommendationTypeModal`: 최초 온보딩 후 추천 유형 미선택 시 true
- `showMonthlyBudgetModal`: 매월 1일 이후 최초 방문 시 true

---

### 11.3 월별 예산 확인 처리

**Endpoint:** `POST /api/v1/members/me/monthly-budget-confirmed`

**Request:**
```json
{
  "year": 2025,
  "month": 10,
  "action": "KEEP"
}
```

**Enum Values:**
- `action`: `KEEP` (기존 유지), `CHANGE` (변경하러 가기)

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "year": 2025,
    "month": 10,
    "confirmedAt": "2025-10-08T12:34:56.789Z",
    "monthlyBudget": 300000
  },
  "error": null
}
```

---

## 13. 장바구니 API

### 12.1 장바구니 조회

**Endpoint:** `GET /api/v1/cart`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "cartId": 501,
    "storeId": 101,
    "storeName": "맛있는집",
    "items": [
      {
        "cartItemId": 1001,
        "foodId": 201,
        "foodName": "김치찌개",
        "price": 7000,
        "quantity": 2,
        "options": [
          {
            "optionName": "맵기",
            "optionValue": "보통",
            "additionalPrice": 0
          }
        ],
        "totalPrice": 14000,
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg"
      },
      {
        "cartItemId": 1002,
        "foodId": 202,
        "foodName": "된장찌개",
        "price": 6500,
        "quantity": 1,
        "options": [],
        "totalPrice": 6500,
        "imageUrl": "https://cdn.smartmealtable.com/foods/202.jpg"
      }
    ],
    "subtotal": 20500,
    "discount": 0,
    "totalAmount": 20500,
    "budgetInfo": {
      "currentMealType": "LUNCH",
      "mealBudget": 5000,
      "dailyBudgetBefore": 15000,
      "dailyBudgetAfter": -5500,
      "monthlyBudgetBefore": 300000,
      "monthlyBudgetAfter": 279500,
      "isOverBudget": true
    }
  },
  "error": null
}
```

**Empty Cart (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "cartId": null,
    "items": [],
    "totalAmount": 0
  },
  "error": null
}
```

---

### 12.2 장바구니에 상품 추가

**Endpoint:** `POST /api/v1/cart/items`

**Request:**
```json
{
  "storeId": 101,
  "foodId": 201,
  "quantity": 2,
  "replaceCart": false,
  "options": [
    {
      "optionName": "맵기",
      "optionValue": "보통"
    }
  ]
}
```

**Parameters:**
- `storeId`: 가게 ID (필수)
- `foodId`: 음식 ID (필수)
- `quantity`: 수량 (필수, 1 이상)
- `replaceCart`: 다른 가게의 상품이 있을 때 자동으로 기존 장바구니를 비우고 추가할지 여부 (선택, 기본값: false)
- `options`: 음식 옵션 (선택)

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "cartItemId": 1001,
    "foodId": 201,
    "foodName": "김치찌개",
    "quantity": 2,
    "totalPrice": 14000,
    "cartTotalAmount": 14000,
    "replacedCart": false
  },
  "error": null
}
```

**Response Fields:**
- `replacedCart`: 기존 장바구니를 자동으로 비웠는지 여부 (replaceCart=true일 때)

**Error Cases:**

*409 Conflict - 다른 가게의 상품이 장바구니에 있음 (replaceCart=false인 경우):*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "CART_001",
    "message": "다른 가게의 상품이 장바구니에 있습니다. 기존 장바구니를 비우고 새로운 상품을 추가하시겠습니까?",
    "data": {
      "currentStoreId": 102,
      "currentStoreName": "다른집",
      "requestedStoreId": 101,
      "requestedStoreName": "맛있는집",
      "suggestion": "replaceCart=true로 재요청하거나 장바구니를 먼저 비워주세요."
    }
  }
}
```

**Note:**
- `replaceCart=true`로 설정하면 다른 가게의 상품이 있어도 자동으로 기존 장바구니를 비우고 새 상품을 추가합니다.
- 클라이언트는 409 에러 수신 시 사용자에게 확인 후 `replaceCart=true`로 재요청할 수 있습니다.

---

### 12.3 장바구니 상품 수량 변경

**Endpoint:** `PUT /api/v1/cart/items/{cartItemId}`

**Request:**
```json
{
  "quantity": 3
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "cartItemId": 1001,
    "quantity": 3,
    "totalPrice": 21000,
    "cartTotalAmount": 27500
  },
  "error": null
}
```

---

### 12.4 장바구니 상품 삭제

**Endpoint:** `DELETE /api/v1/cart/items/{cartItemId}`

**Response (204):** No Content

---

### 12.5 장바구니 전체 비우기

**Endpoint:** `DELETE /api/v1/cart`

**Response (204):** No Content

---

### 12.6 장바구니 → 지출 등록

**Endpoint:** `POST /api/v1/cart/checkout`

**Request:**
```json
{
  "mealType": "LUNCH",
  "discount": 1000,
  "expendedDate": "2025-10-08",
  "expendedTime": "12:30:00"
}
```

**Response (201):**
```json
{
  "result": "SUCCESS",
  "data": {
    "expenditureId": 789,
    "storeName": "맛있는집",
    "items": [
      {
        "foodName": "김치찌개",
        "quantity": 2,
        "price": 14000
      },
      {
        "foodName": "된장찌개",
        "quantity": 1,
        "price": 6500
      }
    ],
    "subtotal": 20500,
    "discount": 1000,
    "finalAmount": 19500,
    "mealType": "LUNCH",
    "expendedDate": "2025-10-08",
    "expendedTime": "12:30:00",
    "budgetSummary": {
      "mealBudgetBefore": 5000,
      "mealBudgetAfter": -14500,
      "dailyBudgetBefore": 15000,
      "dailyBudgetAfter": -4500,
      "monthlyBudgetBefore": 300000,
      "monthlyBudgetAfter": 280500
    },
    "createdAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Note:** 
- 장바구니는 자동으로 비워짐
- 지출 내역에 자동 등록
- 예산 자동 차감

---

## 14. 지도 및 위치 API

### 13.1 주소 검색 (Geocoding)

**Endpoint:** `GET /api/v1/maps/search-address?keyword={keyword}&limit=10`

**Query Parameters:**
- `keyword`: 검색 키워드 (도로명, 지번 등)
- `limit` (optional): 결과 개수 (기본값: 10)

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "addresses": [
      {
        "roadAddress": "서울특별시 강남구 테헤란로 123",
        "jibunAddress": "서울특별시 강남구 역삼동 456-78",
        "latitude": 37.497942,
        "longitude": 127.027621,
        "buildingName": "테헤란빌딩",
        "sigunguCode": "11680",
        "bcode": "1168010100"
      }
    ],
    "totalCount": 1
  },
  "error": null
}
```

**Note:** 네이버 지도 API Geocoding 래핑

---

### 13.2 좌표 → 주소 변환 (Reverse Geocoding)

**Endpoint:** `GET /api/v1/maps/reverse-geocode?lat={latitude}&lng={longitude}`

**설명:** 
- GPS 좌표를 주소 정보로 변환합니다.
- 주소 등록 시 현재 위치로 찾기 기능에서 사용됩니다.

**Query Parameters:**
- `lat`: 위도 (필수)
- `lng`: 경도 (필수)

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "roadAddress": "서울특별시 강남구 테헤란로 123",
    "jibunAddress": "서울특별시 강남구 역삼동 456-78",
    "latitude": 37.497942,
    "longitude": 127.027621,
    "sido": "서울특별시",
    "sigungu": "강남구",
    "dong": "역삼동",
    "buildingName": "테헤란빌딩",
    "sigunguCode": "11680",
    "bcode": "1168010100"
  },
  "error": null
}
```

**Error Cases:**

*400 Bad Request - 잘못된 좌표:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "E400",
    "message": "유효하지 않은 좌표입니다.",
    "data": {
      "latitude": "위도는 -90 ~ 90 범위여야 합니다.",
      "longitude": "경도는 -180 ~ 180 범위여야 합니다."
    }
  }
}
```

*503 Service Unavailable - 외부 API 오류:*
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "EXTERNAL_001",
    "message": "주소 변환 서비스에 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
    "data": null
  }
}
```

**Note:** 
- 네이버 지도 API Reverse Geocoding 래핑
- 주소 등록 프로세스에서 사용 (REQ-ONBOARD-203b)

---

### 13.3 GPS 기반 주소 등록 프로세스

**설명:** GPS 좌표를 이용한 주소 등록은 다음과 같은 클라이언트-서버 협력 프로세스로 진행됩니다.

**프로세스 흐름:**

1. **[Client]** 사용자가 '현재 위치로 찾기' 버튼 클릭
2. **[Client]** 기기 OS로부터 GPS 좌표(latitude, longitude) 획득
3. **[Client]** 획득한 좌표를 중심으로 지도 표시, 마커(핀) 표시
4. **[Client]** 사용자가 마커를 드래그하여 위치 미세 조정 가능
5. **[Client]** 위치 확정 후, `GET /api/v1/maps/reverse-geocode` API 호출
6. **[Server]** 네이버 지도 API를 통해 좌표 → 주소 변환
7. **[Server]** 구조화된 주소 데이터 응답
8. **[Client]** 응답받은 주소를 입력 필드에 자동 채움
9. **[Client]** 사용자가 상세 주소(동/호수) 직접 입력
10. **[Client]** 완성된 주소 정보로 `POST /api/v1/onboarding/address` 또는 `POST /api/v1/members/me/addresses` API 호출
11. **[Server]** 주소 저장 및 응답

**관련 API:**
- Step 5: `GET /api/v1/maps/reverse-geocode` (좌표 → 주소 변환)
- Step 10: `POST /api/v1/onboarding/address` (온보딩) 또는 `POST /api/v1/members/me/addresses` (일반)

**클라이언트 구현 고려사항:**
- GPS 권한 확인 및 요청
- 위치 정보 취득 실패 시 에러 처리
- 네트워크 오류 시 재시도 로직
- 지도 라이브러리 마커 드래그 이벤트 처리

---

### 13.4 현재 위치 기준 변경 (Deprecated)

**Status:** ⚠️ DEPRECATED - 이 API는 더 이상 사용되지 않습니다.

**사유:** 
- 홈 대시보드는 항상 기본 주소(primary address)를 기준으로 동작합니다.
- 위치 기준 변경이 필요한 경우, 기본 주소 설정(`PUT /api/v1/members/me/addresses/{addressHistoryId}/primary`)을 사용하세요.

**대체 API:**
- `PUT /api/v1/members/me/addresses/{addressHistoryId}/primary` (10.7 기본 주소 설정)

---

## 15. 알림 및 설정 API

### 14.1 알림 설정 조회

**Endpoint:** `GET /api/v1/members/me/notification-settings`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "pushEnabled": true,
    "storeNoticeEnabled": true,
    "recommendationEnabled": true,
    "budgetAlertEnabled": true,
    "passwordExpiryAlertEnabled": true
  },
  "error": null
}
```

---

### 14.2 알림 설정 변경

**Endpoint:** `PUT /api/v1/members/me/notification-settings`

**Request:**
```json
{
  "pushEnabled": false,
  "storeNoticeEnabled": false,
  "recommendationEnabled": false,
  "budgetAlertEnabled": true,
  "passwordExpiryAlertEnabled": true
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "pushEnabled": false,
    "storeNoticeEnabled": false,
    "recommendationEnabled": false,
    "budgetAlertEnabled": true,
    "passwordExpiryAlertEnabled": true,
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

**Note:**
- `pushEnabled: false` 시 하위 알림 자동 비활성화
- `pushEnabled: true` 시 이전 상태로 복구

---

### 14.3 앱 설정 조회

**Endpoint:** `GET /api/v1/settings/app`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "privacyPolicyUrl": "https://smartmealtable.com/policies/privacy",
    "termsOfServiceUrl": "https://smartmealtable.com/policies/terms",
    "contactEmail": "support@smartmealtable.com",
    "appVersion": "1.0.0",
    "minSupportedVersion": "1.0.0"
  },
  "error": null
}
```

---

### 14.4 사용자 추적 설정 변경

**Endpoint:** `PUT /api/v1/settings/app/tracking`

**Request:**
```json
{
  "allowTracking": false
}
```

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "allowTracking": false,
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

## 부록

### A. 에러 코드 목록

| 에러 코드 | HTTP Status | 설명 |
|-----------|-------------|------|
| `AUTH_001` | 401 | 인증 실패 (잘못된 토큰) |
| `AUTH_002` | 401 | 토큰 만료 |
| `AUTH_003` | 401 | 이메일 또는 비밀번호 불일치 |
| `AUTH_004` | 403 | 계정 잠김 (로그인 5회 실패) |
| `AUTH_005` | 409 | 이미 사용 중인 이메일 |
| `VALID_001` | 422 | 유효성 검증 실패 (비밀번호 형식) |
| `VALID_002` | 422 | 유효성 검증 실패 (이메일 형식) |
| `VALID_003` | 422 | 필수 약관 미동의 |
| `RESOURCE_001` | 404 | 리소스를 찾을 수 없음 |
| `CONFLICT_001` | 409 | 중복된 리소스 |
| `BUDGET_001` | 422 | 예산은 0 이상이어야 함 |
| `ADDRESS_001` | 422 | 주소 검증 실패 |
| `FAVORITE_001` | 409 | 이미 즐겨찾기에 추가됨 |
| `ADDRESS_002` | 404 | 등록된 주소가 없음 |
| `SERVER_001` | 500 | 내부 서버 오류 |
| `EXTERNAL_001` | 503 | 외부 API 호출 실패 |

---

### B. Enum 값 정리

#### RecommendationType
- `SAVER`: 절약형
- `ADVENTURER`: 모험형
- `BALANCED`: 균형형

#### PreferenceWeight
- `100`: 좋아요
- `0`: 보통
- `-100`: 싫어요

#### MealType
- `BREAKFAST`: 아침
- `LUNCH`: 점심
- `DINNER`: 저녁
- `OTHER`: 기타

#### GroupType
- `UNIVERSITY`: 대학교
- `COMPANY`: 회사
- `OTHER`: 기타

#### AddressType
- `HOME`: 집
- `WORK`: 직장
- `ETC`: 기타

#### DayOfWeek
- `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

#### SortBy (가게 정렬)
- `recommendation`: 추천 점수 순 (기본값)
- `distance`: 거리 순
- `reviewCount`: 리뷰 많은 순
- `priceAsc`: 가격 낮은 순
- `priceDesc`: 가격 높은 순
- `favoriteCount`: 즐겨찾기 많은 순
- `viewCount`: 조회수 높은 순
- `viewCountAsc`: 조회수 낮은 순

---

### C. 페이징 및 정렬 규칙

**기본 페이징:**
- `page`: 0부터 시작
- `size`: 기본값 20, 최대 100

**정렬 파라미터 형식:**
```
?sort=createdAt,desc&sort=name,asc
```

---

### D. 응답 시간 목표

| API 유형 | 목표 응답 시간 |
|----------|----------------|
| 일반 CRUD | < 500ms |
| 복잡한 조회 (지출 통계) | < 2초 |
| 추천 알고리즘 | < 2초 |
| SMS 파싱 (AI) | < 3초 |

---

**문서 종료**
