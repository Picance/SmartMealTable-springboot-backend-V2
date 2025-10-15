# Rest Docs 테스트 누락 엔드포인트 분석 보고서

**작성일:** 2025-10-15  
**목적:** Rest Docs 테스트가 작성되지 않은 API 엔드포인트 식별

---

## 📋 분석 결과 요약

### 전체 통계
- **Controller 파일 수:** 21개
- **Rest Docs 테스트 파일 수:** 27개
- **총 API 엔드포인트 수:** 73개
- **Rest Docs 테스트 누락 추정:** 일부 Controller에서 누락

---

## ✅ Rest Docs 테스트가 있는 Controller

### 1. AuthController (✅ 완료)
**엔드포인트:**
- POST `/api/v1/auth/signup/email` - 이메일 회원가입
- POST `/api/v1/auth/login/email` - 이메일 로그인
- POST `/api/v1/auth/refresh` - 토큰 재발급
- POST `/api/v1/auth/logout` - 로그아웃
- GET `/api/v1/auth/check-email` - 이메일 중복 확인

**Rest Docs 테스트:**
- ✅ `SignupControllerRestDocsTest.java`
- ✅ `LoginControllerRestDocsTest.java`
- ✅ `RefreshTokenControllerRestDocsTest.java`
- ✅ `LogoutControllerRestDocsTest.java`
- ✅ `CheckEmailControllerRestDocsTest.java`

### 2. SocialLoginController (✅ 완료)
**엔드포인트:**
- POST `/api/v1/auth/login/kakao` - 카카오 로그인
- POST `/api/v1/auth/login/google` - 구글 로그인

**Rest Docs 테스트:**
- ✅ `KakaoLoginControllerRestDocsTest.java`
- ✅ `GoogleLoginControllerRestDocsTest.java`

### 3. OnboardingController (✅ 완료)
**엔드포인트:**
- POST `/api/v1/onboarding/profile` - 프로필 설정
- POST `/api/v1/onboarding/address` - 주소 설정
- POST `/api/v1/onboarding/budget` - 예산 설정
- POST `/api/v1/onboarding/preferences` - 카테고리 선호도 설정
- GET `/api/v1/onboarding/foods` - 음식 목록 조회
- POST `/api/v1/onboarding/food-preferences` - 음식 선호도 설정
- POST `/api/v1/onboarding/policy-agreements` - 약관 동의

**Rest Docs 테스트:**
- ✅ `OnboardingProfileControllerRestDocsTest.java`
- ✅ `OnboardingAddressControllerRestDocsTest.java`
- ✅ `SetBudgetControllerRestDocsTest.java`
- ✅ `FoodPreferenceControllerRestDocsTest.java`
- ✅ `PolicyAgreementControllerRestDocsTest.java`

### 4. MemberController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/members/me` - 내 프로필 조회
- PUT `/api/v1/members/me` - 내 프로필 수정
- PUT `/api/v1/members/me/password` - 비밀번호 변경
- DELETE `/api/v1/members/me` - 회원 탈퇴

**Rest Docs 테스트:**
- ✅ `MemberControllerRestDocsTest.java`

### 5. AddressController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/members/me/addresses` - 주소 목록 조회
- POST `/api/v1/members/me/addresses` - 주소 추가
- PUT `/api/v1/members/me/addresses/{addressHistoryId}` - 주소 수정
- DELETE `/api/v1/members/me/addresses/{addressHistoryId}` - 주소 삭제
- PUT `/api/v1/members/me/addresses/{addressHistoryId}/primary` - 기본 주소 설정

**Rest Docs 테스트:**
- ✅ `AddressControllerRestDocsTest.java`

### 6. PreferenceController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/members/me/preferences` - 선호도 조회
- PUT `/api/v1/members/me/preferences/categories` - 카테고리 선호도 수정
- POST `/api/v1/members/me/preferences/foods` - 음식 선호도 추가
- PUT `/api/v1/members/me/preferences/foods/{foodPreferenceId}` - 음식 선호도 수정
- DELETE `/api/v1/members/me/preferences/foods/{foodPreferenceId}` - 음식 선호도 삭제

