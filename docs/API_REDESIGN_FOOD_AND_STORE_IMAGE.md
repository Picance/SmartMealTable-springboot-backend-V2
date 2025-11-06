# API 재설계: Food 및 StoreImage 스키마 변경 반영

**작성일:** 2025-11-07  
**목적:** `food` 엔티티의 `is_main`, `display_order` 추가 및 `store_image` 테이블 신규 추가에 따른 API 스펙 재설계

---

## 1. 변경 사항 요약

### 1.1 데이터베이스 스키마 변경

#### Food 테이블
```sql
-- 추가된 컬럼
is_main         BOOLEAN    NOT NULL DEFAULT FALSE  -- 대표 메뉴 여부
display_order   INT        NULL                     -- 표시 순서 (낮을수록 우선)
```

#### StoreImage 테이블 (신규)
```sql
CREATE TABLE store_image (
    store_image_id BIGINT         NOT NULL AUTO_INCREMENT,
    store_id       BIGINT         NOT NULL,
    image_url      VARCHAR(500)   NOT NULL,
    is_main        BOOLEAN        NOT NULL DEFAULT FALSE,  -- 대표 이미지 여부
    display_order  INT            NULL,                     -- 표시 순서
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (store_image_id),
    INDEX idx_store_id (store_id),
    INDEX idx_store_main (store_id, is_main),
    INDEX idx_store_display (store_id, display_order)
);
```

### 1.2 주요 영향

1. **가게 이미지 관리 방식 변경**
   - 기존: Store 테이블의 단일 `image_url` 필드
   - 변경: StoreImage 테이블로 다중 이미지 관리
   - **API 응답 전략**:
     - 가게 **목록 조회**: 대표 이미지 URL만 반환 (성능 최적화)
     - 가게 **상세 조회**: 전체 이미지 배열 반환 (갤러리 제공)

2. **메뉴 정렬 및 강조 기능 추가**
   - `is_main`: 대표 메뉴 표시
   - `display_order`: 메뉴 표시 순서 제어

---

## 2. API 응답 DTO 재설계

### 2.1 공통 DTO: StoreImageDto

```java
/**
 * 가게 이미지 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreImageDto {
    
    /**
     * 이미지 ID
     */
    private Long storeImageId;
    
    /**
     * 이미지 URL
     */
    private String imageUrl;
    
    /**
     * 대표 이미지 여부
     */
    private Boolean isMain;
    
    /**
     * 표시 순서 (낮을수록 우선)
     */
    private Integer displayOrder;
}
```

### 2.2 공통 DTO: FoodDto (메뉴 정보)

```java
/**
 * 음식(메뉴) 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDto {
    
    /**
     * 메뉴 ID
     */
    private Long foodId;
    
    /**
     * 메뉴명
     */
    private String foodName;
    
    /**
     * 가격
     */
    private Integer price;
    
    /**
     * 설명
     */
    private String description;
    
    /**
     * 메뉴 이미지 URL
     */
    private String imageUrl;
    
    /**
     * 대표 메뉴 여부
     */
    private Boolean isMain;
    
    /**
     * 표시 순서 (낮을수록 우선)
     */
    private Integer displayOrder;
    
    /**
     * 판매 가능 여부 (deleted_at이 null인 경우 true)
     */
    private Boolean isAvailable;
}
```

---

## 3. 재설계된 API 엔드포인트

### 3.1 가게 상세 조회 (변경)

**Endpoint:** `GET /api/v1/stores/{storeId}`

