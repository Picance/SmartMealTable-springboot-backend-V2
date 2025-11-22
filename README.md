# 🍽️ SmartMealTable - 알뜰식탁

![](images/full_logo.png)
> **"누구나 합리적으로 식사하고, 예산을 지키며, 취향에 맞는 한 끼를 고를 수 있는 세상"**

식비 절감과 맞춤형 식사 추천을 목표로 하는 모바일 애플리케이션의 **SpringBoot 백엔드 시스템**입니다.

---

## 📋 목차

- [개요](#개요)
- [기획 내용](#기획-내용)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [프로젝트 구조](#프로젝트-구조)
- [로컬 개발 환경 설정](#로컬-개발-환경-설정)
- [배포 가이드](#배포-가이드)
- [주요 기능](#주요-기능)
- [개발 전략](#개발-전략)
- [문서](#문서)

---

## 개요

### 프로젝트 정보

| 항목 | 설명 |
|------|------|
| **프로젝트명** | SmartMealTable (알뜰식탁) |
| **버전** | v1.1 |
| **Java** | 21 |
| **Spring Boot** | 3.4.10 |
| **빌드 도구** | Gradle |
| **주요 DB** | MySQL, Redis |

### 프로젝트 목표

- 🎯 사용자의 **식비 효율 관리**
- 🍜 **개인 맞춤형 음식점** 및 음식 추천
- 📊 **소비 패턴 분석** 및 예산 관리
- 🌍 **위치 기반 추천** 서비스

---

## 기획 내용

### 핵심 가치

| 구분 | 내용 |
|------|------|
| **핵심 목표** | 사용자의 식비를 효율적으로 관리하고 개인 맞춤형 식단 및 음식점 추천 |
| **부가 목표** | 사용자의 소비 습관 개선 및 음식 낭비 감소 기여 |
| **비즈니스 목표** | 지속적 사용자 확보 및 데이터 기반 추천 서비스 확장 |

### 주요 기능

1. **사용자 인증 (Auth)**
   - 이메일, 카카오, 구글 소셜 로그인
   - JWT 기반 STATELESS 인증
   - 비밀번호 암호화 저장

2. **온보딩 (Onboarding)**
   - 닉네임, 소속, 주소, 예산, 취향 설정
   - 개인 맞춤형 초기 설정

3. **예산 관리 (Budget)**
   - 일/월별 식비 예산 설정
   - 지출 내역 시각화

4. **취향 기반 추천 (Recommendation)**
   - 선호/불호 음식 데이터 기반 추천
   - AI 추천 엔진 (Spring AI 활용)

5. **주소 관리 (Address)**
   - 집, 직장 등 다중 주소 관리
   - 생활 반경 기반 음식점 탐색

6. **지출 내역 파싱**
   - 신용카드 메시지 자동 파싱
   - 지출 내역 자동 등록

### 사용자 시나리오

#### 신규 사용자
```
1. 앱 실행 → 스플래시 화면
2. 로그인 선택 (이메일 / 카카오 / 구글)
3. 온보딩 절차 (프로필 → 주소 → 예산 → 취향 → 약관)
4. 메인 화면 진입 → 예산 대시보드 및 추천 확인
```

#### 기존 사용자
```
1. 로그인 → 메인 화면
2. 일일 식비 확인 및 남은 예산 시각화
3. 주변 추천 식당 확인 및 방문
4. 지출 내역 등록을 통해 취향 데이터 업데이트
```

### 핵심 성과 지표 (KPI)

| 지표 | 목표 | 설명 |
|------|------|------|
| **신규 사용자 온보딩 완료율** | ≥ 80% | 로그인 후 온보딩 절차 완료 비율 |
| **일간 활성 사용자 수 (DAU)** | 1,000명+ | 초기 3개월 내 목표 |
| **평균 예산 유지율** | ≥ 70% | 사용자가 설정한 예산 범위 내 지출 비율 |
| **추천 클릭률 (CTR)** | ≥ 20% | 추천된 식당/음식 클릭 비율 |

---

## 기술 스택

### Backend
- **Java** 21
- **Spring Boot** 3.4.10
- **Spring MVC** (REST API)
- **Spring Data JPA** + **QueryDSL**
- **Spring AI** (지출 내역 파싱, 추천 엔진)
- **Spring Batch** (배치 작업)
- **Spring Rest Docs** (API 문서화)

### Database & Cache
- **MySQL** 8.0 (Primary DB)
- **Redis** 7 (캐싱)

### External API
- **Naver Map API** (지오코딩, 주소 검색)
- **Naver Clova OCR** (영수증 인식)
- **Kakao/Google OAuth** (소셜 로그인)

### Testing & Quality
- **JUnit 5** (단위 테스트)
- **Mockito** (모킹)
- **TestContainers** (통합 테스트)
- **Spring Rest Docs** (API 테스트 문서화)

### DevOps & Infrastructure
- **Docker** & **Docker Compose**
- **Terraform** (IaC)
- **GitHub Actions** (CI/CD)
- **AWS EC2** (배포)

### Logging & Monitoring
- **Logback** (로깅)
- **Micrometer** (메트릭)

### Utilities
- **Lombok** (Boilerplate 제거)
- **Gradle** (빌드 도구)

---

## 아키텍처

### 계층 구조 (Layered Architecture)

```
┌─────────────────────────────────────────┐
│       Presentation Layer (API)          │
│  - Controllers                          │
│  - Exception Handlers                   │
│  - Interceptors                         │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│     Application Layer (Service)         │
│  - Application Services (Use Cases)     │
│  - DTO Mapping                          │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│       Domain Layer (Business Logic)     │
│  - Domain Entities                      │
│  - Domain Services                      │
│  - Repository Interfaces                │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Persistence Layer (Infrastructure)│
│  - JPA Entities                         │
│  - Repository Implementations           │
│  - Database Configuration               │
└─────────────────────────────────────────┘
```

### 멀티 모듈 구조

```
smartmealtable/
├── smartmealtable-api              # 메인 API 서버
├── smartmealtable-admin            # 어드민 서버
├── smartmealtable-domain           # 도메인 로직
├── smartmealtable-storage/
│   ├── db                          # JPA, Repository 구현
│   └── cache                       # Redis 캐싱
├── smartmealtable-client/
│   ├── auth                        # 인증 클라이언트
│   └── external                    # 외부 API 클라이언트
├── smartmealtable-batch/
│   └── crawler                     # 배치/크롤러 작업
├── smartmealtable-scheduler        # 스케줄링 작업
├── smartmealtable-recommendation   # 추천 엔진
├── smartmealtable-core             # 공통 응답, 예외, AOP
├── smartmealtable-support/
│   ├── logging                     # 로깅 유틸
│   └── monitoring                  # 모니터링 유틸
└── smartmealtable-performance      # 성능 테스트
```

### 각 모듈의 역할

| 모듈 | 설명 | 의존성 |
|------|------|--------|
| **api** | 메인 API 서버, 사용자 앱 연동 | domain, storage, client, core |
| **admin** | 어드민 관리 API | domain, storage, client, core |
| **domain** | 도메인 엔티티, 비즈니스 로직 | (Spring Bean Container만 허용) |
| **storage** | JPA 구현, Repository, DB 설정 | domain |
| **client** | 외부 API 클라이언트 (Naver, Kakao 등) | core |
| **core** | ApiResponse, ErrorResult, AOP | (의존성 최소화) |
| **batch** | 배치 작업, 데이터 크롤링 | domain, storage, client |
| **scheduler** | 스케줄링 작업 | domain, storage |
| **recommendation** | 추천 엔진 | domain, storage |
| **support** | 로깅, 모니터링 유틸 | (애플리케이션 독립적) |

---

## 프로젝트 구조

```
.
├── docs/                           # 문서
│   ├── API_SPECIFICATION.md        # API 명세서
│   ├── ADMIN_API_SPECIFICATION.md  # 어드민 API 명세서
│   ├── DEPLOYMENT_GUIDE.md         # 배포 가이드
│   ├── plan/
│   │   ├── SRS.md                  # 소프트웨어 요구사항 명세
│   │   └── PRD.md                  # 제품 요구사항 문서
│   └── ...
├── smartmealtable-api/             # 메인 API 서버
│   ├── src/main/java/
│   ├── src/test/java/
│   ├── build/generated-snippets/   # REST Docs 스니펫
│   └── build.gradle
├── smartmealtable-admin/           # 어드민 서버
├── smartmealtable-domain/          # 도메인 로직
├── smartmealtable-storage/         # 저장소 (DB, Cache)
├── smartmealtable-client/          # 클라이언트 (외부 API)
├── smartmealtable-core/            # 공통 코드
├── smartmealtable-batch/           # 배치 작업
├── smartmealtable-scheduler/       # 스케줄링
├── smartmealtable-recommendation/  # 추천 엔진
├── smartmealtable-support/         # 유틸리티
├── docker-compose.local.yml        # 로컬 개발 Docker Compose
├── docker-compose.api.yml          # API 서버 Docker Compose
├── docker-compose.admin.yml        # 어드민 서버 Docker Compose
├── docker-compose.batch.yml        # 배치 서버 Docker Compose
├── Dockerfile                      # 기본 Dockerfile
├── Dockerfile.api                  # API 서버 Dockerfile
├── Dockerfile.admin                # 어드민 Dockerfile
├── build.gradle                    # Root Gradle 설정
├── settings.gradle                 # 모듈 설정
├── local-dev.sh                    # 로컬 개발 환경 스크립트
├── deploy-all.sh                   # 전체 배포 스크립트
├── deploy-api.sh                   # API 배포 스크립트
├── deploy-admin.sh                 # 어드민 배포 스크립트
├── deploy-docs.sh                  # 문서 배포 스크립트
├── ddl.sql                         # 데이터베이스 스키마
├── dml.sql                         # 초기 데이터
└── main.tf                         # Terraform 설정

```

---

## 로컬 개발 환경 설정

### 필수 사항

- **Java 21** 이상
- **Docker** & **Docker Compose**
- **Gradle** 8.0 이상 (wrapper 포함)
- **Git**

### 1. 환경 변수 설정

```bash
# 프로젝트 루트에서
cp .env.example .env
```

`.env` 파일을 편집하여 필수 설정값 입력:

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=smartmealtable
DB_USER=smartmeal_user
DB_PASSWORD=smartmeal123

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# External APIs
NAVER_MAP_CLIENT_ID=your_naver_map_client_id
NAVER_MAP_CLIENT_SECRET=your_naver_map_client_secret
NAVER_OAUTH_CLIENT_ID=your_naver_oauth_client_id
NAVER_OAUTH_CLIENT_SECRET=your_naver_oauth_client_secret
GOOGLE_OAUTH_CLIENT_ID=your_google_client_id
GOOGLE_OAUTH_CLIENT_SECRET=your_google_client_secret
KAKAO_OAUTH_CLIENT_ID=your_kakao_client_id

# JWT
JWT_SECRET_KEY=your_jwt_secret_key_at_least_256_bits_long
JWT_EXPIRATION_MS=3600000
```

### 2. Docker 컨테이너 실행

```bash
# 로컬 개발 환경 (MySQL + Redis + 애플리케이션)
docker-compose -f docker-compose.local.yml up -d

# 상태 확인
docker-compose -f docker-compose.local.yml ps
```

### 3. 데이터베이스 초기화

```bash
# MySQL 컨테이너 접속
docker exec -it smartmealtable-mysql mysql -uroot -proot123 smartmealtable

# 또는 직접 SQL 파일 실행
mysql -h localhost -u smartmeal_user -psmartmeal123 smartmealtable < ddl.sql
mysql -h localhost -u smartmeal_user -psmartmeal123 smartmealtable < dml.sql
```

### 4. 프로젝트 빌드 및 실행

```bash
# 전체 프로젝트 빌드
./gradlew clean build

# API 서버 실행
./gradlew :smartmealtable-api:bootRun

# 어드민 서버 실행
./gradlew :smartmealtable-admin:bootRun

# 특정 모듈만 빌드
./gradlew :smartmealtable-api:build
```

### 5. 애플리케이션 접근

```
API 서버: http://localhost:8080
어드민 서버: http://localhost:8081
Redis: localhost:6379
MySQL: localhost:3306
```

### 자동 로컬 개발 환경 설정

편의를 위해 제공되는 스크립트 사용:

```bash
chmod +x local-dev.sh
./local-dev.sh
```

이 스크립트는 다음을 수행합니다:
- Docker Compose 실행
- 데이터베이스 초기화
- 프로젝트 빌드

---

## 배포 가이드

### 1. 개발 환경에서 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :smartmealtable-api:test

# 통합 테스트 (TestContainers 사용)
./gradlew :smartmealtable-api:integrationTest
```

### 2. REST Docs 문서 생성

```bash
# 모든 RestDocsTest 실행 및 문서 생성
./deploy-docs.sh

# 특정 테스트만 실행
./deploy-docs.sh --test-filter 'AuthControllerRestDocsTest'

# Git 변경사항 기반 자동 감지
./deploy-docs.sh --auto-detect

# 수용 가능한 테스트 목록 확인
./deploy-docs.sh --list-tests
```

생성된 문서: `smartmealtable-api/build/resources/main/static/index.html`

### 3. Docker 이미지 빌드

```bash
# API 서버 이미지 빌드
docker build -f Dockerfile.api -t smartmealtable-api:latest .

# 어드민 서버 이미지 빌드
docker build -f Dockerfile.admin -t smartmealtable-admin:latest .

# 모든 이미지 빌드
./deploy-all.sh
```

### 4. Docker Compose를 통한 스테이징 배포

```bash
# API 서버
docker-compose -f docker-compose.api.yml up -d

# 어드민 서버
docker-compose -f docker-compose.admin.yml up -d

# 배치 서버
docker-compose -f docker-compose.batch.yml up -d

# 모든 서버
docker-compose -f docker-compose.yml up -d
```

### 5. 클라우드 배포 (AWS EC2)

#### 5.1 Terraform을 통한 인프라 배포

```bash
# Terraform 초기화
terraform init

# 배포 계획 확인
terraform plan -var-file=terraform.tfvars

# 배포 실행
terraform apply -var-file=terraform.tfvars

# 배포 상태 확인
terraform show
```

#### 5.2 수동 배포 (EC2 인스턴스)

```bash
# 1. EC2 인스턴스에 접속
ssh -i smartmealtable-key.pem ec2-user@your-ec2-ip

# 2. 저장소 클론
git clone https://github.com/your-repo/smartmealtable.git
cd smartmealtable

# 3. 환경 변수 설정
sudo nano .env
# 필수 값 입력 후 저장

# 4. Docker 및 Docker Compose 설치
sudo yum update -y
sudo amazon-linux-extras install docker -y
sudo service docker start
sudo usermod -a -G docker ec2-user
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 5. 서비스 시작
docker-compose -f docker-compose.yml up -d

# 6. 로그 확인
docker-compose logs -f
```

### 6. CI/CD 파이프라인 (GitHub Actions)

프로젝트는 GitHub Actions 기반 자동 배포를 지원합니다.

**트리거:**
- `main` 브랜치에 Push
- Pull Request 생성

**작업 흐름:**
1. 코드 체크아웃
2. Java 21 설정
3. Gradle 빌드
4. TestContainers 테스트 실행
5. Docker 이미지 빌드 및 푸시
6. 클라우드 배포

**설정 파일:** `.github/workflows/deploy.yml`

### 7. 모니터링 및 로깅

#### 로그 확인

```bash
# Docker Compose 서비스 로그
docker-compose logs -f api

# 특정 컨테이너 로그
docker logs -f smartmealtable-api

# 로그 파일 위치 (컨테이너 내부)
/logs/application.log
```

#### 헬스 체크

```bash
# API 헬스 체크
curl http://localhost:8080/actuator/health

# 상세 메트릭
curl http://localhost:8080/actuator/metrics
```

#### 메트릭 확인

```bash
# JVM 메모리 사용량
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP 요청 카운트
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### 8. 데이터베이스 백업

```bash
# MySQL 백업
docker exec smartmealtable-mysql mysqldump -uroot -proot123 smartmealtable > backup_$(date +%Y%m%d_%H%M%S).sql

# 백업 복원
mysql -h localhost -u smartmeal_user -psmartmeal123 smartmealtable < backup_20231115_120000.sql
```

### 9. 배포 체크리스트

- [ ] 환경 변수 설정 확인
- [ ] 데이터베이스 마이그레이션 완료
- [ ] 모든 테스트 통과 확인
- [ ] API 문서 최신 상태 확인
- [ ] Docker 이미지 빌드 성공
- [ ] 헬스 체크 통과
- [ ] 모니터링 대시보드 접근 가능
- [ ] 롤백 계획 수립

---

## 주요 기능

### 인증 (Authentication)

```bash
# 회원가입 (이메일)
curl -X POST http://localhost:8080/api/v1/auth/signup/email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "nickname": "nickname"
  }'

# 로그인
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 온보딩 (Onboarding)

```bash
# 온보딩 정보 저장
curl -X POST http://localhost:8080/api/v1/onboarding \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "nickname",
    "address": "서울특별시 강남구",
    "monthlyBudget": 500000,
    "foodPreferences": ["한식", "일식"]
  }'
```

### 예산 관리 (Budget)

```bash
# 예산 조회
curl -X GET http://localhost:8080/api/v1/budgets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 일일 지출 조회
curl -X GET http://localhost:8080/api/v1/expenditures/daily \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 추천 (Recommendation)

```bash
# 음식점 추천
curl -X GET http://localhost:8080/api/v1/recommendations/stores \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 음식 추천
curl -X GET http://localhost:8080/api/v1/recommendations/foods \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 개발 전략

### TDD (Test-Driven Development)

모든 기능은 RED-GREEN-REFACTORING 사이클을 따릅니다:

1. **RED**: 실패하는 테스트 작성
2. **GREEN**: 테스트를 통과시키는 최소한의 코드 작성
3. **REFACTORING**: 코드 개선 및 최적화

### 테스트 전략

- **단위 테스트**: 각 컴포넌트의 독립적 테스트
- **통합 테스트**: TestContainers를 사용한 DB 통합 테스트
- **API 테스트**: Spring REST Docs를 통한 API 테스트 및 문서화
- **성능 테스트**: JMeter를 통한 부하 테스트

### 테스트 작성 원칙

- ✅ 각 테스트의 독립성 보장
- ✅ 해피 패스와 에러 시나리오 모두 테스트
- ✅ 모든 HTTP 상태코드별 테스트 (200, 400, 404, 422, 500 등)
- ✅ 구체적인 에러 메시지 검증
- ✅ 엣지 케이스와 경계값 테스트 포함
- ✅ TestContainers 사용 (H2나 로컬 Docker MySQL 사용 금지)

### 코드 컨벤션

#### DTO 작명

- `XxxServiceRequest`: Controller → Application Service
- `XxxServiceResponse`: Application Service → Controller
- `XxxControllerRequest`: 클라이언트 요청 DTO
- `XxxControllerResponse`: 클라이언트 응답 DTO

#### Annotation 사용

- ❌ `@Setter`, `@Data` 사용 금지 (DTO 제외)
- ✅ `@Getter`, `@NoArgsConstructor`, `@RequiredArgsConstructor` 사용
- ✅ 불변성 유지

#### JPA 연관관계

- ❌ 다른 Aggregate의 연관관계 매핑 금지
- ✅ FK 값을 필드로 사용
- ❌ 물리 FK 제약조건 사용 금지 (논리 FK만 사용)

#### 쿼리 메서드

- 짧은 쿼리: `findBy...`
- 복잡한 쿼리: QueryDSL 사용

### API 명세

- URI: `/api/v1/...` 형식
- 응답: `ApiResponse<T>` 객체 사용
- 에러: `ErrorResult` 객체 사용
- 토큰: JWT 방식 (ArgumentResolver로 파싱)

---

## 문서

### 기획 문서

- 📄 [제품 요구사항 문서 (PRD)](./docs/plan/PRD.md) - 제품 비전 및 요구사항
- 📄 [소프트웨어 요구사항 명세서 (SRS)](./docs/plan/SRS.md) - 상세 기술 요구사항

### API 명세서

- 📋 [사용자 API 명세](./docs/API_SPECIFICATION.md)
- 📋 [어드민 API 명세](./docs/ADMIN_API_SPECIFICATION.md)
- 📖 [API 자동 생성 문서](./smartmealtable-api/build/resources/main/static/index.html) (REST Docs)

### 배포 및 운영

- 🚀 [배포 가이드](./docs/DEPLOY_DOCS_GUIDE.md)
- ☁️ [Terraform 배포 체크리스트](./TERRAFORM_DEPLOYMENT_CHECKLIST.md)
- 📝 [로컬 개발 환경 설정](./local-dev.sh)

### 아키텍처 및 설계

- 🏗️ [멀티 모듈 Layered Architecture](./docs/)
- 🔄 [Spring Boot 3 마이그레이션 정보](./docs/plan/SRS.md#기술-스택)

### 개발 가이드

- 👨‍💻 [Copilot 개발 지침](./.github/copilot-instructions.md)

---

## 주요 라이브러리 및 버전

### Spring Framework
- Spring Boot: 3.4.10
- Spring Data JPA: 3.4.10
- Spring Data Redis: 3.4.10
- Spring Rest Docs: 3.0.0

### Database & ORM
- MySQL Driver: 8.0.36
- JPA Hibernate: 6.5.3
- QueryDSL JPA: 5.0.0

### External Services
- Spring AI (Vertex AI Gemini): 1.0.2
- Naver Map API: REST API
- Spring Social: OAuth 2.0

### Testing
- JUnit 5: 5.10.2
- Mockito: 5.8.0
- TestContainers: 1.19.8
- AssertJ: 3.25.3

### Utilities
- Lombok: 1.18.30
- Jackson: 2.17.2

---

## 문제 해결 (Troubleshooting)

### Docker 관련

```bash
# Docker 데몬이 실행 중인지 확인
docker ps

# Docker Compose 서비스 상태 확인
docker-compose -f docker-compose.local.yml ps

# 컨테이너 로그 확인
docker logs -f smartmealtable-api
```

### 데이터베이스 연결 오류

```bash
# MySQL 접속 확인
mysql -h localhost -u smartmeal_user -psmartmeal123 -e "SELECT 1"

# 포트 확인
lsof -i :3306

# MySQL 컨테이너 재시작
docker-compose -f docker-compose.local.yml restart mysql
```

### 포트 충돌

```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8080
lsof -i :3306
lsof -i :6379

# 프로세스 종료
kill -9 <PID>
```

### 빌드 오류

```bash
# Gradle 캐시 제거
./gradlew clean

# 전체 빌드 다시 실행
./gradlew build -x test

# 특정 모듈만 빌드
./gradlew :smartmealtable-api:build
```

---

## 기여 가이드

### 브랜치 전략 (Git Flow)

```
main (프로덕션)
├── develop (개발)
│   ├── feature/기능명
│   ├── bugfix/버그명
│   └── hotfix/긴급수정
```

### 커밋 메시지 컨벤션

```
[타입] 제목

본문 (선택사항)

푸터 (선택사항)
```

**타입:**
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가/수정
- `docs`: 문서 수정
- `chore`: 빌드, 의존성 등

### 풀 리퀘스트 프로세스

1. Fork 저장소 클론
2. Feature 브랜치 생성 (`git checkout -b feature/새기능`)
3. 코드 작성 및 테스트
4. 커밋 및 Push
5. Pull Request 작성
6. 코드 리뷰
7. Merge

---

## 라이선스

이 프로젝트는 [MIT 라이선스](./LICENSE)를 따릅니다.

---

## 연락처 및 지원

- 📧 Email: ferrater1013@gmail.com
- 🐙 GitHub: [SmartMealTable Repository](https://github.com/Picance)
- 📱 프로젝트 웹사이트: [서비스 배포 사이트](https://smartmealtable.netlify.app)

---

## 변경 이력

### v1.1 (2025-11-23)
- 멀티 모듈 아키텍처 구현
- Spring Boot 3.4.10 업그레이드
- REST Docs 자동 배포 스크립트 추가
- Docker 및 Terraform 설정 완성

### v1.0 (2025-10-07)
- 초기 프로젝트 구조 설정
- 기본 API 엔드포인트 구현
- 온보딩 기능 구현

---

**마지막 업데이트:** 2025-11-23

