# API Specification 최종 수정 보고서

**작성일:** 2025-10-10  
**버전:** v1.2

---

## 📋 최종 수정 사항 요약

### 0. ✅ 평점(rating) 데이터 제거
**문제:** 데이터베이스에 가게 평점(rating) 데이터가 존재하지 않음

**조치:**
- 모든 API 응답에서 `rating` 필드 제거
- 정렬 옵션에서 `rating` 제거
- 리뷰 개수(`reviewCount`)만 사용

**영향받은 API:**
- `GET /api/v1/favorites` - 즐겨찾기 목록 조회
- `GET /api/v1/stores` - 가게 목록 조회
- 기타 가게 정보 포함 API

---

### 1. ✅ 가게 검색/추천 위치 기준 명확화

**변경 전:**
- GPS 좌표를 쿼리 파라미터로 받음
- 예: `GET /api/v1/stores?latitude=37.497942&longitude=127.027621`

**변경 후:**
- 항상 **기본 주소(primary address)** 기준
- GPS 파라미터 제거
- 예: `GET /api/v1/stores?radius=0.5`

**영향받은 API:**
1. `GET /api/v1/stores` - 가게 목록 조회
2. `POST /api/v1/recommendations` - 개인화 추천
3. `GET /api/v1/favorites` - 즐겨찾기 목록
4. `GET /api/v1/home/dashboard` - 홈 대시보드

**에러 처리:**
- 기본 주소가 없으면 `404 Error (ADDRESS_002)` 반환

**기본 주소 관리 API (이미 존재):**
- ✅ `PUT /api/v1/members/me/addresses/{addressHistoryId}/primary` - 기본 주소 설정
- ✅ `GET /api/v1/members/me/addresses` - 주소 목록 조회
- ✅ `POST /api/v1/members/me/addresses` - 주소 추가
- ✅ `PUT /api/v1/members/me/addresses/{addressHistoryId}` - 주소 수정
- ✅ `DELETE /api/v1/members/me/addresses/{addressHistoryId}` - 주소 삭제

---

### 2. ✅ 가게 조회 이력 기록 시점 명확화

**기록 시점:** 
사용자가 가게 목록에서 가게 카드를 터치하여 **상세 페이지로 진입한 시점**

**API:** `GET /api/v1/stores/{storeId}`

**동작:**
1. 가게 상세 정보 조회
2. `store_view_history` 테이블에 조회 이력 기록
3. `store` 테이블의 `view_count` 1 증가

**명세 추가:**
```markdown
### 7.2 가게 상세 조회

**설명:**
- 가게 상세 정보를 조회합니다.
- 조회 시 `store_view_history` 테이블에 조회 이력이 자동으로 기록됩니다.
- `view_count`가 1 증가합니다.

**Note:** 
- 조회 이력 기록 시점: 사용자가 가게 목록에서 가게 카드를 터치하여 
  상세 페이지로 진입한 시점
```

---

### 3. ✅ 장바구니 자동 교체 옵션 추가

**신규 파라미터:** `replaceCart` (boolean, 기본값: false)

**Endpoint:** `POST /api/v1/cart/items`

**Request:**
```json
{
  "storeId": 101,
  "foodId": 201,
  "quantity": 2,
  "replaceCart": false,  // 신규 파라미터
  "options": [...]
}
```

**동작:**
- `replaceCart=false` (기본값): 
  - 다른 가게 상품이 있으면 409 에러 반환
  - 사용자에게 확인 요청
  
- `replaceCart=true`: 
  - 다른 가게 상품이 있어도 자동으로 기존 장바구니 비우고 새 상품 추가
  - `replacedCart: true` 응답

**개선된 409 에러 응답:**
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "CART_001",
    "message": "다른 가게의 상품이 장바구니에 있습니다. 기존 장바구니를 비우고 새로운 상품을 추가하시겠습니까?",
    "data": {
      "currentStoreId": 102,
      "currentStoreName": "다른집",
      "requestedStoreId": 101,
      "requestedStoreName": "맛있는집",
      "suggestion": "replaceCart=true로 재요청하거나 장바구니를 먼저 비워주세요."
    }
  }
}
```

**클라이언트 처리 플로우:**
1. `POST /api/v1/cart/items` 호출 (`replaceCart=false`)
2. 409 에러 수신
3. 사용자에게 확인 대화상자 표시
4. 사용자 확인 시 `replaceCart=true`로 재요청

---

### 4. ✅ 이미지 URL 관리

**처리 방식:** 크롤링 데이터 사용

**결론:**
- 별도 이미지 업로드 API 불필요
- CDN URL 직접 제공: `https://cdn.smartmealtable.com/...`
- 추가 작업 없음

---

### 5. ✅ 예산 초과 알림 정책

**현재 구현 (이미 충분함):**

