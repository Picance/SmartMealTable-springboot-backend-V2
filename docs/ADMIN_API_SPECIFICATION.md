# 📋 API 명세서: SmartMealTable 관리자(ADMIN) 시스템

**버전**: v2.0.1
**작성일**: 2025-11-07 (최종 업데이트)
**대상 모듈**: `smartmealtable-admin`

**변경 이력**:
- v2.0.1 (2025-11-07): Food DELETE 상태코드 204 No Content 적용, API 경로 구조 정규화, 테스트 스위트 완전 통과
- v2.0 (2025-11-07): Food 엔티티에 `is_main`, `display_order` 추가, StoreImage 테이블 신규 추가, 지오코딩 자동 처리 반영
- v1.0 (2025-11-05): 초기 버전

---

## 1. 개요

이 문서는 SmartMealTable 관리자 시스템의 RESTful API 명세를 정의합니다. 관리자는 이 API를 통해 서비스의 핵심 데이터(카테고리, 음식점, 메뉴, 그룹, 약관 등)를 관리하고, 운영에 필요한 통계 데이터를 조회할 수 있습니다.

### 1.1. v2.0.1 주요 변경사항 (2025-11-07)

#### 🔧 개선사항
1. **Food API 경로 구조 정규화**
   - 변경 전: `POST /api/v1/admin/foods` (body에서 storeId 전달)
   - 변경 후: `POST /api/v1/admin/stores/{storeId}/foods` (계층적 경로)
   - 모든 Food API (GET, POST, PUT, DELETE)에 동일 규칙 적용
   - RESTful 설계 원칙 준수 및 가독성 향상

2. **Food DELETE 응답 상태 코드 개선**
   - 변경 전: `200 OK` (ApiResponse<Void> 반환)
   - 변경 후: `204 No Content` (응답 본문 없음)
   - RESTful 컨벤션 준수

3. **전체 테스트 스위트 완전 통과**
   - ADMIN 모듈: 88개 테스트 모두 성공
   - API 계약 변경에 따른 모든 통합 테스트 업데이트 완료
   - 코드-문서 불일치 완전 해소

### 1.2. v2.0 주요 변경사항 (2025-11-07)
1. **가게 이미지 다중 관리**
   - 기존: Store 테이블의 단일 `image_url` 필드
   - 변경: `store_image` 테이블로 다중 이미지 관리
   - 신규 API: `POST/PUT/DELETE /stores/{storeId}/images`
   - 대표 이미지 설정 기능 (`isMain`, `displayOrder`)

2. **메뉴 정렬 및 강조 기능**
   - 신규 필드: `is_main` (대표 메뉴 여부), `display_order` (표시 순서)
   - 메뉴 목록 조회 시 정렬 옵션 추가

3. **주소 기반 자동 지오코딩**
   - 가게 생성/수정 시 `latitude`, `longitude` 필드 제거
   - 서버에서 `address` 기반 지오코딩 API 자동 호출
   - 프론트엔드 부담 감소 및 데이터 정확성 향상

#### ❌ 제거된 필드
- **Store 생성/수정 API**:
  - `latitude`, `longitude`: 서버에서 자동 처리
  - `imageUrl`: 별도의 이미지 관리 API로 분리

#### ✅ 추가된 필드
- **Food (메뉴)**:
  - `isMain`: 대표 메뉴 여부
  - `displayOrder`: 표시 순서
