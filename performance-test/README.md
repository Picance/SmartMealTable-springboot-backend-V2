# JMeter 성능 테스트

SmartMealTable 자동완성 API의 성능을 측정하기 위한 JMeter 테스트 스위트입니다.

## 📋 목차

- [개요](#개요)
- [디렉터리 구조](#디렉터리-구조)
- [사전 준비](#사전-준비)
- [테스트 실행](#테스트-실행)
- [결과 확인](#결과-확인)
- [성능 목표](#성능-목표)
- [커스터마이징](#커스터마이징)
- [트러블슈팅](#트러블슈팅)

## 개요

이 성능 테스트는 다음 자동완성 API들을 대상으로 합니다:

- **Store 자동완성**: `/api/v1/stores/autocomplete`
- **Food 자동완성**: `/api/v1/foods/autocomplete`
- **Group 자동완성**: `/api/v1/groups/autocomplete`

### 테스트 시나리오

| 도메인 | API 엔드포인트 | 동시 사용자 | Ramp-up | 지속 시간 | 목표 TPS |
|--------|----------------|-------------|---------|-----------|----------|
| Store  | `/api/v1/stores/autocomplete` | 100명       | 10초    | 120초     | 100/s    |
| Food   | `/api/v1/foods/autocomplete` | 100명       | 10초    | 120초     | 100/s    |
| Group  | `/api/v1/groups/autocomplete` | 100명       | 10초    | 120초     | 100/s    |
| **합계** | **300명** | -       | **120초** | **300/s** |

## 디렉터리 구조

```
performance-test/
├── README.md                    # 이 파일
├── run-test.sh                  # 테스트 실행 스크립트
├── jmeter/
│   └── autocomplete-performance-test.jmx  # JMeter 테스트 계획
├── data/
│   ├── keywords-store.csv       # Store 검색 키워드
│   ├── keywords-food.csv        # Food 검색 키워드
│   └── keywords-group.csv       # Group 검색 키워드
└── results/
    ├── test-results.jtl         # 원시 테스트 결과 (생성됨)
    ├── summary-report.csv       # 요약 리포트 (생성됨)
    ├── aggregate-report.csv     # 집계 리포트 (생성됨)
    └── html-report/             # HTML 리포트 (생성됨)
        └── index.html
```

## 사전 준비

### 1. JMeter 설치

#### macOS (Homebrew)
```bash
brew install jmeter
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install jmeter
```

#### Linux (CentOS/RHEL)
```bash
sudo yum install jmeter
```

#### 수동 설치
1. [Apache JMeter 다운로드](https://jmeter.apache.org/download_jmeter.cgi)
2. 압축 해제 후 `bin` 디렉터리를 PATH에 추가

설치 확인:
```bash
jmeter -v
```

### 2. 애플리케이션 실행

성능 테스트를 실행하기 전에 API 서버를 먼저 실행해야 합니다.

#### Docker Compose로 인프라 실행
```bash
cd /path/to/SmartMealTable-springboot-backend-V2
docker-compose -f docker-compose.local.yml up -d
```

#### API 서버 실행
```bash
./gradlew :smartmealtable-api:bootRun
```

#### 헬스 체크
```bash
curl http://localhost:8080/actuator/health
```

응답 예시:
```json
{"status":"UP"}
```

### 3. 테스트 데이터 준비

**자동 준비 (권장)**: `run-test.sh` 스크립트가 자동으로 테스트 데이터를 확인하고 삽입합니다.

**수동 준비 (필요 시)**:
```bash
# MySQL에 테스트 데이터 삽입
docker exec -i smartmealtable-mysql mysql -uroot -proot123 smartmealtable < test-data.sql

# 확인
docker exec smartmealtable-mysql mysql -uroot -proot123 smartmealtable -e "SELECT COUNT(*) FROM store; SELECT COUNT(*) FROM food; SELECT COUNT(*) FROM member_group;"
```

**CSV 키워드 데이터** (`data/` 디렉터리):
- `keywords-store.csv`: 20개 Store 검색 키워드
- `keywords-food.csv`: 20개 Food 검색 키워드
- `keywords-group.csv`: 20개 Group 검색 키워드

**DB 테스트 데이터** (`test-data.sql`):
- Store: 20개 (치킨집, 피자집, 맥도날드, 스타벅스 등)
- Food: 22개 (치킨, 파스타, 떡볶이 등)
- Group: 19개 (서울대학교, 삼성전자 등)

> 💡 **자동화**: `run-test.sh`는 Store 테이블의 데이터가 10개 미만일 때 자동으로 `test-data.sql`을 실행합니다.

## 테스트 실행

### 기본 실행 (추천)

```bash
cd performance-test
./run-test.sh
```

**자동 실행 내용**:
1. ✅ JMeter 설치 확인
2. ✅ 애플리케이션 상태 확인 (http://localhost:8080)
3. ✅ MySQL 연결 확인
4. ✅ **테스트 데이터 자동 확인 및 삽입** ⬅️ 새로운 기능!
5. ✅ 이전 결과 백업
6. ✅ JMeter 테스트 실행
7. ✅ HTML 리포트 생성

### 커스텀 파라미터로 실행

환경 변수로 테스트 설정을 변경할 수 있습니다:

```bash
# Base URL 변경
BASE_URL=http://192.168.1.100:8080 ./run-test.sh

# Ramp-up 시간 변경 (10초 → 20초)
RAMP_UP_TIME=20 ./run-test.sh

# 테스트 지속 시간 변경 (120초 → 300초)
TEST_DURATION=300 ./run-test.sh

# 여러 파라미터 동시 변경
BASE_URL=http://production.server.com:8080 \
RAMP_UP_TIME=30 \
TEST_DURATION=600 \
./run-test.sh
```

### GUI 모드로 실행 (디버깅용)

```bash
cd jmeter
jmeter -t autocomplete-performance-test.jmx
```

> ⚠️ **주의**: GUI 모드는 리소스를 많이 사용하므로 실제 성능 테스트에는 CLI 모드(`-n`)를 사용하세요.

## 결과 확인

테스트 완료 후 다음 결과 파일들이 생성됩니다:

### 1. HTML 리포트 (추천)

```bash
open results/html-report/index.html
```

HTML 리포트에는 다음 정보가 포함됩니다:
- **Dashboard**: 전체 테스트 요약
- **Over Time**: 시간에 따른 응답 시간, TPS 그래프
- **Throughput**: 처리량 분석
- **Response Times**: 응답 시간 분포
- **Errors**: 에러 분석

### 2. JTL 파일 (원시 데이터)

```bash
cat results/test-results.jtl | head -10
```

모든 요청/응답의 원시 데이터가 저장되어 있습니다.

### 3. CSV 리포트

#### Summary Report
```bash
cat results/summary-report.csv
```

각 API별 요약 통계:
- Label (API 이름)
- # Samples (요청 수)
- Average (평균 응답 시간)
- Min/Max (최소/최대 응답 시간)
- Error % (에러율)

#### Aggregate Report
```bash
cat results/aggregate-report.csv
```

더 상세한 통계:
- Median (중간값, P50)
- 90% Line (P90)
- 95% Line (P95)
- 99% Line (P99)
- Throughput (TPS)

## 성능 목표

### 응답 시간 목표

| 지표 | 목표 | 설명 |
|------|------|------|
| P50 (Median) | < 50ms | 50% 요청이 50ms 이내 응답 |
| P90 | < 80ms | 90% 요청이 80ms 이내 응답 |
| P95 | < 100ms | 95% 요청이 100ms 이내 응답 |
| P99 | < 300ms | 99% 요청이 300ms 이내 응답 |

### 처리량 목표

| 지표 | 목표 | 설명 |
|------|------|------|
| TPS | > 200/s | 초당 200개 이상의 요청 처리 |
| Error Rate | < 1% | 에러율 1% 미만 |

### 성능 목표 달성 여부 판단

HTML 리포트의 **Statistics** 테이블에서 다음을 확인하세요:

```
✅ 목표 달성:
- 90th pct (P90) < 80ms
- 95th pct (P95) < 100ms
- 99th pct (P99) < 300ms
- Error % < 1.0%
- Throughput > 200.0/sec

❌ 목표 미달성:
- 하나라도 목표치를 초과하면 최적화 필요
```

## 커스터마이징

### 1. 동시 사용자 수 변경

`jmeter/autocomplete-performance-test.jmx` 파일을 열고 각 Thread Group의 `num_threads` 값을 수정:

```xml
<stringProp name="ThreadGroup.num_threads">100</stringProp>
<!-- 100 → 원하는 사용자 수로 변경 -->
```

### 2. 테스트 데이터 추가

`data/` 디렉터리의 CSV 파일에 키워드를 추가:

```csv
keyword,chosung
새키워드,ㅅㅋㅇㄷ
```

### 3. 새로운 API 테스트 추가

1. JMeter GUI 실행:
   ```bash
   jmeter -t jmeter/autocomplete-performance-test.jmx
   ```

2. 새로운 Thread Group 추가:
   - 우클릭 > Add > Threads > Thread Group

3. HTTP Request Sampler 추가:
   - Thread Group 우클릭 > Add > Sampler > HTTP Request

4. 저장 후 CLI로 실행

### 4. 부하 패턴 변경

#### 스파이크 테스트 (급격한 부하 증가)
```xml
<stringProp name="ThreadGroup.ramp_time">1</stringProp>
<!-- 1초 안에 모든 사용자 동시 실행 -->
```

#### 스트레스 테스트 (지속적 부하 증가)
```xml
<stringProp name="ThreadGroup.num_threads">500</stringProp>
<stringProp name="ThreadGroup.ramp_time">60</stringProp>
<stringProp name="ThreadGroup.duration">600</stringProp>
```

#### 내구성 테스트 (장시간 실행)
```xml
<stringProp name="ThreadGroup.duration">3600</stringProp>
<!-- 1시간 동안 실행 -->
```

## 성능 최적화 팁

테스트 결과가 목표치에 미달할 경우 다음을 확인하세요:

### 1. 데이터베이스 최적화
```sql
-- 인덱스 확인
SHOW INDEX FROM store;
SHOW INDEX FROM food;
SHOW INDEX FROM member_group;

-- 쿼리 실행 계획 확인
EXPLAIN SELECT * FROM store WHERE name LIKE '치킨%' LIMIT 10;
```

### 2. 캐시 확인
```bash
# Redis 캐시 히트율 확인
redis-cli info stats | grep keyspace_hits
redis-cli info stats | grep keyspace_misses
```

캐시 히트율 = hits / (hits + misses) × 100
- 목표: > 80%

### 3. 애플리케이션 로그 확인
```bash
# 느린 API 호출 확인
grep "Autocomplete" logs/application.log | grep -E "[0-9]{3,}ms"

# 에러 확인
grep "ERROR" logs/application.log
```

### 4. 시스템 리소스 모니터링
```bash
# CPU 사용률
top

# 메모리 사용률
free -h

# 네트워크 I/O
netstat -s

# MySQL 연결 수
mysql -e "SHOW PROCESSLIST;" | wc -l
```

## 트러블슈팅

### 문제: "JMeter가 설치되지 않았습니다"

**해결방법**:
```bash
# macOS
brew install jmeter

# Linux
sudo apt-get install jmeter
# 또는
sudo yum install jmeter
```

### 문제: "애플리케이션이 실행되지 않았습니다"

**해결방법**:
```bash
# 1. Docker 컨테이너 확인
docker ps

# 2. 포트 사용 확인
lsof -i :8080

# 3. 애플리케이션 로그 확인
tail -f logs/application.log

# 4. 헬스 체크
curl http://localhost:8080/actuator/health
```

### 문제: "Connection refused" 에러

**원인**: API 서버가 실행되지 않았거나 포트가 다름

**해결방법**:
```bash
# 정확한 포트 확인
grep "server.port" smartmealtable-api/src/main/resources/application*.yml

# 올바른 BASE_URL로 실행
BASE_URL=http://localhost:CORRECT_PORT ./run-test.sh
```

### 문제: 높은 에러율 (> 5%)

**원인**:
1. 데이터베이스 연결 풀 부족
2. 메모리 부족
3. 타임아웃

**해결방법**:
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # 증가
      connection-timeout: 30000  # 증가
```

### 문제: 느린 응답 시간 (P95 > 500ms)

**원인**:
1. 인덱스 미설정
2. N+1 쿼리
3. 캐시 미적용

**해결방법**:
1. 인덱스 확인 및 추가:
   ```sql
   CREATE INDEX idx_name_prefix ON store(name(10));
   ```

2. 쿼리 최적화 (QueryDSL):
   ```java
   queryFactory.selectFrom(store)
       .where(store.name.startsWith(keyword))
       .limit(10)
       .fetch();
   ```

3. Redis 캐시 적용:
   ```java
   @Cacheable(value = "autocomplete:store", key = "#keyword")
   public List<StoreResponse> autocomplete(String keyword) { ... }
   ```

### 문제: JMeter가 너무 많은 메모리 사용

**해결방법**:
```bash
# JMeter 힙 크기 증가
export JVM_ARGS="-Xms1024m -Xmx4096m"
./run-test.sh
```

또는 `jmeter` 파일 수정:
```bash
# jmeter 스크립트에서
HEAP="-Xms1g -Xmx4g"
```

## Finance 도메인 성능 자산

- **대용량 데이터 로더**: `smartmealtable-performance` 모듈에서 실행. 사용 방법과 튜닝 포인트는 `docs/performance/PERFORMANCE_TEST_PLAN.md` 참고.
- **SQL 워크로드**: `performance-test/scripts/run-sql-benchmarks.sh`  
  ```bash
  MEMBER_ID=100050 START_DATE=2025-01-01 END_DATE=2025-03-31 \
  performance-test/scripts/run-sql-benchmarks.sh
  ```
  `EXPLAIN ANALYZE`를 한 번에 실행하여 Food/Expenditure/Budget 핵심 쿼리의 계획 및 실행 시간을 수집합니다.
- **API 부하 (k6)**: `performance-test/k6/finance-scenarios.js`  
```bash
JWT_TOKEN=<loader로 생성한 토큰> \
FOOD_KEYWORDS=김치,곱창,버거,라멘,파스타,샐러드,초밥,비건,덮밥,커리 \
RECO_KEYWORDS=김치찌개,초밥,샌드위치,비건,국밥,라면 \
k6 run performance-test/k6/finance-scenarios.js
```
5개의 시나리오(자동완성, 지출 목록/통계, 월/일 예산)를 동시에 주입해 API SLA를 검증합니다.  
`FOOD_KEYWORDS`/`RECO_KEYWORDS`는 콤마로 구분된 prefix 목록입니다. 데이터 분포나 테스트 시나리오에 맞춰 자유롭게 바꿀 수 있으며, 지정하지 않으면 실제 서비스와 비슷한 기본 목록이 사용됩니다.

## 참고 자료

- [Apache JMeter 공식 문서](https://jmeter.apache.org/usermanual/index.html)
- [JMeter Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [성능 테스트 가이드](https://martinfowler.com/articles/performance-testing.html)

## 다음 단계

성능 테스트 완료 후:

1. ✅ **결과 분석**: HTML 리포트에서 모든 지표 확인
2. ✅ **목표 달성 여부 확인**: P50, P95, P99, TPS, Error Rate
3. ✅ **병목 구간 식별**: 느린 API 파악
4. ✅ **최적화 적용**: DB, 캐시, 코드 개선
5. ✅ **재테스트**: 최적화 후 다시 성능 측정
6. ✅ **문서화**: 최종 성능 테스트 결과 보고서 작성

---

**작성일**: 2025-11-10  
**버전**: 1.0.0  
**테스트 대상**: SmartMealTable 자동완성 API v1
