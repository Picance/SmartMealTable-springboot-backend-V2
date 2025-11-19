# 🚀 SmartMealTable 성능 테스트 가이드

## 👋 시작하기 전에

안녕하세요! 이 문서는 **추천 API**, **지출 내역 CRUD**, **예산 CRUD**가 엄청난 데이터 양에서도 버텨주는지 확인하기 위한 실전형 성능 테스트 가이드입니다. 이번 테스트는 다음과 같은 현실적인 규모를 상정합니다.

### 📦 목표 데이터 규모
| 데이터 유형 | 목표 볼륨 | 구성 팁 |
| --- | --- | --- |
| 가게 (food_store) | 1,000,000건 | `PERF_STORE_COUNT`를 1,000,000으로 설정 |
| 음식 (food) | 10,000,000건 이상 | `PERF_FOODS_PER_STORE=12` 정도로 잡으면 1M × 12 = 12M |
| 지출 내역 (expenditure) | 10,000,000건 이상 | `PERF_MEMBER_COUNT × recordsPerMember` 조합으로 확보 |
| 예산 (monthly/daily/meal) | 일별 2,500만 건 / 식사별 1억 건 이하 | `PERFORMANCE_LOADER_BUDGET_MONTHLY_MONTHS`와 `..._DAILY_DAYS`를 조율 |

> 💡 **권장 목표조합**: 회원 250k × 지출 120건 = 30M, 일별 예산 250k × 100일 = 25M → meal_budget은 4배(식사 유형)여도 1억 건 언더를 유지할 수 있습니다.

---

## 🛠 1단계: 로컬 환경 올리기 (docker-compose)

### 1.1 필수 설치물
- **Java 21 (JDK)** – `./gradlew`와 성능 로더 실행용
- **Docker Desktop & Docker Compose** – `docker-compose.local.yml`을 실행하기 위함
- **k6** – API 부하 생성기 (`brew install k6`)
- **MySQL CLI & jq** – 쿼리 검증 및 JSON 파싱용 (선택)

### 1.2 local-dev.sh로 인스턴스 기동하기
```bash
./local-dev.sh start      # MySQL, Redis, API, Admin, Scheduler, Grafana, Prometheus
./local-dev.sh status     # 포트, 헬스체크, 네트워크 상태 확인
# 필요 시
./local-dev.sh logs       # docker-compose 전체 로그
./local-dev.sh stop       # 테스트 종료 시 정리
```
- 모든 서비스는 `docker-compose.local.yml` 기준으로 기동하며, MySQL 스키마는 `ddl.sql`이 자동 적용됩니다.
- 기본 접속 정보: `smartmeal_user / smartmeal123` (`localhost:3306`).

### 1.3 주요 엔드포인트 요약
| 서비스 | 주소 | 비고 |
| --- | --- | --- |
| MySQL | `localhost:3306` | `smartmealtable`, UTF-8 |
| Redis | `localhost:6379` | 캐시 / 세션 |
| API 서버 | `http://localhost:8080` | `JWT_SECRET`은 `.env` 참고 |
| Admin | `http://localhost:8081` | 운영 툴 |
| Scheduler | `http://localhost:8082` | 배치 작업 |
| Grafana | `http://localhost:3000` | `admin/admin123` |
| Prometheus | `http://localhost:9090` | 성능 메트릭 |

---

## 📦 2단계: 테스트 데이터 준비하기

### 2.1 데이터베이스 초기화 확인
- `local-dev.sh`가 자동으로 `ddl.sql`/`dml.sql`을 적용합니다.
- 필요하다면 다음처럼 스키마를 재적용할 수 있습니다.
```bash
mysql -h 127.0.0.1 -u smartmeal_user -psmartmeal123 smartmealtable < ddl.sql
```

### 2.2 대용량 로더 설정하기
`smartmealtable-performance` 모듈은 환경 변수만 변경해도 즉시 다른 규모의 데이터를 생성할 수 있습니다.

