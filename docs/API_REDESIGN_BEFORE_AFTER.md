# 지출 내역 API - 변경 요약 및 비교

**작성일**: 2025-10-23  
**목표**: Before/After 비교로 빠른 이해

---

## 1. 시나리오별 API 비교

### 📋 시나리오 1: 장바구니에서 지출 등록

#### Before (현재)
```
❌ 장바구니 → 지출 등록 시 foodId 필수 (문제점)
❌ storeId 지원 안 됨 (음식점 상세 페이지 링크 불가)
❌ 비정규화 데이터 미활용

POST /api/v1/expenditures

{
  "storeName": "맘스터치",
  "amount": 13500,
  "expendedDate": "2025-10-23",
  "expendedTime": "12:30:00",
  "categoryId": 5,
  "mealType": "LUNCH",
  "memo": "동료와 점심",
  "items": [
    {
      "foodId": 456,           // ◆ 필수이지만 필드명만 제공
      "quantity": 1,
      "price": 6500            // ◆ foodName 없음
    }
  ]
}

Response:
{
  "data": {
    "expenditureId": 999,
    "storeName": "맘스터치",
    "amount": 13500,
    "items": [
      {
        "foodId": 456,
        "quantity": 1,
        "price": 6500
        // ◆ 상세 페이지 링크 여부 알 수 없음
      }
    ]
  }
}
```

#### After (개선)
```
✅ 장바구니 → 지출 등록 신규 엔드포인트
✅ storeId 포함 (음식점 상세 페이지 링크 가능)
✅ foodName 비정규화 저장 (향후 활용)
✅ 명확한 의도 전달

POST /api/v1/expenditures/from-cart  ◆ 새로운 엔드포인트

{
  "storeId": 123,                      // ◆ 가게 ID 명시적 포함
  "storeName": "맘스터치",
  "amount": 13500,
  "expendedDate": "2025-10-23",
  "expendedTime": "12:30:00",
  "categoryId": 5,
  "mealType": "LUNCH",
  "memo": "동료와 점심",
  "items": [
    {
      "foodId": 456,                   // ◆ 필수
      "foodName": "싸이버거 세트",     // ◆ 새로 추가 (비정규화)
      "quantity": 1,
      "price": 6500
    }
  ]
}

Response 201:
{
  "data": {
    "expenditureId": 999,
    "storeId": 123,                    // ◆ 새로 추가
    "storeName": "맘스터치",
    "amount": 13500,
    "items": [
      {
        "itemId": 1,
        "foodId": 456,
        "foodName": "싸이버거 세트", // ◆ 새로 추가
        "quantity": 1,
        "price": 6500,
        "hasFoodLink": true            // ◆ 음식 상세 링크 가능 여부
      }
    ],
    "hasStoreLink": true               // ◆ 가게 상세 링크 가능 여부
  }
}
```

**📊 개선 효과**
| 항목 | Before | After |
|------|--------|-------|
| storeId 지원 | ❌ | ✅ |
| foodName 저장 | ❌ | ✅ |
| 상세 링크 여부 표시 | ❌ | ✅ |
| API 명확성 | 🔶 모호함 | ✅ 명확함 |

---

### 📋 시나리오 2: 수기 입력으로 지출 등록

#### Before (현재)
```
⚠️ 현재도 가능하지만 구조적 한계
⚠️ foodId 필수 → foodId = NULL 처리 불가능

POST /api/v1/expenditures

{
  "storeName": "편의점",
  "amount": 5000,
  "expendedDate": "2025-10-23",
  "expendedTime": "15:30:00",
  "categoryId": null,
  "mealType": "OTHER",
  "memo": null,
  "items": [
    {
      "foodId": ??? ,              // ❌ 필수인데 제공 불가
      "quantity": 1,
      "price": 3000
    }
  ]
}

// ❌ 실제로는 dummy foodId를 줘야 함 또는 항목을 생략해야 함
```

#### After (개선)
```
✅ foodId = NULL 명시적 허용
✅ 음식명만으로 등록 가능
✅ 기존 API 유지 (호환성)

POST /api/v1/expenditures  ◆ 기존 그대로

{
  "storeName": "편의점 (이름 모름)",
  "amount": 5000,
  "expendedDate": "2025-10-23",
  "expendedTime": "15:30:00",
  "categoryId": null,
  "mealType": "OTHER",
  "memo": null,
  "items": [
    {
      "foodName": "김밥",             // ◆ 음식명으로 충분
      "quantity": 1,
      "price": 3000
      // foodId 제공 불필요
    }
  ]
}

Response 201:
{
  "data": {
    "expenditureId": 1000,
    "storeId": null,                 // ◆ NULL
    "storeName": "편의점 (이름 모름)",
    "amount": 5000,
    "items": [
      {
        "itemId": 3,
        "foodId": null,              // ◆ NULL
        "foodName": "김밥",
        "quantity": 1,
        "price": 3000,
        "hasFoodLink": false          // ◆ 상세 링크 불가
      }
    ],
    "hasStoreLink": false            // ◆ 상세 링크 불가
  }
}
```

