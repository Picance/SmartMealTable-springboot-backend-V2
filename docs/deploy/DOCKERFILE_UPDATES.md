# Dockerfile 수정 사항 - 멀티 모듈 구조 + AWS RDS 배포

## 📋 수정 내용

### 1. **기본 Dockerfile** (루트 디렉토리)
- 멀티 모듈 구조를 위한 기본 템플릿
- 보안 강화: 루트가 아닌 일반 사용자(`appuser`)로 실행
- JVM 메모리 최적화: t3.micro 환경용 (256MB~512MB)
- G1GC 가비지 컬렉터 사용 (메모리 효율)

### 2. **모듈별 Dockerfile**

#### Dockerfile.api (API 서버)
```dockerfile
- JAR 위치: smartmealtable-api/build/libs/smartmealtable-api-*.jar
- 메모리: 512MB (-Xmx512m)
- 포트: 8080
- 헬스체크: /actuator/health
```

#### Dockerfile.admin (Admin 서버)
```dockerfile
- JAR 위치: smartmealtable-admin/build/libs/smartmealtable-admin-*.jar
- 메모리: 400MB (-Xmx400m)
- 포트: 8081
- 헬스체크: /actuator/health
```

#### Dockerfile.scheduler (스케줄러)
```dockerfile
- JAR 위치: smartmealtable-scheduler/build/libs/smartmealtable-scheduler-*.jar
- 메모리: 300MB (-Xmx300m)
- 포트: 8082
- 헬스체크: /actuator/health
```

#### Dockerfile.crawler (배치 크롤러)
```dockerfile
- JAR 위치: smartmealtable-batch/crawler/build/libs/crawler-*.jar
- 메모리: 400MB (-Xmx400m)
- 포트: 미노출 (필요시 배포스크립트에서 설정)
- 헬스체크: 제거 (배치용)
```

## 🔑 주요 개선 사항

### 보안
- ✅ 루트 사용자 제거 (appuser 일반 사용자로 실행)
- ✅ JAR 파일 권한 명시적 설정 (`--chown=appuser:appuser`)

### 성능 최적화
- ✅ **G1GC**: 메모리 제한 환경에 적합한 가비지 컬렉터
- ✅ **JVM 메모리 설정**: t3.micro (1GB) 인스턴스에 맞춘 할당
  - API: -Xms256m -Xmx512m (전체의 50%)
  - Admin: -Xms200m -Xmx400m (전체의 40%)
  - Scheduler: -Xms150m -Xmx300m (전체의 30%)
  - Crawler: -Xms200m -Xmx400m (필요시에만 실행)

### 운영 편의성
- ✅ **헬스체크**: Spring Actuator 활용한 자동 재시작
- ✅ **환경 변수**: `JAVA_OPTS`, `SPRING_PROFILES_ACTIVE` 설정 가능
- ✅ **명시적 JAR 지정**: 와일드카드 대신 `-*.jar` 패턴으로 정확한 파일 지정

### AWS RDS 연동
- ✅ 환경변수를 통한 DB 설정 (docker-compose에서 관리)
  - `SPRING_DATASOURCE_URL`: RDS 엔드포인트
  - `SPRING_DATASOURCE_USERNAME`: RDS 사용자명
  - `SPRING_DATASOURCE_PASSWORD`: RDS 패스워드 (환경변수)

## 📦 빌드 및 배포

### 1. 이미지 빌드
```bash
# 전체 프로젝트 빌드 (필수)
./gradlew clean build -x test

# 개별 이미지 빌드
docker build -f Dockerfile.api -t smartmealtable-api:latest .
docker build -f Dockerfile.admin -t smartmealtable-admin:latest .
docker build -f Dockerfile.scheduler -t smartmealtable-scheduler:latest .
docker build -f Dockerfile.crawler -t smartmealtable-crawler:latest .
```

### 2. Docker Compose 사용
```bash
# docker-compose.api.yml에서 사용
docker-compose -f docker-compose.api.yml up -d

# docker-compose.admin.yml에서 사용
docker-compose -f docker-compose.admin.yml up -d

# docker-compose.batch.yml에서 사용
docker-compose -f docker-compose.batch.yml up -d
```

### 3. Docker Compose 예시 (RDS 연동)
```yaml
services:
  api:
    build:
      context: .
      dockerfile: Dockerfile.api
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://your-rds-endpoint:3306/smartmeal
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: com.mysql.cj.jdbc.Driver
    ports:
      - "8080:8080"
```

## 🚀 메모리 구성 (t3.micro = 1GB)

| 서비스 | JVM Min | JVM Max | 예상 메모리 | 설명 |
|--------|---------|---------|-----------|------|
| API | 256m | 512m | ~700MB | 상시 실행 |
| Admin | 200m | 400m | ~550MB | 상시 실행 |
| Scheduler | 150m | 300m | ~450MB | 상시 실행 |
| Crawler | 200m | 400m | ~600MB | 필요시만 실행 |
| Redis | - | - | ~100MB | 캐싱 |
| OS/Docker | - | - | ~150MB | 시스템 |

✅ **총 메모리**: 약 950MB (1GB 내에 수용)

## ⚠️ 주의사항

1. **환경변수 관리**: DB 패스워드는 절대 Dockerfile에 하드코딩하지 말 것
   - docker-compose의 `environment` 또는 `.env` 파일 사용
   - AWS Secrets Manager 연동 권장

2. **메모리 모니터링**: 실운영에서 메모리 사용량 주기적 확인
   ```bash
   docker stats
   ```

3. **헬스체크**: 배치 작업(Crawler)은 헬스체크 미포함
   - 배치는 일회성 작업이므로 불필요
   - 스케줄러는 장시간 운영이므로 헬스체크 필수

4. **JAR 파일명**: `smartmealtable-scheduler-*.jar` 형식 필수
   - build.gradle에서 정확한 이름 설정 확인

## 🔗 관련 파일

- `docker-compose.api.yml` - API 서버 컨테이너 설정
- `docker-compose.admin.yml` - Admin + Redis + 모니터링
- `docker-compose.batch.yml` - Scheduler + Crawler
- `main.tf` - Terraform 인프라 정의
- `deploy-api.sh`, `deploy-admin.sh`, `deploy-batch.sh` - 배포 스크립트
