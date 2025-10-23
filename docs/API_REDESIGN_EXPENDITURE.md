# 지출 내역 API 재설계 문서

**작성일**: 2025-10-23  
**목표**: 장바구니 등록 + 수기 입력 두 시나리오를 모두 지원하는 API 구조 변경

---

## 1. 문제 정의

### 현재 구조의 한계
```
✗ ExpenditureItem.foodId는 필수 필드 (NOT NULL)
✗ 음식정보 없이 가게명만으로 등록 불가능
✗ 비정규화 데이터(storeName, foodName) 미활용
```

### 요구사항
```
✓ 장바구니 → 지출 등록: Food FK 채워짐 + 비정규화 데이터 저장
✓ 수기 입력 → 지출 등록: Food FK = NULL + storeName만 사용
✓ FK 있을 때만 상세 페이지 링크 제공
✓ 기존 데이터 호환성 유지
```

---

## 2. 스키마 변경

### 2.1 데이터베이스 마이그레이션 스크립트

```sql
-- ============================================================
-- expenditure_item 테이블 스키마 변경
-- ============================================================

-- 1. food_name 칼럼 추가 (비정규화)
ALTER TABLE expenditure_item 
ADD COLUMN food_name VARCHAR(500) NULLABLE COMMENT '음식명 (비정규화)' AFTER food_id;

-- 2. food_id를 nullable로 변경
ALTER TABLE expenditure_item 
MODIFY COLUMN food_id BIGINT NULLABLE COMMENT '음식 ID (논리 FK, NULL 허용)';

-- 3. 인덱스 추가 (food_id로 검색 시 성능)
CREATE INDEX idx_food_id ON expenditure_item(food_id);

-- 4. Unique 제약조건 변경 (food_id가 NULL이면 unique 무시)
-- → MySQL은 NULL을 다르게 처리하므로 기존 제약 유지 가능
-- uq_expenditure_food는 (food_id IS NOT NULL)일 때만 체크되도록 변경 필수 (V3 이상)

-- ============================================================
-- expenditure 테이블 스키마 변경
-- ============================================================

-- 1. store_id 칼럼 추가 (논리 FK)
ALTER TABLE expenditure 
ADD COLUMN store_id BIGINT NULLABLE COMMENT '가게 ID (논리 FK, NULL 허용)' AFTER category_id;

-- 2. 인덱스 추가
CREATE INDEX idx_store_id ON expenditure(store_id);
```

### 2.2 스키마 다이어그램

```
┌─────────────────────────────────────┐
│ expenditure                         │
├─────────────────────────────────────┤
│ expenditure_id (PK)                 │
│ member_id (FK)                      │
│ store_id (논리FK, NULL허용) ◆새로추가 │
│ store_name (비정규화)               │
│ amount                              │
│ expended_date                       │
│ expended_time                       │
│ category_id (논리FK)                │
│ meal_type                           │
│ memo                                │
│ deleted                             │
│ created_at                          │
│ updated_at                          │
└─────────────────────────────────────┘
          ▲
          │ 1:N
          │
┌─────────────────────────────────────┐
│ expenditure_item                    │
├─────────────────────────────────────┤
│ expenditure_item_id (PK)            │
│ expenditure_id (FK)                 │
│ food_id (논리FK, NULL허용) ◆변경    │
│ food_name (비정규화) ◆새로추가      │
│ quantity                            │
│ price                               │
│ created_at                          │
│ updated_at                          │
└─────────────────────────────────────┘
```

---

## 3. 도메인 모델 변경

