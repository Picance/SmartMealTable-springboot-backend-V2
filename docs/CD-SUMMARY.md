# 🚀 SmartMealTable GitHub Actions CD 배포 - 최종 요약

## 생성된 파일

### 1. 워크플로우 파일
**`.github/workflows/deploy.yml`** - 심플한 CD 배포 파이프라인

**동작 흐름**:
```
main 브랜치 푸시 
  ↓
Gradle 빌드
  ↓
AWS ECR 로그인
  ↓
4개 마이크로서비스 Docker 이미지 빌드 및 푸시
  ↓
EC2에서 새 컨테이너 실행 (SSM)
  ↓
Slack 알림
```

**포함된 Step** (총 8단계):
1. 코드 체크아웃
2. JDK 21 설정
3. Gradle 빌드 (테스트 제외, 병렬 처리)
4. AWS 인증
5. ECR 로그인
6. Docker 이미지 빌드 & ECR 업로드
7. EC2에서 컨테이너 실행
8. Slack 알림

### 2. 설정 스크립트
**`.github/set-secrets.sh`** - GitHub Secrets 자동 설정

**기능**:
- 대화형 입력으로 AWS, EC2, RDS 정보 설정
- GitHub CLI를 통해 자동으로 Secrets 등록
- 설정 완료 후 리스트 출력

**사용법**:
```bash
chmod +x .github/set-secrets.sh
.github/set-secrets.sh
```

### 3. 문서
**`docs/CD-DEPLOYMENT-GUIDE.md`** - 배포 방법 상세 가이드

**목차**:
- 배포 흐름 개요
- 각 Step별 상세 설명
- Secrets 설정 방법
- 배포 테스트 방법
- 문제 해결
- 프로세스 흐름도

---

## 📊 배포 구조

```
┌─────────────────────────────────────────────────┐
│              GitHub Repository                   │
│           main 브랜치 코드 변경                   │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
        ┌────────────────────────┐
        │ GitHub Actions 감지    │
        │ deploy.yml 실행        │
        └────────────┬───────────┘
                     │
        ┌────────────▼───────────┐
        │ Gradle 빌드            │
        │ (멀티 모듈, 병렬처리)   │
        └────────────┬───────────┘
                     │
        ┌────────────▼───────────┐
        │ AWS ECR 로그인         │
        └────────────┬───────────┘
                     │
        ┌────────────▼──────────────────────┐
        │ Docker 이미지 빌드 & ECR 푸시     │
        │ ├─ smartmealtable-api            │
        │ ├─ smartmealtable-admin          │
        │ ├─ smartmealtable-scheduler      │
        │ └─ smartmealtable-crawler        │
        └────────────┬──────────────────────┘
                     │
        ┌────────────▼──────────────────────┐
        │ SSM 명령으로 EC2 배포             │
        │ ├─ EC2 API Instance              │
        │ ├─ EC2 Admin Instance            │
        │ ├─ EC2 Scheduler Instance        │
        │ └─ EC2 Batch Instance            │
        └────────────┬──────────────────────┘
                     │
        ┌────────────▼───────────┐
        │ Slack 알림 발송        │
        └────────────────────────┘
```

---

## ⚙️ 필요한 Secrets (GitHub Actions)

| Secret Name | 설명 | 예시 |
|---|---|---|
| **AWS_ACCESS_KEY_ID** | AWS IAM 액세스 키 | AKIA... |
| **AWS_SECRET_ACCESS_KEY** | AWS IAM 시크릿 키 | wJal... |
| **EC2_API_INSTANCE_ID** | API EC2 인스턴스 ID | i-0123456789abcdef |
| **EC2_ADMIN_INSTANCE_ID** | Admin EC2 인스턴스 ID | i-0123456789abcdef |
| **EC2_SCHEDULER_INSTANCE_ID** | Scheduler EC2 인스턴스 ID | i-0123456789abcdef |
| **EC2_BATCH_INSTANCE_ID** | Batch EC2 인스턴스 ID | i-0123456789abcdef |
| **RDS_ENDPOINT** | RDS 호스트 주소 | db.xxxxx.ap-northeast-2.rds.amazonaws.com |
| **RDS_PASSWORD** | MySQL 비밀번호 | your_password |
| **REDIS_HOST** | Redis 호스트 주소 | redis.xxxxx.cache.amazonaws.com |
| **SLACK_WEBHOOK** (선택) | Slack Webhook URL | https://hooks.slack.com/... |

