# REST Docs 배포 스크립트 가이드

## 개요

`deploy-docs.sh` 스크립트는 Spring REST Docs 테스트를 실행하고 HTML 문서를 생성하여 `docs/` 디렉토리에 배포합니다.

**주요 개선사항**: 수정된 테스트만 선택적으로 실행하여 배포 시간을 단축할 수 있습니다.

---

## 사용 방법

### 1. 모든 RestDocsTest 실행 (기본 모드)

```bash
./deploy-docs.sh
```

**언제 사용**: 
- 처음 배포할 때
- 여러 파일이 수정되었을 때

---

### 2. 특정 테스트만 실행 (--test-filter)

```bash
# 단일 테스트 클래스 지정
./deploy-docs.sh --test-filter 'AuthControllerRestDocsTest'

# 여러 테스트 지정 (패턴 사용)
./deploy-docs.sh --test-filter '*AuthControllerRestDocsTest|*SignupControllerRestDocsTest'

# 패키지 기반 패턴
./deploy-docs.sh --test-filter 'com.stdev.smartmealtable.api.auth.*'
```

**장점**: 
- 특정 기능만 빠르게 문서 업데이트
- CI/CD 파이프라인에서 선택적 배포 가능

**예시**:
```bash
# 주소 관련 API 문서만 업데이트
./deploy-docs.sh --test-filter 'AddressControllerRestDocsTest'

# 음식 관련 모든 API 문서 업데이트
./deploy-docs.sh --test-filter '*FoodPreferenceControllerRestDocsTest|*GetFoodDetailRestDocsTest'
```

---

### 3. Git 변경사항 기반 자동 감지 (--auto-detect)

```bash
./deploy-docs.sh --auto-detect
```

**동작 방식**:
1. `git diff --name-only HEAD` 명령어로 수정된 파일 확인
2. 해당 파일과 연관된 RestDocsTest만 자동으로 찾아 실행
3. 수정된 테스트가 없으면 모든 RestDocsTest 실행

**장점**:
- 수동으로 테스트 이름을 지정할 필요 없음
- 배포 전 마지막 커밋의 변경사항만 반영
- Git 워크플로우와 자연스러운 통합

**사용 시나리오**:
```bash
# 작업 후 배포 전에
git add .
./deploy-docs.sh --auto-detect
```

---

### 4. 테스트 스킵 (--skip-tests)

```bash
./deploy-docs.sh --skip-tests
```

**언제 사용**:
- 이전에 생성된 snippets이 있을 때
- 테스트 없이 문서만 재생성하고 싶을 때
- 빠른 문서 업데이트 필요할 때

**주의**: `smartmealtable-api/build/generated-snippets` 디렉토리가 존재해야 합니다.

---

### 5. 사용 가능한 테스트 확인 (--list-tests)

```bash
./deploy-docs.sh --list-tests
```

**출력 예시**:
```
AddressControllerRestDocsTest
AppSettingsControllerRestDocsTest
AuthControllerRestDocsTest
BudgetControllerRestDocsTest
...
```

**용도**: 테스트 필터링 시 정확한 테스트 이름 확인

---

## 실전 사용 예시

### 시나리오 1: 인증 API 개선

```bash
# 1. 인증 관련 파일 수정
# - LoginControllerRestDocsTest.java 수정
# - SignupControllerRestDocsTest.java 수정

# 2. 수정된 테스트만 실행
./deploy-docs.sh --test-filter 'LoginControllerRestDocsTest|SignupControllerRestDocsTest'

# 또는 자동 감지
./deploy-docs.sh --auto-detect

# 3. 문서 확인
open docs/api-docs.html

# 4. 커밋
git add docs/
git commit -m "docs: Update authentication API docs"
```

### 시나리오 2: 여러 기능 수정

```bash
# 1. 여러 파일 수정 (주소, 예산, 지출 관련)
# - AddressControllerRestDocsTest.java 수정
# - BudgetControllerRestDocsTest.java 수정
# - ExpenditureControllerRestDocsTest.java 수정

# 2. 패턴으로 선택적 실행
./deploy-docs.sh --test-filter '*AddressControllerRestDocsTest|*BudgetControllerRestDocsTest|*ExpenditureControllerRestDocsTest'

# 또는 간단하게
./deploy-docs.sh --auto-detect
```

### 시나리오 3: 전체 문서 재생성

```bash
# 모든 테스트 실행 (첫 배포 또는 전체 업데이트 필요시)
./deploy-docs.sh
```

---

## Gradle 테스트 필터 문법

스크립트는 Gradle의 `--tests` 플래그를 사용합니다. 다음 패턴을 지원합니다:

| 패턴 | 설명 | 예시 |
|------|------|------|
| `*RestDocsTest` | 모든 RestDocsTest | `*RestDocsTest` |
| `AuthControllerRestDocsTest` | 정확한 클래스명 | `AuthControllerRestDocsTest` |
| `*AuthControllerRestDocsTest` | 와일드카드 | `*AuthControllerRestDocsTest` |
| `*AuthController*` | 부분 매칭 | `*AuthController*` |
| `Test1\|Test2` | 여러 테스트 (OR) | `*Auth*\|*Login*` |
| `com.stdev.*auth.*` | 패키지 패턴 | `com.stdev.smartmealtable.api.auth.*` |

---

## 스크립트 옵션 요약

```bash
./deploy-docs.sh [OPTIONS]

OPTIONS:
  (없음)                    모든 RestDocsTest 실행
  --skip-tests              테스트 스킵, 기존 snippets 사용
  --test-filter <PATTERN>   패턴에 맞는 테스트만 실행
  --auto-detect             Git 변경사항 기반 테스트 자동 감지
  --list-tests              사용 가능한 모든 RestDocsTest 나열
  --help, -h               이 도움말 표시
```

---

## 시간 비교

| 방식 | 소요 시간 | 상황 |
|------|----------|------|
| 모든 테스트 (`./deploy-docs.sh`) | ~5-10분 | 초기 배포, 전체 검증 필요 |
| 특정 테스트 (3-5개) | ~1-2분 | 기능별 배포 |
| 테스트 스킵 | ~30초 | 빠른 문서 재생성 |
| 자동 감지 | 상황별 | 권장 (가장 효율적) |

---

## 트러블슈팅

### Q: "Generated HTML not found" 오류

```bash
# 원인: 테스트가 실패했거나 snippets이 생성되지 않음

# 해결:
./deploy-docs.sh  # 전체 테스트로 다시 시도
```

### Q: 특정 테스트가 실행되지 않음

```bash
# 해결:
./deploy-docs.sh --list-tests  # 정확한 테스트명 확인
./deploy-docs.sh --test-filter 'CorrectTestName'
```

### Q: 자동 감지가 작동하지 않음

```bash
# 원인: git 히스토리가 없거나 수정된 RestDocsTest가 없음

# 해결:
./deploy-docs.sh --test-filter 'TestName'  # 수동으로 지정
```

---

## 베스트 프랙티스

1. **개발 중**: `--auto-detect`로 빠르게 검증
2. **PR 제출 전**: `--auto-detect`로 관련 문서 업데이트
3. **메인 브랜치 배포**: 전체 테스트 실행 (`./deploy-docs.sh`)
4. **긴급 수정**: `--test-filter`로 특정 API만 빠르게 업데이트

---

## 참고

- 스크립트는 `.env` 파일이 필요합니다 (Kakao, Google 클라이언트 정보)
- 생성된 문서는 `docs/api-docs.html`에 저장됩니다
- GitHub Pages 설정: Settings > Pages > Source: main/docs
