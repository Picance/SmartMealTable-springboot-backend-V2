# SRS: SmartMealTable (알뜰식탁)

**버전:** 1.0  
**작성일:** 2025-10-08

---

## 1. Introduction

### 1.1 Purpose
이 문서는 '알뜰식탁(SmartMealTable)' 백엔드 시스템 개발에 필요한 기능적, 비기능적 요구사항을 상세히 기술하는 것을 목적으로 합니다. 개발팀, QA팀, 그리고 프로젝트 관련자들 간의 명확한 소통과 이해를 돕기 위한 기준 문서로 사용됩니다.

### 1.2 Scope
본 SRS는 '알뜰식탁' 서비스의 모든 백엔드 시스템 기능 개발에 대한 명세를 다룹니다. 범위는 다음과 같습니다.

*   **포함되는 범위:**
    *   사용자 인증 및 프로필 관리
    *   온보딩 프로세스(주소, 예산, 취향 설정)
    *   지출 내역 관리 및 SMS 파싱
    *   개인화 음식 추천 시스템
    *   즐겨찾는 가게 관리
    *   배치 작업(데이터 집계, 만료 데이터 처리 등)
    *   외부 API(네이버 지도, 소셜 로그인, Spring AI) 연동

*   **포함되지 않는 범위:**
    *   프론트엔드(모바일 앱) UI/UX 상세 설계
    *   인프라(서버, 네트워크) 구축 상세 방안
    *   관리자(Admin) 페이지의 상세 기능(Group 데이터 CRUD 등은 존재만 명시)

### 1.3 Definitions, acronyms, and abbreviations
*   **SRS**: Software Requirements Specification (소프트웨어 요구사항 명세서)
*   **PRD**: Product Requirements Document (제품 요구사항 문서)
*   **SRD**: System Requirements Document (시스템 요구사항 문서)
*   **API**: Application Programming Interface
*   **JWT**: JSON Web Token
*   **OAuth**: Open Authorization
*   **TDD**: Test-Driven Development (테스트 주도 개발)
*   **Cold Start**: 시스템이 사용자 대한 정보를 충분히 가지고 있지 않은 상태에서 추천해야 하는 문제
*   **Haversine formula**: 구면 삼각법을 이용하여 두 지점 간의 거리를 구하는 공식

### 1.4 References
*   제품 요구사항 문서 (PRD.md)
*   시스템 요구사항 문서 (SRD.md)
*   추천 시스템 요구사항 명세서 (recommendation_requirement_docs.md)
*   요구사항 명확화 문서 (REQUIREMENTS_CLARIFICATION.md)
*   데이터베이스 스키마 (ddl.sql)
*   프로젝트 아키텍처 및 컨벤션 (copilot-instructions.md)

### 1.5 Overview
이 문서는 시스템의 전반적인 설명, 기능적/비기능적 요구사항, 외부 인터페이스, 시스템 아키텍처, 데이터 요구사항 순으로 구성됩니다. 각 섹션은 시스템을 이해하고 개발하는 데 필요한 정보를 제공합니다.

---

## 2. Overall description

### 2.1 Product perspective
본 시스템은 사용자의 식비 절감과 맞춤형 식사 추천을 제공하는 '알뜰식탁' 모바일 애플리케이션의 백엔드 서버입니다. Spring Boot 기반의 멀티 모듈(Layered Architecture)로 구성되며, API 서버, 배치 서버, 추천 엔진 등을 포함하는 독립적인 시스템입니다.

### 2.2 Product functions
*   **사용자 관리**: 이메일 및 소셜 계정(카카오, 구글)을 통한 회원가입, 로그인, 프로필 관리, 탈퇴 기능을 제공합니다.
*   **예산 및 지출 관리**: 월별/일별/끼니별 예산 설정 및 관리가 가능하며, 카드 결제 SMS 파싱을 통한 자동 지출 등록을 지원합니다.
*   **개인화 추천**: 사용자의 프로필(절약형, 모험형, 균형형), 선호/불호 음식, 과거 지출 패턴, 현재 위치 등을 종합적으로 분석하여 최적의 음식점과 메뉴를 추천합니다.
*   **가게 정보 제공**: 음식점의 기본 정보, 영업시간, 메뉴, 리뷰, 위치 정보 등을 제공합니다.
*   **즐겨찾기**: 사용자가 선호하는 가게를 저장하고 순서를 관리하는 기능을 제공합니다.
*   **데이터 처리**: 주기적인 배치 작업을 통해 데이터를 집계하고, 시스템 상태를 최신으로 유지합니다.

### 2.3 User classes and characteristics
*   **일반 사용자**: 식비 관리에 관심이 많고, 개인화된 음식 추천을 받고자 하는 모바일 앱 사용자. 기술적 지식 수준은 다양합니다.
*   **시스템 관리자**: `smartmealtable-admin` 모듈을 통해 마스터 데이터(예: 학교/회사 정보)를 관리하는 운영자. 시스템에 대한 높은 이해도를 가집니다.

### 2.4 Operating environment
*   **서버**: Java 21, Spring Boot 3.x 기반으로 동작합니다.
*   **데이터베이스**: MySQL (Primary), Redis (Caching)
*   **배포**: Docker, Terraform, Git Action을 이용한 CI/CD 환경에서 배포됩니다.
*   **모니터링**: Prometheus, Grafana, Logback을 사용합니다.

### 2.5 Design and implementation constraints
*   **개발 언어/프레임워크**: Java, Spring Boot, Spring Data JPA, QueryDSL을 반드시 사용해야 합니다.
*   **아키텍처**: 멀티 모듈 기반의 Layered Architecture를 준수해야 합니다. (`api`, `domain`, `storage` 등)
*   **테스트**: TDD(Test-Driven Development) 방식으로 개발하며, JUnit5, Mockito, Testcontainers를 사용합니다.
*   **API 문서화**: Spring REST Docs를 사용하여 API 문서를 자동으로 생성해야 합니다.
*   **의존성**: 외부 서비스(네이버 지도, 카카오/구글 OAuth, Google Gemini) API가 정상 동작해야 합니다.