---

## 🔧 설정 방법 (2가지)

### 방법 1️⃣: 자동 설정 (권장)

```bash
# 1. 스크립트 실행
chmod +x .github/set-secrets.sh
.github/set-secrets.sh

# 2. 프롬프트에 정보 입력
```

### 방법 2️⃣: 수동 설정

**GitHub Repository Settings**
```
Settings → Secrets and variables → Actions
→ "New repository secret" 클릭
→ 위의 Secrets 하나씩 입력
```

---

## 🚀 배포 실행

### 1단계: Secrets 설정
```bash
.github/set-secrets.sh
```

### 2단계: Main 브랜치에 푸시
```bash
git push origin main
```

### 3단계: GitHub Actions에서 확인
```
https://github.com/Picance/SmartMealTable-springboot-backend-V2/actions
```

**Deploy to AWS 워크플로우가 자동 실행됨**

### 4단계: EC2에서 배포 확인
```bash
# SSH로 EC2에 접속
ssh -i key.pem ec2-user@your-ec2-ip

# 컨테이너 상태 확인
docker ps | grep smartmealtable
docker logs smartmealtable-api

# API 헬스 체크
curl http://localhost:8080/actuator/health
```

---

## 📋 배포 과정 상세

### Step 1: Checkout & Build
- 최신 코드 체크아웃
- JDK 21 설정
- Gradle로 전체 프로젝트 빌드 (테스트 제외)
- **소요 시간**: 3~5분

### Step 2: AWS 인증
- AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY로 인증
- AWS ECR에 로그인
- **소요 시간**: 30초

### Step 3: Docker 빌드 & ECR 푸시
```
smartmealtable-api:commit_sha        (Dockerfile.api)
smartmealtable-admin:commit_sha      (Dockerfile.admin)
smartmealtable-scheduler:commit_sha  (Dockerfile.scheduler)
smartmealtable-crawler:commit_sha    (Dockerfile.crawler)
```
- **소요 시간**: 2~3분 (캐시 시 30초)

### Step 4: EC2 배포 (SSM)
각 서비스별로 다음을 실행:
1. 기존 컨테이너 중지 & 삭제
2. ECR에서 최신 이미지 로그인
3. 새 컨테이너 실행 (환경변수 포함)

**설정되는 환경변수**:
```
SPRING_APPLICATION_NAME: smartmealtable-api 등
SERVER_PORT: 8080/8081 (API/Admin)
SPRING_DATASOURCE_URL: RDS MySQL 연결
SPRING_DATASOURCE_PASSWORD: DB 비밀번호
SPRING_REDIS_HOST: Redis 호스트
SPRING_JPA_HIBERNATE_DDL_AUTO: validate
JAVA_OPTS: -Xmx256m ~ 512m
```

- **소요 시간**: 1~2분

### Step 5: Slack 알림
- 배포 성공/실패 알림
- Commit SHA, Branch 정보 포함

---

## 🎯 주요 특징

| 특징 | 설명 |
|------|------|
| **자동화** | Main 푸시 → 배포 완료까지 자동 |
| **빠른 배포** | 전체 프로세스 5~10분 |
| **멀티 서비스** | 4개 마이크로서비스 동시 배포 |
| **환경 분리** | 각 서비스별 독립 EC2 인스턴스 |
| **버전 관리** | GitHub Commit SHA를 Docker 태그로 사용 |
| **환경변수 관리** | Secrets로 민감 정보 보호 |
| **실시간 모니터링** | GitHub Actions 로그 확인 |
| **알림** | Slack으로 배포 상태 알림 |

---

## ✅ 체크리스트

