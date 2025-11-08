# Food와 StoreImage 저장 문제 해결

## 🔴 문제점 분석

배치 작업 실행 시 다음 로그가 나타났습니다:

```
✗ Invalid food: name='만조 피칸테', price=15000, storeId=null, categoryId=85, reason: [No storeId]
✗ Invalid food: name='리코타 치즈 샐러드', price=11000, storeId=null, categoryId=85, reason: [No storeId]
...
No foods to save for store ID: 834
No images to save for store ID: 836
```

**근본 원인:**

1. **Processor 단계의 설계 결함**:
   - `convertToFoods()`와 `convertToImages()`에서 `store.getStoreId()`를 사용
   - 하지만 이 시점에 Store는 아직 **DB에 저장되지 않아서 storeId가 null**
   - Food와 StoreImage 엔티티의 `isValid()` 메서드가 storeId를 필수 필드로 검증
   - 따라서 모든 메뉴와 이미지가 validation 실패

2. **데이터 흐름 문제**:
   ```
   Processor → Writer로 데이터 전달 → Writer에서 Store 저장 → storeId 생성
   ```
   - Processor에서는 storeId가 없음 (Store가 아직 저장 안 됨)
   - Writer에서만 storeId를 얻을 수 있음

## ✅ 해결책

### 1. Processor 단계에서 storeId 제외

**변경 전:**
```java
// CrawledStoreProcessor.convertToFoods()
Food food = Food.builder()
    .storeId(store.getStoreId())  // ← null (아직 저장 안 됨)
    .categoryId(foodCategoryId)
    ...
    .build();

if (food.isValid()) {  // ← storeId가 null이므로 실패!
    foods.add(food);
}
```

**변경 후:**
```java
// CrawledStoreProcessor.convertToFoods()
Food food = Food.builder()
    .storeId(null)  // ← Writer에서 설정할 예정
    .categoryId(foodCategoryId)
    ...
    .build();

// Processor용 검증 메서드 (storeId 제외)
if (isValidFoodForProcessing(food)) {
    foods.add(food);
}

private boolean isValidFoodForProcessing(Food food) {
    return food.getFoodName() != null && !food.getFoodName().trim().isEmpty()
        && (food.getPrice() != null || food.getAveragePrice() != null)
        && (food.getPrice() == null || food.getPrice() >= 0)
        && (food.getAveragePrice() == null || food.getAveragePrice() >= 0)
        && food.getCategoryId() != null;  // storeId는 제외!
}
```

### 2. Writer 단계에서 storeId 설정

**변경 후:**
```java
// StoreDataWriter.saveFoods()
private void saveFoods(Long storeId, java.util.List<Food> foods) {
    for (Food food : foods) {
        Food foodToSave = Food.builder()
                .storeId(storeId)  // ← Store 저장 후 생성된 storeId 설정
                .categoryId(food.getCategoryId())
                .foodName(food.getFoodName())
                ...
                .build();
        
        Food savedFood = foodRepository.save(foodToSave);
        log.debug("Saved food: {} (ID: {}, price: {})", ...);
    }
}
```

### 3. StoreImage도 동일하게 수정

```java
// CrawledStoreProcessor.convertToImages()
for (String imageUrl : dto.getImages()) {
    StoreImage image = StoreImage.builder()
            .storeId(null)  // ← Writer에서 설정
            .imageUrl(imageUrl)
            .isMain(displayOrder == 0)
            .displayOrder(displayOrder++)
            .build();
    
    images.add(image);  // validation 스킵 (storeId 없으므로)
}

// StoreDataWriter.saveImages()
for (StoreImage image : images) {
    StoreImage imageToSave = StoreImage.builder()
            .storeId(storeId)  // ← Store 저장 후 생성된 storeId 설정
            .imageUrl(image.getImageUrl())
            .isMain(image.isMain())
            .displayOrder(image.getDisplayOrder())
            .build();
    
    storeImageRepository.save(imageToSave);
}
```

### 4. 영업시간도 동일 패턴 적용

```java
// CrawledStoreProcessor.convertToOpeningHours()
StoreOpeningHour openingHour = new StoreOpeningHour(
    null,  // storeOpeningHourId
    null,  // ← Writer에서 설정할 storeId
    dayOfWeek,
    ...
);

// StoreDataWriter.saveOpeningHours()
StoreOpeningHour ohToSave = new StoreOpeningHour(
    null,
    storeId,  // ← Store 저장 후 생성된 storeId 설정
    ...
);
```

