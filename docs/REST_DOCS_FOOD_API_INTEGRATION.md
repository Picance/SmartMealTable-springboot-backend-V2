# Spring REST Docs에 음식 API 반영 완료

**업데이트 일시**: 2025-10-23 23:30  
**상태**: ✅ **완료**

---

## 📋 개요

음식 API (`/api/v1/foods`)가 **Spring REST Docs**에 완전히 반영되었습니다.

기존에는 API 구현과 테스트 코드만 있었지만, REST Docs 문서화를 통해 자동 생성되는 API 문서에도 포함되도록 업데이트했습니다.

---

## 🎯 수행한 작업

### 1️⃣ **REST Docs 문서 구조 추가**

**파일**: `smartmealtable-api/src/docs/asciidoc/index.adoc`

```adoc
[[food]]
= 음식 (Food)

음식(메뉴) 관련 API입니다. 개별 메뉴의 상세 정보를 조회할 수 있습니다.

== 인증 요구사항

모든 음식 API는 JWT 인증이 필요합니다.

[[food-detail]]
== 메뉴 상세 조회

`GET /api/v1/foods/{foodId}`
```

### 2️⃣ **REST Docs 스니펫 통합**

REST Docs 테스트에서 자동으로 생성되는 스니펫을 AsciiDoc 문서에 포함:

```adoc
=== 요청
include::{snippets}/food/get-food-detail/http-request.adoc[]

==== 요청 헤더
include::{snippets}/food/get-food-detail/request-headers.adoc[]

==== 경로 파라미터
include::{snippets}/food/get-food-detail/path-parameters.adoc[]

=== 성공 응답 (200 OK)
include::{snippets}/food/get-food-detail/http-response.adoc[]

==== 응답 필드
include::{snippets}/food/get-food-detail/response-fields.adoc[]

=== 에러 응답

==== 메뉴 미존재 (404 Not Found)
include::{snippets}/food/get-food-detail-not-found/http-response.adoc[]
include::{snippets}/food/get-food-detail-not-found/response-fields.adoc[]
```

### 3️⃣ **REST Docs 테스트 기반 문서 생성**

**존재하는 테스트**: `GetFoodDetailRestDocsTest.java`
- ✅ `getFoodDetail()` - 메뉴 상세 조회 성공 케이스
- ✅ `getFoodDetail_NotFound()` - 메뉴 미존재 에러 케이스

**생성된 스니펫**:
```
smartmealtable-api/build/generated-snippets/food/get-food-detail/
  ├── http-request.adoc
  ├── request-headers.adoc
  ├── path-parameters.adoc
  ├── http-response.adoc
  ├── response-fields.adoc
  └── ...

smartmealtable-api/build/generated-snippets/food/get-food-detail-not-found/
  ├── http-response.adoc
  └── response-fields.adoc
```

### 4️⃣ **HTML 문서 자동 생성**

Gradle의 `asciidoctor` 태스크를 통해 HTML 문서 자동 생성:

```bash
./gradlew :smartmealtable-api:asciidoctor
```

**생성된 파일**:
```
smartmealtable-api/build/docs/asciidoc/index.html
```

---

## 📊 생성된 REST Docs 내용

### 메뉴 상세 조회 섹션 구조

```html
= 음식 (Food)
  == 인증 요구사항
  == 메뉴 상세 조회
    === 요청
      #### 요청 헤더
      #### 경로 파라미터
    === 성공 응답 (200 OK)
      #### 응답 필드
    === 에러 응답
      #### 메뉴 미존재 (404 Not Found)
    === 예제
      #### cURL
      #### HTTPie
```

### 자동 생성되는 실제 내용

#### 요청 예시 (HTTP Request)
```http
GET /api/v1/foods/1 HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Host: api.smartmealtable.com
```

#### 경로 파라미터
| Parameter | Description |
|-----------|-------------|
| foodId | 조회할 메뉴의 ID |

#### 요청 헤더
| Name | Description |
|------|-------------|
| Authorization | JWT 인증 토큰 (Bearer {token}) |

#### 응답 예시 (HTTP Response)
```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 814

{
  "result": "SUCCESS",
  "data": {
    "foodId": 1,
    "foodName": "교촌 오리지널",
    "description": "교촌의 시그니처 메뉴",
    "price": 18000,
    "imageUrl": "https://cdn.smartmealtable.com/foods/201.jpg",
    "store": { ... },
    "isAvailable": true,
    "budgetComparison": { ... }
  },
  "error": null
}
```