**주요 변경사항:**
- 가게 상세 페이지에서만 전체 이미지 배열(`images`) 제공
- `menus` 배열에 `isMain`, `displayOrder` 필드 추가
- `recommendedMenus`에도 동일 적용

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "storeId": 101,
    "storeName": "교촌치킨 강남점",
    "categoryId": 5,
    "categoryName": "치킨",
    "address": "서울특별시 강남구 테헤란로 123",
    "lotNumberAddress": "서울특별시 강남구 역삼동 123-45",
    "phoneNumber": "02-1234-5678",
    "description": "신선한 국내산 닭으로 만드는 웰빙 치킨",
    "averagePrice": 18000,
    "reviewCount": 1523,
    "viewCount": 45230,
    "favoriteCount": 342,
    "storeType": "FRANCHISE",
    "latitude": 37.498095,
    "longitude": 127.027610,
    "images": [
      {
        "storeImageId": 1001,
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg",
        "isMain": true,
        "displayOrder": 1
      },
      {
        "storeImageId": 1002,
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/interior.jpg",
        "isMain": false,
        "displayOrder": 2
      },
      {
        "storeImageId": 1003,
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/menu.jpg",
        "isMain": false,
        "displayOrder": 3
      }
    ],
    "openingHours": [
      {
        "dayOfWeek": "MONDAY",
        "openTime": "11:00:00",
        "closeTime": "22:00:00",
        "breakStartTime": "15:00:00",
        "breakEndTime": "17:00:00",
        "isHoliday": false
      }
    ],
    "temporaryClosures": [],
    "recommendedMenus": [
      {
        "foodId": 201,
        "foodName": "교촌 오리지널",
        "price": 18000,
        "description": "교촌의 시그니처 메뉴",
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
        "isMain": true,
        "displayOrder": 1,
        "isAvailable": true,
        "recommendationScore": 92.5,
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
        "isMain": true,
        "displayOrder": 1,
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
        "isMain": false,
        "displayOrder": 2,
        "isAvailable": true,
        "budgetComparison": {
          "userMealBudget": 20000,
          "difference": 2000,
          "isOverBudget": false,
          "differenceText": "-2,000원"
        }
      }
    ],
    "seller": {
      "sellerId": 501,
      "businessNumber": "123-45-67890",
      "ownerName": "김사장"
    },
    "registeredAt": "2024-01-15T09:00:00"
  },
  "error": null
}
```

**Response Fields 변경:**

| 필드 | 기존 타입 | 변경 타입 | 설명 |
|------|----------|----------|------|
| `imageUrl` | `String` | `String` (유지) | 대표 이미지 URL (변경 없음, 성능 최적화를 위해 단순 문자열 유지) |
| `images` | ❌ 없음 | `Array<StoreImageDto>` | **가게 상세 조회에만** 제공되는 전체 이미지 배열 |
| `menus[].isMain` | ❌ 없음 | `Boolean` | 대표 메뉴 여부 |
| `menus[].displayOrder` | ❌ 없음 | `Integer` | 표시 순서 |
| `recommendedMenus[].isMain` | ❌ 없음 | `Boolean` | 대표 메뉴 여부 |
| `recommendedMenus[].displayOrder` | ❌ 없음 | `Integer` | 표시 순서 |
| `createdAt` | `String (ISO8601)` | ❌ 제거 | 감사 로그는 노출하지 않음 |
| `registeredAt` | ❌ 없음 | `String (ISO8601)` | 가게 등록일 (비즈니스 필드) |

**이미지 제공 전략:**
- **가게 목록 조회**: `imageUrl` (String) - 대표 이미지만 제공
- **가게 상세 조회**: `images` (Array) - 전체 이미지 배열 제공 (갤러리 형태)

---

### 3.2 가게 목록 조회 (변경)

**Endpoint:** `GET /api/v1/stores?categoryId={categoryId}&latitude={lat}&longitude={lng}&radius={radius}&page=0&size=20`

**주요 변경사항:**
- 가게 목록에서는 대표 이미지 URL만 제공 (성능 최적화)
- 상세 페이지 진입 시에만 전체 이미지 목록 제공

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "content": [
      {
        "storeId": 101,
        "storeName": "교촌치킨 강남점",
        "categoryName": "치킨",
        "address": "서울특별시 강남구 테헤란로 123",
        "distance": 0.5,
        "averagePrice": 18000,
        "reviewCount": 1523,
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
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

**Response Fields 설명:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `content[].imageUrl` | `String` | 대표 이미지 URL (is_main=true인 이미지, 없으면 첫 번째 이미지) |

**참고:**
- 가게 목록 조회 시에는 성능을 위해 대표 이미지 URL만 반환
- 전체 이미지 목록은 가게 상세 조회(`GET /api/v1/stores/{storeId}`)에서 `images` 배열로 제공

---

### 3.3 메뉴 상세 조회 (변경)

**Endpoint:** `GET /api/v1/foods/{foodId}`

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
    "isMain": true,
    "displayOrder": 1,
    "registeredDt": "2024-01-15T09:00:00",
    "store": {
      "storeId": 101,
      "storeName": "교촌치킨 강남점",
      "categoryName": "치킨",
      "address": "서울특별시 강남구 테헤란로 123",
      "phoneNumber": "02-1234-5678",
      "averagePrice": 18000,
      "reviewCount": 1523,
      "mainImage": {
        "storeImageId": 1001,
        "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg",
        "isMain": true,
        "displayOrder": 1
      }
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

**Response Fields 추가:**

| 필드 | 타입 | 설명 |
|------|------|------|
| `isMain` | `Boolean` | 대표 메뉴 여부 |
| `displayOrder` | `Integer` | 표시 순서 |
| `registeredDt` | `String (ISO8601)` | 메뉴 등록일 (신메뉴 표시용) |
| `store.mainImage` | `StoreImageDto` | 가게 대표 이미지 (기존 `imageUrl` 대체) |

---

### 3.4 가게별 메뉴 목록 조회 (신규 추가)

**Endpoint:** `GET /api/v1/stores/{storeId}/foods?sort=displayOrder,asc`

**설명:**
- 특정 가게의 메뉴 목록만 조회
- 정렬 옵션: `displayOrder,asc` (기본값), `price,asc`, `registeredDt,desc` (신메뉴 순)

**Query Parameters:**
- `sort`: 정렬 기준 (옵션)
  - `displayOrder,asc`: 표시 순서 오름차순 (기본값)
  - `displayOrder,desc`: 표시 순서 내림차순
  - `price,asc`: 가격 오름차순
  - `price,desc`: 가격 내림차순
  - `registeredDt,desc`: 신메뉴 순
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
        "isMain": false,
        "displayOrder": 2,
        "isAvailable": true,
        "budgetComparison": {
          "userMealBudget": 20000,
          "difference": 2000,
          "isOverBudget": false,
          "differenceText": "-2,000원"
        }
      }
    ]
  },
  "error": null
}
```

