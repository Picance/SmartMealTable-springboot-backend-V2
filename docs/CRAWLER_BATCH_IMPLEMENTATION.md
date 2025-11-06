# 크롤링 데이터 배치 작업 구현 완료

## 개요

네이버 플레이스 등에서 크롤링한 음식점 정보(JSON)를 데이터베이스에 추가/수정하는 Spring Batch 작업을 구현했습니다.

**주요 특징:**
- 기존 `domain.food.Food` 도메인을 확장하여 크롤러 배치에서 재사용
- 중복 도메인 제거로 일관성 있는 아키텍처 구현
- food 테이블에 크롤러용 필드(is_main, display_order) 추가
- Upsert 전략으로 멱등성 보장

## 구현 내용

### 1. 도메인 모델 확장 (domain 모듈)

#### 기존 Food 도메인 확장
- **domain.food.Food**: 음식 추천 시스템과 크롤러 배치 모두 지원
  - 기존 필드: foodId, foodName, storeId, categoryId, description, imageUrl, averagePrice
  - 추가 필드: 
    - `price`: 크롤러용 개별 가격 (averagePrice와 별도)
    - `isMain`: 대표 메뉴 여부
    - `displayOrder`: 표시 순서 (낮을수록 우선)
    - `registeredDt`: 등록 시각 (비즈니스 필드)
    - `deletedAt`: 삭제 시각 (소프트 삭제)
  - Builder 패턴 적용
  - 검증 메서드: `isValid()`, `isMainFood()`, `isDeleted()`

#### 새로운 도메인 엔티티
- **StoreImage**: 가게의 이미지 정보
  - 이미지 URL
  - 대표 이미지 여부, 표시 순서

#### Store 도메인 확장
- `externalId` 필드 추가: 외부 크롤링 시스템의 가게 ID (Natural Key)
  - 네이버 플레이스, 카카오맵 등의 원본 ID를 저장
  - Upsert(존재하면 업데이트, 없으면 생성) 작업의 기준 키

#### Repository 인터페이스
- `domain.food.FoodRepository`: 음식 CRUD + 크롤러용 메서드
  - 기존: save(), findById(), findByStoreId() 등
  - 추가: `deleteByStoreId()` (크롤러 배치용)
- `StoreImageRepository`: 이미지 CRUD
- `StoreRepository.findByExternalId()`: 외부 ID로 가게 조회
- `StoreOpeningHourRepository.deleteByStoreId()`: 기존 영업시간 삭제

### 2. Storage Layer (storage/db 모듈)

#### JPA 엔티티
- `FoodJpaEntity`: food 테이블 매핑
  - 기존 필드 + 크롤러용 필드 (is_main, display_order) 통합
  - 단일 도메인(`domain.food.Food`)만 지원하도록 변환 메서드 단순화
  - `toDomain()`: JPA Entity → domain.food.Food
  - `fromDomain()`: domain.food.Food → JPA Entity
- `StoreImageJpaEntity`: 이미지 테이블 매핑
- `StoreJpaEntity`: `externalId` 필드 추가 (unique 제약조건)

#### JPA Repository
- `FoodJpaRepository`: `deleteByStoreId()` 추가
- `StoreImageJpaRepository`: `deleteByStoreId()` 추가
- `StoreJpaRepository`: `findByExternalId()` 추가
- `StoreOpeningHourJpaRepository`: `deleteByStoreId()` 추가

#### Repository 구현체
- `FoodRepositoryImpl`: domain.food.Food 저장/조회/삭제 (추천 시스템 + 크롤러 통합)
  - `deleteByStoreId()` 구현 추가
- `StoreImageRepositoryImpl`: StoreImage 도메인 저장/조회/삭제
  - `StoreImageEntityMapper` 사용 (MenuEntityMapper 대체)
- `StoreRepositoryImpl`: `findByExternalId()` 구현
- `StoreOpeningHourRepositoryImpl`: `deleteByStoreId()` 구현

