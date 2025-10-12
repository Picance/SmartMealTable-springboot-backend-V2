# 즐겨찾기 API 구현 완료 리포트

## 📋 개요

사용자 요청: "장바구니 API와 즐겨찾기 API 중 현재 구현하기 쉬운 것을 먼저 구현해줘"

**선택**: 즐겨찾기 API (4개 엔드포인트, 비즈니스 로직 단순)
- 장바구니 API: 6개 엔드포인트, Food 도메인 의존성, 복잡한 체크아웃 로직
- 즐겨찾기 API: 4개 엔드포인트, 독립적인 도메인, CRUD 위주

## ✅ 완료된 작업

### 1. Storage Layer 구현 (신규 구현)

#### **FavoriteEntity.java**
- 위치: `/smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/favorite/FavoriteEntity.java`
- 변경 사항:
  - `@Builder` 제거 (Domain-Driven Design 패턴 준수)
  - `fromDomain(Favorite)` 메서드 추가 (Domain → JPA Entity 변환)
  - `fromDomainWithId(Favorite)` 메서드 추가 (ID 포함 변환)
  - `toDomain()` 메서드 추가 (JPA Entity → Domain 변환)
- 이유: Domain 모델을 중심으로 변환 로직을 Entity 내부에 캡슐화

#### **FavoriteJpaRepository.java**
- 위치: `/smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/favorite/FavoriteJpaRepository.java`
- 이미 존재하던 인터페이스, 수정 없음
- 메서드:
  - `findByMemberIdOrderByPriorityAsc(Long memberId)`
  - `existsByMemberIdAndStoreId(Long memberId, Long storeId)`
  - `@Query("SELECT MAX(...)")` - 최대 우선순위 조회

#### **FavoriteRepositoryImpl.java**
- 위치: `/smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/favorite/FavoriteRepositoryImpl.java`
- 역할: FavoriteRepository (domain) 구현
- 주요 메서드:
  - `save()`, `findById()`, `delete()`, `saveAll()`
  - `findByMemberIdOrderByPriorityAsc()`
  - `existsByMemberIdAndStoreId()`
  - `findMaxPriorityByMemberId()`

#### **FavoriteMapper.java**
- 위치: `/smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/favorite/FavoriteMapper.java`
- 변경 사항: Entity의 static 변환 메서드 사용하도록 수정
  - `toEntity()` → `FavoriteEntity.fromDomain()` 호출
  - `toEntityWithId()` → `FavoriteEntity.fromDomainWithId()` 호출
  - `toDomain()` → `FavoriteEntity.toDomain()` 호출

### 2. Repository 확장 (StoreRepository)

#### **StoreRepository.java**
- 위치: `/smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/store/StoreRepository.java`
- 추가 메서드: `List<Store> findByIdIn(List<Long> storeIds)`
- 이유: FavoriteService.getFavorites()에서 배치로 Store 조회 필요

#### **StoreJpaRepository.java**
- 위치: `/smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/store/StoreJpaRepository.java`
- 추가 메서드: `List<StoreEntity> findByStoreIdInAndDeletedAtIsNull(List<Long> storeIds)`

#### **StoreRepositoryImpl.java**
- 위치: `/smartmealtable-storage/db/src/main/java/com/stdev/smartmealtable/storage/db/store/StoreRepositoryImpl.java`
- `findByIdIn()` 구현 추가

### 3. Controller HTTP 상태 코드 설정

#### **FavoriteController.java**
- 위치: `/smartmealtable-api/src/main/java/com/stdev/smartmealtable/api/favorite/controller/FavoriteController.java`
- 변경 사항: `addFavorite()` 메서드에 `@ResponseStatus(HttpStatus.CREATED)` 추가
- 이유: POST 요청으로 리소스 생성 시 201 Created 반환 (RESTful 규약)

### 4. 테스트 구현 (TDD 완료)

#### **FavoriteControllerTest.java**
- 위치: `/smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/favorite/controller/FavoriteControllerTest.java`
- **총 10개 테스트 케이스** (✅ 전부 성공)