---

### 3.5 추천 메뉴 조회 (변경)

**Endpoint:** `GET /api/v1/recommendations/foods?mealType=LUNCH&budget=10000&latitude=37.498095&longitude=127.027610`

**Response (200):**
```json
{
  "result": "SUCCESS",
  "data": {
    "mealType": "LUNCH",
    "userBudget": 10000,
    "recommendedFoods": [
      {
        "foodId": 201,
        "foodName": "교촌 오리지널",
        "price": 18000,
        "description": "교촌의 시그니처 메뉴",
        "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
        "isMain": true,
        "displayOrder": 1,
        "store": {
          "storeId": 101,
          "storeName": "교촌치킨 강남점",
          "categoryName": "치킨",
          "distance": 0.5,
          "mainImage": {
            "storeImageId": 1001,
            "imageUrl": "https://cdn.smartmealtable.com/stores/101/main.jpg",
            "isMain": true,
            "displayOrder": 1
          }
        },
        "recommendationScore": 92.5,
        "budgetComparison": {
          "userMealBudget": 20000,
          "difference": 2000,
          "isOverBudget": false,
          "differenceText": "-2,000원"
        }
      }
    ]
  },
  "error": null
}
```

---

## 4. 비즈니스 로직 변경

### 4.1 가게 이미지 조회 우선순위

```java
/**
 * 가게 이미지 조회 로직
 */
public List<StoreImage> getStoreImages(Long storeId) {
    return storeImageRepository.findByStoreId(storeId)
        .stream()
        .sorted(Comparator
            .comparing(StoreImage::isMain, Comparator.reverseOrder()) // 대표 이미지 우선
            .thenComparing(StoreImage::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)) // 표시 순서
        )
        .collect(Collectors.toList());
}

/**
 * 대표 이미지만 조회
 */
public Optional<StoreImage> getMainImage(Long storeId) {
    return storeImageRepository.findByStoreIdAndIsMainTrue(storeId);
}
```

### 4.2 메뉴 조회 정렬 우선순위

```java
/**
 * 가게 메뉴 조회 로직 (기본 정렬)
 */
public List<Food> getStoreFoods(Long storeId) {
    return foodRepository.findByStoreIdAndDeletedAtIsNull(storeId)
        .stream()
        .sorted(Comparator
            .comparing(Food::getIsMain, Comparator.reverseOrder()) // 대표 메뉴 우선
            .thenComparing(Food::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)) // 표시 순서
            .thenComparing(Food::getPrice) // 가격 오름차순
        )
        .collect(Collectors.toList());
}
```

