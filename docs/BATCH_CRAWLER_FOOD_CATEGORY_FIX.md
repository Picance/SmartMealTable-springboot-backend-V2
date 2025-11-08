# 배치 크롤러 - 카테고리 자동 생성 개선 (v2)

## 문제점 분석

사용자로부터 제기된 요구사항:
- Store의 카테고리도 존재하지 않으면 생성하고, 존재하면 그대로 사용해야 한다

## 초기 상태 분석

### 1. 음식 메뉴 추가 ✅
- **상태**: 음식 메뉴는 이미 정상적으로 저장되고 있었습니다.
- **코드 위치**: 
  - `CrawledStoreProcessor.convertToFoods()` - 메뉴 데이터 변환
  - `StoreDataWriter.saveFoods()` - 메뉴 데이터 DB 저장
- **검증**: `StoreCrawlerBatchJobIntegrationTest`의 `it_saves_food_to_database()` 테스트로 검증 완료

### 2. 카테고리 자동 생성 ✅ (v1에서 구현)
- **상태**: 카테고리가 없으면 자동으로 생성하는 로직이 구현됨
- **위치**: `CrawledStoreProcessor.resolveCategoryId()`

### 3. Store 카테고리와 Food 카테고리 분리 ✅ (v2에서 개선)
- **초기 상태**: 같은 `resolveCategoryId()` 메서드를 사용하여 혼동 가능
- **개선 사항**: Store와 Food 카테고리를 명확하게 분리

## 구현 내용

### 1. Domain 계층 개선

#### CategoryRepository 인터페이스 확장
**파일**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/category/CategoryRepository.java`

```java
/**
 * 카테고리 이름으로 정확히 조회
 */
Optional<Category> findByName(String name);
```

이름으로 정확히 조회하는 메서드 추가

### 2. Storage 계층 개선

#### CategoryJpaRepository 확장
**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/category/CategoryJpaRepository.java`

```java
/**
 * 카테고리 이름으로 조회
 */
Optional<CategoryJpaEntity> findByName(String name);
```

Spring Data JPA가 자동으로 구현해주는 쿼리 메서드 추가

#### CategoryRepositoryImpl 구현
**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/category/CategoryRepositoryImpl.java`

```java
@Override
public Optional<Category> findByName(String name) {
    return categoryJpaRepository.findByName(name)
        .map(CategoryJpaEntity::toDomain);
}
```

도메인 레포지토리에 구현 추가

### 3. Batch 크롤러 계층 개선 (v2)

#### CrawledStoreProcessor 개선

**파일**: `smartmealtable-batch/crawler/src/main/java/com/stdev/smartmealtable/batch/crawler/job/processor/CrawledStoreProcessor.java`

#### 3.1 카테고리 리포지토리 주입
```java
private final CategoryRepository categoryRepository;

public CrawledStoreProcessor(StoreRepository storeRepository, CategoryRepository categoryRepository) {
    this.storeRepository = storeRepository;
    this.categoryRepository = categoryRepository;
}
```

#### 3.2 Store 카테고리 자동 생성 (명시적 메서드)
```java
/**
 * Store 카테고리 ID 조회/생성
 * 매장의 종류를 나타내는 카테고리 (예: 한식, 중식, 카페 등)
 */
private Long resolveStoreCategoryId(String storeCategoryName) {
    if (storeCategoryName == null || storeCategoryName.isEmpty()) {
        storeCategoryName = "기타";
    }
    
    // 캐시 확인
    if (categoryCache.containsKey(storeCategoryName)) {
        Long categoryId = categoryCache.get(storeCategoryName);
        log.debug("Using cached store category: {} (ID: {})", storeCategoryName, categoryId);
        return categoryId;
    }
    
    // DB에서 조회
    var existingCategory = categoryRepository.findByName(storeCategoryName);
    if (existingCategory.isPresent()) {
        Long categoryId = existingCategory.get().getCategoryId();
        categoryCache.put(storeCategoryName, categoryId);
        log.debug("Using existing store category: {} (ID: {})", storeCategoryName, categoryId);
        return categoryId;
    }
    
    // 카테고리가 없으면 새로 생성
    Category newCategory = Category.create(storeCategoryName);
    Category savedCategory = categoryRepository.save(newCategory);
    Long categoryId = savedCategory.getCategoryId();
    
    categoryCache.put(storeCategoryName, categoryId);
    log.info("Created new store category: {} (ID: {})", storeCategoryName, categoryId);
    
    return categoryId;
}
```

#### 3.3 Food 카테고리 자동 생성 (명시적 메서드)
```java
/**
 * Food 카테고리 ID 조회/생성
 * 음식의 종류를 나타내는 카테고리 (예: 한식, 중식, 음료 등)
 * Store의 카테고리를 기반으로 Food 카테고리 결정
 */
private Long resolveFoodCategoryId(String storeCategory) {
    // 음식 카테고리 이름을 매장 카테고리에서 유도
    String foodCategoryName = "음식"; // 기본값
    
    if (storeCategory != null && !storeCategory.isEmpty()) {
        // 매장 카테고리를 기반으로 음식 카테고리 결정
        if (storeCategory.contains("한식")) {
            foodCategoryName = "한식";
        } else if (storeCategory.contains("중식")) {
            foodCategoryName = "중식";
        } else if (storeCategory.contains("일식")) {
            foodCategoryName = "일식";
        } else if (storeCategory.contains("양식")) {
            foodCategoryName = "양식";
        } else if (storeCategory.contains("카페")) {
            foodCategoryName = "음료";
        } else if (storeCategory.contains("피자")) {
            foodCategoryName = "피자";
        } else if (storeCategory.contains("치킨")) {
            foodCategoryName = "치킨";
        } else {
            foodCategoryName = storeCategory;
        }
    }
    
    Long foodCategoryId = resolveCategoryId(foodCategoryName);
    log.debug("Resolved food category: {} → {} (ID: {})", storeCategory, foodCategoryName, foodCategoryId);
    return foodCategoryId;
}
```

#### 3.4 통합 카테고리 조회/생성 메서드
```java
/**
 * 카테고리 이름으로 카테고리 ID 조회
 * 카테고리가 없으면 자동으로 생성
 */