**성공 시나리오 (5개)**
1. ✅ `addFavorite_success` - 즐겨찾기 추가 성공
2. ✅ `getFavorites_success` - 즐겨찾기 목록 조회 성공 (2개 항목)
3. ✅ `getFavorites_empty` - 즐겨찾기 목록 조회 성공 (빈 목록)
4. ✅ `reorderFavorites_success` - 즐겨찾기 순서 변경 성공
5. ✅ `deleteFavorite_success` - 즐겨찾기 삭제 성공

**실패 시나리오 (5개)**
6. ✅ `addFavorite_fail_storeNotFound` - 존재하지 않는 가게 (404)
7. ✅ `addFavorite_fail_duplicate` - 중복 등록 (409)
8. ✅ `reorderFavorites_fail_forbidden` - 권한 없음 (403)
9. ✅ `deleteFavorite_fail_notFound` - 존재하지 않는 즐겨찾기 (404)
10. ✅ `deleteFavorite_fail_forbidden` - 권한 없음 (403)

**테스트 커버리지**
- HTTP 상태 코드: 201, 200, 404, 409, 403 모두 검증
- ErrorType: STORE_NOT_FOUND, FAVORITE_ALREADY_EXISTS, FORBIDDEN_ACCESS, FAVORITE_NOT_FOUND
- 비즈니스 로직: 우선순위 자동 계산, 중복 검사, 소유권 검증

### 5. Rest Docs 생성

#### 생성된 스니펫 (4개)
- ✅ `favorite-add-success` - POST /api/v1/favorites
- ✅ `favorite-list-success` - GET /api/v1/favorites
- ✅ `favorite-reorder-success` - PUT /api/v1/favorites/reorder
- ✅ `favorite-delete-success` - DELETE /api/v1/favorites/{favoriteId}

위치: `/smartmealtable-api/build/generated-snippets/`

## 🔍 발견 및 해결한 이슈

### Issue 1: Member/MemberAuthentication FK 제약조건 위반
**문제**: 테스트 데이터 생성 시 `DataIntegrityViolationException`
```
Caused by: java.sql.SQLIntegrityConstraintViolationException
```

**원인**: MemberAuthentication을 Member보다 먼저 저장하려고 시도
- `member_authentication` 테이블이 `member.member_id`를 FK로 참조
- 테스트에서 MemberAuthentication 먼저 생성

**해결책**: 
```java
// 1. Member 먼저 저장
Member member = Member.create(null, "테스트유저", RecommendationType.BALANCED);
member = memberRepository.save(member);
memberId = member.getMemberId();

// 2. MemberAuthentication에 memberId 전달
MemberAuthentication auth = MemberAuthentication.createEmailAuth(
    memberId,  // FK reference
    "test@example.com",
    "encodedPassword123!",
    "테스트유저"
);
memberAuthenticationRepository.save(auth);
```

참고: `OnboardingProfileControllerTest` 패턴 참조

### Issue 2: HTTP 상태 코드 불일치
**문제**: 테스트 예상 201 Created, 실제 응답 200 OK
```
AssertionError: Status expected:<201> but was:<200>
```

**원인**: FavoriteController.addFavorite()에 `@ResponseStatus` 누락

**해결책**: 
```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)  // 추가
public ApiResponse<AddFavoriteResponse> addFavorite(...)
```

### Issue 3: API 응답 구조 불일치
**문제**: 테스트에서 `$.success` 기대, 실제 응답은 `$.result`
```
PathNotFoundException: $.success
```

**원인**: ApiResponse 구조 오해
- ApiResponse는 `result`, `data`, `error` 필드 사용
- `result`의 값: `ResultType.SUCCESS` 또는 `ResultType.ERROR`

**해결책**: 
```java
// 변경 전
.andExpect(jsonPath("$.success").value(true))
.andExpect(jsonPath("$.error.errorType").value("STORE_NOT_FOUND"))

// 변경 후
.andExpect(jsonPath("$.result").value("SUCCESS"))
.andExpect(jsonPath("$.error.code").value("E404"))
.andExpect(jsonPath("$.error.message").value("존재하지 않는 가게입니다."))
```