---

## 5. 마이그레이션 가이드

### 5.1 프론트엔드 코드 변경

#### Before (기존)
```typescript
// 가게 목록 조회
interface StoreListItem {
  storeId: number;
  storeName: string;
  imageUrl: string; // ✅ 유지됨 (대표 이미지만 필요)
}

// 가게 상세 조회
interface StoreDetail {
  storeId: number;
  storeName: string;
  imageUrl: string; // ❌ 제거됨
  menus: Array<{
    foodId: number;
    foodName: string;
    price: number;
    // isMain, displayOrder 없음
  }>;
}
```

#### After (변경)
```typescript
// 가게 목록 조회 (변경 없음)
interface StoreListItem {
  storeId: number;
  storeName: string;
  imageUrl: string; // ✅ 유지 (대표 이미지만 제공)
}

// 가게 상세 조회 (변경됨)
interface StoreDetail {
  storeId: number;
  storeName: string;
  images: Array<StoreImage>; // ✅ 추가됨 (상세 페이지에서만 제공)
  menus: Array<{
    foodId: number;
    foodName: string;
    price: number;
    isMain: boolean;        // ✅ 추가됨
    displayOrder: number;   // ✅ 추가됨
  }>;
}

interface StoreImage {
  storeImageId: number;
  imageUrl: string;
  isMain: boolean;
  displayOrder: number;
}

// 대표 이미지 추출 헬퍼 함수 (가게 상세 페이지용)
function getMainImageUrl(images: StoreImage[]): string | null {
  const mainImage = images.find(img => img.isMain);
  return mainImage?.imageUrl ?? images[0]?.imageUrl ?? null;
}
```

### 5.2 백엔드 DTO 매핑 예시

```java
/**
 * Store 엔티티 → StoreListItemResponse 매핑 (목록 조회용)
 */
public StoreListItemResponse toStoreListItemResponse(Store store, String mainImageUrl) {
    return StoreListItemResponse.builder()
        .storeId(store.getStoreId())
        .storeName(store.getName())
        .imageUrl(mainImageUrl) // 대표 이미지 URL만 포함
        .averagePrice(store.getAveragePrice())
        .reviewCount(store.getReviewCount())
        .build();
}

/**
 * Store 엔티티 → StoreDetailResponse 매핑 (상세 조회용)
 */
public StoreDetailResponse toStoreDetailResponse(Store store, List<StoreImage> images, List<Food> menus) {
    return StoreDetailResponse.builder()
        .storeId(store.getStoreId())
        .storeName(store.getName())
        .images(images.stream() // 전체 이미지 배열 포함
            .map(this::toStoreImageDto)
            .collect(Collectors.toList()))
        .menus(menus.stream()
            .map(this::toFoodDto)
            .collect(Collectors.toList()))
        .build();
}

private StoreImageDto toStoreImageDto(StoreImage storeImage) {
    return StoreImageDto.builder()
        .storeImageId(storeImage.getStoreImageId())
        .imageUrl(storeImage.getImageUrl())
        .isMain(storeImage.isMain())
        .displayOrder(storeImage.getDisplayOrder())
        .build();
}

private FoodDto toFoodDto(Food food) {
    return FoodDto.builder()
        .foodId(food.getFoodId())
        .foodName(food.getFoodName())
        .price(food.getPrice())
        .description(food.getDescription())
        .imageUrl(food.getImageUrl())
        .isMain(food.getIsMain())
        .displayOrder(food.getDisplayOrder())
        .isAvailable(food.getDeletedAt() == null)
        .build();
}
```

**대표 이미지 조회 로직:**
```java
/**
 * 대표 이미지 URL 추출 (목록 조회용)
 */
public String getMainImageUrl(Long storeId) {
    return storeImageRepository.findByStoreIdAndIsMainTrue(storeId)
        .map(StoreImage::getImageUrl)
        .orElseGet(() -> 
            storeImageRepository.findFirstByStoreIdOrderByDisplayOrderAsc(storeId)
                .map(StoreImage::getImageUrl)
                .orElse(null)
        );
}
```