| 환경 변수 | 설명 | 권장 값 (예시) |
| --- | --- | --- |
| `PERFORMANCE_LOADER_RUN_ID` | 생성/삭제 추적용 Run ID | `scale-2025Q1` |
| `PERF_THREAD_COUNT` | 동시 Insert 쓰레드 수 | `32` (CPU 코어에 맞춰 조정) |
| `PERF_BATCH_SIZE` | JDBC batch 크기 | `2000` |
| `PERF_MEMBER_COUNT` | Synthetic 회원 수 | `250000` |
| `PERF_STORE_COUNT` | 가게 수 | `1000000` |
| `PERF_FOODS_PER_STORE` | 가게별 메뉴 수 | `12` (→ 12M 메뉴) |
| `PERFORMANCE_LOADER_EXPENDITURE_RECORDS_PER_MEMBER` | 회원당 지출 기록 수 | `120` (→ 30M 지출) |
| `PERFORMANCE_LOADER_BUDGET_MONTHLY_MONTHS` | 월별 예산 개월 수 | `36` (3년) |
| `PERFORMANCE_LOADER_BUDGET_DAILY_DAYS` | 일별 예산 일수 | `100` (≈ 2,500만 일일 예산 → meal_budget 약 1억) |
| `PERF_JDBC_URL` | 대상 DB | `jdbc:mysql://localhost:3306/smartmealtable?...` |
| `PERF_JDBC_USERNAME` / `PERF_JDBC_PASSWORD` | DB 인증 | `smartmeal_user / smartmeal123` |

> ⚠️ **주의**: 30M 이상의 데이터를 생성하면 MySQL 데이터 디렉터리가 수십 GB 이상 커집니다. Docker Desktop의 디스크 용량을 반드시 확인하세요.
> 💾 **meal_budget 캡**: `PERFORMANCE_LOADER_BUDGET_DAILY_DAYS` 값을 100 이하로 잡으면 4개 식사 유형을 모두 생성해도 1억 건 언더를 유지합니다. 더 작게 유지하고 싶다면 `member` 수나 `daily_days`를 비례해 줄이세요.

### 2.3 데이터 로더 실행
```bash
export PERFORMANCE_LOADER_RUN_ID=scale-2025Q1
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
PERF_JDBC_URL='jdbc:mysql://localhost:3306/smartmealtable?rewriteBatchedStatements=true&characterEncoding=UTF-8' \
PERF_JDBC_USERNAME=smartmeal_user \
PERF_JDBC_PASSWORD=smartmeal123 \
PERF_THREAD_COUNT=32 \
PERF_BATCH_SIZE=2000 \
PERF_MEMBER_COUNT=250000 \
PERF_STORE_COUNT=1000000 \
PERF_FOODS_PER_STORE=12 \
PERFORMANCE_LOADER_EXPENDITURE_RECORDS_PER_MEMBER=120 \
PERFORMANCE_LOADER_BUDGET_MONTHLY_MONTHS=36 \
PERFORMANCE_LOADER_BUDGET_DAILY_DAYS=100 \
./gradlew :smartmealtable-performance:bootRun
```
- 로그 마지막에 각 데이터셋별 row 수와 소요 시간이 표로 출력됩니다.
- Run ID는 `PERF_<runId>_*` 형태로 모든 테스트 데이터에 각인되므로, cleanup 및 카운팅이 쉬워집니다.
- 최근 로더는 매장/음식 이름을 실제 서비스와 비슷한 한국어·영어 조합으로 생성하므로, 자동완성/검색 인덱스 테스트에 활용할 수 있는 다양한 prefix가 자동으로 채워집니다. 별도의 CSV를 준비하지 않아도 고유 문자열 분포가 확보됩니다.

### 2.4 데이터 검증 SQL
아래 쿼리로 목표치에 도달했는지 확인하세요 (`scale-2025Q1` Run ID 기준).
```sql
SELECT COUNT(*) FROM member WHERE nickname LIKE 'PERF_scale-2025Q1_MEM_%';
SELECT COUNT(*) FROM store WHERE external_id LIKE 'PERF_scale-2025Q1_STORE_%';
SELECT COUNT(*) FROM food WHERE name LIKE 'PERF_scale-2025Q1_FOOD_%';
SELECT COUNT(*) FROM expenditure WHERE memo LIKE 'PERF_scale-2025Q1_EXP_%';
SELECT COUNT(*) FROM monthly_budget;
SELECT COUNT(*) FROM daily_budget;
```
- 기대치 예시: `food_store` 1,000,000건, `food` 12,000,000건, `expenditure` 약 30,000,000건, `daily_budget` 25,000,000건.
- `SELECT member_id FROM member WHERE nickname LIKE 'PERF_scale-2025Q1_MEM_%' LIMIT 5;`로 테스트에 사용할 회원을 미리 확보합니다.

