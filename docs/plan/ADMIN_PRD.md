# 📋 PRD: SmartMealTable 관리자(ADMIN) 시스템

**버전**: v1.0  
**작성일**: 2025-11-04  
**대상 모듈**: `smartmealtable-admin`

---

## 1. 개요

### 1.1 제품 요약

SmartMealTable 관리자 시스템은 **서비스 운영 및 데이터 관리**를 위한 백엔드 API를 제공합니다. 관리자가 카테고리, 음식점, 음식, 약관, 그룹(학교/회사) 등의 마스터 데이터를 관리하고, 사용자 지표 및 통계를 모니터링할 수 있는 기능을 제공합니다.

**운영 포트**: 8081 (별도 인스턴스에서 독립 실행)

---

## 2. 목표

### 2.1 비즈니스 목표

- 효율적인 서비스 운영을 위한 마스터 데이터 관리 자동화
- 실시간 운영 현황 모니터링 및 통계 제시
- 일관성 있는 서비스 품질 유지를 위한 정책 관리
- 운영진의 의사결정을 지원하는 데이터 기반 인사이트 제공

### 2.2 기술 목표

- RESTful API 설계로 확장성과 유지보수성 확보
- Layered Architecture 준수 (Controller → Application Service → Domain Service → Repository)
- TDD 기반 고품질 테스트 코드 작성
- Spring Rest Docs를 통한 자동 API 문서화

### 2.3 비목표

- 실시간 웹 대시보드 UI 개발 (API만 제공)
- 사용자 인증/인가 세분화 (임시적으로 관리자만 접근)
- 복잡한 분석 기능 (기본 통계만 제공)

---

## 3. 사용자 페르소나

### 3.1 주요 사용자

- **운영관리자**: 마스터 데이터(카테고리, 음식점, 음식) 관리
- **정책담당자**: 약관, 그룹 정보 관리
- **비즈니스 분석가**: 사용자 통계, 지출 현황, 운영 지표 조회

### 3.2 역할별 권한

| 역할 | 권한 | 예시 |
|-----|------|------|
| **카테고리 관리자** | 카테고리 CRUD | 음식 카테고리 추가/수정/삭제 |
| **음식점 관리자** | 음식점/메뉴 CRUD | 가게 정보 추가, 메뉴 등록 |
| **정책 관리자** | 약관/그룹 CRUD | 서비스 약관 업데이트, 학교 마스터 관리 |
| **분석담당자** | 통계/리포트 조회(R) | 사용자 현황, 지출 통계 조회 |

---

## 4. 기능 요구사항

### 4.1 카테고리 관리 (Priority: **HIGH**)

**목적**: 음식 카테고리 마스터 데이터 관리

- 카테고리 목록 조회 (페이징)
- 카테고리 상세 조회
- 카테고리 생성
- 카테고리 수정
- 카테고리 삭제 (Soft Delete)
- 활성/비활성 토글

**관련 엔티티**: `Category`

---

### 4.2 음식점(Store) 관리 (Priority: **HIGH**)

**목적**: 음식점 정보 및 메뉴 관리

#### 4.2.1 음식점 기본 정보 관리

- 음식점 목록 조회 (필터: 카테고리, 활성 여부)
- 음식점 상세 조회
- 음식점 생성 (기본정보)
- 음식점 수정 (이름, 주소, 연락처 등)
- 음식점 삭제 (Soft Delete)
- 영업시간 관리 (요일별 시작/종료 시간)
- 임시 휴무 등록/취소
- 활성/비활성 토글

**관련 엔티티**: `Store`, `StoreOpeningHour`, `StoreTemporaryClosure`

#### 4.2.2 메뉴(음식) 관리

- 음식점별 메뉴 목록 조회
- 메뉴 생성 (음식점별)
- 메뉴 정보 수정 (가격, 설명 등)
- 메뉴 삭제 (Soft Delete)
- 메뉴 활성/비활성 토글

**관련 엔티티**: `Food`, `Store`

---

### 4.3 그룹(학교/회사) 관리 (Priority: **MEDIUM**)

**목적**: 온보딩 시 사용자가 선택할 수 있는 소속 마스터 데이터 관리

- 그룹 목록 조회 (필터: 타입(학교/회사))
- 그룹 검색 (키워드)
- 그룹 상세 조회
- 그룹 생성
- 그룹 수정
- 그룹 삭제 (Soft Delete)
- 활성/비활성 토글

**관련 엔티티**: `MemberGroup`

---

### 4.4 약관 관리 (Priority: **MEDIUM**)

**목적**: 서비스 약관 및 개인정보처리방침 등 정책 관리

- 약관 목록 조회 (필터: 활성 여부)
- 약관 상세 조회
- 약관 생성 (제목, 내용, 유형, 버전)
- 약관 수정
- 약관 삭제 (Soft Delete)
- 필수/선택 여부 토글
- 활성/비활성 토글
- 버전 관리 (기존 약관 보존, 새 버전으로 기본값 변경)

