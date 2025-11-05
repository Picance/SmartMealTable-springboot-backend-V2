# 서울과학기술대학교 학식 크롤러

서울과학기술대학교 학식 정보를 크롤링하여 데이터베이스에 자동으로 저장하는 배치 애플리케이션입니다.

## 📋 기능

- **자동 크롤링**: 매주 월요일 오전 2시에 자동으로 학식 데이터를 크롤링합니다
- **데이터 저장**: 크롤링한 데이터를 가게(Store)와 음식(Food) 테이블에 저장합니다
- **수동 실행**: REST API를 통해 수동으로 크롤링을 실행할 수 있습니다

## 🏗️ 아키텍처

```
crawler/
├── src/main/java/
│   └── com/stdev/smartmealtable/batch/crawler/
│       ├── CrawlerApplication.java           # 메인 애플리케이션
│       ├── controller/
│       │   └── CrawlerController.java        # 수동 실행 API
│       ├── domain/
│       │   └── CampusCafeteriaData.java      # 크롤링 데이터 DTO
│       ├── scheduler/
│       │   └── CafeteriaCrawlerScheduler.java # 스케줄러
│       └── service/
│           ├── SeoulTechCafeteriaService.java    # 크롤링 서비스
│           └── CafeteriaDataImportService.java   # 데이터 저장 서비스
└── src/main/resources/
    └── application.yml                       # 설정 파일
```

## 📊 크롤링 대상

### ST: Table (1학생회관)
- **주소**: 서울 노원구 공릉로 232 1학생회관 1층
- **위치**: 37.6335837919849, 127.07689204595525
- **가게**: 값찌개, 경성카츠, 바비든든, 포포420, 뽀까뽀까

### ST: Dining (2학생회관)
- **주소**: 서울 노원구 공릉로 232 2학생회관 1층
- **위치**: 37.62985806656512, 127.07932350755101
- **가게**: 값찌개, 경성카츠, 중식대장, 키친101, 플라잉팬

## 🚀 실행 방법

### 1. 데이터베이스 설정

먼저 "아시안" 카테고리를 추가해야 합니다:

```sql
INSERT INTO category (name) VALUES ('아시안');
```

또는 프로젝트 루트의 `dml.sql` 파일을 실행하세요.

### 2. 환경 변수 설정

`application.yml` 또는 환경 변수로 데이터베이스 연결 정보를 설정하세요:

```bash
export DB_URL=jdbc:mysql://localhost:3306/smartmealtable
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

### 3. 애플리케이션 빌드

```bash
cd /path/to/SmartMealTable-springboot-backend-V2
./gradlew :smartmealtable-batch:crawler:build
```

### 4. 애플리케이션 실행

```bash
./gradlew :smartmealtable-batch:crawler:bootRun
```

또는 JAR 파일로 실행:

```bash
java -jar smartmealtable-batch/crawler/build/libs/crawler-0.0.1-SNAPSHOT.jar
```

## 🔧 수동 실행

애플리케이션이 실행 중일 때 REST API를 통해 수동으로 크롤링을 실행할 수 있습니다:

```bash
curl -X POST http://localhost:8080/api/crawler/execute
```

응답 예시:
```json
{
  "status": "success",
  "message": "학식 데이터 크롤링이 완료되었습니다."
}
```

## ⏰ 스케줄

- **실행 주기**: 매주 월요일 오전 2시 (KST)
- **Cron 표현식**: `0 0 2 * * MON`
- **타임존**: Asia/Seoul

## 📝 데이터 구조

### Store (가게)
- **store_type**: CAMPUS_RESTAURANT
- **category_id**: 카테고리별 ID (한식, 중식, 일식, 양식, 아시안)
- **영업시간**: 평일 10:00-19:00 (주말 휴무)
- **평균 가격**: 메뉴 가격의 평균값 자동 계산

### Food (음식)
- 각 가게의 메뉴와 가격
- 카테고리는 가게 카테고리와 동일

## 🛠️ 설정

`src/main/resources/application.yml`:

```yaml
crawler:
  notion-url: https://fern-magic-bde.notion.site/21e45244ac0a80fdb02ad064ce75d674
  timeout: 10000  # 10초
  retry-count: 3
```

## 📦 의존성

- **Spring Boot Starter Web**: REST API
- **Spring Boot Starter Batch**: 배치 처리
- **JSoup**: HTML 파싱
- **MySQL Connector**: 데이터베이스 연결

## 🔍 로그

로그 파일은 `logs/crawler.log`에 저장됩니다:

```
2025-11-05 02:00:00 [scheduling-1] INFO  CafeteriaCrawlerScheduler - 학식 데이터 크롤링 작업 시작
2025-11-05 02:00:01 [scheduling-1] INFO  SeoulTechCafeteriaService - 서울과학기술대학교 학식 데이터 크롤링 시작
2025-11-05 02:00:02 [scheduling-1] INFO  CafeteriaDataImportService - 학식 데이터 저장 시작: 2 개 건물
2025-11-05 02:00:05 [scheduling-1] INFO  CafeteriaDataImportService - 학식 데이터 저장 완료: 10 개 가게, 50 개 메뉴
2025-11-05 02:00:05 [scheduling-1] INFO  CafeteriaCrawlerScheduler - 학식 데이터 크롤링 작업 완료
```

## 🐛 트러블슈팅

### 크롤링 실패
- 노션 페이지 URL이 올바른지 확인
- 네트워크 연결 확인
- 노션 페이지 구조가 변경되었을 수 있음 → SeoulTechCafeteriaService 수정 필요

### 데이터베이스 저장 실패
- 데이터베이스 연결 정보 확인
- 카테고리 테이블에 필요한 카테고리가 있는지 확인
- 트랜잭션 롤백 로그 확인

### 스케줄러 동작 안 함
- `@EnableScheduling` 어노테이션 확인
- Cron 표현식 확인
- 서버 시간대 확인

## 📞 문의

문제가 발생하면 로그 파일(`logs/crawler.log`)을 확인하거나 개발팀에 문의하세요.