**📊 개선 효과**
| 항목 | Before | After |
|------|--------|-------|
| foodId NULL 지원 | ❌ | ✅ |
| 음식명만 등록 | 🔶 workaround | ✅ 네이티브 지원 |
| storeId NULL 지원 | ❌ | ✅ |
| API 명확성 | 🔶 모호함 | ✅ 명확함 |

---

## 2. 스키마 변경 요약

### 데이터베이스

```sql
-- Before (현재)
expenditure
├─ expenditure_id (PK)
├─ member_id (FK)
├─ store_name ← 비정규화만 하고 FK 없음
├─ amount
├─ expended_date
├─ expended_time
├─ category_id (논리 FK)
├─ meal_type
├─ memo
├─ deleted
├─ created_at
└─ updated_at

expenditure_item
├─ expenditure_item_id (PK)
├─ expenditure_id (FK)
├─ food_id (FK, NOT NULL) ← 필수
├─ quantity
├─ price
├─ created_at
└─ updated_at

-- After (개선)
expenditure
├─ expenditure_id (PK)
├─ member_id (FK)
├─ store_id (논리 FK, NULL 허용) ◆ 새로 추가
├─ store_name
├─ amount
├─ expended_date
├─ expended_time
├─ category_id (논리 FK)
├─ meal_type
├─ memo
├─ deleted
├─ created_at
└─ updated_at

expenditure_item
├─ expenditure_item_id (PK)
├─ expenditure_id (FK)
├─ food_id (논리 FK, NULL 허용) ◆ 변경
├─ food_name (비정규화) ◆ 새로 추가
├─ quantity
├─ price
├─ created_at
└─ updated_at
```

---

## 3. API 엔드포인트 비교

| 엔드포인트 | Before | After | 용도 |
|-----------|--------|-------|------|
| `POST /api/v1/expenditures` | 기존 | 유지 | 수기 입력 전용 |
| `POST /api/v1/expenditures/from-cart` | - | ✅ 신규 | 장바구니 전용 |
| `GET /api/v1/expenditures` | 기존 | 호환 | 목록 조회 |
| `GET /api/v1/expenditures/{id}` | 기존 | 호환 | 상세 조회 |
| `PUT /api/v1/expenditures/{id}` | 기존 | 호환 | 수정 |
| `DELETE /api/v1/expenditures/{id}` | 기존 | 호환 | 삭제 |

---

## 4. 코드 변경 요약

### 도메인 계층

```java
// Before
Expenditure.create(memberId, storeName, amount, ...)
ExpenditureItem.create(foodId, quantity, price)

// After
// 팩토리 메서드 분리
Expenditure.createFromCart(memberId, storeId, storeName, ...)    // ◆ 장바구니
Expenditure.createFromManualInput(memberId, storeName, ...)      // ◆ 수기 입력

ExpenditureItem.createFromFood(foodId, foodName, ...)            // ◆ 음식 제공
ExpenditureItem.createFromManualInput(foodName, quantity, ...)   // ◆ 음식명만
```

### JPA 계층

```java
// Before
ExpenditureJpaEntity {
    @Column(name = "store_name", nullable = false)
    private String storeName;
}

ExpenditureItemJpaEntity {
    @Column(name = "food_id", nullable = false)
    private Long foodId;
}

// After
ExpenditureJpaEntity {
    @Column(name = "store_id")                  // ◆ 새로 추가
    private Long storeId;
    
    @Column(name = "store_name", nullable = false)
    private String storeName;
}

ExpenditureItemJpaEntity {
    @Column(name = "food_id")                   // ◆ nullable로 변경
    private Long foodId;
    
    @Column(name = "food_name", length = 500)   // ◆ 새로 추가
    private String foodName;
}
```

### DTO 계층

```java
// Before
CreateExpenditureRequest {
    String storeName;
    Integer amount;
    List<ExpenditureItemRequest> items;
}

ExpenditureItemRequest {
    Long foodId;      // ◆ 필수이지만 이름 불명확
    Integer quantity;
    Integer price;
}

// After
CreateExpenditureFromCartRequest {  // ◆ 새로 추가
    Long storeId;
    String storeName;
    Integer amount;
    List<CartItemRequest> items;
}

CartItemRequest {
    Long foodId;
    String foodName;              // ◆ 새로 추가
    Integer quantity;
    Integer price;
}

CreateExpenditureResponse {
    Long storeId;                 // ◆ 새로 추가
    Boolean hasStoreLink;         // ◆ 새로 추가
    List<ExpenditureItemResponse> items;
}

ExpenditureItemResponse {
    Long foodId;
    String foodName;              // ◆ 새로 추가
    Boolean hasFoodLink;          // ◆ 새로 추가
}
```

---

## 5. 클라이언트 영향도

### 📱 모바일 앱 변경사항

#### 장바구니 → 지출 등록

