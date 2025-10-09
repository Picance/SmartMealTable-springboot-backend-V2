#!/bin/bash
# API 서버 배포 스크립트 (인스턴스 1용)

set -e

echo "=== SmartMealTable API 서버 배포 시작 ==="

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
docker build -f Dockerfile.api -t smartmealtable-api:latest .

# 기존 컨테이너 중지 및 제거 (있다면)
echo "기존 컨테이너 정리 중..."
docker-compose -f docker-compose.api.yml down || true

# 새 컨테이너 시작
echo "새 컨테이너 시작 중..."
docker-compose -f docker-compose.api.yml up -d

# 헬스 체크
echo "API 서버 상태 확인 중..."
sleep 30

for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health; then
        echo "✅ API 서버가 성공적으로 시작되었습니다!"
        break
    else
        echo "⏳ API 서버 시작 대기 중... ($i/10)"
        sleep 10
    fi
done

# 서비스 상태 확인
echo "=== 서비스 상태 ==="
docker-compose -f docker-compose.api.yml ps

# 메모리 사용량 확인
echo "=== 메모리 사용량 ==="
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"

echo "=== API 서버 배포 완료 ==="
echo "접속 URL: http://$(terraform output -raw api_public_ip):8080"