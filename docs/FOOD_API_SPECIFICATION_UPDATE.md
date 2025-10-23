# 음식 API 명세 업데이트 완료

**업데이트 일시**: 2025-10-23 14:00  
**상태**: ✅ **완료**

---

## 📋 개요

`/api/v1/foods` 엔드포인트에 대한 완전한 API 명세가 추가되었습니다.

기존에는 음식 관련 기능이 구현되어 있었으나, API 명세 문서에서 독립적인 섹션으로 체계화되지 않았습니다.
본 업데이트로 음식 API의 모든 엔드포인트가 명확하게 문서화되었습니다.

---

## 🎯 추가된 내용

### 1. 새로운 API 명세 섹션: Section 8 "음식 API"

#### 8.1 메뉴 상세 조회
- **엔드포인트**: `GET /api/v1/foods/{foodId}`
- **인증**: JWT 토큰 필수
- **설명**: 특정 메뉴의 상세 정보를 조회합니다.
- **주요 기능**:
  - 메뉴 기본 정보 (ID, 이름, 설명, 가격, 이미지)
  - 해당 가게 정보 (ID, 이름, 주소, 연락처, 카테고리)
  - 사용자 예산과의 비교 정보 (예산 대비 차이, 초과 여부)

**응답 필드**:
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

**에러 응답**:
- `404 Not Found`: 메뉴를 찾을 수 없음
- `401 Unauthorized`: 인증 토큰 없음 또는 유효하지 않음

---

#### 8.2 가게의 메뉴 목록 (가게 상세 조회에 포함)
- **엔드포인트**: `GET /api/v1/stores/{storeId}`
- **설명**: 가게 상세 정보 조회 시 해당 가게의 모든 메뉴 목록이 포함됩니다.
- **메뉴 리스트 필드**: `menus` 배열에 포함
- **메뉴 정보 항목**:
  - `foodId`: 메뉴 ID
  - `name`: 메뉴 이름
  - `storeId`: 판매 가게 ID
  - `categoryId`: 음식 카테고리 ID
  - `description`: 메뉴 설명
  - `imageUrl`: 메뉴 이미지 URL
  - `averagePrice`: 메뉴 평균 가격

**사용 예시**:
```bash
curl -X GET "https://api.smartmealtable.com/api/v1/foods/201" \
  -H "Authorization: Bearer {access_token}" \
  -H "Content-Type: application/json"
```

---

### 2. API 명세 문서 구조 개선

#### 목차 업데이트
기존 14개 섹션에서 15개 섹션으로 확장:

| 번호 | 섹션명 | 상태 |
|------|--------|------|
| 1 | 개요 | - |
| 2 | 공통 사항 | - |
| 3 | 인증 및 회원 관리 API | ✅ |
| 4 | 온보딩 API | ✅ |
| 5 | 예산 관리 API | ✅ |
| 6 | 지출 내역 API | ✅ |
| 7 | 가게 관리 API | ✅ |
| **8** | **음식 API** | **✅ NEW** |
| 9 | 추천 시스템 API | ✅ |
| 10 | 즐겨찾기 API | ✅ |
| 11 | 프로필 및 설정 API | ✅ |
| 12 | 홈 화면 API | ✅ |
| 13 | 장바구니 API | ✅ |
| 14 | 지도 및 위치 API | ✅ |
| 15 | 알림 및 설정 API | ✅ |

#### 섹션 번호 정렬
- 기존 Section 8 (추천 시스템 API) → Section 9
- 기존 Section 9 (즐겨찾기 API) → Section 10
- ... (모두 1씩 증가)
- 기존 Section 14 (알림 및 설정 API) → Section 15

---

### 3. 상세 명세 추가 항목

각 엔드포인트마다 다음 정보가 추가됨:

#### 요청 정보
- Path Parameters
- Query Parameters
- Request Header (인증 정보 포함)
- Request Body (해당 시 제공)

#### 응답 정보
- 성공 응답 (200 OK) - 완전한 JSON 예시
- 각 필드별 상세 설명
- 데이터 타입 명시

#### 에러 처리
- HTTP 상태 코드별 에러 응답
- ErrorCode 및 메시지
- 에러 필드 상세 정보

#### 예제
- 실제 curl 커맨드 예시
- 요청/응답 샘플

---

## 🔍 음식 API 구현 상황

### 코드 구현 현황

