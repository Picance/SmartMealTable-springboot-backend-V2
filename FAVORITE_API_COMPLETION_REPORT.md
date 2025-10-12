# 즐겨찾기 API 구현 완료 보고서

## 📋 작업 개요
사용자가 음식점을 즐겨찾기에 추가/조회/순서변경/삭제할 수 있는 Favorite API를 완전히 구현하고, Spring REST Docs를 통한 API 문서화를 완료했습니다.

**작업 일시**: 2025-10-12  
**작업 범위**: Domain → Storage → Application Service → Controller → REST Docs Test

---

## ✅ 구현 완료 항목

### 1. Domain Layer (도메인 계층)

#### 1.1 Favorite 도메인 엔티티
**파일**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/favorite/Favorite.java`

**주요 기능**:
- 즐겨찾기 생성 (정적 팩토리 메서드 `create()`)
- 우선순위 변경 (`changePriority()`)
- 불변성 보장 (Setter 없음)

**필드**:
```java
- favoriteId: Long         // 즐겨찾기 고유 ID
- memberId: Long           // 회원 ID (외부 참조)
- storeId: Long            // 가게 ID (외부 참조)
- priority: Long           // 표시 순서
- favoritedAt: LocalDateTime  // 즐겨찾기 등록 시각
```

#### 1.2 FavoriteRepository 인터페이스
**파일**: `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/favorite/FavoriteRepository.java`

**메서드**:
- `save(Favorite)`: 즐겨찾기 저장
- `findById(Long)`: ID로 조회
- `findByMemberIdOrderByPriorityAsc(Long)`: 회원의 즐겨찾기 목록 (우선순위 오름차순)
- `findByMemberIdAndStoreId(Long, Long)`: 특정 회원의 특정 가게 즐겨찾기 조회
- `countByMemberId(Long)`: 회원의 즐겨찾기 개수
- `findMaxPriorityByMemberId(Long)`: 회원의 최대 우선순위 값
- `delete(Favorite)`: 즐겨찾기 삭제
- `saveAll(List<Favorite>)`: 일괄 저장
- `existsByMemberIdAndStoreId(Long, Long)`: 존재 여부 확인

---

### 2. Storage Layer (영속성 계층)

#### 2.1 FavoriteEntity (JPA 엔티티)
**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/favorite/FavoriteEntity.java`

**특징**:
- `@Table(uniqueConstraints)`: store_id + member_id 복합 유니크 제약조건
- `@PrePersist`, `@PreUpdate`: created_at, updated_at 자동 관리
- 도메인 모델과 분리된 JPA 전용 엔티티

**테이블**: `favorite`

#### 2.2 FavoriteJpaRepository
**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/favorite/FavoriteJpaRepository.java`

**쿼리 메서드**:
- `findByMemberIdOrderByPriorityAsc(Long)`
- `findByMemberIdAndStoreId(Long, Long)`
- `countByMemberId(Long)`
- `existsByMemberIdAndStoreId(Long, Long)`
- `@Query`: findMaxPriorityByMemberId() - 최대 우선순위 조회

#### 2.3 FavoriteMapper
**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/favorite/FavoriteMapper.java`

- `toDomain(FavoriteEntity)`: JPA 엔티티 → 도메인 변환
- `toEntity(Favorite)`: 도메인 → JPA 엔티티 변환

#### 2.4 FavoriteRepositoryImpl
**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/favorite/FavoriteRepositoryImpl.java`

- FavoriteRepository 구현체
- FavoriteJpaRepository와 FavoriteMapper를 사용하여 영속성 처리

---

### 3. Application Service Layer (애플리케이션 서비스 계층)

#### 3.1 FavoriteService
**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/favorite/service/FavoriteService.java`

**메서드**:

1. **addFavorite(Long memberId, Long storeId)**
   - 즐겨찾기 추가
   - 중복 체크 (이미 존재하면 E409 에러)
   - 가게 존재 여부 확인 (없으면 E404 에러)
   - 자동 우선순위 할당 (maxPriority + 1)

