#!/bin/bash

# 통합 Terraform + GitHub Secrets 배포 스크립트
# 사용법: bash ./.github/deploy-terraform-and-sync-secrets.sh [--apply|--sync-only]
# 옵션:
#   --apply       : Terraform apply 실행 (기본값)
#   --sync-only   : GitHub Secrets만 동기화 (기존 리소스 유지)
# 
# 예:
#   ./.github/deploy-terraform-and-sync-secrets.sh              # Terraform apply
#   ./.github/deploy-terraform-and-sync-secrets.sh --apply      # Terraform apply
#   ./.github/deploy-terraform-and-sync-secrets.sh --sync-only  # Secrets만 동기화

set -e

# 옵션 파싱
MODE="apply"  # 기본값: apply
if [ "$1" == "--sync-only" ]; then
    MODE="sync-only"
elif [ "$1" == "--apply" ]; then
    MODE="apply"
elif [ -n "$1" ]; then
    echo "지원하지 않는 옵션: $1"
    echo "사용법: $0 [--apply|--sync-only]"
    exit 1
fi

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Terraform 배포 + GitHub Secrets 자동 동기화              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# 필수 도구 확인
echo -e "${YELLOW}🔍 필수 도구 확인 중...${NC}"

TOOLS=("terraform" "jq" "gh" "aws")
for tool in "${TOOLS[@]}"; do
    if ! command -v "$tool" &> /dev/null; then
        echo -e "${RED}❌ $tool이 설치되어 있지 않습니다.${NC}"
        exit 1
    fi
done

if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI 인증이 필요합니다.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 모든 도구 확인 완료${NC}"
echo ""

# 프로젝트 루트 이동
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# ========================================
# Step 1: Terraform 상태 확인 및 실행
# ========================================
if [ "$MODE" == "apply" ]; then
    echo -e "${YELLOW}📋 Step 1: Terraform Plan 확인 중...${NC}"

    terraform init
    terraform plan -out=tfplan

    echo ""
    echo -e "${YELLOW}⚠️  중요: 위의 Terraform Plan을 검토하셨습니까?${NC}"
    read -p "계속 진행하시겠습니까? (yes/no): " -r CONFIRM

    if [[ ! $CONFIRM =~ ^[Yy][Ee][Ss]$ ]]; then
        echo -e "${YELLOW}작업이 취소되었습니다.${NC}"
        rm -f tfplan
        exit 0
    fi

    echo ""

    # ========================================
    # Step 2: Terraform Apply
    # ========================================
    echo -e "${YELLOW}🚀 Step 2: Terraform Apply 실행 중...${NC}"

    terraform apply tfplan
    rm -f tfplan

    echo -e "${GREEN}✓ Terraform Apply 완료${NC}"
    echo ""
else
    echo -e "${YELLOW}📋 Step 1: 기존 리소스 유지 (Secrets 동기화만 진행)${NC}"
    terraform init
    echo -e "${GREEN}✓ Terraform 초기화 완료${NC}"
    echo ""
fi

# ========================================
# Step 2(3): Terraform Output 추출
# ========================================
echo -e "${YELLOW}📊 Step 2: Terraform Output 추출 중...${NC}"

TERRAFORM_OUTPUT=$(terraform output -json)

# 필요한 값 추출
RDS_ENDPOINT=$(echo "$TERRAFORM_OUTPUT" | jq -r '.rds_endpoint.value')
RDS_PASSWORD=$(echo "$TERRAFORM_OUTPUT" | jq -r '.db_password.value')
RDS_USERNAME=$(echo "$TERRAFORM_OUTPUT" | jq -r '.db_username.value')

# EC2 인스턴스 정보
EC2_API_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.api_instance_id.value')
EC2_ADMIN_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.admin_instance_id.value')
EC2_BATCH_INSTANCE_ID=$(echo "$TERRAFORM_OUTPUT" | jq -r '.batch_instance_id.value')

# EC2의 Private IP 조회
ADMIN_PRIVATE_IP=$(aws ec2 describe-instances --instance-ids "${EC2_ADMIN_INSTANCE_ID}" \
    --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text 2>/dev/null || echo "")

# Batch 서버의 Private IP 조회
BATCH_PRIVATE_IP=$(aws ec2 describe-instances --instance-ids "${EC2_BATCH_INSTANCE_ID}" \
    --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text 2>/dev/null || echo "")

