# 📝 SRD: SmartMealTable 관리자 시스템 기능 요구사항

**버전**: v1.0
**작성일**: 2025-11-04
**대상 모듈**: `smartmealtable-admin`

---

## 1. 개요

### 1.1 문서 목적

이 문서는 **SmartMealTable 관리자 시스템** 개발에 필요한 구체적인 기능 요구사항을 정의합니다. 운영 및 관리에 필요한 API의 동작, 데이터 처리 방식, 제약 조건 등을 상세히 기술하여 개발팀이 일관된 방향으로 시스템을 구현할 수 있도록 지원하는 것을 목표로 합니다.

### 1.2 시스템 범위

관리자 시스템은 RESTful API 형태로 제공되며, 다음을 포함한 핵심 마스터 데이터 및 운영 데이터를 관리합니다.

- **마스터 데이터 관리**: 카테고리, 음식점, 메뉴, 그룹(학교/회사), 약관
- **운영 데이터 관리**: 음식점 영업시간, 임시 휴무
- **모니터링 및 통계**: 사용자 현황, 지출 통계, 서비스 운영 지표 조회

**제외 범위**:
- 관리자용 웹 프론트엔드 UI 개발
- 최종 사용자(고객) 대상 기능

---

## 2. 공통 요구사항

### 2.1 API 공통 규칙

- **[REQ-ADMIN-COM-001]** 모든 API 엔드포인트는 `/api/v1/admin` 접두사를 사용합니다.
- **[REQ-ADMIN-COM-002]** 인증 및 인가: 초기 버전에서는 별도의 인증 절차를 생략하나, 향후 확장을 위해 JWT 기반 인증을 적용할 수 있는 구조로 설계합니다.
- **[REQ-ADMIN-COM-003]** 데이터 삭제: 
  - **[REQ-ADMIN-COM-003a]** `deleted_at` 필드가 있는 엔티티(store, member_authentication, expenditure)는 논리적 삭제(Soft Delete)를 적용합니다.
  - **[REQ-ADMIN-COM-003b]** `deleted_at` 필드가 없는 마스터 데이터(category, policy, member_group)는 물리적 삭제를 수행합니다.
- **[REQ-ADMIN-COM-004]** 활성/비활성 관리: `is_active` 필드를 지원하는 엔티티(policy)에 대해서만 상태 토글 API를 제공합니다.

### 2.2 응답 형식

- **[REQ-ADMIN-COM-005]** 모든 API 응답은 `smartmealtable-core` 모듈의 `ApiResponse<T>` 래퍼 객체를 사용해야 합니다.
- **[REQ-ADMIN-COM-006]** 성공 시 `result` 필드는 "SUCCESS"이며, `data` 필드에 결과가 포함됩니다.
- **[REQ-ADMIN-COM-007]** 실패 시 `result` 필드는 "ERROR"이며, `error` 필드에 에러 코드, 메시지, 상세 데이터가 포함됩니다.

### 2.3 유효성 검사

- **[REQ-ADMIN-COM-008]** 모든 생성 및 수정 요청의 Body(DTO)에 대해서는 `javax.validation` 어노테이션을 사용하여 유효성 검사를 수행해야 합니다. (예: `@NotBlank`, `@NotNull`, `@Size`)
- **[REQ-ADMIN-COM-009]** 유효성 검사 실패 시, HTTP 상태 코드 400 (Bad Request)과 함께 구체적인 오류 원인을 응답해야 합니다.

---

## 3. 상세 기능 요구사항

### 3.1 카테고리 관리 (Category Management)

**엔티티**: `category`

- **[REQ-ADMIN-CAT-001] (목록 조회)**: 모든 음식 카테고리 목록을 페이지네이션(Paging)으로 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-CAT-001a]** 이름(name)으로 검색할 수 있는 기능을 제공해야 합니다.
- **[REQ-ADMIN-CAT-002] (상세 조회)**: 특정 카테고리의 ID를 사용하여 상세 정보를 조회할 수 있어야 합니다.
- **[REQ-ADMIN-CAT-003] (생성)**: 새로운 음식 카테고리를 생성할 수 있어야 합니다.
  - **요청 필드**: `name` (이름)
  - **[REQ-ADMIN-CAT-003a]** 생성 시 카테고리 이름(`name`)은 중복될 수 없습니다.
