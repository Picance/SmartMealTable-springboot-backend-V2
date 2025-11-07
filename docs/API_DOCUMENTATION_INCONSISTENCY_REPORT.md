# 📋 API 코드 vs 문서 일치성 검토 보고서

**작성일**: 2025-11-07  
**검토 모듈**: `smartmealtable-admin`  
**상태**: ⚠️ **심각한 불일치 발견**

---

## 요약

ADMIN API 코드와 공식 문서(`ADMIN_API_SPECIFICATION.md`)를 비교한 결과, **다음과 같은 불일치 사항**이 발견되었습니다:

| 카테고리 | 심각도 | 문제 |
|---------|--------|------|
| **Store Update 요청 필드** | 🔴 심각 | `latitude`, `longitude`, `imageUrl` 필드가 요청에 여전히 존재 |
| **Food API 경로** | 🔴 심각 | 문서의 `/stores/{storeId}/foods` 구조와 코드의 `/foods` 구조 불일치 |
| **FoodController 응답 상태 코드** | 🟡 중간 | DELETE 메서드가 204 No Content 대신 200 OK 반환 |

---

## 🔴 Critical Issues (심각)

### Issue 1: Store Update 요청 필드 불일치

#### 📄 문서의 명세 (ADMIN_API_SPECIFICATION.md v2.0)

**PUT /stores/{storeId}** - v2.0에서 다음 필드가 **제거**됨:
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
  // ❌ latitude, longitude 제거됨 (서버 자동 처리)
  // ❌ imageUrl 제거됨 (별도 이미지 관리 API 사용)
}
```

#### 💻 실제 코드 (UpdateStoreRequest.java)

```java
public record UpdateStoreRequest(
    // ... 기타 필드 ...
    
    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    BigDecimal latitude,  // ❌ 여전히 요청에 포함됨!

    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    BigDecimal longitude,  // ❌ 여전히 요청에 포함됨!

    @Size(max = 500, message = "이미지 URL은 최대 500자까지 입력 가능합니다.")
    String imageUrl  // ❌ 여전히 요청에 포함됨!
)
```

#### ⚠️ 영향

- **클라이언트 혼동**: 클라이언트에서 문서를 따라 `latitude`, `longitude` 없이 요청하면 `null` 값이 전달되어 기존 데이터가 덮어씌워질 수 있음
- **불일치한 동작**: 문서에서는 주소 기반 자동 지오코딩을 명시하지만, 코드에서는 클라이언트가 전달한 값을 사용할 가능성

#### ✅ 해결 방안

**옵션 1: 코드 수정 (권장)**
- `UpdateStoreRequest`에서 `latitude`, `longitude`, `imageUrl` 필드 제거
- StoreController의 updateStore 메서드 수정

**옵션 2: 문서 수정**
- v2.0 명세를 v1.x로 롤백
- `latitude`, `longitude`, `imageUrl` 필드를 포함하도록 문서 수정

---

### Issue 2: Food API 경로 구조 불일치

#### 📄 문서의 명세 (ADMIN_API_SPECIFICATION.md)

**Resource URI**: `/stores/{storeId}/foods`

| HTTP Method | URI | 설명 |
|-------------|--------------------------------|--------------------|
| `GET`       | `/stores/{storeId}/foods`      | 특정 음식점의 메뉴 목록 조회 |
| `POST`      | `/stores/{storeId}/foods`      | 메뉴 생성          |
| `PUT`       | `/foods/{foodId}`              | 메뉴 수정          |
| `DELETE`    | `/foods/{foodId}`              | 메뉴 삭제 (논리적) |

**설명**: 메뉴는 특정 음식점에 속하므로, 조회/생성 시 부모 리소스(`storeId`)를 포함하는 것이 REST 원칙에 부합합니다.

#### 💻 실제 코드 (FoodController.java)

```java
@RestController
@RequestMapping("/api/v1/admin/foods")  // ❌ /stores/{storeId}/foods가 아님!
@RequiredArgsConstructor
@Slf4j
@Validated
public class FoodController {

    // 메뉴 목록 조회: GET /api/v1/admin/foods?storeId=1
    @GetMapping
    public ApiResponse<FoodListResponse> getFoods(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long storeId,  // Query parameter 사용
            @RequestParam(required = false) String name,
            // ...
    ) { /* ... */ }

