# API Response 구조 수정 계획서

## 개요

**목적**: `ApiResponse` 클래스의 `@JsonInclude(NON_NULL)` 설정을 제거하여 API 명세와 일치시키기

**작성일**: 2025-10-15

## 문제점

### 현재 상태
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final String result;
    private final T data;
    private final ErrorResponse error;
}
```

**문제**: `NON_NULL` 설정으로 인해 null 값을 가진 필드가 JSON 응답에서 생략됨

### API 명세 요구사항
```json
// 성공 응답
{
  "result": "SUCCESS",
  "data": { ... },
  "error": null    // ← null이 명시적으로 표시되어야 함
}

// 에러 응답
{
  "result": "ERROR",
  "data": null,    // ← null이 명시적으로 표시되어야 함
  "error": { ... }
}
```

## 수정 범위

### 1. Core 모듈
- [ ] `ApiResponse.java` - `@JsonInclude` 어노테이션 제거

### 2. 테스트 코드 수정 (Controller 단위)

#### Auth 관련 Controller (7개)
- [ ] `SignupControllerTest.java`
- [ ] `LoginControllerTest.java`
- [ ] `KakaoLoginControllerTest.java`
- [ ] `CheckEmailControllerTest.java`
- [ ] `SignupControllerRestDocsTest.java`
- [ ] `LoginControllerRestDocsTest.java`
- [ ] `KakaoLoginControllerRestDocsTest.java`

#### Member 관련 Controller (4개)
- [ ] `MemberControllerTest.java`
- [ ] `ChangePasswordControllerTest.java`
- [ ] `UpdateCategoryPreferencesControllerTest.java`
- [ ] `MemberControllerRestDocsTest.java`

#### Expenditure 관련 Controller (2개)
- [ ] `ParseSmsControllerTest.java`
- [ ] `ExpenditureControllerRestDocsTest.java`

#### Cart 관련 Controller (2개)
- [ ] `CartControllerTest.java`
- [ ] `CartControllerRestDocsTest.java`

#### Policy 관련 Controller (2개)
- [ ] `PolicyControllerTest.java`
- [ ] `PolicyControllerRestDocsTest.java`

#### Store 관련 Controller (2개)
- [ ] `StoreControllerTest.java`
- [ ] `StoreControllerRestDocsTest.java`

#### 기타 Controller
- [ ] 추가 확인 필요

## 수정 패턴

### Before (현재)
```java
// 성공 시 - error 필드가 존재하지 않는다고 검증
.andExpect(jsonPath("$.error").doesNotExist());

// 실패 시 - data 필드가 존재하지 않는다고 검증
.andExpect(jsonPath("$.data").doesNotExist());
```

### After (수정 후)
```java
// 성공 시 - error 필드가 null임을 검증
.andExpect(jsonPath("$.error").value(nullValue()));

// 실패 시 - data 필드가 null임을 검증
.andExpect(jsonPath("$.data").value(nullValue()));
```

## 진행 순서

### Phase 1: Core 수정
1. `ApiResponse.java` 수정
2. 빌드 확인

### Phase 2: Controller 단위 수정 (TDD 방식)
각 Controller별로 아래 순서로 진행:
1. Test 파일 수정
2. RestDocsTest 파일 수정 (존재하는 경우)
3. 테스트 실행 및 검증
4. 문서 업데이트 (체크리스트 완료 표시)

### Phase 3: 최종 검증
1. 전체 테스트 실행
2. API 문서 생성 확인
3. 실제 API 응답 검증

## 진행 상황

### ✅ Phase 1: Core 수정
- [x] `ApiResponse.java` 수정 완료 (**주의**: `/core/api/response/ApiResponse.java`가 실제 사용됨)
- [x] 빌드 성공 확인
- [x] JacksonConfig 추가 (백업용)

### 📋 Phase 2: Controller 단위 수정

#### Auth Controllers
- [x] Signup Controller 완료
  - [x] `SignupControllerTest.java` 수정
  - [x] 테스트 통과 확인 (6/6 tests passed)
  
- [ ] Login Controller 완료
  - [ ] `LoginControllerTest.java` 수정
  - [ ] `LoginControllerRestDocsTest.java` 수정 (존재 시)
  - [ ] 테스트 통과 확인
  
- [ ] Kakao Login Controller 완료
  - [ ] `KakaoLoginControllerTest.java` 수정
  - [ ] `KakaoLoginControllerRestDocsTest.java` 수정 (존재 시)
  - [ ] 테스트 통과 확인
  
- [ ] Check Email Controller 완료
  - [ ] `CheckEmailControllerTest.java` 수정
  - [ ] 테스트 통과 확인

#### Member Controllers
- [ ] Member Controller 완료
  - [ ] `MemberControllerTest.java` 수정
  - [ ] `MemberControllerRestDocsTest.java` 수정 (존재 시)
  - [ ] 테스트 통과 확인
  
- [ ] Change Password Controller 완료
  - [ ] `ChangePasswordControllerTest.java` 수정
  - [ ] 테스트 통과 확인
  
- [ ] Update Category Preferences Controller 완료
  - [ ] `UpdateCategoryPreferencesControllerTest.java` 수정
  - [ ] 테스트 통과 확인

#### Expenditure Controllers
- [ ] Parse SMS Controller 완료
  - [ ] `ParseSmsControllerTest.java` 수정
  - [ ] 테스트 통과 확인
  
- [ ] Expenditure Controller 완료
  - [ ] `ExpenditureControllerRestDocsTest.java` 수정
  - [ ] 테스트 통과 확인

#### Cart Controllers
- [ ] Cart Controller 완료
  - [ ] `CartControllerTest.java` 수정
  - [ ] `CartControllerRestDocsTest.java` 수정 (존재 시)
  - [ ] 테스트 통과 확인

#### Policy Controllers
- [ ] Policy Controller 완료
  - [ ] `PolicyControllerTest.java` 수정
  - [ ] `PolicyControllerRestDocsTest.java` 수정 (존재 시)
  - [ ] 테스트 통과 확인

#### Store Controllers
- [ ] Store Controller 완료
  - [ ] `StoreControllerTest.java` 수정 (존재 시)
  - [ ] `StoreControllerRestDocsTest.java` 수정
  - [ ] 테스트 통과 확인

### ⏳ Phase 3: 최종 검증
- [ ] 전체 테스트 실행 성공
- [ ] API 문서 생성 확인
- [ ] 실제 API 응답 수동 검증

## 예상 작업 시간
- Phase 1: 5분
- Phase 2: 각 Controller당 10-15분 (총 약 2-3시간)
- Phase 3: 30분

## 주의사항
1. 각 Controller 수정 후 반드시 테스트 실행하여 검증
2. RestDocs 테스트는 API 문서 생성에 영향을 주므로 신중히 수정
3. 수정 완료 시 문서 체크리스트 즉시 업데이트
4. 실패 시 롤백 가능하도록 git commit 단위 관리

## 롤백 계획
문제 발생 시:
1. `git revert` 또는 `git reset` 사용
2. `@JsonInclude(JsonInclude.Include.NON_NULL)` 복원
3. 테스트 코드 복원

## 완료 기준
- [ ] 모든 테스트 통과
- [ ] API 문서 정상 생성
- [ ] API 응답이 명세와 일치 (null 값 포함)
- [ ] 기존 기능에 영향 없음

---

**최종 업데이트**: 2025-10-15
