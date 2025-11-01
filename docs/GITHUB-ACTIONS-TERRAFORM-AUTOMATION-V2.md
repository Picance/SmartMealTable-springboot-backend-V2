# 🚀 GitHub Actions + Terraform 자동 배포 가이드 (v2)

## 📋 목차
1. [개요](#개요)
2. [전체 워크플로우](#전체-워크플로우)
3. [사용 방법](#사용-방법) ⭐ **여기부터 시작!**
4. [자동화 구성](#자동화-구성)
5. [문제 해결](#문제-해결)

---

## 개요

이 프로젝트는 **Terraform → GitHub Secrets → GitHub Actions → ECR → EC2** 형태의 완전 자동화된 배포 파이프라인을 제공합니다.

### 🎯 핵심 특징
- ✅ **Terraform**으로 AWS 리소스 관리
- ✅ **자동 동기화**: Terraform output → GitHub Secrets
- ✅ **기존 리소스 유지**: 이미 생성된 리소스는 건드리지 않음
- ✅ **CI/CD 파이프라인**: GitHub Actions로 자동 배포
- ✅ **ECR 이미지 관리**: Docker 이미지 중앙 관리
- ✅ **EC2 자동 배포**: AWS Systems Manager 사용

---

## 전체 워크플로우

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  Step 1: Terraform 배포 (옵션)                                  │
│  ├─ AWS 리소스 생성/업데이트 (EC2, RDS, Redis 등)               │
│  └─ terraform.tfstate에 출력값 저장                             │
│                                                                 │
│  Step 2: GitHub Secrets 자동 동기화                             │
│  ├─ Terraform output에서 값 추출                                │
│  └─ GitHub Secrets에 자동 등록                                  │
│      (RDS_ENDPOINT, EC2_INSTANCE_ID 등)                         │
│                                                                 │
│  Step 3: GitHub Actions 트리거 (코드 푸시)                      │
│  ├─ GitHub에서 코드 풀                                          │
│  ├─ Gradle 빌드                                                 │
│  └─ Docker 이미지 생성                                          │
│                                                                 │
│  Step 4: ECR에 이미지 푸시                                      │
│  └─ AWS ECR에 Docker 이미지 저장                                │
│                                                                 │
│  Step 5: EC2 자동 배포                                          │
│  ├─ Systems Manager로 EC2 커맨드 실행                          │
│  ├─ ECR에서 이미지 풀                                           │
│  └─ Docker 컨테이너 시작                                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 사용 방법

### ⭐ 가장 추천하는 방법: 기존 리소스 유지, Secrets만 동기화

**상황**: AWS 리소스가 이미 생성되어 있는 경우 (일반적인 상황)

```bash
bash ./.github/deploy-terraform-and-sync-secrets.sh --sync-only
```

**이 스크립트가 수행하는 작업:**
1. ✅ **AWS 리소스는 건드리지 않음** ← 중요!
2. ✅ Terraform 상태 확인
3. ✅ Terraform output에서 모든 값 추출
4. ✅ GitHub Secrets 자동 업데이트
5. ✅ 완료!

**언제 사용할까?**
- GitHub Secrets이 만료되었을 때
- EC2 인스턴스 ID가 변경되었을 때
- AWS 환경 설정이 변경되었을 때
- 단순히 Secrets을 최신 상태로 유지하고 싶을 때

---

### 옵션 1: Terraform 배포 + Secrets 동기화

**상황**: 처음 배포하거나 AWS 리소스를 변경하고 싶을 때

```bash
bash ./.github/deploy-terraform-and-sync-secrets.sh --apply

# 또는 기본값이 apply이므로:
bash ./.github/deploy-terraform-and-sync-secrets.sh
```

**이 스크립트가 수행하는 작업:**
1. ✅ Terraform 초기화 및 검증
2. ✅ Terraform plan 확인 (사용자 승인 필요)
3. ✅ Terraform apply 실행 (AWS 리소스 생성/업데이트)
4. ✅ AWS 인증 정보 추출
5. ✅ GitHub Secrets 자동 업데이트
6. ✅ 완료!

---

### 옵션 2: Terraform 없이 Secrets만 동기화

**상황**: Terraform이 없고, 단순히 기존 Secrets을 업데이트하고 싶을 때

```bash
bash ./.github/sync-terraform-secrets.sh
```

---

### 옵션 3: GitHub Actions 수동 트리거

**상황**: Secrets이 이미 설정되어 있고, 코드 변경 후 배포하고 싶을 때

```bash
# 코드 변경
git add .
git commit -m "feat: Add new API endpoint"
git push origin main
# GitHub Actions 자동 시작!
```

또는 GitHub 웹 UI에서:
1. GitHub 저장소 → Actions 탭
2. "Deploy to AWS" 워크플로우 클릭
3. "Run workflow" 클릭

---

## 자동화 구성

### 1️⃣ GitHub Secrets 설정

자동으로 설정되는 Secrets:

| Secret 이름 | 설명 | 자동 설정 | 수동 설정 |
|------------|------|---------|---------|
| `AWS_ACCESS_KEY_ID` | AWS IAM Access Key | ✅ | ⭐ |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM Secret Key | ✅ | ⭐ |
| `RDS_ENDPOINT` | RDS 데이터베이스 엔드포인트 | ✅ | - |
| `RDS_PASSWORD` | RDS 암호 | ✅ | - |
| `RDS_USERNAME` | RDS 사용자명 | ✅ | - |
| `REDIS_HOST` | Redis 호스트 (Admin EC2 Private IP) | ✅ | - |
| `EC2_API_INSTANCE_ID` | API EC2 인스턴스 ID | ✅ | - |
| `EC2_ADMIN_INSTANCE_ID` | Admin EC2 인스턴스 ID | ✅ | - |
| `EC2_BATCH_INSTANCE_ID` | Batch EC2 인스턴스 ID | ✅ | - |

**⭐ 수동 설정 필요:**

GitHub 웹 UI에서 수동으로 설정해야 하는 항목:
- `AWS_ACCESS_KEY_ID`: AWS IAM 콘솔에서 생성
- `AWS_SECRET_ACCESS_KEY`: AWS IAM 콘솔에서 생성

또는 터미널에서 AWS CLI 프로필이 이미 설정되어 있다면 자동으로 추출됨:
```bash
aws configure list
```

### 2️⃣ 배포 순서 (Secrets 동기화만)

```bash
# Step 1: Terraform 상태 확인
terraform init

# Step 2: Terraform output에서 값 추출
terraform output -json

# Step 3: AWS 인증 정보 추출
aws configure get aws_access_key_id

# Step 4: GitHub Secrets에 모든 값 등록
gh secret set RDS_ENDPOINT <value>
gh secret set EC2_API_INSTANCE_ID <value>
# ... 기타

# Step 5: 완료!
```

---

## 문제 해결

### ❌ "GitHub CLI 인증이 필요합니다"

```bash
gh auth login
# 또는
gh auth refresh
```

### ❌ "AWS 인증 정보를 찾을 수 없습니다"

```bash
# AWS CLI 프로필 확인
aws configure list

# 또는 프로필 지정
export AWS_PROFILE=your-profile-name
bash ./.github/deploy-terraform-and-sync-secrets.sh --sync-only
```

### ❌ Terraform output 값이 빈 상태

```bash
# Terraform 상태 확인
terraform state list

# 특정 output 확인
terraform output <output_name>

# 전체 output 확인
terraform output -json
```

### ❌ "기존 리소스가 있는데 Terraform이 재생성하려고 함"

**문제**: `--apply` 옵션을 사용했을 때 기존 리소스를 삭제하고 재생성하려고 함

**해결책**: `--sync-only` 옵션을 사용하세요!

```bash
# ❌ 잘못된 사용
bash ./.github/deploy-terraform-and-sync-secrets.sh --apply

# ✅ 올바른 사용
bash ./.github/deploy-terraform-and-sync-secrets.sh --sync-only
```

### ❌ GitHub Actions 배포 실패

1. GitHub 저장소 → Actions 탭에서 로그 확인
2. 실패한 단계 상세 보기
3. 일반적인 원인:
   - AWS 인증 정보 오류
   - ECR 리포지토리 없음
   - EC2 인스턴스 ID 오류
   - Systems Manager IAM 권한 부족

---

## 📊 파일 구조

```
.github/
├── workflows/
│   └── deploy.yml                           # GitHub Actions 워크플로우
├── deploy-terraform-and-sync-secrets.sh     # 통합 배포 스크립트 (권장)
├── sync-terraform-secrets.sh                # Secrets 동기화만
└── deploy-and-setup-secrets.sh              # 기존 배포 스크립트

docs/
├── GITHUB-ACTIONS-TERRAFORM-AUTOMATION.md   # 기존 가이드
└── GITHUB-ACTIONS-TERRAFORM-AUTOMATION-V2.md # 이 파일 (최신)
```

---

## 🔄 반복 배포 프로세스

### 시나리오 1: 코드만 변경

```bash
git add .
git commit -m "feat: Add new API endpoint"
git push origin main
# GitHub Actions 자동 시작 → ECR 이미지 빌드 → EC2 자동 배포
```

### 시나리오 2: 인프라 변경 (EC2 크기 조정 등)

```bash
# Terraform 수정
vim main.tf

# Terraform 배포 + Secrets 동기화
bash ./.github/deploy-terraform-and-sync-secrets.sh --apply

# 자동으로:
# 1. AWS 리소스 업데이트
# 2. GitHub Secrets 최신화
# 3. GitHub Actions에 전달

# 코드 푸시하면 GitHub Actions 자동 시작
git add .
git commit -m "infra: Resize EC2 instances"
git push origin main
```

### 시나리오 3: Secrets만 갱신 (일반적)

```bash
# GitHub Secrets 갱신 (기존 리소스 유지)
bash ./.github/deploy-terraform-and-sync-secrets.sh --sync-only

# 완료!
```

### 시나리오 4: 인프라 삭제

```bash
# Terraform destroy
terraform destroy

# GitHub Secrets 정리 (수동)
gh secret delete RDS_ENDPOINT
gh secret delete RDS_PASSWORD
# ... 기타 secrets 삭제
```

---

## ✅ 체크리스트

배포 전 확인사항:

- [ ] AWS CLI 설치 및 설정 완료 (`aws configure`)
- [ ] GitHub CLI 설치 및 인증 완료 (`gh auth login`)
- [ ] Terraform 초기화 완료 (`terraform init`)
- [ ] AWS 인증 정보 (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY) 준비
- [ ] GitHub 저장소에서 Actions 권한 활성화
- [ ] EC2 인스턴스에 Systems Manager 권한 설정
- [ ] ECR 리포지토리 생성 (또는 Terraform에서 자동 생성)

---

## 📚 참고 자료

- [AWS Terraform Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [AWS Systems Manager Session Manager](https://docs.github.com/en/actions/deployment/deploying-to-your-cloud-provider/deploying-to-amazon-elastic-container-service)
- [Amazon ECR](https://docs.aws.amazon.com/AmazonECR/latest/userguide/)

---

## 🎯 최종 정리

### 일반적인 사용 패턴 (추천)

```bash
# 처음 한 번만
gh auth login

# 일반적인 배포 (리소스는 이미 생성되어 있는 경우)
bash ./.github/deploy-terraform-and-sync-secrets.sh --sync-only

# 코드 푸시
git push origin main

# GitHub Actions가 자동으로 배포 진행!
```

### 리소스를 변경해야 할 때

```bash
# Terraform 수정
vim main.tf

# Terraform 배포 + Secrets 동기화
bash ./.github/deploy-terraform-and-sync-secrets.sh --apply

# 코드 푸시
git push origin main
```

---

**✅ 이제 기존 리소스를 건드리지 않으면서 Secrets을 자동으로 동기화할 수 있습니다!**

문제가 있으시면 `--sync-only` 옵션을 사용하세요. 기존 AWS 리소스는 절대 변경되지 않습니다! 🔒