**관련 엔티티**: `Policy`

---

### 4.5 사용자 및 통계 조회 (Priority: **MEDIUM**)

**목적**: 운영 현황 모니터링 및 데이터 기반 의사결정

#### 4.5.1 사용자 통계

- 전체 회원 수 조회
- 일별 신규 가입자 수
- 활성 사용자 수 (DAU, MAU)
- 온보딩 완료율
- 소셜 로그인 vs 이메일 로그인 비율

#### 4.5.2 지출 통계

- 일별 총 지출액
- 일별 평균 지출액
- 카테고리별 지출 분포
- 예산 달성률 (예산 내 지출 사용자 비율)

#### 4.5.3 음식점 통계

- 등록된 음식점 수
- 카테고리별 음식점 수
- 최근 조회수 높은 음식점 (인기도)
- 활성/비활성 음식점 비율

**관련 엔티티**: 모든 조회 기반

---

### 4.6 캐시 및 성능 관리 (Priority: **LOW**)

**목적**: 운영 데이터 갱신 시 서비스 성능 유지

- 카테고리 캐시 갱신
- 그룹 마스터 캐시 갱신
- 통계 캐시 갱신 (선택적)

---

## 5. 기술 스택

| 계층 | 기술 |
|-----|------|
| **API Layer** | Spring Web, REST Controllers |
| **Application Layer** | Application Services, DTOs |
| **Domain Layer** | Domain Services, Entities |
| **Persistence Layer** | JPA, QueryDSL, Repository |
| **Infrastructure** | MySQL, Redis (Optional) |
| **Testing** | Testcontainers, JUnit 5, MockMvc |
| **Documentation** | Spring Rest Docs |

---

## 6. API 구조

### 6.1 기본 URI 규칙

```
/api/v1/admin/[리소스]/[작업]
```

**예시**:
- `GET /api/v1/admin/categories` - 카테고리 목록
- `POST /api/v1/admin/categories` - 카테고리 생성
- `PUT /api/v1/admin/categories/{categoryId}` - 카테고리 수정
- `DELETE /api/v1/admin/categories/{categoryId}` - 카테고리 삭제

### 6.2 응답 포맷

모든 응답은 `ApiResponse<T>` 형식을 따름:


#### 성공 응답 (데이터 포함)
```json
{
  "result": "SUCCESS",
  "data": { ... },
  "error": null
}
```

#### 성공 응답 (데이터 없음)
```json
{
  "result": "SUCCESS",
  "data": null,
  "error": null
}
```

#### 에러 응답 구조
```json
{
  "result": "ERROR",
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "사람이 읽을 수 있는 에러 메시지",
    "data": {
      "field": "email",
      "reason": "이미 사용 중인 이메일입니다."
    }
  }
}
```

---

## 7. 구현 로드맵

### Phase 1: 카테고리 관리 (1주일)
- **목표**: 카테고리 CRUD API 완전 구현
- **범위**: Domain → Storage → Controller → Tests → REST Docs
- **산출물**: 6개 엔드포인트 + 문서

### Phase 2: 음식점/메뉴 관리 (2주일)
- **목표**: 음식점 및 메뉴 관리 API
- **범위**: Store CRUD + StoreOpeningHour + Food CRUD
- **산출물**: 12개 엔드포인트

### Phase 3: 그룹/약관 관리 (1주일)
- **목표**: 마스터 데이터 관리 완성
- **범위**: Group CRUD + Policy CRUD
- **산출물**: 8개 엔드포인트

### Phase 4: 통계 및 모니터링 (1주일)
- **목표**: 운영 통계 API
- **범위**: 사용자 통계, 지출 통계, 음식점 통계
- **산출물**: 5개 통계 엔드포인트

---

## 8. 아키텍처 계층 구조

```
ADMIN Module
├── Controller Layer
│   ├── CategoryController
│   ├── StoreController
│   ├── GroupController
│   ├── PolicyController
│   └── StatisticsController
│
├── Application Service Layer
│   ├── CategoryApplicationService
│   ├── StoreApplicationService
│   ├── GroupApplicationService
│   ├── PolicyApplicationService
│   └── StatisticsApplicationService
│
├── DTO Layer
│   ├── request/ (Create, Update 요청)
│   └── response/ (데이터 응답)
│
└── 의존성
    ├── core (ApiResponse, Exception)
    ├── domain (Domain Services, Repository Interfaces)
    └── storage (Repository Implementations, JPA Entities)
```

---

## 9. 비기능 요구사항