**Rest Docs 테스트:**
- ✅ `PreferenceControllerRestDocsTest.java`

### 7. PasswordExpiryController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/members/me/password/expiry-status` - 비밀번호 만료 상태 조회
- POST `/api/v1/members/me/password/extend-expiry` - 비밀번호 만료일 연장

**Rest Docs 테스트:**
- ✅ `PasswordExpiryControllerRestDocsTest.java`

### 8. SocialAccountController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/members/me/social-accounts` - 소셜 계정 목록 조회
- POST `/api/v1/members/me/social-accounts` - 소셜 계정 연동
- DELETE `/api/v1/members/me/social-accounts/{socialAccountId}` - 소셜 계정 연동 해제

**Rest Docs 테스트:**
- ✅ `SocialAccountControllerRestDocsTest.java`

### 9. BudgetController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/budgets/monthly` - 월간 예산 조회
- GET `/api/v1/budgets/daily` - 일일 예산 조회
- PUT `/api/v1/budgets` - 월간 예산 수정
- PUT `/api/v1/budgets/daily/{date}` - 일일 예산 수정

**Rest Docs 테스트:**
- ✅ `BudgetControllerRestDocsTest.java`

### 10. ExpenditureController (✅ 완료)
**엔드포인트:**
- POST `/api/v1/expenditures` - 지출 내역 등록
- POST `/api/v1/expenditures/parse-sms` - SMS 파싱 후 지출 내역 등록
- GET `/api/v1/expenditures/statistics` - 지출 통계 조회
- GET `/api/v1/expenditures` - 지출 내역 목록 조회
- GET `/api/v1/expenditures/{id}` - 지출 내역 상세 조회
- PUT `/api/v1/expenditures/{id}` - 지출 내역 수정
- DELETE `/api/v1/expenditures/{id}` - 지출 내역 삭제

**Rest Docs 테스트:**
- ✅ `ExpenditureControllerRestDocsTest.java`

### 11. MapController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/map/search-address` - 주소 검색
- GET `/api/v1/map/reverse-geocode` - 역지오코딩

**Rest Docs 테스트:**
- ✅ `MapControllerRestDocsTest.java`

### 12. FavoriteController (✅ 완료)
**엔드포인트:**
- POST `/api/v1/favorites` - 즐겨찾기 추가
- GET `/api/v1/favorites` - 즐겨찾기 목록 조회
- PUT `/api/v1/favorites/reorder` - 즐겨찾기 순서 변경
- DELETE `/api/v1/favorites/{favoriteId}` - 즐겨찾기 삭제

**Rest Docs 테스트:**
- ✅ `FavoriteControllerRestDocsTest.java`

### 13. NotificationSettingsController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/settings/notifications` - 알림 설정 조회
- PUT `/api/v1/settings/notifications` - 알림 설정 변경

**Rest Docs 테스트:**
- ✅ `NotificationSettingsControllerRestDocsTest.java`

### 14. AppSettingsController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/settings/app` - 앱 설정 조회
- PUT `/api/v1/settings/app/tracking` - 추적 설정 변경

**Rest Docs 테스트:**
- ✅ `AppSettingsControllerRestDocsTest.java`

### 15. PolicyController (✅ 완료)
**엔드포인트:**
- GET `/api/v1/policies` - 약관 목록 조회
- GET `/api/v1/policies/{policyId}` - 약관 상세 조회

**Rest Docs 테스트:**
- ✅ `PolicyControllerRestDocsTest.java`

---

## ⚠️ Rest Docs 테스트가 **누락된** Controller

### 1. StoreController (❌ 누락)
**엔드포인트:**
- GET `/api/v1/stores` - 가게 검색 (위치, 반경, 카테고리, 정렬 기준)
- GET `/api/v1/stores/{storeId}` - 가게 상세 조회
- GET `/api/v1/stores/autocomplete` - 가게 자동완성 검색