### 3.1 Expenditure 도메인 엔티티

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expenditure {
    
    private Long expenditureId;
    private Long memberId;
    private Long storeId;           // ◆ 새로 추가 (논리 FK, nullable)
    private String storeName;
    private Integer amount;
    private LocalDate expendedDate;
    private LocalTime expendedTime;
    private Long categoryId;
    private MealType mealType;
    private String memo;
    private List<ExpenditureItem> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private Boolean deleted;
    
    /**
     * 팩토리 메서드: 장바구니 → 지출 등록 (storeFk 포함)
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
        expenditure.storeId = storeId;      // ◆ 장바구니에서 넘어온 storeId
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
    
    /**
     * 팩토리 메서드: 수기 입력 → 지출 등록 (storeId = NULL)
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
        expenditure.storeId = null;         // ◆ 수기 입력은 storeId = NULL
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
    
    /**
     * JPA 복원용
     */
    public static Expenditure reconstruct(
            Long expenditureId,
            Long memberId,
            Long storeId,           // ◆ 새로 추가
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ExpenditureItem> items,
            LocalDateTime createdAt,
            Boolean deleted
    ) {
        Expenditure expenditure = new Expenditure();
        expenditure.expenditureId = expenditureId;
        expenditure.memberId = memberId;
        expenditure.storeId = storeId;      // ◆ 복원
        expenditure.storeName = storeName;
        expenditure.amount = amount;
        expenditure.expendedDate = expendedDate;
        expenditure.expendedTime = expendedTime;
        expenditure.categoryId = categoryId;
        expenditure.mealType = mealType;
        expenditure.memo = memo;
        expenditure.items = new ArrayList<>(items);
        expenditure.createdAt = createdAt;
        expenditure.deleted = deleted;
        
        return expenditure;
    }
}
```

### 3.2 ExpenditureItem 도메인 엔티티

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenditureItem {
    
    private Long expenditureItemId;
    private Long expenditureId;
    private Long foodId;                    // ◆ nullable 변경
    private String foodName;                // ◆ 새로 추가 (비정규화)
    private Integer quantity;
    private Integer price;
    
    /**
     * 팩토리: 장바구니 항목 → 지출항목 (foodId 포함)
     */
    public static ExpenditureItem createFromFood(
            Long foodId,
            String foodName,                // ◆ 비정규화 데이터
            Integer quantity,
            Integer price
    ) {
        ExpenditureItem item = new ExpenditureItem();
        item.foodId = foodId;               // ◆ foodId 채움
        item.foodName = foodName;           // ◆ 비정규화 저장
        item.quantity = quantity;
        item.price = price;
        
        item.validateQuantity();
        item.validatePrice();
        
        return item;
    }
    
    /**
     * 팩토리: 수기 입력 → 지출항목 (foodId = NULL)
     */
    public static ExpenditureItem createFromManualInput(
            String foodName,                // ◆ 음식명만 저장
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
    
    /**
     * JPA 복원용
     */
    public static ExpenditureItem reconstruct(
            Long expenditureItemId,
            Long expenditureId,
            Long foodId,                    // ◆ nullable
            String foodName,                // ◆ 새로 추가
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
}
```

---

## 4. JPA 엔티티 변경

### 4.1 ExpenditureJpaEntity

```java
@Entity
@Table(name = "expenditure")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenditureJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expenditure_id")
    private Long id;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Column(name = "store_id")                      // ◆ nullable
    private Long storeId;
    
    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;
    
    @Column(name = "amount", nullable = false)
    private Integer amount;
    
    @Column(name = "expended_date", nullable = false)
    private LocalDate expendedDate;
    
    @Column(name = "expended_time")
    private LocalTime expendedTime;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", length = 20)
    private MealType mealType;
    
    @Column(name = "memo", length = 500)
    private String memo;
    
    @OneToMany(mappedBy = "expenditure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenditureItemJpaEntity> items = new ArrayList<>();
    
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Domain → JPA Entity 변환
     */
    public static ExpenditureJpaEntity from(Expenditure domain) {
        ExpenditureJpaEntity entity = new ExpenditureJpaEntity();
        entity.id = domain.getExpenditureId();
        entity.memberId = domain.getMemberId();
        entity.storeId = domain.getStoreId();        // ◆ 새로 추가
        entity.storeName = domain.getStoreName();
        entity.amount = domain.getAmount();
        entity.expendedDate = domain.getExpendedDate();
        entity.expendedTime = domain.getExpendedTime();
        entity.categoryId = domain.getCategoryId();
        entity.mealType = domain.getMealType();
        entity.memo = domain.getMemo();
        entity.deleted = domain.getDeleted();
        
        if (domain.getItems() != null) {
            List<ExpenditureItemJpaEntity> itemEntities = domain.getItems().stream()
                    .map(item -> ExpenditureItemJpaEntity.from(item, entity))
                    .collect(Collectors.toList());
            entity.items = itemEntities;
        }
        
        return entity;
    }
    
    /**
     * JPA Entity → Domain 변환
     */
    public Expenditure toDomain() {
        List<ExpenditureItem> domainItems = items.stream()
                .map(ExpenditureItemJpaEntity::toDomain)
                .collect(Collectors.toList());
        
        return Expenditure.reconstruct(
                this.id,
                this.memberId,
                this.storeId,                       // ◆ 새로 추가
                this.storeName,
                this.amount,
                this.expendedDate,
                this.expendedTime,
                this.categoryId,
                this.mealType,
                this.memo,
                domainItems,
                this.createdAt,
                this.deleted
        );
    }
    
    public void addItem(ExpenditureItemJpaEntity item) {
        items.add(item);
        item.setExpenditure(this);
    }
}
```

