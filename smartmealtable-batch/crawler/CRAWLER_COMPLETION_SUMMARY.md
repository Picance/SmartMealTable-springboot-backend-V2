# 서울과기대 학식 크롤러 완성 요약

## 📋 개요

노션 페이지(https://fern-magic-bde.notion.site/21e45244ac0a80fdb02ad064ce75d674)에서 서울과학기술대학교 학생식당 메뉴 정보를 자동으로 크롤링하여 데이터베이스에 저장하는 시스템을 구축했습니다.

## ✅ 완성 기능

### 1. 크롤링 대상
- **ST: Table (1학생회관)**: 5개 가게, 44개 메뉴
  - 값찌개 (한식)
  - 경성카츠 (일식)
  - 바비든든 (한식)
  - 포포420 (아시안)
  - 뽀까뽀까 (한식)

- **ST: Dining (2학생회관)**: 5개 가게, 59개 메뉴
  - 값찌개 (한식)
  - 경성카츠 (일식)
  - 중식대장 (중식)
  - 키친101 (양식)
  - 플라잉팬 (한식)

**총계**: 2개 건물, 10개 가게, 103개 메뉴

### 2. 기술 스택
- **Selenium WebDriver**: 동적 JavaScript 렌더링 처리
- **Headless Chrome**: 백그라운드 실행
- **Spring Batch**: 스케줄링 및 데이터 처리
- **Spring Data JPA**: 데이터베이스 저장

### 3. 주요 특징
✅ **동적 콘텐츠 처리**: Selenium으로 JavaScript 렌더링 대기  
✅ **지연 로딩 대응**: 페이지 스크롤로 모든 메뉴 데이터 로드  
✅ **Board 뷰 파싱**: 노션 특유의 구조(가게 목록 → 메뉴 일괄 나열) 정확히 파싱  
✅ **자동 스케줄링**: 매주 월요일 오전 2시 자동 실행  
✅ **데이터 검증**: 중복 방지 및 메뉴 개수 기반 정확한 분배  

### 4. 데이터 저장
- **Store 엔티티**: 가게 정보 (이름, 주소, 위치, 카테고리, 평균 가격)
- **Food 엔티티**: 메뉴 정보 (이름, 가격, 카테고리)
- **StoreOpeningHour 엔티티**: 영업 시간 (평일 10:00-19:00)
- **가게 타입**: `CAMPUS_RESTAURANT`

## 🔧 핵심 구현 로직

### 노션 Board 뷰 파싱 알고리즘

노션 Board 뷰의 텍스트 구조:
```
ST: Table 메뉴판
Board
값찌개
7          ← 메뉴 개수
경성카츠
9          ← 메뉴 개수
...
햄김치찌개  ← 여기서부터 모든 메뉴가 순서대로!
₩6,500
순두부찌개
₩5,500
...
```

**파싱 단계:**
1. **섹션 식별**: "ST: Table 메뉴판", "ST: Dining 메뉴판"  
2. **가게 정보 수집**: 가게명과 메뉴 개수 추출  
3. **메뉴 데이터 수집**: 모든 메뉴명과 가격을 순서대로 수집  
4. **메뉴 분배**: 메뉴 개수에 따라 각 가게에 정확히 할당  

### 스케줄링
```java
@Scheduled(cron = "0 0 2 * * MON")  // 매주 월요일 오전 2시
public void scheduleCrawling()
```

## 📁 주요 파일

### 크롤링 서비스
- `SeleniumCrawlerService.java`: Selenium 기반 노션 페이지 크롤링
- `CafeteriaDataImportService.java`: 크롤링 데이터를 데이터베이스에 저장
- `CafeteriaCrawlerScheduler.java`: 주기적 크롤링 실행

### 도메인 모델
- `CampusCafeteriaData.java`: 크롤링 데이터 DTO
- `Store.java`, `Food.java`: 데이터베이스 엔티티

### 테스트
- `TestSeleniumCrawler.java`: 크롤링 테스트 (standalone 실행 가능)
- `CrawlerIntegrationTest.java`: 통합 테스트 (H2 in-memory DB)
- `CafeteriaDataImportServiceTest.java`: 단위 테스트

## 🚀 실행 방법

### 1. 테스트 실행
```bash
# IDE에서 실행
TestSeleniumCrawler.main() 메소드 실행

# 또는 Gradle 명령어
./gradlew :smartmealtable-batch:crawler:test --tests TestSeleniumCrawler
```

### 2. 애플리케이션 실행
```bash
./gradlew :smartmealtable-batch:crawler:bootRun
```
- 스케줄러가 자동으로 등록되어 매주 월요일 오전 2시에 크롤링 실행

### 3. 수동 크롤링 트리거
```java
// CafeteriaCrawlerScheduler 에서
scheduleCrawling() 메소드 직접 호출
```

## 🐛 트러블슈팅

### 문제 1: JSoup으로 메뉴 데이터 크롤링 실패
**원인**: 노션 페이지가 JavaScript로 동적 렌더링  
**해결**: Selenium WebDriver로 전환

### 문제 2: Headless 모드에서 메뉴 데이터 없음
**원인**: 노션의 지연 로딩(lazy loading)  
**해결**: 페이지 스크롤로 모든 컨텐츠 강제 로드

### 문제 3: 메뉴가 마지막 가게에 몰림
**원인**: 노션 Board 뷰 구조 오해 (가게별 메뉴가 아닌 일괄 나열)  
**해결**: 메뉴 개수 기반 분배 알고리즘 구현

### 문제 4: 파싱 실패 (테이블 태그 없음)
**원인**: 노션은 `<table>` 태그 대신 `<div>` 기반 레이아웃 사용  
**해결**: Body 텍스트 기반 파싱

## 📊 크롤링 결과 예시

```
========================================
📊 크롤링 통계
========================================
총 건물 수: 2
총 가게 수: 10
총 메뉴 수: 103

최저 가격: 2,500원
최고 가격: 10,500원
평균 가격: 6,425원
========================================
```

## 🔄 유지보수 가이드

### 가게 추가/삭제
`SeleniumCrawlerService.java`에서 `RestaurantConfig` 리스트 수정:
```java
List<RestaurantConfig> configs = List.of(
    new RestaurantConfig("새가게", "카테고리"),
    // ...
);
```

### 스케줄 변경
`CafeteriaCrawlerScheduler.java`에서 `@Scheduled` cron 표현식 수정:
```java
@Scheduled(cron = "0 0 2 * * MON")  // 초 분 시 일 월 요일
```

### 로그 레벨 조정
`application.yml`:
```yaml
logging:
  level:
    com.stdev.smartmealtable.batch.crawler: DEBUG
```

## ✅ 테스트 커버리지

- ✅ 단위 테스트: `CafeteriaDataImportServiceTest`
- ✅ 통합 테스트: `CrawlerIntegrationTest`
- ✅ 실제 크롤링 테스트: `TestSeleniumCrawler`
- ✅ 스케줄러 동작 확인

## 📝 향후 개선 사항

1. **에러 복구**: 크롤링 실패 시 재시도 로직
2. **알림**: 크롤링 실패 시 관리자 알림 (Slack, Email)
3. **변경 감지**: 메뉴 변경 감지 및 히스토리 저장
4. **성능 최적화**: 캐싱 및 배치 insert
5. **모니터링**: Prometheus + Grafana 메트릭

## 🎉 완성!

서울과기대 학식 크롤러가 성공적으로 완성되었습니다!  
10개 가게, 103개 메뉴를 자동으로 수집하여 데이터베이스에 저장합니다.

**작성일**: 2025-11-06  
**버전**: 1.0.0