- **StoreImage (가게 이미지)**:
  - `storeImageId`: 이미지 ID
  - `imageUrl`: 이미지 URL
  - `isMain`: 대표 이미지 여부
  - `displayOrder`: 표시 순서

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
| `GET`       | `/stores/{storeId}/images`                     | 가게 이미지 목록 조회    |
| `POST`      | `/stores/{storeId}/images`                     | 가게 이미지 추가         |
| `PUT`       | `/stores/{storeId}/images/{imageId}`           | 가게 이미지 수정         |
| `DELETE`    | `/stores/{storeId}/images/{imageId}`           | 가게 이미지 삭제         |
| `GET`       | `/stores/{storeId}/opening-hours`              | 영업시간 목록 조회       |
| `POST`      | `/stores/{storeId}/opening-hours`              | 영업시간 추가            |
| `PUT`       | `/stores/{storeId}/opening-hours/{openingHourId}`| 영업시간 수정            |
| `DELETE`    | `/stores/{storeId}/opening-hours/{openingHourId}`| 영업시간 삭제            |
| `GET`       | `/stores/{storeId}/temporary-closures`         | 임시 휴무 목록 조회      |
| `POST`      | `/stores/{storeId}/temporary-closures`         | 임시 휴무 등록           |
| `DELETE`    | `/stores/{storeId}/temporary-closures/{closureId}`| 임시 휴무 삭제           |

#### `GET /stores`

- **설명**: 음식점 목록을 필터링 및 페이지네이션으로 조회합니다.
- **Query Parameters**:
  - `page`, `size` (number, optional)
  - `categoryId` (number, optional)
  - `name` (string, optional)
  - `storeType` (string, optional, e.g., `CAMPUS_RESTAURANT`, `RESTAURANT`)

#### `GET /stores/{storeId}`

- **설명**: 특정 음식점의 상세 정보를 조회합니다 (이미지 목록 포함).
- **Response (200)**:
  ```json
  {
    "result": "SUCCESS",
    "data": {
      "storeId": 101,
      "name": "스마트 식당",
      "categoryId": 1,
      "categoryName": "한식",
      "sellerId": null,
      "address": "서울시 강남구 테헤란로 123",
      "lotNumberAddress": "서울시 강남구 역삼동 123-45",
      "latitude": 37.12345,
      "longitude": 127.12345,
      "phoneNumber": "02-1234-5678",
      "description": "맛있는 집",
      "averagePrice": 15000,
      "storeType": "CAMPUS_RESTAURANT",
      "registeredAt": "2025-01-15T09:00:00",
      "images": [
        {
          "storeImageId": 1,
          "imageUrl": "http://example.com/image1.jpg",
          "isMain": true,
          "displayOrder": 1
        },
        {
          "storeImageId": 2,
          "imageUrl": "http://example.com/image2.jpg",
          "isMain": false,
          "displayOrder": 2
        }
      ]
    },
    "error": null
  }
  ```

#### `POST /stores`

- **설명**: 새로운 음식점을 생성합니다.
- **변경사항 (v2.0)**:
  - ❌ **제거**: `latitude`, `longitude` 필드 (서버에서 주소 기반 지오코딩 자동 처리)
  - ❌ **제거**: `imageUrl` 필드 (별도의 이미지 관리 API 사용)
- **Request Body**:
  ```json
  {
    "name": "스마트 식당",
    "categoryId": 1,
    "sellerId": null,
    "address": "서울시 강남구 테헤란로 123",
    "lotNumberAddress": "서울시 강남구 역삼동 123-45",
    "phoneNumber": "02-1234-5678",
    "description": "맛있는 집",
    "averagePrice": 15000,
    "storeType": "CAMPUS_RESTAURANT"
  }
  ```
- **참고**: 
  - `sellerId`: 판매자 ID (선택 필드, 판매자 관리 기능 구현 후 사용)
  - `storeType`은 `CAMPUS_RESTAURANT` 또는 `RESTAURANT` 값 사용
  - `registeredAt`은 서버에서 자동 설정 (비즈니스 필드)
  - `latitude`, `longitude`는 서버에서 `address` 기반으로 지오코딩 API를 호출하여 자동 설정
  - `reviewCount`, `viewCount`, `favoriteCount`는 기본값 0으로 자동 설정
  - 가게 생성 후 별도로 이미지를 추가하려면 `POST /stores/{storeId}/images` 사용

#### `PUT /stores/{storeId}`