**누락된 Rest Docs 테스트:**
- ❌ `StoreControllerRestDocsTest.java` (파일 없음)

**필요한 테스트:**
- 가게 검색 (위치 기반, 카테고리 필터, 정렬)
- 가게 상세 조회
- 자동완성 검색

---

### 2. CartController (❌ 누락)
**엔드포인트:**
- POST `/api/v1/cart/items` - 장바구니 아이템 추가
- GET `/api/v1/cart/store/{storeId}` - 특정 가게의 장바구니 조회
- GET `/api/v1/cart` - 전체 장바구니 조회
- PUT `/api/v1/cart/items/{cartItemId}` - 장바구니 아이템 수량 변경
- DELETE `/api/v1/cart/items/{cartItemId}` - 장바구니 아이템 삭제
- DELETE `/api/v1/cart/store/{storeId}` - 특정 가게의 장바구니 전체 삭제

**누락된 Rest Docs 테스트:**
- ❌ `CartControllerRestDocsTest.java` (파일 없음)

**필요한 테스트:**
- 장바구니 아이템 추가
- 장바구니 조회 (전체/가게별)
- 장바구니 아이템 수량 변경
- 장바구니 아이템 삭제
- 가게별 장바구니 전체 삭제

---

### 3. CategoryController (❌ 누락)
**엔드포인트:**
- GET `/api/v1/categories` - 카테고리 목록 조회

**누락된 Rest Docs 테스트:**
- ❌ `CategoryControllerRestDocsTest.java` (파일 없음)

**필요한 테스트:**
- 카테고리 목록 조회 (계층 구조 포함)

---

### 4. RecommendationController (❌ 누락)
**엔드포인트:**
- GET `/api/v1/recommendations` - 음식점 추천 목록 조회
- GET `/api/v1/recommendations/{storeId}/score-detail` - 추천 점수 상세 조회
- PUT `/api/v1/recommendations/type` - 추천 타입 변경

**누락된 Rest Docs 테스트:**
- ❌ `RecommendationControllerRestDocsTest.java` (파일 없음)

**필요한 테스트:**
- 음식점 추천 목록 조회 (위치, 식사 타입, 추천 타입별)
- 추천 점수 상세 조회 (점수 계산 근거)
- 추천 타입 변경 (DISTANCE/RATING/POPULAR)

---

### 5. HomeController (❌ 누락)
**엔드포인트:**
- GET `/api/v1/home/dashboard` - 홈 대시보드 조회
- GET `/api/v1/members/me/onboarding-status` - 온보딩 상태 조회
- POST `/api/v1/members/me/monthly-budget-confirmed` - 월간 예산 확인 완료

**누락된 Rest Docs 테스트:**
- ❌ `HomeControllerRestDocsTest.java` (파일 없음)

**필요한 테스트:**
- 홈 대시보드 조회 (위치, 예산, 지출 요약)
- 온보딩 상태 조회
- 월간 예산 확인 완료

---

### 6. GroupController (❌ 누락)
**엔드포인트:**
- GET `/api/v1/groups` - 그룹 조회

**누락된 Rest Docs 테스트:**
- ❌ `GroupControllerRestDocsTest.java` (파일 없음)

**필요한 테스트:**
- 그룹 조회 (그룹 식사 관련 기능)

---

## 📊 우선순위별 정리

### 🔴 높은 우선순위 (핵심 기능)

#### 1. StoreController (❌)
- **이유:** 가게 검색은 앱의 핵심 기능
- **엔드포인트:** 3개
- **복잡도:** 중간 (검색 필터, 정렬, 페이징)

#### 2. RecommendationController (❌)
- **이유:** 음식점 추천은 앱의 주요 가치 제안
- **엔드포인트:** 3개
- **복잡도:** 높음 (복잡한 추천 로직, 점수 계산)

