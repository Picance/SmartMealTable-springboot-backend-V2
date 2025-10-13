# REST Docs 검증 및 수정 완료 보고서

## 📋 작업 개요

기존에 구현된 REST Docs 테스트의 검증 및 오류 수정 작업을 완료했습니다.

**작업 일시**: 2025-10-13  
**작업 범위**: 
- REST Docs 테스트 검증 (116개 테스트)
- ResourceNotFoundException 추가
- API 명세서 업데이트
- 테스트 오류 수정

## ✅ 완료 사항

### 1. 기존 REST Docs 테스트 현황 파악

**발견된 REST Docs 테스트 파일**: 44개 이상

#### 인증 API (7개)
- SignupControllerRestDocsTest
- LoginControllerRestDocsTest  
- KakaoLoginControllerRestDocsTest
- GoogleLoginControllerRestDocsTest
- LogoutControllerRestDocsTest
- RefreshTokenControllerRestDocsTest
- CheckEmailControllerRestDocsTest

#### 온보딩 API (4개)
- OnboardingProfileControllerRestDocsTest
- OnboardingAddressControllerRestDocsTest
- SetBudgetControllerRestDocsTest
- FoodPreferenceControllerRestDocsTest

#### 예산 API (1개)
- BudgetControllerRestDocsTest

#### 즐겨찾기 API (1개)
- FavoriteControllerRestDocsTest

#### 프로필/회원 API (5개)
- MemberControllerRestDocsTest
- AddressControllerRestDocsTest
- PreferenceControllerRestDocsTest
- PasswordExpiryControllerRestDocsTest
- SocialAccountControllerRestDocsTest

#### 지출 내역 API (1개)
- ExpenditureControllerRestDocsTest (1415 lines, 매우 포괄적)

#### 기타 API (2개)
- CategoryControllerRestDocsTest
- PolicyControllerRestDocsTest

### 2. ResourceNotFoundException 구현

**파일**: `smartmealtable-core/src/main/java/com/stdev/smartmealtable/core/exception/ResourceNotFoundException.java`

```java
public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(ErrorType errorType) {
        super(errorType);
    }
    
    public ResourceNotFoundException(ErrorType errorType, Map<String, String> errorData) {
        super(errorType, errorData);
    }
    
    public ResourceNotFoundException(ErrorType errorType, String message) {
        super(errorType, message);
    }
    
    public ResourceNotFoundException(ErrorType errorType, String message, Throwable cause) {
        super(errorType, message, cause);
    }
}
```

**특징**:
- BaseException을 상속하여 GlobalExceptionHandler에서 자동 처리
- ErrorType enum 기반 에러 코드 관리
- 다양한 생성자로 유연한 에러 정보 제공

### 3. API_SPECIFICATION.md 업데이트

**추가된 섹션**: 2.4 HTTP 상태 코드 - 404 Not Found 에러 처리

**내용**:
- ResourceNotFoundException 사용 예시 (Service Layer 코드)
- JSON 에러 응답 포맷
- 14개 주요 404 에러 타입 목록:
  - MEMBER_NOT_FOUND
  - EMAIL_NOT_FOUND
  - GROUP_NOT_FOUND
  - STORE_NOT_FOUND
  - CATEGORY_NOT_FOUND
  - FAVORITE_NOT_FOUND
  - ADDRESS_NOT_FOUND
  - EXPENDITURE_NOT_FOUND
  - EXPENDITURE_ITEM_NOT_FOUND
  - DAILY_BUDGET_NOT_FOUND
  - MONTHLY_BUDGET_NOT_FOUND
  - MEAL_BUDGET_NOT_FOUND
  - FOOD_PREFERENCE_NOT_FOUND
  - POLICY_NOT_FOUND

### 4. 테스트 오류 수정

**초기 테스트 결과**: 116개 테스트 중 9개 실패  
**최종 테스트 결과**: 116개 테스트 모두 성공 ✅

#### 수정한 테스트 오류

##### 4.1. BudgetControllerRestDocsTest
**문제**: Query Parameter 검증 실패 시 422 기대, 실제는 400 반환  
**원인**: GlobalExceptionHandler에서 ConstraintViolationException을 400으로 처리  
**해결**: 테스트를 400 기대하도록 수정

```java
// 수정 전
.andExpect(status().isUnprocessableEntity())
.andExpect(jsonPath("$.error.code").value("E422"))

// 수정 후
.andExpect(status().isBadRequest())
.andExpect(jsonPath("$.error.code").value("E400"))
```

##### 4.2. FavoriteControllerRestDocsTest
**문제**: POST /favorites 요청에 200 기대, 실제는 201 반환  
**원인**: FavoriteController에 @ResponseStatus(HttpStatus.CREATED) 설정  
**해결**: 테스트를 201 기대하도록 수정

```java
// 수정 전
.andExpect(status().isOk())

// 수정 후
.andExpect(status().isCreated())
```

