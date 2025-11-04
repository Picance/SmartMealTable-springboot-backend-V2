# ADMIN_SRS 문서 수정 사항 요약

**수정일**: 2025-11-04  
**수정자**: AI Assistant  
**목적**: 실제 DDL 및 도메인 모델과의 일치성 확보

---

## 주요 변경 사항

### 1. 데이터 삭제 정책 명확화 (REQ-ADMIN-COM-003)

**변경 전**: 모든 엔티티에 Soft Delete 적용
**변경 후**: 엔티티별로 삭제 방식을 구분

- **Soft Delete 적용**: `store`, `member_authentication`, `expenditure` (`deleted_at` 필드 사용)
- **물리적 삭제 적용**: `category`, `policy`, `member_group`, `food`, `seller`
- **이유**: DDL 스키마상 일부 마스터 데이터는 `deleted_at` 필드가 없음

---

### 2. 카테고리 관리 (3.1)

**변경 사항**:
- `description` 필드 제거 (DDL에 존재하지 않음)
- `is_active` 필드 및 토글 API 제거 (DDL에 존재하지 않음)
- 삭제 방식: Soft Delete → 물리적 삭제로 변경
- 삭제 시 참조 무결성 확인 요구사항 추가 (REQ-ADMIN-CAT-005a)

**수정된 필드**:
- 생성/수정 요청: `name` 만 포함

---

### 3. 음식점 관리 (3.2)

**변경 사항**:
- `is_active` 필드 제거 (DDL에 `deleted_at`만 존재)
- 상태 토글 API 제거
- 필터 조건에 `store_type` 추가 (STUDENT_CAFETERIA, GENERAL_RESTAURANT)
- `registered_at` 자동 설정 명시 (REQ-ADMIN-STR-003a)
- 영업시간 관리: `is_holiday` 필드 동작 명시 (REQ-ADMIN-STR-006b)
- 임시 휴무 관리: 종일 휴무 vs 부분 휴무 구분 (REQ-ADMIN-STR-007a, 007b)

**수정된 필드**:
- `store_name` → `name`
- `phone_number`, `category_id` 등 DDL 기준으로 정확히 명시

---

### 4. 메뉴 관리 (3.3)

**변경 사항**:
- `is_active` 필드 및 토글 API 제거
- 삭제 방식: Soft Delete → 물리적 삭제로 변경
- `registered_dt` 자동 설정 명시 (REQ-ADMIN-FOOD-002a)
- `category_id` 필드 추가 (음식도 카테고리를 가짐)
- 삭제 시 참조 확인 요구사항 추가 (REQ-ADMIN-FOOD-004a)

---

### 5. 그룹 관리 (3.4)

**변경 사항**:
- `domain` 필드 제거 (DDL에 존재하지 않음)
- `is_active` 필드 및 토글 API 제거
- 삭제 방식: Soft Delete → 물리적 삭제로 변경
- `type` 값 명시: SCHOOL, COMPANY (REQ-ADMIN-GRP-003a)
- 삭제 시 참조 확인 요구사항 추가 (REQ-ADMIN-GRP-005a)

---

### 6. 약관 관리 (3.5)

**변경 사항**:
- `PolicyType` enum 값 명시 (TERMS_OF_SERVICE, PRIVACY_POLICY 등)
- 삭제 시 참조 확인 요구사항 추가 (REQ-ADMIN-POL-005a)
- 상태 토글 동작 명확화 (REQ-ADMIN-POL-006a)

---

### 7. 판매자 관리 추가 (3.6 - 신규)

**이유**: DDL에 `seller` 테이블이 존재하지만 SRS에 누락됨

**추가된 요구사항**:
- REQ-ADMIN-SELLER-001: 목록 조회
- REQ-ADMIN-SELLER-002: 상세 조회
- REQ-ADMIN-SELLER-003: 생성 (login_id 중복 불가, 음식점당 1명 제한)
- REQ-ADMIN-SELLER-004: 수정
- REQ-ADMIN-SELLER-005: 삭제
- REQ-ADMIN-SELLER-006: 비밀번호 초기화

**관련 제약사항**:
- `login_id` UNIQUE 제약
- `store_id` UNIQUE 제약 (1:1 관계)
- 비밀번호 암호화 저장 필수

