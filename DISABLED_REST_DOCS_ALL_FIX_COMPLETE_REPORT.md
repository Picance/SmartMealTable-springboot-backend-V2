# @Disabled Rest Docs 테스트 수정 완료 보고서

## 📋 개요

TEST_FIX_PROGRESS.md에 명시된 3개의 @Disabled Rest Docs 테스트 클래스를 모두 수정하여 정상적으로 통과하도록 완료했습니다.

**작업 일시:** 2025-10-15  
**작업 범위:** MapControllerRestDocsTest, AppSettingsControllerRestDocsTest, NotificationSettingsControllerRestDocsTest  
**총 테스트 개수:** 13개 (Map 5개 + AppSettings 4개 + NotificationSettings 4개)  
**최종 결과:** ✅ **전체 13개 테스트 모두 통과**

---

## 🎯 수정한 테스트 클래스

### 1. MapControllerRestDocsTest (5개 테스트) ✅

**파일 경로:**
```
smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/map/controller/MapControllerRestDocsTest.java
```

**문제점:**
- @MockBean MapApplicationService 방식으로 응답 구조가 맞지 않음
- AbstractRestDocsTest의 setUp()이 MockMvc를 재구성하면서 MockBean 설정이 깨짐

**해결 방법:**
- @MockBean 제거, 도메인 레이어(MapService)를 직접 Mock
- ApiResponse 구조 통일 (`result/data/error`)
- AddressSearchResultResponse DTO에 누락된 필드 추가 (sido, sigungu, dong)
- @JsonInclude(NON_NULL) 고려한 responseFields 작성

**수정된 테스트:**
1. `searchAddress_Success_Docs` ✅
2. `searchAddress_Fail_MissingKeyword_Docs` ✅
3. `reverseGeocode_Success_Docs` ✅
4. `reverseGeocode_Fail_InvalidLatitude_Docs` ✅
5. `reverseGeocode_Fail_MissingLongitude_Docs` ✅

---

### 2. AppSettingsControllerRestDocsTest (4개 테스트) ✅

**파일 경로:**
```
smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/settings/controller/AppSettingsControllerRestDocsTest.java
```

**문제점:**
- @MockBean AppSettingsApplicationService 방식으로 응답 구조가 맞지 않음
- Controller의 AuthenticatedUser 타입 불일치 (api.common.auth vs core.auth)

**해결 방법:**
- @MockBean 제거, 실제 Repository 사용 (BudgetControllerRestDocsTest 패턴)
- setUp에서 AppSettings 엔티티 생성 및 저장
- Controller의 AuthenticatedUser import를 core.auth로 변경
- ApiResponse 구조 통일 (`result/data/error`)
- 422 Unprocessable Entity 상태 코드 대응

**수정된 테스트:**
1. `getAppSettings_Success_Docs` ✅
2. `updateTrackingSettings_Success_Docs` ✅
3. `updateTrackingSettings_Fail_MissingField_Docs` ✅
4. `updateTrackingSettings_Fail_NoAuth_Docs` ✅

**추가 수정 파일:**
- `AppSettingsController.java`: AuthenticatedUser import 수정
- `AppSettingsControllerRestDocsTest.java`: AppSettingsRepository 주입 및 setUp에서 초기 데이터 생성

---

### 3. NotificationSettingsControllerRestDocsTest (4개 테스트) ✅

**파일 경로:**
```
smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/settings/controller/NotificationSettingsControllerRestDocsTest.java
```

**문제점:**
- @MockBean NotificationSettingsApplicationService 방식으로 응답 구조가 맞지 않음
- Controller의 AuthenticatedUser 타입 불일치

**해결 방법:**
- @MockBean 제거, 실제 Repository 사용
- setUp에서 NotificationSettings 엔티티 생성 및 저장
- Controller의 AuthenticatedUser import를 core.auth로 변경
- ApiResponse 구조 통일 (`result/data/error`)
- 422 Unprocessable Entity 상태 코드 대응