- **[REQ-ADMIN-CAT-004] (수정)**: 기존 카테고리의 정보를 수정할 수 있어야 합니다.
  - **요청 필드**: `name`
- **[REQ-ADMIN-CAT-005] (삭제)**: 특정 카테고리를 물리적으로 삭제할 수 있어야 합니다.
  - **[REQ-ADMIN-CAT-005a]** 해당 카테고리를 사용하는 음식점(store)이나 음식(food)이 존재하는 경우 삭제를 거부하고 적절한 에러 메시지를 반환해야 합니다.

### 3.2 음식점 관리 (Store Management)

**엔티티**: `store`, `store_opening_hour`, `store_temporary_closure`

- **[REQ-ADMIN-STR-001] (목록 조회)**: 모든 음식점 목록을 페이지네이션으로 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-STR-001a]** 필터 조건: `category_id`, `name` (검색), `store_type`
  - **[REQ-ADMIN-STR-001b]** `store_type`은 StoreType enum 값을 허용합니다.
  - **[REQ-ADMIN-STR-001c]** 삭제되지 않은 음식점만 조회되도록 `deleted_at IS NULL` 조건을 기본으로 적용합니다.
- **[REQ-ADMIN-STR-002] (상세 조회)**: 특정 음식점의 ID를 사용하여 상세 정보(영업시간, 임시 휴무 포함)를 조회할 수 있어야 합니다.
- **[REQ-ADMIN-STR-003] (생성)**: 새로운 음식점의 기본 정보를 생성할 수 있어야 합니다.
  - **요청 필드**: `name`, `category_id`, `address`, `lot_number_address`, `latitude`, `longitude`, `phone_number`, `description`, `average_price`, `store_type`, `image_url`
  - **[REQ-ADMIN-STR-003a]** `seller_id`는 NULL 허용됩니다. (판매자 등록은 별도 프로세스)
  - **[REQ-ADMIN-STR-003b]** `latitude`, `longitude`는 DECIMAL(10,7) 형식으로 저장됩니다.
  - **[REQ-ADMIN-STR-003c]** `store_type`은 StoreType enum 값만 허용합니다.
  - **[REQ-ADMIN-STR-003d]** `registered_at` 필드는 생성 시 명시적으로 설정해야 합니다 (비즈니스 필드).
  - **[REQ-ADMIN-STR-003e]** `review_count`, `view_count`, `favorite_count`는 기본값 0으로 자동 설정됩니다.
- **[REQ-ADMIN-STR-004] (수정)**: 기존 음식점의 기본 정보를 수정할 수 있어야 합니다.
- **[REQ-ADMIN-STR-005] (삭제)**: 특정 음식점을 논리적으로 삭제할 수 있어야 합니다.
  - **[REQ-ADMIN-STR-005a]** `deleted_at` 필드를 현재 시각으로 설정하여 삭제 처리합니다.
  - **[REQ-ADMIN-STR-005b]** 삭제된 음식점은 일반 사용자 화면에서 조회되지 않아야 합니다.
- **[REQ-ADMIN-STR-006] (영업시간 관리)**: 특정 음식점의 요일별 영업시간(시작/종료) 및 휴게시간을 추가, 수정, 삭제할 수 있어야 합니다.
  - **[REQ-ADMIN-STR-006a]** 요일(`day_of_week`): MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
  - **[REQ-ADMIN-STR-006b]** `open_time`, `close_time`은 VARCHAR(8) 형식(예: '11:00:00')으로 저장됩니다.
  - **[REQ-ADMIN-STR-006c]** `is_holiday` 필드가 `true`인 경우 해당 요일은 휴무일로 처리하며, `open_time`, `close_time`은 NULL이어야 합니다.
  - **[REQ-ADMIN-STR-006d]** `break_start_time`, `break_end_time`으로 브레이크 타임을 관리할 수 있습니다 (VARCHAR(8) 형식).
- **[REQ-ADMIN-STR-007] (임시 휴무 관리)**: 특정 음식점의 임시 휴무 기간(날짜, 시작/종료 시간, 사유)을 등록하고 삭제할 수 있어야 합니다.
  - **[REQ-ADMIN-STR-007a]** 종일 휴무인 경우 `start_time`, `end_time`은 NULL로 설정합니다.
  - **[REQ-ADMIN-STR-007b]** 부분 휴무인 경우 `start_time`, `end_time`을 TIME 형식으로 모두 설정해야 합니다.
  - **[REQ-ADMIN-STR-007c]** `registered_at` 필드는 DB DEFAULT CURRENT_TIMESTAMP로 자동 설정됩니다 (비즈니스 필드).
  - **[REQ-ADMIN-STR-007d]** `closure_date`는 DATE 타입, `start_time`과 `end_time`은 TIME 타입으로 저장됩니다.
  - **[REQ-ADMIN-STR-007e]** `reason` 필드는 VARCHAR(200)으로 휴무 사유를 저장합니다.