---

## 6. 테스트 시나리오

### 6.1 가게 이미지 조회 테스트

```java
@Test
@DisplayName("가게 상세 조회 시 이미지 배열이 정렬되어 반환되어야 한다")
void getStoreDetail_shouldReturnSortedImages() {
    // given
    Long storeId = 101L;
    
    // when
    StoreDetailResponse response = storeService.getStoreDetail(storeId, memberId);
    
    // then
    assertThat(response.getImages()).isNotEmpty();
    assertThat(response.getImages().get(0).getIsMain()).isTrue(); // 첫 번째는 대표 이미지
    assertThat(response.getImages())
        .extracting(StoreImageDto::getDisplayOrder)
        .isSorted(); // displayOrder 오름차순 정렬
}
```

### 6.2 메뉴 정렬 테스트

```java
@Test
@DisplayName("가게 메뉴 조회 시 대표 메뉴가 우선 표시되어야 한다")
void getStoreFoods_shouldPrioritizeMainMenus() {
    // given
    Long storeId = 101L;
    
    // when
    List<FoodDto> foods = storeService.getStoreFoods(storeId);
    
    // then
    assertThat(foods).isNotEmpty();
    
    // 대표 메뉴가 먼저 오는지 확인
    List<FoodDto> mainMenus = foods.stream()
        .filter(FoodDto::getIsMain)
        .collect(Collectors.toList());
    
    List<FoodDto> normalMenus = foods.stream()
        .filter(food -> !food.getIsMain())
        .collect(Collectors.toList());
    
    // 대표 메뉴들이 일반 메뉴보다 앞에 위치
    if (!mainMenus.isEmpty() && !normalMenus.isEmpty()) {
        int lastMainIndex = foods.indexOf(mainMenus.get(mainMenus.size() - 1));
        int firstNormalIndex = foods.indexOf(normalMenus.get(0));
        assertThat(lastMainIndex).isLessThan(firstNormalIndex);
    }
}
```

---

## 7. API 버전 관리 전략

### 7.1 호환성 유지 방안

**옵션 1: API 버전 업그레이드**
- 기존 `/api/v1/stores/{id}` → `/api/v2/stores/{id}`
- v1은 deprecated 처리 후 일정 기간 유지
- 프론트엔드는 점진적으로 v2로 마이그레이션

**옵션 2: 하위 호환성 유지 (권장)**
- v1 엔드포인트는 기존 형식 유지
- 가게 목록 조회의 `imageUrl`은 변경 없음 (이미 대표 이미지 URL 제공 중)
- 가게 상세 조회에서만 `images` 배열 추가 (신규 필드)

```java
// 가게 상세 조회 응답 DTO
public class StoreDetailResponse {
    private Long storeId;
    private String storeName;
    
    // 신규 필드: 전체 이미지 배열 (상세 페이지에서만 제공)
    private List<StoreImageDto> images;
    
    // ... 기타 필드
}

// 가게 목록 조회 응답 DTO
public class StoreListItemDto {
    private Long storeId;
    private String storeName;
    
    // 기존 필드 유지 (변경 없음)
    private String imageUrl; // 대표 이미지 URL
    
    // ... 기타 필드
}
```

---

## 8. 구현 우선순위

### Phase 1: 핵심 변경 (1-2일)
1. ✅ `FoodJpaEntity`에 `isMain`, `displayOrder` 추가 (완료)
2. ✅ `StoreImageJpaEntity` 생성 및 Repository 구현 (완료)
3. `StoreImageDto`, `FoodDto` 재설계
4. `GET /api/v1/stores/{id}` 응답 변경

### Phase 2: 정렬 및 필터링 (1일)
5. 메뉴 목록 정렬 로직 구현 (`isMain`, `displayOrder` 우선순위)
6. 가게 이미지 정렬 로직 구현
7. `GET /api/v1/stores/{storeId}/foods` 신규 엔드포인트 구현

### Phase 3: 테스트 및 검증 (1일)
8. 단위 테스트 작성
9. 통합 테스트 작성
10. API 문서 업데이트 (Spring Rest Docs)

### Phase 4: 프론트엔드 연동 (협업)
11. 프론트엔드 DTO 타입 정의 업데이트
12. UI 컴포넌트 수정 (이미지 갤러리, 대표 메뉴 뱃지)
13. E2E 테스트