- **설명**: 음식점 정보를 수정합니다.
- **변경사항 (v2.0)**:
  - ❌ **제거**: `latitude`, `longitude` 필드 (서버에서 주소 기반 지오코딩 자동 처리)
  - ❌ **제거**: `imageUrl` 필드 (별도의 이미지 관리 API 사용)
- **Request Body**:
  ```json
  {
    "name": "스마트 식당 (수정)",
    "categoryId": 1,
    "address": "서울시 강남구 테헤란로 456",
    "lotNumberAddress": "서울시 강남구 역삼동 456-78",
    "phoneNumber": "02-5678-1234",
    "description": "더 맛있는 집",
    "averagePrice": 18000,
    "storeType": "RESTAURANT"
  }
  ```
- **참고**:
  - `address` 변경 시 서버에서 자동으로 `latitude`, `longitude` 재계산
  - 지오코딩 실패 시 400 Bad Request 응답 (주소가 유효하지 않음)

#### `POST /stores/{storeId}/images`

- **설명**: 특정 음식점에 이미지를 추가합니다.
- **Request Body**:
  ```json
  {
    "imageUrl": "http://example.com/store-image.jpg",
    "isMain": true,
    "displayOrder": 1
  }
  ```
- **참고**: 
  - `isMain`: 대표 이미지 여부 (기본값: false)
  - `displayOrder`: 표시 순서 (낮을수록 우선, null 허용)
  - 이미 `isMain=true`인 이미지가 있는 경우, 기존 대표 이미지의 `isMain`을 자동으로 false로 변경
  - **Response (201)**:
    ```json
    {
      "result": "SUCCESS",
      "data": {
        "storeImageId": 3,
        "storeId": 101,
        "imageUrl": "http://example.com/store-image.jpg",
        "isMain": true,
        "displayOrder": 1
      },
      "error": null
    }
    ```

#### `PUT /stores/{storeId}/images/{imageId}`

- **설명**: 가게 이미지 정보를 수정합니다.
- **Request Body**:
  ```json
  {
    "imageUrl": "http://example.com/updated-image.jpg",
    "isMain": false,
    "displayOrder": 2
  }
  ```
- **참고**: 
  - `isMain`을 true로 변경하면 기존 대표 이미지는 자동으로 false로 변경됨

#### `DELETE /stores/{storeId}/images/{imageId}`

- **설명**: 가게 이미지를 삭제합니다 (물리적 삭제).
- **Response (204)**: No Content

#### `GET /stores/{storeId}/opening-hours`

- **설명**: 특정 음식점의 영업시간 목록을 조회합니다.
- **Response (200)**:
  ```json
  {
    "result": "SUCCESS",
    "data": [
      {
        "openingHourId": 1,
        "storeId": 101,
        "dayOfWeek": "MONDAY",
        "openTime": "09:00:00",
        "closeTime": "21:00:00",
        "breakStartTime": "15:00:00",
        "breakEndTime": "17:00:00",
        "isHoliday": false
      },
      {
        "openingHourId": 2,
        "storeId": 101,
        "dayOfWeek": "SUNDAY",
        "openTime": null,
        "closeTime": null,
        "breakStartTime": null,
        "breakEndTime": null,
        "isHoliday": true
      }
    ],
    "error": null
  }
  ```
- **참고**:
  - 영업시간은 `dayOfWeek` 순서(MONDAY ~ SUNDAY)로 정렬되어 반환됩니다.
  - 휴무일(`isHoliday=true`)인 경우 `openTime`, `closeTime`은 null입니다.

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
  - 종일 휴무인 경우: `startTime`, `endTime`은 null
  - 브레이크 타임이 없는 경우: `breakStartTime`, `breakEndTime`은 null

#### `GET /stores/{storeId}/images`

- **설명**: 특정 음식점의 이미지 목록을 조회합니다.
- **Response (200)**:
  ```json
  {
    "result": "SUCCESS",
    "data": [
      {
        "storeImageId": 1,
        "storeId": 101,
        "imageUrl": "http://example.com/image1.jpg",
        "isMain": true,
        "displayOrder": 1
      },
      {
        "storeImageId": 2,
        "storeId": 101,
        "imageUrl": "http://example.com/image2.jpg",
        "isMain": false,
        "displayOrder": 2
      }
    ],
    "error": null
  }
  ```
