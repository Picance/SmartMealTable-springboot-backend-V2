#!/bin/bash
# API 서버 배포 스크립트 (원격 EC2 배포)

set -e

echo "=== SmartMealTable API 서버 배포 시작 ==="

# ========================================
# 환경 변수 설정
# ========================================

# Terraform 출력에서 값 가져오기
export API_INSTANCE_ID=$(terraform output -raw api_instance_id)
export API_PUBLIC_IP=$(terraform output -raw api_public_ip)
export ADMIN_INSTANCE_ID=$(terraform output -raw admin_instance_id)
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export DB_USERNAME=$(terraform output -raw db_username)
export DB_PASSWORD=$(terraform output -raw db_password)

# .env 파일에서 추가 환경변수 로드 (로컬 .env 파일이 있는 경우)
if [ -f .env ]; then
    echo "로컬 .env 파일에서 환경변수 로드 중..."
    export $(grep -v '^#' .env | xargs)
fi

# Admin 인스턴스의 Private IP 조회
export ADMIN_PRIVATE_IP=$(aws ec2 describe-instances \
    --instance-ids ${ADMIN_INSTANCE_ID} \
    --query 'Reservations[0].Instances[0].PrivateIpAddress' \
    --output text)

echo "=========================================="
echo "배포 정보"
echo "=========================================="
echo "API 인스턴스 ID: ${API_INSTANCE_ID}"
echo "API 퍼블릭 IP: ${API_PUBLIC_IP}"
echo "Admin 인스턴스 ID: ${ADMIN_INSTANCE_ID}"
echo "Admin Private IP: ${ADMIN_PRIVATE_IP}"
echo "RDS Endpoint: ${RDS_ENDPOINT}"
echo "=========================================="

# SSH 키 파일 확인
SSH_KEY_FILE="smartmealtable-key"
if [ ! -f "$SSH_KEY_FILE" ]; then
    echo "❌ SSH 키 파일을 찾을 수 없습니다: $SSH_KEY_FILE"
    exit 1
fi

# SSH 권한 설정
chmod 400 "$SSH_KEY_FILE"

# ========================================
# 1. EC2에 프로젝트 코드 전송 및 빌드 준비
# ========================================
echo ""
echo "1️⃣  EC2에 프로젝트 코드 전송 중..."

ssh -i "$SSH_KEY_FILE" -o StrictHostKeyChecking=no ubuntu@${API_PUBLIC_IP} "mkdir -p ~/deployment && rm -rf ~/deployment/*" || true

# 전체 프로젝트를 rsync로 전송 (빌드된 파일 제외)
rsync -avz -e "ssh -i $SSH_KEY_FILE -o StrictHostKeyChecking=no" \
    --exclude='build' \
    --exclude='.git' \
    --exclude='.gradle' \
    --exclude='logs' \
    --exclude='bin' \
    --exclude='*.log' \
    . ubuntu@${API_PUBLIC_IP}:~/deployment/

echo "✅ 프로젝트 코드 전송 완료"

# ========================================
# 2. EC2에 .env 파일 생성 및 Java 설치 확인
# ========================================
echo ""
echo "2️⃣  EC2 환경 설정 및 빌드 준비 중..."

ssh -i "$SSH_KEY_FILE" -o StrictHostKeyChecking=no ubuntu@${API_PUBLIC_IP} << EOF_ENV
cd ~/deployment

# .env 파일 생성
cat > .env << EOF
# Database
RDS_ENDPOINT=${RDS_ENDPOINT}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}

# Redis
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

echo ".env 파일:"
cat .env

# Java 설치 확인
echo "Java 버전 확인:"
java -version

# Gradle 빌드 권한 설정
chmod +x ./gradlew
EOF_ENV

echo "✅ 환경 설정 완료"

# ========================================
# 3. 프로젝트 빌드 (EC2에서 실행)
# ========================================
echo ""
echo "3️⃣  EC2에서 Gradle 빌드 수행 중... (약 5-10분 소요)"

ssh -i "$SSH_KEY_FILE" -o StrictHostKeyChecking=no ubuntu@${API_PUBLIC_IP} << EOF_BUILD
set -e
cd ~/deployment

echo "Gradle 캐시 정리 및 API 모듈 빌드 중..."
./gradlew clean :smartmealtable-api:build -x test

echo "빌드 완료!"
ls -lah smartmealtable-api/build/libs/
EOF_BUILD

echo "✅ 프로젝트 빌드 완료"

# ========================================
# 4. Docker 이미지 빌드 및 컨테이너 시작 (EC2에서 원격 실행)
# ========================================
echo ""
echo "4️⃣  EC2에서 Docker 이미지 빌드 및 컨테이너 시작..."

ssh -i "$SSH_KEY_FILE" -o StrictHostKeyChecking=no ubuntu@${API_PUBLIC_IP} << EOF_REMOTE
set -e
cd ~/deployment

# Docker 이미지 빌드
echo "Docker 이미지 빌드 중..."
docker build -f Dockerfile.api -t smartmealtable-api:latest .

# 기존 컨테이너 정지 및 제거
echo "기존 컨테이너 정리 중..."
docker-compose -f docker-compose.api.yml down || true

# 새 컨테이너 시작
echo "새 컨테이너 시작 중..."
docker-compose -f docker-compose.api.yml up -d

echo "컨테이너 시작 완료"
EOF_REMOTE

echo "✅ Docker 컨테이너 시작 완료"

# ========================================
# 5. Health Check
# ========================================
echo ""
echo "5️⃣  API 서버 상태 확인 중..."

sleep 40

RETRY_COUNT=0
MAX_RETRIES=15
HEALTH_CHECK_SUCCESS=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s -f http://${API_PUBLIC_IP}:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ API 서버가 성공적으로 시작되었습니다!"
        HEALTH_CHECK_SUCCESS=true
        break
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            echo "⏳ API 서버 시작 대기 중... ($RETRY_COUNT/$MAX_RETRIES) - 10초 후 재시도"
            sleep 10
        fi
    fi
done

if [ "$HEALTH_CHECK_SUCCESS" = false ]; then
    echo "⚠️  API 서버 Health Check 실패. EC2 로그를 확인하세요:"
    ssh -i "$SSH_KEY_FILE" -o StrictHostKeyChecking=no ubuntu@${API_PUBLIC_IP} "docker logs \$(docker ps -q -f ancestor=smartmealtable-api:latest) 2>/dev/null | tail -30" || true
    exit 1
fi

# ========================================
# 6. 배포 완료
# ========================================
echo ""
echo "=========================================="
echo "✅ API 서버 배포 완료!"
echo "=========================================="
echo "API URL: http://${API_PUBLIC_IP}:8080"
echo "Health Check: http://${API_PUBLIC_IP}:8080/actuator/health"
echo "=========================================="