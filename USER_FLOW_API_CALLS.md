# SmartMealTable 사용자 플로우별 API 호출 순서

이 문서는 SmartMealTable 서비스의 주요 사용자 플로우에 따른 API 호출 순서를 정의합니다. 각 플로우는 사용자가 서비스를 이용하는 과정에서 발생하는 일련의 API 상호작용을 보여줍니다.

**Base URL:** `/api/v1`

## 목차

1.  [신규 사용자 회원가입 및 온보딩](#1-신규-사용자-회원가입-및-온보딩)
    -   [1.1 이메일 회원가입 및 온보딩](#11-이메일-회원가입-및-온보딩)
    -   [1.2 소셜 회원가입 및 온보딩](#12-소셜-회원가입-및-온보딩)
2.  [기존 사용자 로그인](#2-기존-사용자-로그인)
3.  [지출 내역 기록](#3-지출-내역-기록)
    -   [3.1 SMS 파싱을 통한 기록](#31-sms-파싱을-통한-기록)
    -   [3.2 수동 기록](#32-수동-기록)
4.  [홈 화면 조회](#4-홈-화면-조회)
5.  [예산 관리](#5-예산-관리)
6.  [프로필 및 설정 변경](#6-프로필-및-설정-변경)
7.  [가게/음식 검색 및 즐겨찾기](#7-가게음식-검색-및-즐겨찾기)

---

## 1. 신규 사용자 회원가입 및 온보딩

신규 사용자가 계정을 생성하고 서비스 사용에 필요한 초기 설정을 완료하는 과정입니다.

### 1.1 이메일 회원가입 및 온보딩

1.  **이메일 중복 확인 (선택)**
    -   `GET /auth/check-email?email={email}`
    -   사용자가 입력한 이메일이 사용 가능한지 확인합니다.
    -   **Example Request:**
        ```http
        GET /api/v1/auth/check-email?email=user@example.com
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "isAvailable": true
          },
          "error": null
        }
        ```

2.  **회원가입 요청**
    -   `POST /auth/signup/email`
    -   이름, 이메일, 비밀번호로 회원가입을 완료하고 Access/Refresh Token을 발급받습니다.
    -   **Example Request:**
        ```http
        POST /api/v1/auth/signup/email
        Content-Type: application/json

        {
          "name": "홍길동",
          "email": "user@example.com",
          "password": "SecureP@ss123!"
        }
        ```
    -   **Example Response (201 Created):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "expiresIn": 7200
          },
          "error": null
        }
        ```

3.  **약관 목록 조회 및 동의**
    -   `GET /policies`
    -   온보딩에 필요한 약관 목록을 가져옵니다.
    -   **Example Request:**
        ```http
        GET /api/v1/policies
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": [
            { "policyId": 1, "title": "서비스 이용약관", "isRequired": true },
            { "policyId": 2, "title": "개인정보 처리방침", "isRequired": true },
            { "policyId": 3, "title": "마케팅 정보 수신 동의", "isRequired": false }
          ],
          "error": null
        }
        ```
    -   `POST /onboarding/policy-agreements`
    -   필수 및 선택 약관에 동의합니다.
    -   **Example Request:**
        ```http
        POST /api/v1/onboarding/policy-agreements
        Authorization: Bearer {access_token}
        Content-Type: application/json

        {
          "agreements": [
            { "policyId": 1, "isAgreed": true },
            { "policyId": 2, "isAgreed": true },
            { "policyId": 3, "isAgreed": false }
          ]
        }
        ```
    -   **Example Response (201 Created):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "agreedCount": 2
          },
          "error": null
        }
        ```

4.  **프로필(닉네임, 소속) 설정**
    -   `GET /groups?type={type}&name={name}`
    -   소속(그룹) 목록을 검색하고 조회합니다.
    -   **Example Request:**
        ```http
        GET /api/v1/groups?name=대학교
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "content": [
              { "groupId": 123, "name": "스마트대학교", "type": "UNIVERSITY" }
            ],
            "pageable": { /* ... */ }
          },
          "error": null
        }
        ```
    -   `POST /onboarding/profile`
    -   사용할 닉네임과 선택한 소속 ID를 서버에 저장합니다.
    -   **Example Request:**
        ```http
        POST /api/v1/onboarding/profile
        Authorization: Bearer {access_token}
        Content-Type: application/json

        {
          "nickname": "길동이",
          "groupId": 123
        }
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "memberId": 1,
            "nickname": "길동이",
            "groupName": "스마트대학교"
          },
          "error": null
        }
        ```

5.  **주소 등록**
    -   `POST /onboarding/address`
    -   주소 별칭, 주소, 위도/경도 등 주소 정보를 등록합니다. (기본 주소 설정 포함)
    -   **Example Request:**
        ```http
        POST /api/v1/onboarding/address
        Authorization: Bearer {access_token}
        Content-Type: application/json

        {
          "addressAlias": "우리집",
          "address": "서울특별시 강남구 테헤란로 212",
          "latitude": 37.5017,
          "longitude": 127.0396,
          "isPrimary": true
        }
        ```
    -   **Example Response (201 Created):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "addressId": 1,
            "addressAlias": "우리집",
            "isPrimary": true
          },
          "error": null
        }
        ```

6.  **초기 예산 설정**
    -   `POST /onboarding/budget`
    -   월별 총 예산 및 식사별(아침/점심/저녁) 예산을 설정합니다.
    -   **Example Request:**
        ```http
        POST /api/v1/onboarding/budget
        Authorization: Bearer {access_token}
        Content-Type: application/json

        {
          "monthlyBudget": 500000,
          "mealBudgets": {
            "breakfast": 5000,
            "lunch": 10000,
            "dinner": 10000
          }
        }
        ```
    -   **Example Response (201 Created):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "monthlyBudgetId": 1,
            "monthlyBudget": 500000,
            "dailyAverageBudget": 16666
          },
          "error": null
        }
        ```

7.  **음식 취향 설정**
    -   `GET /categories`
    -   선호도 조사를 위한 음식 카테고리 목록을 가져옵니다.
    -   `POST /onboarding/preferences`
    -   카테고리별 선호도(좋아요/싫어요)를 저장합니다.
    -   `GET /onboarding/foods?categoryId={categoryId}`
    -   이미지 그리드에 표시할 개별 음식 목록을 조회합니다.
    -   `POST /onboarding/food-preferences`
    -   선택한 개별 선호 음식 ID 목록을 저장합니다.
    -   **Example Flow:**
        1.  `GET /api/v1/categories` -> 카테고리 목록(한식, 중식, 일식...) 획득
        2.  `POST /api/v1/onboarding/preferences` (Request: `{ "recommendationType": "BALANCED", "categoryPreferences": [{ "categoryId": 1, "weight": 100 }, { "categoryId": 2, "weight": -100 }] }`)
        3.  `GET /api/v1/onboarding/foods?size=20` -> 음식 이미지 그리드 표시
        4.  `POST /api/v1/onboarding/food-preferences` (Request: `{ "preferredFoodIds": [1, 5, 12] }`)

### 1.2 소셜 회원가입 및 온보딩

1.  **소셜 로그인 요청 (카카오/구글)**
    -   `POST /auth/login/kakao` 또는 `POST /auth/login/google`
    -   Provider로부터 받은 인증 코드를 전송하여 로그인/회원가입을 처리합니다.
    -   응답의 `isNewMember: true` 값을 통해 신규 가입자임을 확인하고 온보딩을 시작합니다.
    -   **Example Request (Kakao):**
        ```http
        POST /api/v1/auth/login/kakao
        Content-Type: application/json

        {
          "authorizationCode": "asdf1234qwer5678zxcv"
        }
        ```
    -   **Example Response (New Member):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "isNewMember": true,
            "isOnboardingComplete": false,
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          },
          "error": null
        }
        ```

2.  **온보딩 절차 수행**
    -   이후 과정은 [1.1 이메일 회원가입 및 온보딩](#11-이메일-회원가입-및-온보딩)의 **3~7번**과 동일합니다.

---

## 2. 기존 사용자 로그인

1.  **로그인**
    -   **이메일**: `POST /auth/login/email`
    -   **소셜**: `POST /auth/login/kakao` 또는 `POST /auth/login/google`
    -   로그인 성공 시 Access/Refresh Token을 발급받습니다.
    -   **Example Request (Email):**
        ```http
        POST /api/v1/auth/login/email
        Content-Type: application/json

        {
          "email": "user@example.com",
          "password": "SecureP@ss123!"
        }
        ```
    -   **Example Response (Existing Member):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "isNewMember": false,
            "isOnboardingComplete": true,
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          },
          "error": null
        }
        ```

2.  **토큰 갱신 (필요 시)**
    -   `POST /auth/refresh`
    -   Access Token이 만료되었을 경우, Refresh Token을 사용하여 새로운 토큰을 발급받습니다.
    -   **Example Request:**
        ```http
        POST /api/v1/auth/refresh
        Content-Type: application/json

        {
          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        }
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (new)",
            "expiresIn": 7200
          },
          "error": null
        }
        ```

---

## 3. 지출 내역 기록

### 3.1 SMS 파싱을 통한 기록

1.  **SMS 내용 파싱**
    -   `POST /expenditures/parse-sms`
    -   카드사 승인 SMS 내용을 전송하여 지출 정보를 파싱합니다.
    -   **Example Request:**
        ```http
        POST /api/v1/expenditures/parse-sms
        Authorization: Bearer {access_token}
        Content-Type: application/json

        {
          "smsContent": "[국민카드] 10/08 12:30 맘스터치강남점 13,500원 일시불 승인"
        }
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "storeName": "맘스터치강남점",
            "amount": 13500,
            "transactionDate": "2025-10-08T12:30:00"
          },
          "error": null
        }
        ```

2.  **파싱된 정보로 지출 등록**
    -   `POST /expenditures`
    -   파싱된 가게 이름, 금액, 거래 시간 등의 정보로 지출 내역을 등록합니다. 사용자는 식사 유형, 메모 등을 추가로 입력할 수 있습니다.
    -   **Example Request:**
        ```http
        POST /api/v1/expenditures
        Authorization: Bearer {access_token}
        Content-Type: application/json

        {
          "storeName": "맘스터치강남점",
          "amount": 13500,
          "transactionDate": "2025-10-08T12:30:00",
          "mealType": "LUNCH",
          "memo": "동료와 함께",
          "items": [
            { "itemName": "싸이버거 세트", "price": 6900, "quantity": 1 },
            { "itemName": "그릴드비프버거 세트", "price": 6600, "quantity": 1 }
          ]
        }
        ```
    -   **Example Response (201 Created):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "expenditureId": 101
          },
          "error": null
        }
        ```

### 3.2 수동 기록

1.  **지출 내역 등록**
    -   `POST /expenditures`
    -   사용자가 직접 가게 이름, 금액, 식사 유형, 품목 등 모든 정보를 입력하여 지출 내역을 등록합니다.
    -   (요청/응답 예시는 3.1의 2번과 동일)

---

## 4. 홈 화면 조회

앱 실행 시 가장 먼저 보게 되는 홈 화면을 구성하기 위한 API 호출 흐름입니다. (API 호출은 병렬로 수행될 수 있습니다)

1.  **일별 예산 및 지출 현황 조회**
    -   `GET /budgets/daily?date={today}`
    -   오늘 날짜의 예산(총액, 식사별), 현재까지의 지출, 남은 예산을 조회합니다.
    -   **Example Request:**
        ```http
        GET /api/v1/budgets/daily?date=2025-10-14
        Authorization: Bearer {access_token}
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "date": "2025-10-14",
            "totalBudget": 25000,
            "totalSpent": 8500,
            "remainingBudget": 16500,
            "mealBudgets": { /* ... */ }
          },
          "error": null
        }
        ```

2.  **오늘의 추천 조회**
    -   `GET /recommendations/today`
    -   사용자의 위치, 예산, 취향을 기반으로 한 오늘의 추천 가게/음식 목록을 조회합니다.
    -   **Example Request:**
        ```http
        GET /api/v1/recommendations/today?mealType=LUNCH
        Authorization: Bearer {access_token}
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "recommendationType": "BUDGET_SAVING",
            "stores": [
              { "storeId": 205, "storeName": "가성비맛집", "distance": "250m", "averagePrice": 8000 },
              { "storeId": 311, "storeName": "든든한백반", "distance": "400m", "averagePrice": 9000 }
            ]
          },
          "error": null
        }
        ```

3.  **최근 지출 내역 조회 (선택)**
    -   `GET /expenditures?size=5`
    -   홈 화면에 표시할 최근 지출 내역 5건을 조회합니다.
    -   **Example Request:**
        ```http
        GET /api/v1/expenditures?page=0&size=5&sort=transactionDate,desc
        Authorization: Bearer {access_token}
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "content": [
              { "expenditureId": 101, "storeName": "맘스터치강남점", "amount": 13500, "transactionDate": "2025-10-14T12:30:00" },
              { "expenditureId": 100, "storeName": "스타벅스", "amount": 4500, "transactionDate": "2025-10-13T08:30:00" }
            ],
            "pageable": { /* ... */ }
          },
          "error": null
        }
        ```

---

## 5. 예산 관리

1.  **월별 예산 조회**
    -   `GET /budgets/monthly?year={year}&month={month}`
    -   선택한 연/월의 예산 및 지출 통계를 조회합니다.
    -   **Example Request:**
        ```http
        GET /api/v1/budgets/monthly?year=2025&month=10
        Authorization: Bearer {access_token}
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "year": 2025,
            "month": 10,
            "totalBudget": 500000,
            "totalSpent": 250000,
            "remainingBudget": 250000
          },
          "error": null
        }
        ```

2.  **예산 수정**
    -   `PUT /budgets`
    -   월별 총 예산 및 식사별 예산을 수정합니다.
    -   **Example Request:**
        ```http
        PUT /api/v1/budgets
        Authorization: Bearer {access_token}
        Content-Type: application/json

        {
          "monthlyBudget": 600000,
          "mealBudgets": {
            "breakfast": 7000,
            "lunch": 12000,
            "dinner": 12000
          }
        }
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "monthlyBudget": 600000,
            "dailyAverageBudget": 20000
          },
          "error": null
        }
        ```

3.  **특정 날짜 예산 수정**
    -   `PUT /budgets/daily/{date}`
    -   특정 날짜의 예산을 개별적으로 수정하거나, 해당 날짜 이후의 모든 날짜에 일괄 적용(`applyForward: true`)합니다.
    -   **Example Request:**
        ```http
        PUT /api/v1/budgets/daily/2025-10-15
        Authorization: Bearer {access_token}
        Content-Type: application/json

        {
          "dailyBudget": 30000,
          "mealBudgets": {
            "breakfast": 8000,
            "lunch": 12000,
            "dinner": 10000
          },
          "applyForward": true
        }
        ```
    -   **Example Response (200 OK):**
        ```json
        {
          "result": "SUCCESS",
          "data": {
            "targetDate": "2025-10-15",
            "affectedDatesCount": 78
          },
          "error": null
        }
        ```

---

## 6. 프로필 및 설정 변경

1.  **내 정보 조회**
    -   `GET /members/me`
    -   현재 로그인된 사용자의 프로필 정보(이름, 닉네임, 이메일 등)를 조회합니다.

2.  **프로필 수정**
    -   `PUT /members/me`
    -   닉네임, 프로필 이미지 등을 수정합니다.

3.  **비밀번호 변경**
    -   `PUT /members/me/password`
    -   현재 비밀번호와 새 비밀번호를 입력하여 비밀번호를 변경합니다.

4.  **연동된 소셜 계정 관리**
    -   `GET /members/me/social-accounts` (조회)
    -   `POST /members/me/social-accounts` (추가 연동)
    -   `DELETE /members/me/social-accounts/{socialAccountId}` (연동 해제)

5.  **주소 관리**
    -   `GET /members/me/addresses` (목록 조회)
    -   `POST /members/me/addresses` (추가)
    -   `PUT /members/me/addresses/{addressId}` (수정)
    -   `DELETE /members/me/addresses/{addressId}` (삭제)

6.  **회원 탈퇴**
    -   `DELETE /members/me`
    -   비밀번호 확인 후 계정을 비활성화(Soft Delete) 처리합니다.

---

## 7. 가게/음식 검색 및 즐겨찾기

1.  **가게 검색**
    -   `GET /stores?query={query}&lat={lat}&lon={lon}`
    -   검색어와 현재 위치를 기반으로 가게를 검색합니다.

2.  **가게 상세 정보 조회**
    -   `GET /stores/{storeId}`
    -   특정 가게의 상세 정보(메뉴, 위치, 평점 등)를 조회합니다.

3.  **즐겨찾기 추가/삭제**
    -   `POST /favorites`
    -   특정 가게를 즐겨찾기에 추가합니다.
    -   `DELETE /favorites/{favoriteId}`
    -   즐겨찾기에서 가게를 삭제합니다.

4.  **즐겨찾기 목록 조회**
    -   `GET /favorites`
    -   사용자가 등록한 즐겨찾기 가게 목록을 조회합니다.

