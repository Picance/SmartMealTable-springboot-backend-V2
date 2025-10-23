# ✅ Terraform → GitHub Secrets 자동화 완성

## 🎯 요약

**질문**: terraform apply에서 출력된 인스턴스 ID 등을 자동으로 GitHub Secrets에 적용할 수 있는 방법이 있니?

**답**: YES! 완전 자동화된 스크립트 2개 + 가이드 문서 1개를 만들었습니다.

---

## 📦 생성된 파일

### 1️⃣ `deploy-and-setup-secrets.sh` (244줄) ⭐ 권장
**처음 배포할 때 사용**

```bash
chmod +x .github/deploy-and-setup-secrets.sh
.github/deploy-and-setup-secrets.sh
```

**자동 실행 순서**:
1. ✅ 도구 확인 (terraform, jq, gh, aws-cli)
2. ✅ Terraform init
3. ✅ Terraform validate
4. ✅ Terraform plan (사용자 확인)
5. ✅ 사용자 승인 요청
6. ✅ Terraform apply (리소스 생성)
7. ✅ Output 추출 (JSON)
8. ✅ GitHub Secrets 자동 설정 (10개)
9. ✅ 배포 정보 파일 생성
10. ✅ 결과 확인

### 2️⃣ `apply-terraform-outputs.sh` (185줄)
**Terraform 이미 실행 후 사용**

```bash
chmod +x .github/apply-terraform-outputs.sh
.github/apply-terraform-outputs.sh
```

**기능**:
- 기존 terraform.tfstate에서 output 추출
- GitHub Secrets 자동 적용
- 상황: terraform apply 이미 완료된 경우

### 3️⃣ `set-secrets.sh` (160줄)
**수동 Secrets 설정 (이미 존재)**

```bash
chmod +x .github/set-secrets.sh
.github/set-secrets.sh
```

**기능**:
- OAuth, API, JWT 등 수동 입력
- GitHub Secrets에 설정

### 4️⃣ `TERRAFORM-AUTOMATION-GUIDE.md`
**상세 가이드 문서**

- 사용 방법
- 문제 해결
- 보안 팁

---

## 🚀 실행 방법

### 처음 배포 (권장)

```bash
# 1. 필수 도구 설치
brew install terraform jq gh

# 2. AWS 자격증명 설정
aws configure

# 3. GitHub 인증
gh auth login

# 4. 완전 자동 배포
chmod +x .github/deploy-and-setup-secrets.sh
.github/deploy-and-setup-secrets.sh

# 5. 프롬프트에서 승인 (yes)
# 6. 배포 완료! 자동으로 Secrets 적용됨
```

### Terraform 이미 실행 후

```bash
# 기존 state에서 output 추출 및 Secrets 적용
chmod +x .github/apply-terraform-outputs.sh
.github/apply-terraform-outputs.sh
```

---

## 🔄 자동으로 설정되는 Secrets (10개)

| 항목 | Secret 이름 | 출처 |
|------|-------------|------|
| **EC2 API** | `EC2_API_INSTANCE_ID` | Terraform output |
| **EC2 Admin** | `EC2_ADMIN_INSTANCE_ID` | Terraform output |
| **EC2 Batch** | `EC2_BATCH_INSTANCE_ID` | Terraform output |
| **RDS 엔드포인트** | `RDS_ENDPOINT` | Terraform output |
| **RDS 호스트** | `RDS_HOST` | Terraform output |
| **Admin IP** | `ADMIN_PUBLIC_IP` | Terraform output |
| **ECR API** | `ECR_API_REPOSITORY` | Terraform output |
| **ECR Admin** | `ECR_ADMIN_REPOSITORY` | Terraform output |
| **ECR Scheduler** | `ECR_SCHEDULER_REPOSITORY` | Terraform output |
| **ECR Crawler** | `ECR_CRAWLER_REPOSITORY` | Terraform output |

---

## 📊 처리 흐름도

```
사용자: ./deploy-and-setup-secrets.sh 실행
  ↓
Step 1️⃣ : 도구 확인
  ├─ terraform ✓
  ├─ jq ✓
  ├─ gh ✓
  ├─ aws ✓
  └─ GitHub 인증 ✓
  ↓
Step 2️⃣ : Terraform 초기화
  ├─ terraform init ✓
  ├─ terraform validate ✓
  └─ terraform plan ✓
  ↓
Step 3️⃣ : 사용자 승인
  └─ "Continue? (yes/no):" 
  ↓
Step 4️⃣ : Terraform Apply
  ├─ VPC, 서브넷 생성
  ├─ EC2 3개 인스턴스 생성
  ├─ RDS MySQL 생성
  ├─ ECR 저장소 생성
  └─ IAM 역할 생성
  ↓
Step 5️⃣ : Output 추출
  └─ terraform output -json
  ↓
Step 6️⃣ : JSON 파싱 (jq)
  ├─ api_instance_id
  ├─ admin_instance_id
  ├─ batch_instance_id
  ├─ rds_endpoint
  ├─ rds_host
  ├─ api_public_ip
  ├─ admin_public_ip
  ├─ batch_public_ip
  └─ ecr_*_repository_url (4개)
  ↓
Step 7️⃣ : GitHub Secrets 설정
  └─ gh secret set (10번 반복)
  ↓
Step 8️⃣ : 배포 정보 저장
  └─ docs/DEPLOYMENT_INFO.txt 생성
  ↓
완료! 모든 Secrets 자동 설정됨 ✅
```

