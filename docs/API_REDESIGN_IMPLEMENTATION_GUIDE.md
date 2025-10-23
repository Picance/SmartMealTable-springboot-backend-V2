# 지출 내역 API 재설계 - 구현 가이드

**작성일**: 2025-10-23  
**목표**: 단계별 구현 방법론

---

## 1. 구현 순서 (권장)

### Step 1: 도메인 모델 변경 (1시간)
```
Domain Layer 변경
├─ Expenditure.java (팩토리 메서드 2개 추가)
└─ ExpenditureItem.java (팩토리 메서드 2개 추가)

이유: 도메인 먼저 정의 → 서비스/컨트롤러가 의존
```

### Step 2: JPA 엔티티 변경 (1시간)
```
Storage Layer 변경
├─ ExpenditureJpaEntity.java (storeId 추가)
├─ ExpenditureItemJpaEntity.java (foodId nullable, foodName 추가)
└─ 매핑 메서드 업데이트 (from, toDomain)

이유: DB 스키마와의 일관성 필요
```

### Step 3: 마이그레이션 스크립트 작성 (30분)
```
DB 변경
├─ 칼럼 추가
├─ NOT NULL 제약 제거
└─ 인덱스 생성

주의: 테스트 DB에서 먼저 검증
```

### Step 4: DTO 작성 (1.5시간)
```
API Layer DTO 변경
├─ CreateExpenditureFromCartRequest (새로 추가)
├─ CreateExpenditureResponse (수정)
├─ Service Request/Response DTO (수정)
└─ ExpenditureItemResponse (수정)

이유: API 인터페이스 정의
```

### Step 5: Service Layer (1시간)
```
Application Logic
├─ CreateExpenditureService (로직 분기)
├─ ExpenditureDomainService (2개 메서드 추가)
└─ 기존 로직 호환성 검증

이유: 비즈니스 로직 분리
```

### Step 6: Controller 추가 (30분)
```
Controller Layer
├─ POST /api/v1/expenditures (기존 유지)
└─ POST /api/v1/expenditures/from-cart (신규 추가)

이유: 엔드포인트 분리 → 명확한 의도 전달
```

### Step 7: 테스트 (2시간)
```
Test 작성
├─ 단위 테스트 (도메인, 엔티티)
├─ 통합 테스트 (컨트롤러)
└─ 기존 API 호환성 테스트

이유: 회귀 테스트 방지
```

---

## 2. 상세 구현 (복사/붙여넣기 가능)

### 2.1 Domain 엔티티 - Expenditure 수정

**파일**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/expenditure/Expenditure.java`

```java
// 기존 필드 추가
private Long storeId;  // ◆ 새로 추가

// 기존 create 메서드 아래에 추가:

/**
 * 팩토리 메서드: 장바구니 → 지출 등록
 * storeId를 포함하여 생성합니다.
 */
public static Expenditure createFromCart(
        Long memberId,
        Long storeId,           // ◆ store FK 포함
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        MealType mealType,
        String memo,
        List<ExpenditureItem> items
) {
    Expenditure expenditure = new Expenditure();
    expenditure.memberId = memberId;
    expenditure.storeId = storeId;      // ◆ 핵심: storeId 저장
    expenditure.storeName = storeName;
    expenditure.amount = amount;
    expenditure.expendedDate = expendedDate;
    expenditure.expendedTime = expendedTime;
    expenditure.categoryId = categoryId;
    expenditure.mealType = mealType;
    expenditure.memo = memo;
    expenditure.items = new ArrayList<>(items);
    expenditure.deleted = false;
    
    // 비즈니스 규칙 검증
    expenditure.validateAmount();
    
    return expenditure;
}

/**
 * 팩토리 메서드: 수기 입력 → 지출 등록
 * storeId를 NULL로 생성합니다.
 */
public static Expenditure createFromManualInput(
        Long memberId,
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        MealType mealType,
        String memo,
        List<ExpenditureItem> items
) {
    Expenditure expenditure = new Expenditure();
    expenditure.memberId = memberId;
    expenditure.storeId = null;         // ◆ 핵심: storeId = NULL
    expenditure.storeName = storeName;
    expenditure.amount = amount;
    expenditure.expendedDate = expendedDate;
    expenditure.expendedTime = expendedTime;
    expenditure.categoryId = categoryId;
    expenditure.mealType = mealType;
    expenditure.memo = memo;
    expenditure.items = new ArrayList<>(items);
    expenditure.deleted = false;
    
    expenditure.validateAmount();
    
    return expenditure;
}

