# API 스펙 업데이트 내역 (2025-11-07)

## 변경 사항

### 1. 가게 상세 조회 API 응답 변경

**Endpoint:** `GET /api/v1/stores/{storeId}`

#### 변경된 필드

1. **`images` 필드 추가** (상세 조회에만 제공)
   - 기존: `imageUrl` (String) - 단일 이미지 URL
   - 변경: `images` (Array) - 전체 이미지 배열 (가게 상세 조회에서만 제공)
   - `imageUrl` 필드는 유지 (대표 이미지 URL, 하위 호환성)

```json
"images": [
  {
    "storeImageId": 1,
    "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg",
    "isMain": true,
    "displayOrder": 1
  },
  {
    "storeImageId": 2,
    "imageUrl": "https://cdn.smartmealtable.com/stores/101/menu.jpg",
    "isMain": false,
    "displayOrder": 2
  }
]
```

2. **`menus` 필드 확장**
   - `isMain` (Boolean): 대표 메뉴 여부 추가
   - `displayOrder` (Integer): 표시 순서 추가
   - `registeredDt` (String ISO8601): 메뉴 등록일 추가
   - `isAvailable` (Boolean): 판매 가능 여부 추가

```json
"menus": [
  {
    "foodId": 201,
    "foodName": "교촌 오리지널",
    "price": 18000,
    "description": "교촌의 시그니처 메뉴",
    "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
    "isMain": true,
    "displayOrder": 1,
    "isAvailable": true,
    "registeredDt": "2024-01-15T09:00:00.000Z"
  }
]
```

3. **`registeredAt` 필드 추가**
   - 가게 등록일 (비즈니스 필드)
   - `createdAt` 대신 `registeredAt` 사용

#### 메뉴 정렬 우선순위
- isMain 우선 정렬 (대표 메뉴가 먼저)
- displayOrder 오름차순 정렬 (낮을수록 우선)
- displayOrder가 null인 경우 맨 뒤

---

### 2. 메뉴 상세 조회 API 응답 변경

**Endpoint:** `GET /api/v1/foods/{foodId}`

#### 추가된 필드
- `isMain` (Boolean): 대표 메뉴 여부
- `displayOrder` (Integer): 표시 순서
- `registeredDt` (String ISO8601): 메뉴 등록일

```json
{
  "result": "SUCCESS",
  "data": {
    "foodId": 201,
    "foodName": "교촌 오리지널",
    "description": "교촌의 시그니처 메뉴",
    "price": 18000,
    "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
    "isMain": true,
    "displayOrder": 1,
    "registeredDt": "2024-01-15T09:00:00.000Z",
    "store": { ... },
    "isAvailable": true,
    "budgetComparison": { ... }
  }
}
```

---

### 3. 가게별 메뉴 목록 조회 API (신규)

**Endpoint:** `GET /api/v1/stores/{storeId}/foods`

**설명:**
- 특정 가게의 메뉴 목록만 조회합니다.
- 다양한 정렬 옵션을 지원합니다.

**Query Parameters:**
- `sort` (optional): 정렬 기준 (기본값: `displayOrder,asc`)
  - `displayOrder,asc`: 표시 순서 오름차순
  - `displayOrder,desc`: 표시 순서 내림차순
  - `price,asc`: 가격 오름차순
  - `price,desc`: 가격 내림차순
  - `registeredDt,desc`: 신메뉴 순 (최신순)
  - `isMain,desc`: 대표 메뉴 우선

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "storeId": 101,
    "storeName": "교촌치킨 강남점",
    "foods": [
      {
        "foodId": 201,
        "foodName": "교촌 오리지널",
        "price": 18000,
        "description": "교촌의 시그니처 메뉴",
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
        "isMain": true,
        "displayOrder": 1,
        "isAvailable": true,
        "registeredDt": "2024-01-15T09:00:00.000Z"
      },
      {
        "foodId": 202,
        "foodName": "교촌 레드",
        "price": 18000,
        "description": "매콤한 양념치킨",
        "imageUrl": "https://cdn.smartmealtable.com/foods/202.jpg",
        "isMain": false,
        "displayOrder": 2,
        "isAvailable": true,
        "registeredDt": "2024-01-20T10:30:00.000Z"
      }
    ]
  },
  "error": null
}
```

**Error Cases:**
- `404`: 가게를 찾을 수 없음

---

## 하위 호환성

### 가게 목록 조회
- **변경 없음**: `imageUrl` (String) - 대표 이미지 URL만 제공 (성능 최적화)

### 가게 상세 조회
- **하위 호환성 유지**: 기존 `imageUrl` 필드 유지
- **신규 필드 추가**: `images` 배열 추가 (상세 정보 제공)
- 프론트엔드는 `images` 배열이 있으면 사용하고, 없으면 `imageUrl` 폴백

---

## 데이터베이스 스키마 변경

### Food 테이블
```sql
ALTER TABLE food 
ADD COLUMN is_main BOOLEAN NOT NULL DEFAULT FALSE COMMENT '대표 메뉴 여부',
ADD COLUMN display_order INT NULL COMMENT '표시 순서 (낮을수록 우선)',
ADD INDEX idx_store_is_main (store_id, is_main),
ADD INDEX idx_store_display (store_id, display_order);
```

### StoreImage 테이블 (신규)
```sql
CREATE TABLE store_image (
    store_image_id BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (store_image_id),
    INDEX idx_store_id (store_id),
    INDEX idx_store_main (store_id, is_main),
    INDEX idx_store_display (store_id, display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 마이그레이션 참고사항

1. **기존 Store.imageUrl 데이터를 StoreImage로 마이그레이션 필요**
2. **프론트엔드는 점진적 업데이트 가능** (하위 호환성 유지)
3. **가게 상세 페이지에서만 이미지 갤러리 UI 적용**
