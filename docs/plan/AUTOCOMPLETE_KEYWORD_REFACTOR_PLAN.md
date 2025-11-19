# Autocomplete Keyword Refactor Plan (MySQL + Redis)

**작성일**: 2025-11-12  
**담당 모듈**: `smartmealtable-api`, `smartmealtable-scheduler`, `smartmealtable-core`  
**대상 API**: `{{local}}/api/v1/autocomplete?keyword=키워드&limit=10` (스펙/응답 포맷 절대 변경 금지)

---

## 🎯 목표
- AWS 외 추가 인프라 도입 없이 **MySQL + Redis**만으로 인기/연관 키워드 추천 품질을 끌어올린다.
- substring 테이블 기반 `LIKE` 검색을 제거하거나 fallback 으로 축소해 **저장소 폭증**과 **긴 tail latency**를 해소한다.
- 실사용자 검색/클릭 로그를 저장·집계해 **키워드 트렌드**를 반영하고, Redis 정렬 집합을 통해 서빙 지연을 150ms 이하로 유지한다.
- 기존 호출부와 클라이언트는 `{{local}}/api/v1/autocomplete` API 를 그대로 사용한다.

---

## ✅ 범위 & 산출물
1. **데이터 로깅**
   - API 호출 시 `search_keyword_event` 테이블에 검색/클릭 이벤트를 Append-Only 로 기록.
   - Lat/Lng, 회원 ID, 추천 결과 클릭 ID 등 추후 가중치 계산에 필요한 필드를 포함.
2. **집계 파이프라인**
   - `keyword_popularity_hourly`, `keyword_popularity_daily` 요약 테이블과 Materialized View 성격의 Redis ZSET을 생성.
   - `smartmealtable-scheduler` 배치 잡으로 5분/1시간 단위 집계.
3. **Redis 서빙 계층**
   - Prefix/지역별 Sorted Set 키 설계 (`keyword:prefix:{region}`).
   - Warm-up/TTL 정책 및 fallback 전략 문서화.
4. **API 내부 리팩토링**
   - Service 레이어에서 Redis → MySQL → Legacy substring 순으로 Multi-stage 조회.
   - 스펙은 그대로 유지하되, 내부 Score/정렬/필터만 변경.
5. **테스트 & 모니터링**
   - 단위/통합 테스트, k6 부하 구성, 관측 지표 대시보드 정의.

---

## 🔒 제약사항 & 고려사항
- **인프라 제한**: Redis, MySQL 이외의 검색 엔진(ES, OpenSearch 등) 미도입.
- **API 호환성**: 기존 쿼리 파라미터와 JSON 응답을 100% 유지. (추가 필드가 필요하면 optional 로 뒤에 붙이되 기본 응답 구조는 동일.)
- **데이터 민감도**: 검색 로그는 90일 rolling window 만 유지, PII 최소화 (member_id 는 해시 가능한 surrogate key 사용 고려).
- **데이터 일관성**: Redis TTL 및 백필 로직으로 stale 데이터 대비.

---

## 🧱 현재 문제 요약
| 문제 | 영향 | 원인 |
| --- | --- | --- |
| substring 테이블 폭증 | 테이블/인덱스 수십 GB, 배포 시 마이그레이션 지연 | 모든 substring 저장 |
| Prefix 선택도 낮을 때 p95 800ms | `LIKE '%foo%'` range scan 폭증 | 인기 키워드가 아닌 경우 전체 테이블 탐색 |
| 추천 품질 낮음 | 실제 인기 키워드 반영 어려움 | 정적 이름 기반 |
| 로그/관측 부재 | 추천 품질/성능 분석 어려움 | 검색 이벤트 저장 안함 |

---

