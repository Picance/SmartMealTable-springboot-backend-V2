#!/bin/bash

# SmartMealTable GitHub Actions Secrets 설정 스크립트
# 이 스크립트는 배포에 필요한 모든 Secrets을 GitHub에 자동으로 설정합니다.

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}SmartMealTable GitHub Actions Secrets 설정${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# GitHub CLI 확인
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI가 설치되어 있지 않습니다.${NC}"
    echo "설치: brew install gh"
    exit 1
fi

# GitHub 인증 확인
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI 인증이 필요합니다.${NC}"
    echo "실행: gh auth login"
    exit 1
fi

echo -e "${GREEN}✅ GitHub CLI 준비 완료${NC}"
echo ""

# AWS 자격증명 입력
echo -e "${YELLOW}[1/3] AWS 자격증명 입력${NC}"
read -p "AWS_ACCESS_KEY_ID: " AWS_ACCESS_KEY_ID
read -sp "AWS_SECRET_ACCESS_KEY: " AWS_SECRET_ACCESS_KEY
echo ""
echo ""

# EC2 인스턴스 ID 입력
echo -e "${YELLOW}[2/3] EC2 인스턴스 ID 입력${NC}"
read -p "EC2_API_INSTANCE_ID (예: i-0123456789abcdef): " EC2_API_INSTANCE_ID
read -p "EC2_ADMIN_INSTANCE_ID: " EC2_ADMIN_INSTANCE_ID
read -p "EC2_SCHEDULER_INSTANCE_ID: " EC2_SCHEDULER_INSTANCE_ID
read -p "EC2_BATCH_INSTANCE_ID: " EC2_BATCH_INSTANCE_ID
echo ""

# 데이터베이스 정보 입력
echo -e "${YELLOW}[3/3] 데이터베이스 정보 입력${NC}"
read -p "RDS_ENDPOINT (예: db.xxxxx.ap-northeast-2.rds.amazonaws.com): " RDS_ENDPOINT
read -sp "RDS_PASSWORD: " RDS_PASSWORD
echo ""
read -p "REDIS_HOST (예: redis.xxxxx.ng.0001.apn2.cache.amazonaws.com): " REDIS_HOST
echo ""

# Slack Webhook (선택사항)
read -p "SLACK_WEBHOOK (선택사항, Enter 스킵): " SLACK_WEBHOOK
echo ""

echo -e "${YELLOW}========== Secrets 설정 시작 ==========${NC}"
echo ""

# GitHub 리포지토리 확인
REPO=$(gh repo view --json nameWithOwner -q)
echo -e "리포지토리: $REPO"
echo ""

# Secrets 설정
set_secret() {
    local name=$1
    local value=$2
    if [ -z "$value" ]; then
        echo -e "${YELLOW}⊘ $name: 스킵${NC}"
    else
        echo -n "설정 중: $name... "
        echo "$value" | gh secret set "$name" --repo "$REPO"
        echo -e "${GREEN}✅${NC}"
    fi
}

# 필수 Secrets
set_secret "AWS_ACCESS_KEY_ID" "$AWS_ACCESS_KEY_ID"
set_secret "AWS_SECRET_ACCESS_KEY" "$AWS_SECRET_ACCESS_KEY"
set_secret "EC2_API_INSTANCE_ID" "$EC2_API_INSTANCE_ID"
set_secret "EC2_ADMIN_INSTANCE_ID" "$EC2_ADMIN_INSTANCE_ID"
set_secret "EC2_SCHEDULER_INSTANCE_ID" "$EC2_SCHEDULER_INSTANCE_ID"
set_secret "EC2_BATCH_INSTANCE_ID" "$EC2_BATCH_INSTANCE_ID"
set_secret "RDS_ENDPOINT" "$RDS_ENDPOINT"
set_secret "RDS_PASSWORD" "$RDS_PASSWORD"
set_secret "REDIS_HOST" "$REDIS_HOST"

# 선택 Secrets
set_secret "SLACK_WEBHOOK" "$SLACK_WEBHOOK"

echo ""
echo -e "${GREEN}========== Secrets 설정 완료! ==========${NC}"
echo ""

# 설정된 Secrets 확인
echo -e "${YELLOW}설정된 Secrets 목록:${NC}"
gh secret list --repo "$REPO"

echo ""
echo -e "${GREEN}✅ 모든 설정이 완료되었습니다!${NC}"
echo ""
echo -e "${YELLOW}다음 단계:${NC}"
echo "1. main 브랜치에 코드 변경 및 푸시"
echo "   git commit -m 'chore: Update deployment config'"
echo "   git push origin main"
echo ""
echo "2. GitHub Actions에서 배포 진행 확인"
echo "   https://github.com/$REPO/actions"
echo ""
echo "3. EC2에서 컨테이너 실행 확인"
echo "   docker logs smartmealtable-api"