### Issue 4: StoreType enum 값 오류
**문제**: 컴파일 오류 - `StoreType.GENERAL_RESTAURANT` 존재하지 않음

**해결책**: 
```java
// 변경 전
.storeType(StoreType.GENERAL_RESTAURANT)

// 변경 후
.storeType(StoreType.RESTAURANT)
```

### Issue 5: 잘못된 import 경로
**문제**: Member, MemberAuthentication 클래스 import 오류
- `com.stdev.smartmealtable.domain.member.Member` (존재하지 않음)

**해결책**:
```java
// 정확한 import
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
```

## 📊 API 스펙 확인

### POST /api/v1/favorites
즐겨찾기 추가

**Request**
```json
{
  "storeId": 1
}
```

**Response (201 Created)**
```json
{
  "result": "SUCCESS",
  "data": {
    "favoriteId": 1,
    "storeId": 1,
    "priority": 1,
    "favoritedAt": "2025-10-13T02:42:00"
  }
}
```

**Error Responses**
- 404: STORE_NOT_FOUND (E404) - "존재하지 않는 가게입니다."
- 409: FAVORITE_ALREADY_EXISTS (E409) - "이미 즐겨찾기에 추가된 가게입니다."

---

### GET /api/v1/favorites
즐겨찾기 목록 조회

**Response (200 OK)**
```json
{
  "result": "SUCCESS",
  "data": {
    "favorites": [
      {
        "favoriteId": 1,
        "storeId": 1,
        "storeName": "맛있는 한식집",
        "categoryName": "한식",
        "reviewCount": 123,
        "averagePrice": 12000,
        "address": "서울특별시 강남구...",
        "imageUrl": "https://...",
        "priority": 1,
        "favoritedAt": "2025-10-13T02:42:00"
      }
    ],
    "totalCount": 1
  }
}
```

---

### PUT /api/v1/favorites/reorder
즐겨찾기 순서 변경

**Request**
```json
{
  "favoriteOrders": [
    { "favoriteId": 2, "priority": 1 },
    { "favoriteId": 1, "priority": 2 }
  ]
}
```

**Response (200 OK)**
```json
{
  "result": "SUCCESS",
  "data": {
    "updatedCount": 2,
    "message": "즐겨찾기 순서가 변경되었습니다."
  }
}
```

**Error Responses**
- 403: FORBIDDEN_ACCESS (E403) - "다른 사용자의 리소스에 접근할 수 없습니다."
- 404: FAVORITE_NOT_FOUND (E404) - "존재하지 않는 즐겨찾기입니다."

---

### DELETE /api/v1/favorites/{favoriteId}
즐겨찾기 삭제

**Response (200 OK)**
```json
{
  "result": "SUCCESS",
  "data": {
    "favoriteId": 1,
    "message": "즐겨찾기가 삭제되었습니다."
  }
}
```

**Error Responses**
- 404: FAVORITE_NOT_FOUND (E404) - "존재하지 않는 즐겨찾기입니다."
- 403: FORBIDDEN_ACCESS (E403) - "다른 사용자의 리소스에 접근할 수 없습니다."

## 🏗️ 아키텍처 계층

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  - FavoriteController (REST API)                        │
│  - @AuthUser ArgumentResolver (JWT 인증)                │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Application Layer                      │
│  - FavoriteService                                      │
│    • addFavorite(): 중복 검사, 우선순위 계산            │
│    • getFavorites(): 배치 조회 + 카테고리 정보          │
│    • reorderFavorites(): 소유권 검증 + 순서 변경        │
│    • deleteFavorite(): 소유권 검증 + 삭제               │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                        │
│  - Favorite (도메인 모델)                                │
│  - FavoriteRepository (인터페이스)                      │
│  - Store, StoreRepository                               │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Persistence Layer                      │
│  - FavoriteEntity (JPA Entity)                          │
│  - FavoriteRepositoryImpl (구현체)                      │
│  - FavoriteJpaRepository (Spring Data JPA)              │
│  - StoreRepositoryImpl.findByIdIn() (배치 조회)         │
└─────────────────────────────────────────────────────────┘
```

## 🎯 비즈니스 로직 상세

### 1. 즐겨찾기 추가 (addFavorite)
```java
// 1. 가게 존재 여부 확인
Store store = storeRepository.findById(storeId)
    .orElseThrow(() -> new BusinessException(ErrorType.STORE_NOT_FOUND));

