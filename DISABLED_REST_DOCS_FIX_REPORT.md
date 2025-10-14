# @Disabled Rest Docs 테스트 수정 완료 보고서

**작성일:** 2025-10-15
**작성자:** GitHub Copilot
**목적:** @Disabled 처리된 Rest Docs 테스트 수정 및 검증

---

## 📋 요약

### ✅ 완료된 작업
- **MapControllerRestDocsTest** 수정 완료 (5개 테스트 모두 통과)
- **API Response 구조 통일** (구버전 → 최신 버전)
- **AddressSearchResultResponse DTO 필드 추가** (sido, sigungu, dong)

### 📊 테스트 결과
- **전체 API 모듈 테스트:** ✅ 성공
- **MapControllerRestDocsTest:** ✅ 5개 테스트 모두 통과

---

## 🔍 작업 상세 내역

### 1. MapControllerRestDocsTest 수정

#### 문제점
```
@Disabled("MockBean 방식 개선 필요 - NaverMapClient를 직접 Mock해야 함")
```
- @MockBean으로 MapApplicationService를 Mock하면 응답 구조가 맞지 않음
- AbstractRestDocsTest의 setUp()에서 MockMvc 재빌드 시 MockBean이 제대로 동작하지 않음

#### 해결 방법
**Creative Approach 1 선택**: MapService를 @MockBean으로 Mock (도메인 레이어 Mock)
- 장점: 외부 API 호출 없이 테스트 가능, Application Service 로직도 포함
- 구현: MapApplicationService 대신 MapService(인터페이스)를 Mock

#### 수정 내용

**1) Import 변경**
```java
// Before
import com.stdev.smartmealtable.api.map.service.MapApplicationService;

// After
import com.stdev.smartmealtable.domain.map.MapService;
```

**2) Mock 대상 변경**
```java
// Before
@MockBean
private MapApplicationService mapApplicationService;

// After
@MockBean
private MapService mapService;
```

**3) Mock 데이터 변경**
```java
// Before - DTO Response 객체 사용
AddressSearchServiceResponse response = AddressSearchServiceResponse.of(
    List.of(result1, result2)
);
given(mapApplicationService.searchAddress(eq(keyword), eq(limit)))
    .willReturn(response);

// After - Domain 객체 직접 반환
AddressSearchResult result1 = new AddressSearchResult(...);
AddressSearchResult result2 = new AddressSearchResult(...);

given(mapService.searchAddress(eq(keyword), eq(limit)))
    .willReturn(List.of(result1, result2));
```

### 2. API Response 구조 통일

#### 문제점 발견
**두 가지 ApiResponse 클래스 존재:**
1. `com.stdev.smartmealtable.core.response.ApiResponse` (구버전)
   - 필드: `success`, `data`, `message`, `errorCode`
   
2. `com.stdev.smartmealtable.core.api.response.ApiResponse` (최신 버전)
   - 필드: `result`, `data`, `error`

#### 해결 방법
**올바른 ApiResponse 사용:**
```java
// 모든 Controller에서 올바른 import 사용
import com.stdev.smartmealtable.core.api.response.ApiResponse;
```

**수정된 Controller:**
- `MapController.java`
- `AppSettingsController.java`
- `NotificationSettingsController.java`

### 3. AddressSearchResultResponse DTO 수정

#### 문제점
```java
// Before - sido, sigungu, dong 필드 없음
public record AddressSearchResultResponse(
    String roadAddress,
    String jibunAddress,
    BigDecimal latitude,
    BigDecimal longitude,
    String buildingName,
    String sigunguCode,
    String bcode
) { }
```

#### 해결 방법
```java
// After - 필드 추가
public record AddressSearchResultResponse(
    String roadAddress,
    String jibunAddress,
    BigDecimal latitude,
    BigDecimal longitude,
    String sido,          // 추가
    String sigungu,       // 추가
    String dong,          // 추가
    String buildingName,
    String sigunguCode,
    String bcode
) { }
```

### 4. Rest Docs responseFields 수정

#### 문제점
```java
// Before - error 필드가 null로 포함됨 (실제로는 @JsonInclude로 인해 제외됨)
responseFields(
    fieldWithPath("result")...,
    fieldWithPath("data")...,
    fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보 (성공 시 null)")
)
```

#### 해결 방법
**성공 응답:**
```java
// After - error 필드 제거
responseFields(
    fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS/ERROR)"),
    fieldWithPath("data")...
)
```