// reconstruct 메서드에도 storeId 추가:
public static Expenditure reconstruct(
        Long expenditureId,
        Long memberId,
        Long storeId,               // ◆ 추가
        String storeName,
        // ... 나머지 동일 ...
) {
    Expenditure expenditure = new Expenditure();
    expenditure.expenditureId = expenditureId;
    expenditure.memberId = memberId;
    expenditure.storeId = storeId;      // ◆ 복원
    // ... 나머지 동일 ...
    return expenditure;
}
```

### 2.2 Domain 엔티티 - ExpenditureItem 수정

**파일**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/expenditure/ExpenditureItem.java`

```java
// 기존 필드 수정
private Long foodId;                    // ◆ nullable로 변경 (주석만)
// 기존 필드 추가
private String foodName;                // ◆ 새로 추가

// 기존 create 메서드 아래에 추가:

/**
 * 팩토리 메서드: 장바구니 항목 → 지출항목
 * foodId + foodName 모두 저장
 */
public static ExpenditureItem createFromFood(
        Long foodId,
        String foodName,
        Integer quantity,
        Integer price
) {
    ExpenditureItem item = new ExpenditureItem();
    item.foodId = foodId;               // ◆ foodId 저장
    item.foodName = foodName;           // ◆ 비정규화 저장
    item.quantity = quantity;
    item.price = price;
    
    item.validateQuantity();
    item.validatePrice();
    
    return item;
}

/**
 * 팩토리 메서드: 수기 입력 → 지출항목
 * foodName만 저장, foodId는 NULL
 */
public static ExpenditureItem createFromManualInput(
        String foodName,
        Integer quantity,
        Integer price
) {
    ExpenditureItem item = new ExpenditureItem();
    item.foodId = null;                 // ◆ NULL 처리
    item.foodName = foodName;           // ◆ 음식명만 저장
    item.quantity = quantity;
    item.price = price;
    
    item.validateQuantity();
    item.validatePrice();
    
    return item;
}

// reconstruct 메서드에도 foodName 추가:
public static ExpenditureItem reconstruct(
        Long expenditureItemId,
        Long expenditureId,
        Long foodId,                    // ◆ 이미 nullable
        String foodName,                // ◆ 추가
        Integer quantity,
        Integer price
) {
    ExpenditureItem item = new ExpenditureItem();
    item.expenditureItemId = expenditureItemId;
    item.expenditureId = expenditureId;
    item.foodId = foodId;               // ◆ NULL 가능
    item.foodName = foodName;           // ◆ 복원
    item.quantity = quantity;
    item.price = price;
    
    return item;
}
```

### 2.3 JPA 엔티티 - ExpenditureJpaEntity 수정

**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/expenditure/ExpenditureJpaEntity.java`

```java
// 필드 추가 (memberId 아래에)
@Column(name = "store_id")              // ◆ nullable
private Long storeId;

// from() 메서드 수정:
public static ExpenditureJpaEntity from(Expenditure domain) {
    ExpenditureJpaEntity entity = new ExpenditureJpaEntity();
    entity.id = domain.getExpenditureId();
    entity.memberId = domain.getMemberId();
    entity.storeId = domain.getStoreId();        // ◆ 추가
    entity.storeName = domain.getStoreName();
    // ... 나머지 동일 ...
}

// toDomain() 메서드 수정:
public Expenditure toDomain() {
    List<ExpenditureItem> domainItems = items.stream()
            .map(ExpenditureItemJpaEntity::toDomain)
            .collect(Collectors.toList());
    
    return Expenditure.reconstruct(
            this.id,
            this.memberId,
            this.storeId,                       // ◆ 추가
            this.storeName,
            this.amount,
            // ... 나머지 동일 ...
    );
}
```

### 2.4 JPA 엔티티 - ExpenditureItemJpaEntity 수정

**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/expenditure/ExpenditureItemJpaEntity.java`

