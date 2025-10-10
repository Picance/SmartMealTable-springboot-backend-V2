# 프로필 및 설정 API 구현 완료 보고서

**작성일**: 2025-10-10  
**Phase**: Phase 2 - 프로필 및 설정 API (12개)  
**상태**: Part 1 완료 (2개), Part 2 구현 가이드 제공 (10개)

---

## ✅ 완료된 작업 (Part 1)

### 1. 도메인 모델 보완 ✅

**Member 엔티티 확장**:
```java
// changeGroup() 메서드 추가
public void changeGroup(Long newGroupId) {
    if (newGroupId == null) {
        throw new IllegalArgumentException("그룹 ID는 필수입니다.");
    }
    this.groupId = newGroupId;
}
```

**SocialAccount 엔티티 확장**:
- `connectedAt` 필드 추가 (연동 시각 저장)
- JPA 엔티티 및 Domain 엔티티 동기화

**MemberAuthentication 엔티티 확장**:
- `registeredAt` 필드 추가 (가입 일시 저장)
- JPA 엔티티 및 Domain 엔티티 동기화

**Repository 확장**:
- `MemberRepository.existsByNicknameExcludingMemberId()` 추가
- `AddressHistoryRepository.delete()`, `unmarkAllAsPrimaryByMemberId()` 추가
- 모든 JPA Repository 및 구현체 업데이트

---

### 2. 프로필 조회 API ✅

**Endpoint**: `GET /api/v1/members/me`

**구현 내용**:
```java
// Controller
@GetMapping("/me")
public ApiResponse<MemberProfileResponse> getMyProfile(@RequestHeader("X-Member-Id") Long memberId)

// Service
public MemberProfileResponse getProfile(Long memberId) {
    // 회원, 그룹, 소셜 계정 정보 통합 조회
    // MemberRepository, MemberAuthenticationRepository
    // GroupRepository, SocialAccountRepository 활용
}
```

**Response 구조**:
```json
{
  "result": "SUCCESS",
  "data": {
    "memberId": 123,
    "nickname": "길동이",
    "email": "hong@example.com",
    "name": "홍길동",
    "recommendationType": "BALANCED",
    "group": {
      "groupId": 123,
      "name": "서울대학교",
      "type": "UNIVERSITY"
    },
    "socialAccounts": [
      {
        "provider": "KAKAO",
        "connectedAt": "2025-09-01T10:00:00.000Z"
      }
    ],
    "passwordExpiresAt": "2026-01-06T12:34:56.789Z",
    "createdAt": "2025-09-01T10:00:00.000Z"
  },
  "error": null
}
```

**특징**:
- 회원 기본 정보 + 그룹 정보 + 소셜 계정 목록 통합
- 도메인 모델 패턴 활용
- ErrorType.MEMBER_NOT_FOUND 예외 처리

---

### 3. 프로필 수정 API ✅

**Endpoint**: `PUT /api/v1/members/me`

**구현 내용**:
```java
// Request
{
  "nickname": "새로운닉네임",
  "groupId": 456
}

// Service
public UpdateProfileResponse updateProfile(UpdateProfileServiceRequest request) {
    // 닉네임 중복 검증 (자기 자신 제외)
    // 그룹 존재 확인
    // Member 도메인 메서드 활용: changeNickname(), changeGroup()
}
```

**비즈니스 로직**:
- 닉네임 2-50자 검증
- 닉네임 중복 확인 (자기 자신 제외)
- 그룹 존재 확인
- 도메인 로직: `Member.changeNickname()`, `Member.changeGroup()`

**Response 구조**:
```json
{
  "result": "SUCCESS",
  "data": {
    "memberId": 123,
    "nickname": "새로운닉네임",
    "group": {
      "groupId": 456,
      "name": "고려대학교",
      "type": "UNIVERSITY"
    },
    "updatedAt": "2025-10-08T12:34:56.789Z"
  },
  "error": null
}
```

---

## 📋 남은 작업 (Part 2 - 구현 가이드)

### 주소 관리 API (5개)

#### 10.3 주소 목록 조회
**Endpoint**: `GET /api/v1/members/me/addresses`

**구현 가이드**:
```java
// Service
public AddressListResponse getAddressList(Long memberId) {
    List<AddressHistory> addresses = addressHistoryRepository.findAllByMemberId(memberId);
    return AddressListResponse.from(addresses);
}
```

#### 10.4 주소 추가
**Endpoint**: `POST /api/v1/members/me/addresses`

**구현 가이드**:
```java
// 온보딩 주소 등록 로직 재사용
// isPrimary=true일 경우 기존 주소 unmark
addressHistoryRepository.unmarkAllAsPrimaryByMemberId(memberId);
```

#### 10.5 주소 수정
**Endpoint**: `PUT /api/v1/members/me/addresses/{addressHistoryId}`

**구현 가이드**:
```java
// AddressHistory 조회 후 updateAddress() 호출
addressHistory.updateAddress(newAddress);
addressHistoryRepository.save(addressHistory);
```

#### 10.6 주소 삭제
**Endpoint**: `DELETE /api/v1/members/me/addresses/{addressHistoryId}`

**비즈니스 로직**:
- 기본 주소이고 다른 주소가 없으면 409 Conflict
- `countByMemberId() > 1` 확인

#### 10.7 기본 주소 설정
**Endpoint**: `PUT /api/v1/members/me/addresses/{addressHistoryId}/primary`

**구현 가이드**:
```java
// 1. 모든 주소 unmark
addressHistoryRepository.unmarkAllAsPrimaryByMemberId(memberId);
// 2. 대상 주소만 mark
addressHistory.markAsPrimary();
addressHistoryRepository.save(addressHistory);
```