---

## 💾 생성되는 배포 정보 파일

`docs/DEPLOYMENT_INFO.txt` (자동 생성)

```
EC2 인스턴스
  • API:      i-0d6f611683c55c94f (54.179.xxx.xxx:8080)
  • Admin:    i-0b999ed5de6937b27 (52.xxx.xxx.xxx:8081)
  • Batch:    i-xxx (xxx.xxx.xxx.xxx)

RDS 데이터베이스
  • Endpoint: smartmealtable-db.c1234567890.ap-northeast-2.rds.amazonaws.com:3306
  • Host:     smartmealtable-db.c1234567890.ap-northeast-2.rds.amazonaws.com

ECR 저장소
  • API:      12345678901.dkr.ecr.ap-northeast-2.amazonaws.com/smartmealtable-api
  • Admin:    12345678901.dkr.ecr.ap-northeast-2.amazonaws.com/smartmealtable-admin
  • ...
```

---

## 🔒 보안 특징

✅ **자격증명 보호**
- AWS CLI 설정이나 환경변수 사용
- 로그에 노출 안됨
- Git에 커밋 안됨

✅ **Secrets 보호**
- GitHub Secrets으로 암호화
- 자동 설정되므로 노출 안됨
- 로그에 마스킹됨

✅ **승인 프로세스**
- terraform plan 실행 후 확인
- 사용자 승인 대기
- 승인 후에만 리소스 생성

✅ **오류 처리**
- 도구 설치 확인
- Terraform 검증
- GitHub 인증 확인

---

## 📝 다음 단계

### 1단계: 자동 배포
```bash
.github/deploy-and-setup-secrets.sh
```

### 2단계: 배포 정보 확인
```bash
cat docs/DEPLOYMENT_INFO.txt
```

### 3단계: GitHub Secrets 확인
```
https://github.com/Picance/SmartMealTable-springboot-backend-V2/settings/secrets/actions
```

### 4단계: 수동 Secrets 설정 (나머지 17개)
```bash
.github/set-secrets.sh
```

### 5단계: Main 브랜치에 푸시
```bash
git push origin main
```

### 6단계: GitHub Actions 배포 확인
```
https://github.com/Picance/SmartMealTable-springboot-backend-V2/actions
```

---

## 🎯 주요 특징

| 특징 | 설명 |
|------|------|
| **완전 자동화** | terraform init ~ apply ~ Secrets 설정까지 모두 자동 |
| **안전한 승인** | plan 실행 후 사용자 승인 대기 |
| **오류 처리** | 도구 및 인증 자동 확인 |
| **멱등성** | 여러 번 실행 가능, 기존 리소스는 업데이트만 수행 |
| **배포 정보** | IP, 엔드포인트, URL 자동 저장 |
| **유연성** | 2가지 방식 제공 (완전 자동 또는 Output 추출) |

---

## 📚 파일 구조

```
.github/
├── workflows/
│   └── deploy.yml                    (CD 배포 파이프라인)
├── deploy-and-setup-secrets.sh       ⭐ 완전 자동화 (처음 배포)
├── apply-terraform-outputs.sh        (Output 추출만)
└── set-secrets.sh                    (수동 Secrets 설정)

docs/
├── TERRAFORM-AUTOMATION-GUIDE.md     (상세 가이드)
└── DEPLOYMENT_INFO.txt               (자동 생성됨)

root/
├── main.tf                           (Terraform 인프라)
├── terraform.tfstate                 (자동 생성)
└── smartmealtable-key.pub            (SSH 공개 키)
```

---

## ✨ 완성!

이제 다음 과정이 **완전히 자동화**됩니다:

```
Terraform 정의 (main.tf)
    ↓
AWS 리소스 생성 (EC2, RDS, ECR)
    ↓
Output 추출 (인스턴스 ID, 엔드포인트)
    ↓
GitHub Secrets 자동 설정
    ↓
배포 정보 저장
    ↓
GitHub Actions 배포 준비 완료!
```

**이제 이 명령어 하나로 전체 배포가 시작됩니다:**

```bash
.github/deploy-and-setup-secrets.sh
```

🚀 준비 완료!