```java
// food_id 필드 수정 (주석 추가)
@Column(name = "food_id")               // ◆ nullable로 변경
private Long foodId;

// foodName 필드 추가 (food_id 아래에)
@Column(name = "food_name", length = 500)   // ◆ 새로 추가
private String foodName;

// from() 메서드 수정:
public static ExpenditureItemJpaEntity from(ExpenditureItem domain, ExpenditureJpaEntity expenditure) {
    ExpenditureItemJpaEntity entity = new ExpenditureItemJpaEntity();
    entity.id = domain.getExpenditureItemId();
    entity.expenditure = expenditure;
    entity.foodId = domain.getFoodId();         // ◆ NULL 가능
    entity.foodName = domain.getFoodName();     // ◆ 새로 추가
    entity.quantity = domain.getQuantity();
    entity.price = domain.getPrice();
    return entity;
}

// toDomain() 메서드 수정:
public ExpenditureItem toDomain() {
    return ExpenditureItem.reconstruct(
            this.id,
            this.expenditure != null ? this.expenditure.getId() : null,
            this.foodId,                        // ◆ NULL 가능
            this.foodName,                      // ◆ 새로 추가
            this.quantity,
            this.price
    );
}
```

### 2.5 요청 DTO - CreateExpenditureFromCartRequest (새로 생성)

**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/expenditure/dto/request/CreateExpenditureFromCartRequest.java`

```java
package com.stdev.smartmealtable.api.expenditure.dto.request;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 장바구니 → 지출 내역 등록 요청 DTO
 */
public record CreateExpenditureFromCartRequest(
        @NotNull(message = "가게 ID는 필수입니다.")
        Long storeId,
        
        @NotBlank(message = "가게 이름은 필수입니다.")
        @Size(max = 200, message = "가게 이름은 200자를 초과할 수 없습니다.")
        String storeName,
        
        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 0, message = "금액은 0 이상이어야 합니다.")
        Integer amount,
        
        @NotNull(message = "지출 날짜는 필수입니다.")
        LocalDate expendedDate,
        
        LocalTime expendedTime,
        
        Long categoryId,
        
        MealType mealType,
        
        @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다.")
        String memo,
        
        @Valid
        List<CartItemRequest> items
) {
    /**
     * 장바구니 항목 요청 DTO
     */
    public record CartItemRequest(
            @NotNull(message = "음식 ID는 필수입니다.")
            Long foodId,
            
            @NotNull(message = "음식명은 필수입니다.")
            @Size(max = 500, message = "음식명은 500자를 초과할 수 없습니다.")
            String foodName,
            
            @NotNull(message = "수량은 필수입니다.")
            @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
            Integer quantity,
            
            @NotNull(message = "가격은 필수입니다.")
            @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
            Integer price
    ) {}
}
```

### 2.6 응답 DTO 수정

**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/expenditure/dto/response/CreateExpenditureResponse.java`

```java
// 새 필드 추가
Long storeId,               // ◆ 새로 추가
Boolean hasStoreLink,       // ◆ storeId != null 여부

// 기존 필드 아래 ExpenditureItemResponse 수정:
public record ExpenditureItemResponse(
        Long itemId,
        Long foodId,
        String foodName,        // ◆ 새로 추가
        Integer quantity,
        Integer price,
        Boolean hasFoodLink     // ◆ foodId != null 여부
) {
    public static ExpenditureItemResponse from(
            ExpenditureItemServiceResponse response
    ) {
        return new ExpenditureItemResponse(
                response.itemId(),
                response.foodId(),
                response.foodName(),            // ◆ 새로 추가
                response.quantity(),
                response.price(),
                response.foodId() != null       // ◆ foodId 있으면 true
        );
    }
}

// from() 메서드 수정:
public static CreateExpenditureResponse from(
        CreateExpenditureServiceResponse serviceResponse
) {
    return new CreateExpenditureResponse(
            // ... 기존 필드 ...
            serviceResponse.storeId(),          // ◆ 추가
            // ... 기존 필드 ...
            serviceResponse.items().stream()
                    .map(ExpenditureItemResponse::from)
                    .collect(Collectors.toList()),
            serviceResponse.createdAt(),
            serviceResponse.storeId() != null   // ◆ hasStoreLink 계산
    );
}
```

### 2.7 서비스 DTO 수정