#### 3. HomeController (❌)
- **이유:** 홈 화면은 사용자가 가장 많이 접하는 화면
- **엔드포인트:** 3개
- **복잡도:** 중간 (대시보드 데이터 집계)

#### 4. CartController (❌)
- **이유:** 장바구니는 주문 전 필수 기능
- **엔드포인트:** 6개
- **복잡도:** 중간

---

### 🟡 중간 우선순위

#### 5. CategoryController (❌)
- **이유:** 카테고리는 검색/필터링에 사용
- **엔드포인트:** 1개
- **복잡도:** 낮음

---

### 🟢 낮은 우선순위

#### 6. GroupController (❌)
- **이유:** 그룹 기능은 부가 기능일 가능성
- **엔드포인트:** 1개
- **복잡도:** 낮음

---

## 📝 권장 작업 순서

### Phase 1: 핵심 기능 Rest Docs 작성
1. **StoreController** (3개 엔드포인트)
   - 가게 검색 (필터, 정렬, 페이징)
   - 가게 상세 조회
   - 자동완성 검색

2. **HomeController** (3개 엔드포인트)
   - 홈 대시보드
   - 온보딩 상태
   - 예산 확인 완료

### Phase 2: 주요 기능 Rest Docs 작성
3. **RecommendationController** (3개 엔드포인트)
   - 추천 목록 조회
   - 추천 점수 상세
   - 추천 타입 변경

4. **CartController** (6개 엔드포인트)
   - 장바구니 CRUD
   - 가게별 조회/삭제

### Phase 3: 기타 기능 Rest Docs 작성
5. **CategoryController** (1개 엔드포인트)
   - 카테고리 목록 조회

6. **GroupController** (1개 엔드포인트)
   - 그룹 조회

---

## ✅ 작업 체크리스트

### 누락된 Rest Docs 테스트 파일 생성
- [ ] `StoreControllerRestDocsTest.java` (3개 테스트)
- [ ] `HomeControllerRestDocsTest.java` (3개 테스트)
- [ ] `RecommendationControllerRestDocsTest.java` (3개 테스트)
- [ ] `CartControllerRestDocsTest.java` (6개 테스트)
- [ ] `CategoryControllerRestDocsTest.java` (1개 테스트)
- [ ] `GroupControllerRestDocsTest.java` (1개 테스트)

**총 작업량:** 6개 파일, 17개 테스트

---

## 📚 참고 사항

### Rest Docs 테스트 작성 패턴
```java
@DisplayName("XxxController REST Docs")
class XxxControllerRestDocsTest extends AbstractRestDocsTest {
    
    @Autowired
    private XxxRepository xxxRepository;
    
    @Test
    @DisplayName("엔드포인트 설명")
    void testMethod() throws Exception {
        // given
        // 테스트 데이터 생성
        
        // when & then
        mockMvc.perform(get("/api/v1/xxx")
                .header("Authorization", "Bearer " + accessToken)
                .param("paramName", "value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("xxx-endpoint-name",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestParameters(
                        parameterWithName("paramName").description("설명")
                    ),
                    responseFields(
                        fieldWithPath("result").description("결과 코드"),
                        fieldWithPath("data").description("응답 데이터")
                    )
                ));
    }
}
```

---

## 🎯 결론

### 현황 요약
- **총 Controller:** 21개
- **Rest Docs 완료:** 15개 ✅
- **Rest Docs 누락:** 6개 ❌

### 누락된 Controller
1. ❌ StoreController (높은 우선순위)
2. ❌ RecommendationController (높은 우선순위)
3. ❌ HomeController (높은 우선순위)
4. ❌ CartController (높은 우선순위)
5. ❌ CategoryController (중간 우선순위)
6. ❌ GroupController (낮은 우선순위)

### 권장 작업
**Phase 1부터 순차적으로 진행하여 핵심 기능부터 문서화 완료**

---

**작성자:** AI Assistant  
**작성일:** 2025-10-15  
**다음 작업:** StoreController Rest Docs 테스트 작성
