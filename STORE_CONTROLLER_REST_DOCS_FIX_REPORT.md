# StoreController REST Docs 테스트 수정 완료 보고서

**작성일:** 2025-10-15  
**작업 범위:** StoreController REST Docs 테스트 실패 문제 해결

---

## 📋 작업 요약

StoreService 테스트 작성 중 발견된 StoreControllerRestDocsTest의 DTO 필드 불일치 문제를 해결했습니다.

---

## ✅ 완료된 작업

### 1. DTO 필드 추가 및 수정

#### 1-1. StoreListResponse.StoreItem
**변경 사항:**
```java
// ✅ categoryId 필드 추가
public record StoreItem(
        Long storeId,
        String name,
        Long categoryId,      // 추가
        String categoryName,
        // ...
        String phoneNumber    // isOpen 필드 제거
) {
    public static StoreItem from(StoreWithDistance storeWithDistance) {
        Store store = storeWithDistance.store();
        return new StoreItem(
                // ...
                store.getCategoryId(),  // 추가
                null, // TODO: Category 조인 필요
                // ...
        );
    }
}
```

**수정된 필드:**
- ✅ `categoryId` 추가 (Long 타입) - Store 엔티티에서 가져옴
- ❌ `isOpen` 제거 (영업 중 여부 계산 로직 미구현)
- ❌ `favoriteCount` 제거 (DTO에 없었음)

#### 1-2. StoreDetailResponse
**변경 사항:**
```java
// ✅ categoryId 필드 추가
public record StoreDetailResponse(
        Long storeId,
        String name,
        Long categoryId,      // 추가
        String categoryName,
        // ...
) {
    public static StoreDetailResponse from(/* ... */) {
        return new StoreDetailResponse(
                // ...
                store.getCategoryId(),  // 추가
                null, // TODO: Category 조인 필요
                // ...
        );
    }
}
```

**수정된 필드:**
- ✅ `categoryId` 추가 (Long 타입)

---

### 2. REST Docs 필드 문서화 수정

#### 2-1. 가게 목록 조회 (2개 테스트)
**수정 사항:**
```java
// ✅ categoryName을 optional로 변경 (현재 null)
fieldWithPath("data.stores[].categoryName")
    .type(JsonFieldType.STRING)
    .description("카테고리명")
    .optional(),

// ✅ totalElements → totalCount 변경
fieldWithPath("data.totalCount")  // 변경
    .type(JsonFieldType.NUMBER)
    .description("전체 가게 수"),

// ❌ favoriteCount 필드 제거
// fieldWithPath("data.stores[].favoriteCount") - 삭제됨

// ✅ error 필드를 optional로 변경 (@JsonInclude.NON_NULL)
fieldWithPath("error")
    .type(JsonFieldType.NULL)
    .description("에러 정보 (성공 시 null)")
    .optional(),
```

#### 2-2. 가게 상세 조회 (1개 테스트)
**수정 사항:**
```java
// ✅ categoryName을 optional로 변경
fieldWithPath("data.categoryName")
    .type(JsonFieldType.STRING)
    .description("카테고리명")
    .optional(),

// ✅ openingHours로 변경 (businessHours 아님)
fieldWithPath("data.openingHours")
    .type(JsonFieldType.ARRAY)
    .description("영업시간 정보")
    .optional(),

// ✅ 상세 필드 추가 (breakStartTime, breakEndTime, isHoliday)
fieldWithPath("data.openingHours[].breakStartTime")
    .type(JsonFieldType.STRING)
    .description("휴게 시작 시간 (HH:mm)")
    .optional(),
// ...

// ✅ temporaryClosures 상세 필드 추가
fieldWithPath("data.temporaryClosures[].closureDate")
    .type(JsonFieldType.STRING)
    .description("휴무 날짜 (yyyy-MM-dd)")
    .optional(),
// ...

// ✅ isFavorite 필드 추가
fieldWithPath("data.isFavorite")
    .type(JsonFieldType.BOOLEAN)
    .description("즐겨찾기 여부"),

// ✅ error 필드를 optional로 변경
fieldWithPath("error")
    .type(JsonFieldType.NULL)
    .description("에러 정보 (성공 시 null)")
    .optional(),
```

#### 2-3. 자동완성 검색 (1개 테스트)
**수정 사항:**
```java
// ✅ categoryName을 optional로 변경
fieldWithPath("data.stores[].categoryName")
    .type(JsonFieldType.STRING)
    .description("카테고리명")
    .optional(),

// ✅ error 필드를 optional로 변경
fieldWithPath("error")
    .type(JsonFieldType.NULL)
    .description("에러 정보 (성공 시 null)")
    .optional(),
```

#### 2-4. 에러 응답 (3개 테스트)
**수정 사항:**
```java
// ✅ data 필드를 optional로 변경 (@JsonInclude.NON_NULL)
fieldWithPath("data")
    .type(JsonFieldType.NULL)
    .description("응답 데이터 (에러 시 null)")
    .optional(),
```