### 3.3 메뉴 관리 (Food/Menu Management)

**엔티티**: `food`

> **주의**: 도메인 엔티티(`Food`)는 `averagePrice` 필드를 사용하고, DB 테이블(`food`)의 `price` 칼럼과 매핑됩니다. 
> Storage 계층의 `FoodJpaEntity`에서 `entity.price = food.getAveragePrice()` 방식으로 변환됩니다.

- **[REQ-ADMIN-FOOD-001] (목록 조회)**: 특정 음식점(`store_id`)에 속한 모든 메뉴 목록을 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-FOOD-001a]** 음식 이름(`food_name`)으로 검색할 수 있는 기능을 제공해야 합니다.
  - **[REQ-ADMIN-FOOD-001b]** 카테고리(`category_id`)로 필터링할 수 있어야 합니다.
  - **[REQ-ADMIN-FOOD-001c]** 등록일(`registered_dt`)로 정렬하여 신메뉴를 우선 조회할 수 있어야 합니다.
- **[REQ-ADMIN-FOOD-002] (생성)**: 특정 음식점에 새로운 메뉴를 추가할 수 있어야 합니다.
  - **요청 필드**: `store_id`, `food_name`, `price`, `description`, `image_url`, `category_id`
  - **[REQ-ADMIN-FOOD-002a]** 도메인 모델의 `averagePrice` 필드가 DB의 `price` 칼럼에 저장됩니다.
  - **[REQ-ADMIN-FOOD-002b]** `price`는 INT 타입으로 저장됩니다 (DB 칼럼명: `price`).
  - **[REQ-ADMIN-FOOD-002c]** `registered_dt`는 생성 시점의 현재 시각으로 자동 설정됩니다 (비즈니스 필드).
- **[REQ-ADMIN-FOOD-003] (수정)**: 기존 메뉴의 정보를 수정할 수 있어야 합니다.
  - **요청 필드**: `food_name`, `price`, `description`, `image_url`, `category_id`
- **[REQ-ADMIN-FOOD-004] (삭제)**: 특정 메뉴를 물리적으로 삭제할 수 있어야 합니다.
  - **[REQ-ADMIN-FOOD-004a]** 삭제하기 전에 해당 음식을 참조하는 즐겨찾기(favorite), 장바구니(cart_item), 지출 내역(expenditure_item) 등이 있는지 확인해야 합니다.
  - **[REQ-ADMIN-FOOD-004b]** 참조 데이터가 존재하는 경우 삭제를 거부하고 적절한 에러 메시지를 반환해야 합니다.

### 3.4 그룹 관리 (Group Management)

**엔티티**: `member_group` (Domain: `Group`)

- **[REQ-ADMIN-GRP-001] (목록 조회)**: 모든 그룹(학교/회사) 목록을 페이지네이션으로 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-GRP-001a]** 필터 조건: `type` (UNIVERSITY/COMPANY/OTHER), `name`
- **[REQ-ADMIN-GRP-002] (상세 조회)**: 특정 그룹의 ID를 사용하여 상세 정보를 조회할 수 있어야 합니다.
- **[REQ-ADMIN-GRP-003] (생성)**: 새로운 그룹을 생성할 수 있어야 합니다.
  - **요청 필드**: `name`, `type`, `address`
  - **[REQ-ADMIN-GRP-003a]** `type` 필드는 UNIVERSITY, COMPANY, OTHER 값만 허용합니다.
- **[REQ-ADMIN-GRP-004] (수정)**: 기존 그룹 정보를 수정할 수 있어야 합니다.
  - **요청 필드**: `name`, `type`, `address`
- **[REQ-ADMIN-GRP-005] (삭제)**: 특정 그룹을 물리적으로 삭제할 수 있어야 합니다.
  - **[REQ-ADMIN-GRP-005a]** 해당 그룹에 속한 회원이 존재하는 경우 삭제를 거부하고 적절한 에러 메시지를 반환해야 합니다.