### 2.5 모니터링 팁
- `./local-dev.sh logs mysql`로 InnoDB 경고를 확인합니다.
- `docker stats smartmealtable-mysql`로 I/O 병목을 파악합니다.
- 필요 시 `SET GLOBAL innodb_buffer_pool_size=...`를 조정해 캐시 적중률을 올립니다.

---

## 🔍 3단계: 데이터베이스 쿼리 성능 테스트
`performance-test/scripts/run-sql-benchmarks.sh`는 대량 데이터에서도 주요 쿼리가 인덱스를 타는지 검증합니다.

```bash
MEMBER_ID=200010 \
START_DATE=2024-10-01 \
END_DATE=2025-03-31 \
FOOD_PREFIX=PERF_scale-2025Q1 \
BUDGET_MONTH=2025-02 \
DAILY_DATE=2025-02-15 \
DB_HOST=127.0.0.1 \
DB_USER=smartmeal_user \
DB_PASSWORD=smartmeal123 \
performance-test/scripts/run-sql-benchmarks.sh
```
- `MEMBER_ID`는 앞 단계에서 확보한 PERF 회원 중 하나를 사용합니다.
- 출력에는 실행 시간/스캔 행 수/사용 인덱스가 기재됩니다. 10M 건 이상의 테이블에서도 **range scan**을 유지하는 것이 목표입니다.
- 느린 쿼리가 보이면 즉시 `EXPLAIN`을 실행하여 필터 조건과 인덱스 사용 여부를 확인하세요.

---

## 🌐 4단계: API 부하 테스트 (추천 + CRUD)

### 4.1 API 컨테이너 준비 & JWT 발급
- `./local-dev.sh status`로 `api:8080`이 Healthy인지 확인하고, 필요 시 `./local-dev.sh logs`로 warm-up 상태를 체크합니다.
- PERF 회원 ID로 JWT를 발급합니다.
```bash
JWT_TOKEN="$(JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :smartmealtable-performance:bootRun --args="--jwt-member-id=200010" -q | tail -n1)"
echo $JWT_TOKEN
```

### 4.2 시나리오 구성
`performance-test/k6/finance-scenarios.js`는 아래 표의 시나리오를 동시에 실행합니다.

| 시나리오 | 엔드포인트 | 목적 | 기본 RPS / p95 목표 |
| --- | --- | --- | --- |
| `food_autocomplete` | `GET /api/v1/foods/autocomplete` | 대용량 메뉴 인덱스 검증 | 75 RPS / < 250ms |
| `recommendations` | `GET /api/v1/recommendations` | 1M 가게 + 10M 음식 추천 품질 및 캐싱 검증 | 80 RPS / < 600ms |
| `expenditure_list` | `GET /api/v1/expenditures` | 30M 지출 목록 페이징 | 40 RPS / < 450ms |
| `expenditure_stats` | `GET /api/v1/expenditures/statistics` | 카테고리/일자 집계 | 20 RPS / < 450ms |
| `budget_monthly` | `GET /api/v1/budgets/monthly` | 월 예산 조회 | 20 RPS / < 350ms |
| `budget_daily` | `GET /api/v1/budgets/daily` | 일 예산 조회 | 20 RPS / < 350ms |
| `expenditure_crud` | `POST/PUT/DELETE /api/v1/expenditures` | CRUD 체인(생성→수정→삭제) | 25 RPS / < 650ms (create) |
| `budget_monthly_update` | `PUT /api/v1/budgets` | 월 예산 수정 | 15 RPS / < 400ms |
| `budget_daily_update` | `PUT /api/v1/budgets/daily/{date}` | 일 예산 수정/일괄 적용 | 15 RPS / < 400ms |
| `budget_monthly_create` | `POST /api/v1/budgets/monthly` | 신규 월 예산 등록 (미래 월) | 5 RPS / < 550ms |

> 🧩 CRUD 시나리오 특성:
> - `expenditure_crud`는 생성된 지출을 즉시 수정/삭제하여 테이블 크기를 유지합니다.
> - `budget_monthly_create`는 2090년 이후의 미래 월을 랜덤으로 사용하므로 기존 데이터와 충돌하지 않습니다 (409 응답은 이미 동일 월이 존재한다는 뜻으로, 로드 테스트 중 허용됩니다).

#### 4.2.1 키워드 추천 전략 재검토
`food_autocomplete` 시나리오는 현재 **가게/음식 이름의 모든 substring을 테이블에 저장한 뒤 `LIKE`로 탐색**하는 방식입니다. 이 구조는 자동완성에는 유효하지만, 실제 사용자가 원하는 “키워드 추천” 품질을 높이기에는 아래 한계가 있습니다.