---

### 8. 통계 및 모니터링 상세화 (3.7)

**변경 사항**: 통계 조회 기준을 구체적으로 명시

**추가된 통계 항목**:

#### 사용자 통계 (REQ-ADMIN-STAT-001)
- 온보딩 완료율 (001d)
- 탈퇴 회원 수 (001e)

#### 지출 통계 (REQ-ADMIN-STAT-002)
- 식사 시간대별 지출 분포 (002c)
- 예산 달성률 (002d)

#### 음식점 통계 (REQ-ADMIN-STAT-003)
- 즐겨찾기 많은 음식점 TOP 10 (003c)
- 음식점 유형별 분포 (003d)
- 최근 등록된 음식점 목록 (003e)

#### 예산 통계 추가 (REQ-ADMIN-STAT-004 - 신규)
- 평균 월별 예산 (004a)
- 평균 일일 예산 (004b)
- 식사별 평균 예산 (004c)

#### 취향 통계 추가 (REQ-ADMIN-STAT-005 - 신규)
- 선호 카테고리 TOP 10 (005a)
- 기피 카테고리 TOP 10 (005b)
- 추천 시스템 유형별 분포 (005c)

---

### 9. 비기능적 요구사항 보강 (4장)

**추가된 요구사항**:

- **REQ-ADMIN-NFR-002a**: Testcontainers 사용 명시
- **REQ-ADMIN-NFR-002b**: 모든 HTTP 상태 코드별 에러 시나리오 테스트
- **REQ-ADMIN-NFR-004a/b**: 로깅 레벨 및 내용 상세화
- **REQ-ADMIN-NFR-005**: 트랜잭션 관리 (읽기 전용 vs 쓰기 작업 구분)
- **REQ-ADMIN-NFR-006**: 데이터 무결성 (참조 무결성 검증)
- **REQ-ADMIN-NFR-007**: 페이징 제한 (기본 20, 최대 100)
- **REQ-ADMIN-NFR-008**: 입력 검증 (XSS 방지, 정규식 검증, 필드 제약)

---

### 10. 신규 섹션 추가

#### 5장: 데이터 모델 요약
- 관리 대상 엔티티 테이블 (삭제 방식, 활성화 관리 여부)
- 통계 조회 대상 엔티티 테이블

#### 6장: 우선순위 및 구현 순서
- Phase 1: 마스터 데이터 관리
- Phase 2: 음식점 및 메뉴 관리
- Phase 3: 통계 및 모니터링

#### 7장: 제외 사항
- 관리자 권한 레벨 관리
- 관리자 활동 감사 로그 전용 테이블
- 대시보드 위젯 커스터마이징
- 실시간 알림
- CSV/Excel 내보내기
- 대용량 일괄 등록

#### 8장: 참고 자료
- 관련 문서 링크

---

## 검증 결과

### DDL과의 일치성 확인 ✅
- [x] `category` 테이블 필드 일치
- [x] `store` 테이블 필드 일치 (`deleted_at` 확인)
- [x] `food` 테이블 필드 일치 (`price` vs `averagePrice` 정리)
- [x] `member_group` 테이블 필드 일치
- [x] `policy` 테이블 필드 일치 (`is_active`, `is_mandatory`)
- [x] `seller` 테이블 추가
- [x] `store_opening_hour` 동작 명시
- [x] `store_temporary_closure` 동작 명시

### 도메인 모델과의 일치성 확인 ✅
- [x] `Category` 도메인 (name만 사용)
- [x] `Store` 도메인 (필드 매핑 확인)
- [x] `Food` 도메인 (averagePrice 사용)
- [x] `Group` 도메인 (GroupType enum 확인)
- [x] `Policy` 도메인 (PolicyType enum 확인)

---

## 다음 단계

1. **ADMIN_PRD.md 문서와의 일관성 확인**
   - 엔드포인트 목록 업데이트 필요
   - 예상 엔드포인트 수 재계산

2. **구현 우선순위 확정**
   - Phase 1부터 순차적으로 구현 시작

3. **API 명세 작성**
   - 각 엔드포인트별 상세 Request/Response 정의

4. **테스트 시나리오 작성**
   - Happy Path 및 Error Cases 정의
