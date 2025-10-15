# GetStoreListControllerTest REST Docs 수정 완료 보고서

**작성일:** 2025-10-15  
**작업자:** AI Assistant  
**목적:** GetStoreListControllerTest의 REST Docs 필드 문서화 오류 수정

---

## 📋 문제점

### 발생한 오류
```
org.springframework.restdocs.snippet.SnippetException: The following parts of the payload were not documented:
{
  "data" : {
    "stores" : [ {
      "categoryId" : 1
    } ]
  }
}
```

**원인:**
1. 실제 응답에는 `categoryId` 필드가 포함되어 있으나 REST Docs에서 문서화하지 않음
2. `error` 필드에 `JsonFieldType`을 지정하지 않아 `FieldTypeRequiredException` 발생

---

## 🔧 수정 내용

### 1. Import 추가
```java
import org.springframework.restdocs.payload.JsonFieldType;
```

### 2. 응답 필드 문서화 수정

#### 수정 전
```java
fieldWithPath("data.stores[].categoryName").description("카테고리명").optional(),
// categoryId 필드 누락
fieldWithPath("error").description("에러 정보 (성공 시 null)").optional()
```

#### 수정 후
```java
fieldWithPath("data.stores[].categoryId").description("카테고리 ID"),
fieldWithPath("data.stores[].categoryName").description("카테고리명").optional(),
fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)").optional()
```

### 3. 적용 범위
- `getStores_Success_DefaultRadius()` 테스트
- `getStores_Success_SearchByKeyword()` 테스트

---

## ✅ 테스트 결과

### 수정 전
```
가게 목록 조회 API 테스트 > 가게 목록 조회 성공 - 기본 조회 (반경 3km) FAILED
    org.springframework.restdocs.snippet.SnippetException
```

### 수정 후
```
BUILD SUCCESSFUL in 11s
21 actionable tasks: 2 executed, 19 up-to-date
```

---

## 📊 최종 확인

### 테스트 실행
```bash
# 첫 번째 테스트
./gradlew :smartmealtable-api:test --tests "com.stdev.smartmealtable.api.store.controller.GetStoreListControllerTest.getStores_Success_DefaultRadius"
✅ BUILD SUCCESSFUL

# 두 번째 테스트
./gradlew :smartmealtable-api:test --tests "com.stdev.smartmealtable.api.store.controller.GetStoreListControllerTest.getStores_Success_SearchByKeyword"
✅ BUILD SUCCESSFUL
```

### REST Docs 문서 생성
```bash
./deploy-docs.sh --skip-tests
✅ Documentation deployed successfully!
```

---

## 📝 참고 사항

### StoreListResponse 구조
```java
public record StoreItem(
    Long storeId,
    String name,
    Long categoryId,           // ← 실제 응답에 포함
    String categoryName,       // ← null (TODO: Category 조인 필요)
    String address,
    // ... 기타 필드
) {
    public static StoreItem from(StoreWithDistance storeWithDistance) {
        Store store = storeWithDistance.store();
        return new StoreItem(
            store.getStoreId(),
            store.getName(),
            store.getCategoryId(),
            null, // TODO: Category 조인 필요
            // ...
        );
    }
}
```

**중요:** 
- `categoryId`는 항상 반환됨
- `categoryName`은 현재 null (추후 Category 조인 시 값 채워질 예정)

---

## 🎉 결론

1. ✅ `categoryId` 필드 문서화 추가
2. ✅ `error` 필드 타입 명시 (`JsonFieldType.NULL`)
3. ✅ 모든 테스트 통과
4. ✅ REST Docs 문서 생성 완료

---

**작성일:** 2025-10-15  
**상태:** ✅ 완료
