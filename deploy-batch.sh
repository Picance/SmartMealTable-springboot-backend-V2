#!/bin/bash
# 배치 작업 서버 배포 스크립트 (인스턴스 3용)

set -e

echo "=== SmartMealTable 배치 시스템 배포 시작 ==="

# 환경 변수 설정
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export ADMIN_PRIVATE_IP=$(aws ec2 describe-instances \
    --instance-ids $(terraform output -raw admin_instance_id) \
    --query 'Reservations[0].Instances[0].PrivateIpAddress' \
    --output text)
export DB_PASSWORD=${DB_PASSWORD:-"your_db_password_here"}

echo "RDS Endpoint: $RDS_ENDPOINT"
echo "Admin Private IP: $ADMIN_PRIVATE_IP"

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