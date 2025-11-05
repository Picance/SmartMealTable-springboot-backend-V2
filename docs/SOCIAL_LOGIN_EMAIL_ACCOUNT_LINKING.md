# 소셜 로그인 - 이메일 계정 연동 기능

## 개요

이메일 회원가입으로 생성된 계정에 대해 동일한 이메일로 소셜 로그인이 진행될 때, 충돌 처리가 아닌 **기존 계정에 소셜 계정을 연동**하도록 구현되었습니다.

## 시나리오

### 기존 동작 (Before)
1. 사용자가 `qwe123@example.com`으로 이메일 회원가입
2. 이후 카카오 계정 `qwe123@example.com`으로 소셜 로그인 시도
3. **에러 발생** (이메일 중복 등)

### 개선된 동작 (After)
1. 사용자가 `qwe123@example.com`으로 이메일 회원가입
2. 이후 카카오 계정 `qwe123@example.com`으로 소셜 로그인 시도
3. **기존 계정에 카카오 소셜 계정이 자동으로 연동됨**
4. 이후 이메일 로그인 또는 카카오 로그인 모두 가능

## 구현 내용

### 1. SocialAuthDomainService 수정

#### 변경된 메서드: `createMemberWithSocialAccount`

**기능:**
- 동일 이메일의 기존 회원이 있는지 확인
- 기존 회원이 있으면 소셜 계정 연동
- 없으면 신규 회원 생성 및 소셜 계정 연동

**로직:**
```java
public Member createMemberWithSocialAccount(...) {
    // 1. 동일 이메일의 기존 회원 확인
    Optional<MemberAuthentication> existingAuth = 
        memberAuthenticationRepository.findByEmail(email);
    
    if (existingAuth.isPresent()) {
        // 기존 회원에 소셜 계정 연동
        return linkSocialAccountToExistingMember(...);
    }
    
    // 신규 회원 생성 및 소셜 계정 연동
    return createNewMemberWithSocialAccount(...);
}
```

#### 추가된 메서드

**1. `linkSocialAccountToExistingMember` (private)**
- 기존 회원에 소셜 계정을 연동하는 로직
- 소셜 계정 생성 및 저장
- 회원 정보 업데이트 (이름, 프로필 이미지)

**2. `createNewMemberWithSocialAccount` (private)**
- 신규 회원 생성 및 소셜 계정 연동 로직
- 기존 코드를 별도 메서드로 분리

### 2. 데이터 처리

#### 소셜 계정 연동 시
```java
// 1. 소셜 계정 생성
SocialAccount socialAccount = SocialAccount.create(
    existingAuth.getMemberAuthenticationId(),  // 기존 회원의 인증 ID
    provider,
    providerId,
    accessToken,
    refreshToken,
    tokenType,
    expiresAt
);
socialAccountRepository.save(socialAccount);

// 2. 회원 정보 업데이트 (선택적)
if (name != null && !name.isBlank()) {
    existingAuth.updateName(name);
}

if (profileImageUrl != null && !profileImageUrl.isBlank()) {
    member.changeProfileImage(profileImageUrl);
}
```

#### 논리 FK 관계
- `SocialAccount.memberAuthenticationId` → `MemberAuthentication.memberAuthenticationId`
- 물리 FK 제약조건 없이 논리적 연관관계만 유지
- 동일한 `MemberAuthentication`에 여러 소셜 계정 연동 가능

## 데이터베이스 구조

```
member (회원 기본 정보)
└─ member_authentication (인증 정보)
    ├─ social_account (카카오)
    ├─ social_account (구글)
    └─ social_account (네이버) ...
```

### 예시 데이터

**이메일 회원가입 후:**
```sql
-- member
member_id: 100
nickname: "qwe123"

-- member_authentication
member_authentication_id: 200
member_id: 100
email: "qwe123@example.com"
hashed_password: "hashed_value"

-- social_account: 없음
```

**카카오 로그인 후 (연동):**
```sql
-- member (변경 없음)
member_id: 100
nickname: "qwe123"
profile_image_url: "https://kakao.com/profile.jpg" (업데이트됨)

-- member_authentication (이름 업데이트)
member_authentication_id: 200
member_id: 100
email: "qwe123@example.com"
hashed_password: "hashed_value"
name: "카카오유저" (업데이트됨)

-- social_account (신규 생성)
social_account_id: 1
member_authentication_id: 200  -- 기존 회원 연동
provider: "KAKAO"
provider_id: "kakao123456"
access_token: "..."
```

## 테스트

### 1. 단위 테스트 (`SocialAuthDomainServiceTest`)

#### 추가된 테스트 케이스

**1. 기존 회원에 소셜 계정 연동**
```java
@Test
@DisplayName("동일 이메일의 기존 회원이 있으면 - 기존 회원에 소셜 계정을 연동한다")
void it_links_social_account_to_existing_member()
```
- 이메일 회원가입 후 소셜 로그인 시나리오
- 기존 회원 ID 유지 확인
- 소셜 계정만 신규 생성 확인
- 회원 정보 업데이트 확인

**2. 기존 회원 정보 업데이트**
```java
@Test
@DisplayName("기존 회원 정보를 소셜 정보로 업데이트한다")
void it_updates_existing_member_info_with_social_info()
```
- 이름, 프로필 이미지 업데이트 확인

### 2. Application Service 테스트 (`KakaoLoginServiceTest`)

