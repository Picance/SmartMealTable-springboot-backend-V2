# ⚡ --auto-detect 동작 이해하기

## 핵심

`--auto-detect`는 **RestDocsTest 파일**의 변경사항을 감지합니다.

```bash
./deploy-docs.sh --auto-detect
```

---

## 어떤 파일을 감지하나?

### ✅ 감지되는 파일

```
deploy-docs.sh는 감지 안 됨 ❌
deploy-docs.md는 감지 안 됨 ❌

smartmealtable-api/src/test/java/.../AuthControllerRestDocsTest.java   ✅
smartmealtable-api/src/test/java/.../LoginControllerRestDocsTest.java  ✅
smartmealtable-api/src/test/java/.../SignupControllerRestDocsTest.java ✅
```

---

## 현재 상황 분석

사용자가 다음 파일들을 수정했습니다:
- `M deploy-docs.sh` → **테스트 파일 아님** ❌
- `?? DEPLOY_DOCS_USAGE.md` → **테스트 파일 아님** ❌
- `?? docs/DEPLOY_DOCS_COMPLETION_REPORT.md` → **테스트 파일 아님** ❌

따라서 `--auto-detect`는 **감지할 수 있는 RestDocsTest 파일이 없어서** 다음 메시지를 표시합니다:

```
💡 Git에서 수정된 테스트를 감지하지 못했습니다.

원인 (다음 중 하나):
  1. 모든 변경사항이 커밋됨
  2. 수정된 파일 중 RestDocsTest가 없음   ← 이 경우! ✓
  3. Git에 변경사항이 stage되지 않음
```

---

## ✅ 올바른 사용 방법

### 방법 1: RestDocsTest 파일을 수정했을 때

```bash
# RestDocsTest 파일 수정
vim smartmealtable-api/src/test/java/.../AuthControllerRestDocsTest.java

# Stage
git add .

# 자동 감지 → 정상 작동
./deploy-docs.sh --auto-detect
```

**예상 출력**:
```
🔍 Git에서 수정된 테스트 감지 중...

📝 감지된 수정된 테스트 파일:
   • smartmealtable-api/src/test/java/.../AuthControllerRestDocsTest.java

✓ 다음 테스트를 실행합니다:
   • AuthControllerRestDocsTest

⏱️  테스트 실행 중 (몇 분이 소요될 수 있습니다)...
```

### 방법 2: RestDocsTest가 아닌 파일 수정했을 때

```bash
# 다른 파일 수정 (deploy-docs.sh, 문서 등)
# → --auto-detect는 감지할 수 없음

# 대안 1: 모든 테스트 실행
./deploy-docs.sh

# 대안 2: 특정 테스트 지정
./deploy-docs.sh --test-filter 'AuthControllerRestDocsTest'

# 대안 3: 테스트 스킵 (문서만 재생성)
./deploy-docs.sh --skip-tests
```

---

## 🎯 정리

| 수정한 파일 | --auto-detect | 해결책 |
|-----------|----------|-------|
| RestDocsTest (O) | ✅ 정상 | git add && ./deploy-docs.sh --auto-detect |
| 다른 파일 | ❌ 감지 안 됨 | ./deploy-docs.sh 또는 --test-filter 사용 |
| 아무것도 안 함 | ❌ 감지 안 됨 | RestDocsTest 파일 수정 필요 |

---

## 💡 이번 경우 (사용자의 상황)

**현재**: deploy-docs.sh 스크립트 자체를 개선함
**결과**: `--auto-detect`는 RestDocsTest 파일을 감지하지 못함 (정상)

**해결책**:
```bash
# 방법 1: 모든 RestDocsTest 실행 (권장 - 개선사항 검증)
./deploy-docs.sh

# 방법 2: 특정 테스트만 실행 (빠름)
./deploy-docs.sh --test-filter 'AddressControllerRestDocsTest'

# 방법 3: 테스트 스킵 (가장 빠름)
./deploy-docs.sh --skip-tests
```

---

## ✨ 요점

`--auto-detect`는 **개발 중 RestDocsTest를 수정했을 때 매우 유용**합니다:

```bash
# 작업 흐름
1. RestDocsTest 파일 수정
2. git add .
3. ./deploy-docs.sh --auto-detect ← 수정된 테스트만 빠르게 배포
```

하지만 **RestDocsTest가 아닌 다른 파일을 수정했다면** --auto-detect는 동작하지 않으므로, 위의 대안 중 하나를 사용하세요.

---

**정리**: 현재 상황에서는 완벽하게 동작하고 있습니다! 🎉
