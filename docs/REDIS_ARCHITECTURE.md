# Redis Architecture - EC2 배포 구성

## 개요

SmartMealTable은 공유 Redis 패턴을 사용하여 EC2 환경에서 Redis를 관리합니다. Admin 인스턴스의 Redis를 다른 모든 서비스가 공유합니다.

## 배포 구조

### 인스턴스별 구성

```
┌─────────────────────────────────────┐
│         API Instance                │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  API Server (Port 8080)      │   │
│  │  Redis Host: Admin Private IP│───┼──→ Redis 연결
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘

┌──────────────────────────────────────┐
│       Admin Instance                 │
│                                      │
│  ┌───────────────────────────────┐   │
│  │  Admin Server (Port 8081)     │   │
│  │  Redis Host: localhost        │   │
│  └───────────────────────────────┘   │
│  ┌───────────────────────────────┐   │
│  │  Redis (Port 6379)            │   │
│  │  - 메모리: 100MB              │   │
│  │  - Eviction: allkeys-lru      │   │
│  └───────────────────────────────┘   │
│  ┌───────────────────────────────┐   │
│  │  Scheduler (Port 8082)        │   │
│  │  Redis Host: localhost        │   │
│  └───────────────────────────────┘   │
│  ┌───────────────────────────────┐   │
│  │  Prometheus + Grafana         │   │
│  └───────────────────────────────┘   │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│      Batch Instance                  │
│                                      │
│  ┌───────────────────────────────┐   │
│  │  Crawler (Optional)           │   │
│  │  Redis Host: Admin Private IP │───┼──→ Redis 연결
│  └───────────────────────────────┘   │
└──────────────────────────────────────┘
```

## Redis Host 설정

### Admin 인스턴스 (Redis 소유자)
- **Redis Host**: `localhost`
- **Redis Port**: `6379`
- **배치**: `docker-compose.admin.yml`에 Redis 서비스 정의
- **역할**: 다른 인스턴스의 Redis 요청을 받는 중앙 Redis

### API 인스턴스
- **Redis Host**: Admin의 Private IP (예: `10.0.1.100`)
- **Redis Port**: `6379`
- **배치**: `docker-compose.api.yml`에서 환경변수로 설정
- **설정**:
  ```yaml
  - SPRING_DATA_REDIS_HOST=${REDIS_HOST}
  - SPRING_DATA_REDIS_PORT=${REDIS_PORT:-6379}
  ```

### Batch 인스턴스 (Scheduler + Crawler)
- **Redis Host**: Admin의 Private IP (예: `10.0.1.100`)
- **Redis Port**: `6379`
- **배치**: `docker-compose.batch.yml`에서 환경변수로 설정
- **설정**:
  ```yaml
  - SPRING_DATA_REDIS_HOST=${REDIS_HOST}
  - SPRING_DATA_REDIS_PORT=${REDIS_PORT:-6379}
  ```

## 배포 방법

### 1. Terraform으로 인스턴스 배포
```bash
terraform plan
terraform apply
```

### 2. Admin 인스턴스 배포
```bash
./deploy-admin.sh
```
- Redis 서비스가 로컬에서 시작됨
- Admin 서버가 Redis와 동일 인스턴스에서 실행

### 3. API 인스턴스 배포
```bash
./deploy-api.sh
```
- `.env`에 `REDIS_HOST=<Admin Private IP>`로 설정됨
- Admin 인스턴스의 Redis에 연결

### 4. Batch 인스턴스 배포
```bash
./deploy-batch.sh
```
- `.env`에 `REDIS_HOST=<Admin Private IP>`로 설정됨
- Scheduler와 Crawler가 Admin의 Redis에 연결

## 환경변수

### docker-compose로 실행할 때 필요한 환경변수
```env
# Database
RDS_ENDPOINT=<RDS endpoint>
DB_USERNAME=<database user>
DB_PASSWORD=<database password>

# Redis
REDIS_HOST=<Admin instance private IP>
REDIS_PORT=6379

# OAuth
KAKAO_CLIENT_ID=<kakao client id>
KAKAO_REDIRECT_URI=<kakao redirect uri>
GOOGLE_CLIENT_ID=<google client id>
GOOGLE_CLIENT_SECRET=<google client secret>
GOOGLE_REDIRECT_URI=<google redirect uri>

# Naver Map API
NAVER_MAP_CLIENT_ID=<naver map client id>
NAVER_MAP_CLIENT_SECRET=<naver map client secret>

# Vertex AI
VERTEX_AI_PROJECT_ID=<project id>
VERTEX_AI_MODEL=gemini-2.5-flash
VERTEX_AI_TEMPERATURE=0.1
VERTEX_AI_LOCATION=asia-northeast3

# JWT
JWT_SECRET=<jwt secret>
```

