# Terraform → GitHub Secrets 자동화 가이드

## 🎯 개요

`terraform apply`로 생성된 AWS 리소스(인스턴스 ID, RDS 엔드포인트 등)를 **자동으로 GitHub Secrets에 적용**하는 두 가지 스크립트입니다.

---

## 📦 제공되는 스크립트

### 1️⃣ `apply-terraform-outputs.sh`
**Terraform 이미 실행 후 사용**

- ✅ 이미 `terraform apply` 완료한 상태
- 기존 state에서 output 추출
- Secrets 자동 적용

**사용 시기**:
```bash
# Terraform apply 이미 완료된 경우
terraform apply
./apply-terraform-outputs.sh
```

### 2️⃣ `deploy-and-setup-secrets.sh` (권장)
**처음부터 끝까지 완전 자동화**

- ✅ `terraform init` → `validate` → `plan` → `apply`
- 자동으로 Secrets 적용
- 배포 정보 파일 생성

**사용 시기**:
```bash
# 처음 배포하는 경우
./deploy-and-setup-secrets.sh
```

---

## 🚀 빠른 시작

### Step 1: 필수 도구 설치

```bash
# macOS
brew install terraform jq gh aws-cli

# Linux (Ubuntu)
sudo apt-get install terraform jq gh awscli
```

### Step 2: AWS 자격증명 설정

```bash
aws configure
# 또는 환경변수 설정
export AWS_ACCESS_KEY_ID=xxx
export AWS_SECRET_ACCESS_KEY=xxx
export AWS_DEFAULT_REGION=ap-northeast-2
```

### Step 3: GitHub CLI 인증

```bash
gh auth login
# 브라우저에서 인증 완료
```

### Step 4: 배포 및 Secrets 설정

#### 방법 A: 완전 자동화 (처음 배포)
```bash
chmod +x .github/deploy-and-setup-secrets.sh
.github/deploy-and-setup-secrets.sh
```

**실행 순서**:
1. Terraform 도구 확인
2. Terraform init
3. Terraform validate
4. Terraform plan (확인)
5. 사용자 승인 (yes/no)
6. Terraform apply
7. Output 추출
8. GitHub Secrets 자동 적용
9. 배포 정보 파일 생성

#### 방법 B: 수동 적용 (이미 terraform apply 완료)
```bash
chmod +x .github/apply-terraform-outputs.sh
.github/apply-terraform-outputs.sh
```

---

## 📋 자동으로 적용되는 Secrets

### Terraform에서 자동 추출 (7개)

```
✓ EC2_API_INSTANCE_ID
✓ EC2_ADMIN_INSTANCE_ID
✓ EC2_BATCH_INSTANCE_ID
✓ RDS_ENDPOINT
✓ RDS_HOST
✓ ADMIN_PUBLIC_IP
✓ ECR_API_REPOSITORY
✓ ECR_ADMIN_REPOSITORY
✓ ECR_SCHEDULER_REPOSITORY
✓ ECR_CRAWLER_REPOSITORY
```

### 수동으로 설정해야 할 Secrets (17개)

```bash
# AWS 자격증명
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY

# DB 정보
DB_USERNAME
RDS_PASSWORD
REDIS_HOST
REDIS_PORT
ADMIN_PRIVATE_IP

# OAuth
KAKAO_CLIENT_ID
KAKAO_REDIRECT_URI
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
GOOGLE_REDIRECT_URI
NAVER_MAP_CLIENT_ID
NAVER_MAP_CLIENT_SECRET

# Vertex AI & JWT
VERTEX_AI_PROJECT_ID
VERTEX_AI_MODEL
VERTEX_AI_TEMPERATURE
VERTEX_AI_LOCATION
JWT_SECRET
```

**수동 설정 방법**:
```bash
chmod +x .github/set-secrets.sh
.github/set-secrets.sh
```

---

## 📊 스크립트 흐름도

### deploy-and-setup-secrets.sh

```
START
  ↓
[Step 1] 필수 도구 확인 ✓
  ↓
[Step 2] Terraform init & validate ✓
  ↓
[Step 3] Terraform plan 생성
  ↓
[Step 4] 사용자 승인 대기
  ↓
[Step 5] Terraform apply 실행 ✓
  ↓
[Step 6] Output 추출 (JSON)
  ↓
[Step 7] GitHub Secrets 자동 설정
  ├─ EC2 인스턴스 ID
  ├─ RDS 엔드포인트
  ├─ EC2 공개 IP
  └─ ECR 저장소
  ↓
[Step 8] 배포 정보 파일 생성 (DEPLOYMENT_INFO.txt)
  ↓
[Step 9] GitHub Secrets 목록 표시
  ↓
END
```