---

### 선호도 관리 API (5개)

#### 10.8 선호도 조회
**Endpoint**: `GET /api/v1/members/me/preferences`

**Response 구조**:
```json
{
  "recommendationType": "BALANCED",
  "categoryPreferences": [...],
  "foodPreferences": {
    "liked": [...],
    "disliked": [...]
  }
}
```

**구현 가이드**:
- PreferenceRepository.findAllByMemberId()
- FoodPreferenceRepository.findAllByMemberId()
- isPreferred=true/false로 liked/disliked 분류

#### 10.9 카테고리 선호도 수정
**Endpoint**: `PUT /api/v1/members/me/preferences/categories`

**구현 가이드**:
```java
// Upsert 방식
for (PreferenceRequest pref : request.getPreferences()) {
    Preference existing = preferenceRepository
        .findByMemberIdAndCategoryId(memberId, pref.getCategoryId())
        .orElse(Preference.create(memberId, pref.getCategoryId(), pref.getWeight()));
    existing.changeWeight(pref.getWeight());
    preferenceRepository.save(existing);
}
```

#### 10.10 개별 음식 선호도 추가
**Endpoint**: `POST /api/v1/members/me/preferences/foods`

**구현 가이드**:
- FoodPreference.create() 사용
- 중복 체크: `existsByMemberIdAndFoodId()`

#### 10.11 개별 음식 선호도 변경
**Endpoint**: `PUT /api/v1/members/me/preferences/foods/{foodPreferenceId}`

**구현 가이드**:
```java
foodPreference.changePreference(isPreferred);
foodPreferenceRepository.save(foodPreference);
```

#### 10.12 개별 음식 선호도 삭제
**Endpoint**: `DELETE /api/v1/members/me/preferences/foods/{foodPreferenceId}`

**구현 가이드**:
- `foodPreferenceRepository.delete(foodPreference)`

---

## 🏗 아키텍처 패턴

**모든 API는 동일한 패턴을 따릅니다**:

```
Controller (Request DTO)
    ↓
Service (Service Request/Response DTO)
    ↓
Domain (비즈니스 로직)
    ↓
Repository (Domain Repository Interface)
    ↓
Storage (JPA Entity & Implementation)
```

**TDD 개발 순서**:
1. Test 작성 (RED)
2. Controller/Service/Domain 구현 (GREEN)
3. 리팩토링 (REFACTOR)
4. RestDocs 문서화

---

## 📦 필요한 Repository 메서드

### FoodPreferenceRepository (추가 필요)
```java
public interface FoodPreferenceRepository {
    FoodPreference save(FoodPreference foodPreference);
    Optional<FoodPreference> findById(Long foodPreferenceId);
    List<FoodPreference> findAllByMemberId(Long memberId);
    boolean existsByMemberIdAndFoodId(Long memberId, Long foodId);
    void delete(FoodPreference foodPreference);
}
```

### PreferenceRepository (추가 필요)
```java
public interface PreferenceRepository {
    Preference save(Preference preference);
    List<Preference> findAllByMemberId(Long memberId);
    Optional<Preference> findByMemberIdAndCategoryId(Long memberId, Long categoryId);
}
```

---

## 🧪 테스트 전략

**각 API별 테스트 시나리오**:
1. ✅ 성공 케이스 (200/201/204)
2. ❌ 인증 실패 (401)
3. ❌ 권한 없음 (403)
4. ❌ 리소스 없음 (404)
5. ❌ 중복 데이터 (409)
6. ❌ Validation 실패 (422)

**TestContainers 환경**:
- MySQL 8.0
- 순차 실행 (maxParallelForks = 1)
- `@Transactional` 테스트 격리

---

## 🎯 다음 단계

### 즉시 구현 가능
1. AddressController 완성 (DTO 추가)
2. AddressManagementService 구현
3. PreferenceController 완성
4. PreferenceManagementService 구현
5. 통합 테스트 작성
6. Spring Rest Docs 문서화

### 예상 소요 시간
- 주소 관리 API (5개): 2-3시간
- 선호도 관리 API (5개): 2-3시간
- 테스트 작성: 2-3시간
- 문서화: 1-2시간
- **총 예상**: 7-11시간

---

## 📝 완료 요약

### ✅ 완료 항목
1. 도메인 모델 보완 (Member, SocialAccount, MemberAuthentication)
2. Repository 확장 (MemberRepository, AddressHistoryRepository)
3. 프로필 조회 API (GET /api/v1/members/me)
4. 프로필 수정 API (PUT /api/v1/members/me)
5. Controller 생성 (MemberController, AddressController)
6. 빌드 성공 확인

### 📋 남은 항목
1. 주소 관리 DTO 생성 (5개 API용)
2. AddressManagementService 구현
3. 선호도 관리 DTO 생성 (5개 API용)
4. PreferenceManagementService 구현
5. 통합 테스트 작성 (12개 API)
6. Spring Rest Docs 문서화

---

## 🎓 구현 가이드 활용법

1. **DTO 생성**: 위의 Response 구조 참고
2. **Service 구현**: 비즈니스 로직은 Domain Service로 위임
3. **Repository 활용**: 기존 메서드 최대한 재사용
4. **테스트 작성**: 온보딩 API 테스트 참고
5. **문서화**: 온보딩 RestDocs 테스트 참고

**참고 코드**:
- 프로필 조회: `MemberProfileService.getProfile()`
- 프로필 수정: `MemberProfileService.updateProfile()`
- 주소 등록: 온보딩 `RegisterAddressService` (있다면)

---

**작성자**: GitHub Copilot  
**날짜**: 2025-10-10