echo -e "${GREEN}✓ Output 추출 완료${NC}"
echo ""

# ========================================
# Step 3: AWS 인증 정보 추출
# ========================================
echo -e "${YELLOW}🔐 Step 3: AWS 인증 정보 추출 중...${NC}"

# AWS CLI 프로필에서 자격증명 추출
AWS_PROFILE="${AWS_PROFILE:-default}"
AWS_ACCESS_KEY_ID=$(aws configure get aws_access_key_id --profile "$AWS_PROFILE" 2>/dev/null || echo "")
AWS_SECRET_ACCESS_KEY=$(aws configure get aws_secret_access_key --profile "$AWS_PROFILE" 2>/dev/null || echo "")

if [ -z "$AWS_ACCESS_KEY_ID" ] || [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    echo -e "${RED}❌ AWS 인증 정보를 찾을 수 없습니다.${NC}"
    echo "AWS CLI 설정을 확인하세요: aws configure"
    exit 1
fi

echo -e "${GREEN}✓ AWS 인증 정보 추출 완료${NC}"
echo ""

# ========================================
# Step 4: GitHub Secrets 업데이트
# ========================================
echo -e "${YELLOW}🔄 Step 4: GitHub Secrets 업데이트 중...${NC}"

# 배열 대신 함수로 처리 (shebang 호환성 개선)
set_github_secret() {
    local key="$1"
    local value="$2"
    
    if [ -z "$value" ]; then
        echo -e "${YELLOW}⚠️  ${key}: 값이 없음 (스킵)${NC}"
        return
    fi
    
    echo -n "$value" | gh secret set "$key" 2>/dev/null
    
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
set_github_secret "BATCH_CRAWLER_URL" "http://${BATCH_PRIVATE_IP}:8082"
set_github_secret "EC2_API_INSTANCE_ID" "$EC2_API_INSTANCE_ID"
set_github_secret "EC2_ADMIN_INSTANCE_ID" "$EC2_ADMIN_INSTANCE_ID"
set_github_secret "EC2_BATCH_INSTANCE_ID" "$EC2_BATCH_INSTANCE_ID"

echo ""

# ========================================
# Step 5: 완료 및 다음 단계
# ========================================
if [ "$MODE" == "apply" ]; then
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  ✅ Terraform 배포 및 GitHub Secrets 동기화 완료!         ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
else
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  ✅ GitHub Secrets 동기화 완료! (기존 리소스 유지)        ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
fi
echo ""

echo -e "${YELLOW}📊 배포 정보:${NC}"
echo "  • RDS Endpoint: $RDS_ENDPOINT"
echo "  • Redis Host: $ADMIN_PRIVATE_IP"
echo "  • Batch Crawler URL: http://${BATCH_PRIVATE_IP}:8082"
echo "  • API Instance ID: $EC2_API_INSTANCE_ID"
echo "  • Admin Instance ID: $EC2_ADMIN_INSTANCE_ID"
echo "  • Batch Instance ID: $EC2_BATCH_INSTANCE_ID"
echo ""

echo -e "${YELLOW}🚀 다음 단계:${NC}"
echo "1. GitHub Actions 워크플로우 확인:"
echo "   https://github.com/Picance/SmartMealTable-springboot-backend-V2/actions"
echo ""
echo "2. 코드 변경사항 커밋 및 푸시:"
echo "   git add ."
echo "   git commit -m 'trigger: Deploy to AWS'"
echo "   git push origin main"
echo ""
echo "3. GitHub Actions가 자동으로 시작되어 배포가 진행됩니다!"
echo ""
echo -e "${BLUE}💡 팁: 다음번 배포시에도 이 스크립트를 실행하면 자동으로 최신 값으로 업데이트됩니다.${NC}"
echo ""

if [ "$MODE" == "sync-only" ]; then
    echo -e "${YELLOW}ℹ️  현재 실행 모드: Secrets 동기화만 (기존 리소스 유지)${NC}"
    echo "   리소스를 업데이트하려면: bash ./.github/deploy-terraform-and-sync-secrets.sh --apply"
else
    echo -e "${YELLOW}ℹ️  현재 실행 모드: Terraform 배포 + Secrets 동기화${NC}"
    echo "   다음 번에 리소스를 유지하고 Secrets만 업데이트하려면:"
    echo "   bash ./.github/deploy-terraform-and-sync-secrets.sh --sync-only"
fi