- **참고**:
  - 이미지는 `displayOrder` 오름차순으로 정렬되어 반환됩니다.
  - 대표 이미지(`isMain=true`)가 항상 먼저 표시됩니다.

#### `POST /stores/{storeId}/images`

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

#### `GET /stores/{storeId}/temporary-closures`

- **설명**: 특정 음식점의 임시 휴무 목록을 조회합니다.
- **Response (200)**:
  ```json
  {
    "result": "SUCCESS",
    "data": [
      {
        "closureId": 1,
        "storeId": 101,
        "closureDate": "2025-12-25",
        "startTime": null,
        "endTime": null,
        "reason": "크리스마스 휴무"
      },
      {
        "closureId": 2,
        "storeId": 101,
        "closureDate": "2025-12-31",
        "startTime": "18:00",
        "endTime": "24:00",
        "reason": "연말 조기 마감"
      }
    ],
    "error": null
  }
  ```
- **참고**:
  - 임시 휴무는 `closureDate` 오름차순으로 정렬되어 반환됩니다.
  - 종일 휴무인 경우 `startTime`, `endTime`은 null입니다.
  - 과거 임시 휴무도 포함됩니다 (필요시 필터링은 클라이언트에서 처리).

---

### 3.3. 메뉴 관리 (Food/Menu)

- **Resource URI**: `/foods`, `/stores/{storeId}/foods`
- **관련 요구사항**: `[REQ-ADMIN-FOOD-001]` ~ `[REQ-ADMIN-FOOD-004]`

| HTTP Method | URI | 설명 |
|-------------|--------------------------------|--------------------|
| `GET`       | `/stores/{storeId}/foods`      | 특정 음식점의 메뉴 목록 조회 |
| `POST`      | `/stores/{storeId}/foods`      | 메뉴 생성          |
| `PUT`       | `/foods/{foodId}`              | 메뉴 수정          |
| `DELETE`    | `/foods/{foodId}`              | 메뉴 삭제 (논리적) |

#### `GET /stores/{storeId}/foods`

- **설명**: 특정 음식점의 메뉴 목록을 조회합니다.
- **Query Parameters**:
  - `sort` (string, optional): 정렬 기준
    - `displayOrder,asc`: 표시 순서 오름차순 (기본값)
    - `displayOrder,desc`: 표시 순서 내림차순
    - `price,asc`: 가격 오름차순
    - `price,desc`: 가격 내림차순
    - `isMain,desc`: 대표 메뉴 우선
- **Response (200)**:
  ```json
  {
    "result": "SUCCESS",
    "data": {
      "storeId": 101,
      "storeName": "스마트 식당",
      "foods": [
        {
          "foodId": 201,
          "foodName": "김치찌개",
          "averagePrice": 8000,
          "description": "국내산 김치로 만들었습니다.",
          "imageUrl": "http://example.com/kimchi.jpg",
          "categoryId": 1,
          "isMain": true,
          "displayOrder": 1,
          "isAvailable": true
        },
        {
          "foodId": 202,
          "foodName": "된장찌개",
          "averagePrice": 7500,
          "description": "구수한 된장찌개",
          "imageUrl": "http://example.com/doenjang.jpg",
          "categoryId": 1,
          "isMain": false,
          "displayOrder": 2,
          "isAvailable": true
        }
      ]
    },
    "error": null
  }
  ```

#### `POST /stores/{storeId}/foods`

- **설명**: 특정 음식점에 새로운 메뉴를 추가합니다.
- **변경사항 (v2.0)**:
  - ✅ **추가**: `isMain` 필드 (대표 메뉴 여부)
  - ✅ **추가**: `displayOrder` 필드 (표시 순서)
