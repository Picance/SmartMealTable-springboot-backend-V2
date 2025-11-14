# Performance Test Plan (Food / Expenditure / Budget)

## 1. Scope
- **Food data**: autocomplete queries, heavy menu catalog browsing.
- **Expenditure data**: list/statistics dashboards over large personal ledgers.
- **Budget data**: monthly/daily/meal budget reconciliation APIs.
- Target DB: `smartmealtable` MySQL schema defined in `ddl.sql`.

## 2. Synthetic Data Loader
Module: `smartmealtable-performance`

### 2.1 Build & run
```bash
# Use JDK 21 for the Gradle runtime
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
PERF_JDBC_URL='jdbc:mysql://localhost:3306/smartmealtable?rewriteBatchedStatements=true' \
PERF_JDBC_USERNAME=root \
PERF_JDBC_PASSWORD=root123 \
./gradlew :smartmealtable-performance:bootRun
```
- Default `runId` is timestamp (e.g., `20250106T1030`). Override with `--loader.run-id=my-load` inside `--args="..."`.
- Loader is multi-threaded; tune via `performance.loader.thread-count` and `performance.loader.batch-size` (see `smartmealtable-performance/src/main/resources/application.yml`).

### 2.2 Key knobs
| Property | Default | Description |
| --- | --- | --- |
| `performance.loader.member.count` | 50,000 | Number of synthetic members. |
| `performance.loader.food.stores` | 5,000 | Stores written before menu insertion. |
| `performance.loader.food.foods-per-store` | 120 | Menus per store (→ 600k rows by default). |
| `performance.loader.budget.monthly-months` | 12 | Number of months of budget history per member. |
| `performance.loader.budget.daily-days` | 120 | Sequential days of daily budgets. |
| `performance.loader.expenditure.records-per-member` | 80 | Receipts per member. |
| `performance.loader.expenditure.generate-items` | false | Flip to `true` to fill `expenditure_item` rows. |

Set properties via env vars (`PERF_MEMBER_COUNT`, `PERF_STORE_COUNT`, etc.) or by editing `application.yml`.

### 2.3 Cleanup & JWT helper
```bash
# Remove data created for a given run id
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
./gradlew :smartmealtable-performance:bootRun --args="--cleanup-run-id=202501061030"

# Generate JWT token for a synthetic member
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
./gradlew :smartmealtable-performance:bootRun --args="--jwt-member-id=100001"
```
Run cleanup **before** reusing the same `runId` to avoid unique-key collisions (stores use `external_id = PERF_<runId>_STORE_*`, members use `nickname = PERF_<runId>_MEM_*`).

## 3. SQL Benchmark Harness
Files:
- `performance-test/sql/core_workloads.sql.tpl`
- `performance-test/scripts/run-sql-benchmarks.sh`

### 3.1 Usage
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
What it does:
1. Expands the template with the provided variables.
2. Runs `EXPLAIN ANALYZE` for
   - Food autocomplete prefix search (b-tree coverage, `food_name` index health).
   - Expenditure pagination query.
   - Expenditure category aggregation.
   - Daily budgets + meal budgets join.
   - Monthly budget snapshot.
3. Results land in the console; redirect to files for baselines.

## 4. API Load Testing (k6)
File: `performance-test/k6/finance-scenarios.js`

### 4.1 Prepare
- Ensure API server is running and points to the DB seeded above.
- Generate JWT token via loader CLI and export `JWT_TOKEN`.

### 4.2 Run
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
Scenarios included:
- `food_autocomplete`: unauthenticated, high-RPS suggestion traffic.
- `expenditure_list`: authenticated timeline pagination.
- `expenditure_stats`: statistics API (category + meal type).
- `budget_monthly`: monthly summary endpoint.
- `budget_daily`: daily + meal budget breakdown.

Tune via env vars (`AUTOCOMPLETE_RPS`, `EXPENDITURE_RPS`, `TEST_DURATION`, etc.). Thresholds already encoded in the script (p95 SLA per workload).

## 5. Observability Checklist
- Capture DB metrics (`performance_schema`, `SHOW ENGINE INNODB STATUS`) while scripts run.
- For API runs, enable Spring Boot actuator metrics or ship traces to the existing monitoring stack (`smartmealtable-support:monitoring`).
- Retain console outputs from SQL harness and k6 summary (`k6 --summary-export`).

## 6. Next Steps
1. Hook loader execution into CI/CD (nightly job) if large fixtures are required frequently.
2. Extend `expenditure_item` generation to reference actual `food_id` rows when `generate-items=true`.
3. Plug the new k6 plan into the existing `performance-test/run-test.sh` orchestrator or Github Actions.
4. Add DB snapshots (mysqldump) after each run for rollback/testing.
