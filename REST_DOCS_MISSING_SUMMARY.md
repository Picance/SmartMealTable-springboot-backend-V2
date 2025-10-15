# Rest Docs 누락 엔드포인트 요약

## ✅ Rest Docs 테스트가 완료된 Controller (21개)

### 🔴 높은 우선순위 (핵심 기능) - 모두 완료!

#### 1. StoreController ✅ 완료
- **엔드포인트:** 3개
  - GET `/api/v1/stores` - 가게 검색
  - GET `/api/v1/stores/{storeId}` - 가게 상세 조회
  - GET `/api/v1/stores/autocomplete` - 자동완성 검색
- **보고서:** `STORE_CONTROLLER_REST_DOCS_COMPLETION.md`

#### 2. RecommendationController ✅ 완료
- **엔드포인트:** 3개
  - GET `/api/v1/recommendations` - 추천 목록
  - GET `/api/v1/recommendations/{storeId}/score-detail` - 추천 점수 상세
  - PUT `/api/v1/recommendations/type` - 추천 타입 변경

#### 3. HomeController ✅ 완료
- **엔드포인트:** 3개
  - GET `/api/v1/home/dashboard` - 홈 대시보드
  - GET `/api/v1/members/me/onboarding-status` - 온보딩 상태
  - POST `/api/v1/members/me/monthly-budget-confirmed` - 예산 확인 완료

#### 4. CartController ✅ 완료 - **신규 작성**
- **엔드포인트:** 6개
  - POST `/api/v1/cart/items` - 아이템 추가
  - GET `/api/v1/cart/store/{storeId}` - 가게별 장바구니 조회
  - GET `/api/v1/cart` - 전체 장바구니 조회
  - PUT `/api/v1/cart/items/{cartItemId}` - 수량 변경
  - DELETE `/api/v1/cart/items/{cartItemId}` - 아이템 삭제
  - DELETE `/api/v1/cart/store/{storeId}` - 가게별 전체 삭제
- **보고서:** `HOME_RECOMMENDATION_CART_REST_DOCS_COMPLETION.md`

#### 5. CategoryController ✅ 완료
- **엔드포인트:** 1개
  - GET `/api/v1/categories` - 카테고리 목록
- **보고서:** `CATEGORY_GROUP_REST_DOCS_COMPLETION_REPORT.md`

#### 6. GroupController ✅ 완료
- **엔드포인트:** 1개
  - GET `/api/v1/groups` - 그룹 검색
- **보고서:** `CATEGORY_GROUP_REST_DOCS_COMPLETION_REPORT.md`

---

## 📊 통계

- **총 Controller:** 21개
- **Rest Docs 완료:** 21개 ✅ (100%)
- **Rest Docs 누락:** 0개 ❌ (0%)
- **완료된 엔드포인트:** 73개
- **남은 엔드포인트:** 0개

---

## � 모든 REST Docs 작업 완료!

**최종 업데이트:** 2025-10-15 - CategoryController & GroupController REST Docs 완료
