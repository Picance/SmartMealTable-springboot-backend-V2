# API Specification 개선 보고서

**작성일:** 2025-10-10  
**버전:** v1.1

---

## 개요

와이어프레임, SRD(시스템 요구사항 문서), SRS(소프트웨어 요구사항 명세서), 그리고 기존 API Specification을 종합적으로 검토한 결과, 발견된 상충 및 불일치 사항을 해결하였습니다.

---

## 1. 즐겨찾기 목록 필터링 및 정렬 기능 추가 ✅

### 문제점
- **요구사항 (SRD)**: [REQ-FAV-402], [REQ-FAV-403]에서 '별점 높은 순', '이름 가나다 순' 등 다양한 정렬 기능과 '영업 중인 곳만 보기', '음식 종류' 필터 기능을 명확하게 요구
- **기존 API**: `GET /api/v1/favorites` 엔드포인트에 정렬/필터링 파라미터 없음

### 해결책

**API 엔드포인트:** `GET /api/v1/favorites`

**추가된 Query Parameters:**
```
?sortBy=displayOrder          # 정렬 기준
&isOpenOnly=false              # 영업 중만 보기
&categoryId=5                  # 카테고리 필터
&page=0&size=20                # 페이징
```

**정렬 옵션 (sortBy):**
- `displayOrder`: 사용자 지정 순서 (기본값)
- `name`: 이름 가나다순
- `rating`: 별점 높은 순
- `reviewCount`: 리뷰 많은 순
- `distance`: 거리 순
- `createdAt`: 최근 추가 순

**필터링 옵션:**
- `isOpenOnly`: true면 현재 영업 중인 가게만 조회
- `categoryId`: 특정 카테고리 필터링

**응답 구조 개선:**
```json
{
  "result": "SUCCESS",
  "data": {
    "favorites": [...],
    "totalCount": 15,
    "openCount": 12,           // 현재 영업 중인 가게 수
    "page": 0,
    "size": 20,
    "totalPages": 1
  }
}
```

**추가된 필드:**
- `distance`: 사용자 현재 위치 기준 거리 (km)
- `rating`: 별점
- `isOpenNow`: 현재 영업 여부
- `openCount`: 전체 즐겨찾기 중 현재 영업 중인 가게 수

---

## 2. 개별 음식 선호도(좋아요/싫어요) API 추가 ✅

### 문제점
- **요구사항**: 카테고리 선호도 외에 개별 음식에 대한 좋아요/싫어요 기능 필요
- **기존 API**: 카테고리 선호도만 관리, 개별 음식 선호도 API 누락

### 해결책

**1) 선호도 조회 API 개선**

**기존:** `GET /api/v1/members/me/preferences`

**개선된 응답:**
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
      }
    ],
    "foodPreferences": {
      "liked": [
        {
          "foodPreferenceId": 801,
          "foodId": 12,
          "foodName": "김치찌개",
          "categoryName": "한식"
        }
      ],
      "disliked": [
        {
          "foodPreferenceId": 802,
          "foodId": 35,
          "foodName": "생굴",
          "categoryName": "일식"
        }
      ]
    }
  }
}
```

**2) 신규 API 추가**

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| 카테고리 선호도 수정 | PUT | `/api/v1/members/me/preferences/categories` | 카테고리 선호도만 수정 |
| 개별 음식 선호도 추가 | POST | `/api/v1/members/me/preferences/foods` | 특정 음식 좋아요/싫어요 추가 |
| 개별 음식 선호도 변경 | PUT | `/api/v1/members/me/preferences/foods/{foodPreferenceId}` | 좋아요 ↔ 싫어요 변경 |
| 개별 음식 선호도 삭제 | DELETE | `/api/v1/members/me/preferences/foods/{foodPreferenceId}` | 선호도 제거 |

**요청 예시 (음식 선호도 추가):**
```json
{
  "foodId": 12,
  "isPreferred": true    // true=좋아요, false=싫어요
}
```

---

## 3. 홈 대시보드 주소 처리 방식 변경 ✅

### 문제점
- **기존 설계**: GPS 좌표를 쿼리 파라미터로 받아서 동적으로 위치 기반 추천 제공
  ```
  GET /api/v1/home/dashboard?latitude={lat}&longitude={lng}
  ```
- **의도된 설계**: 사용자가 `address_history`에서 `is_primary=true`로 설정한 주소를 기준으로 고정된 추천 제공

### 해결책

**변경된 API:**

**Endpoint:** `GET /api/v1/home/dashboard` (쿼리 파라미터 제거)

**동작 방식:**
1. 사용자의 기본 주소(`is_primary=true`)를 조회
2. 기본 주소가 없으면 `404 Error` 반환
3. 기본 주소의 좌표를 기준으로 추천 제공

**응답 구조:**
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
    "budget": {...},
    "recommendedMenus": [...],
    "recommendedStores": [...]
  }
}
```