- **Request Body**:
  ```json
  {
    "foodName": "김치찌개",
    "averagePrice": 8000,
    "description": "국내산 김치로 만들었습니다.",
    "imageUrl": "http://example.com/kimchi.jpg",
    "categoryId": 1,
    "isMain": true,
    "displayOrder": 1
  }
  ```
- **참고**: 
  - 도메인 엔티티 `Food`는 `averagePrice` 필드를 사용하며, DB 테이블 `food`의 `price` 칼럼과 매핑됩니다.
  - Storage 계층에서 `entity.price = food.getAveragePrice()` 방식으로 변환됩니다.
  - `registered_dt`는 DB에서 자동 설정됩니다 (비즈니스 필드, 신메뉴 표시용)
  - `isMain`: 대표 메뉴 여부 (기본값: false)
  - `displayOrder`: 표시 순서 (낮을수록 우선, null 허용)
  - **Response (201)**:
    ```json
    {
      "result": "SUCCESS",
      "data": {
        "foodId": 201,
        "storeId": 101,
        "foodName": "김치찌개",
        "averagePrice": 8000,
        "description": "국내산 김치로 만들었습니다.",
        "imageUrl": "http://example.com/kimchi.jpg",
        "categoryId": 1,
        "isMain": true,
        "displayOrder": 1,
        "isAvailable": true
      },
      "error": null
    }
    ```

#### `PUT /foods/{foodId}`

- **설명**: 메뉴 정보를 수정합니다.
- **변경사항 (v2.0)**:
  - ✅ **추가**: `isMain`, `displayOrder` 필드 수정 가능
- **Request Body**:
  ```json
  {
    "foodName": "김치찌개 (매운맛)",
    "averagePrice": 8500,
    "description": "더 매콤해진 김치찌개",
    "imageUrl": "http://example.com/kimchi-spicy.jpg",
    "categoryId": 1,
    "isMain": true,
    "displayOrder": 1
  }
  ```

#### `DELETE /foods/{foodId}`

- **설명**: 메뉴를 삭제합니다 (논리적 삭제).
- **참고**: 
  - `deleted_at` 필드를 현재 시각으로 설정하여 논리적 삭제 처리
  - 삭제된 메뉴는 목록 조회 시 `isAvailable: false`로 표시되거나 제외됨
- **Response (204)**: No Content

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

---

## 4. CRUD 현황표

### 4.1. 완전한 CRUD 리소스 (4가지 모두 구현)

| 리소스 | Create (POST) | Read (GET) | Update (PUT) | Delete (DELETE) | 비고 |
|--------|--------------|-----------|--------------|-----------------|------|
| **Category** | ✅ | ✅ (List, Detail) | ✅ | ✅ (물리 삭제) | 완전한 CRUD |
| **Store** | ✅ | ✅ (List, Detail) | ✅ | ✅ (논리 삭제) | 완전한 CRUD |
| **Food** | ✅ | ✅ (List, Detail) | ✅ | ✅ (논리 삭제) | 완전한 CRUD |
| **Group** | ✅ | ✅ (List, Detail) | ✅ | ✅ (물리 삭제) | 완전한 CRUD |
| **Policy** | ✅ | ✅ (List, Detail) | ✅ | ✅ (물리 삭제) | 완전한 CRUD + PATCH(토글) |

### 4.2. 불완전한 CRUD 리소스

#### 4.2.1. 하위 리소스 (부모 리소스에 종속적)

| 리소스 | Create (POST) | Read (GET) | Update (PUT) | Delete (DELETE) | 상태 | 이유 |
|--------|--------------|-----------|--------------|-----------------|------|------|
| **StoreImage** | ✅ | ✅ | ✅ | ✅ (물리 삭제) | **완전한 CRUD** | GET List API 추가로 완전한 CRUD 구현 |
| **StoreOpeningHour** | ✅ | ✅ | ✅ | ✅ (물리 삭제) | **완전한 CRUD** | GET List API 추가로 완전한 CRUD 구현 |
| **StoreTemporaryClosure** | ✅ | ✅ | ❌ | ✅ (물리 삭제) | **CRD 구현** | GET List API 추가<br>수정은 삭제 후 재등록 |