---

## 9. 주의사항

### 9.1 데이터 일관성
- 기존 Store 테이블의 `image_url` 데이터를 StoreImage 테이블로 마이그레이션 필요
- 마이그레이션 스크립트:
```sql
-- Store의 기존 image_url을 StoreImage로 마이그레이션
INSERT INTO store_image (store_id, image_url, is_main, display_order, created_at, updated_at)
SELECT 
    store_id,
    image_url,
    TRUE AS is_main,
    1 AS display_order,
    NOW() AS created_at,
    NOW() AS updated_at
FROM store
WHERE image_url IS NOT NULL;
```

### 9.2 성능 최적화

**가게 목록 조회 최적화:**
```java
/**
 * 가게 목록 조회 시 대표 이미지만 조회 (JOIN 최소화)
 */
@Query("""
    SELECT s.storeId, s.name, si.imageUrl
    FROM Store s
    LEFT JOIN StoreImage si ON si.storeId = s.storeId AND si.isMain = true
    WHERE s.deletedAt IS NULL
""")
List<StoreListProjection> findStoreListWithMainImage();
```

**가게 상세 조회 최적화:**
- N+1 문제 방지를 위해 `@EntityGraph` 또는 `JOIN FETCH` 사용:
```java
@EntityGraph(attributePaths = {"images"})
@Query("SELECT s FROM Store s WHERE s.storeId = :storeId")
Optional<Store> findByIdWithImages(@Param("storeId") Long storeId);
```

**Repository 쿼리 메서드:**
```java
// 대표 이미지만 조회
Optional<StoreImage> findByStoreIdAndIsMainTrue(Long storeId);

// 첫 번째 이미지 조회 (fallback)
Optional<StoreImage> findFirstByStoreIdOrderByDisplayOrderAsc(Long storeId);

// 전체 이미지 조회 (상세 페이지용)
List<StoreImage> findByStoreIdOrderByIsMainDescDisplayOrderAsc(Long storeId);
```

### 9.3 Null 처리
- `displayOrder`가 null인 경우 정렬 순서를 맨 뒤로 처리
- `images` 배열이 비어있는 경우 기본 이미지 URL 반환

---

## 10. 체크리스트

### 백엔드
- [ ] `StoreImageDto` 생성
- [ ] `FoodDto`에 `isMain`, `displayOrder` 추가
- [ ] `StoreDetailResponse` 수정 (`images` 배열 추가)
- [ ] `StoreListItemDto`는 `imageUrl` 유지 (변경 없음)
- [ ] 가게 상세 조회 Service 로직 수정 (전체 이미지 조회)
- [ ] 가게 목록 조회 Service 로직 수정 (대표 이미지만 조회)
- [ ] 메뉴 조회 Service 로직 수정 (정렬 추가)
- [ ] `GET /api/v1/stores/{storeId}/foods` 신규 엔드포인트 구현
- [ ] StoreImageRepository 쿼리 메서드 추가
  - [ ] `findByStoreIdAndIsMainTrue(Long storeId)`
  - [ ] `findFirstByStoreIdOrderByDisplayOrderAsc(Long storeId)`
  - [ ] `findByStoreIdOrderByIsMainDescDisplayOrderAsc(Long storeId)`
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] Spring Rest Docs 업데이트

### 데이터베이스
- [ ] 기존 `image_url` 데이터 마이그레이션 스크립트 작성
- [ ] 마이그레이션 스크립트 검증 (테스트 환경)
- [ ] 프로덕션 마이그레이션 실행

### 프론트엔드
- [ ] TypeScript 인터페이스 업데이트
  - [ ] 가게 목록: `imageUrl` 유지 (변경 없음)
  - [ ] 가게 상세: `images` 배열 추가
- [ ] 가게 상세 페이지 UI 수정 (이미지 갤러리 구현)
- [ ] 메뉴 목록 UI 수정 (대표 메뉴 뱃지, 정렬)
- [ ] E2E 테스트

---

## 11. 참고 문서

- [ddl.sql](../ddl.sql) - 데이터베이스 스키마
- [API_SPECIFICATION.md](./API_SPECIFICATION.md) - 기존 API 명세
- 프로젝트 계획서: `.github/copilot-instructions.md`