장바구니 조회 시 예산 정보 자동 포함:
```json
{
  "budgetInfo": {
    "currentMealType": "LUNCH",
    "mealBudget": 5000,
    "dailyBudgetBefore": 15000,
    "dailyBudgetAfter": -5500,
    "monthlyBudgetBefore": 300000,
    "monthlyBudgetAfter": 279500,
    "isOverBudget": true  // 예산 초과 여부 플래그
  }
}
```

**클라이언트 처리:**
- `isOverBudget: true`일 때 경고 메시지 표시
- `dailyBudgetAfter < 0`일 때 일일 예산 초과 안내
- `monthlyBudgetAfter < 0`일 때 월별 예산 초과 안내

**결론:** 추가 작업 불필요 (이미 충분한 정보 제공)

---

## 📊 변경 사항 매핑

| 순번 | 이슈 | 변경 전 | 변경 후 | 상태 |
|------|------|---------|---------|------|
| 0 | 평점 데이터 | `rating` 필드 포함 | `rating` 필드 제거 | ✅ 완료 |
| 1 | 위치 기준 | GPS 좌표 파라미터 | 기본 주소 사용 | ✅ 완료 |
| 2 | 조회 이력 | 명확하지 않음 | 상세 페이지 진입 시점 | ✅ 완료 |
| 3 | 장바구니 충돌 | 409 에러만 반환 | 자동 교체 옵션 추가 | ✅ 완료 |
| 4 | 이미지 관리 | 불명확 | 크롤링 데이터 사용 | ✅ 확인 |
| 5 | 예산 초과 | 불명확 | 이미 구현됨 | ✅ 확인 |

---

## 🔧 구현 체크리스트

### 즉시 구현 필요
- [ ] 평점(rating) 필드 제거
- [ ] GPS 파라미터 제거, 기본 주소 로직 적용
- [ ] 가게 상세 조회 시 이력 기록 로직
- [ ] 장바구니 `replaceCart` 파라미터 처리

### 이미 구현됨
- [x] 기본 주소 설정 API
- [x] 주소 관리 API (CRUD)
- [x] 예산 초과 정보 제공

### 제외/불필요
- [x] ~~이미지 업로드 API~~
- [x] ~~실시간 GPS 활용~~
- [x] ~~평점 데이터 추가~~

---

## 🎯 주요 설계 원칙

### 1. 위치 기준 통일
**모든 위치 기반 기능은 기본 주소(primary address)를 사용**

**적용 대상:**
- ✅ 홈 대시보드
- ✅ 가게 목록 조회
- ✅ 개인화 추천
- ✅ 즐겨찾기 거리 계산

**장점:**
1. 일관된 사용자 경험
2. 서버 측 캐싱 가능
3. GPS 권한 이슈 없음
4. 배터리 절약
5. 예측 가능한 동작

### 2. 명확한 에러 처리
- 기본 주소 없음: `404 (ADDRESS_002)` + 주소 등록 안내
- 장바구니 충돌: `409 (CART_001)` + 자동 교체 제안
- 모든 에러에 `suggestion` 필드 포함

### 3. 클라이언트 친화적 설계
- 옵션 파라미터로 유연성 제공 (`replaceCart`)
- 구체적인 에러 메시지 및 해결 방법 제시
- 예산 초과 정보 자동 계산

---

## 📝 에러 코드 추가

| 에러 코드 | HTTP Status | 메시지 | 해결 방법 |
|-----------|-------------|--------|-----------|
| `ADDRESS_002` | 404 | 등록된 주소가 없습니다 | 주소 등록 화면으로 이동 |
| `CART_001` | 409 | 다른 가게의 상품이 장바구니에 있습니다 | `replaceCart=true` 재요청 또는 수동 삭제 |

---

## 📅 변경 이력

| 날짜 | 버전 | 주요 변경 사항 |
|------|------|----------------|
| 2025-10-10 | v1.2 | - 평점 필드 제거<br>- 위치 기준을 기본 주소로 통일<br>- 조회 이력 기록 시점 명확화<br>- 장바구니 자동 교체 옵션 추가<br>- 예산 초과 정책 확인 |
| 2025-10-10 | v1.1 | - 즐겨찾기 필터/정렬 추가<br>- 개별 음식 선호도 API 추가<br>- 홈 대시보드 주소 기준 명확화<br>- GPS 주소 등록 프로세스 정의 |
| 2025-10-08 | v1.0 | 최초 작성 |

---

## 🚀 다음 단계

1. **백엔드 개발**
   - 평점 필드 제거
   - 기본 주소 기반 로직 구현
   - 장바구니 자동 교체 로직 구현
   - 가게 조회 이력 기록 로직 구현

2. **프론트엔드 개발**
   - GPS 파라미터 제거
   - 기본 주소 선택 UI
   - 장바구니 충돌 처리 플로우
   - 예산 초과 경고 UI

3. **테스트**
   - 기본 주소 없을 때 에러 처리
   - 장바구니 자동 교체 시나리오
   - 조회 이력 정확성 검증

---

**문서 작성자:** GitHub Copilot  
**검토 상태:** ✅ 완료  
**최종 승인:** 대기 중