**에러 처리 (기본 주소 없음):**
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

**장점:**
- 일관된 사용자 경험 제공
- 서버 측 캐싱 가능
- 위치 기준 명확화 (항상 기본 주소 사용)
- 클라이언트의 GPS 권한 문제 회피

**위치 변경 방법:**
- 기본 주소 변경: `PUT /api/v1/members/me/addresses/{addressHistoryId}/primary`

---

## 4. GPS 기반 주소 등록 프로세스 명확화 ✅

### 문제점
- **요구사항**: [REQ-ONBOARD-203b] GPS 기반 주소 등록 기능
- **기존 API**: 프로세스 및 역할 분담 불명확

### 해결책

**클라이언트-서버 협력 프로세스 정의:**

```
[Client] 1. '현재 위치로 찾기' 버튼 클릭
         ↓
[Client] 2. 기기 OS로부터 GPS 좌표(lat, lng) 획득
         ↓
[Client] 3. 지도에 좌표 표시, 마커(핀) 표시
         ↓
[Client] 4. 사용자가 마커 드래그로 위치 미세 조정
         ↓
[Client] 5. 위치 확정 후 API 호출
         ↓
[Server] 6. GET /api/v1/maps/reverse-geocode?lat={lat}&lng={lng}
         ↓
[Server] 7. 네이버 지도 API로 좌표 → 주소 변환
         ↓
[Server] 8. 구조화된 주소 데이터 응답
         ↓
[Client] 9. 응답받은 주소를 입력 필드에 자동 채움
         ↓
[Client] 10. 사용자가 상세 주소(동/호수) 입력
         ↓
[Client] 11. 완성된 주소로 API 호출
         ↓
[Server] 12. POST /api/v1/onboarding/address (온보딩)
         또는 POST /api/v1/members/me/addresses (일반)
         ↓
[Server] 13. 주소 저장 및 응답
```

**관련 API 개선:**

**1) Reverse Geocoding API 에러 처리 추가**

**Endpoint:** `GET /api/v1/maps/reverse-geocode`

**추가된 에러 케이스:**
```json
// 400 - 잘못된 좌표
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

// 503 - 외부 API 오류
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "EXTERNAL_001",
    "message": "주소 변환 서비스에 일시적인 오류가 발생했습니다."
  }
}
```

**2) 응답 필드 추가**

기존 응답에 추가:
```json
{
  "buildingName": "테헤란빌딩",
  "sigunguCode": "11680",
  "bcode": "1168010100"
}
```

**클라이언트 구현 가이드:**

- **GPS 권한 확인 및 요청**
  - iOS: `NSLocationWhenInUseUsageDescription`
  - Android: `ACCESS_FINE_LOCATION`
  
- **위치 정보 취득 실패 처리**
  - 권한 거부 시 안내 메시지
  - GPS 비활성화 시 설정 유도
  
- **네트워크 오류 재시도**
  - 최대 3회 재시도
  - 지수 백오프 적용
  
- **지도 라이브러리**
  - 네이버 지도 SDK 권장
  - 마커 드래그 이벤트 처리

---

## 5. Deprecated API 정리

### 13.4 현재 위치 기준 변경 API (DEPRECATED)

**Endpoint:** `PUT /api/v1/members/me/current-location`

**상태:** ⚠️ DEPRECATED

**사유:**
- 홈 대시보드가 항상 기본 주소를 기준으로 동작하도록 변경
- 위치 기준의 일관성 확보

**대체 API:**
- `PUT /api/v1/members/me/addresses/{addressHistoryId}/primary` (10.7 기본 주소 설정)

---

## 6. 추가 개선 사항 및 남은 이슈

### 6.1 발견된 추가 애매모호한 사항

#### ✅ 해결 완료
1. 즐겨찾기 정렬/필터 기능 누락
2. 개별 음식 선호도 API 누락
3. 홈 대시보드 위치 기준 불명확
4. GPS 주소 등록 프로세스 불명확

#### 🔍 검토 필요 (명확화 요청)

**1) 추천 시스템 실시간 위치 활용 여부**