#### 1. Controller 계층
```
smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/food/controller/FoodController.java
```
- `GET /api/v1/foods/{foodId}` → `getFoodDetail()`

#### 2. Service 계층
```
smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/store/service/StoreService.java
```
- `getFoodDetail()` 메서드 구현

#### 3. Domain 계층
```
smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/food/FoodRepository.java
smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/food/FoodService.java
```

#### 4. Storage 계층
```
smartmealtable-storage/src/main/java/com/stdev/smartmealtable/storage/db/.../FoodEntity.java
smartmealtable-storage/src/main/java/com/stdev/smartmealtable/storage/db/.../FoodRepositoryImpl.java
```

---

## 📊 관련 엔드포인트

### 직접 음식 조회
- ✅ `GET /api/v1/foods/{foodId}` - 메뉴 상세 조회
- ✅ `GET /api/v1/stores/{storeId}` - 가게 상세 조회 (메뉴 목록 포함)

### 음식 관련 보조 기능
- ✅ `GET /api/v1/onboarding/foods` - 온보딩용 음식 목록 (Section 4.9)
- ✅ `POST /api/v1/onboarding/food-preferences` - 음식 선호도 저장 (Section 4.10)
- ✅ `PUT /api/v1/members/me/food-preferences` - 음식 선호도 수정 (Section 11)
- ✅ `DELETE /api/v1/members/me/food-preferences/{foodId}` - 음식 선호도 삭제 (Section 11)

---

## 🎯 음식 API 엔드포인트 사용 시나리오

### 시나리오 1: 추천 결과에서 메뉴 보기
1. 사용자가 추천 식당 목록 조회 (`GET /api/v1/recommendations`)
2. 원하는 식당 상세 조회 (`GET /api/v1/stores/{storeId}`)
3. 메뉴 목록 확인 (응답에 포함)
4. 특정 메뉴 상세 보기 (`GET /api/v1/foods/{foodId}`)

### 시나리오 2: 온보딩 중 음식 선택
1. 온보딩용 음식 목록 조회 (`GET /api/v1/onboarding/foods`)
2. 선호하는 음식 선택
3. 선호도 저장 (`POST /api/v1/onboarding/food-preferences`)

### 시나리오 3: 장바구니 추가
1. 메뉴 상세 조회로 가격 및 예산 확인 (`GET /api/v1/foods/{foodId}`)
2. 예산 내인지 확인 (budgetComparison 필드)
3. 장바구니에 추가 (`POST /api/v1/carts`)

---

## 📝 문서 업데이트 파일

| 파일명 | 변경 사항 |
|--------|----------|
| `docs/API_SPECIFICATION.md` | 음식 API 섹션 추가, 전체 섹션 번호 업데이트 |
| `docs/IMPLEMENTATION_PROGRESS.md` | 최신 업데이트 내용 추가 |

---

## ✅ 완료 항목

- [x] 음식 API 명세 섹션 추가
- [x] 메뉴 상세 조회 엔드포인트 상세 명세
- [x] 가게 상세 조회 시 메뉴 목록 포함 명세
- [x] 요청/응답 예시 작성
- [x] 에러 케이스 문서화
- [x] 인증 정보 명시
- [x] 필드별 상세 설명
- [x] 섹션 번호 정렬
- [x] 목차 업데이트
- [x] 진행상황 문서 업데이트
- [x] Git 커밋

---

## 🔗 참고 자료

### 관련 구현 문서
- `docs/FOOD_LIST_FEATURE.md` - 음식 목록 조회 기능 구현 완료
- `docs/API_REDESIGN_SUMMARY.md` - API 리디자인 요약
- `smartmealtable-api/src/docs/asciidoc/index.adoc` - REST Docs

### API 엔드포인트
- Base URL: `/api/v1`
- 인증: JWT Bearer Token
- 응답 형식: JSON

---

## 📌 주의사항

1. **인증 필수**: 모든 음식 API 엔드포인트는 JWT 토큰 필수
2. **예산 정보**: `budgetComparison`은 사용자의 현재 예산 정보 기반
3. **메뉴 목록**: 개별 메뉴 조회가 아니라 가게 상세 조회에 포함됨
4. **응답 구조**: 모든 응답은 `ApiResponse<T>` 형식 준수

---

**업데이트 완료**: 2025-10-23 14:00  
**다음 작업**: 필요시 추가 음식 관련 기능 구현 (예: 음식 검색, 카테고리별 조회 등)