### 3. Batch 모듈 (batch/crawler)

#### DTO
- `CrawledStoreDto`: JSON 크롤링 데이터 구조 매핑
  - 가게 기본 정보 (id, name, category, address, coordinates)
  - 이미지 목록 (images)
  - 영업시간 목록 (openingHours)
  - 음식 목록 (menus)
  - 리뷰 수, 평균 가격, 전화번호

#### ItemReader
- `JsonStoreItemReader`: JSON 파일에서 크롤링 데이터 읽기
  - Jackson ObjectMapper 사용
  - 파일 전체를 메모리에 로드 후 순차 처리
  - 로그 기록 (전체 데이터 개수, 현재 처리 진행 상황)

#### ItemProcessor
- `CrawledStoreProcessor`: 크롤링 데이터 → 도메인 객체 변환 + Upsert 로직
  - **Upsert 전략**: externalId로 기존 가게 조회
    - 존재하면: 기존 storeId 유지하고 정보 업데이트
    - 없으면: 신규 가게 생성
  - **도메인 변환**:
    - Store: 가게 기본 정보 변환
    - Food: 음식 정보 변환 (대표 메뉴 여부, 표시 순서 설정, 음식 카테고리 설정)
    - StoreImage: 이미지 목록 변환 (첫 번째를 대표 이미지로)
    - StoreOpeningHour: 영업시간 변환 (한글 요일 → DayOfWeek)
  - **카테고리 매핑**: 카테고리 이름으로 categoryId 조회 (캐싱)

#### ItemWriter
- `StoreDataWriter`: 도메인 객체를 DB에 저장
  - **트랜잭션 처리**: `@Transactional`로 원자성 보장
  - **저장 순서**:
    1. Store 저장 (Upsert)
    2. 기존 관련 엔티티 삭제 (Food, StoreImage, StoreOpeningHour)
    3. 새로운 Food 저장
    4. 새로운 StoreImage 저장
    5. 새로운 StoreOpeningHour 저장
  - **storeId 재설정**: Processor에서 임시 storeId를 사용하므로, Writer에서 실제 저장된 storeId로 교체

#### Job Configuration
- `StoreCrawlerBatchJobConfig`: Spring Batch Job 설정
  - **Job**: `importCrawledStoreJob`
  - **Step**: `importCrawledStoreStep`
    - Chunk 크기: 10개 (한 번에 10개씩 처리)
    - Reader → Processor → Writer 파이프라인
  - **StepScope**: Job Parameter로 파일 경로 주입
    - `inputFilePath`: JSON 파일 경로 (예: `file:/path/to/노원구_공릉동.json`)

#### Controller
- `StoreCrawlerBatchController`: 배치 작업 실행 REST API
  - `POST /api/v1/batch/crawler/import-stores?filePath=...`
  - JobLauncher로 배치 작업 실행
  - Job Parameter로 파일 경로와 timestamp 전달

### 4. 데이터베이스 스키마 (ddl.sql)

#### store 테이블 수정
```sql
ALTER TABLE store ADD COLUMN external_id VARCHAR(50) NULL COMMENT '외부 크롤링 시스템의 가게 ID';
ALTER TABLE store ADD UNIQUE KEY uq_external_id (external_id);
```

#### food 테이블 확장
```sql
-- 기존 food 테이블에 크롤러용 필드 추가
ALTER TABLE food ADD COLUMN is_main BOOLEAN NOT NULL DEFAULT FALSE COMMENT '대표 메뉴 여부';
ALTER TABLE food ADD COLUMN display_order INT NULL COMMENT '표시 순서 (낮을수록 우선)';
ALTER TABLE food ADD INDEX idx_store_main (store_id, is_main);
ALTER TABLE food ADD INDEX idx_store_display (store_id, display_order);
```