- **유연성 부족**: substring으로만 추천을 만들면 실제로 많이 입력된 키워드/조합을 반영하기 어렵습니다. “치즈돈까스”가 많이 검색·클릭됐더라도, 이름에 해당 문자열이 없으면 추천 목록에 올리지 못합니다. 사용자 행동 로그를 활용한 랭킹과는 거리가 있습니다.
- **데이터 폭발**: 모든 substring을 저장하면 데이터와 인덱스가 기하급수적으로 커지고, 조인·스캔 비용도 상승합니다. prefix 선택도가 낮은 키워드에서는 범위가 크게 잡혀 latency가 튈 수 있습니다.
- **추천 품질 저하**: 단순 substring 기반 추천은 “인기 키워드가 무엇인지”, “현재 지역/시즌에서 많이 찾는 조합인지”를 알려주지 못해 연관어·유사어·실시간 트렌드를 놓칩니다.

추천 품질과 비용을 동시에 잡으려면 **데이터 기반 접근**으로 전환하는 것이 좋습니다.

1. **검색 로그 기반 랭킹**  
   - `search_keyword`, `member_id`, `clicked_food_id`, `created_at` 등을 남기고, 일정 주기로 Top-N 키워드를 산출합니다.  
   - Prefix 검색은 Trie, Redis Sorted Set(ZSET), ElasticSearch Prefix Query 중 하나로 구현해 랭크를 유지합니다.  
   - `k6` 시나리오에서는 실제 인기 키워드 샘플을 기반으로 RPS를 조정하면서 캐시/Sorted Set hit ratio를 관찰합니다.

2. **카테고리·태그·지역 메타데이터 기반 추천**  
   - 음식 카테고리, 해시태그, 상권/지역 등 정형 메타데이터에서 인기 키워드를 추출해 prefix 인덱스에 주입합니다.  
   - 시즌/이벤트(예: 여름 냉면, 지역 축제)처럼 정적 substring에서 포착하기 어려운 테마를 사전에 push할 수 있습니다.  
   - 성능 테스트 시에는 `FOOD_KEYWORDS`/`RECO_KEYWORDS` 값을 카테고리·지역별 트래픽 비율에 맞춰 조정하면 더 현실적인 부하 분포를 만들 수 있습니다.

이 두 가지를 조합하면 substring 테이블을 완전히 제거하거나, 최소한 “fallback 용도”로만 사용하면서 트래픽과 스토리지 비용을 줄일 수 있습니다.

#### 4.2.2 Redis + MySQL 기반 키워드 추천 계획
ElasticSearch 같은 추가 인프라 없이도 **MySQL + Redis**만으로 랭킹형 키워드 추천을 운영할 수 있도록 다음 단계를 권장합니다.

1. **로그 스키마 설계 (MySQL)**  
   - 테이블 예시: `search_keyword_event (id BIGINT PK, member_id BIGINT, raw_keyword VARCHAR(100), normalized_keyword VARCHAR(60), clicked_food_id BIGINT, lat DOUBLE, lng DOUBLE, created_at DATETIME(3))`.  
   - 인덱스: `(created_at, normalized_keyword)`, `(normalized_keyword, lat_bucket, lng_bucket)` 정도를 두어 시간/지역 단위 집계를 빠르게 합니다.  
   - API는 사용자가 입력 완료하거나 추천을 클릭할 때마다 해당 테이블에 Append-Only로 적재합니다 (배치 Insert 또는 Redis Stream → MySQL 적재도 가능).

2. **주기적 집계 (MySQL)**  
   - 1시간 혹은 1일 주기로 `INSERT INTO keyword_popularity_daily (event_date, prefix, keyword, searches, clicks)` 같은 요약 테이블을 만듭니다.  
   - 집계 SQL 예시:
     ```sql
     INSERT INTO keyword_popularity_hourly (event_hour, prefix, keyword, search_cnt, click_cnt)
     SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00'), LEFT(normalized_keyword, 2), normalized_keyword,
            COUNT(*), SUM(clicked_food_id IS NOT NULL)
     FROM search_keyword_event
     WHERE created_at BETWEEN ? AND ?
     GROUP BY event_hour, prefix, normalized_keyword;
     ```
   - 필요하면 `region_code` 또는 `category_id`를 조인해 지역·카테고리별 랭킹도 동시에 산출합니다.

