# SmartMealTable API Documentation

> **알뜰식탁** 서비스의 RESTful API 문서입니다.

## 📚 문서 바로가기

### 메인 문서
- **[API 전체 문서](./api-docs.html)** - Spring REST Docs로 생성된 전체 API 문서
- **[API 명세서](./API_SPECIFICATION.md)** - Markdown 형식의 API 명세서
- **[구현 진행상황](./IMPLEMENTATION_PROGRESS.md)** - 현재 구현 진행 상태

### 기획/요구사항 문서
- **[PRD (Product Requirements Document)](./plan/PRD.md)** - 제품 요구사항 문서
- **[SRS (Software Requirements Specification)](./plan/SRS.md)** - 소프트웨어 요구사항 명세서
- **[SRD (Software Requirements Document)](./plan/SRD.md)** - 기능 요구사항 문서
- **[추천 시스템 요구사항](./plan/recommendation_requirement_docs.md)** - 추천 시스템 상세 요구사항

### 아키텍처 문서
- **[Aggregate 설계](./architecture/aggregate.md)** - DDD Aggregate 경계 설계
- **[Gradle 멀티모듈 설정](./architecture/GRADLE_SETUP.md)** - Gradle 멀티모듈 구조 가이드

### 배포 문서
- **[분산 배포 가이드](./deploy/DISTRIBUTED_DEPLOYMENT.md)** - 분산 환경 배포 방법
- **[빠른 시작 가이드](./deploy/QUICK_START.md)** - 로컬 개발 환경 빠른 시작

## 🚀 빠른 시작

### 로컬 개발 환경

```bash
# 1. 저장소 클론
git clone https://github.com/Picance/SmartMealTable-springboot-backend-V2.git
cd SmartMealTable-springboot-backend-V2

# 2. 환경 변수 설정
cp .env.example .env
# .env 파일을 열어 실제 값으로 수정

# 3. Docker Compose로 실행
docker-compose -f docker-compose.local.yml up -d

# 4. API 서버 접속
curl http://localhost:8080/api/v1/health
```

### API 테스트

```bash
# 전체 테스트 실행
./gradlew test

# REST Docs 테스트만 실행
./gradlew :smartmealtable-api:test --tests '*RestDocsTest'

# API 문서 생성
./gradlew :smartmealtable-api:asciidoctor
```

## 🏗️ 아키텍처

### 멀티모듈 구조

```
smartmealtable-backend-v2/
├── smartmealtable-core/          # 공통 응답/에러 처리
├── smartmealtable-domain/         # 도메인 모델 & 비즈니스 로직
├── smartmealtable-storage/        # 영속성 계층
│   ├── db/                        # JPA 엔티티 & Repository
│   └── cache/                     # Redis 캐싱
├── smartmealtable-api/            # REST API & Application Service
├── smartmealtable-admin/          # 관리자 API
├── smartmealtable-client/         # 외부 API 클라이언트
│   ├── auth/                      # OAuth 인증
│   └── external/                  # 네이버 지도 등
├── smartmealtable-batch/          # 배치 작업
├── smartmealtable-recommendation/ # 추천 시스템
└── smartmealtable-support/        # 유틸리티
```

### 기술 스택

- **Backend**: Java 21, Spring Boot 3.5.6, Spring MVC
- **Data**: Spring Data JPA, QueryDSL, MySQL 8.0
- **Cache**: Redis
- **AI**: Spring AI (Vertex AI Gemini)
- **Test**: JUnit 5, Mockito, TestContainers
- **Docs**: Spring REST Docs, AsciiDoc
- **Infra**: Docker, Docker Compose, Terraform
- **CI/CD**: GitHub Actions

## 📖 주요 API

### 인증 (Authentication)

- `POST /api/v1/auth/signup/email` - 이메일 회원가입
- `POST /api/v1/auth/login/email` - 이메일 로그인
- `POST /api/v1/auth/login/kakao` - 카카오 소셜 로그인
- `POST /api/v1/auth/login/google` - 구글 소셜 로그인
- `POST /api/v1/auth/logout` - 로그아웃
- `POST /api/v1/auth/refresh` - 토큰 갱신

### 온보딩 (Onboarding)

- `POST /api/v1/onboarding/profile` - 프로필 설정
- `POST /api/v1/onboarding/address` - 주소 등록
- `POST /api/v1/onboarding/budget` - 예산 설정
- `POST /api/v1/onboarding/preference` - 음식 취향 설정

### 예산 관리 (Budget)

- `GET /api/v1/budgets/monthly/{year}/{month}` - 월별 예산 조회
- `GET /api/v1/budgets/daily/{date}` - 일별 예산 조회
- `PUT /api/v1/budgets/daily/{date}` - 일별 예산 수정

### 지출 내역 (Expenditure)

