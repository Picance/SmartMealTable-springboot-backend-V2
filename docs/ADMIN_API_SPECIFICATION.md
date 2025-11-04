# 📋 API 명세서: SmartMealTable 관리자(ADMIN) 시스템

**버전**: v1.0
**작성일**: 2025-11-05
**대상 모듈**: `smartmealtable-admin`

---

## 1. 개요

이 문서는 SmartMealTable 관리자 시스템의 RESTful API 명세를 정의합니다. 관리자는 이 API를 통해 서비스의 핵심 데이터(카테고리, 음식점, 메뉴, 그룹, 약관 등)를 관리하고, 운영에 필요한 통계 데이터를 조회할 수 있습니다.

---

## 2. 공통 사항

### 2.1. 기본 URI

모든 관리자 API의 기본 URI는 다음과 같습니다.

```
/api/v1/admin
```

### 2.2. 인증

- 초기 버전에서는 별도의 인증 절차를 생략합니다.
- 향후 JWT 기반의 인증을 도입할 예정입니다.

### 2.3. 응답 형식

모든 API 응답은 `smartmealtable-core` 모듈의 `ApiResponse<T>` 래퍼 객체를 사용합니다.

#### 성공 응답

```json
{
  "result": "SUCCESS",
  "data": {
    // 요청에 대한 결과 데이터
  },
  "error": null
}
```

#### 실패 응답

```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "data": {
      // 에러 관련 추가 정보 (선택적)
    }
  }
}
```

### 2.4. 데이터 삭제 정책

- **논리적 삭제 (Soft Delete)**: `deleted_at` 필드가 있는 엔티티 (`store`, `expenditure` 등)
- **물리적 삭제 (Hard Delete)**: `deleted_at` 필드가 없는 마스터 데이터 (`category`, `policy`, `member_group` 등)

---

## 3. API 명세

### 3.1. 카테고리 관리 (Category)

- **Resource URI**: `/categories`
- **관련 요구사항**: `[REQ-ADMIN-CAT-001]` ~ `[REQ-ADMIN-CAT-005]`

| HTTP Method | URI | 설명 |
|-------------|--------------------------------|--------------------------|
| `GET`       | `/categories`                  | 카테고리 목록 조회 (페이징) |
| `GET`       | `/categories/{categoryId}`     | 카테고리 상세 조회       |
| `POST`      | `/categories`                  | 카테고리 생성            |
| `PUT`       | `/categories/{categoryId}`     | 카테고리 수정            |
| `DELETE`    | `/categories/{categoryId}`     | 카테고리 삭제 (물리적)   |

#### `GET /categories`

- **설명**: 카테고리 목록을 이름 검색과 함께 페이지네이션으로 조회합니다.
- **Query Parameters**:
  - `page` (number, optional, default: 0): 페이지 번호
  - `size` (number, optional, default: 20): 페이지 크기
  - `name` (string, optional): 검색할 카테고리 이름

#### `POST /categories`

- **설명**: 새로운 카테고리를 생성합니다.
- **Request Body**:
  ```json
  {
    "name": "한식"
  }
  ```

---

### 3.2. 음식점 관리 (Store)

- **Resource URI**: `/stores`
- **관련 요구사항**: `[REQ-ADMIN-STR-001]` ~ `[REQ-ADMIN-STR-007]`

| HTTP Method | URI | 설명 |
|-------------|------------------------------------------------|--------------------------|
| `GET`       | `/stores`                                      | 음식점 목록 조회 (페이징) |
| `GET`       | `/stores/{storeId}`                            | 음식점 상세 조회         |
| `POST`      | `/stores`                                      | 음식점 생성              |
| `PUT`       | `/stores/{storeId}`                            | 음식점 수정              |
| `DELETE`    | `/stores/{storeId}`                            | 음식점 삭제 (논리적)     |
| `POST`      | `/stores/{storeId}/opening-hours`              | 영업시간 추가            |
| `PUT`       | `/stores/{storeId}/opening-hours/{openingHourId}`| 영업시간 수정            |
| `DELETE`    | `/stores/{storeId}/opening-hours/{openingHourId}`| 영업시간 삭제            |
| `POST`      | `/stores/{storeId}/temporary-closure`          | 임시 휴무 등록           |
| `DELETE`    | `/stores/{storeId}/temporary-closure/{closureId}`| 임시 휴무 삭제           |

