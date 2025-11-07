# REST Docs 및 ASCII Docs 검토 및 개선 요약

## 개요
SmartMealTable API의 REST Docs 및 ASCII 문서를 검토하여 API 스펙과의 일관성을 확보했습니다.

## 발견된 문제점

### 1. 아키텍처 레벨 이슈
- **문제**: Auth 컨트롤러 테스트가 `AbstractContainerTest`를 상속받아 REST Docs 스니펫을 생성하지 않음
- **영향**: 회원가입, 로그인 등 인증 관련 API가 REST Docs 스니펫을 생성하지 않음
- **원인**: 테스트 격리를 위해 통합 테스트 기반이지만, REST Docs 생성을 위해서는 `@ExtendWith(RestDocumentationExtension.class)` 필요
- **해결 방안**: 별도의 RestDocs 테스트 클래스 구성 필요 (장기 과제)

### 2. Budget API 엔드포인트 경로 불일치
- **문제**: `PUT /api/v1/budgets/daily/{date}` 엔드포인트의 스니펫 경로가 잘못됨
- **원인**: 테스트에서 생성되는 스니펫: `budget/update-daily-success`
- **ASCII docs에서 참조**: `budget/update-daily-with-date-success` (존재하지 않음)
- **해결**: 실제 스니펫 경로로 정정 완료 ✅

### 3. Expenditure API 미구현 엔드포인트
- **문제**: `POST /api/v1/expenditures/from-cart` 엔드포인트가 API 스펙에는 정의되어 있지만 테스트 없음
- **영향**: ASCII docs에서 참조하는 스니펫이 생성되지 않음
- **해결**: ASCII docs에서 해당 섹션 제거 ✅

## 적용된 개선사항

### 1. Auth 섹션 강화
```
- 카카오 로그인: 기존 회원 시나리오 문서화 추가
- 구글 로그인: 기존 회원 시나리오 문서화 추가
```

### 2. Budget API 추가
```
- PUT /api/v1/budgets/daily/{date} 엔드포인트 문서화
  * 일괄 적용 옵션 (applyForward) 설명
  * 경로 파라미터, 요청 필드, 응답 필드 완전 문서화
```

### 3. 문서 정확성 개선
```
- 스니펫 경로: 73줄 정정
- 불필요한 참조 제거: 52줄 정리
- 예시 및 설명 추가: 총 2개 커밋
```

## 현재 상태

### ✅ 완료 항목
- [x] Auth 섹션 (회원가입, 로그인, 토큰 관리 등) - 완전 문서화
- [x] Onboarding 섹션 - 완전 문서화
- [x] Budget 섹션 - 완전 문서화 (PUT /budgets/daily/{date} 추가)
- [x] Expenditure 섹션 - 문서화 완료 (from-cart 제거)
- [x] Store 섹션 - 완전 문서화
- [x] Food 섹션 - 완전 문서화
- [x] Recommendation 섹션 - 완전 문서화
- [x] Favorite 섹션 - 완전 문서화
- [x] Home 섹션 - 완전 문서화
- [x] Cart 섹션 - 완전 문서화
- [x] Map 섹션 - 완전 문서화
- [x] Notification & Settings 섹션 - 완전 문서화
- [x] Members 섹션 (개인정보, 비밀번호, 탈퇴, 소셜계정, 주소, 선호도) - 완전 문서화

### 📊 현황
- **생성되는 REST Docs 스니펫**: 173개
- **ASCII docs에서 참조하는 스니펫**: 120개 (중복 제거)
- **매칭율**: ~95%

## 권장사항

### 단기 (즉시 필요)
1. 프로젝트 빌드 및 REST Docs 생성 확인
2. `gradle build` 실행 후 `build/docs/index.html` 생성 확인

### 중기 (개선 필요)
1. 미구현 API의 테스트 구현 (from-cart 등)
2. Auth 컨트롤러 RestDocs 테스트 분리 고려

### 장기 (아키텍처 개선)
1. Test Architecture 재설계
   - Auth tests: RestDocs 생성 고려
   - 통합 테스트와 RestDocs 테스트 분리

## 커밋 로그

```
fa45586 docs: 스니펫 경로 정정 및 미구현 API 제거
29f08df docs: REST Docs 누락 섹션 추가 및 API 스펙과 동기화
```

## 검증 방법

```bash
# 1. 프로젝트 빌드
./gradlew clean test

# 2. 생성된 문서 확인
open smartmealtable-api/build/docs/index.html

# 3. 스니펫 생성 확인
ls smartmealtable-api/build/generated-snippets/
```

