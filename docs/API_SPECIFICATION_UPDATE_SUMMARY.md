# API_SPECIFICATION.md 업데이트 요약

**작성일**: 2025-10-08  
**변경 범위**: 커서 기반 페이징 도입 및 주소 API isPrimary 필드 제거

## 📋 변경 사항 상세

### 1. 가게 목록 조회 API (7.1)

#### 변경 전
- **HTTP Method**: GET
- **페이징**: 오프셋 기반만 지원 (`page`, `size`)
- **응답 필드**: `content`, `pageable`, `totalElements`

#### 변경 후
- **HTTP Method**: GET (동일)
- **페이징**: 커서 기반 + 오프셋 기반 (하위 호환성 유지)
  - 커서 기반 (권장): `lastId` + `limit` 사용
  - 오프셋 기반: `page` + `size` 사용
- **응답 필드 추가**:
  ```json
  {
    "stores": [],           // 기존 content
    "totalCount": 45,
    "currentPage": 0,
    "pageSize": 20,
    "totalPages": 3,
    "hasMore": true,        // 다음 데이터 존재 여부
    "lastId": 101           // 마지막 항목 ID (커서)
  }
  ```

#### Query Parameters 변경

**커서 기반 페이징 (권장)**
```
GET /api/v1/stores?keyword=치킨&lastId=101&limit=20
```
- `lastId` (optional): 이전 응답의 마지막 가게 ID (첫 요청시 생략)
- `limit` (1-100, 기본값: 20): 조회할 개수

**오프셋 기반 페이징 (기존, 하위 호환성)**
```
GET /api/v1/stores?keyword=치킨&page=0&size=20
```
- `page` (기본값: 0)
- `size` (1-100, 기본값: 20)

#### 추가 note
- 커서 기반 페이징 사용 시 무한 스크롤 구현 가능
- `radius` 파라미터 범위 확대: 0.5 ~ 50 km (기본값: 3.0)

---

### 2. 추천 목록 조회 API (9.1)

#### 변경 전
- **HTTP Method**: POST (Body에서 파라미터 전달)
- 요청 DTO 필요
- 사용자의 기본 주소 기반 추천 (위도/경도 미지정)

#### 변경 후
- **HTTP Method**: GET (Query Parameters 사용)
- **위도/경도 필수**: `latitude` + `longitude` 필수 파라미터
- **페이징**: 커서 기반 + 오프셋 기반 모두 지원
- **응답 구조**: 배열 형태 (기존 wrapper 제거)
  ```json
  [
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
  ]
  ```

#### Query Parameters 변경

**필수 파라미터**
- `latitude`: 현재 위도 (-90 ~ 90)
- `longitude`: 현재 경도 (-180 ~ 180)

**선택 파라미터**
- `radius` (0.1 ~ 10 km, 기본값: 0.5): 검색 반경
- `sortBy` (SCORE, reviewCount, distance, 기본값: SCORE): 정렬 기준
- `includeDisliked` (기본값: false): 불호 음식 포함 여부
- `openNow` (기본값: false): 영업 중인 가게만
- `storeType` (ALL, CAMPUS_RESTAURANT, RESTAURANT, 기본값: ALL)

**커서 기반 페이징**
- `lastId` (optional): 이전 응답의 마지막 항목 ID
- `limit` (1-100, 기본값: 20)

**오프셋 기반 페이징**
- `page` (기본값: 0)
- `size` (1-100, 기본값: 20)

---

### 3. 온보딩 주소 등록 API (4.2)

#### 변경 전
```json
{
  "addressAlias": "우리집",
  "addressType": "HOME",
  "streetNameAddress": "서울특별시 강남구 테헤란로 123",
  "lotNumberAddress": "서울특별시 강남구 역삼동 456-78",
  "detailedAddress": "101동 1234호",
  "latitude": 37.497942,
  "longitude": 127.027621,
  "isPrimary": true  // ← 필수 필드
}
```

#### 변경 후
```json
{
  "addressAlias": "우리집",
  "addressType": "HOME",
  "streetNameAddress": "서울특별시 강남구 테헤란로 123",
  "lotNumberAddress": "서울특별시 강남구 역삼동 456-78",
  "detailedAddress": "101동 1234호",
  "latitude": 37.497942,
  "longitude": 127.027621
  // isPrimary 필드 제거
}
```