#### `GET /stores`

- **설명**: 음식점 목록을 필터링 및 페이지네이션으로 조회합니다.
- **Query Parameters**:
  - `page`, `size` (number, optional)
  - `categoryId` (number, optional)
  - `name` (string, optional)
  - `storeType` (string, optional, e.g., `CAMPUS_RESTAURANT`, `RESTAURANT`)

#### `POST /stores`

- **설명**: 새로운 음식점을 생성합니다.
- **Request Body**:
  ```json
  {
    "name": "스마트 식당",
    "categoryId": 1,
    "sellerId": null,
    "address": "서울시 강남구 테헤란로 123",
    "lotNumberAddress": "서울시 강남구 역삼동 123-45",
    "latitude": 37.12345,
    "longitude": 127.12345,
    "phoneNumber": "02-1234-5678",
    "description": "맛있는 집",
    "averagePrice": 15000,
    "storeType": "CAMPUS_RESTAURANT",
    "imageUrl": "http://example.com/image.jpg"
  }
  ```
- **참고**: 
  - `sellerId`: 판매자 ID (선택 필드, 판매자 관리 기능 구현 후 사용)
  - `storeType`은 `CAMPUS_RESTAURANT` 또는 `RESTAURANT` 값 사용
  - `registeredAt`은 서버에서 자동 설정 (비즈니스 필드)
  - `reviewCount`, `viewCount`, `favoriteCount`는 기본값 0으로 자동 설정

#### `POST /stores/{storeId}/opening-hours`

- **설명**: 특정 음식점의 영업시간을 추가합니다.
- **Request Body**:
  ```json
  {
    "dayOfWeek": "MONDAY",
    "openTime": "09:00:00",
    "closeTime": "21:00:00",
    "breakStartTime": "15:00:00",
    "breakEndTime": "17:00:00",
    "isHoliday": false
  }
  ```
- **참고**: 
  - `dayOfWeek`: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
  - 휴무일인 경우: `isHoliday: true`, `openTime`, `closeTime`은 null
  - 브레이크 타임이 없는 경우: `breakStartTime`, `breakEndTime`은 null

#### `POST /stores/{storeId}/temporary-closure`

- **설명**: 특정 음식점의 임시 휴무를 등록합니다.
- **Request Body**:
  ```json
  {
    "closureDate": "2025-12-25",
    "startTime": "12:00",
    "endTime": "18:00",
    "reason": "크리스마스 휴무"
  }
  ```
- **참고**: 
  - 종일 휴무인 경우: `startTime`, `endTime`은 null
  - `registeredAt`은 DB에서 자동 설정됩니다 (비즈니스 필드, 최근 휴업 알림용)
  - **주의**: 도메인 엔티티 `StoreTemporaryClosure`는 record 타입으로 `registeredAt` 필드를 포함하지 않습니다. 조회가 필요한 경우 Storage 계층 처리를 고려하세요.

---

### 3.3. 메뉴 관리 (Food/Menu)

- **Resource URI**: `/foods`, `/stores/{storeId}/foods`
- **관련 요구사항**: `[REQ-ADMIN-FOOD-001]` ~ `[REQ-ADMIN-FOOD-004]`

| HTTP Method | URI | 설명 |
|-------------|--------------------------------|--------------------|
| `GET`       | `/stores/{storeId}/foods`      | 특정 음식점의 메뉴 목록 조회 |
| `POST`      | `/stores/{storeId}/foods`      | 메뉴 생성          |
| `PUT`       | `/foods/{foodId}`              | 메뉴 수정          |
| `DELETE`    | `/foods/{foodId}`              | 메뉴 삭제 (물리적) |

