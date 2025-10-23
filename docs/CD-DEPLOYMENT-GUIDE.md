# GitHub Actions를 통한 CD 배포 가이드

## 배포 흐름 개요

GitHub Actions를 활용한 CI/CD 배포 흐름은 다음과 같습니다.

```
1. 개발자가 코드를 변경하고 GitHub에 푸시
    ↓
2. GitHub Actions가 main 브랜치 변경을 감지하여 자동 실행
    ↓
3. Gradle로 프로젝트 빌드 (테스트 제외)
    ↓
4. AWS ECR(Elastic Container Registry)에 로그인
    ↓
5. 4개 마이크로서비스 Docker 이미지 빌드 및 ECR에 업로드
    - smartmealtable-api
    - smartmealtable-admin
    - smartmealtable-scheduler
    - smartmealtable-crawler
    ↓
6. AWS Systems Manager(SSM)를 통해 EC2에서 새로운 Docker 컨테이너 실행
    ↓
7. Slack으로 배포 완료 알림
```

---

## 상세 동작 방식

배포 과정의 각 단계는 `.github/workflows/deploy.yml` 파일에 정의되어 있습니다.

### 1) GitHub Actions가 코드 변경 감지

```yaml
on:
  push:
    branches: [ main ]  # main 브랜치에 변경이 발생하면 실행
```

- `main` 브랜치에 코드가 푸시되면 워크플로우가 자동으로 시작됩니다.

### 2) 프로젝트 빌드

```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: gradle

- name: Build with Gradle
  run: ./gradlew clean build -x test --parallel
```

- JDK 21을 설정합니다.
- `./gradlew clean build` 명령으로 프로젝트를 빌드합니다. (테스트는 제외)
- `--parallel` 옵션으로 멀티 모듈 병렬 빌드를 수행합니다.

### 3) AWS 인증 및 로그인

```yaml
- name: Configure AWS credentials
  uses: aws-actions/configure-aws-credentials@v4
  with:
    aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
    aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    aws-region: ap-northeast-2

- name: Login to Amazon ECR
  id: login-ecr
  uses: aws-actions/amazon-ecr-login@v2
```

- GitHub Secrets에 저장된 AWS 자격증명으로 인증합니다.
- AWS ECR에 로그인합니다.

### 4) Docker 이미지 빌드 및 ECR에 업로드

```yaml
- name: Build and push Docker image to ECR
  env:
    ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
    IMAGE_TAG: ${{ github.sha }}
  run: |
    # API 서비스
    docker build -t $ECR_REGISTRY/$ECR_REPOSITORY-api:$IMAGE_TAG -f Dockerfile.api .
    docker push $ECR_REGISTRY/$ECR_REPOSITORY-api:$IMAGE_TAG
    
    # Admin 서비스
    docker build -t $ECR_REGISTRY/$ECR_REPOSITORY-admin:$IMAGE_TAG -f Dockerfile.admin .
    docker push $ECR_REGISTRY/$ECR_REPOSITORY-admin:$IMAGE_TAG
    
    # Scheduler 서비스
    docker build -t $ECR_REGISTRY/$ECR_REPOSITORY-scheduler:$IMAGE_TAG -f Dockerfile.scheduler .
    docker push $ECR_REGISTRY/$ECR_REPOSITORY-scheduler:$IMAGE_TAG
    
    # Crawler 서비스
    docker build -t $ECR_REGISTRY/$ECR_REPOSITORY-crawler:$IMAGE_TAG -f Dockerfile.crawler .
    docker push $ECR_REGISTRY/$ECR_REPOSITORY-crawler:$IMAGE_TAG
```

- 각 Dockerfile(Dockerfile.api, Dockerfile.admin 등)을 사용해 이미지를 빌드합니다.
- 현재 GitHub 커밋 SHA를 이미지 태그로 사용합니다. (버전 관리)
- 빌드된 이미지를 AWS ECR에 업로드합니다.

### 5) EC2에서 새로운 Docker 컨테이너 실행

```bash
aws ssm send-command \
  --instance-ids ${{ secrets.EC2_API_INSTANCE_ID }} \
  --document-name "AWS-RunShellScript" \
  --parameters '{
    "commands": [
      "docker stop smartmealtable-api || true",
      "docker rm smartmealtable-api || true",
      "aws ecr get-login-password | docker login ...",
      "docker run -d --name smartmealtable-api -p 8080:8080 \
        -e SPRING_DATASOURCE_URL=... \
        -e SPRING_DATASOURCE_PASSWORD=${{ secrets.RDS_PASSWORD }} \
        ..."
    ]
  }'
```

- AWS Systems Manager (SSM)를 사용해 EC2에서 명령어를 실행합니다.
- 기존 컨테이너 중지 및 삭제 (`docker stop`, `docker rm`)
- ECR에서 최신 이미지를 로그인하고 가져옵니다.
- 새로운 컨테이너를 실행하며, 환경변수로 DB 정보 등을 설정합니다.
  - `SPRING_DATASOURCE_URL`: MySQL 연결 정보
  - `SPRING_DATASOURCE_PASSWORD`: DB 비밀번호
  - `SPRING_REDIS_HOST`: Redis 서버 주소
  - 기타 Spring 설정 환경변수

---

## GitHub Actions Secrets 설정

배포가 정상 작동하려면 GitHub Repository에 다음 Secrets을 설정해야 합니다.

### AWS 관련 (필수)
```
AWS_ACCESS_KEY_ID              # AWS IAM 액세스 키
AWS_SECRET_ACCESS_KEY          # AWS IAM 시크릿 키
```