### 3.5 약관 관리 (Policy Management)

**엔티티**: `policy`

- **[REQ-ADMIN-POL-001] (목록 조회)**: 모든 약관 목록을 버전 정보와 함께 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-POL-001a]** 필터 조건: `type` (REQUIRED, OPTIONAL), `is_active`
- **[REQ-ADMIN-POL-002] (상세 조회)**: 특정 약관 ID를 사용하여 상세 내용을 조회할 수 있어야 합니다.
- **[REQ-ADMIN-POL-003] (생성)**: 새로운 버전의 약관을 생성할 수 있어야 합니다.
  - **요청 필드**: `title`, `content`, `version`, `type`, `is_mandatory`
  - **[REQ-ADMIN-POL-003a]** `is_active` 필드는 생성 시 자동으로 `true`로 설정됩니다.
  - **[REQ-ADMIN-POL-003b]** `type` 필드는 PolicyType enum 값(REQUIRED, OPTIONAL)만 허용합니다.
- **[REQ-ADMIN-POL-004] (수정)**: 기존 약관의 내용을 수정할 수 있어야 합니다.
  - **[REQ-ADMIN-POL-004a]** 법적 리스크를 고려하여 주요 내용 변경 시 신규 버전 생성을 권장하며, 경미한 오탈자 수정 등에만 사용합니다.
- **[REQ-ADMIN-POL-005] (삭제)**: 특정 약관을 물리적으로 삭제할 수 있어야 합니다.
  - **[REQ-ADMIN-POL-005a]** 해당 약관에 동의한 사용자가 존재하는 경우 삭제를 거부하고 적절한 에러 메시지를 반환해야 합니다.
- **[REQ-ADMIN-POL-006] (상태 변경)**: 특정 약관의 활성 상태(`is_active`)를 토글할 수 있어야 합니다.
  - **[REQ-ADMIN-POL-006a]** 비활성화된 약관은 신규 사용자에게 노출되지 않지만, 기존 동의 내역은 유지됩니다.

### 3.6 판매자 관리 (Seller Management) - **향후 구현 예정**

**엔티티**: `seller`

> **주의**: 현재 판매자(Seller) 도메인 엔티티가 구현되지 않았습니다. 이 기능은 향후 구현 예정입니다.
> DB 스키마에는 seller 테이블이 존재하며, store 테이블에 seller_id 참조 칼럼이 있습니다.

- **[REQ-ADMIN-SELLER-001] (목록 조회)**: 모든 판매자 목록을 페이지네이션으로 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-SELLER-001a]** 필터 조건: `store_id`, `login_id`
- **[REQ-ADMIN-SELLER-002] (상세 조회)**: 특정 판매자의 ID를 사용하여 상세 정보를 조회할 수 있어야 합니다.
- **[REQ-ADMIN-SELLER-003] (생성)**: 새로운 판매자 계정을 생성할 수 있어야 합니다.
  - **요청 필드**: `store_id`, `login_id`, `password`
  - **[REQ-ADMIN-SELLER-003a]** `login_id`는 중복될 수 없습니다 (UNIQUE 제약).
  - **[REQ-ADMIN-SELLER-003b]** 비밀번호는 암호화하여 저장해야 합니다.
  - **[REQ-ADMIN-SELLER-003c]** 하나의 음식점(`store_id`)에는 하나의 판매자만 매핑될 수 있습니다 (UNIQUE KEY `uq_store_id`).
- **[REQ-ADMIN-SELLER-004] (수정)**: 기존 판매자의 정보를 수정할 수 있어야 합니다.
  - **요청 필드**: `login_id`, `password` (선택적)
- **[REQ-ADMIN-SELLER-005] (삭제)**: 특정 판매자를 물리적으로 삭제할 수 있어야 합니다.
  - **[REQ-ADMIN-SELLER-005a]** 판매자를 삭제해도 연관된 음식점(`store.seller_id`)은 NULL로 설정되며 삭제되지 않습니다.
- **[REQ-ADMIN-SELLER-006] (비밀번호 초기화)**: 판매자의 비밀번호를 초기화할 수 있어야 합니다.
  - **[REQ-ADMIN-SELLER-006a]** 임시 비밀번호를 생성하고 암호화하여 저장합니다.

### 3.7 통계 및 모니터링 (Statistics & Monitoring)