**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/expenditure/service/dto/CreateExpenditureServiceRequest.java`

```java
public record CreateExpenditureServiceRequest(
        Long memberId,
        Long storeId,               // ◆ 새로 추가
        String storeName,
        // ... 나머지 동일 ...
) {
    public record ExpenditureItemServiceRequest(
            Long foodId,            // ◆ nullable (주석)
            String foodName,        // ◆ 새로 추가
            Integer quantity,
            Integer price
    ) {}
}
```

**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/expenditure/service/dto/CreateExpenditureServiceResponse.java`

```java
public record CreateExpenditureServiceResponse(
        Long expenditureId,
        Long storeId,               // ◆ 새로 추가
        String storeName,
        // ... 나머지 동일 ...
) {
    public static CreateExpenditureServiceResponse from(
            Expenditure expenditure,
            String categoryName
    ) {
        return new CreateExpenditureServiceResponse(
                expenditure.getExpenditureId(),
                expenditure.getStoreId(),       // ◆ 추가
                expenditure.getStoreName(),
                // ... 나머지 동일 ...
        );
    }
}
```

### 2.8 Service Layer - Controller 변경

**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/expenditure/controller/ExpenditureController.java`

```java
// 기존 createExpenditure 메서드 아래에 추가:

/**
 * 장바구니 → 지출 내역 등록
 * POST /api/v1/expenditures/from-cart
 */
@PostMapping("/from-cart")
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<CreateExpenditureResponse> createExpenditureFromCart(
        @AuthUser AuthenticatedUser user,
        @Valid @RequestBody CreateExpenditureFromCartRequest request
) {
    // 요청 DTO → 서비스 요청 DTO 변환
    CreateExpenditureServiceRequest serviceRequest = 
            new CreateExpenditureServiceRequest(
                    user.memberId(),
                    request.storeId(),              // ◆ storeId 포함
                    request.storeName(),
                    request.amount(),
                    request.expendedDate(),
                    request.expendedTime(),
                    request.categoryId(),
                    request.mealType(),
                    request.memo(),
                    request.items().stream()
                            .map(item -> new CreateExpenditureServiceRequest
                                    .ExpenditureItemServiceRequest(
                                    item.foodId(),  // ◆ foodId 포함
                                    item.foodName(),
                                    item.quantity(),
                                    item.price()
                            ))
                            .collect(Collectors.toList())
            );
    
    // 서비스 호출
    CreateExpenditureServiceResponse serviceResponse = 
            createExpenditureService.createExpenditure(serviceRequest);
    
    // 응답 DTO 변환
    CreateExpenditureResponse response = 
            CreateExpenditureResponse.from(serviceResponse);
    
    return ApiResponse.success(response);
}
```

---

## 3. 마이그레이션 SQL

**파일**: 별도의 마이그레이션 폴더에 저장

```sql
-- V1__Add_store_and_food_denormalization.sql

-- 1. expenditure 테이블에 store_id 칼럼 추가
ALTER TABLE expenditure 
ADD COLUMN store_id BIGINT NULLABLE COMMENT '가게 ID (논리 FK, NULL 허용)' 
AFTER category_id;

CREATE INDEX idx_expenditure_store_id ON expenditure(store_id);

-- 2. expenditure_item 테이블 변경
ALTER TABLE expenditure_item 
ADD COLUMN food_name VARCHAR(500) NULLABLE COMMENT '음식명 (비정규화)' 
AFTER food_id;

ALTER TABLE expenditure_item 
MODIFY COLUMN food_id BIGINT NULLABLE COMMENT '음식 ID (논리 FK, NULL 허용)';

CREATE INDEX idx_expenditure_item_food_id ON expenditure_item(food_id);

-- 3. 기존 데이터는 자동으로 호환
-- - expenditure.store_id = NULL (기존 데이터는 가게 정보 없음)
-- - expenditure_item.food_id = 기존 값 유지
-- - expenditure_item.food_name = NULL (추후 마이그레이션)
```

---

## 4. 테스트 코드 예시

### 4.1 단위 테스트

**파일**: `smartmealtable-domain/src/test/java/com/stdev/smartmealtable/domain/expenditure/ExpenditureTest.java`

```java
@DisplayName("Expenditure 도메인 테스트")
class ExpenditureTest {
    