### 2.6 Assumptions and dependencies
*   사용자의 모바일 디바이스에서 GPS 좌표를 획득하여 서버로 전송할 수 있다고 가정합니다.
*   외부 API(네이버, 카카오, 구글)의 사양 변경이 없을 것으로 가정하며, 변경 시 대응이 필요합니다.
*   카드사 결제 승인 문자 메시지는 지정된 금융기관(KB국민, NH농협 등)의 정형화된 포맷을 따른다고 가정합니다.

---

## 3. System features

### 3.1. 사용자 인증 및 관리 (User Authentication & Management)

*   **Type**: Functional (F)
*   **ID**: F-AUTH-001
*   **Description**: 사용자는 이메일 또는 소셜 계정(카카오, 구글)을 통해 회원가입 및 로그인을 할 수 있습니다.
*   **Priority**: High
*   **Inputs**:
    *   이메일 가입: 이름, 이메일, 비밀번호
    *   이메일 로그인: 이메일, 비밀번호
    *   소셜 로그인: OAuth 인증 코드
*   **Processing**:
    *   이메일 유효성(형식, 중복)을 검증합니다.
    *   비밀번호는 최소 8자, 영문/숫자/특수문자 조합 규칙을 따르며, 해시 처리하여 저장합니다. (`SHA-256` 이상)
    *   소셜 로그인 시, 플랫폼에서 받은 이메일이 이미 존재하면 기존 계정과 연동하고, 없으면 신규 계정을 생성합니다.
    *   로그인 성공 시, 시스템은 접근(Access) 및 재발급(Refresh)을 위한 JWT를 발급합니다.
*   **Outputs**:
    *   성공: JWT (Access Token, Refresh Token), 사용자 정보
    *   실패: 에러 코드 및 메시지 (예: "사용할 수 없는 이메일입니다.", "비밀번호가 일치하지 않습니다.")
*   **Acceptance criteria**:
    *   [REQ-AUTH-103a] 이메일 중복 검증이 실시간으로 동작해야 합니다.
    *   [REQ-AUTH-302] 소셜 로그인 시 추가 정보 입력 없이 자동 가입/로그인이 처리되어야 합니다.
    *   동일 이메일 계정에 여러 소셜 계정(카카오, 구글)을 연동할 수 있어야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-AUTH-002
*   **Description**: 사용자는 자신의 프로필 정보(닉네임, 소속, 이메일, 비밀번호 등)를 수정하고, 계정을 탈퇴할 수 있습니다.
*   **Priority**: Medium
*   **Inputs**:
    *   비밀번호 변경: 현재 비밀번호, 새 비밀번호
    *   탈퇴: 사용자 동의
*   **Processing**:
    *   비밀번호 변경 시 현재 비밀번호 일치 여부를 먼저 검증합니다.
    *   비밀번호는 90일마다 변경을 권장하며, 만료 시 `password_expires_at`을 갱신합니다.
    *   회원 탈퇴 요청 시, `member_authentication.deleted_at` 필드를 현재 시간으로 업데이트(Soft Delete)합니다.
    *   탈퇴 후 1년이 경과한 사용자의 개인정보(지출, 선호도, 주소 등)는 배치 작업을 통해 영구 삭제(Hard Delete)합니다.
*   **Outputs**:
    *   성공: 성공 메시지
    *   실패: 에러 메시지
*   **Acceptance criteria**:
    *   [REQ-PROFILE-102a] 현재 비밀번호가 일치해야만 새 비밀번호로 변경할 수 있습니다.
    *   [REQ-PROFILE-402e] 탈퇴 후 1년이 지난 데이터는 배치 작업에 의해 영구적으로 삭제되어야 합니다.

### 3.2. 지출 내역 관리 (Spending Management)

*   **Type**: Functional (F)
*   **ID**: F-SPEND-001
*   **Description**: 사용자는 카드 결제 승인 문자 메시지를 붙여넣어 지출 내역을 등록할 수 있습니다.
*   **Priority**: High
*   **Inputs**: 카드 결제 승인 SMS 원문 (String)
*   **Processing**:
    *   입력된 문자열을 Spring AI - Gemini 라이브러리를 통해 파싱합니다.
    *   가게명, 결제 금액, 결제 일시 정보를 추출합니다.
    *   추출된 정보를 기반으로 `expenditure` 테이블에 새로운 레코드를 생성합니다.
    *   파싱 실패 시, 사용자에게 직접 입력을 유도합니다.
*   **Outputs**:
    *   성공: 파싱된 가게명, 금액, 날짜 정보
    *   실패: 에러 메시지
*   **Acceptance criteria**:
    *   [REQ-SPEND-412] KB국민, NH농협 등 사전 정의된 금융기관의 문자 포맷을 분석하여 정보를 정확히 추출해야 합니다.
    *   [REQ-SPEND-413] 추출된 정보는 지출 등록 양식의 해당 필드에 자동으로 채워져야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-SPEND-002
*   **Description**: 사용자는 지출 내역을 직접 입력, 조회, 수정, 삭제할 수 있습니다.
*   **Priority**: High
*   **Inputs**:
    *   등록/수정: 날짜, 상호명, 가격, 카테고리, 식사 유형
    *   조회: 기간 필터(일/주/월)
    *   삭제: 지출 내역 ID
*   **Processing**:
    *   등록된 지출은 `expenditure` 테이블에 저장됩니다.
    *   삭제 시 Soft Delete 방식을 사용하며, 해당 내역은 추천 알고리즘 계산에서 제외됩니다.
    *   지출 등록 시 당일 설정된 예산을 초과하면 사용자에게 알림을 보냅니다. (배치 작업)
