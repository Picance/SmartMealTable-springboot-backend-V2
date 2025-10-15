# StoreService 테스트 작성 완료 보고서

**작성일:** 2025-10-15  
**작업 범위:** StoreService 단위 테스트 작성 (Mockist 스타일)

---

## 📋 작업 개요

StoreController REST Docs 테스트 실패 원인 분석 결과, Service 레이어 테스트가 없어 StoreService의 동작을 검증하기 어려운 상황이었습니다. Mockist 스타일로 StoreService의 단위 테스트를 작성하여 비즈니스 로직의 정확성을 보장하도록 했습니다.

---

## ✅ 완료된 작업

### 1. StoreServiceTest 파일 생성
**파일 위치:**
```
smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/store/service/StoreServiceTest.java
```

### 2. 작성된 테스트 케이스 (11개)

#### 가게 목록 조회 (3개)
1. ✅ **가게 목록 조회 성공 - 기본 주소 존재**
   - 기본 파라미터로 가게 목록 조회
   - 페이징 정보 검증
   - 거리 정보 포함 확인

2. ✅ **가게 목록 조회 실패 - 기본 주소 없음**
   - `BusinessException` with `ErrorType.ADDRESS_NOT_FOUND` 발생 확인
   - 예외 처리 흐름 검증

3. ✅ **가게 목록 조회 성공 - 필터 적용**
   - 검색 키워드, 반경, 카테고리 ID, 영업 중 여부, 가게 유형, 정렬 기준 모두 적용
   - 복잡한 필터 조건 처리 검증

#### 가게 상세 조회 (3개)
4. ✅ **가게 상세 조회 성공**
   - 조회 이력 기록 확인
   - 조회수 증가 확인 (500 → 501)
   - 영업시간 및 임시 휴무 정보 포함 확인

5. ✅ **가게 상세 조회 실패 - 가게 없음**
   - `BusinessException` with `ErrorType.STORE_NOT_FOUND` 발생 확인

6. ✅ **가게 상세 조회 성공 - 임시 휴무 포함**
   - 임시 휴무 정보가 포함된 경우 응답 구조 검증
   - 휴무 사유, 시작일, 종료일 확인

#### 자동완성 검색 (5개)
7. ✅ **자동완성 검색 성공**
   - 검색 키워드와 limit으로 자동완성 검색
   - 가게 ID, 이름, 주소 반환 확인

8. ✅ **자동완성 검색 - 빈 키워드**
   - 빈 문자열 입력 시 빈 리스트 반환

9. ✅ **자동완성 검색 - null 키워드**
   - null 입력 시 빈 리스트 반환

10. ✅ **자동완성 검색 - limit 기본값 적용**
    - limit이 null인 경우 기본값 10 적용 확인

11. ✅ **자동완성 검색 - 결과 없음**
    - 존재하지 않는 가게 검색 시 빈 리스트 반환

---

## 🧪 테스트 스타일 및 패턴

### Mockist 스타일 적용
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 테스트")
class StoreServiceTest {
    
    @Mock
    private StoreRepository storeRepository;
    
    @Mock
    private StoreOpeningHourRepository storeOpeningHourRepository;
    
    @Mock
    private StoreTemporaryClosureRepository storeTemporaryClosureRepository;
    
    @Mock
    private StoreViewHistoryRepository storeViewHistoryRepository;
    
    @Mock
    private AddressHistoryRepository addressHistoryRepository;
    
    @InjectMocks
    private StoreService storeService;
}
```

### BDD 패턴 (given-when-then)
```java
@Test
@DisplayName("가게 목록 조회 성공 - 기본 주소 존재")
void getStores_success() {
    // given
    StoreListRequest request = new StoreListRequest(...);
    given(addressHistoryRepository.findPrimaryByMemberId(testMemberId))
            .willReturn(Optional.of(testAddress));
    
    // when
    StoreListResponse response = storeService.getStores(testMemberId, request);
    
    // then
    assertThat(response).isNotNull();
    assertThat(response.stores()).hasSize(1);
    verify(addressHistoryRepository).findPrimaryByMemberId(testMemberId);
}
```

---

## 🔧 해결한 기술적 문제

### 1. StoreListRequest의 기본값 처리
**문제:**
- StoreListRequest의 compact constructor가 null 값을 기본값으로 변환
- radius: null → 3.0
- sortBy: null → "distance"
- page: null → 0
- size: null → 20

**해결:**
- Mock stubbing 시 변환된 기본값을 사용하도록 수정
```java
given(storeRepository.searchStores(
        isNull(),
        any(BigDecimal.class),
        any(BigDecimal.class),
        eq(3.0), // 기본값
        isNull(),
        isNull(),
        isNull(),
        eq("distance"), // 기본값
        eq(0),
        eq(20)
)).willReturn(searchResult);
```

### 2. 도메인 엔티티의 Record 타입 처리
**문제:**
- `StoreOpeningHour`, `StoreTemporaryClosure`, `StoreViewHistory`가 모두 Record 타입
- static factory method가 없음

**해결:**
- Record의 canonical constructor를 사용하여 직접 생성
```java
new StoreOpeningHour(
        1L,
        testStoreId,
        DayOfWeek.MONDAY,
        "09:00",
        "21:00",
        null,
        null,
        false
)
```

### 3. StoreViewHistory 생성 방식
**확인 사항:**
- `StoreViewHistoryRepository.createViewHistory()` 메서드 사용
- 실제 서비스 코드에서 사용되는 방식과 동일하게 테스트

---

## 📊 테스트 커버리지

### 메서드 커버리지
- ✅ `getStores(Long memberId, StoreListRequest request)` - 3개 테스트
- ✅ `getStoreDetail(Long memberId, Long storeId)` - 3개 테스트
- ✅ `autocomplete(String keyword, Integer limit)` - 5개 테스트

### 시나리오 커버리지
- ✅ 정상 흐름 (Happy Path)
- ✅ 예외 처리 (Exception Handling)
- ✅ 경계값 테스트 (Edge Cases)
- ✅ null 처리
- ✅ 빈 값 처리
- ✅ 기본값 적용

---

## 🎯 테스트 결과

### 실행 결과
```bash
./gradlew :smartmealtable-api:test --tests "com.stdev.smartmealtable.api.store.service.StoreServiceTest"

