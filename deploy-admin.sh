#!/bin/bash
# Admin 서버 + Redis + 모니터링 배포 스크립트 (인스턴스 2용)

set -e

echo "=== SmartMealTable Admin 시스템 배포 시작 ==="

# 환경 변수 설정
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export ADMIN_PUBLIC_IP=$(terraform output -raw admin_public_ip)
export DB_PASSWORD=${DB_PASSWORD:-"your_db_password_here"}

echo "RDS Endpoint: $RDS_ENDPOINT"
echo "Admin Public IP: $ADMIN_PUBLIC_IP"

# 모니터링 설정 디렉토리 생성
mkdir -p monitoring/grafana/provisioning/{datasources,dashboards}

# Docker 이미지 빌드 및 태그
echo "Docker 이미지 빌드 중..."
docker build -f Dockerfile.admin -t smartmealtable-admin:latest .

# 기존 컨테이너 중지 및 제거 (있다면)
echo "기존 컨테이너 정리 중..."
docker-compose -f docker-compose.admin.yml down || true

# 새 컨테이너 시작
echo "새 컨테이너 시작 중..."
docker-compose -f docker-compose.admin.yml up -d

# 서비스별 헬스 체크
echo "서비스 상태 확인 중..."

# Redis 체크
echo "Redis 상태 확인..."
sleep 10
# 동적으로 Redis 컨테이너 찾기
REDIS_CONTAINER=$(docker ps --filter "name=redis" -q | head -1)
if [ -n "$REDIS_CONTAINER" ]; then
  for i in {1..5}; do
    if docker exec $REDIS_CONTAINER redis-cli ping | grep -q PONG; then
      echo "✅ Redis가 성공적으로 시작되었습니다!"
      break
    else
      echo "⏳ Redis 시작 대기 중... ($i/5)"
      sleep 5
    fi
  done
else
  echo "⚠️  Redis 컨테이너를 찾을 수 없습니다."
fi

# Admin 서버 체크
echo "Admin 서버 상태 확인..."
sleep 20
for i in {1..10}; do
    if curl -f http://localhost:8081/actuator/health; then
        echo "✅ Admin 서버가 성공적으로 시작되었습니다!"
        break
    else
        echo "⏳ Admin 서버 시작 대기 중... ($i/10)"
        sleep 10
    fi
done

# Prometheus 체크
echo "Prometheus 상태 확인..."
for i in {1..5}; do
    if curl -f http://localhost:9090/-/healthy; then
        echo "✅ Prometheus가 성공적으로 시작되었습니다!"
        break
    else
        echo "⏳ Prometheus 시작 대기 중... ($i/5)"
        sleep 10
    fi
done

# Grafana 체크
echo "Grafana 상태 확인..."
for i in {1..5}; do
    if curl -f http://localhost:3000/api/health; then
        echo "✅ Grafana가 성공적으로 시작되었습니다!"
        break
    else
        echo "⏳ Grafana 시작 대기 중... ($i/5)"
        sleep 10
    fi
done

# 서비스 상태 확인
echo "=== 서비스 상태 ==="
docker-compose -f docker-compose.admin.yml ps

# 메모리 사용량 확인
echo "=== 메모리 사용량 ==="
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"

# 시스템 메모리 사용량
echo "=== 시스템 메모리 ==="
free -h

echo "=== Admin 시스템 배포 완료 ==="
echo "Admin URL: http://$ADMIN_PUBLIC_IP:8081"
echo "Grafana URL: http://$ADMIN_PUBLIC_IP:3000 (admin/admin123)"
echo "Prometheus URL: http://$ADMIN_PUBLIC_IP:9090"
echo "Redis는 내부적으로 실행 중입니다 (포트: 6379)"