**설명**:
- 이들 리소스는 Store의 하위 리소스이지만, 독립적인 목록 조회 API를 제공합니다.
- Store 상세 조회 시에도 함께 조회되므로, 두 가지 방법 모두 사용 가능합니다.
- StoreTemporaryClosure는 수정(PUT) 없이 삭제 후 재등록 방식으로 관리합니다.

#### 4.2.2. 조회 전용 리소스

| 리소스 | Create (POST) | Read (GET) | Update (PUT) | Delete (DELETE) | 상태 | 이유 |
|--------|--------------|-----------|--------------|-----------------|------|------|
| **Statistics** | ❌ | ✅ | ❌ | ❌ | **Read-Only** | 통계 데이터는 조회만 가능 (생성/수정/삭제 불가) |

**설명**:
- Statistics는 시스템에서 자동으로 집계되는 통계 데이터입니다.
- 관리자는 조회만 가능하며, 직접 생성하거나 수정할 수 없습니다.

---

## 5. 구현 체크리스트

### 5.1. 백엔드 (smartmealtable-admin)

#### Domain Layer
- [x] `StoreImage` 도메인 엔티티 생성 ✅ (2025-11-07 완료)
  - [x] `storeImageId`, `storeId`, `imageUrl`, `isMain`, `displayOrder` 필드
  - [x] `StoreImage.create()` 팩토리 메서드 (자동 대표 이미지 설정)
- [x] `Food` 도메인 엔티티 수정 ✅ (2025-11-07 완료)
  - [x] `isMain`, `displayOrder` 필드 추가
  - [x] `Food.reconstituteWithMainAndOrder()` 메서드 추가
- [x] `StoreImageRepository` 인터페이스 생성 ✅ (2025-11-07 완료)
  - [x] `deleteById(Long)` 메서드 추가

#### Storage Layer
- [x] `StoreImageJpaEntity` 생성 ✅ (2025-11-07 완료)
- [x] `StoreImageRepositoryImpl` 구현 ✅ (2025-11-07 완료)
  - [x] `deleteById()` vs `deleteByStoreId()` 구분
- [x] `FoodJpaEntity`에 `isMain`, `displayOrder` 매핑 ✅ (2025-11-07 완료)

#### Application Layer
- [x] `StoreApplicationService` 수정 ✅ (2025-11-07 완료)
  - [x] 주소 기반 지오코딩 로직 추가 (Naver Maps API)
  - [x] 가게 생성/수정 시 `latitude`, `longitude` 자동 계산
- [x] `StoreImageService` 생성 (도메인 서비스) ✅ (2025-11-07 완료)
  - [x] `createImage()` - 대표 이미지 자동 관리
  - [x] `updateImage()` - 대표 이미지 전환
  - [x] `deleteImage()` - 삭제 시 다음 이미지 자동 승격
  - [x] `getStoreImages()` - 대표 이미지 우선 정렬
  - [x] Store 존재 여부 검증
- [x] `FoodApplicationService` 수정 ✅ (2025-11-07 완료)
  - [x] `isMain`, `displayOrder` 처리 로직 추가
  - [x] 메뉴 목록 조회 시 정렬 기능 추가

#### Presentation Layer (Controller)
- [x] `StoreController` 수정 ✅ (2025-11-07 완료)
  - [x] `POST /stores`: `latitude`, `longitude` 제거
  - [x] `PUT /stores/{storeId}`: `latitude`, `longitude` 제거 (자동 계산)
  - [x] `GET /stores/{storeId}`: 응답에 `images` 배열 추가
- [x] `StoreImageController` 생성 ✅ (2025-11-07 완료)
  - [x] `POST /stores/{storeId}/images`
  - [x] `PUT /stores/{storeId}/images/{imageId}`
  - [x] `DELETE /stores/{storeId}/images/{imageId}`
