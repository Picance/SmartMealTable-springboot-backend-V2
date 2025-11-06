# 🐛 --auto-detect 동작하지 않을 때 해결 가이드

## 📋 --auto-detect vs --auto-detect-full 비교

| 옵션 | 테스트 범위 | 문서 생성 | 실행 시간 | 용도 |
|------|----------|---------|---------|------|
| `--auto-detect` | 감지된 테스트만 | 스니펫만 업데이트 | 1-2분 | 빠른 확인 |
| `--auto-detect-full` | 감지된 테스트만 | **전체 HTML 생성** | 1-2분 | **권장** ✅ |
| (기본) | **모든 테스트** | 전체 HTML 생성 | 5-10분 | 완전한 검증 |

**결론**: 대부분의 경우 **`--auto-detect-full`을 사용하세요!**

---

## 문제 현상

`./deploy-docs.sh --auto-detect` 또는 `--auto-detect-full` 실행 시 다음 메시지가 나타나고 종료됩니다:

```
Git에서 수정된 테스트를 감지하지 못했습니다.

원인 (다음 중 하나):
  1. 모든 변경사항이 커밋됨
  2. 수정된 파일 중 RestDocsTest가 없음
  3. Git에 변경사항이 stage되지 않음
```

---

## 🔧 해결 방법

### 원인 1: 파일이 아직 stage되지 않음
antml

**문제**: 파일을 수정했지만 `git add`를 하지 않았을 때

**해결**:
```bash
# 현재 파일 상태 확인
git status

# 변경사항을 stage
git add .

# 다시 시도
./deploy-docs.sh --auto-detect
```

---

### 원인 2: 모든 변경사항이 커밋됨

**문제**: 파일을 수정하고 이미 커밋했을 때

**해결**:
```bash
# 옵션 1: 수동으로 파일 지정
./deploy-docs.sh --test-filter 'AuthControllerRestDocsTest'

# 옵션 2: 모든 테스트 실행
./deploy-docs.sh

# 옵션 3: 이전 커밋 이후 변경사항이 있는지 확인
git diff HEAD~1 --name-only
```

---

### 원인 3: RestDocsTest가 아닌 다른 테스트 파일 수정

**문제**: `*RestDocsTest.java` 패턴이 아닌 다른 테스트를 수정했을 때

예시: `AuthControllerTest.java` (O) vs `AuthControllerRestDocsTest.java` (X)

**확인 방법**:
```bash
# 사용 가능한 RestDocsTest 목록 확인
./deploy-docs.sh --list-tests

# 수정한 파일이 RestDocsTest인지 확인
git status --short | grep "test"
```

**해결**: RestDocsTest 파일을 수정하거나, 해당 테스트를 수동으로 지정
```bash
./deploy-docs.sh --test-filter 'AuthControllerTest'
```

---

## ✅ 정상 작동 확인

### 1. Git 상태 확인

```bash
# 수정된 파일 목록 확인
git status

# 또는
git diff --name-only
```

예상 출력:
```
 M smartmealtable-api/src/test/java/.../AuthControllerRestDocsTest.java
```

### 2. 테스트 이름 확인

```bash
# 사용 가능한 모든 RestDocsTest
./deploy-docs.sh --list-tests
```

### 3. 수정된 파일의 정확한 이름

```bash
# 테스트 파일의 정확한 이름 확인
basename smartmealtable-api/src/test/java/.../AuthControllerRestDocsTest.java .java
# 출력: AuthControllerRestDocsTest
```

---

## 🚀 권장하는 사용 방법

### 방법 1: 파일 수정 → Stage → auto-detect-full (가장 권장)

```bash
# 1. 테스트 파일 수정
vim smartmealtable-api/src/test/java/.../AuthControllerRestDocsTest.java

# 2. 변경사항 stage
git add .

# 3. 자동 감지 + 전체 문서 생성 (가장 빠름!)
./deploy-docs.sh --auto-detect-full
```

### 방법 2: 파일 수정 → Stage → auto-detect (스니펫만 업데이트)

```bash
# 1. 테스트 파일 수정
vim smartmealtable-api/src/test/java/.../AuthControllerRestDocsTest.java

# 2. 변경사항 stage
git add .

# 3. 자동 감지로 배포 (스니펫만 업데이트)
./deploy-docs.sh --auto-detect
```

### 방법 3: 특정 테스트 수동 지정

```bash
# 정확한 테스트 이름으로 지정
./deploy-docs.sh --test-filter 'AuthControllerRestDocsTest'

# 여러 테스트
./deploy-docs.sh --test-filter '*AuthController*|*LoginController*'
```

### 방법 4: 모든 테스트 실행

```bash
./deploy-docs.sh
```

---

## 📊 디버깅 단계별 가이드

```bash
# Step 1: Git 상태 확인
git status

# Step 2: 수정된 테스트 파일만 필터링
git diff --name-only | grep -i "test.*\.java$"

# Step 3: 그 중 RestDocsTest만 필터링
git diff --name-only | grep -i "test.*\.java$" | grep -i "RestDocsTest"

# Step 4: 테스트 이름 추출
git diff --name-only | grep -i "test.*\.java$" | grep -i "RestDocsTest" | xargs -I {} basename {} .java

# Step 5: 추출한 이름으로 테스트 실행
./deploy-docs.sh --test-filter 'ExtractedTestName'
```

---

## 💡 팁

### Alias로 더 빠르게

```bash
# ~/.zshrc에 추가
alias dd-debug='git status && echo "---" && git diff --name-only | grep -i "test.*RestDocsTest"'

# 사용
dd-debug  # 수정된 RestDocsTest 파일 확인
```

### 자세한 로그 출력

스크립트의 자동 감지 부분에서 다음 메시지가 보이면 정상입니다:

```
🔍 Git에서 수정된 테스트 감지 중...

📝 감지된 수정된 테스트 파일:
   • smartmealtable-api/src/test/java/.../AuthControllerRestDocsTest.java

✓ 다음 테스트를 실행합니다:
   • AuthControllerRestDocsTest
```

이 메시지가 보이지 않고 바로 종료되면, 위의 원인 중 하나에 해당합니다.

---

## ❓ FAQ

### Q: `git add`를 했는데도 감지가 안 됨

**A**: 다음을 확인하세요:
```bash
# Staged 파일 확인
git diff --cached --name-only

# 파일명이 *RestDocsTest.java 패턴인지 확인
```

### Q: 파일은 수정했는데 git status에 안 보임

**A**: `.gitignore` 파일을 확인하세요:
```bash
cat .gitignore | grep -i test
```

### Q: 특정 테스트만 실행하고 싶은데 이름을 모름

**A**: 목록을 확인하세요:
```bash
./deploy-docs.sh --list-tests | grep -i auth
```

---

## 🎯 결론

| 상황 | 해결책 |
|------|--------|
| 파일 수정 후 (가장 권장) | `git add . && ./deploy-docs.sh --auto-detect-full` |
| 파일 수정 후 (스니펫만) | `git add . && ./deploy-docs.sh --auto-detect` |
| 커밋 후 특정 테스트 | `./deploy-docs.sh --test-filter 'TestName'` |
| 테스트명 모를 때 | `./deploy-docs.sh --list-tests` |
| 자동 감지 실패 | `./deploy-docs.sh` (모든 테스트 실행) |

---

**추가 지원**: `./deploy-docs.sh --help` 를 실행하면 더 자세한 사용법을 볼 수 있습니다.