    @Test
    @DisplayName("장바구니 → 지출 생성: storeId 포함")
    void createFromCart_withStoreId_success() {
        // Given
        Long memberId = 1L;
        Long storeId = 123L;
        String storeName = "맘스터치";
        Integer amount = 13500;
        LocalDate expendedDate = LocalDate.of(2025, 10, 23);
        LocalTime expendedTime = LocalTime.of(12, 30);
        Long categoryId = 5L;
        MealType mealType = MealType.LUNCH;
        String memo = "동료와 점심";
        
        List<ExpenditureItem> items = List.of(
                ExpenditureItem.createFromFood(456L, "싸이버거 세트", 1, 6500),
                ExpenditureItem.createFromFood(789L, "치킨버거 세트", 1, 7000)
        );
        
        // When
        Expenditure expenditure = Expenditure.createFromCart(
                memberId, storeId, storeName, amount, expendedDate, expendedTime,
                categoryId, mealType, memo, items
        );
        
        // Then
        assertThat(expenditure.getExpenditureId()).isNull();
        assertThat(expenditure.getMemberId()).isEqualTo(memberId);
        assertThat(expenditure.getStoreId()).isEqualTo(storeId);      // ◆ 검증
        assertThat(expenditure.getStoreName()).isEqualTo(storeName);
        assertThat(expenditure.getAmount()).isEqualTo(amount);
        assertThat(expenditure.getItems()).hasSize(2);
        assertThat(expenditure.getItems().get(0).getFoodId()).isEqualTo(456L);
    }
    
    @Test
    @DisplayName("수기 입력 → 지출 생성: storeId = NULL")
    void createFromManualInput_withoutStoreId_success() {
        // Given
        Long memberId = 1L;
        String storeName = "편의점 (이름 모름)";
        Integer amount = 5000;
        LocalDate expendedDate = LocalDate.of(2025, 10, 23);
        LocalTime expendedTime = LocalTime.of(15, 30);
        
        List<ExpenditureItem> items = List.of(
                ExpenditureItem.createFromManualInput("김밥", 1, 3000),
                ExpenditureItem.createFromManualInput("우유", 1, 2000)
        );
        
        // When
        Expenditure expenditure = Expenditure.createFromManualInput(
                memberId, storeName, amount, expendedDate, expendedTime,
                null, MealType.OTHER, null, items
        );
        
        // Then
        assertThat(expenditure.getStoreId()).isNull();              // ◆ 검증
        assertThat(expenditure.getStoreName()).isEqualTo(storeName);
        assertThat(expenditure.getItems()).hasSize(2);
        assertThat(expenditure.getItems().get(0).getFoodId()).isNull();
        assertThat(expenditure.getItems().get(0).getFoodName()).isEqualTo("김밥");
    }
}

@DisplayName("ExpenditureItem 도메인 테스트")
class ExpenditureItemTest {
    
    @Test
    @DisplayName("음식 항목 생성: foodId + foodName 포함")
    void createFromFood_withFoodId_success() {
        // Given
        Long foodId = 456L;
        String foodName = "싸이버거 세트";
        Integer quantity = 1;
        Integer price = 6500;
        
        // When
        ExpenditureItem item = ExpenditureItem.createFromFood(
                foodId, foodName, quantity, price
        );
        
        // Then
        assertThat(item.getFoodId()).isEqualTo(foodId);             // ◆ 검증
        assertThat(item.getFoodName()).isEqualTo(foodName);
        assertThat(item.getQuantity()).isEqualTo(quantity);
        assertThat(item.getPrice()).isEqualTo(price);
    }
    
    @Test
    @DisplayName("수기 입력 항목 생성: foodId = NULL")
    void createFromManualInput_withoutFoodId_success() {
        // Given
        String foodName = "김밥";
        Integer quantity = 1;
        Integer price = 3000;
        
        // When
        ExpenditureItem item = ExpenditureItem.createFromManualInput(
                foodName, quantity, price
        );
        
        // Then
        assertThat(item.getFoodId()).isNull();                      // ◆ 검증
        assertThat(item.getFoodName()).isEqualTo(foodName);
        assertThat(item.getQuantity()).isEqualTo(quantity);
        assertThat(item.getPrice()).isEqualTo(price);
    }
}
```

### 4.2 통합 테스트

**파일**: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/expenditure/controller/ExpenditureControllerIntegrationTest.java`