#### 추가된 테스트 케이스

**이메일 회원가입 후 카카오 로그인**
```java
@Test
@DisplayName("이메일 회원가입 후 동일 이메일로 카카오 로그인 시 계정 연동")
void loginWithKakao_LinkToEmailAccount_Success()
```
- 전체 로그인 플로우 검증
- 기존 회원 ID 반환 확인
- `createMemberWithSocialAccount` 호출 확인

### 테스트 실행 결과

```bash
# Domain Service 테스트
./gradlew :smartmealtable-domain:test --tests "SocialAuthDomainServiceTest"
# ✅ BUILD SUCCESSFUL - 14 tests completed

# Application Service 테스트
./gradlew :smartmealtable-api:test --tests "KakaoLoginServiceTest"
# ✅ BUILD SUCCESSFUL - 3 tests completed
```

## 사용자 시나리오

### 시나리오 1: 이메일 가입 → 카카오 로그인

1. **이메일 회원가입**
   ```
   POST /api/v1/auth/signup/email
   {
     "email": "qwe123@example.com",
     "password": "password123!",
     "name": "홍길동"
   }
   ```
   - 회원 ID 100 생성
   - 이메일/비밀번호 인증 정보 저장

2. **카카오 로그인**
   ```
   POST /api/v1/auth/login/kakao
   {
     "authorizationCode": "kakao_auth_code",
     "redirectUri": "..."
   }
   ```
   - 카카오에서 이메일 `qwe123@example.com` 확인
   - 기존 회원 ID 100과 자동 연동
   - 소셜 계정 추가 생성
   - 프로필 이미지 업데이트

3. **이후 로그인 가능**
   - 이메일/비밀번호 로그인 ✅
   - 카카오 소셜 로그인 ✅

### 시나리오 2: 카카오 가입 → 구글 로그인 (동일 이메일)

1. **카카오 로그인 (신규)**
   - 회원 ID 200 생성
   - 카카오 소셜 계정 연동

2. **구글 로그인 (동일 이메일)**
   - 기존 회원 ID 200과 자동 연동
   - 구글 소셜 계정 추가 연동

3. **이후 로그인 가능**
   - 카카오 소셜 로그인 ✅
   - 구글 소셜 로그인 ✅

## 주요 장점

### 1. 사용자 경험 개선
- 이메일 회원가입 후 소셜 로그인 사용 가능
- 여러 소셜 계정을 하나의 회원 계정에 연동
- 충돌 에러 없이 자연스러운 계정 통합

### 2. 데이터 일관성
- 하나의 이메일 = 하나의 회원
- 중복 계정 방지
- 명확한 논리 FK 관계 유지

### 3. 유연성
- 소셜 계정 추가/제거 용이
- 여러 소셜 플랫폼 연동 가능
- 향후 확장성 확보

## 보안 고려사항

### 1. 이메일 검증
- 이메일 회원가입 시 이메일 인증 필요 (향후 구현)
- 소셜 로그인의 경우 OAuth 제공자가 이메일 인증 보장

### 2. 계정 탈취 방지
- 소셜 계정 연동 시 추가 인증 단계 고려 (선택적)
- 예: "이미 가입된 이메일입니다. 소셜 계정을 연동하시겠습니까?"

### 3. 개인정보 업데이트
- 소셜 로그인 시 최신 정보로 업데이트
- 이름, 프로필 이미지 자동 갱신

## 향후 개선 사항

### 1. 명시적 연동 확인
현재는 자동 연동이지만, 향후 사용자 확인 단계 추가 고려:
```
"qwe123@example.com 계정이 이미 존재합니다.
카카오 계정을 연동하시겠습니까? [연동] [취소]"
```

### 2. 연동된 소셜 계정 관리
- 연동된 소셜 계정 목록 조회
- 특정 소셜 계정 연동 해제
- 주 로그인 방법 설정

### 3. 이메일 인증
- 이메일 회원가입 시 이메일 인증 추가
- 인증되지 않은 이메일로 소셜 계정 연동 시 추가 보안 체크

## 커밋 메시지

```
feat(auth): 소셜 로그인 시 동일 이메일 계정 자동 연동 기능 추가

- 이메일 회원가입 후 동일 이메일로 소셜 로그인 시 기존 계정에 소셜 계정 연동
- SocialAuthDomainService.createMemberWithSocialAccount 메서드 개선
- linkSocialAccountToExistingMember, createNewMemberWithSocialAccount 메서드 추가
- 기존 회원 정보 업데이트 (이름, 프로필 이미지)
- 단위 테스트 추가: SocialAuthDomainServiceTest
- 통합 테스트 추가: KakaoLoginServiceTest

Related to: #소셜로그인, #계정연동
```

## 관련 파일

### 수정된 파일
- `smartmealtable-domain/src/main/java/com/stdev/smartmealtable/domain/member/service/SocialAuthDomainService.java`
- `smartmealtable-domain/src/test/java/com/stdev/smartmealtable/domain/member/service/SocialAuthDomainServiceTest.java`
- `smartmealtable-api/src/test/java/com/stdev/smartmealtable/api/auth/service/KakaoLoginServiceTest.java`

### 참조 문서
- `SRS.md` - 3.1. 사용자 인증 및 관리 (F-AUTH-001)
- `.github/copilot-instructions.md` - JPA 연관관계 매핑 지양 정책