3. **Redis 제공 레이어 구성**  
   - Key 설계: `keyword:prefix:{region}` → Sorted Set. Score는 가중치(예: `search_cnt * 0.7 + click_cnt * 1.3`).  
   - 집계 배치가 끝나면 상위 N개(예: 200개)를 Redis ZSET에 `ZADD`로 밀어넣고 TTL(예: 2h)을 설정해 stale 데이터를 자동 교체합니다.  
   - Prefix는 1~3글자까지만 관리하고, 나머지는 Redis `ZRANGE` 결과에서 필터링합니다. Prefix가 없으면 `keyword:global` ZSET을 사용합니다.

4. **서빙 경로**  
   - API는 입력 prefix → 지역 → Redis ZSET `ZRANGE`로 즉시 응답 (p99 < 5ms 예상).  
   - Redis 미스 시에는 MySQL `keyword_popularity_daily` 테이블에서 최근 Top-N을 읽고 Redis에 warm-up한 뒤 응답합니다.  
   - 기존 substring 테이블은 fallback 으로 남겨 급격한 신조어/미집계 키워드에 대응하되, Redis Top-N과 Merge할 때는 중복 제거 + 랭킹 score 비교를 합니다.

5. **성능/품질 검증**  
   - `k6`에서 `FOOD_KEYWORDS` 목록을 “Redis 상위 20개 + Long-tail 5개”로 분리해 히트율을 측정합니다.  
   - `ZCARD`, `INFO memory` 등을 통해 Redis 메모리 사용량을 기록하고, 집계 배치를 `smartmealtable-scheduler`에 넣어 5분 단위로 실행했을 때 MySQL CPU/IO가 허용 내인지 확인합니다.  
   - KPI: Redis 요청 hit ratio > 95%, `food_autocomplete` 응답 p95 < 150ms (Redis + API 처리 시간만 포함).

### 4.3 k6 실행 (대규모 설정 예시)
```bash
JWT_TOKEN="$JWT_TOKEN" \
BASE_URL=http://localhost:8080 \
TEST_DURATION=180s \
RECOMMEND_RPS=120 \
RECOMMEND_VUS=45 \
RECO_LATITUDE=37.498 \
RECO_LONGITUDE=127.028 \
RECO_KEYWORDS=김치찌개,초밥,샌드위치,비건 \
FOOD_KEYWORDS=김치,곱창,버거,라멘,파스타,샐러드,초밥,비건,덮밥,커리 \
EXPENDITURE_RPS=90 \
EXPENDITURE_VUS=35 \
EXP_CRUD_RPS=60 \
EXP_CRUD_VUS=30 \
EXP_CRUD_CATEGORY_ID=5 \
BUDGET_MONTHLY_RPS=35 \
BUDGET_DAILY_RPS=30 \
BUDGET_UPDATE_RPS=25 \
BUDGET_DAILY_UPDATE_RPS=25 \
BUDGET_CREATE_RPS=8 \
BUDGET_DAILY_DATES=2025-02-15,2025-02-16,2025-02-17,2025-02-18 \
AUTOCOMPLETE_RPS=120 \
AUTOCOMPLETE_VUS=40 \
k6 run performance-test/k6/finance-scenarios.js
```
- `RECO_KEYWORD_RATIO`, `EXP_CRUD_MIN_AMOUNT`, `BUDGET_CREATE_BASE_YEAR` 등 세부 변수도 필요에 따라 조정 가능합니다.
- `FOOD_KEYWORDS` / `RECO_KEYWORDS`는 기본적으로 다양한 실제 음식명을 포함하도록 설정되어 있습니다. 특정 데이터셋이나 테스트 케이스에 맞춰 prefix/substring 분포를 조정하고 싶다면 콤마로 구분된 문자열을 덮어써 주세요. (예: `FOOD_KEYWORDS=김밥,비빔밥,돈까스`)
- 실행 중 `k6`는 `recommendation_duration_ms`, `expenditure_create_duration_ms` 등 커스텀 Trend를 출력합니다.

### 4.4 결과 해석 가이드
- **주요 지표**: 각 Trend의 `p(95)` / `p(99)`, `http_req_failed`, `iteration_duration`.
- **권장 기준**
  - 추천 API: `recommendation_duration_ms p(95) < 600ms`, 오류율 < 1%
  - 지출 CRUD: `create < 650ms`, `update < 500ms`, `delete < 350ms`
  - 예산 CRUD: `budget_monthly_update_duration_ms p(95) < 400ms`, `budget_daily_update_duration_ms < 400ms`, `budget_monthly_create_duration_ms < 550ms`
  - 조회 계열: 모두 < 450ms 유지