#### 응답 필드
| Field | Type | Description |
|-------|------|-------------|
| result | String | 응답 결과 (SUCCESS/ERROR) |
| data | Object | 메뉴 상세 정보 |
| data.foodId | Number | 메뉴 ID |
| data.foodName | String | 메뉴명 |
| data.store | Object | 판매 가게 정보 |
| ... | ... | ... |

---

## 🔍 REST Docs 생성 프로세스

### 1단계: 테스트 실행
```bash
./gradlew :smartmealtable-api:test --tests GetFoodDetailRestDocsTest
```
↓
REST Docs 스니펫 생성 (build/generated-snippets/)

### 2단계: 문서 빌드
```bash
./gradlew :smartmealtable-api:asciidoctor
```
↓
스니펫들을 index.adoc에 포함시켜 HTML 문서 생성

### 3단계: HTML 생성 완료
```
smartmealtable-api/build/docs/asciidoc/index.html
```

---

## 📚 REST Docs 장점

| 항목 | 설명 |
|------|------|
| **정확성** | 테스트 기반이므로 항상 최신 상태 유지 |
| **자동화** | 테스트 실행 시 자동으로 문서 생성 |
| **신뢰도** | 실제 API 응답 기반 문서화 |
| **유지보수성** | 코드 변경 시 자동 반영 |
| **가독성** | HTML, AsciiDoc 등 다양한 형식 지원 |

---

## 📄 문서 구조

### 전체 목차 (REST Docs)
```
SmartMealTable API Documentation
├── 개요
├── API 서버 정보
├── 공통 응답 형식
├── HTTP Status Codes
├── 인증 (Authentication)
├── 온보딩 (Onboarding)
├── 식당 조회 (Store)
├── 음식 (Food) ⭐ NEW
│   ├── 인증 요구사항
│   └── 메뉴 상세 조회
├── 로그인
├── ...
└── 알림 및 설정 (Settings)
```

---

## 🎯 REST Docs 통합 방식

### GetFoodDetailRestDocsTest.java 구조
```java
@Test
@DisplayName("메뉴 상세 조회 API - REST Docs")
void getFoodDetail() throws Exception {
    mockMvc.perform(get("/api/v1/foods/{foodId}", testFood.getFoodId())
            .header("Authorization", jwtToken)
            .contentType("application/json"))
            .andExpect(status().isOk())
            .andDo(document("food/get-food-detail",  // ← 스니펫 경로
                    pathParameters(...),             // ← 경로 파라미터 문서화
                    authorizationHeader(),            // ← 헤더 문서화
                    responseFields(...)              // ← 응답 필드 문서화
            ));
}
```

### 생성되는 스니펫
```
build/generated-snippets/food/get-food-detail/
├── curl-request.adoc
├── http-request.adoc
├── request-headers.adoc
├── path-parameters.adoc
├── http-response.adoc
├── response-fields.adoc
├── response-body.adoc
└── request-body.adoc
```

---

## 🔗 참고 자료

### 관련 파일
- `smartmealtable-api/src/docs/asciidoc/index.adoc` - REST Docs 소스
- `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/food/controller/GetFoodDetailRestDocsTest.java` - REST Docs 테스트
- `smartmealtable-api/build/docs/asciidoc/index.html` - 생성된 HTML 문서

### Gradle 태스크
- `./gradlew :smartmealtable-api:test` - 테스트 및 스니펫 생성
- `./gradlew :smartmealtable-api:asciidoctor` - HTML 문서 생성

---

## ✅ 완료 항목

- [x] index.adoc에 음식 API 섹션 추가
- [x] 메뉴 상세 조회 엔드포인트 문서 작성
- [x] REST Docs 스니펫 경로 올바르게 설정
- [x] 성공 응답 (200 OK) 자동 문서화
- [x] 에러 응답 (404 Not Found) 자동 문서화
- [x] HTML 문서 생성 및 검증
- [x] Git 커밋 완료

---

## 🎯 다음 단계 (Optional)

1. **추가 음식 API 문서화**
   - 음식 목록 조회 (계획 중인 경우)
   - 카테고리별 음식 조회

2. **문서 배포**
   - REST Docs HTML을 정적 호스팅으로 배포
   - CI/CD 파이프라인에 문서 생성 단계 추가

3. **스타일 커스터마이징**
   - REST Docs HTML 디자인 개선
   - 회사 로고 및 스타일 추가

---

**업데이트 완료**: 2025-10-23 23:30  
**상태**: ✅ 음식 API가 Spring REST Docs에 완전 반영됨
