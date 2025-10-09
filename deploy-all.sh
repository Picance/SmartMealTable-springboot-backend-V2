#!/bin/bash
# 전체 시스템 배포 마스터 스크립트

set -e

echo "🚀 SmartMealTable 완전 분리형 배포 시작"
echo "=========================================="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 환경 변수 확인
check_env() {
    echo -e "${YELLOW}📋 환경 변수 확인 중...${NC}"
    
    if [ -z "$DB_PASSWORD" ]; then
        echo -e "${RED}❌ DB_PASSWORD 환경변수가 설정되지 않았습니다.${NC}"
        echo "export DB_PASSWORD='your_password' 를 실행하고 다시 시도하세요."
        exit 1
    fi
    
    if [ -z "$TF_VAR_db_password" ]; then
        echo -e "${YELLOW}⚠️  TF_VAR_db_password가 설정되지 않았습니다. DB_PASSWORD를 사용합니다.${NC}"
        export TF_VAR_db_password="$DB_PASSWORD"
    fi
    
    echo -e "${GREEN}✅ 환경 변수 확인 완료${NC}"
}

# Terraform 배포
deploy_infrastructure() {
    echo -e "${YELLOW}🏗️  인프라 배포 중...${NC}"
    
    # SSH 키 확인
    if [ ! -f "smartmealtable-key.pub" ]; then
        echo -e "${YELLOW}🔑 SSH 키 페어 생성 중...${NC}"
        ssh-keygen -t rsa -b 2048 -f smartmealtable-key -N ""
        echo -e "${GREEN}✅ SSH 키 페어 생성 완료${NC}"
    fi
    
    # Terraform 실행
    terraform init
    terraform plan
    terraform apply -auto-approve
    
    echo -e "${GREEN}✅ 인프라 배포 완료${NC}"
}

# 인스턴스 정보 수집
get_instance_info() {
    echo -e "${YELLOW}📊 인스턴스 정보 수집 중...${NC}"
    
    export API_PUBLIC_IP=$(terraform output -raw api_public_ip)
    export ADMIN_PUBLIC_IP=$(terraform output -raw admin_public_ip)  
    export BATCH_PUBLIC_IP=$(terraform output -raw batch_public_ip)
    export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
    
    echo "API Server Public IP: $API_PUBLIC_IP"
    echo "Admin Server Public IP: $ADMIN_PUBLIC_IP"
    echo "Batch Server Public IP: $BATCH_PUBLIC_IP"
    echo "RDS Endpoint: $RDS_ENDPOINT"
    
    echo -e "${GREEN}✅ 인스턴스 정보 수집 완료${NC}"
}

# Prometheus 설정 업데이트
update_prometheus_config() {
    echo -e "${YELLOW}📝 Prometheus 설정 업데이트 중...${NC}"
    
    # 인스턴스 private IP 가져오기
    API_PRIVATE_IP=$(aws ec2 describe-instances \
        --instance-ids $(terraform output -raw api_instance_id) \
        --query 'Reservations[0].Instances[0].PrivateIpAddress' \
        --output text)
    
    BATCH_PRIVATE_IP=$(aws ec2 describe-instances \
        --instance-ids $(terraform output -raw batch_instance_id) \
        --query 'Reservations[0].Instances[0].PrivateIpAddress' \
        --output text)
    
    # prometheus.yml 업데이트
    sed -i.bak "s/API_PRIVATE_IP/$API_PRIVATE_IP/g" monitoring/prometheus.yml
    sed -i.bak "s/BATCH_PRIVATE_IP/$BATCH_PRIVATE_IP/g" monitoring/prometheus.yml
    
    echo -e "${GREEN}✅ Prometheus 설정 업데이트 완료${NC}"
}

# 인스턴스 연결 대기
wait_for_instances() {
    echo -e "${YELLOW}⏳ 인스턴스 부팅 대기 중... (60초)${NC}"
    sleep 60
    
    echo -e "${YELLOW}🔍 인스턴스 연결 확인 중...${NC}"
    
    for ip in $API_PUBLIC_IP $ADMIN_PUBLIC_IP $BATCH_PUBLIC_IP; do
        echo "인스턴스 $ip 연결 확인 중..."
        for i in {1..10}; do
            if ssh -i smartmealtable-key -o ConnectTimeout=10 -o StrictHostKeyChecking=no ubuntu@$ip "echo 'Connected'" 2>/dev/null; then
                echo -e "${GREEN}✅ $ip 연결 성공${NC}"
                break
            else
                echo "⏳ $ip 연결 대기 중... ($i/10)"
                sleep 30
            fi
        done
    done
}