*   **Outputs**:
    *   조회: 기간에 맞는 지출 목록, 일별 지출 그래프 데이터
    *   기타: 성공/실패 메시지
*   **Acceptance criteria**:
    *   [REQ-SPEND-103b] 필터 변경 시 그래프와 목록이 즉시 갱신되어야 합니다.
    *   [REQ-SPEND-305] 삭제된 지출 내역은 사용자에게 보이지 않아야 하며, 추천 점수 계산에 포함되지 않아야 합니다.
        *   [REQ-SPEND-501] 예산 초과 시에도 지출 등록은 가능해야 하며, 강제로 제한하지 않습니다.

### 3.4. 가게 정보 관리 (Store Information Management)

*   **Type**: Functional (F)
*   **ID**: F-STORE-001
*   **Description**: 사용자는 음식점 목록을 조회하고, 다양한 조건으로 검색 및 필터링할 수 있습니다.
*   **Priority**: High
*   **Inputs**: 검색어(선택), 위치(위도/경도), 필터 조건(반경, 영업시간, 가게유형)
*   **Processing**:
    *   사용자가 입력한 검색어로 가게명(`store.name`) 또는 카테고리(`category.name`)를 검색합니다.
    *   위치 기반 필터링: 사용자 위치로부터 지정된 반경(0.5km/1km/2km) 내의 가게만 조회합니다.
    *   영업 시간 필터: '영업 중만' 선택 시, 현재 시간이 영업시간(`store_opening_hour`) 내에 있고 임시 휴무(`store_temporary_closure`)가 아닌 가게만 표시합니다.
    *   가게 유형 필터: 학식(`is_campus_restaurant=true`), 음식점, 전체 중 선택 가능합니다.
    *   조회 시마다 `store_view_history` 테이블에 조회 이력을 기록합니다.
*   **Outputs**: 가게 목록 (가게명, 주소, 평균 가격, 리뷰 수, 거리, 영업 상태 등)
*   **Acceptance criteria**:
    *   [REQ-STORE-101] 검색어 입력 없이도 주변 가게 목록을 조회할 수 있어야 합니다.
    *   [REQ-STORE-103] 필터 조건 변경 시 결과가 즉시 갱신되어야 합니다.
    *   [REQ-STORE-105] 임시 휴무 중인 가게는 '영업 중만' 필터 적용 시 제외되어야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-STORE-002
*   **Description**: 사용자는 특정 음식점의 상세 정보를 조회할 수 있습니다.
*   **Priority**: High
*   **Inputs**: 가게 ID
*   **Processing**:
    *   가게 기본 정보(`store` 테이블): 이름, 주소, 좌표, 전화번호, 평균 가격, 리뷰 수, 조회수 등을 조회합니다.
    *   영업 시간(`store_opening_hour` 테이블): 요일별 영업 시간 정보를 조회합니다.
    *   메뉴 정보(`food` 테이블): 메뉴명, 가격, 이미지를 조회합니다.
    *   임시 휴무 정보(`store_temporary_closure` 테이블): 현재 임시 휴무 중인지 확인합니다.
    *   상세 페이지 조회 시 `store_view_history`에 기록하고 `store.view_count`를 증가시킵니다.
*   **Outputs**: 가게 상세 정보 (기본 정보, 영업 시간, 메뉴, 임시 휴무 상태)
*   **Acceptance criteria**:
    *   [REQ-STORE-201] 가게의 모든 관련 정보가 한 번의 API 호출로 조회되어야 합니다.
    *   [REQ-STORE-203] 임시 휴무 중일 경우 명확한 안내 메시지가 표시되어야 합니다.
    *   [REQ-STORE-204] 상세 조회 시마다 조회수가 정확히 증가해야 합니다.

### 3.5. 즐겨찾기 관리 (Favorite Management)

*   **Type**: Functional (F)
*   **ID**: F-FAV-001
*   **Description**: 사용자는 자주 방문하는 가게를 즐겨찾기로 등록하고 관리할 수 있습니다.
*   **Priority**: Medium
*   **Inputs**: 가게 ID, 정렬 순서(선택)
*   **Processing**:
    *   즐겨찾기 추가: `favorite` 테이블에 새 레코드를 생성하며, `display_order`는 현재 최대값 +1로 자동 설정됩니다.
    *   즐겨찾기 삭제: 해당 가게의 즐겨찾기 레코드를 삭제하고, 나머지 항목들의 `display_order`를 재정렬합니다.
    *   순서 변경: 사용자가 드래그 앤 드롭으로 순서를 변경하면 `display_order` 값을 업데이트합니다.
    *   즐겨찾기 목록 조회: `display_order` 오름차순으로 정렬된 목록을 반환합니다.
*   **Outputs**: 즐겨찾기 목록 (가게 정보, 순서 포함) 또는 성공/실패 메시지
*   **Acceptance criteria**:
    *   [REQ-FAV-101] 즐겨찾기 추가 시 중복 검사를 수행하여 동일 가게의 중복 등록을 방지해야 합니다.
    *   [REQ-FAV-103] 즐겨찾기는 사용자별로 독립적으로 관리되어야 합니다.
    *   [REQ-FAV-201] 순서 변경 시 다른 항목들의 순서가 자동으로 조정되어야 합니다.
    *   즐겨찾기 여부는 추천 알고리즘의 점수 계산에 영향을 주지 않습니다.

### 3.6. 프로필 및 설정 관리 (Profile & Settings Management)

*   **Type**: Functional (F)
*   **ID**: F-PROFILE-001
*   **Description**: 사용자는 자신의 프로필 정보를 조회하고 수정할 수 있습니다.
*   **Priority**: Medium
*   **Inputs**: 수정할 프로필 정보 (닉네임, 소속 등)
*   **Processing**:
    *   프로필 조회: `member`, `member_authentication`, `groups` 테이블에서 사용자 정보를 조회합니다.
    *   닉네임 수정: `member.nickname` 필드를 업데이트합니다.
    *   소속 변경: `member.group_id`를 업데이트합니다. 소속은 관리자가 등록한 마스터 데이터에서만 선택 가능합니다.