배포 전에 다음을 확인하세요:

- [ ] AWS IAM 사용자 생성 및 액세스 키 발급
- [ ] AWS ECR 저장소 생성 (smartmealtable-api, admin, scheduler, crawler)
- [ ] EC2 인스턴스 3개 생성 및 ID 확인
- [ ] RDS MySQL 생성 및 접속 정보 확인
- [ ] Redis 생성 및 호스트 주소 확인
- [ ] EC2에 Docker 설치 확인
- [ ] GitHub Repository Settings 접근 가능 확인
- [ ] GitHub CLI 설치 및 인증 완료
- [ ] Slack Webhook 생성 (선택)

---

## 🔍 모니터링

### 실시간 로그 확인
```
GitHub Repository → Actions 탭
→ "Deploy to AWS" 워크플로우 클릭
→ "Build & Deploy to EC2" Job 클릭
→ 각 Step 로그 확인
```

### EC2 컨테이너 상태
```bash
# 컨테이너 실행 상태
docker ps | grep smartmealtable

# 컨테이너 로그
docker logs smartmealtable-api
docker logs smartmealtable-admin

# 환경변수 확인
docker inspect smartmealtable-api | grep -A 20 Env
```

### 네트워크 확인
```bash
# 포트 확인
netstat -tlnp | grep 8080
netstat -tlnp | grep 8081

# 헬스 체크
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

---

## ⚠️ 일반적인 문제

### 문제 1: "Failed to assume IAM role"
**원인**: EC2에 IAM role이 없음

**해결**:
```bash
# EC2에 IAM role 부여 필요
# AWS Console → EC2 → Instance → Modify IAM role
```

### 문제 2: "Connection refused to RDS"
**원인**: EC2에서 RDS 접근 불가

**해결**:
- RDS 보안 그룹에 EC2 보안 그룹 추가
- RDS 포트 3306 확인

### 문제 3: "Docker image not found in ECR"
**원인**: ECR 저장소 미생성

**해결**:
```bash
aws ecr create-repository --repository-name smartmealtable-api
aws ecr create-repository --repository-name smartmealtable-admin
aws ecr create-repository --repository-name smartmealtable-scheduler
aws ecr create-repository --repository-name smartmealtable-crawler
```

### 문제 4: "SSM command failed"
**원인**: EC2에 SSM 권한 없음

**해결**:
- EC2 IAM role에 `AmazonSSMManagedInstanceCore` 정책 추가

---

## 📚 참고 파일

| 파일 | 용도 |
|------|------|
| `.github/workflows/deploy.yml` | 배포 워크플로우 정의 |
| `.github/set-secrets.sh` | Secrets 자동 설정 |
| `docs/CD-DEPLOYMENT-GUIDE.md` | 배포 상세 가이드 |
| `Dockerfile.api`, `Dockerfile.admin` 등 | 서비스별 Docker 빌드 파일 |
| `docker-compose.yml` | 로컬 개발용 Compose (참고) |

---

## 🎉 완료!

**이제 다음과 같은 자동화 배포 환경이 준비되었습니다:**

✅ 개발자가 `git push origin main`만 하면
✅ GitHub Actions가 자동으로 빌드/배포 수행
✅ AWS ECR에 이미지 저장
✅ EC2에서 새 컨테이너 자동 실행
✅ Slack으로 배포 완료 알림 발송

**모든 과정이 자동화되어 운영 효율성이 극대화됩니다! 🚀**

---

## 다음 단계

1. **Secrets 설정**
   ```bash
   .github/set-secrets.sh
   ```

2. **첫 배포 테스트**
   ```bash
   git commit -m "chore: Initialize CD deployment"
   git push origin main
   ```

3. **GitHub Actions 모니터링**
   ```
   https://github.com/Picance/SmartMealTable-springboot-backend-V2/actions
   ```

4. **EC2 배포 확인**
   ```bash
   ssh ec2-user@your-ec2-ip
   docker logs smartmealtable-api
   ```

준비가 완료되면 언제든지 배포가 시작됩니다! 🎯
