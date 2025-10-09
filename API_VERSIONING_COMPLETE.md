# API Versioning 업데이트 완료

## 개요
모든 API 엔드포인트에 `/api/v1` prefix를 추가하여 API 버전 관리 체계를 구축했습니다.

## 변경 사항

### 1. API 문서 업데이트 (API_SPECIFICATION.md)
- **Base URL**: `미정` → `/api/v1`
- **모든 엔드포인트**: `/api/v1` prefix 추가 완료

### 2. 업데이트된 엔드포인트 목록

#### 인증 API (Authentication)
- `POST /api/v1/auth/signup/email`
- `POST /api/v1/auth/login/email`
- `POST /api/v1/auth/login/kakao`
- `POST /api/v1/auth/login/google`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/check-email?email={email}`

#### 회원 관리 API (Members)
- `GET /api/v1/members/me`
- `PUT /api/v1/members/me`
- `DELETE /api/v1/members/me`
- `PUT /api/v1/members/me/password`
- `GET /api/v1/members/me/password/expiry-status`
- `POST /api/v1/members/me/password/extend-expiry`
- `GET /api/v1/members/me/social-accounts`
- `POST /api/v1/members/me/social-accounts`
- `DELETE /api/v1/members/me/social-accounts/{socialAccountId}`
- `GET /api/v1/members/me/addresses`
- `POST /api/v1/members/me/addresses`
- `PUT /api/v1/members/me/addresses/{addressHistoryId}`
- `DELETE /api/v1/members/me/addresses/{addressHistoryId}`
- `PUT /api/v1/members/me/addresses/{addressHistoryId}/primary`
- `GET /api/v1/members/me/preferences`
- `PUT /api/v1/members/me/preferences`
- `GET /api/v1/members/me/onboarding-status`
- `POST /api/v1/members/me/monthly-budget-confirmed`
- `PUT /api/v1/members/me/current-location`
- `GET /api/v1/members/me/notification-settings`
- `PUT /api/v1/members/me/notification-settings`
- `PUT /api/v1/members/me/recommendation-type`

#### 온보딩 API (Onboarding)
- `POST /api/v1/onboarding/profile`
- `POST /api/v1/onboarding/address`
- `POST /api/v1/onboarding/budget`
- `POST /api/v1/onboarding/preferences`
- `POST /api/v1/onboarding/policy-agreements`
- `GET /api/v1/onboarding/foods?categoryId={categoryId}&page=0&size=20`
- `POST /api/v1/onboarding/food-preferences`

#### 그룹 및 카테고리 API
- `GET /api/v1/groups?type={type}&name={name}&page=0&size=20`
- `GET /api/v1/categories`

#### 약관 API (Policies)
- `GET /api/v1/policies`
- `GET /api/v1/policies/{policyId}`

#### 예산 관리 API (Budgets)
- `GET /api/v1/budgets/monthly?year=2025&month=10`
- `GET /api/v1/budgets/daily?date=2025-10-08`
- `PUT /api/v1/budgets`
- `PUT /api/v1/budgets/daily/{date}`

#### 지출 내역 API (Expenditures)
- `POST /api/v1/expenditures/parse-sms`
- `POST /api/v1/expenditures`
- `GET /api/v1/expenditures?startDate=2025-10-01&endDate=2025-10-31&mealType=LUNCH&page=0&size=20`
- `GET /api/v1/expenditures/{expenditureId}`
- `PUT /api/v1/expenditures/{expenditureId}`
- `DELETE /api/v1/expenditures/{expenditureId}`
- `GET /api/v1/expenditures/statistics/daily?year=2025&month=10`

#### 가게 관리 API (Stores)
- `GET /api/v1/stores`
- `GET /api/v1/stores/{storeId}`
- `GET /api/v1/stores/autocomplete?keyword=치킨&limit=10`

#### 추천 API (Recommendations)
- `POST /api/v1/recommendations`
- `GET /api/v1/recommendations/{storeId}/scores`

#### 즐겨찾기 API (Favorites)
- `POST /api/v1/favorites`
- `GET /api/v1/favorites`
- `PUT /api/v1/favorites/order`
- `DELETE /api/v1/favorites/{favoriteId}`

#### 장바구니 API (Cart)
- `GET /api/v1/cart`
- `POST /api/v1/cart/items`
- `PUT /api/v1/cart/items/{cartItemId}`
- `DELETE /api/v1/cart/items/{cartItemId}`
- `DELETE /api/v1/cart`
- `POST /api/v1/cart/checkout`

#### 지도 API (Maps)
- `GET /api/v1/maps/search-address?keyword={keyword}&limit=10`
- `GET /api/v1/maps/reverse-geocode?lat={latitude}&lng={longitude}`

#### 홈 대시보드 API
- `GET /api/v1/home/dashboard?latitude={lat}&longitude={lng}`

#### 설정 API (Settings)
- `GET /api/v1/settings/app`
- `PUT /api/v1/settings/app/tracking`

### 3. 코드 업데이트
- **AuthController**: `@RequestMapping("/api/v1/auth")` 적용
- **SignupControllerTest**: 모든 테스트 URL에 `/api/v1` prefix 적용

### 4. 검증 결과
- ✅ 모든 엔드포인트에 `/api/v1` prefix 적용 완료
- ✅ API 문서와 코드 일치 확인
- ✅ 테스트 통과 (BUILD SUCCESSFUL)

## API Versioning 패턴

### URL 구조
```
/api/v{version}/{resource}
```

### 예시
- `/api/v1/auth/signup/email` - 이메일 회원가입
- `/api/v1/members/me` - 내 정보 조회
- `/api/v1/budgets/monthly` - 월별 예산 조회

## 향후 버전 관리 계획

### Version 2 (v2) 준비 시
1. 새로운 컨트롤러 생성: `@RequestMapping("/api/v2/...")`
2. 기존 v1 API와 병행 운영
3. Deprecated 정책에 따라 v1 단계적 폐지

### 버전 관리 규칙
- **Major version**: Breaking changes 발생 시 (v1 → v2)
- **Minor update**: 하위 호환성 유지하면서 기능 추가 시 (같은 버전 내에서)
- **Patch**: 버그 수정 (버전 변경 없음)

## 구현 완료 상태

### ✅ 완료된 작업
1. API 문서 전체 엔드포인트 업데이트
2. AuthController에 `/api/v1/auth` prefix 적용
3. SignupControllerTest 테스트 URL 업데이트
4. 모든 테스트 통과 확인

### 📋 다음 단계
향후 추가 API 구현 시:
1. 컨트롤러에 `@RequestMapping("/api/v1/{resource}")` 적용
2. 테스트 코드에서 `/api/v1/{resource}` URL 사용
3. API 문서에 `/api/v1` prefix 포함

## 참고사항
- 모든 새로운 API는 `/api/v1` prefix를 사용해야 합니다
- API 버전 변경 시 클라이언트와 사전 협의 필요
- 하위 호환성을 위해 버전 간 병행 운영 기간 필요
