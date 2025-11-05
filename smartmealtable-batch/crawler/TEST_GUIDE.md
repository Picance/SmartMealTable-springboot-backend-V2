# 크롤러 테스트 가이드

학식 크롤러의 정상 동작을 검증하기 위한 테스트 가이드입니다.

## 📋 테스트 구조

### 1. 단위 테스트 (Unit Tests)

#### CafeteriaDataImportServiceTest
**위치**: `src/test/java/com/stdev/smartmealtable/batch/crawler/service/CafeteriaDataImportServiceTest.java`

데이터 저장 서비스의 핵심 로직을 검증합니다:
- ✅ 학식 데이터 저장 성공
- ✅ 평균 가격 계산
- ✅ 영업시간 저장 (평일 10:00-19:00, 주말 휴무)
- ✅ 가게 타입 설정 (CAMPUS_RESTAURANT)
- ✅ 카테고리 없을 시 처리

#### CampusCafeteriaDataTest
**위치**: `src/test/java/com/stdev/smartmealtable/batch/crawler/domain/CampusCafeteriaDataTest.java`

도메인 객체(DTO)의 생성 및 구조를 검증합니다:
- ✅ CampusCafeteriaData 생성
- ✅ RestaurantData 생성
- ✅ MenuData 생성
- ✅ 완전한 데이터 구조 생성

#### SeoulTechCafeteriaServiceTest
**위치**: `src/test/java/com/stdev/smartmealtable/batch/crawler/service/SeoulTechCafeteriaServiceTest.java`

크롤링 서비스를 검증합니다:
- ⚠️ 실제 노션 페이지 크롤링 (기본적으로 @Disabled)
- 필요 시 수동으로 활성화하여 실행

### 2. 통합 테스트 (Integration Tests)

#### CrawlerIntegrationTest
**위치**: `src/test/java/com/stdev/smartmealtable/batch/crawler/CrawlerIntegrationTest.java`

실제 데이터베이스(H2 인메모리)를 사용한 E2E 테스트:
- ✅ 학식 데이터를 데이터베이스에 저장
- ✅ 같은 이름의 가게가 건물명으로 구분됨
- ✅ 여러 건물의 데이터 한 번에 저장

## 🚀 테스트 실행 방법

### 방법 1: Gradle 명령어

```bash
# 프로젝트 루트에서
cd /path/to/SmartMealTable-springboot-backend-V2

# 모든 테스트 실행
./gradlew :smartmealtable-batch:crawler:test

# 특정 테스트만 실행
./gradlew :smartmealtable-batch:crawler:test --tests CafeteriaDataImportServiceTest

# 상세 로그와 함께 실행
./gradlew :smartmealtable-batch:crawler:test --info
```

### 방법 2: 테스트 스크립트

```bash
# 크롤러 디렉토리에서
cd smartmealtable-batch/crawler
./test-crawler.sh
```

### 방법 3: IDE에서 실행

IntelliJ IDEA, Eclipse 등에서:
1. 테스트 파일 열기
2. 클래스 또는 메서드 옆의 실행 버튼 클릭
3. "Run 'CafeteriaDataImportServiceTest'" 선택

## 📊 테스트 결과 확인

### 콘솔 출력
테스트 실행 시 콘솔에서 실시간으로 결과를 확인할 수 있습니다:

```
CafeteriaDataImportServiceTest > 학식 데이터를 성공적으로 저장한다 PASSED
CafeteriaDataImportServiceTest > 평균 가격이 올바르게 계산된다 PASSED
CafeteriaDataImportServiceTest > 영업시간이 올바르게 저장된다 PASSED
...
```

### HTML 보고서
더 상세한 결과는 HTML 보고서에서 확인:

```bash
# 보고서 열기 (Mac)
open smartmealtable-batch/crawler/build/reports/tests/test/index.html

# 보고서 열기 (Linux)
xdg-open smartmealtable-batch/crawler/build/reports/tests/test/index.html

# 보고서 열기 (Windows)
start smartmealtable-batch/crawler/build/reports/tests/test/index.html
```