## 🏗️ 타깃 아키텍처 (텍스트 다이어그램)
```
사용자 입력 → API (keyword, limit)
  ├─> (Stage 1) Redis ZSET: keyword:prefix:{region}
  │      ├─ Hit → 최종 응답 (정렬된 Top-N)
  │      └─ Miss → Stage 2
  ├─> (Stage 2) MySQL keyword_popularity_daily
  │      ├─ Hit → Redis warm-up + 응답
  │      └─ Miss → Stage 3
  └─> (Stage 3) Legacy substring table (fallback)

배치 경로:
search_keyword_event (Raw) ─> Aggregation SQL ─> keyword_popularity_* (MySQL) ─> Redis ZSET + TTL
```

---

## 📦 데이터 모델 설계

### 1. Raw Event
```sql
CREATE TABLE search_keyword_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NULL,
  raw_keyword VARCHAR(100) NOT NULL,
  normalized_keyword VARCHAR(60) NOT NULL,
  clicked_food_id BIGINT NULL,
  lat DECIMAL(10,7) NULL,
  lng DECIMAL(10,7) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB;

CREATE INDEX idx_ske_created_keyword ON search_keyword_event (created_at, normalized_keyword);
CREATE INDEX idx_ske_keyword_geo ON search_keyword_event (normalized_keyword, lat, lng);
```

### 2. Aggregated Popularity
```sql
CREATE TABLE keyword_popularity_hourly (
  event_hour DATETIME NOT NULL,
  prefix VARCHAR(6) NOT NULL,
  region_code VARCHAR(10) NOT NULL,
  keyword VARCHAR(60) NOT NULL,
  search_cnt INT NOT NULL,
  click_cnt INT NOT NULL,
  PRIMARY KEY (event_hour, prefix, region_code, keyword)
);
```

### 3. Redis Keys
- `keyword:prefix:{region}` → Sorted Set (score = `search_cnt*0.7 + click_cnt*1.3`).
- `keyword:prefix:global` → 지역 정보 없을 때 fallback.
- TTL = 2h, but scheduler refresh every 5m → overlapping 갱신으로 공백 구간 제거.

---

## 🚧 구현 단계

### Phase 0. 사전 준비 (0.5 Sprint)
1. **DTO & Normalizer**: 검색 키워드 전처리 유틸 (lowercase, 공백/특수문자 제거, 초성 변환).
2. **Migration**: 위 Raw/Aggregation 테이블 DDL 배포.
3. **Feature flag**: `autocomplete.redis.enabled`, `autocomplete.logging.enabled`.

