#!/bin/bash

# Terraform Output을 GitHub Secrets에 자동 동기화
# 사용법: ./.github/sync-terraform-secrets.sh
# 또는 터미널에서 직접: bash ./.github/sync-terraform-secrets.sh

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Terraform Output → GitHub Secrets 자동 동기화            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# 필수 도구 확인
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI(gh)가 설치되어 있지 않습니다.${NC}"
    echo "설치 방법: https://cli.github.com/"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ jq가 설치되어 있지 않습니다.${NC}"
    echo "설치 방법: brew install jq"
    exit 1
fi

if ! command -v terraform &> /dev/null; then
    echo -e "${RED}❌ Terraform이 설치되어 있지 않습니다.${NC}"
    exit 1
fi

# GitHub 인증 확인
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI 인증이 필요합니다.${NC}"
    echo "먼저 'gh auth login'을 실행하세요."
    exit 1
fi

echo -e "${GREEN}✓ 필수 도구 확인 완료${NC}"
echo ""

# Terraform Output 값 추출
echo -e "${YELLOW}📋 Terraform Output 값 추출 중...${NC}"

TERRAFORM_OUTPUT=$(terraform output -json)

# 필요한 값 추출
AWS_ACCESS_KEY_ID=$(aws configure get aws_access_key_id 2>/dev/null || echo "")
AWS_SECRET_ACCESS_KEY=$(aws configure get aws_secret_access_key 2>/dev/null || echo "")
RDS_ENDPOINT=$(echo "$TERRAFORM_OUTPUT" | jq -r '.rds_endpoint.value')
RDS_PASSWORD=$(echo "$TERRAFORM_OUTPUT" | jq -r '.db_password.value')
RDS_USERNAME=$(echo "$TERRAFORM_OUTPUT" | jq -r '.db_username.value')

# EC2 인스턴스 정보
EC2_API_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.api_instance_id.value')
EC2_ADMIN_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.admin_instance_id.value')
EC2_BATCH_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.batch_instance_id.value')

# EC2의 Private IP 조회 (Scheduler는 구성되지 않았으므로 Batch 사용)
ADMIN_PRIVATE_IP=$(aws ec2 describe-instances --instance-ids "${EC2_ADMIN_INSTANCE_ID}" \
    --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text 2>/dev/null || echo "")

echo -e "${GREEN}✓ Terraform Output 추출 완료${NC}"
echo ""

# GitHub Secrets 설정
echo -e "${YELLOW}🔐 GitHub Secrets 업데이트 중...${NC}"

# 함수로 처리 (배열 인덱싱 오류 방지)
set_github_secret() {
    local key="$1"
    local value="$2"
    
    if [ -z "$value" ]; then
        echo -e "${YELLOW}⚠️  ${key}: 값이 없음 (스킵)${NC}"
        return
    fi
    
    echo -n "$value" | gh secret set "$key"
    
    # 민감한 정보는 마스킹
    if [[ "$key" == *"PASSWORD"* ]] || [[ "$key" == *"KEY"* ]]; then
        echo -e "${GREEN}✓ ${key}: ***${NC}"
    else
        echo -e "${GREEN}✓ ${key}: ${value}${NC}"
    fi
}

# 각 Secret 설정
set_github_secret "AWS_ACCESS_KEY_ID" "$AWS_ACCESS_KEY_ID"
set_github_secret "AWS_SECRET_ACCESS_KEY" "$AWS_SECRET_ACCESS_KEY"
set_github_secret "RDS_ENDPOINT" "$RDS_ENDPOINT"
set_github_secret "RDS_PASSWORD" "$RDS_PASSWORD"
set_github_secret "RDS_USERNAME" "$RDS_USERNAME"
set_github_secret "REDIS_HOST" "$ADMIN_PRIVATE_IP"
set_github_secret "EC2_API_INSTANCE_ID" "$EC2_API_INSTANCE_ID"
set_github_secret "EC2_ADMIN_INSTANCE_ID" "$EC2_ADMIN_INSTANCE_ID"
set_github_secret "EC2_BATCH_INSTANCE_ID" "$EC2_BATCH_INSTANCE_ID"

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  ✅ GitHub Secrets 동기화 완료!                           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "설정된 값:"
echo "  • AWS_ACCESS_KEY_ID: ***"
echo "  • AWS_SECRET_ACCESS_KEY: ***"
echo "  • RDS_ENDPOINT: $RDS_ENDPOINT"
echo "  • RDS_PASSWORD: ***"
echo "  • RDS_USERNAME: $RDS_USERNAME"
echo "  • REDIS_HOST: $ADMIN_PRIVATE_IP"
echo "  • EC2_API_INSTANCE_ID: $EC2_API_INSTANCE_ID"
echo "  • EC2_ADMIN_INSTANCE_ID: $EC2_ADMIN_INSTANCE_ID"
echo "  • EC2_BATCH_INSTANCE_ID: $EC2_BATCH_INSTANCE_ID"
echo ""
echo -e "${YELLOW}다음 단계:${NC}"
echo "1. GitHub Actions 워크플로우 확인: https://github.com/Picance/SmartMealTable-springboot-backend-V2/actions"
echo "2. 코드 푸시: git push origin main"
echo "3. 자동 배포 시작!"