#### 변경 이유
- 첫 번째로 등록된 주소는 자동으로 기본 주소로 설정
- 명시적인 요청 필드 제거로 단순화

#### Note 추가
```
첫 번째로 등록된 주소는 자동으로 기본 주소(primary address)로 설정됩니다.
```

---

### 4. 프로필 API - 주소 추가 (10.4)

#### 변경 전
```
**Request/Response:** 온보딩 주소 등록과 동일
```

#### 변경 후
명시적인 예제 추가

**Request**
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

**Response (201)**
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

**Note**
```
- 첫 번째 주소 등록 시 자동으로 기본 주소로 설정됩니다.
- 추가 주소 등록 시 기본 주소 설정은 별도의 API를 이용합니다.
```

---

### 5. 프로필 API - 주소 수정 (10.5)

#### 변경 전
```
**Request/Response:** 온보딩 주소 등록과 동일
```

#### 변경 후
명시적인 예제 추가

**Request**
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

**Response (200)**
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

## 📊 영향 범위

### API 엔드포인트
- `GET /api/v1/stores` ✅ 커서 페이징 지원
- `GET /api/v1/recommendations` ✅ 커서 페이징 지원 + HTTP 메서드 변경
- `POST /api/v1/onboarding/address` ✅ isPrimary 필드 제거
- `POST /api/v1/members/me/addresses` ✅ isPrimary 필드 제거
- `PUT /api/v1/members/me/addresses/{id}` ✅ isPrimary 필드 제거

### 클라이언트 변경 필요
1. **가게 목록 조회**
   - 기존 `page` + `size` → `lastId` + `limit` 변경 (권장)
   - 또는 기존 방식 유지 (하위 호환성)

2. **추천 목록 조회**
   - POST → GET 변경
   - Body 파라미터 → Query Parameters 변경
   - 위도/경도 필수 추가

3. **주소 등록/수정**
   - 요청 DTO에서 `isPrimary` 필드 제거
   - 응답은 `isPrimary` 포함 (변경 없음)

### 서버 코드 상태
- ✅ StoreController: 커서 기반 페이징 구현 완료
- ✅ RecommendationController: GET 메서드 및 커서 페이징 구현 완료
- ✅ AddressController: isPrimary 필드 제거 완료
- ✅ StoreListResponse: 커서 페이징 응답 DTO 구현 완료

---

## 📝 REST Docs 업데이트 사항

### 문서 통계
- **파일**: `docs/API_SPECIFICATION.md`
- **변경 라인**: 164 insertions(+), 69 deletions(-)
- **총 변경**: 233 라인 수정

---

## ✅ 검증 사항

### 코드 검증 완료
- [x] StoreController 구현 확인
- [x] RecommendationController 구현 확인
- [x] AddressRequest DTO 확인 (isPrimary 제거됨)
- [x] StoreListResponse 커서 페이징 응답 확인

### 문서 검증 완료
- [x] 모든 변경사항 문서에 반영
- [x] 요청/응답 예제 업데이트
- [x] Query Parameters 명시
- [x] Note 추가로 명확성 강화

---

## 🔗 관련 파일
- `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/store/controller/StoreController.java`
- `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/recommendation/controller/RecommendationController.java`
- `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/member/controller/AddressController.java`
- `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/store/dto/StoreListResponse.java`

---

## 📌 주의사항

1. **하위 호환성**: 가게 목록 조회는 기존 `page`/`size` 파라미터도 계속 지원합니다.
2. **기본 주소 자동 설정**: 첫 번째 주소 등록 시 자동으로 기본 주소가 되므로 클라이언트에서 `isPrimary` 전달 불가능합니다.
3. **응답의 isPrimary**: 응답 DTO에서는 여전히 `isPrimary` 필드가 포함되어 클라이언트가 기본 주소 여부를 인식할 수 있습니다.

---

**최종 커밋**: `docs: API 스펙 문서 업데이트 - 커서 페이징 및 isPrimary 필드 제거`
