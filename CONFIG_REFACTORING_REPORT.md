# 설정 파일 리팩토링 완료 보고서

## 작업 일시
2025-10-15

## 작업 개요
멀티 모듈 아키텍처 원칙에 따라 외부 클라이언트 관련 설정을 core 모듈로 분리하여 의존성 방향을 올바르게 정리했습니다.

## 문제점

### 1. 잘못된 설정 위치
- **기존**: `smartmealtable-api` 모듈에 OAuth, Naver Map API, Spring AI 등 client 관련 설정이 포함됨
- **문제**: 
  - client 모듈이 api 모듈의 설정을 참조할 수 없음 (의존성 방향 위배)
  - api 모듈에만 설정이 있어 다른 모듈(admin, batch)에서 재사용 불가능
  - 멀티 모듈 구조의 관심사 분리 원칙 위배

## 해결 방안

### 옵션 선택
**옵션 1**: 공통 설정을 core 모듈로 이동 (채택 ✅)
- 장점: 모든 실행 모듈(api, admin, batch)에서 재사용 가능
- 장점: 의존성 방향이 올바름 (api → core, client → core)
- 장점: 관심사 분리 명확

## 구현 내용

### 1. Core 모듈에 Client 설정 추가

#### 파일 생성
```
smartmealtable-core/
  └── src/
      ├── main/
      │   └── resources/
      │       └── application-client.yml  ← 신규 생성
      └── test/
          └── resources/
              └── application-client.yml  ← 신규 생성
```

#### application-client.yml (main)
```yaml
# OAuth 설정
oauth:
  kakao:
    client-id: ${KAKAO_CLIENT_ID}
    redirect-uri: ${KAKAO_REDIRECT_URI}
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${GOOGLE_REDIRECT_URI}

# 네이버 지도 API 설정
naver:
  map:
    client-id: ${NAVER_MAP_CLIENT_ID}
    client-secret: ${NAVER_MAP_CLIENT_SECRET}

# Spring AI 설정
spring:
  ai:
    model:
      chat: vertexai
    vertex:
      ai:
        gemini:
          project-id: ${VERTEX_AI_PROJECT_ID}
          # ...
```

#### application-client.yml (test)
```yaml
# OAuth 설정 (테스트용 더미 값)
oauth:
  kakao:
    client-id: test-kakao-client-id
    redirect-uri: http://localhost:3000/auth/kakao/callback
  # ...

# 네이버 지도 API 설정 (테스트용 더미 값)
naver:
  map:
    client-id: test-naver-map-client-id
    client-secret: test-naver-map-client-secret

# Spring AI 자동 설정 비활성화
spring:
  autoconfigure:
    exclude:
      - org.springframework.ai.model.vertexai.autoconfigure.gemini.VertexAiGeminiChatAutoConfiguration
      - org.springframework.ai.model.vertexai.autoconfigure.VertexAiChatAutoConfiguration
```

### 2. API 모듈 설정 수정

#### application.yml (main)
```yaml
spring:
  application:
    name: smartmealtable-api

  # Client 설정 포함
  profiles:
    include: client  # ← 추가

  # Database 설정
  datasource:
    # ...
```

**제거된 섹션:**
- `oauth` 전체 섹션
- `naver.map` 섹션
- `spring.ai` 섹션

#### application.yml (test)
```yaml
spring:
  # Client 설정 포함
  profiles:
    include: client  # ← 추가

  datasource:
    # ...
```

**제거된 섹션:**
- `oauth` 전체 섹션
- `naver.map` 섹션
- `spring.autoconfigure.exclude` 섹션

## 아키텍처 개선

### 의존성 방향 (Before)
```
❌ 잘못된 구조:
client (외부 API 호출) 
  ↓
api (설정 파일 보유) ← 순환 참조 위험
  ↓
client
```

### 의존성 방향 (After)
```
✅ 올바른 구조:
api → core (설정 보유)
  ↑
client → core (설정 보유)
```

### 모듈별 역할 명확화

| 모듈 | 역할 | 설정 파일 |
|-----|------|---------|
| **core** | 공통 설정 및 기본 기능 | application-client.yml |
| **api** | REST API 실행 모듈 | application.yml (api 전용 설정) |
| **client** | 외부 API 호출 | (설정 파일 없음, core 참조) |
| **admin** | 관리자 기능 실행 모듈 | (향후 추가 시 client 설정 재사용) |
| **batch** | 배치 작업 실행 모듈 | (향후 추가 시 client 설정 재사용) |

## 재사용성 향상

### 다른 모듈에서 사용 방법

#### Admin 모듈 (향후)
```yaml
# smartmealtable-admin/src/main/resources/application.yml
spring:
  application:
    name: smartmealtable-admin
  profiles:
    include: client  # core의 client 설정 재사용
```