- `k6 run ... --summary-export k6-summary.json`로 결과를 JSON으로 내보내 Grafana나 BigQuery에 적재할 수 있습니다.

### 4.5 CRUD 후 검증
- `expenditure` 테이블이 폭증하지 않는지 확인합니다.
```sql
SELECT COUNT(*) FROM expenditure WHERE memo LIKE 'PERF-CRUD%';  -- 수천 건 이하 유지가 정상
```
- 미래 월 예산이 잘 쌓이는지 확인합니다.
```sql
SELECT budget_month, COUNT(*) FROM monthly_budget WHERE budget_month LIKE '21__-%' GROUP BY budget_month ORDER BY budget_month DESC LIMIT 5;
```
- API 로그(`./local-dev.sh logs`)에서 4xx, 5xx 비율을 함께 확인하세요.

---

## ✅ 5단계: 결과 분석 & 튜닝 체크리스트
- **인덱스 확인**: `EXPLAIN ANALYZE`로 추천/지출/예산 쿼리가 전부 covering index를 탔는지 확인합니다.
- **DB 리소스**: `SHOW ENGINE INNODB STATUS\G`, `SHOW GLOBAL STATUS LIKE 'Threads_connected';`로 커넥션 / 잠금 병목을 파악합니다.
- **애플리케이션**: Spring Actuator (`/actuator/metrics`, `/actuator/prometheus`)에서 Hikari Pool 대기 시간과 GC 시간을 모니터링합니다.
- **캐시 전략**: 추천 API가 600ms를 넘기면 Redis 캐시 키 만료 정책이나 Geo 쿼리 인덱스를 재점검합니다.
- **확장 실험**: `RECOMMEND_RPS`와 `EXP_CRUD_RPS`를 1.5배씩 올리며 브레이크 포인트를 찾고, 병목 여부를 기록합니다.

---

## 🧹 6단계: 테스트 데이터 정리하기
PERF Run ID 단위로 데이터를 삭제할 수 있습니다.
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
./gradlew :smartmealtable-performance:bootRun --args="--cleanup-run-id=${PERFORMANCE_LOADER_RUN_ID:-scale-2025Q1}"
```
- 삭제 전후로 `SELECT COUNT(*) ...`를 실행해 잔여 데이터가 없는지 확인하세요.
- MySQL 볼륨을 완전히 비우려면 `./local-dev.sh clean`으로 컨테이너와 볼륨을 정리합니다 (⚠️ 모든 데이터 삭제).

---

## ❓ 자주 묻는 질문

**Q1. local-dev.sh가 MySQL 포트 충돌로 실패합니다.**  
A1. `lsof -i :3306`로 기존 프로세스를 종료한 뒤 재시도하거나, `docker-compose.local.yml`의 포트를 비워둔 대역으로 수정하세요.

**Q2. 데이터 로더가 중간에 OOM으로 종료됩니다.**  
A2. `PERF_THREAD_COUNT`를 절반으로 줄이고, `PERF_BATCH_SIZE=1000`으로 낮춰 여러 번 나눠 실행하세요. Run ID만 다르게 주면 병합이 가능합니다.

**Q3. k6 실행 시 401 오류가 발생합니다.**  
A3. JWT 토큰이 만료되었거나 회원 ID가 존재하지 않는 경우입니다. 새 PERF 회원 ID로 5분 이내에 토큰을 다시 발급하세요.

**Q4. `budget_monthly_create` 시나리오에서 409가 자주 뜹니다.**  
A4. 이미 생성된 미래 월이 겹친 경우이며, 데이터 무결성 상 정상입니다. 필요한 경우 `BUDGET_CREATE_BASE_YEAR`를 더 큰 숫자로 올려 충돌 확률을 낮출 수 있습니다.

**Q5. 로컬 Docker 자원이 부족합니다.**  
A5. Docker Desktop → Settings → Resources에서 Memory 12GB+, Disk 80GB+를 확보하세요. 혹은 MySQL만 물리 머신에서 돌리고 `PERF_JDBC_URL`을 해당 호스트로 변경하세요.

---

성공적인 대용량 테스트를 기원합니다! 🙌 새로운 기능을 추가할 때마다 이 가이드를 반복해 성능 회귀를 빠르게 잡아내세요.