*   **Outputs**: 프로필 정보 또는 성공/실패 메시지
*   **Acceptance criteria**:
    *   [REQ-PROFILE-101] 프로필 조회 시 모든 관련 정보(소속명 포함)가 제공되어야 합니다.
    *   [REQ-PROFILE-103] 닉네임은 중복 검증 없이 자유롭게 변경 가능해야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-PROFILE-002
*   **Description**: 사용자는 등록된 주소를 관리(추가, 수정, 삭제, 기본 주소 설정)할 수 있습니다.
*   **Priority**: High
*   **Inputs**: 주소 정보 (별칭, 분류, 도로명 주소, 지번 주소, 좌표)
*   **Processing**:
    *   주소 추가: `address_history` 테이블에 새 레코드를 생성합니다.
    *   주소 수정: 기존 레코드의 정보를 업데이트합니다.
    *   주소 삭제: 해당 레코드를 삭제합니다. 단, 기본 주소(`is_primary=true`)는 다른 주소가 있을 경우에만 삭제 가능합니다.
    *   기본 주소 설정: 선택한 주소의 `is_primary`를 `true`로 설정하고, 기존 기본 주소는 `false`로 변경합니다.
*   **Outputs**: 주소 목록 또는 성공/실패 메시지
*   **Acceptance criteria**:
    *   [REQ-PROFILE-301] 사용자는 최소 1개 이상의 주소를 보유해야 합니다.
    *   [REQ-PROFILE-303] 기본 주소는 반드시 1개만 존재해야 합니다.
    *   [REQ-PROFILE-304] 주소 추가 시 네이버 지도 API를 통해 검증된 주소만 등록 가능해야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-PROFILE-003
*   **Description**: 사용자는 예산(일별, 월별, 끼니별)을 조회하고 수정할 수 있습니다.
*   **Priority**: High
*   **Inputs**: 예산 정보 (일별 총액, 끼니별 금액, 월별 총액)
*   **Processing**:
    *   예산 조회: `daily_budget`, `monthly_budget`, `meal_budget` 테이블에서 현재 설정된 예산을 조회합니다.
    *   일별 예산 수정: 끼니별 예산(`meal_budget`)과 일별 총 예산(`daily_budget.total_budget`)을 업데이트합니다.
    *   월별 예산 수정: `monthly_budget.total_budget`을 업데이트합니다. 일별 예산과 독립적으로 관리됩니다.
*   **Outputs**: 예산 정보 또는 성공/실패 메시지
*   **Acceptance criteria**:
    *   [REQ-PROFILE-201] 일별 예산과 월별 예산은 독립적으로 수정 가능해야 합니다.
    *   [REQ-PROFILE-203] 예산은 0 이상의 값만 허용되어야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-PROFILE-004
*   **Description**: 사용자는 음식 카테고리별 선호도(좋아요/보통/싫어요)를 설정하고 수정할 수 있습니다.
*   **Priority**: High
*   **Inputs**: 카테고리 ID, 선호도 가중치 (100: 좋아요, 0: 보통, -100: 싫어요)
*   **Processing**:
    *   선호도 조회: `preference` 테이블에서 사용자의 카테고리별 선호도를 조회합니다.
    *   선호도 설정/수정: 해당 카테고리의 `weight` 값을 업데이트합니다. 기존 레코드가 없으면 새로 생성합니다.
    *   선호도는 추천 알고리즘의 '안정성' 점수 계산에 40% 가중치로 반영됩니다.
*   **Outputs**: 선호도 설정 정보 또는 성공/실패 메시지
*   **Acceptance criteria**:
    *   [REQ-PROFILE-401] 모든 카테고리에 대해 선호도를 설정할 수 있어야 합니다.
    *   [REQ-PROFILE-403] 선호도 변경 시 즉시 추천 결과에 반영되어야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-PROFILE-005
*   **Description**: 온보딩 과정에서 선택한 개별 음식 선호도를 다시 조회하여 복구할 수 있어야 합니다.
*   **Priority**: Medium
*   **Inputs**: 사용자 액세스 토큰, (내부) 사용자 ID
*   **Processing**:
    *   `food_preference` 테이블에서 `member_id`와 `is_preferred=true` 조건으로 데이터를 조회합니다.
    *   해당 음식 ID 목록으로 `food`, `category` 테이블을 조인하여 이름, 이미지, 카테고리 정보를 구성합니다.
    *   `GET /api/v1/onboarding/food-preferences` API를 통해 최신 선호도를 반환하며, 최근 선택 순서로 정렬합니다.
*   **Outputs**: `totalCount`, `preferredFoodIds`, `preferredFoods[] {foodId, foodName, categoryName, imageUrl}`
*   **Acceptance criteria**:
    *   [REQ-ONBOARD-406] 저장된 선호도가 없으면 빈 배열과 `totalCount = 0`을 반환해야 합니다.
    *   인증되지 않은 사용자는 HTTP 401을 반환해야 합니다.
    *   최대 50개의 선호 음식까지 안정적으로 응답해야 합니다.

### 3.7. 배치 작업 (Batch Processing)

### 3.3. 음식 추천 시스템 (Food Recommendation System)