---

## 🔄 Terraform Output → Secrets 매핑

| Terraform Output | GitHub Secret |
|---|---|
| `api_instance_id` | `EC2_API_INSTANCE_ID` |
| `admin_instance_id` | `EC2_ADMIN_INSTANCE_ID` |
| `batch_instance_id` | `EC2_BATCH_INSTANCE_ID` |
| `rds_endpoint` | `RDS_ENDPOINT` |
| `rds_host` | `RDS_HOST` |
| `api_public_ip` | (저장만, Secrets 미등록) |
| `admin_public_ip` | `ADMIN_PUBLIC_IP` |
| `batch_public_ip` | (저장만, Secrets 미등록) |
| `ecr_api_repository_url` | `ECR_API_REPOSITORY` |
| `ecr_admin_repository_url` | `ECR_ADMIN_REPOSITORY` |
| `ecr_scheduler_repository_url` | `ECR_SCHEDULER_REPOSITORY` |
| `ecr_crawler_repository_url` | `ECR_CRAWLER_REPOSITORY` |

---

## 🛡️ 안전한 사용법

### 1. 실행 전 확인

```bash
# terraform plan 검토
terraform plan

# 배포할 리소스 확인
terraform plan -out=tfplan
```

### 2. Secrets 보안

```bash
# 자동 설정 후 확인
gh secret list

# 특정 Secret 확인 (필요시)
gh secret view EC2_API_INSTANCE_ID
```

### 3. 배포 정보 관리

```bash
# 배포 정보 백업
cp docs/DEPLOYMENT_INFO.txt docs/DEPLOYMENT_INFO.backup.txt

# Git에 추가 (보안주의: 민감 정보 제거)
git add docs/DEPLOYMENT_INFO.txt
git commit -m "docs: Add deployment information"
```

---

## 🆘 문제 해결

### Q1: "terraform: command not found"
```bash
# Terraform 설치
brew install terraform
```

### Q2: "jq: command not found"
```bash
# jq 설치
brew install jq
```

### Q3: "GitHub CLI 인증이 필요합니다"
```bash
# GitHub CLI 인증
gh auth login
```

### Q4: "RDS 또는 EC2 생성 실패 (PendingVerification)"
- AWS 계정 검증 대기 중
- Support에 문의하여 리전 활성화 요청
- 최대 4시간 소요

### Q5: 특정 리소스만 재생성하려면?
```bash
# EC2만 재생성
terraform apply -target=aws_instance.api

# RDS만 재생성
terraform apply -target=aws_db_instance.smartmealtable
```

### Q6: Secrets 다시 업데이트하려면?
```bash
# 기존 state 확인
terraform show

# 스크립트 재실행
./apply-terraform-outputs.sh
```

---

## 📚 다음 단계

1. **배포 정보 확인**
   ```bash
   cat docs/DEPLOYMENT_INFO.txt
   ```

2. **EC2 인스턴스 접속 확인**
   ```bash
   ssh -i smartmealtable-key.pem ec2-user@<API_PUBLIC_IP>
   ```

3. **GitHub Secrets 확인**
   ```
   https://github.com/Picance/SmartMealTable-springboot-backend-V2/settings/secrets/actions
   ```

4. **나머지 Secrets 수동 설정**
   ```bash
   .github/set-secrets.sh
   ```

5. **Main 브랜치에 푸시**
   ```bash
   git commit -m "chore: Add Terraform deployment"
   git push origin main
   ```

6. **GitHub Actions 배포 확인**
   ```
   https://github.com/Picance/SmartMealTable-springboot-backend-V2/actions
   ```

---

## 🔗 관련 파일

- `.github/deploy-and-setup-secrets.sh` - 완전 자동화 스크립트
- `.github/apply-terraform-outputs.sh` - Output 추출 스크립트
- `.github/set-secrets.sh` - 수동 Secrets 설정 스크립트
- `main.tf` - Terraform 인프라 정의
- `docs/DEPLOYMENT_INFO.txt` - 배포 정보 저장

---

## 💡 팁

### 스크립트 실행 권한 설정
```bash
chmod +x .github/*.sh
```

### 반복 실행 가능
```bash
# 여러 번 실행해도 안전 (Terraform idempotent)
./deploy-and-setup-secrets.sh
./deploy-and-setup-secrets.sh  # 다시 실행 가능
```

### 환경변수로 자동화 (CI/CD)
```bash
export AWS_ACCESS_KEY_ID=xxx
export AWS_SECRET_ACCESS_KEY=xxx
export GITHUB_TOKEN=xxx
.github/deploy-and-setup-secrets.sh
```

---

완성! 🚀