#### `POST /stores/{storeId}/foods`

- **설명**: 특정 음식점에 새로운 메뉴를 추가합니다.
- **Request Body**:
  ```json
  {
    "foodName": "김치찌개",
    "averagePrice": 8000,
    "description": "국내산 김치로 만들었습니다.",
    "imageUrl": "http://example.com/kimchi.jpg",
    "categoryId": 1
  }
  ```
- **참고**: 
  - 도메인 엔티티 `Food`는 `averagePrice` 필드를 사용하며, DB 테이블 `food`의 `price` 칼럼과 매핑됩니다.
  - Storage 계층에서 `entity.price = food.getAveragePrice()` 방식으로 변환됩니다.
  - `registered_dt`는 DB에서 자동 설정됩니다 (비즈니스 필드, 신메뉴 표시용)
  - **주의**: 현재 도메인 엔티티 `Food`에는 `registeredDt` 필드가 없으므로, 신메뉴 정렬 등의 기능 구현 시 도메인 엔티티에 필드 추가가 필요할 수 있습니다.

---

### 3.4. 그룹 관리 (Group)

- **Resource URI**: `/groups`
- **관련 요구사항**: `[REQ-ADMIN-GRP-001]` ~ `[REQ-ADMIN-GRP-005]`

| HTTP Method | URI | 설명 |
|-------------|--------------------------|--------------------------|
| `GET`       | `/groups`                | 그룹 목록 조회 (페이징)   |
| `GET`       | `/groups/{groupId}`      | 그룹 상세 조회           |
| `POST`      | `/groups`                | 그룹 생성                |
| `PUT`       | `/groups/{groupId}`      | 그룹 수정                |
| `DELETE`    | `/groups/{groupId}`      | 그룹 삭제 (물리적)       |

#### `POST /groups`

- **설명**: 새로운 그룹(학교/회사)을 생성합니다.
- **Request Body**:
  ```json
  {
    "name": "스마트 대학교",
    "type": "UNIVERSITY",
    "address": "서울시 관악구 신림동"
  }
  ```
- **참고**: 
  - `type`은 `UNIVERSITY`, `COMPANY`, `OTHER` 값 사용

---

### 3.5. 약관 관리 (Policy)

- **Resource URI**: `/policies`
- **관련 요구사항**: `[REQ-ADMIN-POL-001]` ~ `[REQ-ADMIN-POL-006]`

| HTTP Method | URI | 설명 |
|-------------|--------------------------------|--------------------------|
| `GET`       | `/policies`                    | 약관 목록 조회 (페이징)   |
| `GET`       | `/policies/{policyId}`         | 약관 상세 조회           |
| `POST`      | `/policies`                    | 약관 생성                |
| `PUT`       | `/policies/{policyId}`         | 약관 수정                |
| `DELETE`    | `/policies/{policyId}`         | 약관 삭제 (물리적)       |
| `PATCH`     | `/policies/{policyId}/toggle`  | 약관 활성/비활성 토글    |

#### `POST /policies`

- **설명**: 새로운 버전의 약관을 생성합니다.
- **Request Body**:
  ```json
  {
    "title": "개인정보 처리방침",
    "content": "...",
    "version": "1.1",
    "type": "REQUIRED",
    "isMandatory": true
  }
  ```
- **참고**: 
  - `type`은 `REQUIRED` 또는 `OPTIONAL` 값 사용
  - `isActive`는 서버에서 기본값 `true`로 자동 설정

---

### 3.6. 통계 조회 (Statistics)

- **Resource URI**: `/statistics`
- **관련 요구사항**: `[PRD 4.5]`

| HTTP Method | URI | 설명 |
|-------------|--------------------------|--------------------|
| `GET`       | `/statistics/users`      | 사용자 통계 조회   |
| `GET`       | `/statistics/expenditures`| 지출 통계 조회     |
| `GET`       | `/statistics/stores`     | 음식점 통계 조회   |

**참고**: 각 통계 API의 구체적인 응답 데이터 구조는 추후 정의합니다.