## 🔍 실제 크롤링 테스트

실제 노션 페이지를 크롤링하여 테스트하려면:

### 1. 테스트 활성화
`SeoulTechCafeteriaServiceTest.java` 파일에서 `@Disabled` 어노테이션 제거:

```java
@Test
// @Disabled("실제 노션 페이지 크롤링 테스트 - 수동 실행용")  // 이 줄 삭제 또는 주석
@DisplayName("노션 페이지에서 학식 데이터를 크롤링한다")
void crawlCafeteriaData_Success() {
    // ...
}
```

### 2. 네트워크 권한 설정
실제 네트워크 요청이 필요하므로 방화벽 등을 확인하세요.

### 3. 테스트 실행
```bash
./gradlew :smartmealtable-batch:crawler:test --tests SeoulTechCafeteriaServiceTest
```

### 4. 결과 확인
콘솔에 크롤링 결과가 출력됩니다:

```
========================================
크롤링 결과:
========================================

건물: ST: Table
주소: 서울 노원구 공릉로 232 1학생회관 1층
가게 수: 5

  가게: 값찌개 (한식)
  메뉴:
    - 김치찌개: 6000원
    - 된장찌개: 6000원
    ...
```

## 🧪 테스트 커버리지

각 테스트가 검증하는 내용:

### CafeteriaDataImportServiceTest (단위 테스트)
- ✅ Repository Mock을 사용한 격리된 테스트
- ✅ 비즈니스 로직 검증 (평균 가격, 영업시간)
- ✅ 예외 상황 처리 (카테고리 없음)
- ⚡ 빠른 실행 속도

### CrawlerIntegrationTest (통합 테스트)
- ✅ 실제 데이터베이스 사용 (H2 인메모리)
- ✅ 전체 플로우 검증
- ✅ 데이터 정합성 확인
- ✅ 트랜잭션 롤백 (테스트 후 자동 정리)

### SeoulTechCafeteriaServiceTest (E2E 테스트)
- ✅ 실제 노션 페이지 크롤링
- ✅ HTML 파싱 검증
- ⚠️ 네트워크 의존성 (노션 서버 상태에 영향받음)
- ⚠️ 느린 실행 속도

## 🐛 테스트 실패 시 확인사항

### 1. 의존성 문제
```bash
# Gradle 의존성 다시 다운로드
./gradlew :smartmealtable-batch:crawler:clean
./gradlew :smartmealtable-batch:crawler:build --refresh-dependencies
```

### 2. 데이터베이스 설정
- H2 데이터베이스 의존성 확인
- `application-test.yml` 설정 확인

### 3. Mock 객체 설정
- Mockito 버전 확인
- Mock 설정이 올바른지 확인

### 4. 네트워크 문제 (실제 크롤링 테스트)
- 노션 페이지 URL이 올바른지 확인
- 방화벽 설정 확인
- 네트워크 연결 상태 확인

## 📈 테스트 통계

테스트 보고서에서 확인할 수 있는 정보:
- **성공/실패/스킵된 테스트 수**
- **각 테스트 실행 시간**
- **전체 테스트 커버리지**
- **실패한 테스트의 상세 스택 트레이스**

## 💡 테스트 작성 가이드

새로운 기능을 추가할 때:

1. **단위 테스트 먼저 작성** (TDD)
2. **Mock을 사용하여 격리**
3. **통합 테스트로 전체 플로우 검증**
4. **테스트 이름을 명확하게** (Given-When-Then 패턴)

```java
@Test
@DisplayName("새로운 기능이 정상 동작한다")
void newFeature_Success() {
    // Given - 준비
    // When - 실행
    // Then - 검증
}
```

## 🔗 관련 문서

- [README.md](README.md) - 크롤러 전체 가이드
- [Gradle Build Guide](../../docs/architecture/GRADLE_SETUP.md)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

---

문제가 발생하면 테스트 로그와 함께 개발팀에 문의하세요! 🚀

