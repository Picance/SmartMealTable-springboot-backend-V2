#!/bin/bash

# 완전 자동화: Terraform 배포 → GitHub Secrets 적용
# 사용법: ./deploy-and-setup-secrets.sh [AWS_PROFILE]
# 예: ./deploy-and-setup-secrets.sh
# 또는: AWS_PROFILE=production ./deploy-and-setup-secrets.sh

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  완전 자동화: Terraform → GitHub Secrets                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# 설정
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo -e "${YELLOW}📍 프로젝트 경로: $PROJECT_ROOT${NC}"
cd "$PROJECT_ROOT"
echo ""

# 1단계: 필수 도구 확인
echo -e "${YELLOW}🔍 Step 1: 필수 도구 확인...${NC}"

TOOLS=("terraform" "jq" "gh" "aws")
for tool in "${TOOLS[@]}"; do
    if ! command -v "$tool" &> /dev/null; then
        echo -e "${RED}❌ $tool이 설치되어 있지 않습니다.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ $tool${NC}"
done

if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI 인증이 필요합니다.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ GitHub 인증${NC}"
echo ""

# 2단계: Terraform 초기화 및 검증
echo -e "${YELLOW}🔧 Step 2: Terraform 초기화 및 검증...${NC}"

if [ ! -f "main.tf" ]; then
    echo -e "${RED}❌ main.tf를 찾을 수 없습니다.${NC}"
    exit 1
fi

terraform init
echo -e "${GREEN}✓ Terraform init 완료${NC}"

terraform validate
echo -e "${GREEN}✓ Terraform validate 완료${NC}"
echo ""

# 3단계: Terraform Plan 확인
echo -e "${YELLOW}📋 Step 3: Terraform Plan 확인...${NC}"
terraform plan -out=tfplan
echo ""

# 4단계: 사용자 승인
echo -e "${YELLOW}⚠️  중요: Terraform Apply를 진행하시겠습니까?${NC}"
echo "이 작업으로 AWS 리소스가 생성됩니다."
read -p "Continue? (yes/no): " -r CONFIRM
echo ""

if [[ ! $CONFIRM =~ ^yes$ ]]; then
    echo -e "${YELLOW}작업이 취소되었습니다.${NC}"
    exit 0
fi

# 5단계: Terraform Apply
echo -e "${YELLOW}🚀 Step 4: Terraform Apply 실행...${NC}"
terraform apply tfplan

# tfplan 파일 정리
rm -f tfplan

echo -e "${GREEN}✓ Terraform Apply 완료${NC}"
echo ""

# 6단계: Output JSON 추출
echo -e "${YELLOW}📊 Step 5: Terraform Output 추출...${NC}"

if [ ! -f "terraform.tfstate" ]; then
    echo -e "${RED}❌ terraform.tfstate를 찾을 수 없습니다.${NC}"
    exit 1
fi

TERRAFORM_OUTPUT=$(terraform output -json)

if [ -z "$TERRAFORM_OUTPUT" ]; then
    echo -e "${RED}❌ Terraform output을 가져올 수 없습니다.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Output 추출 완료${NC}"
echo ""

# 7단계: GitHub Secrets에 적용
echo -e "${YELLOW}🔐 Step 6: GitHub Secrets 적용...${NC}"

REPO=$(gh repo view --json nameWithOwner -q)
echo -e "리포지토리: ${BLUE}$REPO${NC}"
echo ""

# Secrets 설정 함수
set_secret() {
    local name=$1
    local value=$2
    if [ -z "$value" ] || [ "$value" = "null" ]; then
        echo -e "${YELLOW}⊘ $name: 값 없음${NC}"
    else
        echo -n "  ✓ $name... "
        echo "$value" | gh secret set "$name" --repo "$REPO" 2>/dev/null
        echo -e "${GREEN}OK${NC}"
    fi
}

# EC2 인스턴스 ID
echo -e "${BLUE}📍 EC2 인스턴스 ID:${NC}"
API_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.api_instance_id.value // empty')
ADMIN_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.admin_instance_id.value // empty')
BATCH_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.batch_instance_id.value // empty')

set_secret "EC2_API_INSTANCE_ID" "$API_ID"
set_secret "EC2_ADMIN_INSTANCE_ID" "$ADMIN_ID"
set_secret "EC2_BATCH_INSTANCE_ID" "$BATCH_ID"
echo ""

