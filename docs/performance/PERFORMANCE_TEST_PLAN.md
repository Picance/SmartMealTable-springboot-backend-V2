# 성능 테스트 계획 (음식 / 지출 / 예산)

## 1. 범위
- **음식 데이터**: 자동 완성 쿼리, 대용량 메뉴 카탈로그 탐색.
- **지출 데이터**: 대규모 개인 원장에 대한 목록/통계 대시보드.
- **예산 데이터**: 월별/일별/끼니별 예산 조정 API.
- 대상 DB: `ddl.sql`에 정의된 `smartmealtable` MySQL 스키마.

## 2. 합성 데이터 로더
모듈: `smartmealtable-performance`

### 2.1 빌드 및 실행
```bash
# Gradle 런타임으로 JDK 21 사용
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
PERF_JDBC_URL='jdbc:mysql://localhost:3306/smartmealtable?rewriteBatchedStatements=true' \
PERF_JDBC_USERNAME=root \
PERF_JDBC_PASSWORD=root123 \
./gradlew :smartmealtable-performance:bootRun
```
- 기본 `runId`는 타임스탬프입니다 (예: `20250106T1030`). `--args="..."` 내에 `--loader.run-id=my-load`를 사용하여 재정의할 수 있습니다.
- 로더는 멀티 스레드로 동작합니다; `performance.loader.thread-count`와 `performance.loader.batch-size`를 통해 조정하세요 (`smartmealtable-performance/src/main/resources/application.yml` 참조).

### 2.2 주요 설정값
| 속성 | 기본값 | 설명 |
| --- | --- | --- |
| `performance.loader.member.count` | 50,000 | 합성 회원 수. |
| `performance.loader.food.stores` | 5,000 | 메뉴 삽입 전 기록될 가게 수. |
| `performance.loader.food.foods-per-store` | 120 | 가게당 메뉴 수 (→ 기본적으로 60만 행). |
| `performance.loader.budget.monthly-months` | 12 | 회원별 월별 예산 기록 개월 수. |
| `performance.loader.budget.daily-days` | 120 | 연속된 일별 예산 일수. |
| `performance.loader.expenditure.records-per-member` | 80 | 회원별 영수증 수. |
| `performance.loader.expenditure.generate-items` | false | `expenditure_item` 행을 채우려면 `true`로 변경. |

환경 변수(`PERF_MEMBER_COUNT`, `PERF_STORE_COUNT` 등)를 통하거나 `application.yml`을 편집하여 속성을 설정하세요.

### 2.3 데이터 정리 및 JWT 헬퍼
```bash
# 특정 실행 ID로 생성된 데이터 제거
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
./gradlew :smartmealtable-performance:bootRun --args="--cleanup-run-id=202501061030"

# 합성 회원을 위한 JWT 토큰 생성
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
./gradlew :smartmealtable-performance:bootRun --args="--jwt-member-id=100001"
```
동일한 `runId`를 재사용하기 **전에** 데이터 정리를 실행하여 고유 키 충돌을 피하세요 (가게는 `external_id = PERF_<runId>_STORE_*`, 회원은 `nickname = PERF_<runId>_MEM_*` 사용).

## 3. SQL 벤치마크 하네스
파일:
- `performance-test/sql/core_workloads.sql.tpl`
- `performance-test/scripts/run-sql-benchmarks.sh`

### 3.1 사용법
```bash
DB_HOST=127.0.0.1 \
DB_USER=root \
DB_PASSWORD=root123 \
MEMBER_ID=100050 \
START_DATE=2025-01-01 \
END_DATE=2025-03-31 \
FOOD_PREFIX=PERF \
BUDGET_MONTH=2025-02 \
DAILY_DATE=2025-02-15 \
performance-test/scripts/run-sql-benchmarks.sh
```
수행하는 작업:
1. 제공된 변수로 템플릿을 확장합니다.
2. 다음 항목에 대해 `EXPLAIN ANALYZE`를 실행합니다.
    - 음식 자동 완성 접두어 검색 (b-tree 커버리지, `food_name` 인덱스 상태).
    - 지출 내역 페이지네이션 쿼리.
    - 지출 내역 카테고리 집계.
    - 일별 예산 + 끼니별 예산 조인.
    - 월별 예산 스냅샷.
3. 결과는 콘솔에 출력됩니다; 베이스라인을 위해 파일로 리디렉션하세요.

## 4. API 부하 테스트 (k6)
파일: `performance-test/k6/finance-scenarios.js`

### 4.1 준비
- API 서버가 실행 중이고 위에서 시딩된 DB를 가리키고 있는지 확인하세요.
- 로더 CLI를 통해 JWT 토큰을 생성하고 `JWT_TOKEN`을 export 하세요.

### 4.2 실행
```bash
JWT_TOKEN="$(JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :smartmealtable-performance:bootRun --args="--jwt-member-id=100050" -q | tail -n1)"
BASE_URL=http://localhost:8080 \
START_DATE=2025-01-01 \
END_DATE=2025-03-31 \
DAILY_DATE=2025-02-15 \
BUDGET_YEAR=2025 \
BUDGET_MONTH=02 \
k6 run performance-test/k6/finance-scenarios.js
```
포함된 시나리오:
- `food_autocomplete`: 인증되지 않은, 높은 RPS의 추천 트래픽.
- `expenditure_list`: 인증된 타임라인 페이지네이션.
- `expenditure_stats`: 통계 API (카테고리 + 끼니 유형).
- `budget_monthly`: 월별 요약 엔드포인트.
- `budget_daily`: 일별 + 끼니별 예산 상세 내역.

환경 변수(`AUTOCOMPLETE_RPS`, `EXPENDITURE_RPS`, `TEST_DURATION` 등)를 통해 조정하세요. 임계값은 스크립트에 이미 인코딩되어 있습니다 (워크로드별 p95 SLA).

## 5. 관측 가능성 체크리스트
- 스크립트 실행 중 DB 메트릭(`performance_schema`, `SHOW ENGINE INNODB STATUS`)을 캡처하세요.
- API 실행 시, Spring Boot 액추에이터 메트릭을 활성화하거나 기존 모니터링 스택(`smartmealtable-support:monitoring`)으로 트레이스를 전송하세요.
- SQL 하네스의 콘솔 출력과 k6 요약 정보(`k6 --summary-export`)를 보관하세요.

## 6. 다음 단계
1. 대규모 픽스처가 자주 필요한 경우 로더 실행을 CI/CD(야간 작업)에 연결하세요.
2. `generate-items=true`일 때 `expenditure_item` 생성이 실제 `food_id` 행을 참조하도록 확장하세요.
3. 새로운 k6 계획을 기존 `performance-test/run-test.sh` 오케스트레이터나 Github Actions에 연결하세요.
4. 롤백/테스트를 위해 각 실행 후 DB 스냅샷(mysqldump)을 추가하세요.