### EC2 관련 (필수)
```
EC2_API_INSTANCE_ID            # API 서비스 EC2 인스턴스 ID
EC2_ADMIN_INSTANCE_ID          # Admin 서비스 EC2 인스턴스 ID
EC2_SCHEDULER_INSTANCE_ID      # Scheduler 서비스 EC2 인스턴스 ID
EC2_BATCH_INSTANCE_ID          # Crawler 서비스 EC2 인스턴스 ID
```

### 데이터베이스 관련 (필수)
```
RDS_ENDPOINT                   # RDS 호스트 주소 (예: db.xxxx.ap-northeast-2.rds.amazonaws.com)
RDS_PASSWORD                   # RDS MySQL 비밀번호
REDIS_HOST                     # Redis 호스트 주소
```

### 알림 관련 (선택)
```
SLACK_WEBHOOK                  # Slack Webhook URL (배포 완료 알림)
```

---

## Secrets 설정 방법

### 1. GitHub Repository로 이동
```
https://github.com/Picance/SmartMealTable-springboot-backend-V2
```

### 2. Settings 클릭
```
Settings → Secrets and variables → Actions
```

### 3. "New repository secret" 버튼 클릭

### 4. 위의 필수 Secrets을 하나씩 추가

예시:
```
Name: AWS_ACCESS_KEY_ID
Value: AKIAIOSFODNN7EXAMPLE

Name: RDS_ENDPOINT
Value: db.xxxxx.ap-northeast-2.rds.amazonaws.com

Name: RDS_PASSWORD
Value: your_mysql_password
```

---

## 배포 테스트

### 1. main 브랜치에 푸시
```bash
git push origin main
```

### 2. GitHub Actions 확인
```
https://github.com/Picance/SmartMealTable-springboot-backend-V2/actions
```

- "Deploy to AWS" 워크플로우가 실행되는지 확인
- 각 Step이 성공적으로 진행되는지 모니터링

### 3. 배포 완료 확인
- EC2 인스턴스에서 새 컨테이너가 실행되는지 확인
- API가 정상 응답하는지 확인
```bash
curl http://ec2-instance-ip:8080/actuator/health
```

### 4. Slack 알림 확인 (선택)
- 배포 완료 후 Slack 채널에 알림이 오는지 확인

---

## 일반적인 문제 해결

### 1. "AWS credentials not found"
**원인**: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY 미설정

**해결책**: 
- GitHub Settings → Secrets에서 AWS 자격증명 확인
- AWS IAM 사용자 액세스 키 재생성

### 2. "ECR repository not found"
**원인**: AWS ECR에 저장소가 없음

**해결책**:
```bash
# AWS CLI로 ECR 저장소 생성
aws ecr create-repository --repository-name smartmealtable-api
aws ecr create-repository --repository-name smartmealtable-admin
aws ecr create-repository --repository-name smartmealtable-scheduler
aws ecr create-repository --repository-name smartmealtable-crawler
```

### 3. "Failed to connect to instance"
**원인**: EC2 인스턴스 ID 잘못 설정

**해결책**:
- `EC2_API_INSTANCE_ID` 등의 Secrets 재확인
- EC2 대시보드에서 인스턴스 ID 확인

### 4. Docker 컨테이너 실행 실패
**원인**: 환경변수 설정 오류, RDS 연결 실패

**해결책**:
```bash
# EC2에서 컨테이너 로그 확인
docker logs smartmealtable-api
```

---

## 배포 프로세스 흐름도

```
┌─────────────────────┐
│  개발자 코드 푸시     │
│  git push origin main │
└──────────────┬──────┘
               │
               ▼
    ┌──────────────────────┐
    │ GitHub Actions 감지   │
    │ Workflow 자동 실행     │
    └──────────────┬───────┘
                   │
        ┌──────────┴──────────┬──────────┐
        │                     │          │
        ▼                     ▼          ▼
    Checkout          Set JDK 21    Gradle Build
                                        │
                         ┌──────────────┘
                         │
                         ▼
    ┌──────────────────────────────────────┐
    │    AWS 인증 & ECR 로그인              │
    └──────────────┬───────────────────────┘
                   │
        ┌──────────┴──────────┬──────────┬──────────┐
        │                     │          │          │
        ▼                     ▼          ▼          ▼
    API 빌드      Admin 빌드  Scheduler  Crawler
    및 푸시        및 푸시      빌드      빌드
                              및 푸시    및 푸시
        │                     │          │          │
        └──────────┬──────────┴──────────┴──────────┘
                   │
                   ▼
    ┌──────────────────────────────────────┐
    │  SSM을 통해 EC2에서 컨테이너 실행      │
    │  - 기존 컨테이너 중지/삭제             │
    │  - 새 컨테이너 실행                    │
    └──────────────┬───────────────────────┘
                   │
                   ▼
    ┌──────────────────────────────────────┐
    │  배포 완료 - Slack 알림                │
    └──────────────────────────────────────┘
```

---

## 주요 특징

✅ **자동화**: main 브랜치 푸시만으로 전체 배포 프로세스 자동 실행

✅ **빠른 배포**: Docker 이미지 캐시로 5분 이내 배포 완료

✅ **멀티 서비스**: 4개 마이크로서비스 병렬 빌드

✅ **환경 변수 관리**: Secrets로 민감한 정보 보호

✅ **모니터링**: Slack 알림으로 실시간 배포 상태 추적

✅ **간단한 운영**: 복잡한 스크립트 없이 GitHub Push만으로 완료

---

## 참고 자료

- `.github/workflows/deploy.yml` - 실제 워크플로우 파일
- [AWS Systems Manager 문서](https://docs.aws.amazon.com/systems-manager/)
- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