2. **getFavorites(Long memberId)**
   - 즐겨찾기 목록 조회
   - Store, Category 정보 JOIN
   - 우선순위 오름차순 정렬

3. **reorderFavorites(Long memberId, List<FavoriteOrderDto>)**
   - 즐겨찾기 순서 변경
   - 권한 검증 (다른 사용자의 즐겨찾기는 변경 불가, E403)
   - 일괄 업데이트

4. **deleteFavorite(Long memberId, Long favoriteId)**
   - 즐겨찾기 삭제
   - 존재 여부 확인 (없으면 E404 에러)
   - 권한 검증 (다른 사용자의 즐겨찾기는 삭제 불가, E403)

---

### 4. Controller Layer (컨트롤러 계층)

#### 4.1 FavoriteController
**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/favorite/controller/FavoriteController.java`

**엔드포인트**:

| Method | URI | 설명 |
|--------|-----|------|
| POST | /api/v1/favorites | 즐겨찾기 추가 |
| GET | /api/v1/favorites | 즐겨찾기 목록 조회 |
| PUT | /api/v1/favorites/reorder | 즐겨찾기 순서 변경 |
| DELETE | /api/v1/favorites/{favoriteId} | 즐겨찾기 삭제 |

**인증**: 모든 API는 JWT 인증 필요 (`@AuthUser AuthenticatedUser`)

---

### 5. DTO Classes (데이터 전송 객체)

#### 5.1 Request DTO
**파일 위치**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/favorite/dto/`

1. **AddFavoriteRequest**
   ```java
   - storeId: Long  // 즐겨찾기할 가게 ID
   ```

2. **ReorderFavoritesRequest**
   ```java
   - favoriteOrders: List<FavoriteOrderDto>
     - favoriteId: Long   // 즐겨찾기 ID
     - priority: Long     // 새로운 우선순위
   ```

#### 5.2 Response DTO

1. **AddFavoriteResponse**
   ```java
   - favoriteId: Long
   - storeId: Long
   - priority: Long
   - favoritedAt: LocalDateTime
   ```

2. **GetFavoritesResponse**
   ```java
   - favorites: List<FavoriteStoreDto>
   - totalCount: Integer
   ```

3. **FavoriteStoreDto**
   ```java
   - favoriteId: Long
   - storeId: Long
   - storeName: String
   - categoryName: String
   - reviewCount: Integer
   - averagePrice: Integer
   - address: String
   - imageUrl: String
   - priority: Long
   - favoritedAt: LocalDateTime
   ```

4. **ReorderFavoritesResponse**
   ```java
   - updatedCount: Integer
   - message: String
   ```

5. **DeleteFavoriteResponse**
   ```java
   - favoriteId: Long
   - message: String
   ```

---

### 6. REST Docs Test (API 문서화 테스트)

#### 6.1 FavoriteControllerRestDocsTest
**파일**: `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/favorite/controller/FavoriteControllerRestDocsTest.java`

**테스트 케이스** (총 10개):

| 테스트명 | HTTP Status | 설명 |
|---------|------------|------|
| addFavorite_Success | 200 OK | 즐겨찾기 추가 성공 |
| addFavorite_AlreadyExists | 409 Conflict | 이미 존재하는 즐겨찾기 |
| addFavorite_StoreNotFound | 404 Not Found | 존재하지 않는 가게 |
| getFavorites_Success | 200 OK | 즐겨찾기 목록 조회 성공 |
| getFavorites_Empty | 200 OK | 빈 목록 조회 |
| reorderFavorites_Success | 200 OK | 순서 변경 성공 |
| reorderFavorites_Forbidden | 403 Forbidden | 다른 사용자의 즐겨찾기 |
| deleteFavorite_Success | 200 OK | 즐겨찾기 삭제 성공 |
| deleteFavorite_NotFound | 404 Not Found | 존재하지 않는 즐겨찾기 |
| deleteFavorite_Forbidden | 403 Forbidden | 다른 사용자의 즐겨찾기 |