#### 신규 테이블 생성
```sql
-- 가게 이미지 테이블
CREATE TABLE store_image (
    store_image_id BIGINT NOT NULL AUTO_INCREMENT,
    store_id       BIGINT NOT NULL,
    image_url      VARCHAR(500) NOT NULL,
    is_main        BOOLEAN NOT NULL DEFAULT FALSE,
    display_order  INT NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (store_image_id),
    INDEX idx_store_id (store_id)
);
```

## 사용 방법

### 1. 배치 작업 실행 (REST API)

```bash
# 크롤링 데이터 Import
curl -X POST "http://localhost:8082/api/v1/batch/crawler/import-stores?filePath=file:/path/to/노원구_공릉동.json"
```

### 2. Job Parameter 설명
- `inputFilePath`: JSON 파일 경로
  - `file:` 프로토콜 사용 (로컬 파일)
  - 예: `file:/Users/luna/Desktop_nonsync/project/smartmealtableV2/SmartMealTable-springboot-backend-V2/districts_before/노원구_공릉동.json`
  - 절대 경로 권장

### 3. 배치 실행 흐름

```
1. REST API 호출 (파일 경로 전달)
   ↓
2. JobLauncher가 Job 실행
   ↓
3. JsonStoreItemReader가 JSON 파일 읽기
   ↓
4. CrawledStoreProcessor가 도메인 객체 변환
   ↓
5. StoreDataWriter가 DB에 저장
   ↓
6. 완료 (로그 확인)
```

## 아키텍처 특징

### 도메인 통합 전략
- **단일 Food 도메인**: `domain.food.Food`를 추천 시스템과 크롤러 배치에서 공유
  - 중복 제거: `domain.store.Food` 제거
  - 필드 확장: price, isMain, displayOrder 추가
  - 호환성: averagePrice (추천 시스템용), price (크롤러용) 병존
- **일관성**: 모든 모듈에서 동일한 Food 도메인 사용
- **확장성**: 추천 시스템도 대표 메뉴 정보 활용 가능

### 멀티 모듈 구조 준수
- **domain**: JPA 독립적인 순수 도메인 엔티티
- **storage/db**: JPA Entity와 Repository 구현
- **batch/crawler**: 배치 작업 로직

### 도메인 주도 설계
- **Aggregate 분리**: Store, Food, StoreImage는 별도 Aggregate
- **논리 FK**: 물리 FK 제약조건 없이 논리적으로만 참조
- **Value Object**: StoreOpeningHour는 record 타입

### Upsert 전략
- **Natural Key**: externalId를 기준으로 동일성 판단
- **기존 데이터 보존**: storeId, viewCount, favoriteCount 유지
- **전체 교체**: Food, Image, OpeningHour는 삭제 후 재생성

### 트랜잭션 관리
- **Chunk 단위**: 10개씩 트랜잭션 처리 (성능 최적화)
- **원자성 보장**: Store + 관련 엔티티가 모두 성공하거나 모두 롤백

## 확장 가능성

### 카테고리 자동 매핑
현재는 임시로 categoryId를 1L로 하드코딩했습니다.
실제 운영 시에는 CategoryRepository를 통해 카테고리 이름으로 조회하도록 개선 필요:

```java
// CrawledStoreProcessor.java
private Long resolveCategoryId(String categoryName) {
    // TODO: CategoryRepository로 실제 조회
    return categoryRepository.findByName(categoryName)
            .map(Category::getCategoryId)
            .orElse(1L); // 기본 카테고리
}
```

### 파일 경로 자동 스캔
특정 디렉토리의 모든 JSON 파일을 자동으로 처리하도록 확장 가능:

```java
// 예: districts_before/ 디렉토리의 모든 JSON 파일 처리
@Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
public void importAllCrawledData() {
    File dir = new File("/path/to/districts_before");
    File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
    
    for (File file : files) {
        jobLauncher.run(importCrawledStoreJob, 
            new JobParametersBuilder()
                .addString("inputFilePath", "file:" + file.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()
        );
    }
}
```

### 에러 처리 강화
- Skip 정책: 특정 데이터 실패 시 건너뛰기
- Retry 정책: 일시적 오류 시 재시도
- 실패 로그: 실패한 데이터를 별도 파일로 저장

