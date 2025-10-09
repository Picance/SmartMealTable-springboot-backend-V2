# Smartmealtable 멀티 모듈 Gradle 설정 완료

## 프로젝트 구조

```
smartmealtable (루트)
├── smartmealtable-core               (공통 코드 - ApiResponse, Exception 등)
├── smartmealtable-domain             (도메인 로직 및 Repository 인터페이스)
├── smartmealtable-storage
│   ├── db                            (JPA, QueryDSL, Repository 구현체)
│   └── cache                         (Redis)
├── smartmealtable-api                (REST API - Controller, Application Service)
├── smartmealtable-admin              (관리자 API)
├── smartmealtable-recommendation     (음식 추천 시스템)
├── smartmealtable-batch
│   └── crawler                       (배치 크롤러)
├── smartmealtable-scheduler          (스케줄링)
├── smartmealtable-client
│   ├── auth                          (OAuth 인증)
│   └── external                      (외부 API - Spring AI)
└── smartmealtable-support
    ├── logging                       (로깅)
    └── monitoring                    (모니터링)
```

## 모듈별 의존관계

### 1. Core Layer
**smartmealtable-core**
- 역할: 애플리케이션 전역 공통 코드
- 의존성: Spring Boot Starter, Validation, AOP
- 특징: 다른 모듈에 의존하지 않음

### 2. Domain Layer
**smartmealtable-domain**
- 역할: 도메인 모델, 비즈니스 로직, Repository 인터페이스
- 의존성: core 모듈, Spring Transaction
- 특징: JPA 관련 기술은 노출되지 않음

### 3. Persistence Layer
**smartmealtable-storage:db**
- 역할: JPA Entity, Repository 구현체, QueryDSL
- 의존성: domain 모듈, Spring Data JPA, QueryDSL, MySQL
- QueryDSL 설정 포함

**smartmealtable-storage:cache**
- 역할: Redis 캐싱
- 의존성: domain 모듈, Spring Data Redis

### 4. Application Layer
**smartmealtable-api**
- 역할: REST API (Controller, Application Service)
- 의존성: core, domain, storage:db, storage:cache 모듈
- 추가: Spring Web, Spring Rest Docs, Testcontainers
- 특징: 실행 가능한 모듈 (메인 클래스 생성 시 bootJar 활성화)

**smartmealtable-admin**
- 역할: 관리자 전용 API
- 의존성: core, domain, storage:db, storage:cache 모듈
- 추가: Spring Web
- 특징: 실행 가능한 모듈

### 5. Business Modules
**smartmealtable-recommendation**
- 역할: 음식 추천 시스템
- 의존성: core, domain, storage:db 모듈

**smartmealtable-batch:crawler**
- 역할: 배치 크롤러 작업
- 의존성: core, domain, storage:db 모듈
- 추가: Spring Batch
- 특징: 실행 가능한 모듈

**smartmealtable-scheduler**
- 역할: 스케줄링 작업
- 의존성: core, domain, storage:db 모듈
- 특징: 실행 가능한 모듈

### 6. Client Modules
**smartmealtable-client:auth**
- 역할: OAuth 인증
- 의존성: core 모듈
- 추가: Spring OAuth2 Client

**smartmealtable-client:external**
- 역할: 외부 API (Spring AI - Gemini)
- 의존성: core 모듈
- 추가: Spring Web (RestClient), Spring AI

### 7. Support Modules
**smartmealtable-support:logging**
- 역할: 로깅
- 의존성: Logback

**smartmealtable-support:monitoring**
- 역할: 모니터링
- 의존성: Spring Actuator, Micrometer

## 빌드 설정

### 루트 build.gradle
- 플러그인 버전 관리 (apply false)
- allprojects: 그룹, 버전, 저장소 설정
- subprojects: 공통 플러그인 및 의존성 적용
  - Java 21
  - Lombok
  - JUnit5, Mockito

### 공통 의존성
모든 서브프로젝트에 자동으로 적용:
- Lombok
- JUnit5, Mockito
- Spring Boot BOM 3.5.6
- Spring AI BOM 1.0.2

### bootJar 설정
- **라이브러리 모듈**: bootJar disabled, jar enabled
- **실행 가능 모듈**: bootJar enabled, jar disabled
  - smartmealtable-api (ApiApplication)
  - smartmealtable-admin (AdminApplication)
  - smartmealtable-scheduler (SchedulerApplication)
  - smartmealtable-batch:crawler (CrawlerApplication)

## 실행 가능한 애플리케이션