// 2. 중복 검사
if (favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId)) {
    throw new BusinessException(ErrorType.FAVORITE_ALREADY_EXISTS);
}

// 3. 우선순위 계산 (최대값 + 1)
Long maxPriority = favoriteRepository.findMaxPriorityByMemberId(memberId);
Long newPriority = maxPriority + 1;

// 4. 도메인 모델 생성 및 저장
Favorite favorite = Favorite.create(memberId, storeId, newPriority);
Favorite savedFavorite = favoriteRepository.save(favorite);
```

**특징**:
- 우선순위 자동 계산 (사용자가 지정하지 않음)
- 중복 등록 방지 (같은 가게 2번 추가 불가)
- 존재하지 않는 가게 ID 검증

### 2. 즐겨찾기 목록 조회 (getFavorites)
```java
// 1. 즐겨찾기 목록 조회 (우선순위 오름차순)
List<Favorite> favorites = favoriteRepository.findByMemberIdOrderByPriorityAsc(memberId);

// 2. 가게 정보 배치 조회
List<Long> storeIds = favorites.stream()
    .map(Favorite::getStoreId)
    .collect(Collectors.toList());

Map<Long, Store> storeMap = storeRepository.findByIdIn(storeIds).stream()
    .collect(Collectors.toMap(Store::getStoreId, store -> store));

// 3. 카테고리 정보 배치 조회
List<Long> categoryIds = storeMap.values().stream()
    .map(Store::getCategoryId)
    .collect(Collectors.toList());

Map<Long, Category> categoryMap = categoryRepository.findByIdIn(categoryIds).stream()
    .collect(Collectors.toMap(Category::getCategoryId, category -> category));

// 4. DTO 조합
List<FavoriteItemDto> items = favorites.stream()
    .map(favorite -> {
        Store store = storeMap.get(favorite.getStoreId());
        Category category = categoryMap.get(store.getCategoryId());
        return FavoriteItemDto.builder()
            .favoriteId(favorite.getFavoriteId())
            .storeId(store.getStoreId())
            .storeName(store.getName())
            .categoryName(category.getName())
            // ...
            .build();
    })
    .collect(Collectors.toList());
```

**특징**:
- **N+1 문제 해결**: Store와 Category를 배치 조회
- 우선순위 순서대로 정렬 (priority ASC)
- 빈 목록 처리 (totalCount: 0)

### 3. 즐겨찾기 순서 변경 (reorderFavorites)
```java
// 1. favoriteId 목록 추출
List<Long> favoriteIds = request.getFavoriteOrders().stream()
    .map(ReorderFavoritesRequest.FavoriteOrderDto::getFavoriteId)
    .collect(Collectors.toList());

// 2. 즐겨찾기 목록 조회
List<Favorite> favorites = favoriteRepository.findByIdIn(favoriteIds);

// 3. 소유권 검증
boolean hasUnauthorized = favorites.stream()
    .anyMatch(favorite -> !favorite.getMemberId().equals(memberId));
if (hasUnauthorized) {
    throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
}

// 4. 우선순위 변경
Map<Long, Long> priorityMap = request.getFavoriteOrders().stream()
    .collect(Collectors.toMap(
        ReorderFavoritesRequest.FavoriteOrderDto::getFavoriteId,
        ReorderFavoritesRequest.FavoriteOrderDto::getPriority
    ));