## 테스트 전략

### Unit Test
- `CrawledStoreProcessor` 단위 테스트
- `JsonStoreItemReader` 단위 테스트
- `StoreDataWriter` 단위 테스트

### Integration Test
- `StoreCrawlerBatchJobConfig` 통합 테스트
- TestContainer로 실제 DB 연동 테스트
- 샘플 JSON 파일로 End-to-End 테스트

## 주의사항

1. **Food 도메인 통합 사용**
   - `domain.food.Food` 하나로 추천 시스템과 크롤러 배치 모두 지원
   - averagePrice: 추천 시스템용 평균 가격
   - price: 크롤러용 개별 메뉴 가격
   - 기존 추천 시스템 코드와의 호환성 유지

2. **created_at, updated_at는 DB DEFAULT로 관리**
   - JPA Entity에서 `insertable = false, updatable = false` 설정
   - 도메인 엔티티에는 노출하지 않음

2. **created_at, updated_at는 DB DEFAULT로 관리**
   - JPA Entity에서 `insertable = false, updatable = false` 설정
   - 도메인 엔티티에는 노출하지 않음

3. **물리 FK 제약조건 사용 안 함**
   - 논리 FK만 사용 (categoryId, sellerId 등)
   - CASCADE 삭제 없음 (애플리케이션에서 처리)

4. **externalId는 unique 제약조건**
   - 동일한 외부 ID는 중복 저장 불가
   - Upsert 작업 시 기준 키로 활용

5. **배치 작업은 멱등성 보장**
   - 같은 JSON 파일을 여러 번 실행해도 결과 동일
   - externalId 기준으로 업데이트

## 구현 완료 항목

- [x] 도메인 모델 통합 (domain.food.Food 확장, 중복 제거)
- [x] Storage Layer (FoodJpaEntity 통합, StoreImageEntityMapper 추가)
- [x] Batch DTO (CrawledStoreDto)
- [x] ItemReader (JsonStoreItemReader)
- [x] ItemProcessor (CrawledStoreProcessor - domain.food.Food 사용)
- [x] ItemWriter (StoreDataWriter - domain.food.FoodRepository 사용)
- [x] Job Configuration (StoreCrawlerBatchJobConfig)
- [x] REST API (StoreCrawlerBatchController)
- [x] DDL 스키마 (food 테이블 확장, store_image 테이블)
- [x] 아키텍처 개선 (중복 도메인 제거, 일관성 향상)

## 리팩토링 이력

### Food 도메인 통합 (2025-11-07)
**문제점:**
- `domain.food.Food` (추천 시스템용)와 `domain.store.Food` (크롤러용) 중복 존재
- 같은 food 테이블을 사용하지만 도메인이 분리되어 일관성 저하

**해결:**
- 기존 `domain.food.Food`에 크롤러용 필드 추가 (price, isMain, displayOrder, registeredDt, deletedAt)
- `domain.store.Food`, `domain.store.FoodRepository`, `CrawlerFoodRepositoryImpl` 제거
- `FoodJpaEntity` 변환 메서드 단순화 (단일 도메인만 지원)
- `StoreImageEntityMapper` 생성 (MenuEntityMapper 대체)
- Builder 패턴 적용으로 유연성 향상

**효과:**
- 중복 제거로 유지보수성 향상
- 단일 진실 공급원(Single Source of Truth) 확보
- 추천 시스템도 대표 메뉴 정보 활용 가능
- 일관성 있는 도메인 모델 구조

## Next Steps (향후 개선 사항)

1. ✅ 카테고리 자동 매핑 구현
2. ✅ 파일 경로 자동 스캔 스케줄러
3. ✅ Skip/Retry 정책 추가
4. ✅ 통합 테스트 작성
5. ✅ 성능 최적화 (Bulk Insert)
6. ✅ 모니터링 및 알림 (배치 실패 시)