**수정된 테스트:**
1. `getNotificationSettings_Success_Docs` ✅
2. `updateNotificationSettings_Success_Docs` ✅
3. `updateNotificationSettings_Fail_MissingField_Docs` ✅
4. `getNotificationSettings_Fail_NoAuth_Docs` ✅

**추가 수정 파일:**
- `NotificationSettingsController.java`: AuthenticatedUser import 수정
- `NotificationSettingsControllerRestDocsTest.java`: NotificationSettingsRepository 주입 및 setUp에서 초기 데이터 생성

---

## 🔧 주요 수정 사항

### 1. ApiResponse 구조 통일

**기존 (잘못된 구조):**
```json
{
  "success": true,
  "data": { ... },
  "message": "...",
  "errorCode": "..."
}
```

**수정 후 (올바른 구조):**
```json
{
  "result": "SUCCESS",  // or "ERROR"
  "data": { ... },      // 성공 시에만 존재
  "error": {            // 에러 시에만 존재
    "code": "E422",
    "message": "...",
    "data": { ... }     // optional
  }
}
```

**수정된 파일:**
- `core/response/ApiResponse.java` 삭제 (obsolete 버전)
- `core/api/response/ApiResponse.java` 사용 (canonical 버전)
- `MapController.java`, `AppSettingsController.java`, `NotificationSettingsController.java`: import 경로 수정

---

### 2. AuthenticatedUser 타입 통일

**문제:**
- `core.auth.AuthenticatedUser` (ArgumentResolver가 사용)
- `api.common.auth.AuthenticatedUser` (Controller가 사용) ← 잘못됨

**해결:**
- 모든 Controller에서 `core.auth.AuthenticatedUser`를 사용하도록 import 수정

**수정된 파일:**
- `AppSettingsController.java`
- `NotificationSettingsController.java`

---

### 3. Rest Docs ResponseFields 패턴

**성공 응답:**
```java
responseFields(
    fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS/ERROR)"),
    fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
    fieldWithPath("data.*").type(JsonFieldType.*).description("...")
    // error 필드 없음 - @JsonInclude(NON_NULL)에 의해 제외됨
)
```

**에러 응답:**
```java
responseFields(
    fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (ERROR)"),
    // data 필드 없음 - @JsonInclude(NON_NULL)에 의해 제외됨
    fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
    fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
    fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
    fieldWithPath("error.data").type(JsonFieldType.OBJECT).description("에러 상세 정보").optional()
)
```

---

### 4. HTTP 상태 코드 매핑

| 시나리오 | 상태 코드 | 에러 코드 | 예시 |
|---------|----------|----------|------|
| 성공 | 200 OK | - | 정상 조회/수정 |
| 인증 실패 | 401 Unauthorized | E401 | Authorization 헤더 없음 |
| Validation 실패 | 422 Unprocessable Entity | E422 | 필수 필드 누락 |

---

## ✅ 검증 결과

### 전체 API 모듈 테스트 실행

```bash
./gradlew :smartmealtable-api:test
```

**결과:**
```
BUILD SUCCESSFUL in 9m 56s
21 actionable tasks: 1 executed, 20 up-to-date
```

### 개별 테스트 클래스 검증

#### 1. MapControllerRestDocsTest
```bash
./gradlew :smartmealtable-api:test --tests "MapControllerRestDocsTest"
```
**결과:** ✅ 5/5 tests passing

#### 2. AppSettingsControllerRestDocsTest
```bash
./gradlew :smartmealtable-api:test --tests "AppSettingsControllerRestDocsTest"
```
**결과:** ✅ 4/4 tests passing

#### 3. NotificationSettingsControllerRestDocsTest
```bash
./gradlew :smartmealtable-api:test --tests "NotificationSettingsControllerRestDocsTest"
```
**결과:** ✅ 4/4 tests passing

---

