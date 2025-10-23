# Docker Compose 환경변수 설정 가이드

## 📋 파일 구조

```
.env                    # 프로덕션/로컬 환경변수 (Git 무시 대상)
.env.example            # 템플릿 파일 (Git 추적)
.env.local              # 로컬 개발용 환경변수 (선택사항)
docker-compose.yml      # 로컬 개발용 (완전 통합)
docker-compose.local.yml # 로컬 개발용 (모듈별 분리)
docker-compose.api.yml  # 프로덕션 API
docker-compose.admin.yml # 프로덕션 Admin + 모니터링
docker-compose.batch.yml # 프로덕션 배치
```

## 🔧 환경변수 설정

### 1. 로컬 개발 환경

#### 방법 A: docker-compose.yml 사용
```bash
# MySQL, Redis, 모든 서비스가 한 파일에 포함됨
docker-compose -f docker-compose.yml up -d
```

#### 방법 B: docker-compose.local.yml 사용
```bash
# 모듈별로 분리된 버전
docker-compose -f docker-compose.local.yml up -d
```

**필요한 환경변수** (.env에서 자동으로 로드):
```bash
# DB 설정 (로컬은 자동 생성)
MYSQL_DATABASE=smartmealtable
MYSQL_USER=smartmeal_user
MYSQL_PASSWORD=smartmeal123

# Redis (로컬은 6379 고정)
REDIS_HOST=redis (docker-compose.local.yml)
REDIS_PORT=6379

# OAuth & 외부 API (필수)
KAKAO_CLIENT_ID=...
GOOGLE_CLIENT_ID=...
VERTEX_AI_PROJECT_ID=...
```

### 2. 프로덕션 배포 (AWS)

#### 인스턴스 1: API 서버
```bash
export RDS_ENDPOINT=smartmealtable-db.xxx.rds.amazonaws.com
export DB_USERNAME=smartmeal_user
export DB_PASSWORD=your-secure-password
export REDIS_HOST=admin-instance-private-ip
export REDIS_PORT=6379

docker-compose -f docker-compose.api.yml up -d
```

#### 인스턴스 2: Admin + Redis + 모니터링
```bash
export RDS_ENDPOINT=smartmealtable-db.xxx.rds.amazonaws.com
export DB_USERNAME=smartmeal_user
export DB_PASSWORD=your-secure-password
export ADMIN_PUBLIC_IP=54.123.45.67

docker-compose -f docker-compose.admin.yml up -d
```

#### 인스턴스 3: 배치 작업
```bash
export RDS_ENDPOINT=smartmealtable-db.xxx.rds.amazonaws.com
export DB_USERNAME=smartmeal_user
export DB_PASSWORD=your-secure-password
export REDIS_HOST=admin-instance-private-ip
export REDIS_PORT=6379

# 스케줄러 실행
docker-compose -f docker-compose.batch.yml up -d scheduler

# 크롤러 필요시 실행
docker-compose -f docker-compose.batch.yml --profile crawler up -d crawler
```

## 📝 환경변수 상세 설명

### Docker Compose용 환경변수

| 변수명 | 설정값 | 설명 |
|--------|--------|------|
| RDS_ENDPOINT | AWS RDS 엔드포인트 | MySQL 데이터베이스 호스트 |
| DB_USERNAME | smartmeal_user | 데이터베이스 사용자명 |
| DB_PASSWORD | 강력한 패스워드 | 데이터베이스 패스워드 |
| REDIS_HOST | localhost (로컬) / Admin IP (프로덕션) | Redis 호스트 |
| REDIS_PORT | 6379 | Redis 포트 (기본값) |
| ADMIN_PUBLIC_IP | Admin 공개 IP | Grafana 접속 URL 생성 |
| ADMIN_PRIVATE_IP | Admin Private IP | 내부 통신용 |

### OAuth & 외부 API 환경변수

| 변수명 | 설정값 | 설명 |
|--------|--------|------|
| KAKAO_CLIENT_ID | OAuth Client ID | 카카오 로그인 |
| GOOGLE_CLIENT_ID | OAuth Client ID | 구글 로그인 |
| GOOGLE_CLIENT_SECRET | OAuth Secret | 구글 로그인 시크릿 |
| VERTEX_AI_PROJECT_ID | Google Cloud Project ID | Gemini API 사용 |
| VERTEX_AI_MODEL | gemini-2.5-flash | 사용할 Gemini 모델 |
| NAVER_MAP_CLIENT_ID | Naver API Key | 네이버 지도 API |
| NAVER_MAP_CLIENT_SECRET | Naver API Secret | 네이버 지도 Secret |

## 🚀 사용 방법

### 1. 로컬 개발 시작
```bash
# .env 파일의 OAuth 설정 확인
cat .env

# 서비스 시작
docker-compose -f docker-compose.local.yml up -d

# 로그 확인
docker-compose -f docker-compose.local.yml logs -f
```

### 2. 프로덕션 배포
```bash
# 1. .env 파일 준비 (또는 환경변수 설정)
cp .env.example .env
# .env 파일의 RDS, Redis 정보 수정

# 2. Terraform으로 AWS 인프라 배포
terraform init
terraform apply

# 3. 각 인스턴스에서 배포 스크립트 실행
./deploy-all.sh

# 또는 개별 배포
./deploy-api.sh
./deploy-admin.sh
./deploy-batch.sh
```

## ⚠️ 보안 주의사항

1. **`.env` 파일은 Git에서 제외**
   ```bash
   # .gitignore 확인
   cat .gitignore | grep .env
   ```

2. **프로덕션 DB 패스워드는 강력하게**
   - 최소 16자 이상
   - 대문자, 소문자, 숫자, 특수문자 포함

3. **AWS Secrets Manager 사용 권장**
   ```bash
   # Docker Compose에서 직접 읽기
   export DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id smartmealtable-db-password --query SecretString --output text)
   ```

4. **로그 파일에 민감한 정보 포함되지 않도록 확인**
   ```bash
   # 환경변수 마스킹 설정 필수
   docker-compose logs | grep -i password  # 비어있어야 함
   ```

## 🔍 디버깅

### 환경변수 확인
```bash
# 실행 중인 컨테이너의 환경변수 확인
docker exec smartmealtable-api env | grep SPRING

# 또는 .env 파일에서 직접 확인
cat .env
```

### 연결 테스트
```bash
# RDS 연결 테스트
docker exec smartmealtable-api mysql -h${RDS_ENDPOINT} -u${DB_USERNAME} -p${DB_PASSWORD} -e "SELECT 1"

# Redis 연결 테스트
docker exec smartmealtable-api redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} ping
```

## 📚 참고 파일

- `Dockerfile.api` - API 서버 이미지 정의
- `Dockerfile.admin` - Admin 서버 이미지 정의
- `Dockerfile.scheduler` - 스케줄러 이미지 정의
- `Dockerfile.crawler` - 크롤러 이미지 정의
- `docs/deploy/DISTRIBUTED_DEPLOYMENT.md` - 배포 상세 가이드
- `docs/deploy/QUICK_START.md` - 빠른 시작 가이드
