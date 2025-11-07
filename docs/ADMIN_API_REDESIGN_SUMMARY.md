# 📋 ADMIN API 재설계 완료 보고서

**작성일**: 2025-11-07  
**버전**: v2.0  
**관련 문서**: `ADMIN_API_SPECIFICATION.md`

---

## 1. 변경 개요

### 1.1. 배경

다음 스키마 변경사항을 ADMIN API에 반영하기 위한 재설계:

1. **Food 테이블**: `is_main`, `display_order` 필드 추가
2. **StoreImage 테이블**: 신규 테이블 생성 (가게 이미지 다중 관리)
3. **주소 기반 지오코딩**: 클라이언트에서 좌표를 보내는 대신 서버에서 자동 처리

### 1.2. 목표

- **관리 편의성 향상**: 가게 이미지를 여러 장 등록하고 대표 이미지 설정
- **메뉴 관리 강화**: 대표 메뉴 표시 및 정렬 순서 제어
- **UX 개선**: 프론트엔드에서 좌표를 직접 입력하지 않아도 됨
- **데이터 정확성**: 서버에서 표준화된 지오코딩 API로 좌표 계산

---

## 2. 주요 변경사항

### 2.1. 🆕 신규 API

#### StoreImage 관리 API
| HTTP Method | URI | 설명 |
|-------------|-------------------------------------|------|
| `POST`      | `/stores/{storeId}/images`          | 가게 이미지 추가 |
| `PUT`       | `/stores/{storeId}/images/{imageId}`| 가게 이미지 수정 |
| `DELETE`    | `/stores/{storeId}/images/{imageId}`| 가게 이미지 삭제 |

**Request Body 예시**:
```json
{
  "imageUrl": "http://example.com/store-image.jpg",
  "isMain": true,
  "displayOrder": 1
}
```

**특징**:
- `isMain`: 대표 이미지 여부 (true 설정 시 기존 대표 이미지 자동 해제)
- `displayOrder`: 이미지 표시 순서 (낮을수록 우선)

---

### 2.2. ✏️ 수정된 API

#### 2.2.1. Store 생성/수정 API

**Before (v1.0)**:
```json
{
  "name": "스마트 식당",
  "categoryId": 1,
  "address": "서울시 강남구 테헤란로 123",
  "latitude": 37.12345,      // ❌ 제거됨
  "longitude": 127.12345,    // ❌ 제거됨
  "imageUrl": "http://..."   // ❌ 제거됨
}
```

**After (v2.0)**:
```json
{
  "name": "스마트 식당",
  "categoryId": 1,
  "address": "서울시 강남구 테헤란로 123",
  "lotNumberAddress": "서울시 강남구 역삼동 123-45"
  // 서버에서 address 기반으로 latitude, longitude 자동 계산
  // 이미지는 별도 API로 추가
}
```

**변경 사유**:
- **지오코딩 자동화**: 관리자가 좌표를 직접 입력할 필요 없음
- **이미지 분리 관리**: 다중 이미지 지원을 위해 별도 API로 분리

#### 2.2.2. Food (메뉴) 생성/수정 API

**Before (v1.0)**:
```json
{
  "foodName": "김치찌개",
  "averagePrice": 8000,
  "categoryId": 1
}
```

**After (v2.0)**:
```json
{
  "foodName": "김치찌개",
  "averagePrice": 8000,
  "categoryId": 1,
  "isMain": true,          // ✅ 추가됨
  "displayOrder": 1        // ✅ 추가됨
}
```

**변경 사유**:
- **대표 메뉴 표시**: 추천 메뉴 또는 시그니처 메뉴 강조
- **정렬 순서 제어**: 메뉴판에서 표시 순서 커스터마이징

#### 2.2.3. Store 상세 조회 API

**Response 변경**:
```json
{
  "result": "SUCCESS",
  "data": {
    "storeId": 101,
    "name": "스마트 식당",
    // ... 기존 필드
    "images": [              // ✅ 추가됨 (이미지 배열)
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
  }
}
```

#### 2.2.4. Food 목록 조회 API

**새로운 정렬 옵션**:
```
GET /stores/{storeId}/foods?sort=displayOrder,asc
GET /stores/{storeId}/foods?sort=isMain,desc
```

**Response 변경**:
```json
{
  "foods": [
    {
      "foodId": 201,
      "foodName": "김치찌개",
      "isMain": true,          // ✅ 추가됨
      "displayOrder": 1,       // ✅ 추가됨
      "isAvailable": true
    }
  ]
}
```

---

## 3. 기술적 구현 사항

### 3.1. 지오코딩 서비스

