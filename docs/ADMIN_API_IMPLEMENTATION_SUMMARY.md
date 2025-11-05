# ADMIN API 구현 현황 상세 요약

**작성일**: 2025-11-05  
**작성자**: AI Assistant  
**프로젝트**: SmartMealTable Backend V2

---

## 📋 목차
1. [전체 개요](#전체-개요)
2. [카테고리 관리 API](#카테고리-관리-api)
3. [약관 관리 API](#약관-관리-api)
4. [공통 구현 사항](#공통-구현-사항)
5. [테스트 현황](#테스트-현황)
6. [알려진 이슈](#알려진-이슈)
7. [다음 단계](#다음-단계)

---

## 전체 개요

### 구현 목표
관리자(Admin)용 백오피스 API를 제공하여 서비스 운영에 필요한 마스터 데이터 관리 기능 구현

### 아키텍처 원칙
- **Layered Architecture**: Controller → Application Service → Domain Service → Repository
- **POJO 원칙**: Domain 모듈에 Spring Data 의존성 노출 금지
- **Transaction 관리**: Application Service 레이어에서 @Transactional 처리
- **DTO 분리**: Service DTO와 Controller DTO 명확히 분리
- **테스트 전략**: Testcontainers + MySQL 8.0 통합 테스트

### 기술 스택
- **Java**: 21
- **Spring Boot**: 6.2.11
- **Spring Data JPA**: QueryDSL 활용
- **Database**: MySQL 8.0
- **Test**: JUnit 5, Mockito, Testcontainers
- **Build**: Gradle Multi-module

---

## 카테고리 관리 API

### 개요
음식점 및 음식에 대한 카테고리 분류 체계를 관리하는 API

### 구현 계층

#### 1. Domain Layer
- **패키지**: `com.stdev.smartmealtable.domain.category`
- **엔티티**: `Category.java`
  - POJO 엔티티 (Spring Data 의존성 없음)
  - 팩토리 메서드: `create()`, `reconstitute()`
- **Repository 인터페이스**: `CategoryRepository.java`
  - Pure Java 인터페이스
  - 메서드: `save()`, `findById()`, `searchByName()`, `existsByName()`, `existsByNameAndIdNot()`, `isUsedInStoreOrFood()`, `deleteById()`
- **Page Result**: `CategoryPageResult.java`
  - POJO record 타입
  - Spring Data Page 대체

#### 2. Storage Layer
- **패키지**: `com.stdev.smartmealtable.storage.db.category`
- **JPA Entity**: `CategoryJpaEntity.java`
  - Lombok 사용
  - `toDomain()`, `fromDomain()` 변환 메서드
- **Repository 구현**: `CategoryRepositoryImpl.java`
  - QueryDSL 기반 동적 쿼리
  - `searchByName()`: 이름 검색 + 페이징
  - `existsByName()`: 중복 체크
  - `existsByNameAndIdNot()`: 수정 시 중복 체크 (자신 제외)
  - `isUsedInStoreOrFood()`: Store/Food 테이블 JOIN 체크
  - `deleteById()`: 물리적 삭제

#### 3. Application Service Layer
- **패키지**: `com.stdev.smartmealtable.admin.category.service`
- **Service**: `CategoryApplicationService.java`
  - `@Transactional(readOnly = true)` 기본 설정
  - 메서드:
    - `getCategories()`: 목록 조회 (페이징, 검색)
    - `getCategory()`: 상세 조회
    - `createCategory()`: 생성 (중복 체크)
    - `updateCategory()`: 수정 (중복 체크)
    - `deleteCategory()`: 삭제 (사용 여부 체크)

#### 4. Controller Layer
- **패키지**: `com.stdev.smartmealtable.admin.category.controller`
- **Controller**: `CategoryController.java`
  - Base URL: `/api/v1/admin/categories`
  - 5개 엔드포인트 구현

### API 엔드포인트

#### 1. 카테고리 목록 조회
```http
GET /api/v1/admin/categories?name={검색어}&page={페이지}&size={크기}
```
- **Query Parameters**:
  - `name` (optional): 카테고리명 검색 (부분 일치)
  - `page` (optional, default=0): 페이지 번호
  - `size` (optional, default=20): 페이지 크기
- **Response**: `CategoryListResponse`
  - `categories[]`: 카테고리 요약 목록
  - `pageInfo`: 페이징 정보

#### 2. 카테고리 상세 조회
```http
GET /api/v1/admin/categories/{categoryId}
```
- **Response**: `CategoryResponse`
- **Error**: 404 NOT_FOUND (카테고리 미존재)

#### 3. 카테고리 생성
```http
POST /api/v1/admin/categories
Content-Type: application/json

{
  "name": "한식"
}
```
- **Validation**: `@NotBlank`, `@Size(max=50)`
- **Response**: 201 CREATED + `CategoryResponse`
- **Error**: 409 CONFLICT (중복된 이름)

#### 4. 카테고리 수정
```http
PUT /api/v1/admin/categories/{categoryId}
Content-Type: application/json

{
  "name": "한식 (수정)"
}
```
- **Response**: `CategoryResponse`
- **Error**: 
  - 404 NOT_FOUND (카테고리 미존재)
  - 409 CONFLICT (중복된 이름)

#### 5. 카테고리 삭제
```http
DELETE /api/v1/admin/categories/{categoryId}
```
- **Response**: 204 NO_CONTENT
- **Error**: 
  - 404 NOT_FOUND (카테고리 미존재)
  - 409 CONFLICT (사용 중인 카테고리)

### 테스트 현황
- **테스트 클래스**: `CategoryControllerTest.java`
- **테스트 케이스**: 12개
- **결과**: ✅ **100% PASS**

**테스트 케이스 목록**:
1. ✅ 목록 조회 - 성공
2. ✅ 목록 조회 - 이름 검색
3. ✅ 상세 조회 - 성공
4. ✅ 상세 조회 - 존재하지 않는 카테고리 (404)
5. ✅ 생성 - 성공
6. ✅ 생성 - 중복된 이름 (409)
7. ✅ 생성 - 필수 필드 누락 (400)
8. ✅ 수정 - 성공
9. ✅ 수정 - 존재하지 않는 카테고리 (404)
10. ✅ 수정 - 중복된 이름 (409)
11. ✅ 삭제 - 성공
12. ✅ 삭제 - 사용 중인 카테고리 (409)

---

## 약관 관리 API

### 개요
서비스 이용약관, 개인정보처리방침 등 약관을 관리하는 API

### 구현 계층

#### 1. Domain Layer
- **패키지**: `com.stdev.smartmealtable.domain.policy`
- **엔티티**: `Policy.java`
  - 기존 엔티티 활용
  - 팩토리 메서드: `create()`, `reconstitute()`, `deactivate()`
- **Enum**: `PolicyType.java`
  - `REQUIRED` (필수), `OPTIONAL` (선택)
- **Repository 인터페이스**: `PolicyRepository.java` (확장)
  - 추가 메서드: `searchByTitle()`, `existsByTitle()`, `existsByTitleAndIdNot()`, `hasAgreements()`
- **Page Result**: `PolicyPageResult.java`
  - POJO record 타입

#### 2. Storage Layer
- **패키지**: `com.stdev.smartmealtable.storage.db.policy`
- **JPA Entity**: `PolicyJpaEntity.java` (기존)
- **Repository 구현**: `PolicyRepositoryImpl.java` (확장)
  - QueryDSL 기반 동적 쿼리
  - `searchByTitle()`: 제목 검색 + 활성 상태 필터 + 페이징
  - `existsByTitle()`: 중복 체크
  - `existsByTitleAndIdNot()`: 수정 시 중복 체크
  - `hasAgreements()`: PolicyAgreement JOIN 체크
  - `deleteById()`: 물리적 삭제

#### 3. Application Service Layer
- **패키지**: `com.stdev.smartmealtable.admin.policy.service`
- **Service**: `PolicyApplicationService.java`
  - 메서드:
    - `getPolicies()`: 목록 조회 (페이징, 검색, 필터)
    - `getPolicy()`: 상세 조회
    - `createPolicy()`: 생성 (중복 체크)
    - `updatePolicy()`: 수정 (중복 체크)
    - `deletePolicy()`: 삭제 (동의 내역 체크)
    - `togglePolicyActive()`: 활성/비활성 토글

#### 4. Controller Layer
- **패키지**: `com.stdev.smartmealtable.admin.policy.controller`
- **Controller**: `PolicyController.java`
  - Base URL: `/api/v1/admin/policies`
  - 6개 엔드포인트 구현

### API 엔드포인트

#### 1. 약관 목록 조회
```http
GET /api/v1/admin/policies?title={검색어}&isActive={활성상태}&page={페이지}&size={크기}
```
- **Query Parameters**:
  - `title` (optional): 제목 검색 (부분 일치)
  - `isActive` (optional): 활성 상태 필터 (true/false)
  - `page` (optional, default=0): 페이지 번호
  - `size` (optional, default=20): 페이지 크기
- **Response**: `PolicyListResponse`

#### 2. 약관 상세 조회
```http
GET /api/v1/admin/policies/{policyId}
```
- **Response**: `PolicyResponse`
- **Error**: 404 NOT_FOUND

#### 3. 약관 생성
```http
POST /api/v1/admin/policies
Content-Type: application/json

{
  "title": "서비스 이용약관",
  "content": "제1조 (목적) ...",
  "type": "REQUIRED",
  "version": "1.0",
  "isMandatory": true
}
```
- **Validation**: 
  - `title`: @NotBlank, @Size(max=100)
  - `content`: @NotBlank
  - `type`: @NotNull (REQUIRED/OPTIONAL)
  - `version`: @NotBlank, @Size(max=20)
  - `isMandatory`: @NotNull
- **Response**: 201 CREATED + `PolicyResponse`
- **Error**: 409 CONFLICT (중복된 제목)

#### 4. 약관 수정
```http
PUT /api/v1/admin/policies/{policyId}
Content-Type: application/json

{
  "title": "서비스 이용약관 (개정)",
  "content": "제1조 (목적) ...",
  "type": "REQUIRED",
  "version": "2.0",
  "isMandatory": true
}
```
- **Response**: `PolicyResponse`
- **Error**: 
  - 404 NOT_FOUND
  - 409 CONFLICT (중복된 제목)

#### 5. 약관 삭제
```http
DELETE /api/v1/admin/policies/{policyId}
```
- **Response**: 204 NO_CONTENT
- **Error**: 
  - 404 NOT_FOUND
  - 409 CONFLICT (동의 내역이 있는 약관)

#### 6. 약관 활성/비활성 토글
```http
PATCH /api/v1/admin/policies/{policyId}/toggle
```
- **Response**: `PolicyResponse` (변경된 isActive 상태 포함)
- **Error**: 404 NOT_FOUND

### 테스트 현황
- **테스트 클래스**: `PolicyControllerTest.java`
- **테스트 케이스**: 17개 작성
- **결과**: ⚠️ **JSON path 수정 필요** (ApiResponse 포맷 변경)

**테스트 케이스 목록**:
1. ✏️ 목록 조회 - 성공
2. ✏️ 목록 조회 - 제목 검색
3. ✏️ 목록 조회 - 활성 상태 필터
4. ✏️ 상세 조회 - 성공
5. ✏️ 상세 조회 - 존재하지 않는 약관 (404)
6. ✏️ 생성 - 성공
7. ✏️ 생성 - 중복된 제목 (409)
8. ✏️ 생성 - 필수 필드 누락 (400)
9. ✏️ 수정 - 성공
10. ✏️ 수정 - 존재하지 않는 약관 (404)
11. ✏️ 수정 - 중복된 제목 (409)
12. ✏️ 삭제 - 성공
13. ✏️ 삭제 - 존재하지 않는 약관 (404)
14. ✏️ 삭제 - 동의 내역이 있는 경우 (409)
15. ✏️ 토글 - 활성 → 비활성
16. ✏️ 토글 - 비활성 → 활성
17. ✏️ 토글 - 존재하지 않는 약관 (404)

---

## 공통 구현 사항

### ErrorType 확장
- **파일**: `smartmealtable-core/src/main/java/com/stdev/smartmealtable/core/error/ErrorType.java`
- **추가된 에러 코드**:

```java
// Category
DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "C001", "이미 존재하는 카테고리 이름입니다"),
CATEGORY_IN_USE(HttpStatus.CONFLICT, "C002", "사용 중인 카테고리는 삭제할 수 없습니다"),

// Policy
DUPLICATE_POLICY_TITLE(HttpStatus.CONFLICT, "P001", "이미 존재하는 약관 제목입니다"),
POLICY_HAS_AGREEMENTS(HttpStatus.CONFLICT, "P002", "동의 내역이 있는 약관은 삭제할 수 없습니다"),

// Store
STORE_OPENING_HOUR_NOT_FOUND(HttpStatus.NOT_FOUND, "S003", "영업시간 정보를 찾을 수 없습니다"),
STORE_TEMPORARY_CLOSURE_NOT_FOUND(HttpStatus.NOT_FOUND, "S004", "임시 휴무 정보를 찾을 수 없습니다"),
DUPLICATE_OPENING_HOUR(HttpStatus.CONFLICT, "S005", "해당 요일의 영업시간이 이미 등록되어 있습니다"),

// Food
FOOD_IN_USE(HttpStatus.CONFLICT, "F002", "사용 중인 메뉴는 삭제할 수 없습니다"),

// Group
DUPLICATE_GROUP_NAME(HttpStatus.CONFLICT, "G001", "이미 존재하는 그룹 이름입니다"),
GROUP_HAS_MEMBERS(HttpStatus.CONFLICT, "G002", "멤버가 있는 그룹은 삭제할 수 없습니다"),
```

### AdminApplication 설정
- **파일**: `smartmealtable-admin/src/main/java/com/stdev/smartmealtable/admin/AdminApplication.java`

```java
@SpringBootApplication(scanBasePackages = "com.stdev.smartmealtable")
@EntityScan(basePackages = "com.stdev.smartmealtable.storage.db")
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

**주의사항**:
- `@EnableJpaRepositories`는 JpaConfig에서만 선언 (중복 방지)
- EntityScan은 필요 (JPA 엔티티 인식)

### 테스트 인프라

#### AbstractAdminContainerTest
- **파일**: `smartmealtable-admin/src/test/java/com/stdev/smartmealtable/admin/common/AbstractAdminContainerTest.java`
- **기능**: Testcontainers MySQL 8.0 공유
- **설정**:
  - `@ServiceConnection` 사용
  - 컨테이너 재사용 (`withReuse(true)`)
  - DB: smartmealtable_admin_test
  - User/Password: admin_test

#### application.yml (test)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
  
  ai:
    vertex:
      ai:
        gemini:
          enabled: false
```

---

## 테스트 현황

### 전체 통계
- **총 테스트 클래스**: 2개
- **총 테스트 케이스**: 29개
- **통과율**: 
  - ✅ CategoryControllerTest: **100%** (12/12)
  - ⚠️ PolicyControllerTest: **0%** (0/17 - JSON path 수정 필요)

### 테스트 실행 방법
```bash
# 카테고리 테스트
./gradlew :smartmealtable-admin:test --tests CategoryControllerTest

# 약관 테스트 (수정 후)
./gradlew :smartmealtable-admin:test --tests PolicyControllerTest

# 전체 ADMIN 테스트
./gradlew :smartmealtable-admin:test
```

---

## 알려진 이슈

### 1. API 응답 포맷 변경
**문제**: ApiResponse 구조가 프로젝트 전반에서 변경됨

**변경 내용**:
```json
// 기존
{
  "success": true,
  "data": { ... },
  "error": null
}

// 현재
{
  "result": "SUCCESS",
  "data": { ... },
  "error": null
}
```

**영향**:
- 모든 Controller 테스트의 JSON path assertion 수정 필요
- `jsonPath("$.success").value(true)` → `jsonPath("$.result").value("SUCCESS")`
- `jsonPath("$.success").value(false)` → `jsonPath("$.result").value("ERROR")`

**해결 방법**:
1. CategoryControllerTest를 제외한 모든 테스트 수정
2. PolicyControllerTest 우선 수정
3. 향후 Group, Store, Food 테스트 작성 시 신규 포맷 사용

### 2. JPA 엔티티 생성자 접근 제한
**문제**: PolicyJpaEntity, MemberJpaEntity 등이 protected 생성자 사용

**해결**: Reflection 기반 헬퍼 메서드 사용
```java
private PolicyJpaEntity createPolicy(...) throws Exception {
    PolicyJpaEntity entity = PolicyJpaEntity.class
        .getDeclaredConstructor().newInstance();
    setField(entity, "title", title);
    // ...
    return entity;
}

private void setField(Object entity, String fieldName, Object value) throws Exception {
    Field field = entity.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(entity, value);
}
```

---

## 다음 단계

### 우선순위 1: 테스트 코드 수정
- [ ] PolicyControllerTest JSON path 수정
- [ ] 전체 ADMIN 테스트 실행 및 검증

### 우선순위 2: Group Management API
- [ ] GroupRepository 인터페이스 확장
- [ ] GroupRepositoryImpl (QueryDSL)
- [ ] GroupApplicationService
- [ ] GroupController
- [ ] GroupControllerTest (신규 포맷 적용)

**예상 엔드포인트**:
- GET `/api/v1/admin/groups` - 목록 조회
- GET `/api/v1/admin/groups/{id}` - 상세 조회
- POST `/api/v1/admin/groups` - 생성
- PUT `/api/v1/admin/groups/{id}` - 수정
- DELETE `/api/v1/admin/groups/{id}` - 삭제

**주요 검증 로직**:
- 이름 중복 체크
- 멤버가 있는 그룹 삭제 방지

### 우선순위 3: Store Management API
- [ ] StoreRepository 인터페이스 확장
- [ ] StoreRepositoryImpl (QueryDSL)
- [ ] OpeningHour, TemporaryClosure 관리 로직
- [ ] StoreApplicationService
- [ ] StoreController
- [ ] StoreControllerTest

**예상 엔드포인트**:
- GET `/api/v1/admin/stores` - 목록 조회
- GET `/api/v1/admin/stores/{id}` - 상세 조회
- POST `/api/v1/admin/stores` - 생성
- PUT `/api/v1/admin/stores/{id}` - 수정
- DELETE `/api/v1/admin/stores/{id}` - 삭제
- GET `/api/v1/admin/stores/{id}/opening-hours` - 영업시간 목록
- POST `/api/v1/admin/stores/{id}/opening-hours` - 영업시간 등록
- PUT `/api/v1/admin/stores/{id}/opening-hours/{hourId}` - 영업시간 수정
- DELETE `/api/v1/admin/stores/{id}/opening-hours/{hourId}` - 영업시간 삭제
- GET `/api/v1/admin/stores/{id}/closures` - 임시 휴무 목록
- POST `/api/v1/admin/stores/{id}/closures` - 임시 휴무 등록
- DELETE `/api/v1/admin/stores/{id}/closures/{closureId}` - 임시 휴무 삭제

**주요 검증 로직**:
- 영업시간: 요일별 중복 등록 방지, 시간 범위 검증
- 임시 휴무: 날짜 범위 검증, 과거 날짜 방지

### 우선순위 4: Food Management API
- [ ] FoodRepository 인터페이스 확장
- [ ] FoodRepositoryImpl (QueryDSL)
- [ ] FoodApplicationService
- [ ] FoodController
- [ ] FoodControllerTest

---

## 부록

### 파일 구조
```
smartmealtable-admin/
├── src/main/java/com/stdev/smartmealtable/admin/
│   ├── AdminApplication.java
│   ├── category/
│   │   ├── controller/
│   │   │   ├── CategoryController.java
│   │   │   └── dto/
│   │   │       ├── CategoryResponse.java
│   │   │       ├── CategoryListResponse.java
│   │   │       ├── CreateCategoryRequest.java
│   │   │       └── UpdateCategoryRequest.java
│   │   └── service/
│   │       ├── CategoryApplicationService.java
│   │       └── dto/
│   │           ├── CategoryServiceResponse.java
│   │           ├── CategoryListServiceResponse.java
│   │           ├── CreateCategoryServiceRequest.java
│   │           └── UpdateCategoryServiceRequest.java
│   └── policy/
│       ├── controller/
│       │   ├── PolicyController.java
│       │   └── dto/
│       │       ├── PolicyResponse.java
│       │       ├── PolicyListResponse.java
│       │       ├── CreatePolicyRequest.java
│       │       └── UpdatePolicyRequest.java
│       └── service/
│           ├── PolicyApplicationService.java
│           └── dto/
│               ├── PolicyServiceResponse.java
│               ├── PolicyListServiceResponse.java
│               ├── CreatePolicyServiceRequest.java
│               └── UpdatePolicyServiceRequest.java
└── src/test/java/com/stdev/smartmealtable/admin/
    ├── common/
    │   └── AbstractAdminContainerTest.java
    ├── category/
    │   └── controller/
    │       └── CategoryControllerTest.java
    └── policy/
        └── controller/
            └── PolicyControllerTest.java
```

### 의존성 구조
```
smartmealtable-admin
├── smartmealtable-core (ApiResponse, ErrorType)
├── smartmealtable-domain (Category, Policy, Repository)
└── smartmealtable-storage:db (JpaEntity, RepositoryImpl)
```

---

**문서 끝**