**현재 설계:**
- 홈 대시보드: 기본 주소 기준
- 가게 목록 조회 (`GET /api/v1/stores`): GPS 좌표 쿼리 파라미터 허용
- 추천 API (`POST /api/v1/recommendations`): GPS 좌표 옵션 허용

**질문:**
- 가게 목록 및 추천 API에서 GPS 좌표를 허용하는 이유는?
  - A) 사용자가 외출 중 다른 지역에서 검색할 수 있도록
  - B) 실시간 위치 기반 추천 제공
  - C) 기타

**제안:**
- 홈 대시보드: 기본 주소 고정 ✅
- 가게 검색: GPS 선택적 허용 (기본값: 기본 주소)
- 추천: GPS 선택적 허용 (기본값: 기본 주소)

---

**2) 가게 조회 이력 (`store_view_history`) 기록 시점**

**현재 명세:**
- 가게 상세 조회 시 자동 기록

**질문:**
- 가게 목록에서 카드를 터치했을 때도 기록해야 하는가?
- 상세 화면 진입 시점에만 기록하는가?

**제안:**
- 상세 화면 진입 시점에만 기록 (현재 명세 유지)
- 이유: 실제 관심도를 더 정확히 측정

---

**3) 장바구니 유효성 검증**

**현재 명세:**
- 다른 가게 상품 추가 시 409 에러
- 클라이언트가 장바구니를 먼저 비워야 함

**질문:**
- 서버에서 자동으로 기존 장바구니를 비우고 새 상품을 추가하는 옵션을 제공할까?
  - 예: `?replaceCart=true` 파라미터

**제안:**
- 현재 명세 유지 (명시적 사용자 확인 필요)
- 장바구니 충돌 시 클라이언트가 사용자에게 확인 후 `DELETE /api/v1/cart` → `POST /api/v1/cart/items` 순차 호출

---

**4) 음식 이미지 URL 관리**

**현재 명세:**
- `imageUrl` 필드로 CDN URL 제공
- 예: `https://cdn.smartmealtable.com/foods/201.jpg`

**질문:**
- 이미지 업로드 API는?
- 이미지 크기별 최적화 URL 제공 여부?
  - 예: `thumbnailUrl`, `fullImageUrl`

**제안:**
- 별도 이미지 업로드 API 추가 필요 (`POST /api/v1/images/upload`)
- 응답에 여러 크기 URL 제공:
  ```json
  {
    "imageUrls": {
      "thumbnail": "https://cdn.../foods/201-thumb.jpg",
      "medium": "https://cdn.../foods/201-medium.jpg",
      "original": "https://cdn.../foods/201.jpg"
    }
  }
  ```

---

**5) 예산 초과 시 알림/경고 처리**

**현재 명세:**
- 예산 정보 조회만 가능
- 초과 여부는 클라이언트가 판단

**질문:**
- 예산 초과 시 푸시 알림 전송 여부?
- 지출 등록 시 예산 초과 경고 응답 포함 여부?

**제안:**
- 지출 등록 시 예산 초과 경고 플래그 추가:
  ```json
  {
    "budgetWarnings": {
      "mealBudgetExceeded": true,
      "dailyBudgetExceeded": false,
      "monthlyBudgetNearLimit": true
    }
  }
  ```

---

## 7. 에러 코드 추가

| 에러 코드 | HTTP Status | 설명 |
|-----------|-------------|------|
| `ADDRESS_002` | 404 | 등록된 주소가 없음 |

---

## 8. 변경 이력

| 날짜 | 버전 | 변경 사항 |
|------|------|-----------|
| 2025-10-10 | v1.1 | - 즐겨찾기 필터/정렬 추가<br>- 개별 음식 선호도 API 추가<br>- 홈 대시보드 주소 기준 명확화<br>- GPS 주소 등록 프로세스 정의<br>- 현재 위치 기준 변경 API deprecated |
| 2025-10-08 | v1.0 | 최초 작성 |

---

## 9. 다음 단계

### 즉시 조치 필요 ✅
1. ✅ 즐겨찾기 API 구현
2. ✅ 개별 음식 선호도 API 구현
3. ✅ 홈 대시보드 기본 주소 로직 구현
4. ✅ GPS 주소 등록 프로세스 문서화

### 추가 논의 필요 🔍
1. 실시간 GPS 위치 활용 범위 결정
2. 장바구니 자동 교체 옵션 검토
3. 이미지 업로드 API 설계
4. 예산 초과 알림 정책 결정
5. 가게 조회 이력 기록 정책 명확화

---

**문서 작성자:** GitHub Copilot  
**검토 상태:** ✅ 완료  
**승인 대기:** 개발팀 리뷰