| 요구사항 | 상세 내용 |
|--------|---------|
| **성능** | API 응답 시간 < 2초, 캐시 활용으로 DB 부하 감소 |
| **보안** | 관리자 전용 API (임시: 무인증, 향후 JWT 추가) |
| **확장성** | 새로운 관리 대상 추가 시 기존 코드 수정 최소화 |
| **문서화** | Spring Rest Docs 100% 적용 |
| **테스트** | 모든 API에 통합 테스트 작성 (Happy Path + Error Cases) |

---

## 10. 예상 엔드포인트 목록

### 카테고리 (6개)
- `GET /api/v1/admin/categories` - 목록 조회
- `GET /api/v1/admin/categories/{id}` - 상세 조회
- `POST /api/v1/admin/categories` - 생성
- `PUT /api/v1/admin/categories/{id}` - 수정
- `DELETE /api/v1/admin/categories/{id}` - 삭제
- `PATCH /api/v1/admin/categories/{id}/toggle` - 활성/비활성 토글

### 음식점 (12개)
- `GET /api/v1/admin/stores` - 목록 조회
- `GET /api/v1/admin/stores/{id}` - 상세 조회
- `POST /api/v1/admin/stores` - 생성
- `PUT /api/v1/admin/stores/{id}` - 수정
- `DELETE /api/v1/admin/stores/{id}` - 삭제
- `PATCH /api/v1/admin/stores/{id}/toggle` - 토글
- `POST /api/v1/admin/stores/{id}/opening-hours` - 영업시간 추가
- `PUT /api/v1/admin/stores/{storeId}/opening-hours/{id}` - 영업시간 수정
- `DELETE /api/v1/admin/stores/{storeId}/opening-hours/{id}` - 영업시간 삭제
- `POST /api/v1/admin/stores/{id}/temporary-closure` - 임시 휴무 등록
- `DELETE /api/v1/admin/stores/{storeId}/temporary-closure/{id}` - 임시 휴무 취소

### 메뉴/음식 (6개)
- `GET /api/v1/admin/stores/{storeId}/foods` - 메뉴 목록
- `POST /api/v1/admin/stores/{storeId}/foods` - 메뉴 생성
- `PUT /api/v1/admin/foods/{id}` - 메뉴 수정
- `DELETE /api/v1/admin/foods/{id}` - 메뉴 삭제
- `PATCH /api/v1/admin/foods/{id}/toggle` - 토글

### 그룹 (6개)
- `GET /api/v1/admin/groups` - 목록 조회
- `GET /api/v1/admin/groups/{id}` - 상세 조회
- `POST /api/v1/admin/groups` - 생성
- `PUT /api/v1/admin/groups/{id}` - 수정
- `DELETE /api/v1/admin/groups/{id}` - 삭제
- `PATCH /api/v1/admin/groups/{id}/toggle` - 토글

### 약관 (6개)
- `GET /api/v1/admin/policies` - 목록 조회
- `GET /api/v1/admin/policies/{id}` - 상세 조회
- `POST /api/v1/admin/policies` - 생성
- `PUT /api/v1/admin/policies/{id}` - 수정
- `DELETE /api/v1/admin/policies/{id}` - 삭제
- `PATCH /api/v1/admin/policies/{id}/toggle` - 토글

### 통계 (5개)
- `GET /api/v1/admin/statistics/users` - 사용자 통계
- `GET /api/v1/admin/statistics/expenditures` - 지출 통계
- `GET /api/v1/admin/statistics/stores` - 음식점 통계
- `GET /api/v1/admin/statistics/dashboard` - 종합 대시보드
- `GET /api/v1/admin/statistics/export` - 통계 내보내기 (선택사항)

**총 41개 엔드포인트 (Phase 4 기준)**

---

## 11. 성공 지표

| 지표 | 목표 | 측정 방법 |
|-----|-----|---------|
| **API 구현률** | 100% | 모든 엔드포인트 구현 및 테스트 |
| **테스트 커버리지** | ≥ 80% | JaCoCo 리포트 |
| **문서화율** | 100% | Spring Rest Docs 자동 생성 |
| **응답 시간** | ≤ 2초 | 성능 테스트 |
| **에러 처리** | 모든 케이스 | 404, 400, 422, 500 등 |

---

## 12. 위험 요소 및 완화 전략

| 위험 | 영향 | 완화 전략 |
|-----|------|---------|
| DB 성능 저하 | 대용량 통계 조회 실패 | 캐시 활용, 페이징, 인덱스 최적화 |
| 데이터 일관성 | 부정확한 통계 | 트랜잭션 관리, 감사 로그 |
| 보안 미흡 | 관리자 권한 남용 | 임시: 무인증 (IP 제한), 향후 JWT + Role 추가 |

---

## 13. 다음 단계

1. ✅ 이 PRD 검토 및 승인
2. → Phase 1 시작: 카테고리 관리 API 구현
3. → 각 Phase 완료 후 REST Docs 자동 생성
4. → 전체 완료 후 Admin 모듈 통합 테스트