## GitHub Actions 배포 플로우

### 워크플로우 개요
GitHub Actions의 `deploy.yml`은 다음 순서로 서비스를 배포합니다:

1. **API 배포** (API Instance)
   - `SPRING_DATA_REDIS_HOST=${{ secrets.REDIS_HOST }}`
   - Admin의 Private IP를 사용

2. **Admin + Redis 배포** (Admin Instance)
   - Redis 컨테이너 시작 (`redis-smartmealtable`)
   - Docker 네트워크 생성 (`smartmealtable-network`)
   - Admin 컨테이너를 네트워크에 연결
   - `SPRING_DATA_REDIS_HOST=redis-smartmealtable` (컨테이너명으로 접근)

3. **Scheduler 배포** (Admin Instance)
   - 동일 네트워크 (`smartmealtable-network`)에 연결
   - `SPRING_DATA_REDIS_HOST=redis-smartmealtable`
   - Redis 컨테이너와 같은 네트워크에서 실행되므로 컨테이너명으로 접근 가능

4. **Crawler 배포** (Batch Instance - 선택적)
   - `SPRING_DATA_REDIS_HOST=${{ secrets.REDIS_HOST }}`
   - Admin의 Private IP를 사용
   - 다른 인스턴스이므로 Private IP로 접근

### GitHub Secrets 설정 필요 항목
```
REDIS_HOST=<Admin instance private IP>
RDS_ENDPOINT=<RDS endpoint>
RDS_USERNAME=<database user>
RDS_PASSWORD=<database password>
GOOGLE_CLIENT_ID=<google client id>
GOOGLE_CLIENT_SECRET=<google client secret>
GOOGLE_REDIRECT_URI=<google redirect uri>
KAKAO_CLIENT_ID=<kakao client id>
KAKAO_REDIRECT_URI=<kakao redirect uri>
NAVER_MAP_CLIENT_ID=<naver map client id>
NAVER_MAP_CLIENT_SECRET=<naver map client secret>
VERTEX_AI_PROJECT_ID=<project id>
VERTEX_AI_MODEL=gemini-2.5-flash
VERTEX_AI_TEMPERATURE=0.1
VERTEX_AI_LOCATION=asia-northeast3
JWT_SECRET=<jwt secret>
EC2_API_INSTANCE_ID=<API instance ID>
EC2_ADMIN_INSTANCE_ID=<Admin instance ID>
EC2_BATCH_INSTANCE_ID=<Batch instance ID>
AWS_ACCESS_KEY_ID=<AWS access key>
AWS_SECRET_ACCESS_KEY=<AWS secret access key>
```

## 네트워크 설정

### VPC/보안 그룹 요구사항
1. **Admin 인스턴스 보안 그룹**
   - 인바운드 6379 (Redis): API, Batch 인스턴스의 보안 그룹 허용
   - 아웃바운드: 필요한 외부 서비스 허용

2. **API/Batch 인스턴스 보안 그룹**
   - 아웃바운드 6379: Admin 인스턴스의 보안 그룹 허용

### 같은 VPC/서브넷 필요
- Admin, API, Batch 인스턴스는 모두 같은 VPC에 있어야 함
- Private IP로 통신하므로 같은 서브넷 또는 라우팅 설정 필요

## 로컬 개발 (docker-compose.local.yml)

로컬 개발 환경에서는 독립적인 Redis 컨테이너를 사용합니다:

```yaml
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    # ... 설정
```

로컬 환경에서는 `localhost` 또는 `redis`(컨테이너명)를 host로 사용합니다.

## 모니터링

### Admin 인스턴스에서 Redis 상태 확인
```bash
# Admin 인스턴스 접속
ssh -i <key> ubuntu@<admin-public-ip>

# Redis 연결 테스트
docker exec <redis-container> redis-cli ping

# Redis 정보 조회
docker exec <redis-container> redis-cli info
```

### API/Batch 인스턴스에서 Redis 연결 확인
```bash
# 로그에서 Redis 연결 상태 확인
docker logs <service-container> | grep -i redis

# 네트워크 연결 테스트
docker exec <service-container> nc -zv <admin-private-ip> 6379
```