**에러 응답:**
```java
// Before
responseFields(
    fieldWithPath("result")...,
    fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (에러 시 null)"),
    fieldWithPath("error")...,
    fieldWithPath("error.code")...,
    fieldWithPath("error.message")...
)

// After - data 필드 제거, error.data 추가
responseFields(
    fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (ERROR)"),
    fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
    fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
    fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
    fieldWithPath("error.data").type(JsonFieldType.OBJECT).description("에러 상세 정보").optional()
)
```

---

## 📝 수정된 파일 목록

### Test 파일
1. `/smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/map/controller/MapControllerRestDocsTest.java`
   - @Disabled 제거
   - MapService Mock 방식으로 전환
   - responseFields 수정

### DTO 파일
2. `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/map/dto/AddressSearchResultResponse.java`
   - sido, sigungu, dong 필드 추가

### Controller 파일
3. `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/map/controller/MapController.java`
   - ApiResponse import 경로 수정

4. `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/settings/controller/AppSettingsController.java`
   - ApiResponse import 경로 수정

5. `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/settings/controller/NotificationSettingsController.java`
   - ApiResponse import 경로 수정

---

## 🎯 남은 작업

### 우선순위 1: Rest Docs 테스트 수정 (별도 세션 필요)
아래 테스트들은 @Disabled 처리되어 있으며, MapController와 유사한 방식으로 수정 필요:

1. **AppSettingsControllerRestDocsTest** (4개 테스트)
   - 현재 상태: `@Disabled("MockBean 방식 개선 필요 - 실제 Repository를 사용한 통합 테스트로 전환해야 함")`
   - 필요 작업: BudgetControllerRestDocsTest 패턴으로 전환 (실제 Repository 사용)

2. **NotificationSettingsControllerRestDocsTest** (4개 테스트)
   - 현재 상태: `@Disabled("MockBean 방식 개선 필요 - 실제 Repository를 사용한 통합 테스트로 전환해야 함")`
   - 필요 작업: BudgetControllerRestDocsTest 패턴으로 전환 (실제 Repository 사용)

### 우선순위 2: Phase 2 진행
- Service 레이어 테스트 확인
- Domain 모듈 테스트 확인

---

## ✅ 검증 결과

### 테스트 실행 명령어
```bash
# MapControllerRestDocsTest 개별 실행
./gradlew :smartmealtable-api:test --tests "com.stdev.smartmealtable.api.map.controller.MapControllerRestDocsTest"

# 전체 API 모듈 테스트
./gradlew :smartmealtable-api:test
```

### 테스트 결과
```
✅ MapControllerRestDocsTest: 5 tests completed (100% 통과)
✅ 전체 API 모듈: BUILD SUCCESSFUL
```

**테스트 목록:**
1. ✅ [Docs] 주소 검색 성공
2. ✅ [Docs] 주소 검색 실패 - 키워드 누락
3. ✅ [Docs] 역지오코딩 성공
4. ✅ [Docs] 역지오코딩 실패 - 유효하지 않은 위도
5. ✅ [Docs] 역지오코딩 실패 - 경도 누락

---

## 💡 배운 점 및 개선 사항

### 1. MockBean vs 실제 Bean
- **MapController 케이스**: MapService를 Mock → 성공
  - 이유: MapService가 인터페이스이고, 실제 네이버 API 호출 없이 테스트 가능
  
- **Settings Controller 케이스**: 실제 Repository 필요
  - 이유: ApplicationService 로직이 복잡하고, DTO 변환 과정 포함

### 2. API Response 구조 통일의 중요성
- 프로젝트 초기에 API Response 구조를 명확히 정의하고 일관되게 사용해야 함
- 구버전과 신버전이 혼재하면 혼란 발생

### 3. Rest Docs responseFields 작성 시 주의사항
- @JsonInclude(JsonInclude.Include.NON_NULL) 적용 시, null 필드는 응답에 포함되지 않음
- responseFields에 정의한 필드는 반드시 응답에 존재해야 함 (또는 .optional() 사용)

---

## 📚 참고 자료
- `API_SPECIFICATION.md` - API 스펙 및 에러 코드 정의
- `TEST_FIX_PROGRESS.md` - 전체 테스트 수정 진행 상황
- `BudgetControllerRestDocsTest.java` - 성공 패턴 참조

---

**다음 단계:**
1. AppSettingsControllerRestDocsTest 수정 (별도 세션)
2. NotificationSettingsControllerRestDocsTest 수정 (별도 세션)
3. Phase 2 진행 (Service 레이어 테스트)
