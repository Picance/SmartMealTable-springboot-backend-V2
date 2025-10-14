# 설정 파일 리팩토링 요약

## 변경 사항

### ✅ 신규 파일
1. **`smartmealtable-core/src/main/resources/application-client.yml`**
   - OAuth (Kakao, Google)
   - Naver Map API
   - Spring AI (Vertex AI) 설정

2. **`smartmealtable-core/src/test/resources/application-client.yml`**
   - 테스트용 더미 값 설정
   - Spring AI 자동 설정 비활성화

### 📝 수정 파일
1. **`smartmealtable-api/src/main/resources/application.yml`**
   - ✅ 추가: `spring.profiles.include: client`
   - ❌ 제거: OAuth, Naver Map, Spring AI 설정

2. **`smartmealtable-api/src/test/resources/application.yml`**
   - ⚠️ client 프로파일을 include하지 않음 (환경 변수 문제 방지)
   - ✅ OAuth, Naver Map 설정을 직접 더미 값으로 포함

3. **`.env.example`**
   - 더 명확한 섹션 구분
   - 각 설정의 용도 설명 추가

## 핵심 개선점

### 1. 의존성 방향 정리
```
Before: api (설정) ← client (사용) ❌ 순환 참조 위험
After:  core (설정) → api, client ✅ 명확한 방향
```

### 2. 재사용성
- Admin, Batch 등 다른 모듈에서도 `spring.profiles.include: client`만 추가하면 사용 가능

### 3. 테스트 환경 분리
- Main: 환경 변수 기반 설정 (`application-client.yml` from core)
- Test: 직접 더미 값 설정 (`application.yml`에 직접 포함)

## 사용 방법

### 실행 환경 (Main)
```yaml
# smartmealtable-api/src/main/resources/application.yml
spring:
  profiles:
    include: client  # core의 설정 로드
```

### 테스트 환경 (Test)
```yaml
# smartmealtable-api/src/test/resources/application.yml
# client 프로파일 include 하지 않음!
oauth:
  kakao:
    client-id: test-kakao-client-id  # 직접 더미 값
```

### 새로운 실행 모듈 추가 시
```yaml
# smartmealtable-admin/src/main/resources/application.yml
spring:
  application:
    name: smartmealtable-admin
  profiles:
    include: client  # core의 client 설정 재사용
```

## 검증 완료
- ✅ 빌드 성공
- ✅ 테스트 성공 (BudgetControllerRestDocsTest 등)
- ✅ 설정 파일 로딩 정상

## 참고 문서
- 상세 내용: `CONFIG_REFACTORING_REPORT.md`
- 환경 변수: `.env.example`
