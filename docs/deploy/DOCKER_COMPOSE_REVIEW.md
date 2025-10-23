# Docker Compose 검토 및 개선 사항

## 📋 현재 상태 분석

### 1. **docker-compose.yml** (로컬 개발용)
✅ 기본 구조 좋음
⚠️ 개선 필요 항목들:
- Admin 포트 매핑 오류: `8081:8080` → `8081:8081` 수정 필요
- 구버전 Prometheus/Grafana 설정 포함
- Redis Exporter 불필요 (로컬용)
- 메모리 할당이 과도함 (로컬 개발은 효율성 고려)

### 2. **docker-compose.api.yml** (프로덕션 API)
⚠️ 개선 필요:
- `DB_URL` 환경변수명이 Spring Boot 표준과 다름
  - Spring Boot 표준: `SPRING_DATASOURCE_URL`
  - 또는 `spring.datasource.url` properties 파일 사용
- REDIS_HOST 환경변수가 실제 사용되지 않을 수 있음
- 메모리 예약값이 낮음 (256M → 300M 권장)

### 3. **docker-compose.admin.yml** (프로덕션 Admin + 모니터링)
✅ 구조 양호
⚠️ 개선 필요:
- Admin 서비스가 Redis에 depend_on하지만 REDIS_HOST 환경변수 미정의
- Prometheus/Grafana 최신 설정 필요
- 볼륨 관리 개선 필요

### 4. **docker-compose.batch.yml** (프로덕션 배치)
✅ 구조 양호
⚠️ 개선 필요:
- Crawler와 Scheduler의 REDIS_HOST 참조 불명확
- Admin 인스턴스 private IP 하드코딩 필요

### 5. **docker-compose.local.yml** (전체 로컬 개발)
✅ 전체 구조 양호
⚠️ 개선 필요:
- 포트 충돌 방지를 위한 명시적 설정 필요
- 로깅 볼륨 마운트 확인

## 🔧 권장 수정 사항

### 1. 환경변수 표준화
**문제**: `DB_URL` vs `SPRING_DATASOURCE_URL` 혼용

**해결 방안**:
```yaml
# docker-compose에서는 Spring Boot 표준 환경변수 사용
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://${RDS_ENDPOINT}/smartmealtable...
  SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
  SPRING_DATA_REDIS_HOST: ${REDIS_HOST}
  SPRING_DATA_REDIS_PORT: ${REDIS_PORT}
```

### 2. Docker Compose 파일 통합 개선
**개선 사항**:
- 공통 설정을 base 파일로 분리
- 환경별로 override 파일 사용
- 버전 관리 통일

### 3. 메모리 할당 최적화
**현재**:
- API: 512MB (Good)
- Admin: 400MB (Good)
- Scheduler: 300MB (Good)
- Crawler: 400MB (Good)

**권장**: 현재 설정 유지 (최적화됨)

### 4. 포트 매핑 오류 수정
**docker-compose.yml**:
```yaml
admin:
  ports:
    - "8081:8081"  # 현재: 8081:8080 (오류)
```

### 5. 헬스체크 개선
**현재**: curl 사용
**권장**: curl 또는 wget 사용 (이미 적용됨)

### 6. Logging 드라이버 설정
**개선**:
- 모든 파일에 일관된 로깅 설정
- 로그 로테이션 명시적 설정

### 7. 네트워크 설정
**현재**: 각 파일마다 다른 네트워크명
**권장**: 일관된 네트워크명 사용

## 📝 수정 파일 목록

| 파일 | 우선순위 | 수정 사항 |
|------|---------|---------|
| docker-compose.yml | 🔴 높음 | Admin 포트, 메모리 조정 |
| docker-compose.api.yml | 🟡 중간 | 환경변수 표준화 |
| docker-compose.admin.yml | 🟡 중간 | Redis 환경변수 추가 |
| docker-compose.batch.yml | 🟡 중간 | Redis 환경변수, 네트워크 설정 |
| docker-compose.local.yml | 🟢 낮음 | 네트워크명 통일 |

## 🎯 주요 수정 전략

1. **환경변수 표준화**: Spring Boot 표준 사용
2. **일관성**: 모든 파일에서 동일한 포맷 사용
3. **메모리**: t3.micro 환경에 맞춘 최적화 유지
4. **오류 수정**: docker-compose.yml Admin 포트 매핑 수정
5. **문서화**: 각 파일의 용도 명확히 표시