- [x] `FoodController` 수정 ✅ (2025-11-07 완료)
  - [x] `POST /stores/{storeId}/foods`: `isMain`, `displayOrder` 추가
  - [x] `PUT /foods/{foodId}`: `isMain`, `displayOrder` 추가
  - [x] `GET /stores/{storeId}/foods`: 정렬 옵션 추가

#### Client Layer (Geocoding)
- [x] 지오코딩 서비스 구현 ✅ (기존 API 모듈 재사용)
  - [x] Naver Maps API 연동 (API 모듈의 `MapService` 재사용)
  - [x] 주소 → 좌표 변환 로직
  - [x] 에러 처리 (주소가 유효하지 않을 경우 `INVALID_ADDRESS`)

### 4.2. 테스트

#### 단위 테스트
- [x] `StoreImageService` 테스트 ✅ (2025-11-07 완료)
  - [x] 이미지 추가/수정/삭제 테스트
  - [x] 대표 이미지 자동 변경 테스트
  - [x] 대표 이미지 삭제 시 다음 이미지 자동 승격 테스트
  - [x] Store 존재 여부 검증 테스트

#### 통합 테스트 ✅ (2025-11-07 완료 - 81/81 테스트 통과)
- [x] `StoreControllerTest` ✅
  - [x] 가게 생성 시 지오코딩 자동 처리 검증
  - [x] 가게 수정 시 주소 변경 시 좌표 자동 재계산 검증
  - [x] 가게 상세 조회 시 이미지 배열 포함 검증
  - [x] 유효하지 않은 주소 에러 처리 검증
- [x] `StoreImageControllerTest` (11개 테스트) ✅
  - [x] 첫 번째 이미지 자동 대표 설정
  - [x] 명시적 대표 이미지 설정
  - [x] 여러 이미지 추가
  - [x] 대표 이미지 변경
  - [x] 이미지 수정 (존재하지 않는 이미지 404)
  - [x] 이미지 삭제 성공
  - [x] **대표 이미지 삭제 시 다음 이미지 자동 승격**
  - [x] 존재하지 않는 이미지 삭제 404
  - [x] 존재하지 않는 가게 404
  - [x] 이미지 URL 누락 422 (Validation)
- [x] `FoodControllerTest` (6개 정렬 테스트) ✅
  - [x] 메뉴 생성/수정 시 `isMain`, `displayOrder` 검증
  - [x] isMain 기준 정렬 (대표 메뉴 우선)
  - [x] displayOrder 기준 정렬 (오름차순/내림차순)
  - [x] 복합 정렬 (isMain 우선, displayOrder 차선)

### 5.3. 문서화
- [x] ADMIN API 명세서 업데이트 ✅ (2025-11-07 완료)
  - [x] Store API 문서 갱신 (지오코딩 자동 처리 반영)
  - [x] StoreImage API 문서 추가 (CRUD, 대표 이미지 관리)
  - [x] Food API 문서 갱신 (정렬 기능 추가)
  - [x] 구현 체크리스트 업데이트

---

## 6. 참고 문서

- [ddl.sql](../ddl.sql) - 데이터베이스 스키마
- [API_REDESIGN_FOOD_AND_STORE_IMAGE.md](./API_REDESIGN_FOOD_AND_STORE_IMAGE.md) - API 모듈 재설계 문서
- 프로젝트 계획서: `.github/copilot-instructions.md`

---

## 7. 지오코딩 API 연동 가이드

### 7.1. Naver Maps API (구현 완료)

**주의**: 본 프로젝트는 API 모듈에서 이미 Naver Maps API 기반 지오코딩 기능을 구현하여 사용하고 있습니다.  
ADMIN 모듈에서는 API 모듈의 `MapService`를 재사용하거나, 동일한 방식으로 구현하면 됩니다.

#### 설정
```yaml
# application.yml
naver:
  map:
    client-id: ${NAVER_MAP_CLIENT_ID}
    client-secret: ${NAVER_MAP_CLIENT_SECRET}
```

#### Geocoding API (주소 → 좌표 변환)