*   **Type**: Functional (F)
*   **ID**: F-RECO-001
*   **Description**: 시스템은 사용자 프로필(절약형, 모험형, 균형형)에 따라 4가지 속성(안정성, 탐험성, 예산 효율성, 접근성)의 가중치를 달리하여 개인화된 추천 점수를 계산합니다.
*   **Priority**: High
*   **Inputs**: 사용자 ID, 현재 위치(위도/경도), 필터/정렬 조건
*   **Processing**:
    1.  사용자의 추천 유형(`member.recommendation_type`)에 따라 속성별 가중치(w1~w4)를 가져옵니다.
    2.  필터 조건에 맞는 가게 목록을 조회합니다.
    3.  각 가게에 대해 4가지 속성 점수를 0~100점으로 정규화하여 계산합니다.
    4.  `최종 추천 점수 = (안정성*w1) + (탐험성*w2) + (예산효율성*w3) + (접근성*w4)` 공식을 적용합니다.
    5.  정렬 조건에 따라 최종 목록을 정렬하여 반환합니다.
*   **Outputs**: 추천 음식점 목록 (가게 정보, 거리, 추천 점수 등 포함)
*   **Acceptance criteria**:
    *   '절약형' 사용자는 '예산 효율성' 점수가 높은 가게를 우선 추천받아야 합니다.
    *   '모험형' 사용자는 '탐험성' 점수가 높은 가게를 우선 추천받아야 합니다.
    *   최종 추천 점수 계산 및 정렬 로직이 정확히 구현되어야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-RECO-003
*   **Description**: 사용자는 추천 결과에 다양한 필터와 정렬 옵션을 적용할 수 있습니다.
*   **Priority**: High
*   **Inputs**: 필터 조건(반경, 불호 음식, 영업 시간, 가게 유형), 정렬 기준
*   **Processing**:
    *   **위치 기반 필터**:
        *   사용자 위치 우선순위: ① 현재 GPS 위치 → ② 기본 주소(`is_primary=true`) → ③ 최근 등록 주소
        *   반경 필터: 0.5km(기본값), 1km, 2km 중 선택
        *   Haversine 공식을 사용하여 거리 계산
    *   **추가 필터**:
        *   불호 음식: 포함(기본값) / 제외
        *   영업 시간: 전체(기본값) / 영업 중만
        *   가게 유형: 전체(기본값) / 학식만 / 음식점만
    *   **정렬 옵션**:
        *   기본 순: 추천 점수 내림차순(알고리즘)
        *   가까운 순: 거리 오름차순
        *   리뷰 순: `review_count` 내림차순
        *   평균 가격 낮은 순: `average_price` 오름차순
        *   평균 가격 높은 순: `average_price` 내림차순
        *   즐겨찾기 많은 순: `favorite` 테이블 COUNT 내림차순
        *   관심도 높은 순: `view_count` 내림차순
        *   관심도 낮은 순: `view_count` 오름차순
*   **Outputs**: 필터 및 정렬이 적용된 음식점 목록
*   **Acceptance criteria**:
    *   위치 정보 미제공 시 기본 주소를 사용해야 합니다.
    *   불호 음식 제외 필터 적용 시, 해당 카테고리의 가게는 결과에서 완전히 제외되어야 합니다.
    *   임시 휴무(`store_temporary_closure`) 상태인 가게는 '영업 중만' 필터 시 제외되어야 합니다.

*   **Type**: Functional (F)
*   **ID**: F-RECO-004
*   **Description**: 사용자는 자신의 추천 유형(절약형, 모험형, 균형형)을 선택하고 변경할 수 있습니다.
*   **Priority**: Medium
*   **Inputs**: 추천 유형 선택 (SAVER, ADVENTURER, BALANCED)
*   **Processing**:
    *   각 유형별 가중치는 다음과 같이 적용됩니다:
        *   절약형(SAVER): 안정성 30%, 탐험성 15%, 예산효율성 50%, 접근성 5%
        *   모험형(ADVENTURER): 안정성 30%, 탐험성 50%, 예산효율성 10%, 접근성 10%
        *   균형형(BALANCED): 안정성 30%, 탐험성 25%, 예산효율성 30%, 접근성 15%
    *   선택한 유형은 `member.recommendation_type` 필드에 저장됩니다.
*   **Outputs**: 성공/실패 메시지
*   **Acceptance criteria**:
    *   유형 변경 시 즉시 추천 알고리즘에 반영되어야 합니다.
    *   온보딩 과정에서 최초 1회 선택이 필수입니다.

*   **Type**: Functional (F)
*   **ID**: F-RECO-002
*   **Description**: 4가지 핵심 속성(안정성, 탐험성, 예산 효율성, 접근성) 점수를 계산합니다.
*   **Priority**: High
*   **Inputs**: 가게 정보, 사용자 정보(선호도, 지출 이력, 예산), 현재 위치
*   **Processing**:
    *   **안정성 (Stability)**:
        *   선호 카테고리 점수 (40%): `preference.weight` 값 (-100, 0, 100) 사용.
        *   과거 지출 기록 점수 (40%): 시간 감쇠 함수 `w = exp(-0.01 * days_ago)`를 적용한 카테고리별 지출 비율.
        *   리뷰 신뢰도 점수 (20%): `store.review_count`를 정규화.
    *   **탐험성 (Exploration)**:
        *   카테고리 신선도 점수 (40%): `1 - norm(최근 30일 카테고리 방문 비율)`.
        *   음식점 신규성 점수 (30%): 마지막 방문일(`expenditure`), 가게 등록일(`store.created_at`) 기반.
        *   최근 관심도 점수 (30%): 최근 7일 조회수(`store_view_history`), 조회수 증가율 기반.
    *   **예산 효율성 (Budget Efficiency)**:
        *   가성비 점수 (60%): `log(1 + review_count) / avg_price` 정규화.
        *   예산 대비 적정성 점수 (40%): 사용자 설정 예산(`daily_budget`)과 가게 평균 가격(`store.average_price`) 비교.
    *   **접근성 (Accessibility)**:
        *   Haversine 공식을 이용해 사용자 현재 위치와 가게(`store.latitude`, `longitude`) 간의 거리를 계산하고, 거리가 가까울수록 높은 점수를 부여.
    *   **정규화 처리**:
        *   모든 점수는 Min-Max 정규화를 통해 0~100점으로 변환됩니다.
        *   정규화는 현재 필터링된 가게 목록 내에서 동적으로 수행됩니다(전체 가게 대상이 아님).
        *   편차가 작은 데이터(예: 지출 금액)는 로그 스케일 변환 후 정규화합니다: `log(1 + x)`
    *   **신규 사용자(Cold Start) 처리**:
        *   최근 6개월 지출 내역 < 3건인 경우:
            *   안정성 - 과거 지출: 가중치 0, 선호 카테고리만 활용
            *   탐험성 - 카테고리 신선도: 모든 카테고리 동일 점수(50) 부여
            *   탐험성 - 방문 기록: 신규 가게 점수(100) 적용
            *   예산 효율성: 온보딩 시 설정한 예산 활용
