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
echo -e "${YELLOW}[3/5] 데이터베이스 정보 입력${NC}"
read -p "RDS_ENDPOINT (예: db.xxxxx.ap-northeast-2.rds.amazonaws.com): " RDS_ENDPOINT
read -p "DB_USERNAME (기본값: admin): " DB_USERNAME
read -sp "RDS_PASSWORD (DB_PASSWORD): " RDS_PASSWORD
echo ""
read -p "REDIS_HOST: " REDIS_HOST
read -p "REDIS_PORT (기본값: 6379): " REDIS_PORT
read -p "ADMIN_PUBLIC_IP: " ADMIN_PUBLIC_IP
read -p "ADMIN_PRIVATE_IP: " ADMIN_PRIVATE_IP
echo ""

# OAuth 및 외부 API 정보 입력
echo -e "${YELLOW}[4/5] OAuth & 외부 API 정보 입력 (선택사항)${NC}"
read -p "KAKAO_CLIENT_ID (Enter 스킵): " KAKAO_CLIENT_ID
read -p "KAKAO_REDIRECT_URI (기본값: http://localhost:5173/oauth/kakao/callback): " KAKAO_REDIRECT_URI
read -p "GOOGLE_CLIENT_ID (Enter 스킵): " GOOGLE_CLIENT_ID
read -sp "GOOGLE_CLIENT_SECRET (Enter 스킵): " GOOGLE_CLIENT_SECRET
echo ""
read -p "GOOGLE_REDIRECT_URI (기본값: http://localhost:5173/oauth/google/callback): " GOOGLE_REDIRECT_URI
read -p "NAVER_MAP_CLIENT_ID (Enter 스킵): " NAVER_MAP_CLIENT_ID
read -sp "NAVER_MAP_CLIENT_SECRET (Enter 스킵): " NAVER_MAP_CLIENT_SECRET
echo ""
echo ""

# Vertex AI 및 JWT 정보 입력
echo -e "${YELLOW}[5/5] Vertex AI & JWT 정보 입력 (선택사항)${NC}"
read -p "VERTEX_AI_PROJECT_ID (Enter 스킵): " VERTEX_AI_PROJECT_ID
read -p "VERTEX_AI_MODEL (기본값: gemini-2.5-flash): " VERTEX_AI_MODEL
read -p "VERTEX_AI_TEMPERATURE (기본값: 0.1): " VERTEX_AI_TEMPERATURE
read -p "VERTEX_AI_LOCATION (기본값: asia-northeast3): " VERTEX_AI_LOCATION
read -sp "JWT_SECRET (Enter 스킵): " JWT_SECRET
echo ""
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

# DB 및 Redis
set_secret "RDS_ENDPOINT" "$RDS_ENDPOINT"
set_secret "DB_USERNAME" "$DB_USERNAME"
set_secret "RDS_PASSWORD" "$RDS_PASSWORD"
set_secret "REDIS_HOST" "$REDIS_HOST"
set_secret "REDIS_PORT" "$REDIS_PORT"
set_secret "ADMIN_PUBLIC_IP" "$ADMIN_PUBLIC_IP"
set_secret "ADMIN_PRIVATE_IP" "$ADMIN_PRIVATE_IP"

# OAuth 및 외부 API (선택사항)
set_secret "KAKAO_CLIENT_ID" "$KAKAO_CLIENT_ID"
set_secret "KAKAO_REDIRECT_URI" "$KAKAO_REDIRECT_URI"
set_secret "GOOGLE_CLIENT_ID" "$GOOGLE_CLIENT_ID"
set_secret "GOOGLE_CLIENT_SECRET" "$GOOGLE_CLIENT_SECRET"
set_secret "GOOGLE_REDIRECT_URI" "$GOOGLE_REDIRECT_URI"
set_secret "NAVER_MAP_CLIENT_ID" "$NAVER_MAP_CLIENT_ID"
set_secret "NAVER_MAP_CLIENT_SECRET" "$NAVER_MAP_CLIENT_SECRET"

# Vertex AI 및 JWT (선택사항)
set_secret "VERTEX_AI_PROJECT_ID" "$VERTEX_AI_PROJECT_ID"
set_secret "VERTEX_AI_MODEL" "$VERTEX_AI_MODEL"
set_secret "VERTEX_AI_TEMPERATURE" "$VERTEX_AI_TEMPERATURE"
set_secret "VERTEX_AI_LOCATION" "$VERTEX_AI_LOCATION"
set_secret "JWT_SECRET" "$JWT_SECRET"

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