- `POST /api/v1/expenditures` - 지출 내역 등록
- `POST /api/v1/expenditures/parse-sms` - SMS 파싱 후 등록
- `GET /api/v1/expenditures` - 지출 목록 조회
- `GET /api/v1/expenditures/{id}` - 지출 상세 조회
- `PUT /api/v1/expenditures/{id}` - 지출 내역 수정
- `DELETE /api/v1/expenditures/{id}` - 지출 내역 삭제

### 추천 시스템 (Recommendation)

- `GET /api/v1/recommendations` - 개인화 추천 목록 조회
- `GET /api/v1/recommendations/{storeId}/score` - 추천 점수 상세 조회
- `PUT /api/v1/recommendations/type` - 추천 유형 변경

### 즐겨찾기 (Favorite)

- `POST /api/v1/favorites` - 즐겨찾기 추가
- `GET /api/v1/favorites` - 즐겨찾기 목록 조회
- `PUT /api/v1/favorites/order` - 즐겨찾기 순서 변경
- `DELETE /api/v1/favorites/{storeId}` - 즐겨찾기 삭제

### 가게 관리 (Store)

- `GET /api/v1/stores` - 가게 목록 조회 (위치 기반)
- `GET /api/v1/stores/{id}` - 가게 상세 조회
- `GET /api/v1/stores/autocomplete` - 가게 자동완성 검색

### 프로필 및 설정 (Profile & Settings)

- `GET /api/v1/members/me` - 내 프로필 조회
- `PUT /api/v1/members/me` - 프로필 수정
- `PUT /api/v1/members/me/password` - 비밀번호 변경
- `DELETE /api/v1/members/me` - 회원 탈퇴

### 홈 화면 (Home)

- `GET /api/v1/home/dashboard` - 홈 대시보드 조회
- `GET /api/v1/home/stores` - 홈 화면 가게 목록 조회

## 🔒 인증 방식

API는 JWT(JSON Web Token) 기반 인증을 사용합니다.

### 인증 헤더

```
Authorization: Bearer <JWT_TOKEN>
```

### 토큰 갱신

Access Token 만료 시, Refresh Token으로 갱신:

```bash
POST /api/v1/auth/refresh
Authorization: Bearer <REFRESH_TOKEN>
```

## 📊 응답 형식

### 성공 응답

```json
{
  "result": "SUCCESS",
  "data": {
    // 응답 데이터
  },
  "error": null
}
```

### 에러 응답

```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "errorType": "VALIDATION_FAILED",
    "message": "입력값 검증에 실패했습니다.",
    "details": {
      "email": "유효한 이메일 형식이 아닙니다."
    }
  }
}
```

## 🧪 테스트

### 테스트 커버리지

- **Total APIs**: 76개
- **REST Docs Tests**: 141개
- **Test Success Rate**: 100%

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 모듈별 테스트
./gradlew :smartmealtable-api:test
./gradlew :smartmealtable-domain:test
./gradlew :smartmealtable-storage:db:test

# REST Docs 생성
./gradlew :smartmealtable-api:test :smartmealtable-api:asciidoctor
```

## 🚢 배포

### Docker Compose

```bash
# 전체 서비스 실행
./deploy-all.sh

# API 서버만 실행
./deploy-api.sh

# Admin 서버만 실행
./deploy-admin.sh

# Batch 서버만 실행
./deploy-batch.sh
```

### 환경 변수

필수 환경 변수는 `.env.example` 파일을 참고하세요:

- OAuth (Kakao, Google)
- Vertex AI (Gemini)
- Database
- Redis
- Naver Map API

## 📝 문서 업데이트

### REST Docs 재생성

```bash
# 1. 테스트 실행 (snippets 생성)
./gradlew :smartmealtable-api:test --tests '*RestDocsTest'

# 2. HTML 문서 생성
./gradlew :smartmealtable-api:asciidoctor

# 3. docs 디렉토리에 복사
cp smartmealtable-api/build/docs/asciidoc/index.html docs/api-docs.html
```

### GitHub Pages 배포

```bash
# docs 디렉토리를 GitHub에 push
git add docs/
git commit -m "docs: Update API documentation"
git push origin main

# GitHub 저장소 Settings > Pages에서 설정:
# Source: Deploy from a branch
# Branch: main / docs
```

## 🔗 관련 링크

- [GitHub Repository](https://github.com/Picance/SmartMealTable-springboot-backend-V2)
- [Issue Tracker](https://github.com/Picance/SmartMealTable-springboot-backend-V2/issues)
- [PRD (제품 요구사항 문서)](./plan/PRD.md)
- [SRS (소프트웨어 요구사항 명세서)](./plan/SRS.md)
- [SRD (기능 요구사항 문서)](./plan/SRD.md)

## 📞 문의

- **개발자**: luna
- **이메일**: (추가 필요)
- **프로젝트**: 알뜰식탁 (SmartMealTable)

---

**Last Updated**: 2025-10-14