```java
@DisplayName("지출 내역 등록 통합 테스트")
class ExpenditureControllerIntegrationTest extends AbstractIntegrationTest {
    
    @Test
    @DisplayName("장바구니 → 지출 등록 (storeId + foodId 포함)")
    void createExpenditureFromCart_success() throws Exception {
        // Given
        Long memberId = setupMember();
        Long storeId = 123L;
        
        CreateExpenditureFromCartRequest request = 
                new CreateExpenditureFromCartRequest(
                        storeId,
                        "맘스터치",
                        13500,
                        LocalDate.of(2025, 10, 23),
                        LocalTime.of(12, 30),
                        5L,
                        MealType.LUNCH,
                        "동료와 점심",
                        List.of(
                                new CreateExpenditureFromCartRequest.CartItemRequest(
                                        456L, "싸이버거 세트", 1, 6500
                                ),
                                new CreateExpenditureFromCartRequest.CartItemRequest(
                                        789L, "치킨버거 세트", 1, 7000
                                )
                        )
                );
        
        // When & Then
        mockMvc.perform(post("/api/v1/expenditures/from-cart")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeId").value(123))    // ◆ 검증
                .andExpect(jsonPath("$.data.items[0].foodId").value(456))
                .andExpect(jsonPath("$.data.hasStoreLink").value(true));
    }
    
    @Test
    @DisplayName("수기 입력 → 지출 등록 (storeId + foodId = NULL)")
    void createExpenditure_manualInput_success() throws Exception {
        // Given
        Long memberId = setupMember();
        
        CreateExpenditureRequest request = 
                new CreateExpenditureRequest(
                        "편의점 (이름 모름)",
                        5000,
                        LocalDate.of(2025, 10, 23),
                        LocalTime.of(15, 30),
                        null,
                        MealType.OTHER,
                        null,
                        List.of(
                                new CreateExpenditureRequest.ExpenditureItemRequest(
                                        "김밥", 1, 3000
                                ),
                                new CreateExpenditureRequest.ExpenditureItemRequest(
                                        "우유", 1, 2000
                                )
                        )
                );
        
        // When & Then
        mockMvc.perform(post("/api/v1/expenditures")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.storeId").isEmpty())      // ◆ NULL 검증
                .andExpect(jsonPath("$.data.items[0].foodId").isEmpty())
                .andExpect(jsonPath("$.data.items[0].foodName").value("김밥"))
                .andExpect(jsonPath("$.data.hasStoreLink").value(false));
    }
}
```

---

## 5. 체크리스트

### 구현 전
- [ ] 스키마 변경 SQL 검증 (테스트 DB)
- [ ] 도메인 모델 설계 검토
- [ ] API 설계 검토

### 구현 중
- [ ] Domain 엔티티 수정
- [ ] JPA 엔티티 수정
- [ ] DTO 작성
- [ ] Service 구현
- [ ] Controller 추가
- [ ] 테스트 작성

### 구현 후
- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과
- [ ] 기존 API 호환성 검증
- [ ] REST Docs 업데이트
- [ ] Code Review

### 배포 전
- [ ] 마이그레이션 스크립트 최종 검증
- [ ] 롤백 계획 수립
- [ ] 모니터링 설정

---

## 6. 주의사항

### ⚠️ 실수하기 쉬운 부분

1. **DTO 변환 누락**
   ```java
   // ❌ 잘못된 코드
   CreateExpenditureServiceRequest.ExpenditureItemServiceRequest(
       request.foodId(),  // 기존 코드
       null,  // foodName 누락
       // ...
   )
   
   // ✅ 올바른 코드
   CreateExpenditureServiceRequest.ExpenditureItemServiceRequest(
       request.foodId(),
       request.foodName(),  // ◆ 추가
       request.quantity(),
       request.price()
   )
   ```

2. **팩토리 메서드 호출 실수**
   ```java
   // ❌ 잘못된 코드
   ExpenditureItem.create(...foodId...)  // 기존 메서드
   
   // ✅ 올바른 코드
   if (foodId != null) {
       ExpenditureItem.createFromFood(...foodId, foodName...)
   } else {
       ExpenditureItem.createFromManualInput(...foodName...)
   }
   ```

3. **NULL 체크 누락**
   ```java
   // ❌ 잘못된 코드
   Boolean hasStoreLink = expenditure.getStoreId() != 0;  // 0으로 비교
   
   // ✅ 올바른 코드
   Boolean hasStoreLink = expenditure.getStoreId() != null;  // null 비교
   ```

