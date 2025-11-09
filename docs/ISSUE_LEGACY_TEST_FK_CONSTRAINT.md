# 기존 API 테스트 FK 제약조건 오류 수정 필요

**이슈 생성일**: 2025-11-10  
**우선순위**: Medium  
**상태**: 🔴 To Do

---

## 📋 문제 요약

Phase 3 (검색 기능 강화) 구현 과정에서 Store 엔티티의 Category 관계가 N:M으로 변경되면서, 기존 Store 관련 API 테스트들이 FK 제약조건 위반으로 실패하고 있습니다.

---

## 🔍 문제 상세

### 에러 메시지
```
org.springframework.dao.DataIntegrityViolationException: 
could not execute statement [Cannot add or update a child row: 
a foreign key constraint fails (`smartmealtable_test`.`store_category`, 
CONSTRAINT `FKm2p2repecp4mx2i2ibmw75deb` 
FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`))]
```

### 근본 원인
기존 테스트 코드들이 Store 생성 시 `categoryIds(List.of(1L))`처럼 하드코딩된 카테고리 ID를 사용하고 있으나, 테스트 DB에 해당 카테고리가 존재하지 않아 FK 제약조건 위반이 발생합니다.

### Phase 3 변경사항
- **이전**: Store가 단일 Category와 N:1 관계
- **이후**: Store가 여러 Category와 N:M 관계 (중간 테이블 `store_category` 사용)

---

## 📊 영향받는 테스트

### 실패한 테스트 개수
- **총 57개 테스트 실패** (전체 515개 중)

### 영향받는 테스트 파일
1. **Store 관련 테스트** (약 30개)
   - `GetStoreAutocompleteControllerTest.java` ✅ **수정 완료**
   - `GetStoreDetailControllerTest.java` ❌ 수정 필요
   - `GetStoreListControllerTest.java` ❌ 수정 필요
   - `GetStoreFoodsControllerTest.java` ❌ 수정 필요
   - `StoreControllerRestDocsTest.java` ❌ 수정 필요

2. **Food 관련 테스트** (약 5개)
   - `GetFoodDetailControllerTest.java` ❌ 수정 필요
   - `GetFoodDetailRestDocsTest.java` ❌ 수정 필요

3. **Cart 관련 테스트** (약 12개)
   - `CartControllerRestDocsTest.java` ❌ 수정 필요

4. **기타 연관 테스트** (약 10개)

---

## ✅ 해결 방법

### 수정 패턴 (GetStoreAutocompleteControllerTest 참고)

#### Before (실패하는 코드)
```java
@BeforeEach
void setUp() {
    createTestStores();
}

private Store createStore(String name, String address) {
    return Store.builder()
            .name(name)
            .categoryIds(java.util.List.of(1L))  // ❌ 존재하지 않는 ID
            .sellerId(1L)
            ...
            .build();
}
```

#### After (수정된 코드)
```java
@Autowired
private CategoryRepository categoryRepository;

private Long categoryId;

@BeforeEach
void setUp() {
    // 카테고리 먼저 생성 (FK 제약조건 충족)
    Category category = categoryRepository.save(Category.create("한식"));
    categoryId = category.getCategoryId();
    
    createTestStores();
}

private Store createStore(String name, String address) {
    return Store.builder()
            .name(name)
            .categoryIds(java.util.List.of(categoryId))  // ✅ 실제 생성된 ID 사용
            .sellerId(1L)
            ...
            .build();
}
```

---

## 📝 작업 계획

### Step 1: Store 관련 테스트 수정
- [ ] GetStoreDetailControllerTest.java
- [ ] GetStoreListControllerTest.java
- [ ] GetStoreFoodsControllerTest.java
- [ ] StoreControllerRestDocsTest.java

### Step 2: Food 관련 테스트 수정
- [ ] GetFoodDetailControllerTest.java
- [ ] GetFoodDetailRestDocsTest.java

### Step 3: Cart 관련 테스트 수정
- [ ] CartControllerRestDocsTest.java

### Step 4: 기타 연관 테스트 수정
- [ ] 나머지 실패하는 테스트들 확인 및 수정

### Step 5: 전체 테스트 검증
```bash
./gradlew clean build
```

---

## 🎯 수정 우선순위

### High Priority
- Store Detail/List/Foods API 테스트 (핵심 기능)

### Medium Priority
- Food Detail API 테스트
- REST Docs 테스트들

### Low Priority
- Cart API 테스트 (별도 기능 영역)

---

## ⏱️ 예상 작업 시간

- **단일 테스트 파일 수정**: 약 5분
- **전체 57개 테스트 수정**: 약 2-3시간
- **검증 및 문서화**: 약 30분

**총 예상 시간**: **3-4시간**

---

## 📌 주의사항

### CategoryRepository 의존성
모든 Store 관련 테스트에 CategoryRepository 주입 필요:
```java
@Autowired
private CategoryRepository categoryRepository;
```

### 테스트 격리 (Test Isolation)
각 테스트마다 독립적으로 Category를 생성하여 테스트 간 의존성을 제거합니다.

### REST Docs 응답 필드 검증
Autocomplete API의 경우 응답 필드가 변경되었으므로 문서화도 함께 업데이트 필요:
- ❌ `categoryName` (String)
- ✅ `categoryNames` (List<String>)
- ✅ `storeType` (StoreType)

---

## ✅ 완료 조건

- [ ] 전체 테스트 성공 (515/515 PASS)
- [ ] 빌드 성공 (`./gradlew clean build`)
- [ ] REST Docs 문서 생성 확인
- [ ] 수정 내용 문서화

---

## 🔗 관련 문서

- [Phase 3 완료 보고서](./SEARCH_ENHANCEMENT_PHASE3_COMPLETE.md)
- [전체 프로젝트 요약](./SEARCH_ENHANCEMENT_FINAL_SUMMARY.md)

---

**작성자**: Development Team  
**문서 버전**: 1.0