#### 선택한 API
- **Naver Maps API** (구현 완료)
  - 국내 주소 정확도 우수
  - 무료 할당량: 일 10만 건
  - RESTful API 제공
  - **주의**: API 모듈(`smartmealtable-api`)에서 이미 구현되어 있는 `MapService` 인터페이스와 `NaverMapClient` 구현체를 재사용할 것을 권장

#### 기존 구현 활용 (권장)

**아키텍처**:
```
smartmealtable-domain
  └── MapService (인터페이스)
       └── searchAddress(String keyword, Integer limit)
       └── reverseGeocode(BigDecimal latitude, BigDecimal longitude)

smartmealtable-client
  └── NaverMapClient (구현체)
       └── Naver Maps Geocoding API 호출

smartmealtable-admin
  └── StoreService
       └── MapService 의존성 주입
       └── 가게 생성/수정 시 자동 지오코딩
```

**구현 예시**:
```java
@Service
@RequiredArgsConstructor
public class StoreService {
    
    private final MapService mapService; // ✅ 의존성 주입
    private final StoreRepository storeRepository;
    
    public StoreResponse createStore(StoreCreateRequest request) {
        // 1. 주소로 좌표 검색 (Naver Maps Geocoding API)
        List<AddressSearchResult> results = mapService.searchAddress(request.getAddress(), 1);
        
        if (results.isEmpty()) {
            throw new InvalidAddressException("유효하지 않은 주소입니다: " + request.getAddress());
        }
        
        AddressSearchResult addressResult = results.get(0);
        
        // 2. Store 엔티티 생성 (좌표 자동 설정)
        Store store = Store.builder()
            .name(request.getName())
            .address(request.getAddress())
            .latitude(addressResult.latitude())   // ✅ 지오코딩 결과 사용
            .longitude(addressResult.longitude()) // ✅ 지오코딩 결과 사용
            .build();
        
        // 3. 저장 및 응답
        return StoreResponse.from(storeRepository.save(store));
    }
}
```

#### 새로운 구현 (필요 시)

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

#### 에러 처리
| 에러 케이스 | HTTP Status | Error Code |
|------------|-------------|------------|
| 주소 없음   | 400         | INVALID_ADDRESS |
| API 장애    | 503         | GEOCODING_SERVICE_UNAVAILABLE |

### 3.2. 대표 이미지 자동 관리

#### 로직
```java
// 새로운 이미지를 대표 이미지로 설정 시
if (request.isMain()) {
    // 1. 기존 대표 이미지 찾기
    Optional<StoreImage> existingMain = storeImageRepository
        .findByStoreIdAndIsMainTrue(storeId);
    
    // 2. 기존 대표 이미지의 isMain을 false로 변경
    existingMain.ifPresent(image -> image.setIsMain(false));
    
    // 3. 새 이미지를 대표 이미지로 설정
    newImage.setIsMain(true);
}
```

### 3.3. 메뉴 정렬 우선순위

```java
// 메뉴 조회 시 정렬 순서
public List<Food> getStoreFoods(Long storeId, String sort) {
    return foodRepository.findByStoreIdAndDeletedAtIsNull(storeId)
        .stream()
        .sorted(Comparator
            .comparing(Food::getIsMain, Comparator.reverseOrder())  // 대표 메뉴 우선
            .thenComparing(Food::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(Food::getFoodId))
        .collect(Collectors.toList());
}
```

---

## 4. 데이터 마이그레이션

### 4.1. 기존 Store 이미지 마이그레이션

```sql
-- Store의 기존 image_url을 StoreImage로 이전
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

### 4.2. 기존 좌표 데이터 검증

```sql
-- 좌표가 없는 가게 찾기
SELECT store_id, name, address
FROM store
WHERE latitude IS NULL OR longitude IS NULL;

