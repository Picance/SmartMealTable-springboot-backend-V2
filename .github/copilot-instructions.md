# Smartmealtable 서비스의 SpringBoot 백엔드 시스템 계획서

## Overview

Smartmealtable 서비스의 SpringBoot 백엔드 시스템을 구축합니다.

**기술 스택:**
- Java 21
- Spring MVC, Spring Boot
- Spring Data JPA, QueryDSL
- MySQL (Primary DB)
- Logback (로깅)
- Spring Batch (배치 작업)
- Spring Rest Docs (문서화)
- Redis (캐싱)
- Spring Ai (지출 내역 파싱 기능에서 활용)
- testcontainer, Junit5, Mockito (Test)
- Docker compose
- Terraform (IaC)
- Git Action (CI/CD)
- lombok
- 주의!! Spring Security는 사용하지 않는다. 
    - ex) 암호화가 필요한 경우 암호화와 관련된 라이브러리만 가져다 사용한다.

**아키 텍처**
- 멀티 모듈 Layered Architecture
    - Presentation Layer (Api, ...)
    - Application Layer
    - Domain Layer
    - Persistence Layer

**개발 전략**
- TDD로 진행
    - RED-GREEN-REFACTORING

**API 문서화**
- Spring Rest Docs 사용

**테스트 전략:**
- 각 테스트 독립성 보장
- Test Container 사용
- 해피 패스와 에러 시나리오 모두 테스트
- 모든 HTTP 상태코드별 에러 시나리오 테스트 (404, 400, 422)
- 구체적인 에러 메시지 검증
- 엣지 케이스와 경계값 테스트 포함
- Mockist 스타일로 테스트 진행
- 테스트에 실패한다고 테스트 구성 자체를 바꾸면 안 됨. 무조건 Test Container를 사용할 것. 임의로 H2나 로컬의 Docker MySQL로 환경을 바꾸면 안 됨

## 데이터베이스 스키마

## API Spec
- URI는 `/api/v1`과 같은 `/api/버전정보`로 구성되어야 한다.
- 응답 포맷은 ApiResponse<T> 응답 객체를 따른다.

## 지켜야할 컨벤션
### Controller -> application Service
- DTO(XxxServiceRequest)

### application Service -> Controller
- DTO(XxxServiceResponse)

### 각 계층간 통신에서는 DTO를 사용한다.
- 의존관계 방향이 아래를 항하도록 DTO를 위치시킨다.

### Controller Advice
- Controller Advice로 404, 500 등 오류 처리
- 비즈니스 예외, IllegalArgument, ... 등 구분

### JPA 연관관계 매핑 지양
- 같은 Aggregate에 속하는 객체 (ex. Order와 OrderItem)을 제외하고 연관관계 매핑을 사용하지 않는다.
- FK 값을 필드로 사용하도록 한다.

### FK 제약조건 사용하지 않는다.
- 물리 FK는 사용하지 않는다.
- 논리 FK만 사용한다.

### 쿼리 메서드가 길어지는 경우 queryDSL로 JPQL을 작성한다.

### 도메인 모델 패턴을 사용한다.

### Application Service는 유즈케이스에 집중한다.

## 모듈
### **api**
- 독립적으로 실행 가능하며, storage 모듈을 제외한 다른 모듈과 협력하여 기능을 처리하는 모듈

- 예) controller, Application Service, Controller Advice, Interceptor, ArgumentResolver ...

### **domain**
- domain 모듈에는 애플리케이션 로직은 배제하고 entity CRUD 등 domain 자체의 로직인 도메인 로직, 핵심 비즈니스 로직만 포함되어야 합니다.
- Repository 인터페이스 또한 이 곳에 존재합니다.

- Spring Bean Container 라이브러리 의존성, 트랜잭션 관련 의존성을 허용합니다.
- JPA 관련 기술은 이 모듈에 노출되지 않습니다.

### **recommendation**
- 음식 추천 시스템을 위한 모듈입니다.

### **admin**
- admin 모듈에는 서비스 관리/운영에 관련된 로직 및 api만 존재합니다.

### **core**
- 우리 애플리케이션의 비즈니스 로직이나 정책과 직접적인 관련이 있는 공통 코드를 모아둡니다.
- ex) ApiReponse<T> 객체, Error 객체, ErrorResult 객체, Exception 객체
- ex) 애플리케이션의 특정 서비스 계층에 대한 로그를 남기는 AOP

### batch:cralwer
- 배치 작업과 관련된 작업 모듈입니다

### client
- RestClient, Oauth 등에서 사용되는 모듈입니다.

### scheduler
- 스케줄링 작업에 필요한 모듈입니다.

### storage
- jpa, 엔티티, repository 구현체. Infra 역할을 하는 모듈입니다
- 예) db-config, repositoryImpl

### support
- 시스템과 무관하게 어디에서나 사용 가능한 라이브러리를 모아둔 계층
- 범용적인, 애플리케이션 비종속적인 코드
- domain 모듈에 대한 의존성이 전혀 없는 순수한 유틸리티. 마치 잘 만들어진 외부 라이브러리처럼 동작한다.
- 예) 로깅, 모니터링

## 특이사항
- 지출내역 파싱 구현 코드는 legacy/src/main/java/com/stcom/smartmealtable/component/creditmessage 를 참고할 것 (재활용)

## 주의사항
IMPLEMENTATION_PROGRESS는 100% 신뢰할 수 있는 문서가 아님. 항상 사실 여부를 확인하고 진행할 것. 문서에는 구현되어있지 않다고 나와있어도, 실제로 일부 구현되어있을 수 있다.