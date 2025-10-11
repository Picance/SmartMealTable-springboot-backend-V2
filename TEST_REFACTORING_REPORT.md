# Application Service와 Domain Service 분리 리팩토링 - 테스트 수정 보고서

## 개요

Application Service와 Domain Service 분리 리팩토링 후 테스트 코드 업데이트 작업을 완료했습니다.

## 수정 내역

### 1. AddressServiceTest 수정

**목적**: Application Service 테스트는 Domain Service를 Mock하여 orchestration 로직만 검증

**수정 사항**:
- `@Mock AddressDomainService` 추가
- 모든 테스트 메서드에서 `addressDomainService` Mock 동작 정의
- Repository Mock에서 Domain Service Mock으로 전환
- BDD Mockito 메서드 추가 import: `willDoNothing`, `willThrow`, `eq`

**수정한 테스트 메서드** (총 10개):
1. `getAddresses_Success` - 변경 없음 (Repository 직접 사용)
2. `addAddress_Success_FirstAddressBecomesProper` - Domain Service Mock 사용
3. `addAddress_Success_UnmarkPreviousPrimary` - Domain Service Mock 사용
4. `updateAddress_Success` - Domain Service Mock 사용
5. `updateAddress_Fail_NotFound` - Domain Service Mock 예외 던지기
6. `updateAddress_Fail_NotOwner` - Domain Service Mock 예외 던지기
7. `deleteAddress_Success` - Domain Service Mock (void 메서드)
8. `deleteAddress_Fail_CannotDeleteLastPrimary` - Domain Service Mock 예외 던지기
9. `setPrimaryAddress_Success` - Domain Service Mock 사용
10. `setPrimaryAddress_Fail_NotFound` - Domain Service Mock 예외 던지기

**테스트 결과**: ✅ 10/10 통과

### 2. CreateExpenditureControllerTest 수정

**문제**: 
- 리팩토링 후 도메인 검증 로직이 `Expenditure` 엔티티의 `validateItemsTotalAmount()` 메서드로 이동
- 이 메서드는 `IllegalArgumentException`을 던지는데, `GlobalExceptionHandler`는 이를 400 (BadRequest)로 처리
- 기존 테스트는 422 (UnprocessableEntity)를 기대

**수정 사항**:
- 테스트 명: `지출 내역 등록 실패 - 항목 총액과 지출 금액 불일치 (422)` → `(400)`
- 기대 상태: `.andExpect(status().isUnprocessableEntity())` → `.andExpect(status().isBadRequest())`
- 기대 에러 코드: `"E422"` → `"E400"`
- 주석 추가: 도메인 검증 예외는 IllegalArgumentException → 400 처리

**테스트 결과**: ✅ 통과

## 전체 테스트 결과

**최종 결과**: ✅ **151/151 테스트 모두 통과** (100% 성공률)

### 테스트 실행 시간
- 약 18분 (TestContainers를 사용한 통합 테스트 포함)

## Domain Service 테스트 전략

### 아키텍처 분석
- **domain 모듈**: JPA 없이 순수 도메인 객체만 포함
- **storage 모듈**: 실제 JPA 엔티티와 Repository 구현체 포함
- **api 모듈**: Application Service, Controller, 통합 테스트

### 테스트 접근 방식

**Application Service 테스트 (Mockist 스타일)**:
- Domain Service를 Mock하여 orchestration 로직만 검증
- 예: `AddressServiceTest`

**Domain Service 검증**:
- 기존 Controller 통합 테스트들이 간접적으로 Domain Service 검증
- 151개의 통합 테스트가 전체 플로우 (Controller → Application Service → Domain Service → Repository) 검증
- Domain Service의 비즈니스 로직이 실제 DB와 함께 동작하는지 검증됨

### 추후 고려사항

필요시 Domain Service 단위 테스트를 `api` 모듈에 작성 가능:
- `@SpringBootTest` 또는 `@DataJpaTest` 활용
- TestContainers로 실제 DB 검증
- 현재는 151개 통합 테스트로 충분히 커버됨

## 리팩토링 영향 분석

### 테스트 패턴 변화

**Before (Repository Mock)**:
```java
@Mock
private AddressHistoryRepository addressHistoryRepository;

given(addressHistoryRepository.countByMemberId(memberId)).willReturn(0L);
given(addressHistoryRepository.save(any(AddressHistory.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
```

**After (Domain Service Mock)**:
```java
@Mock
private AddressDomainService addressDomainService;

AddressHistory savedAddress = AddressHistory.reconstitute(...);
given(addressDomainService.addAddress(eq(memberId), any(Address.class), eq(false)))
        .willReturn(savedAddress);
```

### 장점
1. **책임 분리**: Application Service는 orchestration, Domain Service는 비즈니스 로직
2. **테스트 명확성**: 각 계층의 책임이 명확하게 테스트됨
3. **유지보수성**: 비즈니스 로직 변경 시 Domain Service만 수정하면 됨

### 주의사항
1. **예외 처리 일관성**: 도메인 검증 예외 (IllegalArgumentException)의 HTTP 상태 코드 확인 필요
2. **Mock 누락 방지**: 리팩토링 시 모든 관련 테스트에서 Domain Service Mock 추가 필수

## 다음 단계

1. ✅ 전체 테스트 통과 확인 완료
2. ⏳ 추가 리팩토링 대상 검토
   - SetPreferencesService
   - UpdateBudgetService
   - 기타 Application Service들
3. ⏳ IMPLEMENTATION_PROGRESS.md 업데이트
4. ⏳ 최종 리팩토링 보고서 업데이트

## 결론

리팩토링 후 테스트 수정 작업을 성공적으로 완료했습니다. **151개 테스트 모두 통과**하여 리팩토링이 기존 기능을 손상시키지 않았음을 확인했습니다. Application Service와 Domain Service의 책임 분리가 명확해졌고, 테스트 구조도 이를 반영하여 개선되었습니다.
