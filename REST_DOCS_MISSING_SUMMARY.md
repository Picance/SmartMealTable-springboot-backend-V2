# Rest Docs 누락 엔드포인트 요약

## ❌ Rest Docs 테스트가 없는 Controller (6개)

### 🔴 높은 우선순위 (핵심 기능)

#### 1. StoreController ❌
- **엔드포인트:** 3개
  - GET `/api/v1/stores` - 가게 검색
  - GET `/api/v1/stores/{storeId}` - 가게 상세 조회
  - GET `/api/v1/stores/autocomplete` - 자동완성 검색

#### 2. RecommendationController ❌
- **엔드포인트:** 3개
  - GET `/api/v1/recommendations` - 추천 목록
  - GET `/api/v1/recommendations/{storeId}/score-detail` - 추천 점수 상세
  - PUT `/api/v1/recommendations/type` - 추천 타입 변경

#### 3. HomeController ❌
- **엔드포인트:** 3개
  - GET `/api/v1/home/dashboard` - 홈 대시보드
  - GET `/api/v1/members/me/onboarding-status` - 온보딩 상태
  - POST `/api/v1/members/me/monthly-budget-confirmed` - 예산 확인 완료

#### 4. CartController ❌
- **엔드포인트:** 6개
  - POST `/api/v1/cart/items` - 아이템 추가
  - GET `/api/v1/cart/store/{storeId}` - 가게별 장바구니 조회
  - GET `/api/v1/cart` - 전체 장바구니 조회
  - PUT `/api/v1/cart/items/{cartItemId}` - 수량 변경
  - DELETE `/api/v1/cart/items/{cartItemId}` - 아이템 삭제
  - DELETE `/api/v1/cart/store/{storeId}` - 가게별 전체 삭제

### 🟡 중간 우선순위

#### 5. CategoryController ❌
- **엔드포인트:** 1개
  - GET `/api/v1/categories` - 카테고리 목록

### 🟢 낮은 우선순위

#### 6. GroupController ❌
- **엔드포인트:** 1개
  - GET `/api/v1/groups` - 그룹 조회

---

## 📊 통계

- **총 Controller:** 21개
- **Rest Docs 완료:** 15개 ✅
- **Rest Docs 누락:** 6개 ❌
- **누락된 엔드포인트:** 17개

---

## 🎯 권장 작업 순서

1. **StoreController** (가게 검색 - 핵심 기능)
2. **HomeController** (홈 화면 - 사용자 진입점)
3. **RecommendationController** (추천 기능 - 앱의 핵심 가치)
4. **CartController** (장바구니 - 주문 기능)
5. **CategoryController** (카테고리 - 검색 필터)
6. **GroupController** (그룹 기능 - 부가 기능)

---

**상세 분석:** `REST_DOCS_MISSING_ENDPOINTS_ANALYSIS.md` 참고