*   **Outputs**: 각 속성별 0~100점 사이의 점수
*   **Acceptance criteria**:
    *   모든 점수 계산 로직은 `recommendation_requirement_docs.md`에 명시된 공식을 따라야 합니다.
    *   Min-Max 정규화는 현재 필터링된 가게 목록 내에서 동적으로 수행되어야 합니다.
    *   신규 사용자(Cold Start)의 경우, `REQUIREMENTS_CLARIFICATION.md`에 정의된 기본값 전략을 따라야 합니다.
    *   시간 감쇠 함수의 λ(람다) 값은 0.01을 사용하며, 약 100일 후 영향력이 37%로 감소합니다.

### 3.4. 배치 작업 (Batch Processing)

*   **Type**: Functional (F)
*   **ID**: F-BATCH-001
*   **Description**: 시스템은 정기적인 배치 작업을 통해 데이터를 집계하고 관리합니다.
*   **Priority**: Medium
*   **Inputs**: -
*   **Processing**:
    *   **조회수 집계 (매일 자정)**: `store_view_history` 테이블의 일일 데이터를 집계하여 `store.view_count`를 업데이트합니다.
    *   **만료 토큰 정리 (매일 새벽 3시)**: `social_account` 테이블에서 만료된 토큰 정보를 삭제하거나 갱신합니다.
    *   **탈퇴 회원 데이터 삭제 (매일 새벽 4시)**: `member_authentication.deleted_at`이 1년 이상 경과된 회원의 개인정보를 영구 삭제합니다.
    *   **예산 초과 알림 (매일 저녁 9시)**: 당일 예산을 초과한 사용자에게 푸시 알림을 발송합니다.
    *   **비밀번호 만료 알림 (매일 오전 9시)**: `password_expires_at`이 7일 이내로 남은 사용자에게 알림을 발송합니다.
*   **Outputs**: -
*   **Acceptance criteria**:
    *   각 배치 작업은 지정된 시간에 정확히 실행되어야 합니다.
    *   배치 작업 실패 시, 관리자에게 알림을 보내고 재시도 로직이 있어야 합니다.

---

## 4. External interface requirements

### 4.1 User interfaces
본 문서는 백엔드 시스템을 다루므로, 사용자 인터페이스(UI)에 대한 상세 명세는 생략합니다. 단, 백엔드 API는 프론트엔드(모바일 앱)의 요구사항(`SRD.md`)에 명시된 화면 구성과 사용자 상호작용을 모두 지원해야 합니다.

### 4.2 Hardware interfaces
특별한 하드웨어 인터페이스 요구사항은 없습니다.

### 4.3 Software interfaces
*   **소셜 로그인 API (Google, Kakao)**
    *   **ID**: SI-OAUTH-001
    *   **Description**: OAuth 2.0 프로토콜을 사용하여 사용자 인증 및 프로필 정보(이메일, 이름 등)를 가져옵니다.
    *   **Details**: 각 플랫폼의 개발자 문서에 명시된 인증 절차(Authorization Code Grant Flow)를 따릅니다. Access Token과 Refresh Token을 받아 시스템 내 `social_account` 테이블에 저장하고 관리합니다. 토큰 만료 시 Refresh Token을 사용하여 자동으로 갱신합니다.

*   **네이버 지도 API**
    *   **ID**: SI-NAVER-MAP-001
    *   **Description**: 주소 검색, 좌표 변환(Geocoding), 좌표-주소 변환(Reverse Geocoding) 기능을 위해 사용됩니다.
    *   **Details**:
        *   **Geocoding**: 사용자가 입력한 주소 문자열을 위도/경도 좌표로 변환합니다.
        *   **Reverse Geocoding**: GPS로 얻은 좌표를 법정동/도로명 주소로 변환합니다.
        *   API 요청/응답 형식은 네이버 클라우드 플랫폼의 공식 문서를 따릅니다. 에러 발생 시(예: API 키 오류, 쿼터 초과) 적절한 예외 처리를 수행합니다.

*   **Spring AI - Google Gemini API**
    *   **ID**: SI-GEMINI-001
    *   **Description**: 사용자가 입력한 카드 결제 문자 메시지를 파싱하여 구조화된 데이터(가게명, 금액, 날짜)를 추출하는 데 사용됩니다.
    *   **Details**: Spring AI 라이브러리를 통해 Gemini 모델에 정해진 프롬프트를 전송하고, 반환된 JSON 형식의 결과를 파싱하여 사용합니다. 기존 레거시 프로젝트의 검증된 코드를 재사용합니다.

### 4.4 Communication interfaces
*   **프로토콜**: 클라이언트(모바일 앱)와 서버 간의 모든 통신은 `HTTPS`를 사용합니다.
*   **데이터 형식**: API 요청 및 응답의 본문(Body)은 `JSON` 형식을 사용합니다.
*   **API 엔드포인트**: RESTful 원칙에 따라 설계되며, 각 리소스(예: `/members`, `/stores`, `/expenditures`)에 대한 CRUD 연산을 지원합니다.

### 4.5 API Specification
주요 API 엔드포인트의 개요는 별도의 문서를 통해 관리됩니다. 상세한 요청/응답 스키마는 Spring REST Docs를 통해 자동 생성됩니다.