### 4.2 ExpenditureItemJpaEntity

```java
@Entity
@Table(name = "expenditure_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenditureItemJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expenditure_item_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expenditure_id", nullable = false)
    private ExpenditureJpaEntity expenditure;
    
    @Column(name = "food_id")                       // ◆ nullable로 변경
    private Long foodId;
    
    @Column(name = "food_name", length = 500)      // ◆ 새로 추가
    private String foodName;
    
    @Column(name = "order_quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "order_price", nullable = false)
    private Integer price;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Domain → JPA Entity 변환
     */
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
    
    /**
     * JPA Entity → Domain 변환
     */
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
    
    protected void setExpenditure(ExpenditureJpaEntity expenditure) {
        this.expenditure = expenditure;
    }
}
```

---

## 5. API 변경

### 5.1 Request DTO 변경

#### CreateExpenditureRequest (기존 - 호환성 유지)

```java
/**
 * 지출 내역 등록 요청 DTO
 * - 수기 입력 시나리오 지원 (storeId, 음식명 수동)
 */
public record CreateExpenditureRequest(
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
        List<ExpenditureItemRequest> items
) {
    /**
     * 지출 항목 요청 DTO - 수기 입력
     */
    public record ExpenditureItemRequest(
            @NotNull(message = "음식명은 필수입니다.")
            @Size(max = 500)
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

#### CreateExpenditureFromCartRequest ◆ 새로 추가

```java
/**
 * 장바구니 → 지출 내역 등록 요청 DTO
 * - 장바구니 시나리오 지원 (storeId, foodId 포함)
 */
public record CreateExpenditureFromCartRequest(
        @NotNull(message = "가게 ID는 필수입니다.")
        Long storeId,               // ◆ 새로 추가
        
        @NotBlank(message = "가게 이름은 필수입니다.")
        @Size(max = 200)
        String storeName,
        
        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 0)
        Integer amount,
        
        @NotNull(message = "지출 날짜는 필수입니다.")
        LocalDate expendedDate,
        
        LocalTime expendedTime,
        
        Long categoryId,
        
        MealType mealType,
        
        @Size(max = 500)
        String memo,
        
        @Valid
        List<CartItemRequest> items
) {
    /**
     * 장바구니 항목 요청 DTO
     */
    public record CartItemRequest(
            @NotNull(message = "음식 ID는 필수입니다.")
            Long foodId,            // ◆ 음식 FK
            
            @NotNull(message = "음식명은 필수입니다.")
            @Size(max = 500)
            String foodName,        // ◆ 비정규화 저장
            
            @NotNull(message = "수량은 필수입니다.")
            @Min(value = 1)
            Integer quantity,
            
            @NotNull(message = "가격은 필수입니다.")
            @Min(value = 0)
            Integer price
    ) {}
}
```

### 5.2 Response DTO 변경

```java
/**
 * 지출 내역 등록 응답 DTO
 */