```typescript
// Before
async addExpenditure(expense: {
  storeName: string;
  amount: number;
  items: Array<{
    foodId: number;
    quantity: number;
    price: number;
  }>;
}) {
  return fetch('/api/v1/expenditures', {
    method: 'POST',
    body: JSON.stringify(expense)
  });
}

// After
async addExpenditureFromCart(expense: {
  storeId: number;              // ◆ 새로 추가
  storeName: string;
  amount: number;
  items: Array<{
    foodId: number;
    foodName: string;           // ◆ 새로 추가
    quantity: number;
    price: number;
  }>;
}) {
  return fetch('/api/v1/expenditures/from-cart',  // ◆ 새 엔드포인트
    {
    method: 'POST',
    body: JSON.stringify(expense)
  });
}

// 응답 처리
const response = await addExpenditureFromCart(cartData);
if (response.data.hasStoreLink) {
  // ✅ 가게 상세 페이지로 이동 가능
  navigate(`/stores/${response.data.storeId}`);
}
```

#### 수기 입력 (기존 유지)

```typescript
// 기존 API 그대로 사용 가능
// 단, foodName 제공 가능 (선택사항)

async addExpenditureManual(expense: {
  storeName: string;
  amount: number;
  items: Array<{
    foodName: string;
    quantity: number;
    price: number;
  }>;
}) {
  return fetch('/api/v1/expenditures',  // 기존 엔드포인트
    {
    method: 'POST',
    body: JSON.stringify(expense)
  });
}
```

---

## 6. 마이그레이션 영향 분석

### ✅ 호환되는 부분 (기존 코드 유지 가능)

```java
// 기존 지출 목록 조회 - 완벽 호환
GET /api/v1/expenditures?startDate=...&endDate=...
→ 응답에 storeId, hasStoreLink 추가되지만 선택사항

// 기존 지출 상세 조회 - 완벽 호환
GET /api/v1/expenditures/{id}
→ 응답에 storeId, hasFoodLink 추가되지만 선택사항
```

### ⚠️ 주의할 부분

```java
// 기존 데이터
expenditure_item {
  food_id: 123,
  food_name: NULL  // ◆ 기존 데이터는 NULL
}

// 마이그레이션 필요
// Option 1: 배치 작업으로 food 테이블에서 foodName 채우기
// Option 2: 앱에서 foodId로 foodName 조회해서 디스플레이

// 새 데이터
expenditure_item {
  food_id: 123,
  food_name: "싸이버거 세트"  // ◆ 명시적 저장
}
```

---

## 7. 롤백 계획

### 긴급 상황 시 롤백

```sql
-- 1. 컬럼 제거
ALTER TABLE expenditure 
DROP COLUMN store_id;

ALTER TABLE expenditure_item 
DROP COLUMN food_name;

-- 2. food_id 제약 복원
ALTER TABLE expenditure_item 
MODIFY COLUMN food_id BIGINT NOT NULL;

-- 3. 이전 코드 배포
// Java 코드 롤백
```

---

## 8. 성능 영향 분석

### 📊 성능 변화

| 쿼리 | Before | After | 영향 |
|------|--------|-------|------|
| 지출 목록 조회 | O(1) | O(1) | 무 |
| 지출 상세 조회 | O(1) | O(1) | 무 |
| 검색 (storeId 기준) | - | O(n) | ✅ 신규 기능 |
| 검색 (foodId 기준) | O(n) | O(n) | 무 |
| 저장 | O(1) | O(1) | 무 |

**결론**: 추가 인덱스로 성능 영향 최소화 ✅

---

## 9. 배포 순서

### Phase 1: 준비 (사전)
1. 스키마 변경 DB 검증 ✅
2. 코드 변경 검증 ✅
3. 테스트 완료 ✅

### Phase 2: 배포 (본 배포)
1. DB 마이그레이션 실행
2. 신규 API 엔드포인트 배포
3. 클라이언트 앱 배포 (이전 API도 지원하므로 순서 무관)

### Phase 3: 모니터링 (배포 후)
1. API 응답 시간 모니터링
2. 에러율 모니터링
3. 데이터 일관성 검증

---

## 10. 자주 묻는 질문 (FAQ)

### Q1: 기존 API도 계속 쓸 수 있나?
**A**: 네, 완벽하게 호환됩니다.
- `POST /api/v1/expenditures` 계속 사용 가능
- 단, 새로운 엔드포인트 사용 권장 (더 명확함)

### Q2: 기존 데이터는 어떻게 되나?
**A**: 자동 호환됩니다.
- storeId = NULL로 마이그레이션
- foodId, quantity, price는 그대로 유지

### Q3: foodName이 NULL인 기존 데이터는?
**A**: 향후 배치 작업으로 채우기 추천
```sql
UPDATE expenditure_item ei
SET food_name = (SELECT name FROM food WHERE food.id = ei.food_id)
WHERE ei.food_name IS NULL AND ei.food_id IS NOT NULL;
```

### Q4: 언제 기존 API를 제거할 수 있나?
**A**: 클라이언트 100% 업데이트 후 1개월 뒤
- 지금 당장은 제거 금지
- 충분한 안내 기간 필요

### Q5: storeId가 음식점 테이블의 FK일 필요가 있나?
**A**: 아니오, 논리 FK입니다.
- 물리 FK 제약 없음
- 유연성 높음 (음식점 삭제해도 지출 기록 유지)

---