**생성된 문서**: `smartmealtable-api/build/generated-snippets/favorite-*`
- curl-request.adoc
- http-request.adoc
- http-response.adoc
- request-fields.adoc
- response-fields.adoc
- path-parameters.adoc

---

### 7. ErrorType 추가

**파일**: `smartmealtable-core/src/main/java/com/stdev/smartmealtable/core/error/ErrorType.java`

**추가된 에러**:
```java
FAVORITE_ALREADY_EXISTS(E409, "이미 즐겨찾기에 추가된 가게입니다."),
FAVORITE_NOT_FOUND(E404, "존재하지 않는 즐겨찾기입니다."),
FORBIDDEN_ACCESS(E403, "다른 사용자의 리소스에 접근할 수 없습니다.")
```

---

## 🔧 추가 수정 사항

### 1. JpaConfig에 JPAQueryFactory Bean 등록
**파일**: `smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/config/JpaConfig.java`

**변경 사항**:
```java
@Bean
public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
}
```

**이유**: StoreQueryDslRepository에서 JPAQueryFactory가 필요하기 때문

### 2. StoreService 수정
**파일**: `smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/store/service/StoreService.java`

**변경 사항**:
1. AddressHistory import 경로 수정
   - 변경 전: `com.stdev.smartmealtable.domain.member.AddressHistory`
   - 변경 후: `com.stdev.smartmealtable.domain.member.entity.AddressHistory`

2. AddressHistoryRepository 메서드 호출 수정
   - 변경 전: `findPrimaryAddressByMemberId()`
   - 변경 후: `findPrimaryByMemberId()`

3. Address VO 접근 방식 수정
   - 변경 전: `primaryAddress.getLatitude()`
   - 변경 후: `primaryAddress.getAddress().getLatitude()`
   - BigDecimal 변환: `BigDecimal.valueOf()`

---

## 📊 테스트 결과

### 빌드 결과
```
BUILD SUCCESSFUL in 40s
18 actionable tasks: 2 executed, 16 up-to-date
```

### 테스트 결과
- **전체 테스트**: 10개
- **성공**: 10개
- **실패**: 0개
- **성공률**: 100%

### 생성된 REST Docs 스니펫
- **총 10개 API 문서** 생성
- 각 문서당 9~11개의 스니펫 파일
- curl, http, httpie 요청 예제 포함
- 요청/응답 필드 상세 문서화

---

## 🎯 API 사용 예시

### 1. 즐겨찾기 추가
```bash
curl -X POST http://localhost:8080/api/v1/favorites \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"storeId": 1}'
```

**응답**:
```json
{
  "result": "SUCCESS",
  "data": {
    "favoriteId": 1,
    "storeId": 1,
    "priority": 1,
    "favoritedAt": "2025-10-12T23:28:00"
  }
}
```

### 2. 즐겨찾기 목록 조회
```bash
curl -X GET http://localhost:8080/api/v1/favorites \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

**응답**:
```json
{
  "result": "SUCCESS",
  "data": {
    "favorites": [
      {
        "favoriteId": 1,
        "storeId": 1,
        "storeName": "맛있는 한식당",
        "categoryName": "한식",
        "reviewCount": 150,
        "averagePrice": 8000,
        "address": "서울특별시 관악구 봉천동 123",
        "imageUrl": "https://example.com/korean-store.jpg",
        "priority": 1,
        "favoritedAt": "2025-10-12T23:28:00"
      }
    ],
    "totalCount": 1
  }
}
```

### 3. 즐겨찾기 순서 변경
```bash
curl -X PUT http://localhost:8080/api/v1/favorites/reorder \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "favoriteOrders": [
      {"favoriteId": 1, "priority": 2},
      {"favoriteId": 2, "priority": 1}
    ]
  }'