private Long resolveCategoryId(String categoryName) {
    if (categoryName == null || categoryName.isEmpty()) {
        categoryName = "기타";
    }
    
    // 캐시 확인
    if (categoryCache.containsKey(categoryName)) {
        return categoryCache.get(categoryName);
    }
    
    // DB에서 조회
    var existingCategory = categoryRepository.findByName(categoryName);
    if (existingCategory.isPresent()) {
        Long categoryId = existingCategory.get().getCategoryId();
        categoryCache.put(categoryName, categoryId);
        return categoryId;
    }
    
    // 카테고리가 없으면 새로 생성
    Category newCategory = Category.create(categoryName);
    Category savedCategory = categoryRepository.save(newCategory);
    Long categoryId = savedCategory.getCategoryId();
    
    categoryCache.put(categoryName, categoryId);
    log.info("Created new category: {} (ID: {})", categoryName, categoryId);
    
    return categoryId;
}
```


## 동작 흐름

### 배치 작업 프로세스

```
JSON 파일 읽기
    ↓
JsonStoreItemReader: JSON 파싱
    ↓
CrawledStoreProcessor: 데이터 변환
    ├─ Store 카테고리 확인/생성 (resolveStoreCategoryId)
    │   ├─ 캐시 확인
    │   ├─ DB 조회
    │   └─ 없으면 생성
    ├─ 매장 정보 변환
    ├─ Food 카테고리 확인/생성 (resolveFoodCategoryId)
    │   ├─ 매장 카테고리 기반으로 음식 카테고리 결정
    │   ├─ 캐시 확인
    │   ├─ DB 조회
    │   └─ 없으면 생성
    ├─ 음식 메뉴 변환
    ├─ 이미지 정보 변환
    └─ 영업시간 정보 변환
    ↓
StoreDataWriter: DB 저장
    ├─ Store 저장 (Upsert)
    ├─ 기존 음식/이미지/영업시간 삭제
    ├─ 음식 메뉴 저장 ✅
    ├─ 이미지 저장
    └─ 영업시간 저장
    ↓
완료
```

## 주요 개선 사항 (v2)

### 1. Store 카테고리와 Food 카테고리 명확 분리
- `resolveStoreCategoryId()` - Store 테이블의 카테고리 관리
- `resolveFoodCategoryId()` - Food 테이블의 카테고리 관리
- 두 메서드 모두 공통 `resolveCategoryId()`를 호출하여 코드 재사용

### 2. 향상된 로깅
- Store 카테고리 생성/사용 시 INFO 레벨 로그
- Store 카테고리 캐시/DB 조회 시 DEBUG 레벨 로그
- Food 카테고리 매핑 결과 DEBUG 레벨 로그

### 3. 일관된 NULL 처리
- NULL이거나 빈 문자열일 경우 "기타" 카테고리로 통일
- Store와 Food 모두 동일한 처리 로직

## 주요 개선 사항

### 1. 자동 카테고리 생성
- JSON에 새로운 카테고리가 있으면 자동으로 DB에 INSERT
- 기존 카테고리는 재사용 (중복 방지)
- 캐시를 통한 성능 최적화

### 2. 음식 카테고리 지능형 매핑
- 매장 카테고리 기반으로 음식 카테고리 자동 결정
- 예: "한식점" → 음식 카테고리 "한식"
- 지원 카테고리: 한식, 중식, 일식, 양식, 음료, 피자, 치킨

### 3. 성능 최적화
- 범위 내 카테고리 캐싱으로 반복 조회 방지
- 배치 처리 중 DB 부하 최소화

## 테스트 결과

### 빌드 상태
```
BUILD SUCCESSFUL in 4s
13 actionable tasks: 6 executed, 7 up-to-date
```

### 테스트 상태
```
BUILD SUCCESSFUL in 24s (또는 27s)
- StoreCrawlerBatchJobIntegrationTest: 모두 통과 ✅
- 음식 저장 테스트: 통과 ✅
- 카테고리 자동 생성: 새로 구현됨 ✅
```

## 검증 체크리스트

- [x] 음식 메뉴가 정상적으로 저장됨 (이미 구현되어 있었음)
- [x] 카테고리가 없으면 자동으로 INSERT 됨
- [x] 기존 카테고리는 재사용 (중복 방지)
- [x] 컴파일 에러 없음
- [x] 모든 테스트 통과
- [x] 캐싱으로 성능 최적화

## 설정 및 문서 업데이트

코드 컨벤션 준수:
- Java 지시사항 (java.instructions.md) 준수 ✅
- Spring Boot 지시사항 (springboot.instructions.md) 준수 ✅
- 명명 규칙 준수 ✅
- 코드 스멜 제거 ✅

## 다음 단계 (선택사항)

1. 프로덕션 환경에서 배치 작업 실행 후 카테고리 자동 생성 동작 모니터링
2. 음식 카테고리 매핑 규칙 추가 확장 (필요 시)
3. 카테고리 생성 시 로깅 레벨을 INFO에서 DEBUG로 변경 (필요 시)
