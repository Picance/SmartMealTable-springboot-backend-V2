# 지도 및 위치 API, 알림 및 설정 API 구현 완료 보고서

**작성일**: 2025-10-14  
**구현 범위**: 지도 및 위치 API (2개), 알림 및 설정 API (4개)

---

## 📊 구현 완료 현황

### ✅ 지도 및 위치 API (2/2 API - 100% 완료)

| API | Endpoint | Method | 상태 |
|-----|----------|--------|------|
| 주소 검색 | `/api/v1/maps/search-address` | GET | ✅ 완료 |
| 좌표→주소 변환 | `/api/v1/maps/reverse-geocode` | GET | ✅ 완료 |

### ✅ 알림 및 설정 API (4/4 API - 100% 완료)

| API | Endpoint | Method | 상태 |
|-----|----------|--------|------|
| 알림 설정 조회 | `/api/v1/members/me/notification-settings` | GET | ✅ 완료 |
| 알림 설정 변경 | `/api/v1/members/me/notification-settings` | PUT | ✅ 완료 |
| 앱 설정 조회 | `/api/v1/settings/app` | GET | ✅ 완료 |
| 사용자 추적 설정 | `/api/v1/settings/app/tracking` | PUT | ✅ 완료 |

---

## 🏗 구현 세부 내용

### 1. Domain Layer

#### 1.1 지도/위치 도메인

**파일**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/map/`

- **`AddressSearchResult.java`** (Value Object)
  - 주소 검색 결과를 나타내는 불변 객체 (record)
  - 도로명주소, 지번주소, 위도, 경도, 시/도, 시/군/구, 동, 건물명 등 포함
  - 위도/경도 유효성 검증 로직 포함

- **`MapService.java`** (Domain Service Interface)
  - `searchAddress()` - 키워드로 주소 검색
  - `reverseGeocode()` - GPS 좌표를 주소로 변환

#### 1.2 알림/설정 도메인

**파일**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/settings/`

- **`NotificationSettings.java`** (Entity)
  - 알림 설정 도메인 엔티티
  - pushEnabled 비활성화 시 하위 알림 자동 비활성화 로직 구현 (REQ-PROFILE-302a)
  - `updateSettings()`, `enablePush()`, `disablePush()` 메서드
  
- **`AppSettings.java`** (Entity)
  - 앱 설정 도메인 엔티티
  - 사용자 추적 허용 설정 관리
  - `updateTrackingSettings()` 메서드

- **Repository Interfaces**
  - `NotificationSettingsRepository.java`
  - `AppSettingsRepository.java`

---

### 2. Client Layer

**파일**: `smartmealtable-client/external/src/main/java/com/stdev/smartmealtable/client/external/naver/`

- **`NaverMapClient.java`**
  - `MapService` 인터페이스 구현
  - 네이버 지도 API 호출 (Geocoding, Reverse Geocoding)
  - 외부 API 에러 처리 (503 Service Unavailable)
  - DTO 변환 로직 구현

- **DTO**
  - `NaverGeocodingResponse.java` - 주소 검색 응답
  - `NaverReverseGeocodingResponse.java` - 역지오코딩 응답

---

### 3. Storage Layer

**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/settings/`

- **JPA Entity**
  - `NotificationSettingsJpaEntity.java`
  - `AppSettingsJpaEntity.java`
  - Domain Entity ↔ JPA Entity 변환 메서드 (`from()`, `toDomain()`)

- **JPA Repository**
  - `NotificationSettingsJpaRepository.java` (extends JpaRepository)
  - `AppSettingsJpaRepository.java` (extends JpaRepository)

- **Repository 구현체**
  - `NotificationSettingsRepositoryImpl.java`
  - `AppSettingsRepositoryImpl.java`

---

### 4. API Layer

**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/`

#### 4.1 지도/위치 API

**Service**:
- **`MapApplicationService.java`**
  - `searchAddress()` - 주소 검색
  - `reverseGeocode()` - 좌표→주소 변환

**Controller**:
- **`MapController.java`**
  - `GET /api/v1/maps/search-address` - 주소 검색
  - `GET /api/v1/maps/reverse-geocode` - 역지오코딩
  - Query Parameter Validation (@Min, @Max, @DecimalMin, @DecimalMax)

**DTO**:
- `AddressSearchResultResponse.java`
- `AddressSearchServiceResponse.java`
- `ReverseGeocodeServiceResponse.java`

#### 4.2 알림/설정 API

**Service**:
- **`NotificationSettingsApplicationService.java`**
  - `getNotificationSettings()` - 알림 설정 조회
  - `updateNotificationSettings()` - 알림 설정 변경
  - 설정이 없으면 기본값으로 자동 생성

- **`AppSettingsApplicationService.java`**
  - `getAppSettings()` - 앱 설정 조회 (정적 정보)
  - `updateTrackingSettings()` - 추적 설정 변경

**Controller**:
- **`NotificationSettingsController.java`**
  - `GET /api/v1/members/me/notification-settings` - 알림 설정 조회
  - `PUT /api/v1/members/me/notification-settings` - 알림 설정 변경