### 1. API Application
```bash
java -jar smartmealtable-api/build/libs/smartmealtable-api-0.0.1-SNAPSHOT.jar
```
- 메인 클래스: `com.stdev.smartmealtable.api.ApiApplication`
- 역할: REST API 서버

### 2. Admin Application
```bash
java -jar smartmealtable-admin/build/libs/smartmealtable-admin-0.0.1-SNAPSHOT.jar
```
- 메인 클래스: `com.stdev.smartmealtable.admin.AdminApplication`
- 역할: 관리자 API 서버

### 3. Scheduler Application
```bash
java -jar smartmealtable-scheduler/build/libs/smartmealtable-scheduler-0.0.1-SNAPSHOT.jar
```
- 메인 클래스: `com.stdev.smartmealtable.scheduler.SchedulerApplication`
- 역할: 스케줄링 작업 실행
- 특징: `@EnableScheduling` 활성화

### 4. Batch Crawler Application
```bash
java -jar smartmealtable-batch/crawler/build/libs/crawler-0.0.1-SNAPSHOT.jar
```
- 메인 클래스: `com.stdev.smartmealtable.batch.crawler.CrawlerApplication`
- 역할: 배치 크롤링 작업 실행
- 특징: `@EnableBatchProcessing` 활성화

## 다음 단계

1. **각 모듈에 소스 디렉토리 구조 생성** ✅ 완료
   ```
   src/
   ├── main/
   │   ├── java/
   │   └── resources/
   └── test/
       ├── java/
       └── resources/
   ```

2. **실행 가능한 모듈에 메인 클래스 생성** ✅ 완료
   - smartmealtable-api (ApiApplication.java)
   - smartmealtable-admin (AdminApplication.java)
   - smartmealtable-scheduler (SchedulerApplication.java)
   - smartmealtable-batch:crawler (CrawlerApplication.java)

3. **Core 모듈에 공통 클래스 생성**
   - ApiResponse<T>
   - Custom Exception 클래스들
   - ErrorCode enum
   - GlobalControllerAdvice

4. **Domain 모듈 구현**
   - 도메인 엔티티 (순수 Java)
   - Repository 인터페이스
   - 도메인 서비스

5. **Storage 모듈 구현**
   - JPA Entity (storage:db)
   - Repository 구현체
   - QueryDSL 쿼리

## 빌드 확인

```bash
# 전체 빌드 (테스트 제외)
./gradlew clean build -x test

# 특정 모듈 빌드
./gradlew :smartmealtable-api:build

# 프로젝트 구조 확인
./gradlew projects

# 의존성 확인
./gradlew :smartmealtable-api:dependencies
```

## 주요 컨벤션

1. **모듈 간 의존관계**
   - 하위 레이어만 의존 (Layered Architecture)
   - core → domain → storage → application

2. **JPA 연관관계**
   - 같은 Aggregate 내에서만 사용
   - FK는 논리적으로만 사용 (물리 FK 제약조건 없음)

3. **Repository**
   - 인터페이스는 domain 모듈
   - 구현체는 storage:db 모듈
   - 긴 쿼리는 QueryDSL 사용

4. **테스트 전략**
   - TDD (RED-GREEN-REFACTORING)
   - Mockist 스타일
   - Testcontainers 사용
   - 모든 HTTP 상태코드 시나리오 테스트

## 빌드 결과

✅ BUILD SUCCESSFUL
✅ 모든 모듈 정상 컴파일
✅ 의존성 관계 정상 설정
✅ 4개의 실행 가능한 애플리케이션 jar 생성 완료

## 변경 사항 (2025-10-07)

### 1. external 모듈 의존성 변경
- **변경 전**: Spring WebFlux
- **변경 후**: Spring Web (RestClient 사용)
- **이유**: RestClient를 사용한 외부 API 통신

### 2. 실행 가능한 모듈 설정 완료
모든 실행 가능한 모듈에 메인 클래스를 생성하고 bootJar를 활성화했습니다:

| 모듈 | 메인 클래스 | 특징 |
|------|-----------|------|
| smartmealtable-api | ApiApplication | REST API 서버 |
| smartmealtable-admin | AdminApplication | 관리자 API 서버 |
| smartmealtable-scheduler | SchedulerApplication | @EnableScheduling |
| smartmealtable-batch:crawler | CrawlerApplication | @EnableBatchProcessing |

모든 메인 클래스는 `scanBasePackages = "com.stdev.smartmealtable"`로 설정하여 모든 모듈의 컴포넌트를 스캔합니다.