> **주의**: 통계 기능은 초기 버전에서는 기본적인 집계만 제공하며, 복잡한 분석 기능은 향후 추가될 예정입니다.

- **[REQ-ADMIN-STAT-001] (사용자 통계 조회)**: 주요 사용자 관련 지표를 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-STAT-001a]** 기간별(일/주/월) 신규 가입자 수
    - 조회 기준: `member_authentication.registered_at`
  - **[REQ-ADMIN-STAT-001b]** 전체 회원 수 및 소셜/이메일 가입 비율
    - 조회 기준: `member_authentication.deleted_at IS NULL`인 활성 회원 수
    - 소셜 가입: `social_account`에 연동된 회원
    - 이메일 가입: `hashed_password`가 존재하는 회원
  - **[REQ-ADMIN-STAT-001c]** 탈퇴 회원 수
    - 조회 기준: `member_authentication.deleted_at IS NOT NULL`
  - **[REQ-ADMIN-STAT-001d]** 그룹별 회원 분포 (학교/회사)
    - 조회 기준: `member.group_id` 및 `member_group.type`

- **[REQ-ADMIN-STAT-002] (지출 통계 조회)**: 사용자 지출 관련 통계를 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-STAT-002a]** 기간별 총 지출액 및 평균 지출액
    - 조회 기준: `expenditure.expended_date`, `expenditure.amount`
    - 삭제되지 않은 지출만 집계 (`expenditure.deleted = false`)
  - **[REQ-ADMIN-STAT-002b]** 지출이 가장 많은 상위 카테고리 TOP 5
    - 조회 기준: `expenditure.category_id` 그룹별 합계
  - **[REQ-ADMIN-STAT-002c]** 식사 시간대별 지출 분포
    - 조회 기준: `expenditure.meal_type` (BREAKFAST, LUNCH, DINNER, SNACK)
  - **[REQ-ADMIN-STAT-002d]** 1인당 평균 지출액 (기간별)
    - 조회 기준: 전체 지출액 / 활성 회원 수

- **[REQ-ADMIN-STAT-003] (음식점 통계 조회)**: 음식점 관련 통계를 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-STAT-003a]** 총 등록된 음식점 수 및 카테고리별 분포
    - 조회 기준: `store.deleted_at IS NULL`인 활성 음식점 수
    - 카테고리별: `store.category_id` 그룹별 집계
  - **[REQ-ADMIN-STAT-003b]** 음식점 타입별 분포 (학생식당/일반음식점)
    - 조회 기준: `store.store_type`
  - **[REQ-ADMIN-STAT-003c]** 조회수/리뷰수/즐겨찾기 상위 TOP 10 음식점
    - 조회 기준: `store.view_count`, `store.review_count`, `store.favorite_count` 내림차순
  - **[REQ-ADMIN-STAT-003d]** 등록된 메뉴 수 (총합 및 음식점별 평균)
    - 조회 기준: `food` 테이블의 전체 레코드 수

- **[REQ-ADMIN-STAT-004] (운영 현황 조회)**: 전반적인 서비스 운영 현황을 조회할 수 있어야 합니다.
  - **[REQ-ADMIN-STAT-004a]** 최근 7일간 가게 조회 이력 추이
    - 조회 기준: `store_view_history.viewed_at`
  - **[REQ-ADMIN-STAT-004b]** 임시 휴무 중인 음식점 수
    - 조회 기준: `store_temporary_closure.closure_date = 현재 날짜`
  - **[REQ-ADMIN-STAT-004c]** 최근 등록된 음식점/메뉴 (최근 7일)
    - 조회 기준: `store.registered_at`, `food.registered_dt`
  - **[REQ-ADMIN-STAT-004d]** 평균 예산 및 예산 달성률
    - 조회 기준: 월별/일일 예산 대비 실제 지출 비율

---

## 4. 비기능적 요구사항

- **[REQ-ADMIN-NFR-001] (성능)**: 모든 목록 조회 API는 2초 이내에 응답해야 합니다. 통계 API는 5초 이내 응답을 목표로 하며, 필요시 캐싱을 적용합니다.
- **[REQ-ADMIN-NFR-002] (테스트)**: 모든 API 엔드포인트에 대해 통합 테스트(Happy Path, Error Cases)를 작성해야 하며, 테스트 커버리지는 80% 이상을 유지해야 합니다.
  - **[REQ-ADMIN-NFR-002a]** 테스트는 Testcontainers를 사용하여 실제 MySQL 환경에서 수행합니다.
  - **[REQ-ADMIN-NFR-002b]** 모든 HTTP 상태 코드별 에러 시나리오를 테스트합니다 (400, 404, 409, 422, 500).