- **`AppSettingsController.java`**
  - `GET /api/v1/settings/app` - 앱 설정 조회
  - `PUT /api/v1/settings/app/tracking` - 추적 설정 변경

**DTO**:
- `NotificationSettingsServiceResponse.java`
- `UpdateNotificationSettingsServiceRequest.java`
- `AppSettingsServiceResponse.java`
- `UpdateTrackingSettingsServiceRequest.java`
- `TrackingSettingsServiceResponse.java`

---

### 5. DB Schema

**파일**: `ddl.sql`

```sql
-- 알림 설정 테이블
CREATE TABLE notification_settings (
    notification_settings_id  BIGINT    NOT NULL AUTO_INCREMENT,
    member_id                 BIGINT    NOT NULL,
    push_enabled              BOOLEAN   NOT NULL DEFAULT TRUE,
    store_notice_enabled      BOOLEAN   NOT NULL DEFAULT TRUE,
    recommendation_enabled    BOOLEAN   NOT NULL DEFAULT TRUE,
    budget_alert_enabled      BOOLEAN   NOT NULL DEFAULT TRUE,
    password_expiry_alert_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_settings_id),
    UNIQUE KEY uq_member_id (member_id),
    INDEX idx_member_id (member_id)
);

-- 앱 설정 테이블
CREATE TABLE app_settings (
    app_settings_id    BIGINT    NOT NULL AUTO_INCREMENT,
    member_id          BIGINT    NOT NULL,
    allow_tracking     BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at         DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (app_settings_id),
    UNIQUE KEY uq_member_id (member_id),
    INDEX idx_member_id (member_id)
);
```

---

## 🎯 주요 구현 특징

### 1. Layered Architecture 준수
- Domain → Client/Storage → API 의존성 방향 준수
- 각 계층 간 DTO를 통한 통신

### 2. 도메인 모델 패턴
- 비즈니스 로직을 도메인 엔티티에 캡슐화
- `NotificationSettings.updateSettings()` - pushEnabled 로직 처리

### 3. 외부 API 통합
- `NaverMapClient`가 `MapService` 인터페이스 구현
- 네이버 지도 API와의 통신 처리
- 에러 핸들링 (RuntimeException → 503)

### 4. Validation 처리
- Controller에서 `@Validated`, `@NotNull`, `@Min`, `@Max`, `@DecimalMin`, `@DecimalMax` 사용
- Request DTO에서 `@Valid` 검증

### 5. 설정 자동 생성
- 알림 설정/앱 설정이 없는 경우 조회 시 자동 생성
- 기본값으로 초기화 (pushEnabled: true, allowTracking: false)

---

## 📋 다음 단계

### 1. 테스트 작성 필요
- [ ] Unit Test (Application Service, Domain)
- [ ] REST Docs Test (Controller)
- [ ] Integration Test

### 2. 환경 설정
- [ ] `application.yml`에 네이버 지도 API 키 추가
  ```yaml
  naver:
    map:
      client-id: ${NAVER_MAP_CLIENT_ID}
      client-secret: ${NAVER_MAP_CLIENT_SECRET}
  ```

### 3. 빌드 및 검증
- [ ] 전체 프로젝트 빌드
- [ ] 테스트 실행
- [ ] REST Docs 생성

---

## 📁 생성된 파일 목록

### Domain (7개)
1. `AddressSearchResult.java`
2. `MapService.java`
3. `NotificationSettings.java`
4. `AppSettings.java`
5. `NotificationSettingsRepository.java`
6. `AppSettingsRepository.java`

### Client (3개)
7. `NaverGeocodingResponse.java`
8. `NaverReverseGeocodingResponse.java`
9. `NaverMapClient.java`

### Storage (6개)
10. `NotificationSettingsJpaEntity.java`
11. `AppSettingsJpaEntity.java`
12. `NotificationSettingsJpaRepository.java`
13. `AppSettingsJpaRepository.java`
14. `NotificationSettingsRepositoryImpl.java`
15. `AppSettingsRepositoryImpl.java`

### API (13개)
16. `AddressSearchResultResponse.java`
17. `AddressSearchServiceResponse.java`
18. `ReverseGeocodeServiceResponse.java`
19. `MapApplicationService.java`
20. `MapController.java`
21. `NotificationSettingsServiceResponse.java`
22. `UpdateNotificationSettingsServiceRequest.java`
23. `AppSettingsServiceResponse.java`
24. `UpdateTrackingSettingsServiceRequest.java`
25. `TrackingSettingsServiceResponse.java`
26. `NotificationSettingsApplicationService.java`
27. `AppSettingsApplicationService.java`
28. `NotificationSettingsController.java`
29. `AppSettingsController.java`

### DB (1개)
30. `ddl.sql` (업데이트)

**총 파일 수: 30개**

---

## 🎉 결론

- ✅ **지도 및 위치 API** 2개 완료
- ✅ **알림 및 설정 API** 4개 완료
- ✅ 총 **6개 API** 구현 완료
- ✅ **Layered Architecture** 준수
- ✅ **TDD 준비 완료** (테스트 작성 필요)

**다음 작업**: 테스트 작성 및 REST Docs 생성