-- 해당 가게들의 주소로 지오코딩 재처리 필요
```

---

## 5. 영향도 분석

### 5.1. 프론트엔드 (Admin Dashboard)

#### 필요한 변경사항
1. **가게 생성/수정 폼**:
   - ❌ 위도/경도 입력 필드 제거
   - ❌ 이미지 URL 입력 필드 제거
   - ✅ 주소 입력만 필수 (지오코딩 자동)
   - ✅ 이미지 업로드는 가게 생성 후 별도 관리

2. **가게 상세 페이지**:
   - ✅ 이미지 갤러리 컴포넌트 추가
   - ✅ 대표 이미지 설정 토글 버튼
   - ✅ 이미지 순서 드래그 앤 드롭

3. **메뉴 관리 페이지**:
   - ✅ 대표 메뉴 체크박스 추가
   - ✅ 메뉴 순서 조정 UI
   - ✅ 드래그 앤 드롭으로 순서 변경

### 5.2. 백엔드 (API 모듈)

#### 영향 없음
- API 모듈(`smartmealtable-api`)은 조회 API만 제공하므로 영향 없음
- 단, 가게 상세 조회 응답에 `images` 배열이 추가되므로 프론트엔드 대응 필요

---

## 6. 테스트 계획

### 6.1. 단위 테스트

| 테스트 클래스 | 테스트 케이스 |
|--------------|-------------|
| `StoreServiceTest` | - 가게 생성 시 지오코딩 자동 호출 검증<br>- 주소 변경 시 좌표 재계산 검증<br>- 유효하지 않은 주소 입력 시 예외 발생 |
| `StoreImageServiceTest` | - 이미지 추가 시 대표 이미지 자동 변경<br>- 이미지 삭제 시 정렬 순서 재조정 |
| `FoodServiceTest` | - 대표 메뉴 설정/해제<br>- 메뉴 정렬 우선순위 검증 |

### 6.2. 통합 테스트

| 테스트 클래스 | 테스트 케이스 |
|--------------|-------------|
| `StoreControllerTest` | - POST /stores (지오코딩 성공/실패)<br>- GET /stores/{id} (이미지 배열 포함) |
| `StoreImageControllerTest` | - POST /stores/{id}/images<br>- PUT /stores/{id}/images/{imageId}<br>- DELETE /stores/{id}/images/{imageId} |
| `FoodControllerTest` | - POST /stores/{id}/foods (isMain, displayOrder)<br>- GET /stores/{id}/foods?sort=isMain,desc |

---

## 7. 배포 계획

### 7.1. 사전 준비

1. **환경 변수 설정**:
   ```bash
   # Naver Maps API 키 설정
   export NAVER_MAP_CLIENT_ID=your_client_id
   export NAVER_MAP_CLIENT_SECRET=your_client_secret
   ```

2. **데이터 마이그레이션**:
   - 기존 `store.image_url` → `store_image` 테이블 이전
   - 좌표 없는 가게 목록 확인 및 재처리

3. **API 모듈 의존성 확인**:
   - `MapService` 인터페이스가 admin 모듈에서 접근 가능한지 확인
   - `NaverMapClient` 구현체가 Bean으로 등록되어 있는지 확인

### 7.2. 배포 순서

1. **백엔드 배포** (smartmealtable-admin)
   - 지오코딩 서비스 활성화
   - 신규 API 엔드포인트 배포

2. **프론트엔드 배포** (Admin Dashboard)
   - 가게/메뉴 관리 UI 업데이트
   - 이미지 갤러리 컴포넌트 추가

3. **데이터 검증**:
   - 기존 가게의 좌표 정확성 확인
   - 이미지 마이그레이션 완료 여부 확인

---

## 8. 체크리스트

### 8.1. 개발
- [ ] `StoreImage` 도메인 엔티티 생성
- [ ] `StoreImageRepository` 인터페이스 및 구현체
- [ ] `MapService` 의존성 주입 확인 (API 모듈 재사용)
- [ ] `StoreService` 지오코딩 로직 추가
- [ ] `StoreImageService` 생성 (CRUD)
- [ ] `FoodService` 정렬 로직 추가
- [ ] `StoreImageController` 생성
- [ ] `StoreController`, `FoodController` 수정
- [ ] Request/Response DTO 생성 및 수정

### 8.2. 테스트
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] Spring Rest Docs 업데이트

### 8.3. 배포
- [ ] 데이터 마이그레이션 스크립트 작성 및 검증
- [ ] 환경 변수 설정 (Naver Maps API Key)
- [ ] 프로덕션 배포
- [ ] 데이터 검증
- [ ] 지오코딩 API 호출 모니터링 (일 10만 건 제한 확인)

---

## 9. 참고 문서

- [ADMIN_API_SPECIFICATION.md](./ADMIN_API_SPECIFICATION.md) - 업데이트된 API 명세
- [API_REDESIGN_FOOD_AND_STORE_IMAGE.md](./API_REDESIGN_FOOD_AND_STORE_IMAGE.md) - API 모듈 재설계
- [ddl.sql](../ddl.sql) - 데이터베이스 스키마

---

## 10. 버전 히스토리

| 버전 | 날짜 | 변경 내용 |
|------|------|----------|
| v2.0 | 2025-11-07 | Food `is_main`, `display_order` 추가<br>StoreImage 다중 관리<br>지오코딩 자동화 |
| v1.0 | 2025-11-05 | 초기 ADMIN API 명세 |
