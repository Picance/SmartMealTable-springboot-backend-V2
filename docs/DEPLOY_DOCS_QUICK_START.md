# 🚀 REST Docs 배포 스크립트 - 빠른 시작

## 핵심 개선사항

기존 `deploy-docs.sh`는 **모든 테스트를 실행**하여 REST Docs를 업데이트했습니다.  
이제 **수정된 테스트만 선택적으로 실행**할 수 있습니다! ⚡

---

## 가장 빠른 방법 (권장)

### 자동 감지 모드

```bash
./deploy-docs.sh --auto-detect
```

**이 방식의 장점**:
- Git에서 수정된 파일을 자동으로 감지
- 관련된 RestDocsTest만 실행
- 가장 빠른 배포 가능 (1-2분)
- 수동 조작 불필요

**사용 시나리오**:
```bash
# 작업 완료 후
git add .
./deploy-docs.sh --auto-detect

# 특정 기능만 수정했으면 해당 테스트만 실행됨
# 여러 기능을 수정했으면 모든 관련 테스트 실행
```

---

## 원하는 테스트만 지정하기

### 단일 테스트

```bash
./deploy-docs.sh --test-filter 'AuthControllerRestDocsTest'
```

### 여러 테스트

```bash
# 여러 테스트를 | 로 구분
./deploy-docs.sh --test-filter '*AddressControllerRestDocsTest|*AuthControllerRestDocsTest|*BudgetControllerRestDocsTest'
```

### 패턴 매칭

```bash
# 특정 패키지의 모든 테스트
./deploy-docs.sh --test-filter 'com.stdev.smartmealtable.api.auth.*'

# 이름에 "Food"가 포함된 모든 테스트
./deploy-docs.sh --test-filter '*Food*'
```

---

## 사용 가능한 모든 테스트 보기

```bash
./deploy-docs.sh --list-tests
```

출력:
```
AddressControllerRestDocsTest
AuthControllerRestDocsTest
BudgetControllerRestDocsTest
...
```

---

## 테스트 스킵 (가장 빠름)

이미 생성된 snippets가 있다면:

```bash
./deploy-docs.sh --skip-tests
```

**소요 시간**: ~30초 (테스트 실행 없이 HTML만 생성)

---

## 시간 비교

| 명령어 | 소요 시간 | 용도 |
|--------|----------|------|
| `./deploy-docs.sh` | 5-10분 | 전체 검증 (초기 배포) |
| `./deploy-docs.sh --auto-detect` | 1-2분 | 일반적인 배포 (권장) |
| `./deploy-docs.sh --test-filter 'AuthControllerRestDocsTest'` | 30초 | 특정 기능만 수정 |
| `./deploy-docs.sh --skip-tests` | 30초 | 문서 재생성만 (snippets 재사용) |

---

## 실전 사용 예시

### 예시 1: 인증 기능 업데이트

```bash
# 파일 수정
vim smartmealtable-api/src/test/java/.../LoginControllerRestDocsTest.java

# 배포
./deploy-docs.sh --test-filter 'LoginControllerRestDocsTest'

# 확인
open docs/api-docs.html
```

### 예시 2: 여러 기능 업데이트 (권장 방식)

```bash
# 여러 파일 수정
# - AddressControllerRestDocsTest
# - BudgetControllerRestDocsTest
# - ExpenditureControllerRestDocsTest

# 자동으로 감지하고 배포
./deploy-docs.sh --auto-detect

# 끝!
```

### 예시 3: 빠른 재배포 (snippets 유지)

```bash
# 문서 스타일만 수정한 경우
./deploy-docs.sh --skip-tests

# 가장 빠름 (~30초)
```

---

## 주의사항

1. **최초 배포**는 반드시 모든 테스트를 실행하세요:
   ```bash
   ./deploy-docs.sh
   ```

2. **자동 감지는 HEAD와의 diff 기반**입니다:
   ```bash
   # git에서 수정된 파일을 인식하므로
   git add .  # 변경사항을 stage 해야 감지됨
   ./deploy-docs.sh --auto-detect
   ```

3. **테스트 스킵 시**는 `build/generated-snippets`이 존재해야 합니다

---

## 더 자세한 정보

`docs/DEPLOY_DOCS_GUIDE.md` 문서를 참고하세요.

---

## 팁

### 별칭(Alias) 설정으로 더 빠르게

```bash
# ~/.zshrc에 추가
alias dd='./deploy-docs.sh --auto-detect'
alias dd-all='./deploy-docs.sh'
alias dd-list='./deploy-docs.sh --list-tests'

# 사용
dd              # 자동 감지 배포
dd-all          # 전체 테스트
dd-list         # 사용 가능한 테스트 목록
```

---

**🎯 핵심**: `./deploy-docs.sh --auto-detect` 명령어 하나로 대부분의 경우를 해결할 수 있습니다!