    // 메뉴 생성: POST /api/v1/admin/foods (body에 storeId 포함)
    @PostMapping
    public ApiResponse<FoodResponse> createFood(
            @RequestBody @Valid CreateFoodRequest request  // storeId는 request body에 포함
    ) { /* ... */ }

    // 메뉴 수정: PUT /api/v1/admin/foods/{foodId}
    @PutMapping("/{foodId}")
    public ApiResponse<FoodResponse> updateFood(
            @PathVariable @Positive Long foodId,
            @RequestBody @Valid UpdateFoodRequest request
    ) { /* ... */ }

    // 메뉴 삭제: DELETE /api/v1/admin/foods/{foodId}
    @DeleteMapping("/{foodId}")
    public ApiResponse<Void> deleteFood(
            @PathVariable @Positive Long foodId
    ) { /* ... */ }
}
```

#### 현재 API 요청/응답 구조

| 문서 API | 현재 코드 API | 파라미터 방식 |
|---------|-------------|-------------|
| `GET /stores/{storeId}/foods` | `GET /api/v1/admin/foods?storeId=1` | Query parameter |
| `POST /stores/{storeId}/foods` | `POST /api/v1/admin/foods` + body | Request body |
| `PUT /foods/{foodId}` | `PUT /api/v1/admin/foods/{foodId}` | ✅ 일치 |
| `DELETE /foods/{foodId}` | `DELETE /api/v1/admin/foods/{foodId}` | ✅ 일치 |

#### ⚠️ 영향

- **API 계약 위반**: 클라이언트가 문서를 따라 `GET /api/v1/admin/stores/1/foods`로 요청하면 404 에러 발생
- **REST 설계 불일치**: 
  - 문서: 부모 리소스 기반 계층 구조 (`/stores/{storeId}/foods`)
  - 코드: 단일 리소스 + 쿼리/바디 파라미터 (RPC 스타일)
- **OpenAPI/Swagger 문제**: Swagger로 생성된 클라이언트 SDK는 문서 기반으로 생성되므로, 실제 API와 맞지 않음

#### ✅ 해결 방안

**옵션 1: 코드 수정 - PathVariable 구조로 변경 (권장)**

```java
@RestController
@RequestMapping("/api/v1/admin/stores/{storeId}/foods")
@RequiredArgsConstructor
public class FoodController {
    