## 📊 테스트 통계

| 항목 | 이전 | 이후 | 변화 |
|-----|------|------|------|
| 전체 테스트 수 | 390 | 403 | +13 |
| 통과 테스트 수 | 390 | 403 | +13 |
| 실패 테스트 수 | 0 | 0 | - |
| @Disabled 테스트 수 | 13 | 0 | -13 |

---

## 🎓 학습한 내용

### 1. AbstractRestDocsTest와 MockBean의 비호환성

**문제:**
```java
@BeforeEach
void setUp(WebApplicationContext webApplicationContext,
           RestDocumentationContextProvider restDocumentation) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
            .build();
}
```

- AbstractRestDocsTest의 setUp()이 MockMvc를 재구성하면서 @MockBean 설정이 초기화됨
- 해결 방법: 도메인 레이어를 직접 Mock하거나, 실제 Repository 사용

### 2. @JsonInclude(NON_NULL)과 Rest Docs Field Validation

- ApiResponse에 `@JsonInclude(NON_NULL)` 적용됨
- 성공 시 error 필드가 null이므로 JSON에서 제외됨
- 에러 시 data 필드가 null이므로 JSON에서 제외됨
- Rest Docs responseFields는 실제 JSON에 존재하는 필드만 문서화해야 함

### 3. Repository 기반 통합 테스트 패턴

**BudgetControllerRestDocsTest 패턴:**
```java
@Autowired
private MemberRepository memberRepository;

@Autowired
private AppSettingsRepository appSettingsRepository;

@BeforeEach
void setUp() {
    // 테스트 데이터 생성
    Member member = Member.create(...);
    memberRepository.save(member);
    
    AppSettings settings = AppSettings.create(member.getMemberId());
    appSettingsRepository.save(settings);
}
```

- 실제 DB에 데이터를 생성하여 테스트
- @Transactional로 테스트 간 격리 보장
- Mock보다 실제 동작을 정확하게 검증

---

## 🔄 다음 단계

1. **Phase 2 진행:** Service 레이어 및 Domain 모듈 테스트 검증
   - NotificationSettingsApplicationServiceTest
   - AppSettingsApplicationServiceTest
   - HomeDashboardQueryServiceTest
   - AppSettingsTest
   - NotificationSettingsTest
   
2. **Rest Docs 문서 생성 확인:**
   ```bash
   ./gradlew :smartmealtable-api:asciidoctor
   ```
   생성된 문서 확인:
   - `build/docs/asciidoc/index.html`

---

## 📝 참고 사항

### 수정된 파일 목록

**테스트 파일:**
1. `MapControllerRestDocsTest.java` - 완전 재작성
2. `AppSettingsControllerRestDocsTest.java` - @MockBean 제거, Repository 패턴 적용
3. `NotificationSettingsControllerRestDocsTest.java` - @MockBean 제거, Repository 패턴 적용

**프로덕션 코드:**
1. `MapController.java` - ApiResponse import 수정
2. `AppSettingsController.java` - ApiResponse 및 AuthenticatedUser import 수정
3. `NotificationSettingsController.java` - AuthenticatedUser import 수정
4. `AddressSearchResultResponse.java` - sido, sigungu, dong 필드 추가
5. `core/response/ApiResponse.java` - 삭제 (obsolete)

**테스트 완료 시각:** 2025-10-15 04:05:11 KST

---

## ✅ 최종 확인

- [x] MapControllerRestDocsTest 5개 테스트 모두 통과
- [x] AppSettingsControllerRestDocsTest 4개 테스트 모두 통과
- [x] NotificationSettingsControllerRestDocsTest 4개 테스트 모두 통과
- [x] 전체 API 모듈 테스트 통과 (403 tests)
- [x] @Disabled 어노테이션 모두 제거
- [x] TEST_FIX_PROGRESS.md 업데이트 완료

**작업 상태:** ✅ **완료 (COMPLETED)**