### Grafana 모니터링
- Admin 인스턴스의 Grafana에서 Redis 메트릭 모니터링
- Prometheus가 Redis metrics를 수집

## 장점 및 단점

### 장점
- **간단한 구조**: 단일 Redis 인스턴스로 관리 용이
- **낮은 비용**: Redis 인스턴스 1개만 필요
- **메모리 절약**: 서비스별 Redis 메모리 중복 없음

### 단점
- **단일 장애점**: Admin 인스턴스의 Redis 다운 시 모든 서비스 영향
- **네트워크 통신**: 인스턴스 간 네트워크 통신 필요 (지연 가능)
- **확장성 제한**: Redis 성능 상한선이 고정

## Redis 수동 시작 (GitHub Actions 외)

### Admin 인스턴스에서 Redis 수동 시작
```bash
# Admin 인스턴스에 접속
ssh -i <key> ubuntu@<admin-public-ip>

# 네트워크 생성 (이미 있으면 무시)
docker network create smartmealtable-network 2>/dev/null || true

# Redis 컨테이너 시작
docker run -d --name redis-smartmealtable \
  --network smartmealtable-network \
  -e TZ=Asia/Seoul \
  redis:7-alpine \
  redis-server \
    --maxmemory 80mb \
    --maxmemory-policy allkeys-lru \
    --save 900 1 \
    --save 300 10 \
    --save 60 10000

# Redis 상태 확인
docker logs redis-smartmealtable
docker exec redis-smartmealtable redis-cli ping
# 응답: PONG
```

### 기존 Redis 컨테이너 제거
```bash
docker stop redis-smartmealtable
docker rm redis-smartmealtable
```

## 트러블슈팅

### Redis 연결 실패
1. Admin 인스턴스에서 Redis 실행 확인
   ```bash
   docker ps | grep redis
   ```

2. 보안 그룹 설정 확인
   - Admin 인스턴스 보안 그룹에 6379 인바운드 규칙 존재
   - API/Batch 인스턴스의 보안 그룹이 허용 대상

3. 네트워크 연결 테스트
   ```bash
   nc -zv <admin-private-ip> 6379
   ```

### GitHub Actions 배포 실패
1. Admin 배포 후 Redis가 실행되지 않았을 경우
   ```bash
   # Admin 인스턴스에서 수동으로 Redis 시작
   docker network create smartmealtable-network 2>/dev/null || true
   docker run -d --name redis-smartmealtable --network smartmealtable-network \
     -e TZ=Asia/Seoul \
     redis:7-alpine redis-server --maxmemory 80mb --maxmemory-policy allkeys-lru
   ```

2. Admin/Scheduler가 Redis에 연결 안 될 경우
   ```bash
   # 동일 네트워크인지 확인
   docker network inspect smartmealtable-network

   # Admin/Scheduler가 네트워크에 속해있는지 확인
   docker inspect smartmealtable-admin | grep smartmealtable-network
   docker inspect smartmealtable-scheduler | grep smartmealtable-network
   ```

### Redis 메모리 부족
- Admin 인스턴스의 메모리 확인
- Redis maxmemory 정책 확인 (현재: `allkeys-lru`)
- EC2 인스턴스 타입 업그레이드 필요 가능

### 느린 Redis 응답
- Prometheus/Grafana에서 Redis 메트릭 확인
- Admin 인스턴스의 네트워크 지연 확인
- 대용량 데이터 저장 시 ElastiCache 전환 고려

## 향후 개선 계획

### ElastiCache로 마이그레이션
```terraform
resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "smartmealtable-redis"
  engine               = "redis"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  port                 = 6379
}
```
- AWS 관리형 Redis 사용
- 자동 백업 및 페일오버
- 더 나은 성능 및 확장성

### Redis Cluster 구성
- 고가용성을 위한 Redis Sentinel 도입
- 여러 Redis 노드로 구성

## 참고자료

- [docker-compose.admin.yml](../docker-compose.admin.yml) - Admin 인스턴스 구성
- [docker-compose.api.yml](../docker-compose.api.yml) - API 인스턴스 구성
- [docker-compose.batch.yml](../docker-compose.batch.yml) - Batch 인스턴스 구성
- [.github/workflows/deploy.yml](../.github/workflows/deploy.yml) - CI/CD 파이프라인
- [deploy-api.sh](../deploy-api.sh) - API 배포 스크립트
- [deploy-batch.sh](../deploy-batch.sh) - Batch 배포 스크립트