- **[REQ-ADMIN-NFR-003] (문서화)**: 모든 API는 Spring Rest Docs를 사용하여 문서화되어야 하며, `api-docs.html`에 자동으로 포함되어야 합니다.
- **[REQ-ADMIN-NFR-004] (로깅)**: 주요 관리자 활동(생성, 수정, 삭제)에 대해서는 로그를 기록하여 추적 가능하도록 해야 합니다.
  - **[REQ-ADMIN-NFR-004a]** 로그 레벨: INFO (성공 작업), WARN (검증 실패), ERROR (시스템 오류)
  - **[REQ-ADMIN-NFR-004b]** 로그 내용: 작업 유형, 대상 엔티티 ID, 요청 시각, 결과 상태
- **[REQ-ADMIN-NFR-005] (트랜잭션)**: 데이터 일관성을 보장하기 위해 모든 생성/수정/삭제 작업은 트랜잭션 내에서 수행되어야 합니다.
  - **[REQ-ADMIN-NFR-005a]** 읽기 전용 작업(`@Transactional(readOnly = true)`)과 쓰기 작업을 명확히 구분합니다.
- **[REQ-ADMIN-NFR-006] (데이터 무결성)**: 외래 키 관계에 있는 데이터 삭제 시 참조 무결성을 확인하고 적절한 에러를 반환해야 합니다.
  - **[REQ-ADMIN-NFR-006a]** 삭제 전 참조 여부를 확인하는 검증 로직을 구현합니다.
- **[REQ-ADMIN-NFR-007] (페이징)**: 목록 조회 API는 기본 페이지 크기 20, 최대 페이지 크기 100으로 제한합니다.
- **[REQ-ADMIN-NFR-008] (입력 검증)**: 
  - **[REQ-ADMIN-NFR-008a]** 모든 문자열 입력에 대해 XSS 방지를 위한 특수문자 검증을 수행합니다.
  - **[REQ-ADMIN-NFR-008b]** 이메일, 전화번호 등 형식이 정해진 필드는 정규식 검증을 적용합니다.
  - **[REQ-ADMIN-NFR-008c]** 필수 필드(`@NotNull`, `@NotBlank`)와 길이 제한(`@Size`)을 명확히 정의합니다.

---

## 5. 데이터 모델 요약

### 5.1 관리 대상 엔티티

| 엔티티 | 테이블명 | 삭제 방식 | 활성화 관리 | 비고 |
|--------|---------|----------|-----------|------|
| Category | `category` | 물리 삭제 | 미지원 | 참조 확인 필요 |
| Store | `store` | Soft Delete (`deleted_at`) | 미지원 | `registered_at`은 NOT NULL, 생성 시 명시적 설정 필요 (비즈니스 필드) |
| Food | `food` | 물리 삭제 | 미지원 | `registered_dt` DB 자동 설정 (비즈니스 필드) |
| StoreOpeningHour | `store_opening_hour` | 물리 삭제 | `is_holiday` | 요일별 영업시간, 브레이크 타임 포함 |
| StoreTemporaryClosure | `store_temporary_closure` | 물리 삭제 | N/A | 임시 휴무 정보, `registered_at` DB 자동 설정 (비즈니스 필드) |
| Group | `member_group` (Domain: `Group`) | 물리 삭제 | 미지원 | 참조 확인 필요 |
| Policy | `policy` | 물리 삭제 | `is_active` | 약관 버전 관리 |
| Seller | `seller` | 물리 삭제 | N/A | **향후 구현 예정**, 음식점당 1명 제한 (UNIQUE KEY `uq_store_id`) |

### 5.2 통계 조회 대상 엔티티