    @GetMapping
    public ApiResponse<FoodListResponse> getFoods(
            @PathVariable Long storeId,  // PathVariable로 변경
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        // ...
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FoodResponse> createFood(
            @PathVariable Long storeId,  // PathVariable로 변경
            @RequestBody @Valid CreateFoodRequest request
    ) {
        // request body에서 storeId 제거, PathVariable에서 전달받음
        // ...
    }
}
```

**옵션 2: 문서 수정**
- FoodController의 현재 API 구조에 맞게 문서 변경
- Query parameter와 request body 사용 명시

---

## 🟡 Medium Issues (중간)

### Issue 3: FoodController DELETE 메서드 응답 상태 코드 불일치

#### 📄 문서의 명세 (ADMIN_API_SPECIFICATION.md)

```markdown
#### `DELETE /foods/{foodId}`

- **설명**: 메뉴를 삭제합니다 (논리적 삭제).
- **Response (204)**: No Content
```

#### 💻 실제 코드 (FoodController.java)

```java
@DeleteMapping("/{foodId}")
public ApiResponse<Void> deleteFood(
        @PathVariable @Positive Long foodId
) {
    log.info("[ADMIN] DELETE /api/v1/admin/foods/{} - foodId: {}", foodId, foodId);
    
    foodApplicationService.deleteFood(foodId);
    
    return ApiResponse.success();  // ❌ 200 OK 반환 (ApiResponse wrapper 사용)
}
```

#### ⚠️ 영향

- **REST 컨벤션 위반**: DELETE 성공은 일반적으로 204 No Content 또는 200 OK (응답 본문 있는 경우)
- **클라이언트 기대값 불일치**: 문서를 따라 204를 기대하는 클라이언트에서 200이 오면 혼동 가능

#### ✅ 해결 방안

**옵션 1: 코드 수정 (권장)**

```java
@DeleteMapping("/{foodId}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteFood(
        @PathVariable @Positive Long foodId
) {
    log.info("[ADMIN] DELETE /api/v1/admin/foods/{} - foodId: {}", foodId, foodId);
    
    foodApplicationService.deleteFood(foodId);
    // 응답 없음 (204 No Content 자동 반환)
}
```

**옵션 2: 문서 수정**
- DELETE 응답을 200 OK로 변경
- 응답 본문 명시

---

## ✅ 검증된 일치하는 항목

### 1. Store 생성 (CreateStoreRequest) ✅

**문서**: latitude, longitude 제거, imageUrl 제거  
**코드**: ✅ 일치

```java
public record CreateStoreRequest(
    String name,
    Long categoryId,
    Long sellerId,
    String address,
    String lotNumberAddress,
    String phoneNumber,
    String description,
    Integer averagePrice,
    StoreType storeType
    // ✅ latitude, longitude, imageUrl 없음
)
```

### 2. StoreImage API ✅

**문서**: 
- `GET /stores/{storeId}/images`
- `POST /stores/{storeId}/images`
- `PUT /stores/{storeId}/images/{imageId}`
- `DELETE /stores/{storeId}/images/{imageId}`

**코드**: ✅ 완벽하게 일치 (StoreImageController)

### 3. Store 상세 조회 응답 (StoreResponse) ✅

**문서**: `images` 배열 포함, `imageUrl` (대표 이미지 URL)  
**코드**: ✅ 완벽하게 일치

```java
public record StoreResponse(
    // ... 기타 필드 ...
    String imageUrl,
    List<StoreImageResponse> images,
    LocalDateTime registeredAt
)
```

### 4. Category API ✅

**문서**: GET, GET(detail), POST, PUT, DELETE  
**코드**: ✅ 완벽하게 일치

### 5. Group API ✅

**문서**: GET, GET(detail), POST, PUT, DELETE  
**코드**: ✅ 완벽하게 일치

### 6. Policy API ✅

**문서**: GET, GET(detail), POST, PUT, DELETE, PATCH  
**코드**: ✅ 완벽하게 일치

### 7. Store 영업시간/임시휴무 API ✅

**문서**: GET, POST, PUT, DELETE  
**코드**: ✅ 완벽하게 일치

---

## 📊 종합 분석

### 불일치 심각도 분포

```
🔴 심각 (Critical): 2개
  ├─ Store Update 요청 필드 (latitude, longitude, imageUrl)
  └─ Food API 경로 구조

🟡 중간 (Medium): 1개
  └─ Food DELETE 응답 상태 코드

✅ 일치: 7개 API 그룹
```

### 권장 우선순위

| 우선순위 | 항목 | 이유 |
|---------|------|------|
| 🥇 1순위 | **Food API 경로 수정** | 클라이언트가 404 에러 경험, API 계약 위반 |
| 🥈 2순위 | **Store Update 필드 수정** | 기존 데이터 손상 위험 |
| 🥉 3순위 | **Food DELETE 상태 코드** | REST 컨벤션 준수 |

---

## 📋 액션 아이템

### Phase 1: 긴급 수정 (오늘)

- [ ] **Food API 경로 변경**
  - FoodController를 `/stores/{storeId}/foods` 구조로 변경
  - CreateFoodRequest에서 `storeId` 필드 제거
  - Path Variable로 storeId 전달받도록 수정

- [ ] **Store Update Request 필드 정리**
  - UpdateStoreRequest에서 `latitude`, `longitude`, `imageUrl` 필드 제거
  - StoreController.updateStore() 메서드 수정

### Phase 2: 테스트 및 검증 (내일)

- [ ] Food API 경로 변경 후 통합 테스트 실행
- [ ] StoreControllerTest 수정 및 실행
- [ ] API 명세 문서와 코드 최종 비교

### Phase 3: 문서 업데이트 (내일)

- [ ] ADMIN_API_SPECIFICATION.md 수정 사항 반영
- [ ] Food DELETE 응답 상태 코드 명시 (204 No Content)
- [ ] 변경 이력 추가 (v2.0.1)

---

## 참고

- 현재 검토 기준: ADMIN_API_SPECIFICATION.md v2.0 (2025-11-07)
- 검토 범위: Controller 계층 및 요청/응답 DTO
- 미검토: Service, Domain, Storage 계층 (요청 사항이 있으면 별도 검토)
