# 음식 메뉴 미저장 문제 진단 및 해결

## 문제 상황
```
mysql> select * from food;
Empty set (0.01 sec)
```

음식 메뉴가 Food 테이블에 저장되지 않고 있습니다.

## 원인 분석

### 근본 원인: Food.isValid() 검증 실패
```java
public boolean isValid() {
    return foodName != null && !foodName.trim().isEmpty() 
        && (price != null || averagePrice != null)  // ← 가격이 필수!
        && (price == null || price >= 0)
        && (averagePrice == null || averagePrice >= 0)
        && storeId != null
        && categoryId != null;
}
```

**가장 가능성 높은 원인들:**

1. **가격(price) 데이터가 NULL** ❌
   - JSON 메뉴에 `price` 필드가 없음
   - MenuInfo.getPrice()가 NULL 반환

2. **음식명(foodName)이 비어있음** ❌
   - MenuInfo.getName()이 NULL 또는 공백
   - trim() 후 빈 문자열

3. **메뉴 리스트가 비어있음** ❌
   - JSON의 `menus` 배열이 없거나 빈 상태

4. **categoryId가 설정되지 않음** ❌
   - resolveFoodCategoryId()가 NULL 반환
   - CategoryRepository 오류

5. **storeId가 설정되지 않음** ❌
   - Store가 저장되지 않아 storeId가 NULL

## 해결책

### 1. 상세 로깅 추가
모든 음식 변환 및 저장 과정에서 상세한 로그 출력:

**CrawledStoreProcessor.convertToFoods():**
```
INFO: Processing 5 menus for store: 김밥천국 (storeId: 123)
✓ Valid food: name='김밥', price=3000, storeId=123
✗ Invalid food: name='', price=null, storeId=123, categoryId=1, reason: [No name][No price/averagePrice]
✗ Invalid food: name='국수', price=-100, storeId=123, categoryId=1, reason: [Negative price]
INFO: Menu conversion result for store '김밥천국': valid=3, invalid=2, total=5
```

**StoreDataWriter.saveFoods():**
```
INFO: Saving 3 foods for store ID: 123
✓ Saved food: 김밥 (ID: 456, price: 3000) to store ID: 123
✓ Saved food: 국수 (ID: 457, price: 4000) to store ID: 123
✓ Saved food: 국 (ID: 458, price: 2000) to store ID: 123
INFO: Successfully saved 3 foods for store ID: 123
```

### 2. 무효한 이유 분석 메서드
```java
private String getInvalidReasons(Food food) {
    StringBuilder reasons = new StringBuilder();
    
    if (food.getFoodName() == null || food.getFoodName().trim().isEmpty()) {
        reasons.append("[No name]");
    }
    if (food.getPrice() == null && food.getAveragePrice() == null) {
        reasons.append("[No price/averagePrice]");
    }
    if (food.getPrice() != null && food.getPrice() < 0) {
        reasons.append("[Negative price]");
    }
    if (food.getStoreId() == null) {
        reasons.append("[No storeId]");
    }
    if (food.getCategoryId() == null) {
        reasons.append("[No categoryId]");
    }
    
    return reasons.length() > 0 ? reasons.toString() : "[Unknown reason]";
}
```

### 3. 명시적 averagePrice 설정
```java
Food food = Food.builder()
    .foodId(null)
    .storeId(store.getStoreId())
    .categoryId(foodCategoryId)
    .foodName(menuInfo.getName())
    .description(menuInfo.getIntroduce())
    .price(menuInfo.getPrice())              // ← 크롤러용 가격
    .averagePrice(null)                      // ← 명시적으로 NULL (추천 시스템용)
    .imageUrl(menuInfo.getImgUrl())
    .isMain(menuInfo.getIsMain() != null && menuInfo.getIsMain())
    .displayOrder(displayOrder++)
    .registeredDt(null)
    .deletedAt(null)
    .build();
```

## 진단 방법

### 배치 작업 실행 후 로그 확인:
```bash
# 로그 레벨을 DEBUG로 설정하고 실행
# application-batch.yml 또는 logback.xml에서:
logging:
  level:
    com.stdev.smartmealtable.batch.crawler.job.processor: DEBUG
    com.stdev.smartmealtable.batch.crawler.job.writer: INFO
```

### 확인해야 할 로그:
1. **"Processing X menus for store"** - 메뉴가 몇 개 읽혔는가?
2. **"✓ Valid food"** - 유효한 음식이 몇 개인가?
3. **"✗ Invalid food"** - 무효한 이유가 무엇인가?
4. **"Saving X foods"** - 저장하려는 음식이 몇 개인가?
5. **"Saved food: X"** - 실제로 저장되었는가?

## 체크리스트

음식이 저장되지 않는 문제를 해결하려면:

- [ ] 배치 작업 로그 확인 (위의 로그 패턴)
- [ ] JSON 파일 검증
  - [ ] `menus` 배열이 존재하는가?
  - [ ] 각 메뉴에 `name` 필드가 있는가?
  - [ ] 각 메뉴에 `price` 필드가 있는가? (숫자 타입)
  - [ ] 가격이 음수가 아닌가?
- [ ] 데이터베이스 확인
  - [ ] Store가 저장되었는가? (store 테이블 확인)
  - [ ] Category가 저장되었는가? (category 테이블 확인)
- [ ] 트랜잭션 상태 확인
  - [ ] 배치 작업이 성공 상태로 완료되었는가?
  - [ ] 롤백되지 않았는가?

## 예상 시나리오

### 시나리오 1: price가 NULL
```json
{
  "menus": [
    {
      "name": "김밥",
      // price가 없음!
      "imgUrl": "..."
    }
  ]
}
```
→ `isValid()` 실패 → 저장 안 됨

### 시나리오 2: name이 빈 문자열
```json
{
  "menus": [
    {
      "name": "",  // 빈 문자열
      "price": 3000,
      "imgUrl": "..."
    }
  ]
}
```
→ `isValid()` 실패 → 저장 안 됨

### 시나리오 3: menus 배열이 없음
```json
{
  "id": "123",
  "name": "음식점",
  // menus가 없음!
}
```
→ convertToFoods() 반환 빈 리스트 → 저장할 음식 없음

## 다음 단계

1. **배치 작업 실행 후 로그 확인**
   ```bash
   ./gradlew smartmealtable-batch:crawler:bootRun \
     --args='inputFilePath=file:///path/to/data.json' \
     -Dlogging.level.com.stdev=DEBUG
   ```

2. **로그에서 invalid 음식 찾기**
   - `✗ Invalid food` 패턴으로 검색
   - 무효한 이유 확인

3. **JSON 파일 수정**
   - price 필드 추가
   - name 필드 검증
   - 필수 필드 확인

4. **다시 배치 작업 실행**
   - 로그 재확인
   - Food 테이블 데이터 확인