| 엔티티 | 테이블명 | 통계 항목 |
|--------|---------|----------|
| Member | `member` | 회원 수, 추천 유형 분포 |
| MemberAuthentication | `member_authentication` | 가입자 수, 탈퇴자 수, 가입 방식 |
| SocialAccount | `social_account` | 소셜 로그인 비율 |
| Expenditure | `expenditure` | 지출액, 카테고리별 지출, 식사 시간대별 지출 |
| StoreViewHistory | `store_view_history` | 음식점 조회수, 인기 음식점 |
| Favorite | `favorite` | 즐겨찾기 수, 인기 음식점 |
| MonthlyBudget | `monthly_budget` | 평균 월별 예산 |
| DailyBudget | `daily_budget` | 평균 일일 예산 |
| MealBudget | `meal_budget` | 식사별 평균 예산 |
| Preference | `preference` | 선호/불호 카테고리 통계 |

---

## 6. 우선순위 및 구현 순서

### Phase 1: 마스터 데이터 관리 (우선순위: HIGH)
1. 카테고리 관리 (REQ-ADMIN-CAT-001 ~ 005)
2. 그룹 관리 (REQ-ADMIN-GRP-001 ~ 005)
3. 약관 관리 (REQ-ADMIN-POL-001 ~ 006)

### Phase 2: 음식점 및 메뉴 관리 (우선순위: HIGH)
4. 음식점 관리 (REQ-ADMIN-STR-001 ~ 007)
5. 메뉴 관리 (REQ-ADMIN-FOOD-001 ~ 004)

### Phase 2.5: 판매자 관리 (우선순위: LOW - 향후 구현)
6. 판매자 관리 (REQ-ADMIN-SELLER-001 ~ 006) - **도메인 엔티티 구현 후 진행**

### Phase 3: 통계 및 모니터링 (우선순위: MEDIUM)
7. 사용자 통계 (REQ-ADMIN-STAT-001)
8. 지출 통계 (REQ-ADMIN-STAT-002)
9. 음식점 통계 (REQ-ADMIN-STAT-003)
10. 예산 통계 (REQ-ADMIN-STAT-004)
11. 취향 통계 (REQ-ADMIN-STAT-005)

---

## 7. 제외 사항

다음 기능은 초기 버전에서 제외하며, 향후 확장 시 고려합니다:

- **[EXCLUDED-001]** 관리자 권한 레벨 관리 (SUPER_ADMIN, ADMIN, VIEWER 등)
- **[EXCLUDED-002]** 관리자 활동 감사 로그 전용 테이블 (현재는 Logback으로 파일 저장)
- **[EXCLUDED-003]** 대시보드 위젯 커스터마이징
- **[EXCLUDED-004]** 실시간 알림 (웹소켓)
- **[EXCLUDED-005]** CSV/Excel 형식 데이터 내보내기
- **[EXCLUDED-006]** 대용량 데이터 일괄 등록 (Bulk Insert)

---

## 8. 참고 자료

- DDL 파일: `ddl.sql`
- Domain 모델: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain`
- 기존 API 명세: `docs/API_SPECIFICATION.md`
- ADMIN PRD: `docs/plan/ADMIN_PRD.md`

---

## 9. 수정 이력

| 버전 | 일자 | 내용 | 작성자 |
|-----|------|------|--------|
| v1.0 | 2025-11-04 | 초안 작성 | - |
| v1.1 | 2025-11-04 | DDL 및 도메인 엔티티 검증 후 수정<br>- `store.store_type`: CAMPUS_RESTAURANT, GENERAL_RESTAURANT (STUDENT_CAFETERIA 오류 수정)<br>- `food` 생성 시 `store_id` 필드 추가 명시<br>- `food.price` 필드와 도메인 엔티티 `Food.averagePrice` 불일치 확인 및 주의사항 추가<br>- `store` 생성 시 `seller_id` NULL 허용 명시<br>- DB 자동 설정 필드 명확화 (registered_at, registered_dt - 비즈니스 필드)<br>- `store_opening_hour` VARCHAR(8) 형식 명시 ('11:00:00')<br>- `store_opening_hour` `break_start_time`, `break_end_time` 필드 추가<br>- `store_temporary_closure` TIME 형식 명시<br>- `seller` 테이블 UNIQUE 제약 조건 추가 및 향후 구현 예정 명시<br>- `member_group` 테이블의 도메인 엔티티명 `Group` 명시<br>- `food` 삭제 시 참조 확인 대상 테이블 구체화 (favorite, cart_item, expenditure_item)<br>- 통계 조회 기준 필드 및 조건 구체화 | AI Assistant |
- PRD 문서: `docs/plan/PRD.md`, `docs/plan/ADMIN_PRD.md`
- SRD 문서: `docs/plan/SRD.md`