##### 4.3. MemberControllerRestDocsTest (7개 실패)
**문제**: X-Member-Id 헤더 사용, 실제 컨트롤러는 @AuthUser 사용  
**원인**: 테스트가 구 버전 인증 방식 사용  
**해결**: 
1. JWT Access Token 생성 추가
2. 모든 X-Member-Id 헤더를 Authorization 헤더로 변경
3. 헤더 문서화 업데이트

```java
// setUp() 수정
private String accessToken;

@BeforeEach
void setUp() {
    // ... 회원 생성 ...
    testMemberId = auth.getMemberId();
    
    // JWT Access Token 생성
    accessToken = createAccessToken(testMemberId);
}

// 테스트 수정 (전체 9곳)
// 수정 전
.header("X-Member-Id", testMemberId)
requestHeaders(
    headerWithName("X-Member-Id")
        .description("회원 ID (JWT 토큰에서 추출, 향후 ArgumentResolver로 대체 예정)")
)

// 수정 후
.header("Authorization", accessToken)
requestHeaders(
    headerWithName("Authorization")
        .description("JWT 인증 토큰 (Bearer {token})")
)
```

**sed를 사용한 일괄 수정**:
```bash
sed -i.bak 's/.header("X-Member-Id", testMemberId)/.header("Authorization", accessToken)/g'
sed -i.bak2 's/headerWithName("X-Member-Id")/headerWithName("Authorization")/g'
sed -i.bak3 's/.description("회원 ID (JWT 토큰에서 추출, 향후 ArgumentResolver로 대체 예정)")/.description("JWT 인증 토큰 (Bearer {token})")/g'
```

## 🔍 검증 결과

### HTTP 상태 코드 처리 규칙 확립

| 검증 유형 | HTTP Status | ErrorCode | Exception |
|----------|-------------|-----------|-----------|
| Request Body 검증 실패 | 422 | E422 | MethodArgumentNotValidException |
| Query Parameter 검증 실패 | 400 | E400 | ConstraintViolationException |
| 리소스 없음 | 404 | E404 | ResourceNotFoundException |
| 생성 성공 | 201 | - | @ResponseStatus(CREATED) |

### GlobalExceptionHandler 동작 확인

```java
// Request Body 검증 (@Valid on @RequestBody)
@ExceptionHandler(MethodArgumentNotValidException.class)
→ 422 UNPROCESSABLE_ENTITY, E422

// Query Parameter 검증 (@Validated on class)
@ExceptionHandler(ConstraintViolationException.class)
→ 400 BAD_REQUEST, E400

// 리소스 없음
@ExceptionHandler(BaseException.class)
→ ResourceNotFoundException 포함, 404 NOT_FOUND, E404
```

## 📊 테스트 통계

- **총 REST Docs 테스트**: 116개
- **테스트 파일**: 44개 이상
- **성공률**: 100% (116/116)
- **실행 시간**: ~3분 35초

## 📝 주요 개선사항

1. **404 에러 처리 표준화**
   - ResourceNotFoundException 도입
   - 14개 주요 404 에러 타입 정의
   - 명확한 사용 가이드 문서화

2. **테스트 정확성 향상**
   - 실제 API 동작과 테스트 기대값 일치
   - 최신 인증 방식 (JWT) 적용
   - HTTP 상태 코드 정확성 보장

3. **문서화 품질 개선**
   - API_SPECIFICATION.md에 404 에러 섹션 추가
   - 코드 예시와 JSON 응답 포맷 포함
   - 개발자 가이드 제공

## 🚀 다음 단계 (선택사항)

### Cart API REST Docs 구현 (미완료)
**상태**: 시도했으나 도메인 엔티티 복잡도로 인해 보류  
**이슈**:
- Cart, CartItem 엔티티의 static factory method 시그니처 파악 필요
- CartItemRepository import 경로 확인 필요

**권장사항**:
- 기존 ExpenditureControllerRestDocsTest (1415 lines) 참고
- Cart 도메인 모델 분석 후 진행
- 우선순위: 낮음 (대부분의 API 이미 커버됨)

## 📌 참고사항

### 테스트 실행 명령어
```bash
# 모든 REST Docs 테스트 실행
./gradlew :smartmealtable-api:test --tests "*RestDocsTest"

# 특정 컨트롤러 테스트 실행
./gradlew :smartmealtable-api:test --tests "*MemberControllerRestDocsTest"

# 실패해도 계속 진행
./gradlew :smartmealtable-api:test --tests "*RestDocsTest" --continue
```

### 문서 생성 명령어
```bash
# Asciidoc 문서 생성
./gradlew asciidoc

# 생성된 문서 위치
# smartmealtable-api/build/docs/asciidoc/
```

## ✨ 결론

모든 REST Docs 테스트가 성공적으로 검증되었으며, 발견된 오류는 모두 수정 완료되었습니다. 
ResourceNotFoundException 추가와 API 명세서 업데이트로 404 에러 처리가 표준화되었습니다.

**최종 상태**: ✅ 모든 작업 완료, 116개 테스트 통과
