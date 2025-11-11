#!/bin/bash
# 배치 작업 서버 배포 스크립트 (인스턴스 3용)

set -e

echo "=== SmartMealTable 배치 시스템 배포 시작 ==="

# 환경 변수 설정
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export ADMIN_INSTANCE_ID=$(terraform output -raw admin_instance_id)
export ADMIN_PRIVATE_IP=$(aws ec2 describe-instances \
    --instance-ids ${ADMIN_INSTANCE_ID} \
    --query 'Reservations[0].Instances[0].PrivateIpAddress' \
    --output text)
export DB_USERNAME=$(terraform output -raw db_username)
export DB_PASSWORD=$(terraform output -raw db_password)

echo "=========================================="
echo "배포 정보"
echo "=========================================="
echo "RDS Endpoint: $RDS_ENDPOINT"
echo "Admin Private IP: $ADMIN_PRIVATE_IP"
echo "Redis Host (Admin Private IP): $ADMIN_PRIVATE_IP"
echo "=========================================="

# .env 파일 생성 (필요한 환경변수 설정)
echo "=========================================="
echo "환경변수 설정 중..."
echo "=========================================="

cat > .env << EOF
# Database
RDS_ENDPOINT=${RDS_ENDPOINT}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}

# Redis (Admin 인스턴스의 로컬 Redis 사용)
REDIS_HOST=${ADMIN_PRIVATE_IP}
REDIS_PORT=6379

# OAuth
KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID}
KAKAO_REDIRECT_URI=${KAKAO_REDIRECT_URI}
GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
GOOGLE_REDIRECT_URI=${GOOGLE_REDIRECT_URI}

# Naver Map API
NAVER_MAP_CLIENT_ID=${NAVER_MAP_CLIENT_ID}
NAVER_MAP_CLIENT_SECRET=${NAVER_MAP_CLIENT_SECRET}

# Vertex AI
VERTEX_AI_PROJECT_ID=${VERTEX_AI_PROJECT_ID}
VERTEX_AI_MODEL=${VERTEX_AI_MODEL:-gemini-2.5-flash}
VERTEX_AI_TEMPERATURE=${VERTEX_AI_TEMPERATURE:-0.1}
VERTEX_AI_LOCATION=${VERTEX_AI_LOCATION:-asia-northeast3}

# JWT
JWT_SECRET=${JWT_SECRET}
EOF

echo "✅ .env 파일 생성 완료"

# Docker 이미지 빌드 및 태그
echo "Docker 이미지 빌드 중..."
docker build -f Dockerfile.scheduler -t smartmealtable-scheduler:latest .
docker build -f Dockerfile.crawler -t smartmealtable-crawler:latest .

# 기존 컨테이너 중지 및 제거 (있다면)
echo "기존 컨테이너 정리 중..."
docker-compose -f docker-compose.batch.yml down || true

# 스케줄러만 상시 실행
echo "스케줄러 시작 중..."
docker-compose -f docker-compose.batch.yml up -d scheduler

# 스케줄러 헬스 체크
echo "스케줄러 상태 확인 중..."
sleep 30

for i in {1..10}; do
    if curl -f http://localhost:8082/actuator/health; then
        echo "✅ 스케줄러가 성공적으로 시작되었습니다!"
        break
    else
        echo "⏳ 스케줄러 시작 대기 중... ($i/10)"
        sleep 10
    fi
done

# 서비스 상태 확인
echo "=== 서비스 상태 ==="
docker-compose -f docker-compose.batch.yml ps

# 메모리 사용량 확인
echo "=== 메모리 사용량 ==="
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"

# 시스템 메모리 사용량
echo "=== 시스템 메모리 ==="
free -h

echo "=== 배치 시스템 배포 완료 ==="
echo "스케줄러가 실행 중입니다."
echo ""
echo "📋 크롤러 실행 방법:"
echo "  시작: docker-compose -f docker-compose.batch.yml --profile crawler up -d crawler"
echo "  중지: docker-compose -f docker-compose.batch.yml --profile crawler down"
echo "  로그 확인: docker-compose -f docker-compose.batch.yml logs -f crawler"

# 크롤러 실행 여부 확인
read -p "지금 크롤러를 실행하시겠습니까? (y/N): " run_crawler
if [[ $run_crawler =~ ^[Yy]$ ]]; then
    echo "크롤러 시작 중..."
    docker-compose -f docker-compose.batch.yml --profile crawler up -d crawler
    echo "✅ 크롤러가 시작되었습니다!"
    echo "크롤러 로그 확인: docker-compose -f docker-compose.batch.yml logs -f crawler"
fi