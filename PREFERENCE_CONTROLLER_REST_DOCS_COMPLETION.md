# PreferenceController REST Docs 작업 완료 보고서

## 작업 개요
- **작업일**: 2025-10-12
- **작업 내용**: PreferenceController REST Docs 테스트 생성 및 JWT 인증 적용
- **작업 파일**: `PreferenceControllerRestDocsTest.java`

## 주요 변경사항

### 1. PreferenceControllerRestDocsTest 생성
위치: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/member/controller/PreferenceControllerRestDocsTest.java`

#### 구현된 테스트 메서드 (총 6개)
1. **getPreferences_Success**: 선호도 조회 성공 (200 OK)
   - 카테고리 선호도 + 음식 선호도 조회
   - 응답: recommendationType, categoryPreferences, foodPreferences (liked/disliked)

2. **updateCategoryPreferences_Success**: 카테고리 선호도 수정 성공 (200 OK)
   - 요청: preferences (categoryId, weight)
   - 응답: updatedCount, updatedAt

3. **addFoodPreference_Success**: 음식 선호도 추가 성공 (201 CREATED)
   - 요청: foodId, isPreferred (Boolean)
   - 응답: foodPreferenceId, foodId, foodName, categoryName, isPreferred, createdAt

4. **updateFoodPreference_Success**: 음식 선호도 변경 성공 (200 OK)
   - 요청: isPreferred (Boolean)
   - 응답: foodPreferenceId, foodId, foodName, categoryName, isPreferred, updatedAt

5. **deleteFoodPreference_Success**: 음식 선호도 삭제 성공 (204 NO_CONTENT)
   - Path Parameter: foodPreferenceId

6. **getPreferences_NotFound_MemberNotExists**: 선호도 조회 실패 - 회원 없음 (404 NOT_FOUND)
   - 에러 응답: result, error (code, message, data)

### 2. JWT 인증 적용
- **기존**: `@RequestHeader("X-Member-Id")` 사용
- **변경**: `Authorization` 헤더 + JWT 토큰 사용
- **적용 컨트롤러**:
  - PreferenceController (5개 메서드)
  - MemberController (4개 메서드)

### 3. Request DTO 패턴 확인
모든 Request DTO는 **@Builder 패턴이 아닌 생성자 기반**으로 구현되어 있음을 확인하고 테스트 코드 수정:

- `AddFoodPreferenceRequest`: `new AddFoodPreferenceRequest(foodId, isPreferred)`
  - foodId: Long
  - isPreferred: Boolean (NOT Integer weight)

- `UpdateFoodPreferenceRequest`: `new UpdateFoodPreferenceRequest(isPreferred)`
  - isPreferred: Boolean (NOT Integer weight)

- `UpdateCategoryPreferencesRequest`: `new UpdateCategoryPreferencesRequest(List.of(...))`
  - preferences: List<CategoryPreferenceItem>
  - CategoryPreferenceItem(categoryId, weight)

### 4. Response 구조 발견
ApiResponse의 `@JsonInclude(JsonInclude.Include.NON_NULL)` 설정으로 인해:
- **성공 응답**: `error` 필드 제외 (null이므로)
- **에러 응답**: `data` 필드 제외 (null이므로)

→ REST Docs responseFields에서 null 필드 제거

### 5. 테스트 데이터 구성
`@BeforeEach setUpTestData()`:
- Group (테스트대학교, UNIVERSITY)
- Member (테스트유저, BALANCED)
- MemberAuthentication (email)
- Category 2개 (한식, 중식)
- Food 3개 (비빔밥, 짜장면, 김치찌개)
- Preference 2개 (카테고리 선호도)
- FoodPreference 2개 (좋아요, 싫어요)

## 생성된 REST Docs 스니펫
`smartmealtable-api/build/generated-snippets/`:
- `preference-get-preferences-success/`
- `preference-update-category-preferences-success/`
- `preference-add-food-preference-success/`
- `preference-update-food-preference-success/`
- `preference-delete-food-preference-success/`
- `preference-get-preferences-not-found/`

각 디렉터리 내 파일:
- curl-request.adoc
- http-request.adoc
- http-response.adoc
- httpie-request.adoc
- request-body.adoc (해당 시)
- request-fields.adoc (해당 시)
- request-headers.adoc
- response-body.adoc
- response-fields.adoc
- path-parameters.adoc (해당 시)

## 테스트 결과
```
> Task :smartmealtable-api:test
8 tests completed, 0 failed
BUILD SUCCESSFUL in 32s
```

✅ 모든 테스트 통과

## 발견한 이슈와 해결

### Issue 1: Request DTO Builder 패턴 오해
- **문제**: Request DTO에 @Builder가 없는데 `.builder()` 사용 시도
- **해결**: 생성자 기반으로 변경
  ```java
  // WRONG
  AddFoodPreferenceRequest.builder().foodId(1L).weight(100).build();
  
  // CORRECT
  new AddFoodPreferenceRequest(1L, true);
  ```

### Issue 2: weight vs isPreferred 필드명 불일치
- **문제**: AddFoodPreferenceRequest가 `weight` 대신 `isPreferred` 사용
- **해결**: Request/Response 클래스 확인 후 `isPreferred (Boolean)` 사용

### Issue 3: ApiResponse의 @JsonInclude 정책
- **문제**: 성공 응답에 `error` 필드, 에러 응답에 `data` 필드 문서화 시도
- **해결**: `@JsonInclude(JsonInclude.Include.NON_NULL)` 정책 확인 후 null 필드 제거

### Issue 4: FoodPreference 중복 생성
- **문제**: setup에서 이미 생성한 FoodPreference를 테스트에서 재생성
- **해결**: 
  - setup에서 ID를 필드로 저장 (`likedFoodPreferenceId`, `dislikedFoodPreferenceId`)
  - 테스트에서 저장된 ID 재사용
  - 새 음식 추가 테스트용 `foodId3` 추가

### Issue 5: UpdateCategoryPreferencesRequest 필드명
- **문제**: Request 필드명이 `categoryPreferences`가 아닌 `preferences`
- **해결**: 실제 DTO 확인 후 `preferences` 사용

### Issue 6: UpdateCategoryPreferencesResponse 구조
- **문제**: Response에 `message` 필드가 없음
- **해결**: 실제 Response 확인 후 `updatedCount`, `updatedAt` 만 문서화

## 다음 단계

### 남은 Controller REST Docs 작업
1. ✅ PreferenceController (완료)
2. ⏭️ PasswordExpiryController
   - JWT 인증으로 변경 필요
   - REST Docs 업데이트

### 예상 작업 시간
- PasswordExpiryController REST Docs: 약 20분

## 참고 사항
- 모든 Controller는 `/api/v1` prefix 사용
- JWT 토큰은 `createAccessToken(memberId)` 헬퍼 메서드로 생성
- Authorization 헤더는 `authorizationHeader()` 스니펫 사용
- Request/Response 필드 검증 시 실제 DTO 클래스 확인 필수
- 도메인 엔티티 생성 시 `reconstitute()` 또는 `create()` 패턴 사용