public record CreateExpenditureResponse(
        Long expenditureId,
        Long storeId,               // ◆ 새로 추가
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        String categoryName,
        MealType mealType,
        String memo,
        List<ExpenditureItemResponse> items,
        LocalDateTime createdAt,
        Boolean hasStoreLink        // ◆ 새로 추가: storeId != null 여부
) {
    public static CreateExpenditureResponse from(
            CreateExpenditureServiceResponse serviceResponse
    ) {
        return new CreateExpenditureResponse(
                serviceResponse.expenditureId(),
                serviceResponse.storeId(),          // ◆ 새로 추가
                serviceResponse.storeName(),
                serviceResponse.amount(),
                serviceResponse.expendedDate(),
                serviceResponse.expendedTime(),
                serviceResponse.categoryId(),
                serviceResponse.categoryName(),
                serviceResponse.mealType(),
                serviceResponse.memo(),
                serviceResponse.items().stream()
                        .map(ExpenditureItemResponse::from)
                        .collect(Collectors.toList()),
                serviceResponse.createdAt(),
                serviceResponse.storeId() != null  // ◆ storeId 있으면 true
        );
    }
    
    public record ExpenditureItemResponse(
            Long itemId,
            Long foodId,
            String foodName,
            Integer quantity,
            Integer price,
            Boolean hasFoodLink         // ◆ 새로 추가: foodId != null 여부
    ) {
        public static ExpenditureItemResponse from(
                ExpenditureItemServiceResponse response
        ) {
            return new ExpenditureItemResponse(
                    response.itemId(),
                    response.foodId(),
                    response.foodName(),
                    response.quantity(),
                    response.price(),
                    response.foodId() != null       // ◆ foodId 있으면 true
            );
        }
    }
}
```

### 5.3 Service Layer DTO

```java
/**
 * 지출 내역 등록 서비스 요청 DTO
 */
public record CreateExpenditureServiceRequest(
        Long memberId,
        Long storeId,               // ◆ nullable
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        MealType mealType,
        String memo,
        List<ExpenditureItemServiceRequest> items
) {
    public record ExpenditureItemServiceRequest(
            Long foodId,            // ◆ nullable
            String foodName,        // ◆ 새로 추가
            Integer quantity,
            Integer price
    ) {}
}

/**
 * 지출 내역 등록 서비스 응답 DTO
 */
public record CreateExpenditureServiceResponse(
        Long expenditureId,
        Long storeId,               // ◆ nullable
        String storeName,
        Integer amount,
        LocalDate expendedDate,
        LocalTime expendedTime,
        Long categoryId,
        String categoryName,
        MealType mealType,
        String memo,
        List<ExpenditureItemServiceResponse> items,
        LocalDateTime createdAt
) {
    public static CreateExpenditureServiceResponse from(
            Expenditure expenditure,
            String categoryName
    ) {
        return new CreateExpenditureServiceResponse(
                expenditure.getExpenditureId(),
                expenditure.getStoreId(),           // ◆ 새로 추가
                expenditure.getStoreName(),
                expenditure.getAmount(),
                expenditure.getExpendedDate(),
                expenditure.getExpendedTime(),
                expenditure.getCategoryId(),
                categoryName,
                expenditure.getMealType(),
                expenditure.getMemo(),
                expenditure.getItems().stream()
                        .map(ExpenditureItemServiceResponse::from)
                        .collect(Collectors.toList()),
                expenditure.getCreatedAt()
        );
    }
    
    public record ExpenditureItemServiceResponse(
            Long itemId,
            Long foodId,            // ◆ nullable
            String foodName,        // ◆ 새로 추가
            Integer quantity,
            Integer price
    ) {
        public static ExpenditureItemServiceResponse from(ExpenditureItem item) {
            return new ExpenditureItemServiceResponse(
                    item.getExpenditureItemId(),
                    item.getFoodId(),               // ◆ nullable
                    item.getFoodName(),             // ◆ 새로 추가
                    item.getQuantity(),
                    item.getPrice()
            );
        }
    }
}
```

---

## 6. Controller 변경

### 6.1 기존 엔드포인트 (수기 입력)

```java
/**
 * 지출 내역 등록 - 수기 입력
 * POST /api/v1/expenditures
 */
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<CreateExpenditureResponse> createExpenditure(
        @AuthUser AuthenticatedUser user,
        @Valid @RequestBody CreateExpenditureRequest request
) {
    // 수기 입력: storeId = NULL
    CreateExpenditureServiceRequest serviceRequest = 
            new CreateExpenditureServiceRequest(
                    user.memberId(),
                    null,                           // storeId = NULL
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
                                    null,           // foodId = NULL
                                    item.foodName(),
                                    item.quantity(),
                                    item.price()
                            ))
                            .collect(Collectors.toList())
            );
    
    CreateExpenditureServiceResponse serviceResponse = 
            createExpenditureService.createExpenditure(serviceRequest);
    
    return ApiResponse.success(CreateExpenditureResponse.from(serviceResponse));
}
```

### 6.2 새 엔드포인트 (장바구니)

```java
/**
 * 장바구니 → 지출 내역 등록
 * POST /api/v1/expenditures/from-cart
 * 
 * 장바구니에서 직접 지출을 등록할 때 사용합니다.
 * storeId와 foodId 정보를 포함하여 저장합니다.
 */