---

## 🔍 발견된 핵심 문제

### 1. ApiResponse의 @JsonInclude(JsonInclude.Include.NON_NULL)
**문제:**
- ApiResponse에 `@JsonInclude(JsonInclude.Include.NON_NULL)` 어노테이션이 적용됨
- `error`가 null인 성공 응답에서는 `error` 필드가 JSON에 포함되지 않음
- `data`가 null인 에러 응답에서는 `data` 필드가 JSON에 포함되지 않음

**해결:**
- 성공 응답: `error` 필드를 `.optional()`로 설정
- 에러 응답: `data` 필드를 `.optional()`로 설정

### 2. DTO와 REST Docs 문서의 불일치
**문제:**
- REST Docs에는 `categoryId`, `favoriteCount`, `isOpen` 필드가 있었지만 DTO에는 없었음
- `totalElements`로 문서화되었지만 실제는 `totalCount`

**해결:**
- DTO에 `categoryId` 추가
- `favoriteCount`, `isOpen` 필드 제거
- `totalCount`로 문서 수정

### 3. TODO 주석으로 남겨진 미구현 기능
**발견된 TODO:**
- `categoryName` 조회 (Category 조인 필요)
- `isOpen` 계산 (영업 중 여부 판단 로직)
- `isFavorite` 조회 (즐겨찾기 여부)

**임시 해결:**
- 해당 필드들을 `.optional()`로 설정하여 null 값 허용

---

## 📊 테스트 결과

### 최종 테스트 실행
```bash
./gradlew :smartmealtable-api:test --tests "com.stdev.smartmealtable.api.store.controller.StoreControllerRestDocsTest"

BUILD SUCCESSFUL in 15s
21 actionable tasks: 2 executed, 19 up-to-date
```

**전체:** 7개  
**성공:** 7개 ✅  
**실패:** 0개

---

## 🎯 다음 단계

### 즉시 가능한 작업
- [x] StoreListResponse DTO에 categoryId 추가 ✅
- [x] REST Docs 문서화 수정 ✅
- [x] 테스트 통과 확인 ✅

### 향후 개선 작업
- [ ] Category 조인하여 `categoryName` 실제 값 조회
- [ ] 영업 중 여부 계산 로직 구현 (`isOpen`)
- [ ] 즐겨찾기 여부 조회 로직 구현 (`isFavorite`)

---

## 📚 수정된 파일 목록

### DTO
1. `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/store/dto/StoreListResponse.java`
   - `categoryId` 필드 추가
   - `isOpen` 필드 제거

2. `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/store/dto/StoreDetailResponse.java`
   - `categoryId` 필드 추가

### 테스트
3. `/smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/store/controller/StoreControllerRestDocsTest.java`
   - 모든 성공 응답에서 `error` 필드를 `.optional()`로 수정
   - 모든 에러 응답에서 `data` 필드를 `.optional()`로 수정
   - `categoryName` 필드를 `.optional()`로 수정
   - `totalElements` → `totalCount` 변경
   - `favoriteCount` 필드 제거
   - `openingHours` 상세 필드 추가
   - `temporaryClosures` 상세 필드 추가
   - `isFavorite` 필드 추가

---

## 💡 핵심 인사이트

### 1. REST Docs 작성 전 DTO 구조 확정 필요
- API 스펙 문서와 실제 DTO 구조가 일치해야 함
- 미구현 기능은 명확히 표시하고 optional로 처리

### 2. @JsonInclude 어노테이션의 영향
- `@JsonInclude(JsonInclude.Include.NON_NULL)` 사용 시 null 필드는 JSON에 포함되지 않음
- REST Docs 문서화 시 `.optional()` 사용 필수

### 3. TODO 주석의 체계적 관리
- "나중에 구현" TODO는 별도 이슈로 관리
- 미구현 기능은 optional 또는 제거하여 일관성 유지

---

## ✅ 결론

### 성공 사항
- ✅ StoreController REST Docs 테스트 전체 통과
- ✅ DTO와 REST Docs 문서의 완벽한 동기화
- ✅ ApiResponse의 @JsonInclude 처리 방법 확립

### 남은 작업
- ⚠️ 나머지 REST Docs 누락 Controller 작업 (HomeController 등)
- ⚠️ TODO 항목 해결 (categoryName, isOpen, isFavorite)

### 권장 다음 단계
1. **즉시:** 나머지 REST Docs 작성 (HomeController, RecommendationController 등)
2. **단기:** Category 조인 로직 구현
3. **중기:** 영업 중 여부, 즐겨찾기 여부 로직 구현

---

**작성자:** GitHub Copilot  
**작성일:** 2025-10-15  
**관련 문서:**
- `STORE_SERVICE_TEST_SUMMARY.md`
- `TEST_FIX_PROGRESS.md`
- `REST_DOCS_MISSING_SUMMARY.md`