---

## 5. Non-functional requirements

### 5.1 Performance
*   **응답 시간**:
    *   음식 추천 API (`/api/recommendations`): 2초 이내
    *   복잡한 데이터 조회 쿼리(예: 통계, 지출 내역 필터링): 3초 이내
    *   일반적인 API(CRUD): 500ms 이내
*   **동시성**: 최소 100명의 동시 사용자 요청을 안정적으로 처리할 수 있어야 합니다.
*   **처리량**: 초당 최소 50개의 API 요청을 처리할 수 있어야 합니다.

### 5.2 Reliability
*   **가용성**: 시스템은 99.5% 이상의 가용성을 목표로 합니다.
*   **데이터 무결성**: 데이터베이스 트랜잭션 관리를 통해 데이터의 일관성과 무결성을 보장해야 합니다. 특히, 지출 등록 시 예산 업데이트는 원자적으로 처리되어야 합니다.
*   **장애 복구**: API 서버 장애 시, 로드 밸런서를 통해 다른 인스턴스로 트래픽이 자동 전환되어야 합니다. 배치 작업 실패 시, 재시도 메커니즘이 동작해야 합니다.

### 5.3 Security
*   **인증**: API 접근은 JWT 기반의 인증을 통과해야 합니다. 민감한 정보에 접근하는 API는 적절한 역할(Role) 기반 권한 검사를 수행해야 합니다.
*   **데이터 암호화**: 사용자의 비밀번호(`hashed_password`)는 복호화가 불가능한 해시 함수(SHA-256 이상)를 사용하여 저장해야 합니다.
*   **통신 보안**: 모든 클라이언트-서버 통신은 HTTPS를 통해 암호화되어야 합니다.
*   **API 키 관리**: 외부 API 키(네이버, 구글 등)는 소스코드에 직접 노출되지 않도록 환경 변수나 보안 관리 도구를 통해 안전하게 관리해야 합니다.

### 5.4 Maintainability
*   **코드 표준**: 프로젝트에 정의된 Java 및 Spring Boot 코딩 컨벤션을 준수해야 합니다.
*   **모듈화**: 시스템은 기능별(api, domain, storage 등)로 명확하게 분리된 멀티 모듈 구조를 유지해야 합니다.
*   **문서화**: Spring REST Docs를 통해 API 문서를 항상 최신 상태로 유지해야 합니다.
*   **테스트 커버리지**: 전체 코드 커버리지는 80% 이상을 목표로 합니다.

### 5.5 Portability
*   시스템은 Docker 컨테이너 환경에서 동작 가능해야 하며, `Dockerfile` 및 `docker-compose.yml`을 통해 환경 구성을 관리합니다. 이를 통해 로컬 개발 환경과 운영 서버 환경 간의 이식성을 보장합니다.

---

## 6. System architecture

### 6.1 Overview diagram
본 시스템은 멀티 모듈 기반의 계층형 아키텍처(Layered Architecture)를 따릅니다.

```
+-----------------------------------------------------------------+
|                        Presentation Layer                       |
| (smartmealtable-api, smartmealtable-admin)                      |
| : Controllers, DTOs, GlobalExceptionHandler, Interceptors     : |
+-----------------------------------------------------------------+
                              |
+-----------------------------------------------------------------+
|                       Application Layer                         |
| (smartmealtable-api, smartmealtable-recommendation)             |
| : Application Services (UseCases), External API Clients       : |
+-----------------------------------------------------------------+
                              |
+-----------------------------------------------------------------+
|                          Domain Layer                           |
| (smartmealtable-domain)                                         |
| : Domain Models (Entities), Repository Interfaces, Biz Logic  : |
+-----------------------------------------------------------------+
                              |
+-----------------------------------------------------------------+
|                       Persistence Layer                         |
| (smartmealtable-storage)                                        |
| : Repository Implementations (JPA), DB-Config, Redis-Config   : |
+-----------------------------------------------------------------+
```

### 6.2 Components and interactions
*   **smartmealtable-api**: 사용자에게 노출되는 주 API 서버. Controller, Application Service 등을 포함하며, 다른 모듈과 협력하여 비즈니스 로직을 처리합니다.
*   **smartmealtable-admin**: 관리자용 API 서버.
*   **smartmealtable-recommendation**: 음식 추천 로직을 전담하는 모듈.
*   **smartmealtable-domain**: 핵심 도메인 모델과 비즈니스 규칙, Repository 인터페이스를 포함합니다.
*   **smartmealtable-storage**: `domain`의 Repository를 구현하며, JPA Entity, DB/Redis 연동 등 데이터 영속성을 처리합니다.
*   **smartmealtable-batch**: 데이터 집계, 알림 등 주기적인 작업을 수행합니다.
*   **smartmealtable-core**: 모듈 전반에서 사용되는 공통 객체(ApiResponse, ErrorResult 등)를 정의합니다.
*   **smartmealtable-client**: 외부 API(OAuth, Naver 등) 연동을 위한 클라이언트 모듈.
*   **smartmealtable-support**: 로깅, 모니터링 등 비즈니스와 무관한 공통 유틸리티를 제공합니다.

### 6.3 Data flow
1.  **사용자 요청 (API)**: 클라이언트(모바일 앱)가 API 서버(`smartmealtable-api`)로 HTTP 요청을 보냅니다.
2.  **요청 처리**: `Controller`가 요청을 받아 `Application Service`에 위임합니다.
3.  **비즈니스 로직**: `Application Service`는 `Domain` 계층의 도메인 객체와 `Repository` 인터페이스를 사용하여 비즈니스 로직을 수행합니다.
4.  **데이터 영속성**: `Repository`의 실제 구현체인 `smartmealtable-storage` 모듈이 DB에 접근하여 데이터를 CRUD 합니다.
5.  **응답 반환**: 처리 결과를 DTO로 변환하여 `Controller`를 통해 클라이언트에 반환합니다.