**요청 예시:**
```http
GET https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=서울시%20강남구%20테헤란로%20123
X-NCP-APIGW-API-KEY-ID: {client_id}
X-NCP-APIGW-API-KEY: {client_secret}
```

**응답 예시:**
```json
{
  "status": "OK",
  "addresses": [
    {
      "roadAddress": "서울특별시 강남구 테헤란로 123",
      "jibunAddress": "서울특별시 강남구 역삼동 123-45",
      "x": "127.027621",
      "y": "37.498095"
    }
  ]
}
```

### 7.2. 에러 처리

| 케이스 | HTTP Status | Error Code | Message |
|--------|-------------|------------|---------|
| 주소를 찾을 수 없음 | 400 | INVALID_ADDRESS | 유효하지 않은 주소입니다. |
| 지오코딩 API 장애 | 503 | GEOCODING_SERVICE_UNAVAILABLE | 주소 변환 서비스를 사용할 수 없습니다. |
| API 키 오류 | 500 | GEOCODING_API_ERROR | 주소 변환 중 오류가 발생했습니다. |

### 7.3. 기존 구현체 활용 방법

**API 모듈의 MapService 활용:**
```java
// smartmealtable-domain 모듈
public interface MapService {
    List<AddressSearchResult> searchAddress(String keyword, Integer limit);
    AddressSearchResult reverseGeocode(BigDecimal latitude, BigDecimal longitude);
}

// smartmealtable-client 모듈
@Component
public class NaverMapClient implements MapService {
    // 네이버 지도 API 구현 완료
}
```

**ADMIN 모듈에서 사용:**
```java
@Service
@RequiredArgsConstructor
public class StoreService {
    
    private final MapService mapService; // 의존성 주입
    
    public StoreResponse createStore(StoreCreateRequest request) {
        // 1. 주소로 좌표 검색
        List<AddressSearchResult> results = mapService.searchAddress(request.getAddress(), 1);
        
        if (results.isEmpty()) {
            throw new InvalidAddressException("유효하지 않은 주소입니다: " + request.getAddress());
        }
        
        AddressSearchResult addressResult = results.get(0);
        
        // 2. Store 엔티티 생성 (좌표 자동 설정)
        Store store = Store.builder()
            .name(request.getName())
            .address(request.getAddress())
            .latitude(addressResult.latitude())
            .longitude(addressResult.longitude())
            .build();
        
        // 3. 저장 및 응답
        Store savedStore = storeRepository.save(store);
        return StoreResponse.from(savedStore);
    }
}
```

### 7.4. 새로운 구현 (필요 시)

만약 ADMIN 모듈에서 독립적으로 구현하려면:

```java
@Service
@RequiredArgsConstructor
public class NaverGeocodingService implements GeocodingService {
    
    private final RestClient restClient;
    
    @Value("${naver.map.client-id}")
    private String clientId;
    
    @Value("${naver.map.client-secret}")
    private String clientSecret;
    
    @Override
    public Coordinate getCoordinateFromAddress(String address) {
        try {
            NaverGeocodingResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("maps.apigw.ntruss.com")
                    .path("/map-geocode/v2/geocode")
                    .queryParam("query", address)
                    .queryParam("count", 1)
                    .build())
                .header("X-NCP-APIGW-API-KEY-ID", clientId)
                .header("X-NCP-APIGW-API-KEY", clientSecret)
                .retrieve()
                .body(NaverGeocodingResponse.class);
            
            if (response == null || response.addresses() == null || response.addresses().isEmpty()) {
                throw new InvalidAddressException("주소를 찾을 수 없습니다: " + address);
            }
            
            NaverGeocodingResponse.Address addr = response.addresses().get(0);
            return new Coordinate(
                new BigDecimal(addr.y()), // latitude
                new BigDecimal(addr.x())  // longitude
            );
            
        } catch (RestClientException e) {
            throw new GeocodingServiceException("주소 변환 중 오류가 발생했습니다.", e);
        }
    }
}
```