```

**응답**:
```json
{
  "result": "SUCCESS",
  "data": {
    "updatedCount": 2,
    "message": "즐겨찾기 순서가 성공적으로 변경되었습니다."
  }
}
```

### 4. 즐겨찾기 삭제
```bash
curl -X DELETE http://localhost:8080/api/v1/favorites/1 \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

**응답**:
```json
{
  "result": "SUCCESS",
  "data": {
    "favoriteId": 1,
    "message": "즐겨찾기가 성공적으로 삭제되었습니다."
  }
}
```

---

## 🏗️ 아키텍처 구조

```
smartmealtable-domain (도메인 계층)
├── favorite/
│   ├── Favorite.java                    // 도메인 엔티티
│   └── FavoriteRepository.java          // Repository 인터페이스

smartmealtable-storage/db (영속성 계층)
├── favorite/
│   ├── FavoriteEntity.java              // JPA 엔티티
│   ├── FavoriteJpaRepository.java       // Spring Data JPA Repository
│   ├── FavoriteMapper.java              // 도메인 ↔ JPA 변환
│   └── FavoriteRepositoryImpl.java      // Repository 구현체

smartmealtable-api (애플리케이션/프레젠테이션 계층)
├── favorite/
│   ├── controller/
│   │   └── FavoriteController.java      // REST Controller
│   ├── service/
│   │   └── FavoriteService.java         // Application Service
│   └── dto/
│       ├── AddFavoriteRequest.java
│       ├── AddFavoriteResponse.java
│       ├── GetFavoritesResponse.java
│       ├── FavoriteStoreDto.java
│       ├── ReorderFavoritesRequest.java
│       ├── ReorderFavoritesResponse.java
│       ├── DeleteFavoriteResponse.java
│       └── ...

smartmealtable-core (공통 계층)
└── error/
    └── ErrorType.java                   // 에러 타입 정의
```

---

## 📝 주요 설계 원칙 준수

### 1. 도메인 모델 패턴
- 비즈니스 로직을 도메인 엔티티에 집중
- `Favorite.create()`, `changePriority()` 등 도메인 메서드

### 2. 계층 분리
- Domain (비즈니스 로직)
- Storage (영속성 처리)
- Application Service (유즈케이스 조율)
- Controller (HTTP 요청 처리)

### 3. DTO 사용
- 각 계층 간 통신에 DTO 사용
- 도메인 객체를 직접 노출하지 않음

### 4. JPA 연관관계 지양
- FK 값을 필드로 직접 사용
- memberId, storeId를 Long 타입으로 관리

### 5. 에러 처리
- ErrorType Enum으로 중앙집중식 에러 관리
- HTTP 상태코드별 명확한 에러 메시지

### 6. REST Docs 문서화
- 테스트 기반 자동 API 문서 생성
- 성공/실패 시나리오 모두 문서화

---

## 🔄 다음 단계

### 추천 시스템 API 구현 예정
- 사용자 맞춤 음식점 추천
- 예산 기반 추천
- 위치 기반 추천
- REST Docs 문서화

---

## 📚 참고 자료

### 프로젝트 문서
- `IMPLEMENTATION_PROGRESS.md`: 전체 구현 진행 상황
- `API_SPECIFICATION.md`: API 스펙 문서
- `.github/copilot-instructions.md`: 프로젝트 가이드라인

### 코드 가이드라인
- TDD (RED-GREEN-REFACTORING)
- Spring REST Docs 기반 문서화
- Layered Architecture (Multi-Module)
- Domain Driven Design 원칙

---

## ✅ 체크리스트

- [x] Domain Entity 구현
- [x] Repository Interface 정의
- [x] JPA Entity 및 Repository 구현
- [x] Application Service 구현
- [x] REST Controller 구현
- [x] DTO 클래스 구현
- [x] ErrorType 추가
- [x] REST Docs 테스트 작성 (10개)
- [x] 모든 테스트 통과
- [x] 전체 빌드 성공
- [x] REST Docs 스니펫 생성
- [x] 문서화 완료

---

**작성자**: GitHub Copilot  
**날짜**: 2025-10-12  
**버전**: 1.0