---

## 7. Data requirements

### 7.1 Data models
데이터베이스의 상세 스키마는 `ddl.sql` 파일에 정의되어 있습니다. 주요 애그리거트(Aggregate)는 다음과 같습니다.

*   **회원 (Member) Aggregate**:
    *   Root: `Member`
    *   Entities: `MemberAuthentication`, `SocialAccount`, `AddressHistory`, `PolicyAgreement`, `Preference`, `Favorite`
*   **가게 (Store) Aggregate**:
    *   Root: `Store`
    *   Entities: `Food`, `Seller`, `StoreOpeningHour`, `StoreTemporaryClosure`
*   **지출 (Expenditure) Aggregate**:
    *   Root: `Expenditure`
    *   Entities: `ExpenditureItem`
*   **예산 (Budget) Aggregate**:
    *   Roots: `DailyBudget`, `MonthlyBudget`
    *   Entities: `MealBudget`
*   **추천 시스템 지원 테이블**:
    *   `StoreViewHistory`: 가게 조회 이력 추적 (조회수 집계, 최근 관심도 계산)
    *   `Category`: 음식 카테고리 마스터 데이터

ERD 다이어그램은 아래와 같이 주요 테이블 간의 관계를 나타냅니다. (상세 관계는 `ddl.sql` 참조)
```
[Member] 1--N [AddressHistory]
[Member] 1--1 [MemberAuthentication]
[MemberAuthentication] 1--N [SocialAccount]
[Member] 1--N [Expenditure]
[Expenditure] 1--N [ExpenditureItem]
[Store] 1--N [Food]
[Store] 1--N [StoreViewHistory]
[Store] 1--N [StoreTemporaryClosure]
[Member] N--M [Store] (through Favorite, StoreViewHistory)
[Member] 1--N [Preference]
[Category] 1--N [Preference]
```

#### 7.1.1 연관관계 및 FK 정책
본 시스템은 다음과 같은 데이터베이스 설계 원칙을 따릅니다:

*   **물리 FK 제약조건 미사용**: 데이터베이스 레벨의 외래키(Foreign Key) 제약조건을 사용하지 않습니다.
*   **논리 FK만 사용**: 코드와 문서상으로만 참조 관계를 명시하며, 실제 DB에는 제약조건을 걸지 않습니다.
*   **JPA 연관관계 매핑 최소화**: 
    *   같은 Aggregate에 속하는 객체(예: `Order`와 `OrderItem`, `Store`와 `Food`)를 제외하고는 JPA 연관관계 매핑(`@OneToMany`, `@ManyToOne` 등)을 사용하지 않습니다.
    *   대신 FK 값을 일반 필드로 선언하여 사용합니다. (예: `Long memberId`, `Long storeId`)
*   **장점**: 
    *   배포 및 마이그레이션 시 테이블 간 의존성으로 인한 제약 감소
    *   대용량 데이터 처리 시 성능 향상
    *   마이크로서비스 아키텍처로의 확장 용이성

### 7.2 Data storage
*   **Primary Database**: `MySQL`을 사용하여 모든 정형 데이터를 저장합니다.
*   **Caching**: `Redis`를 사용하여 자주 조회되지만 변경이 적은 데이터(예: 가게 상세 정보, 카테고리 목록)나 추천 결과 등을 캐싱하여 성능을 향상시킵니다.
*   **Indexing**: `ddl.sql`에 명시된 바와 같이, 조회 성능 향상을 위해 주요 조회 조건 컬럼(FK, 정렬/필터 조건 등)에 인덱스를 생성합니다.
    *   예: `store_view_history.idx_store_viewed_at (store_id, viewed_at)`
    *   예: `groups.idx_name (name)` - 소속 검색 성능 향상
    *   예: `store.idx_location (latitude, longitude)` - 위치 기반 검색 최적화
    *   예: `expenditure.idx_member_date (member_id, expenditure_date)` - 지출 내역 조회 최적화

### 7.3 Data validation
*   **입력 유효성 검증**: API의 DTO 레벨에서 `@Valid` 어노테이션과 Jakarta Bean Validation을 사용하여 입력값의 형식, 길이, 필수 여부 등을 검증합니다.
*   **데이터 정합성**: 데이터베이스 레벨에서 `NOT NULL`, `UNIQUE` 제약조건을 활용하여 데이터의 정합성을 유지합니다.
*   **비즈니스 규칙**: 도메인 모델 내에서 비즈니스 규칙(예: 예산은 0 이상이어야 함)을 검증하는 로직을 포함합니다.

---

## 8. Other requirements

### 8.1 Legal and regulatory
*   **개인정보 보호**: 사용자의 개인정보(이름, 이메일, 주소 등)는 관련 법규(개인정보보호법)에 따라 안전하게 처리 및 보관되어야 합니다.
*   **데이터 보관**: 회원 탈퇴 시, 법적 보관 의무가 있는 최소한의 정보(예: 상거래 관련 기록)를 제외한 개인정보는 1년 후 영구 삭제합니다.

### 8.2 Localization
*   현재 시스템은 한국어(ko-KR) 환경을 기준으로 개발됩니다. 시간대는 `Asia/Seoul`을 사용합니다.

### 8.3 Accessibility
*   백엔드 시스템이므로 직접적인 UI 접근성 요구사항은 없으나, API 설계 시 클라이언트가 접근성 기능을 구현하는 데 필요한 모든 정보를 제공해야 합니다.

---

## 9. Appendices

### 9.1 Supporting diagrams
(필요 시 시퀀스 다이어그램, 상태 다이어그램 등을 추가할 수 있습니다.)

### 9.2 Change history
| 버전 | 날짜 | 변경 내용 | 작성자 |
|---|---|---|---|
| 1.0 | 2025-10-08 | 초안 작성 | GitHub Copilot |