@PostMapping("/from-cart")
@ResponseStatus(HttpStatus.CREATED)
public ApiResponse<CreateExpenditureResponse> createExpenditureFromCart(
        @AuthUser AuthenticatedUser user,
        @Valid @RequestBody CreateExpenditureFromCartRequest request
) {
    // 장바구니: storeId + foodId 포함
    CreateExpenditureServiceRequest serviceRequest = 
            new CreateExpenditureServiceRequest(
                    user.memberId(),
                    request.storeId(),              // storeId 포함
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
                                    item.foodId(),  // foodId 포함
                                    item.foodName(),
                                    item.quantity(),
                                    item.price()
                            ))
                            .collect(Collectors.toList())
            );
    
    CreateExpenditureServiceResponse serviceResponse = 
            createExpenditureService.createExpenditure(serviceRequest);
    
    return ApiResponse.success(CreateExpenditureResponse.from(serviceResponse));
}
```

---

## 7. Service Layer 변경

### 7.1 CreateExpenditureService

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateExpenditureService {
    
    private final ExpenditureDomainService expenditureDomainService;
    
    @Transactional
    public CreateExpenditureServiceResponse createExpenditure(
            CreateExpenditureServiceRequest request
    ) {
        // storeId 존재 여부에 따라 다른 팩토리 메서드 사용
        Expenditure expenditure;
        
        if (request.storeId() != null) {
            // 장바구니 → 지출 (storeId 포함)
            expenditure = expenditureDomainService.createExpenditureFromCart(
                    request.memberId(),
                    request.storeId(),
                    request.storeName(),
                    request.amount(),
                    request.expendedDate(),
                    request.expendedTime(),
                    request.categoryId(),
                    request.mealType(),
                    request.memo(),
                    request.items().stream()
                            .map(item -> new ExpenditureDomainService.ExpenditureItemRequest(
                                    item.foodId(),
                                    item.foodName(),
                                    item.quantity(),
                                    item.price()
                            ))
                            .collect(Collectors.toList())
            );
        } else {
            // 수기 입력 → 지출 (storeId = NULL)
            expenditure = expenditureDomainService.createExpenditureFromManualInput(
                    request.memberId(),
                    request.storeName(),
                    request.amount(),
                    request.expendedDate(),
                    request.expendedTime(),
                    request.categoryId(),
                    request.mealType(),
                    request.memo(),
                    request.items().stream()
                            .map(item -> new ExpenditureDomainService.ExpenditureItemRequest(
                                    item.foodId(),
                                    item.foodName(),
                                    item.quantity(),
                                    item.price()
                            ))
                            .collect(Collectors.toList())
            );
        }
        
        // 저장소에 저장
        Expenditure saved = expenditureRepository.save(expenditure);
        
        // 응답 생성
        String categoryName = getCategoryName(saved.getCategoryId());
        return CreateExpenditureServiceResponse.from(saved, categoryName);
    }
    
    private String getCategoryName(Long categoryId) {
        // 카테고리 조회 로직
        if (categoryId == null) return null;
        // ...
    }
}
```