favorites.forEach(favorite -> {
    Long newPriority = priorityMap.get(favorite.getFavoriteId());
    favorite.changePriority(newPriority);
});

// 5. 배치 저장
favoriteRepository.saveAll(favorites);
```

**특징**:
- 소유권 검증 (다른 사용자의 즐겨찾기 변경 불가)
- 배치 저장으로 성능 최적화
- 도메인 메서드 `changePriority()` 활용

### 4. 즐겨찾기 삭제 (deleteFavorite)
```java
// 1. 즐겨찾기 조회
Favorite favorite = favoriteRepository.findById(favoriteId)
    .orElseThrow(() -> new BusinessException(ErrorType.FAVORITE_NOT_FOUND));

// 2. 소유권 검증
if (!favorite.getMemberId().equals(memberId)) {
    throw new BusinessException(ErrorType.FORBIDDEN_ACCESS);
}

// 3. 삭제
favoriteRepository.delete(favorite);
```

**특징**:
- 존재하지 않는 즐겨찾기 검증
- 소유권 검증 (다른 사용자의 즐겨찾기 삭제 불가)
- Hard Delete (Soft Delete 아님)

## 📈 테스트 결과

```
> Task :smartmealtable-api:test

즐겨찾기 API 테스트 > 즐겨찾기 추가 - 성공 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 추가 - 실패: 존재하지 않는 가게 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 추가 - 실패: 중복 등록 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 목록 조회 - 성공 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 목록 조회 - 빈 목록 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 순서 변경 - 성공 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 순서 변경 - 실패: 권한 없음 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 삭제 - 성공 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 삭제 - 실패: 존재하지 않는 즐겨찾기 PASSED
즐겨찾기 API 테스트 > 즐겨찾기 삭제 - 실패: 권한 없음 PASSED

BUILD SUCCESSFUL in 16s
10 tests completed, 10 passed
```

## 🎓 학습 사항

### 1. ApiResponse 구조 표준화
- `result`: "SUCCESS" | "ERROR"
- `data`: 성공 시 응답 데이터
- `error`: 실패 시 에러 정보
  - `code`: ErrorCode (E404, E409, E403 등)
  - `message`: 에러 메시지
  - `data`: 추가 정보 (선택)

### 2. ErrorType과 ErrorCode 매핑
- `STORE_NOT_FOUND` → E404
- `FAVORITE_ALREADY_EXISTS` → E409
- `FORBIDDEN_ACCESS` → E403
- `FAVORITE_NOT_FOUND` → E404

### 3. Test Container 패턴
- Member → MemberAuthentication 순서 중요 (FK 제약조건)
- Store.builder()에서 StoreType.RESTAURANT 사용
- Category 먼저 생성 후 Store 생성

### 4. Domain 모델 패턴
- `Favorite.create()`: 정적 팩토리 메서드
- `Favorite.changePriority()`: 도메인 로직 캡슐화
- Repository는 인터페이스만 Domain에 위치

### 5. N+1 문제 해결
- `StoreRepository.findByIdIn()` 배치 조회
- `CategoryRepository.findByIdIn()` 배치 조회
- Map으로 조인 대신 메모리에서 매칭

## 🚀 다음 단계 제안

### 1. 장바구니 API 구현
- 현재 즐겨찾기 API 완료로 다음 우선순위
- 6개 엔드포인트: 추가, 조회, 수량 변경, 삭제, 전체 삭제, 주문하기
- Food 도메인 의존성 확인 필요

### 2. IMPLEMENTATION_PROGRESS.md 업데이트
- 즐겨찾기 API 4/4 완료 상태 반영
- Rest Docs 생성 완료 체크

### 3. API 통합 테스트
- 실제 JWT 토큰으로 인증 테스트
- 전체 API 흐름 검증 (회원가입 → 로그인 → 즐겨찾기 CRUD)

---

**작성일**: 2025-10-13  
**작성자**: GitHub Copilot  
**세션 시간**: 약 2시간  
**총 수정 파일**: 11개  
**총 테스트**: 10개 (100% 성공)
