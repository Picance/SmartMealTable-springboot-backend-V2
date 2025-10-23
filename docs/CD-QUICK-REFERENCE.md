# 📌 GitHub Actions CD 배포 - 최종 정리

## 📂 생성된 파일 구조

```
.github/
├── workflows/
│   └── deploy.yml              ← 🎯 핵심 배포 워크플로우 (심플 CD만)
├── set-secrets.sh              ← Secrets 자동 설정 스크립트
└── (기존 파일들)

docs/
├── CD-DEPLOYMENT-GUIDE.md      ← 📖 배포 방법 상세 가이드
└── CD-SUMMARY.md               ← 📋 최종 요약 (이 파일)
```

---

## 🎯 핵심: `.github/workflows/deploy.yml`

**용도**: GitHub main 브랜치 푸시 시 자동 배포

**동작**:
1. 코드 체크아웃 + Gradle 빌드 (3~5분)
2. AWS ECR 로그인
3. 4개 서비스 Docker 이미지 빌드 & ECR 푸시 (2~3분)
4. EC2에서 새 컨테이너 실행 (SSM) (1~2분)
5. Slack 알림 발송

**총 시간**: 약 6~10분

---

## 🔑 필요한 Secrets (9개 필수 + 1개 선택)

### 필수 Secrets

```
AWS_ACCESS_KEY_ID              # AWS 액세스 키
AWS_SECRET_ACCESS_KEY          # AWS 시크릿 키
EC2_API_INSTANCE_ID            # API EC2 ID
EC2_ADMIN_INSTANCE_ID          # Admin EC2 ID
EC2_SCHEDULER_INSTANCE_ID      # Scheduler EC2 ID
EC2_BATCH_INSTANCE_ID          # Batch EC2 ID
RDS_ENDPOINT                   # RDS 호스트
RDS_PASSWORD                   # MySQL 비밀번호
REDIS_HOST                     # Redis 호스트
```

### 선택 Secrets

```
SLACK_WEBHOOK                  # Slack 알림용 (선택)
```

---

## ⚡ 빠른 시작 (3단계)

### 1️⃣ Secrets 설정 (2분)

```bash
chmod +x .github/set-secrets.sh
.github/set-secrets.sh
```

**프롬프트에 입력**:
- AWS 자격증명
- EC2 인스턴스 ID × 4
- RDS, Redis 정보
- Slack Webhook (선택)

### 2️⃣ Main 브랜치 푸시 (1분)

```bash
git commit -m "chore: Deploy configuration"
git push origin main
```

### 3️⃣ 배포 확인 (1분)

```
GitHub → Actions 탭
→ "Deploy to AWS" 워크플로우 클릭
→ 로그 확인
```

**또는**:
```bash
# EC2에서 컨테이너 확인
docker logs smartmealtable-api
curl http://localhost:8080/actuator/health
```

---

## 📊 배포 흐름도

```
git push origin main
         ↓
   GitHub Actions 감지
         ↓
   📦 Gradle 빌드 (3~5분)
         ↓
   🔐 AWS ECR 로그인
         ↓
   🐳 Docker 빌드 & 푸시 (2~3분)
      ├─ smartmealtable-api
      ├─ smartmealtable-admin
      ├─ smartmealtable-scheduler
      └─ smartmealtable-crawler
         ↓
   🚀 EC2에서 컨테이너 실행 (1~2분)
      ├─ EC2 API Instance
      ├─ EC2 Admin Instance
      ├─ EC2 Scheduler Instance
      └─ EC2 Batch Instance
         ↓
   📢 Slack 알림
```

---

## 📖 문서 안내

| 문서 | 용도 |
|------|------|
| **CD-SUMMARY.md** | 이 파일 - 빠른 개요 |
| **CD-DEPLOYMENT-GUIDE.md** | 배포 상세 가이드 |
| `.github/workflows/deploy.yml` | 실제 워크플로우 정의 |

---

## ⚙️ EC2 배포 상세

각 EC2 인스턴스에서 실행되는 커맨드:

```bash
# 기존 컨테이너 중지/삭제
docker stop smartmealtable-api || true
docker rm smartmealtable-api || true

# ECR 로그인
aws ecr get-login-password --region ap-northeast-2 \
  | docker login --username AWS --password-stdin [registry]

# 새 컨테이너 실행
docker run -d --name smartmealtable-api -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://[RDS]/smartmealtable \
  -e SPRING_DATASOURCE_PASSWORD=[RDS_PASSWORD] \
  -e SPRING_REDIS_HOST=[REDIS_HOST] \
  -e JAVA_OPTS=-Xmx512m \
  [ECR_REGISTRY]/smartmealtable-api:[COMMIT_SHA]
```

---

## 🔍 모니터링

### GitHub Actions 로그
```
https://github.com/Picance/SmartMealTable-springboot-backend-V2/actions
```

### EC2 컨테이너 상태
```bash
ssh -i key.pem ec2-user@[EC2_IP]

# 컨테이너 목록
docker ps | grep smartmealtable

# 컨테이너 로그
docker logs smartmealtable-api
docker logs smartmealtable-admin

# 헬스 체크
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health  # Admin
```

---

## ⚠️ 주의사항

### 필수 사전 준비
- ✅ AWS ECR 저장소 생성 (4개)
- ✅ EC2 인스턴스 3개 생성
- ✅ RDS MySQL 생성
- ✅ Redis 생성
- ✅ EC2에 Docker 설치
- ✅ EC2에 IAM role 부여 (SSM 권한)
- ✅ RDS 보안 그룹 설정 (EC2 접근 허용)

### GitHub CLI 필수
```bash
# GitHub CLI 설치
brew install gh

# 인증
gh auth login
```

---

## 🆘 일반적인 문제

### Q1: "Failed to authenticate with AWS"
**A**: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY 확인

### Q2: "ECR repository not found"
**A**: AWS CLI로 ECR 저장소 생성
```bash
aws ecr create-repository --repository-name smartmealtable-api
```

### Q3: "SSM command failed"
**A**: EC2에 SSM IAM role 부여 필요

### Q4: "Connection refused to RDS"
**A**: RDS 보안 그룹에 EC2 보안 그룹 추가

### Q5: "Container failed to start"
**A**: EC2에서 `docker logs smartmealtable-api` 확인

---

## ✅ 체크리스트

배포 전:
- [ ] AWS ECR 저장소 4개 생성
- [ ] EC2 인스턴스 3개 생성
- [ ] RDS, Redis 생성
- [ ] EC2에 Docker, SSM agent 설치
- [ ] EC2에 IAM role (SSM 권한) 부여
- [ ] GitHub CLI 설치 및 인증

배포:
- [ ] `.github/set-secrets.sh` 실행
- [ ] GitHub Secrets 9개 등록 확인
- [ ] `git push origin main` 실행
- [ ] GitHub Actions 워크플로우 성공 확인
- [ ] EC2 컨테이너 실행 확인
- [ ] API 헬스 체크 성공 확인

---

## 🎯 다음 단계

### 즉시 (지금)
```bash
# 1. Secrets 설정
chmod +x .github/set-secrets.sh
.github/set-secrets.sh

# 2. 배포 테스트
git push origin main

# 3. 결과 모니터링
# GitHub Actions 탭 확인
```

### 이후
```bash
# 일반적인 개발 플로우
git add .
git commit -m "feat: New feature"
git push origin main
# → 자동으로 배포 시작!
```

---

## 📚 참고 자료

- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [AWS ECR](https://docs.aws.amazon.com/ecr/)
- [AWS Systems Manager](https://docs.aws.amazon.com/systems-manager/)
- `.github/workflows/deploy.yml` - 실제 구현
- `docs/CD-DEPLOYMENT-GUIDE.md` - 상세 가이드

---

## 🎉 완료!

**이제 심플하고 강력한 CD 배포 자동화가 완성되었습니다!**

```
🔄 Main 푸시 → 🏗️ 빌드 → 🐳 Docker → ☁️ ECR → 🚀 EC2 배포
```

모든 과정이 자동화되어 개발자는 코드만 푸시하면 됩니다! 🚀

---

**준비 완료! 지금 시작하세요** ⚡