### 7.2 ExpenditureDomainService

```java
@Service
@RequiredArgsConstructor
public class ExpenditureDomainService {
    
    /**
     * 장바구니 → 지출 생성
     */
    public Expenditure createExpenditureFromCart(
            Long memberId,
            Long storeId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ExpenditureItemRequest> itemRequests
    ) {
        // 항목 생성
        List<ExpenditureItem> items = itemRequests.stream()
                .map(req -> ExpenditureItem.createFromFood(
                        req.foodId(),
                        req.foodName(),
                        req.quantity(),
                        req.price()
                ))
                .collect(Collectors.toList());
        
        // 지출 생성
        return Expenditure.createFromCart(
                memberId,
                storeId,
                storeName,
                amount,
                expendedDate,
                expendedTime,
                categoryId,
                mealType,
                memo,
                items
        );
    }
    
    /**
     * 수기 입력 → 지출 생성
     */
    public Expenditure createExpenditureFromManualInput(
            Long memberId,
            String storeName,
            Integer amount,
            LocalDate expendedDate,
            LocalTime expendedTime,
            Long categoryId,
            MealType mealType,
            String memo,
            List<ExpenditureItemRequest> itemRequests
    ) {
        // 항목 생성
        List<ExpenditureItem> items = itemRequests.stream()
                .map(req -> ExpenditureItem.createFromManualInput(
                        req.foodName(),
                        req.quantity(),
                        req.price()
                ))
                .collect(Collectors.toList());
        
        // 지출 생성
        return Expenditure.createFromManualInput(
                memberId,
                storeName,
                amount,
                expendedDate,
                expendedTime,
                categoryId,
                mealType,
                memo,
                items
        );
    }
    
    public record ExpenditureItemRequest(
            Long foodId,            // nullable
            String foodName,
            Integer quantity,
            Integer price
    ) {}
}
```

---

## 8. 마이그레이션 체크리스트

### Phase 1: 준비 (Day 1)
- [ ] 스키마 변경 SQL 검증
- [ ] 도메인 모델 작성
- [ ] 테스트 케이스 작성 (양 시나리오)

### Phase 2: 구현 (Day 2-3)
- [ ] JPA 엔티티 변경
- [ ] DTO 작성
- [ ] Service Layer 구현
- [ ] Controller 구현
- [ ] 기존 테스트 마이그레이션

### Phase 3: 검증 (Day 4)
- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과
- [ ] REST Docs 업데이트
- [ ] 기존 API 호환성 검증

### Phase 4: 배포 (Day 5)
- [ ] 스키마 마이그레이션
- [ ] 롤백 계획 수립
- [ ] 모니터링 설정

---

## 9. 호환성 및 주의사항

### ✓ 기존 API 호환성
```
POST /api/v1/expenditures  → 수기 입력 (기존 방식)
POST /api/v1/expenditures/from-cart  → 장바구니 (새로운 방식)
```

### ✓ 데이터 호환성
```
기존 데이터: storeId = NULL, foodId 필수 (기존 값 유지)
새 데이터:   storeId 선택, foodId 선택 (시나리오별)
```

### ⚠️ 주의사항
1. **NULL 제약 조건**: food_id를 nullable로 변경 시 기존 UNIQUE 제약 영향 (선택사항)
2. **마이그레이션**: 기존 데이터는 이미 foodId가 채워져 있으므로 자동 호환
3. **쿼리 영향**: foodId 필터링 시 `IS NOT NULL` 조건 추가 필요

---

## 10. 예제

### 10.1 장바구니 시나리오