# RDS 정보
echo -e "${BLUE}📍 RDS 정보:${NC}"
RDS_ENDPOINT=$(echo "$TERRAFORM_OUTPUT" | jq -r '.rds_endpoint.value // empty')
RDS_HOST=$(echo "$TERRAFORM_OUTPUT" | jq -r '.rds_host.value // empty')

set_secret "RDS_ENDPOINT" "$RDS_ENDPOINT"
set_secret "RDS_HOST" "$RDS_HOST"
echo ""

# EC2 공개 IP
echo -e "${BLUE}📍 EC2 공개 IP:${NC}"
API_IP=$(echo "$TERRAFORM_OUTPUT" | jq -r '.api_public_ip.value // empty')
ADMIN_IP=$(echo "$TERRAFORM_OUTPUT" | jq -r '.admin_public_ip.value // empty')
BATCH_IP=$(echo "$TERRAFORM_OUTPUT" | jq -r '.batch_public_ip.value // empty')

set_secret "ADMIN_PUBLIC_IP" "$ADMIN_IP"
echo ""

# 8단계: 배포 정보 저장
echo -e "${YELLOW}💾 Step 7: 배포 정보 저장...${NC}"

DEPLOY_INFO_FILE="docs/DEPLOYMENT_INFO.txt"
cat > "$DEPLOY_INFO_FILE" << EOF
# SmartMealTable 배포 정보
# 생성 일시: $(date)

## EC2 인스턴스
API 인스턴스 ID:        $API_ID
API 공개 IP:            $API_IP
API URL:                http://$API_IP:8080

Admin 인스턴스 ID:      $ADMIN_ID
Admin 공개 IP:          $ADMIN_IP
Admin URL:              http://$ADMIN_IP:8081
Grafana:                http://$ADMIN_IP:3000
Prometheus:             http://$ADMIN_IP:9090

Batch 인스턴스 ID:      $BATCH_ID
Batch 공개 IP:          $BATCH_IP

## RDS 데이터베이스
RDS 엔드포인트:         $RDS_ENDPOINT
RDS 호스트:             $RDS_HOST
DB 이름:                smartmealtable
DB 사용자:              smartmeal_user

## 다음 단계
1. GitHub Secrets 확인
   https://github.com/$REPO/settings/secrets/actions

2. EC2 인스턴스 접근
   ssh -i smartmealtable-key.pem ec2-user@$API_IP

3. Docker 배포 확인
   docker ps | grep smartmealtable
   docker logs smartmealtable-api

4. GitHub Actions 배포 실행
   git push origin main
EOF

echo -e "${GREEN}✓ 배포 정보: $DEPLOY_INFO_FILE${NC}"
echo ""

# 최종 요약
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  ✅ 모든 설정이 완료되었습니다!                           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GREEN}📋 설정 요약:${NC}"
echo ""
echo "EC2 인스턴스:"
echo "  • API:      $API_ID ($API_IP:8080)"
echo "  • Admin:    $ADMIN_ID ($ADMIN_IP:8081)"
echo "  • Batch:    $BATCH_ID ($BATCH_IP)"
echo ""
echo "RDS 데이터베이스:"
echo "  • Endpoint: $RDS_ENDPOINT"
echo ""
echo "ECR 저장소:"
echo "  $(echo "$TERRAFORM_OUTPUT" | jq -r '.ecr_api_repository_url.value // empty')"
echo "  $(echo "$TERRAFORM_OUTPUT" | jq -r '.ecr_admin_repository_url.value // empty')"
echo "  $(echo "$TERRAFORM_OUTPUT" | jq -r '.ecr_scheduler_repository_url.value // empty')"
echo "  $(echo "$TERRAFORM_OUTPUT" | jq -r '.ecr_crawler_repository_url.value // empty')"
echo ""

echo -e "${YELLOW}🔐 GitHub Secrets 목록:${NC}"
echo ""
gh secret list --repo "$REPO"
echo ""

echo -e "${YELLOW}📝 다음 단계:${NC}"
echo "1. 배포 정보 파일 확인"
echo "   cat $DEPLOY_INFO_FILE"
echo ""
echo "2. 수동으로 설정해야 할 Secrets (set-secrets.sh 실행)"
echo "   RDS_PASSWORD, REDIS_HOST, OAuth 정보 등"
echo ""
echo "3. Main 브랜치에 푸시"
echo "   git push origin main"
echo ""
echo "4. GitHub Actions에서 배포 확인"
echo "   https://github.com/$REPO/actions"
echo ""
echo -e "${GREEN}✅ 준비 완료!${NC}"