# 애플리케이션 배포
deploy_applications() {
    echo -e "${YELLOW}🚀 애플리케이션 배포 중...${NC}"
    
    # 각 인스턴스에 파일 복사 및 배포
    for server in "api" "admin" "batch"; do
        case $server in
            "api")
                ip=$API_PUBLIC_IP
                ;;
            "admin") 
                ip=$ADMIN_PUBLIC_IP
                ;;
            "batch")
                ip=$BATCH_PUBLIC_IP
                ;;
        esac
        
        echo -e "${YELLOW}📦 $server 서버 ($ip) 배포 중...${NC}"
        
        # 필요한 파일들 복사
        scp -i smartmealtable-key -o StrictHostKeyChecking=no \
            docker-compose.$server.yml \
            deploy-$server.sh \
            Dockerfile.* \
            ubuntu@$ip:~/
        
        if [ "$server" = "admin" ]; then
            scp -i smartmealtable-key -o StrictHostKeyChecking=no -r \
                monitoring \
                ubuntu@$ip:~/
        fi
        
        # 배포 스크립트 실행
        ssh -i smartmealtable-key -o StrictHostKeyChecking=no ubuntu@$ip \
            "export DB_PASSWORD='$DB_PASSWORD' && \
             export RDS_ENDPOINT='$RDS_ENDPOINT' && \
             chmod +x deploy-$server.sh && \
             ./deploy-$server.sh"
        
        echo -e "${GREEN}✅ $server 서버 배포 완료${NC}"
    done
}

# 배포 결과 확인
check_deployment() {
    echo -e "${YELLOW}🔍 배포 결과 확인 중...${NC}"
    
    echo "=== 서비스 접속 URL ==="
    echo "🌐 API 서버: http://$API_PUBLIC_IP:8080"
    echo "🌐 Admin 서버: http://$ADMIN_PUBLIC_IP:8081"
    echo "📊 Grafana: http://$ADMIN_PUBLIC_IP:3000 (admin/admin123)"
    echo "📈 Prometheus: http://$ADMIN_PUBLIC_IP:9090"
    echo "⚙️  Scheduler: http://$BATCH_PUBLIC_IP:8082"
    
    echo ""
    echo "=== 서비스 상태 확인 ==="
    
    # API 서버 확인
    if curl -s -f "http://$API_PUBLIC_IP:8080/actuator/health" > /dev/null; then
        echo -e "${GREEN}✅ API 서버 정상${NC}"
    else
        echo -e "${RED}❌ API 서버 응답 없음${NC}"
    fi
    
    # Admin 서버 확인  
    if curl -s -f "http://$ADMIN_PUBLIC_IP:8081/actuator/health" > /dev/null; then
        echo -e "${GREEN}✅ Admin 서버 정상${NC}"
    else
        echo -e "${RED}❌ Admin 서버 응답 없음${NC}"
    fi
    
    # Grafana 확인
    if curl -s -f "http://$ADMIN_PUBLIC_IP:3000/api/health" > /dev/null; then
        echo -e "${GREEN}✅ Grafana 정상${NC}"
    else
        echo -e "${RED}❌ Grafana 응답 없음${NC}"
    fi
}

# 메인 실행 함수
main() {
    echo "시작 시각: $(date)"
    
    check_env
    deploy_infrastructure
    get_instance_info
    update_prometheus_config
    wait_for_instances
    deploy_applications
    check_deployment
    
    echo ""
    echo -e "${GREEN}🎉 SmartMealTable 완전 분리형 배포 완료!${NC}"
    echo "종료 시각: $(date)"
    echo ""
    echo "📋 다음 단계:"
    echo "1. 서비스 URL에 접속하여 정상 작동 확인"
    echo "2. Grafana에서 모니터링 대시보드 설정" 
    echo "3. 필요시 배치 서버에서 크롤러 실행"
    echo ""
    echo "💡 크롤러 실행 방법:"
    echo "ssh -i smartmealtable-key ubuntu@$BATCH_PUBLIC_IP"
    echo "docker-compose -f docker-compose.batch.yml --profile crawler up -d crawler"
}

# 스크립트 실행
main "$@"