```bash
POST /api/v1/expenditures/from-cart

{
  "storeId": 123,
  "storeName": "맘스터치 강남점",
  "amount": 13500,
  "expendedDate": "2025-10-23",
  "expendedTime": "12:30:00",
  "categoryId": 5,
  "mealType": "LUNCH",
  "memo": "동료와 점심",
  "items": [
    {
      "foodId": 456,
      "foodName": "싸이버거 세트",
      "quantity": 1,
      "price": 6500
    },
    {
      "foodId": 789,
      "foodName": "치킨버거 세트",
      "quantity": 1,
      "price": 7000
    }
  ]
}

Response 200 OK:
{
  "result": "SUCCESS",
  "data": {
    "expenditureId": 999,
    "storeId": 123,              // ◆ storeId 포함
    "storeName": "맘스터치 강남점",
    "amount": 13500,
    "expendedDate": "2025-10-23",
    "categoryId": 5,
    "categoryName": "패스트푸드",
    "mealType": "LUNCH",
    "items": [
      {
        "itemId": 1,
        "foodId": 456,            // ◆ foodId 포함
        "foodName": "싸이버거 세트",
        "quantity": 1,
        "price": 6500,
        "hasFoodLink": true       // ◆ 상세 페이지 링크 가능
      },
      {
        "itemId": 2,
        "foodId": 789,
        "foodName": "치킨버거 세트",
        "quantity": 1,
        "price": 7000,
        "hasFoodLink": true
      }
    ],
    "createdAt": "2025-10-23T12:34:56.789Z",
    "hasStoreLink": true          // ◆ 가게 상세 페이지 링크 가능
  }
}
```

### 10.2 수기 입력 시나리오

```bash
POST /api/v1/expenditures

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
      "foodName": "김밥",
      "quantity": 1,
      "price": 3000
    },
    {
      "foodName": "우유",
      "quantity": 1,
      "price": 2000
    }
  ]
}

Response 200 OK:
{
  "result": "SUCCESS",
  "data": {
    "expenditureId": 1000,
    "storeId": null,              // ◆ storeId = NULL
    "storeName": "편의점 (이름 모름)",
    "amount": 5000,
    "expendedDate": "2025-10-23",
    "categoryId": null,
    "categoryName": null,
    "mealType": "OTHER",
    "items": [
      {
        "itemId": 3,
        "foodId": null,            // ◆ foodId = NULL
        "foodName": "김밥",
        "quantity": 1,
        "price": 3000,
        "hasFoodLink": false       // ◆ 상세 페이지 링크 불가
      },
      {
        "itemId": 4,
        "foodId": null,
        "foodName": "우유",
        "quantity": 1,
        "price": 2000,
        "hasFoodLink": false
      }
    ],
    "createdAt": "2025-10-23T15:34:56.789Z",
    "hasStoreLink": false         // ◆ 가게 상세 페이지 링크 불가
  }
}
```

---

## 11. 테스트 계획

### 11.1 단위 테스트

```java
class ExpenditureTest {
    
    @Test
    void createFromCart_withStoreId_success() {
        // Given: storeId 포함
        // When: createFromCart 호출
        // Then: storeId 채워진 상태로 생성
    }
    
    @Test
    void createFromManualInput_withoutStoreId_success() {
        // Given: storeId 없음
        // When: createFromManualInput 호출
        // Then: storeId = NULL 상태로 생성
    }
}

class ExpenditureItemTest {
    
    @Test
    void createFromFood_withFoodId_success() {
        // Given: foodId + foodName
        // When: createFromFood 호출
        // Then: foodId 채워진 상태로 생성
    }
    
    @Test
    void createFromManualInput_withoutFoodId_success() {
        // Given: foodId 없음, 음식명만
        // When: createFromManualInput 호출
        // Then: foodId = NULL, foodName 저장
    }
}
```

### 11.2 통합 테스트

```java
class ExpenditureControllerIntegrationTest {
    
    @Test
    void createExpenditureFromCart_success() throws Exception {
        // POST /api/v1/expenditures/from-cart
        // Verify storeId, foodId 저장됨
    }
    
    @Test
    void createExpenditure_manualInput_success() throws Exception {
        // POST /api/v1/expenditures
        // Verify storeId = NULL, foodId = NULL
    }
}
```