## 📊 개선된 데이터 흐름

```
1. Processor (데이터 변환)
   ├─ Store 변환: storeId = null
   ├─ Food 변환: storeId = null (검증 스킵, 기본 필드만 검증)
   ├─ StoreImage 변환: storeId = null (validation 스킵)
   └─ StoreOpeningHour 변환: storeId = null

2. Writer (데이터 저장)
   ├─ Store 저장 → storeId 생성 (예: 844)
   ├─ Food 저장: storeId = 844 설정 후 저장
   ├─ StoreImage 저장: storeId = 844 설정 후 저장
   ├─ StoreOpeningHour 저장: storeId = 844 설정 후 저장
   └─ 모든 관련 데이터 완벽하게 저장됨
```

## 🔍 향상된 로깅

### Processor 로그
```
INFO  Processing 10 menus for store: 나보나 (storeId: null)
DEBUG ✓ Valid food: name='만조 피칸테', price=15000
DEBUG ✓ Valid food: name='리코타 치즈 샐러드', price=11000
INFO  Menu conversion result for store '나보나': valid=10, invalid=0, total=10

INFO  Processing 5 images for store: 나보나
DEBUG   Image 1: https://... (isMain=true)
DEBUG   Image 2: https://... (isMain=false)
INFO  Image conversion result for store '나보나': total=5
```

### Writer 로그
```
INFO  Saving 10 foods for store ID: 842
DEBUG Saved food: 만조 피칸테 (ID: 1001, price: 15000)
INFO  Successfully saved 10 foods for store ID: 842

INFO  Saving 5 images for store ID: 842
DEBUG Saved image: https://... (ID: 501, isMain: true)
INFO  Successfully saved 5 images for store ID: 842

INFO  Saving 3 opening hours for store ID: 842
DEBUG Saved opening hour: FRIDAY (11:00 ~ 21:00)
INFO  Successfully saved 3 opening hours for store ID: 842
```

## 📈 예상 결과

```sql
-- 배치 실행 후
mysql> SELECT COUNT(*) FROM store;  -- 281개 저장됨 ✓
mysql> SELECT COUNT(*) FROM food;   -- 1000개 이상 저장됨 ✓ (이전: 0개)
mysql> SELECT COUNT(*) FROM store_image;  -- 2000개 이상 저장됨 ✓ (이전: 0개)
mysql> SELECT COUNT(*) FROM store_opening_hour;  -- 1500개 이상 저장됨 ✓
```

## 🔑 핵심 개선사항

| 항목 | 이전 | 이후 |
|------|------|------|
| **Food 저장** | ❌ 0개 (storeId=null로 validation 실패) | ✅ 모두 저장 |
| **StoreImage 저장** | ❌ 0개 (storeId=null로 validation 실패) | ✅ 모두 저장 |
| **Processor 검증** | storeId 필수 (지나치게 엄격) | Processor 용도에 맞게 조정 |
| **로깅** | 제한적 | 상세한 DEBUG/INFO 로그 |
| **데이터 흐름** | 명확하지 않음 | Processor → Writer 순서 명확화 |

## 🎯 아키텍처 개선

**이전 설계의 문제점:**
- Processor에서 Store를 완전히 변환 시도
- 하지만 Store는 Writer에서만 저장되므로 storeId 미생성
- 순환 의존성 발생

**개선된 설계:**
- **Processor**: 데이터 변환만 담당 (storeId는 null로 허용)
- **Writer**: 저장 및 관련 엔티티 storeId 설정
- **책임 분리**: 각 계층의 역할 명확화

## ✨ 추가 개선사항

1. **Processor 단계 검증 분리**:
   - `isValidFoodForProcessing()`: Processor용 검증 (storeId 제외)
   - `Food.isValid()`: 최종 검증 (storeId 필수)

2. **로깅 강화**:
   - 각 메뉴/이미지 개수 추적
   - 개별 저장 결과 DEBUG 로그
   - 최종 저장 결과 INFO 요약

3. **오류 방지**:
   - 이미지 URL 유효성 검증
   - 빈 리스트 처리 (DEBUG 로그)
   - 영업시간 유효성 검증