#### Batch 모듈 (향후)
```yaml
# smartmealtable-batch/src/main/resources/application.yml
spring:
  application:
    name: smartmealtable-batch
  profiles:
    include: client  # core의 client 설정 재사용
```

## 변경 파일 목록

### 신규 생성
1. `smartmealtable-core/src/main/resources/application-client.yml`
2. `smartmealtable-core/src/test/resources/application-client.yml`

### 수정
1. `smartmealtable-api/src/main/resources/application.yml`
   - `spring.profiles.include: client` 추가
   - OAuth, Naver Map, Spring AI 설정 제거
2. `smartmealtable-api/src/test/resources/application.yml`
   - ~~`spring.profiles.include: client` 추가~~ (제거 - 테스트는 직접 더미 값 사용)
   - OAuth, Naver Map 설정을 직접 더미 값으로 포함 (프로파일 include 대신)
   - autoconfigure 설정은 그대로 유지

## 테스트 확인 사항

### 1. 설정 로딩 확인
```bash
# API 모듈 실행 시 client 프로파일이 로드되는지 확인
./gradlew :smartmealtable-api:bootRun

# 로그에서 다음과 같은 메시지 확인:
# The following profiles are active: client
```

### 2. 환경 변수 확인
```bash
# .env 파일에 필요한 환경 변수가 있는지 확인
KAKAO_CLIENT_ID=xxx
KAKAO_REDIRECT_URI=xxx
GOOGLE_CLIENT_ID=xxx
GOOGLE_CLIENT_SECRET=xxx
GOOGLE_REDIRECT_URI=xxx
NAVER_MAP_CLIENT_ID=xxx
NAVER_MAP_CLIENT_SECRET=xxx
VERTEX_AI_PROJECT_ID=xxx
```

### 3. 통합 테스트 실행
```bash
# 모든 테스트가 통과하는지 확인
./gradlew test
```

### 4. 테스트 환경 주의사항 ⚠️

**문제**: 테스트에서 `spring.profiles.include: client`를 사용하면 main의 application-client.yml이 로드되어 환경 변수 플레이스홀더 오류 발생

**해결**: 테스트용 application.yml에서는 client 프로파일을 포함하지 않고, 필요한 OAuth/Naver Map 설정을 직접 더미 값으로 포함

```yaml
# smartmealtable-api/src/test/resources/application.yml
# ❌ 잘못된 방법:
spring:
  profiles:
    include: client  # main의 환경변수 필요한 설정이 로드됨

# ✅ 올바른 방법:
oauth:
  kakao:
    client-id: test-kakao-client-id  # 직접 더미 값 설정
    # ...
```

### 5. 빌드 및 테스트 성공 확인 ✅

```bash
# 빌드 성공
$ ./gradlew :smartmealtable-api:clean :smartmealtable-api:build -x test
BUILD SUCCESSFUL in 3s

# 테스트 성공
$ ./gradlew :smartmealtable-api:test --tests "*.BudgetControllerRestDocsTest"
BUILD SUCCESSFUL in 11s
```

## 추가 권장 사항

### 1. 환경별 설정 분리
향후 필요 시 환경별 설정을 추가할 수 있습니다:
```
application-client.yml           # 기본 설정
application-client-dev.yml       # 개발 환경
application-client-staging.yml   # 스테이징 환경
application-client-prod.yml      # 운영 환경
```

### 2. 설정 암호화
민감한 정보는 환경 변수나 AWS Secrets Manager 등을 활용하여 관리합니다.

### 3. 설정 문서화
`.env.example` 파일에 필요한 환경 변수 목록을 명시합니다.

## 결론

### 개선 효과
1. ✅ **의존성 방향 정리**: core ← api, core ← client
2. ✅ **재사용성 향상**: admin, batch 등 다른 모듈에서 client 설정 재사용 가능
3. ✅ **관심사 분리**: 각 모듈의 책임이 명확해짐
4. ✅ **유지보수성**: client 관련 설정을 한 곳에서 관리
5. ✅ **확장성**: 새로운 실행 모듈 추가 시 client 설정 재사용 가능

### 멀티 모듈 아키텍처 원칙 준수
- Core 모듈이 공통 설정과 기능을 제공
- 실행 모듈(api, admin, batch)이 core를 의존
- Client 모듈도 core를 의존하여 설정 사용
- 순환 참조 없는 깔끔한 의존성 그래프

## 다음 단계
1. API 모듈 실행하여 설정 로딩 확인
2. 통합 테스트 실행
3. Admin, Batch 모듈 추가 시 동일한 패턴 적용
4. .env.example 파일 업데이트