### Phase 1. 이벤트 로깅 (1 Sprint)
1. API Controller → Service 로깅 Hook 추가 (`keyword`, `memberId`, `lat/lng`, 추천 클릭 ID`).
2. 비동기 Insert (Spring `@Async` or internal queue) + 단위 테스트.
3. Kibana/Log 미러링 설정: 이벤트 삽입 실패 시 fallback 로깅.
4. **완료 기준**: 하루 평균 이벤트 1M 건 저장 가능, API 지연 영향 < 5ms.

### Phase 2. 집계 및 Redis 적재 (1 Sprint)
1. `smartmealtable-scheduler` 배치 작성  
   - 파라미터: `event_from`, `event_to`, `region_filters`  
   - SQL: Raw → Hourly 테이블 → 상위 500개 키워드 추출.
2. Redis Writer 모듈 (`RedisKeywordWriter`)  
   - `ZADD keyword:prefix:{region}` with scores, `PEXPIRE`.
   - 집계 주기: 5분 (staggered) / 1시간 (정확도용) 두 레벨 운영.
3. 메트릭  
   - Redis `ZCARD`, `zset_refresh_latency_ms`, 스케줄러 성공/실패 카운터.

### Phase 3. API 리팩토링 (1 Sprint)
1. Service에 다단계 조회 파이프라인 도입  
   - Stage1: Redis (prefix 1~3글자 + 지역). ZRANGE + score/limit apply.  
   - Stage2: MySQL `keyword_popularity_daily` (JPA or mybatis).  
   - Stage3: Legacy substring table fallback.  
   - Stage별 결과 merge 후 limit 적용.
2. Spring Cache (Caffeine) 로 30초 메모리 캐시 (prefix/region pair) 추가.
3. Feature flag 로 신규 로직 롤아웃 (`redis.percent`).

### Phase 4. 클린업 & 최적화 (0.5 Sprint)
1. substring 테이블 사이즈 모니터링 → fallback 히트율 5% 이하 되면 데이터 축소.  
2. Redis Memory Tuning (`maxmemory-policy=allkeys-lru`, eviction 모니터링).  
3. 최종 문서화 & 운영 핸드북 업데이트.

총 일정: 약 4 Sprint (8주) + 안정화 1주.

---

## 🧪 테스트 & 검증 플랜

| 구분 | 검증 항목 | 도구 |
| --- | --- | --- |
| 단위 | Normalizer, Log Writer, Redis Writer | JUnit + Mockito |
| 통합 | 이벤트 저장 → 배치 → Redis → API 응답까지 E2E | Testcontainers (MySQL/Redis) |
| 부하 | Redis hit/miss 분포, Stage 전환 p95 | `k6 performance-test/k6/finance-scenarios.js` |
| 데이터 | 롤업 SQL 정확성, 중복 키워드 제거 | SQL 스냅샷 diff |
| 모니터링 | Redis memory, miss ratio, fallback count | Grafana + Prometheus Exporter |

성공 기준:
- Redis Stage hit ratio ≥ 95% (전체 요청 기준).
- API 응답 `p95 ≤ 150ms`, `p99 ≤ 250ms`.
- 배치 5분 주기 업데이트 성공률 ≥ 99%.

---

## 📈 관측 지표 & 알림

| Metric | 기준 | Alert 조건 |
| --- | --- | --- |
| `autocomplete_redis_hit_ratio` | > 0.95 | 5분 평균 < 0.9 |
| `autocomplete_fallback_count` | 0 ~ 5% | 5분 동안 10% 이상 | 
| `scheduler_keyword_aggregation_latency_ms` | < 3000ms | 한 번이라도 6000ms 초과 |
| `redis_memory_used_percent` | < 70% | 80% 이상 10분 지속 |
| `search_keyword_event_insert_error_count` | 0 | 5분 동안 10회 이상 |

---

## 🧭 롤아웃 전략
1. **Shadow Mode (주간 1)**: 기존 substring 응답과 신규 응답을 동시에 계산해 로깅만 하고, 실제 응답은 기존 방식으로. 품질/속도 비교.
2. **Canary (주간 2)**: 전체 트래픽의 10%만 Redis 우선 경로 사용 (Feature flag).
3. **Full Rollout (주간 3)**: Hit ratio 및 오류율 문제 없으면 100% 전환.
4. **Fallback 제거 (주간 4+)**: substring 테이블 사이즈 감소 & deprecated 플래그 추가.

롤백 플랜: Feature flag 끄고 즉시 기존 substring 경로만 사용. Redis/집계 잡은 비활성화하되 로그 수집은 유지.

---

## ⚠️ 리스크 & 완화
- **Redis 장애 시 전체 추천 불가** → Multi-stage fallback + TTL + Prometheus alert.
- **로그 데이터 기하급수 증가** → 90일 TTL 파티셔닝, Archive 배치로 구간별 삭제.
- **집계 주기가 너무 길면 트렌드 반영 실패** → 5분 incremental + 1시간 full refresh 혼용.
- **개발/QA 환경 데이터 부족** → 성능 모듈(`smartmealtable-performance`)로 synthetic 로그 생성 스크립트 제공.

---

## 📄 참고 문서
- `docs/performance/PERFORMANCE_TEST_PLAN.md` §4.2.1~4.2.2
- `docs/REDIS_ARCHITECTURE.md`
- `docs/plan/SEARCH_ENHANCEMENT_PLAN.md`
- `performance-test/k6/finance-scenarios.js`

이 문서는 리팩토링 작업의 상위 계획서이며, 개발 중 업데이트 시 버전/날짜를 반드시 갱신합니다. API 스펙 변경이 필요한 경우 PO/클라이언트 팀과 별도 승인 절차를 밟으십시오.