BUILD SUCCESSFUL in 3s
```

**전체 테스트:** 11개  
**성공:** 11개 ✅  
**실패:** 0개

---

## 📝 검증 항목

### 1. 비즈니스 로직 검증
- [x] 기본 주소 존재 여부 확인
- [x] 가게 검색 Repository 호출 확인
- [x] 조회 이력 생성 및 저장 확인
- [x] 조회수 증가 로직 확인
- [x] 영업시간 및 임시 휴무 조회 확인
- [x] 자동완성 검색 로직 확인

### 2. 예외 처리 검증
- [x] 기본 주소 없음 → `BusinessException` (ADDRESS_NOT_FOUND)
- [x] 가게 없음 → `BusinessException` (STORE_NOT_FOUND)

### 3. 데이터 변환 검증
- [x] `StoreWithDistance` → `StoreListResponse.StoreItem`
- [x] `Store` + `StoreOpeningHour` + `StoreTemporaryClosure` → `StoreDetailResponse`
- [x] `Store` → `StoreAutocompleteResponse`

### 4. 페이징 계산 검증
- [x] `totalPages = ceil(totalCount / size)`
- [x] currentPage, pageSize 반영 확인

---

## 🚀 다음 단계

### 1. StoreController REST Docs 테스트 수정
현재 StoreControllerRestDocsTest가 실패하는 이유:
- ❌ `categoryId` 필드가 응답에 없음 (DTO에서 `categoryName`만 존재)
- ❌ `favoriteCount` 필드가 응답에 없음
- ❌ `isOpen` 필드가 `null`로 반환됨 (TODO 상태)
- ❌ 문서화된 필드명과 실제 응답 구조 불일치

**필요한 작업:**
1. StoreListResponse.StoreItem에 `categoryId` 추가 (또는 문서에서 제거)
2. `favoriteCount` 필드 추가 여부 결정
3. `isOpen` 계산 로직 구현 또는 문서에서 제거
4. REST Docs 필드 문서화 수정

### 2. Repository 구현체 테스트
- `StoreRepositoryImpl` 통합 테스트 작성 (QueryDSL 쿼리 검증)
- TestContainer 사용하여 실제 DB 쿼리 동작 확인

### 3. 도메인 서비스 테스트 (필요 시)
- 영업 중 여부 판단 로직이 Domain Service로 분리되면 별도 테스트 작성

---

## 💡 개선 제안

### 1. DTO 필드 일관성
**현재 문제:**
- `StoreListResponse.StoreItem`에 `categoryName`은 있지만 `categoryId`는 없음
- REST Docs 문서에는 `categoryId`가 명시되어 있음

**제안:**
- DTO에 `categoryId` 추가 (Category 조인 필요 없이 Store 엔티티에 이미 존재)
- 또는 API 스펙에서 `categoryId` 제거

### 2. 영업 중 여부 계산
**현재 상태:** TODO 주석으로 남겨져 있음

**제안:**
- Domain Service에 `isStoreOpen(Store, List<StoreOpeningHour>, List<StoreTemporaryClosure>)` 메서드 추가
- 현재 시간 기준으로 영업 중 여부 판단 로직 구현

### 3. 즐겨찾기 여부 조회
**현재 상태:** 항상 `false` 반환

**제안:**
- `FavoriteRepository.existsByMemberIdAndStoreId(Long memberId, Long storeId)` 메서드 활용
- StoreService에서 즐겨찾기 여부 조회 추가

---

## 📚 참고 문서

- [TEST_FIX_PROGRESS.md](./TEST_FIX_PROGRESS.md) - 테스트 수정 진행 현황
- [REST_DOCS_MISSING_SUMMARY.md](./REST_DOCS_MISSING_SUMMARY.md) - REST Docs 누락 엔드포인트 요약
- [API_SPECIFICATION.md](./API_SPECIFICATION.md) - API 스펙 문서

---

## 📌 요약

### ✅ 성공적으로 완료
- StoreService 단위 테스트 11개 작성 완료
- Mockist 스타일 및 BDD 패턴 적용
- 모든 비즈니스 로직 검증 완료
- 예외 처리 및 경계값 테스트 포함

### ⚠️ 남은 작업
- StoreController REST Docs 테스트 수정 (DTO 필드 불일치 해결)
- Repository 구현체 통합 테스트 작성 (선택)
- 영업 중 여부 계산 로직 구현 (TODO 해결)
- 즐겨찾기 여부 조회 로직 추가 (TODO 해결)

---

**작성자:** GitHub Copilot  
**테스트 프레임워크:** JUnit 5, Mockito, AssertJ  
**테스트 스타일:** Mockist (Collaboration-based Testing)
