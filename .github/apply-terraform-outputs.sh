#!/bin/bash

# Terraform Output을 GitHub Secrets에 자동 적용하는 스크립트
# terraform apply 완료 후 이 스크립트를 실행하면 모든 인스턴스 ID와 엔드포인트가
# 자동으로 GitHub Secrets에 등록됩니다.

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Terraform Output → GitHub Secrets 자동 적용              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# 필수 도구 확인
echo -e "${YELLOW}🔍 필수 도구 확인...${NC}"

if ! command -v terraform &> /dev/null; then
    echo -e "${RED}❌ Terraform이 설치되어 있지 않습니다.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Terraform OK${NC}"

if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ jq가 설치되어 있지 않습니다.${NC}"
    echo "설치: brew install jq"
    exit 1
fi
echo -e "${GREEN}✓ jq OK${NC}"

if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI가 설치되어 있지 않습니다.${NC}"
    echo "설치: brew install gh"
    exit 1
fi
echo -e "${GREEN}✓ GitHub CLI OK${NC}"

if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI 인증이 필요합니다.${NC}"
    echo "실행: gh auth login"
    exit 1
fi
echo -e "${GREEN}✓ GitHub 인증 OK${NC}"
echo ""

# Terraform output 추출
echo -e "${YELLOW}📋 Terraform Output 추출 중...${NC}"

if [ ! -f "terraform.tfstate" ]; then
    echo -e "${RED}❌ terraform.tfstate 파일을 찾을 수 없습니다.${NC}"
    echo "현재 디렉토리: $(pwd)"
    exit 1
fi

# Terraform output을 JSON으로 추출
TERRAFORM_OUTPUT=$(terraform output -json)

if [ -z "$TERRAFORM_OUTPUT" ]; then
    echo -e "${RED}❌ Terraform output을 가져올 수 없습니다.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Terraform output 추출 완료${NC}"
echo ""

# GitHub 리포지토리 확인
REPO=$(gh repo view --json nameWithOwner -q)
echo -e "${BLUE}📦 리포지토리: $REPO${NC}"
echo ""

# Secrets 설정 함수
set_secret() {
    local name=$1
    local value=$2
    if [ -z "$value" ]; then
        echo -e "${YELLOW}⊘ $name: 값이 없음 (스킵)${NC}"
    else
        echo -n "  설정 중: $name... "
        echo "$value" | gh secret set "$name" --repo "$REPO"
        echo -e "${GREEN}✅${NC}"
    fi
}

# EC2 인스턴스 ID 추출 및 설정
echo -e "${YELLOW}🔧 EC2 인스턴스 ID 설정${NC}"

API_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.api_instance_id.value // empty')
ADMIN_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.admin_instance_id.value // empty')
BATCH_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.batch_instance_id.value // empty')

set_secret "EC2_API_INSTANCE_ID" "$API_INSTANCE_ID"
set_secret "EC2_ADMIN_INSTANCE_ID" "$ADMIN_INSTANCE_ID"
set_secret "EC2_BATCH_INSTANCE_ID" "$BATCH_INSTANCE_ID"
echo ""

# RDS 정보 추출 및 설정
echo -e "${YELLOW}🗄️  RDS 정보 설정${NC}"

RDS_ENDPOINT=$(echo "$TERRAFORM_OUTPUT" | jq -r '.rds_endpoint.value // empty')
RDS_HOST=$(echo "$TERRAFORM_OUTPUT" | jq -r '.rds_host.value // empty')

set_secret "RDS_ENDPOINT" "$RDS_ENDPOINT"
set_secret "RDS_HOST" "$RDS_HOST"
echo ""

# EC2 공개 IP 추출 및 설정
echo -e "${YELLOW}🌐 EC2 공개 IP 설정${NC}"

API_PUBLIC_IP=$(echo "$TERRAFORM_OUTPUT" | jq -r '.api_public_ip.value // empty')
ADMIN_PUBLIC_IP=$(echo "$TERRAFORM_OUTPUT" | jq -r '.admin_public_ip.value // empty')
BATCH_PUBLIC_IP=$(echo "$TERRAFORM_OUTPUT" | jq -r '.batch_public_ip.value // empty')

set_secret "ADMIN_PUBLIC_IP" "$ADMIN_PUBLIC_IP"
echo ""

# ECR URL 추출 및 설정
echo -e "${YELLOW}🐳 ECR 저장소 URL 설정${NC}"

ECR_API=$(echo "$TERRAFORM_OUTPUT" | jq -r '.ecr_api_repository_url.value // empty')
ECR_ADMIN=$(echo "$TERRAFORM_OUTPUT" | jq -r '.ecr_admin_repository_url.value // empty')
ECR_SCHEDULER=$(echo "$TERRAFORM_OUTPUT" | jq -r '.ecr_scheduler_repository_url.value // empty')
ECR_CRAWLER=$(echo "$TERRAFORM_OUTPUT" | jq -r '.ecr_crawler_repository_url.value // empty')

set_secret "ECR_API_REPOSITORY" "$ECR_API"
set_secret "ECR_ADMIN_REPOSITORY" "$ECR_ADMIN"
set_secret "ECR_SCHEDULER_REPOSITORY" "$ECR_SCHEDULER"
set_secret "ECR_CRAWLER_REPOSITORY" "$ECR_CRAWLER"
echo ""

# 요약 정보 출력
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  설정 완료 요약                                            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GREEN}EC2 인스턴스 ID:${NC}"
echo "  API:       $API_INSTANCE_ID"
echo "  Admin:     $ADMIN_INSTANCE_ID"
echo "  Batch:     $BATCH_INSTANCE_ID"
echo ""

echo -e "${GREEN}RDS 정보:${NC}"
echo "  Endpoint:  $RDS_ENDPOINT"
echo "  Host:      $RDS_HOST"
echo ""

echo -e "${GREEN}EC2 공개 IP:${NC}"
echo "  API:       $API_PUBLIC_IP"
echo "  Admin:     $ADMIN_PUBLIC_IP"
echo "  Batch:     $BATCH_PUBLIC_IP"
echo ""

echo -e "${GREEN}ECR 저장소:${NC}"
echo "  API:       $ECR_API"
echo "  Admin:     $ECR_ADMIN"
echo "  Scheduler: $ECR_SCHEDULER"
echo "  Crawler:   $ECR_CRAWLER"
echo ""

# GitHub Secrets 목록 확인
echo -e "${YELLOW}📝 GitHub Secrets 목록:${NC}"
echo ""
gh secret list --repo "$REPO"

echo ""
echo -e "${GREEN}✅ 모든 설정이 완료되었습니다!${NC}"
echo ""
echo -e "${YELLOW}다음 단계:${NC}"
echo "1. GitHub Secrets 확인"
echo "   https://github.com/$REPO/settings/secrets/actions"
echo ""
echo "2. .env.example 나머지 값 설정"
echo "   RDS_PASSWORD, REDIS_HOST 등을 수동으로 추가"
echo ""
echo "3. Main 브랜치에 푸시"
echo "   git push origin main"
echo ""
echo "4. GitHub Actions 배포 확인"
echo "   https://github.com/$REPO/actions"
