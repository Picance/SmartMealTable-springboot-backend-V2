# Terraform 수정 완료 - 설정 가이드

## ✅ 완료된 수정 사항

다음의 Critical 문제들이 수정되었습니다:

### 1. ✅ aws_instance.app 참조 제거
- **변경**: Line 517-524 제거
- **결과**: Terraform validation 오류 해결
- **파일**: `main.tf`

### 2. ✅ 로드밸런서 메트릭 제거
- **변경**: CloudWatch 대시보드 메트릭 재설정
- **기존**: AWS/ApplicationELB → ALB 성능 메트릭
- **신규**: EC2 네트워크 트래픽 메트릭
- **파일**: `main.tf` Line 824-840

### 3. ✅ RDS 엔드포인트 출력 형식 수정
- **기존**: `aws_db_instance.smartmealtable.endpoint` (잘못된 형식)
- **신규**: `"${aws_db_instance.smartmealtable.address}:${aws_db_instance.smartmealtable.port}"`
- **추가**: `rds_host` 출력값 추가 (호스트만 추출)
- **파일**: `main.tf` Line 902-910

### 4. ✅ EC2 인스턴스 Key pair 참조 수정
- **변경 내용**:
  - `key_name = "smartmealtable-key"` → `key_name = aws_key_pair.smartmealtable.key_name`
  - 3개 모든 인스턴스 (api, admin, batch) 수정
  - 명시적 의존성 설정으로 순서 보장

- **수정된 인스턴스**:
  - API 서버 (Line 226)
  - Admin 서버 (Line 298)
  - Batch 서버 (Line 370)

### 5. ✅ 공개 IP 명시적 할당
- **추가**: `associate_public_ip_address = true`
- **영향**: 3개 모든 인스턴스에 추가
- **결과**: 인스턴스 생성 직후 공개 IP 할당 보장

### 6. ✅ deploy-admin.sh 컨테이너 이름 동적화
- **변경**: 하드코딩된 컨테이너 이름 → 동적 조회
- **기존**: `docker exec smartmealtableV2_redis_1`
- **신규**: `REDIS_CONTAINER=$(docker ps --filter "name=redis" -q | head -1)`
- **파일**: `deploy-admin.sh` Line 31-42

---

## 🔍 사전 배포 점검 리스트

배포 전 반드시 확인하세요:

### 1. SSH 키 페어 준비
```bash
# 공개키 파일 확인
ls -la smartmealtable-key.pub

# 없다면 생성
ssh-keygen -t rsa -b 2048 -f smartmealtable-key -N ""

# 파일 확인
ls -la smartmealtable-key*
# smartmealtable-key (private key)
# smartmealtable-key.pub (public key)
```

### 2. AWS 자격증명 설정
```bash
# AWS CLI 자격증명 확인
cat ~/.aws/credentials

# 또는 환경변수 설정
export AWS_ACCESS_KEY_ID="your_access_key"
export AWS_SECRET_ACCESS_KEY="your_secret_key"
export AWS_DEFAULT_REGION="ap-northeast-2"
```

### 3. 환경 변수 설정
```bash
# DB 패스워드 설정 (강력한 패스워드 사용)
export DB_PASSWORD="YourStrongPassword123!@#"
export TF_VAR_db_password="$DB_PASSWORD"

# 확인
echo $TF_VAR_db_password
```

### 4. Terraform 초기화
```bash
cd /Users/luna/Desktop_nonsync/project/smartmealtableV2/SmartMealTable-springboot-backend-V2

# Terraform 초기화
terraform init

# 백엔드 설정 (선택사항)
# terraform init -backend-config="bucket=your-bucket" ...
```

### 5. Terraform 검증
```bash
# Syntax 검증
terraform validate
# ✅ 성공: "Success! The configuration is valid."

# 계획 검증
terraform plan
# ✅ 성공: Plan 출력, 오류 없음
```

---

## 🚀 배포 단계별 가이드

### 단계 1: 인프라 배포 (5~10분)
```bash
# 전체 배포 한 번에 실행
export DB_PASSWORD="YourStrongPassword123!@#"
export TF_VAR_db_password="$DB_PASSWORD"

./deploy-all.sh
```

**또는 단계별 실행**:

```bash
# Step 1: Terraform 적용
terraform init
terraform apply -auto-approve

# Step 2: 인스턴스 정보 수집
export API_PUBLIC_IP=$(terraform output -raw api_public_ip)
export ADMIN_PUBLIC_IP=$(terraform output -raw admin_public_ip)
export BATCH_PUBLIC_IP=$(terraform output -raw batch_public_ip)
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)

# Step 3: Prometheus 설정 업데이트 (필요시)
# ...

# Step 4: 인스턴스 부팅 대기 (60~90초)
sleep 90

# Step 5: 배포 스크립트 실행
export DB_PASSWORD="YourStrongPassword123!@#"

# API 서버 배포
ssh -i smartmealtable-key ubuntu@$API_PUBLIC_IP \
  "cd SmartMealTable-springboot-backend-V2 && \
   export DB_PASSWORD='$DB_PASSWORD' && \
   export RDS_ENDPOINT='$RDS_ENDPOINT' && \
   ./deploy-api.sh"

# Admin 서버 배포
ssh -i smartmealtable-key ubuntu@$ADMIN_PUBLIC_IP \
  "cd SmartMealTable-springboot-backend-V2 && \
   export DB_PASSWORD='$DB_PASSWORD' && \
   export RDS_ENDPOINT='$RDS_ENDPOINT' && \
   ./deploy-admin.sh"

# Batch 서버 배포
ssh -i smartmealtable-key ubuntu@$BATCH_PUBLIC_IP \
  "cd SmartMealTable-springboot-backend-V2 && \
   export DB_PASSWORD='$DB_PASSWORD' && \
   export RDS_ENDPOINT='$RDS_ENDPOINT' && \
   ./deploy-batch.sh"
```

---

## 📊 예상 배포 결과

### Terraform Apply 출력 예시
```
Apply complete! Resources have been created.

Outputs:

api_instance_id = "i-1234567890abcdef0"
admin_instance_id = "i-0987654321fedcba0"
batch_instance_id = "i-abcdef1234567890"

api_public_ip = "203.0.113.1"
admin_public_ip = "203.0.113.2"
batch_public_ip = "203.0.113.3"

api_url = "http://203.0.113.1:8080"
admin_url = "http://203.0.113.2:8081"
grafana_url = "http://203.0.113.2:3000"
prometheus_url = "http://203.0.113.2:9090"

rds_endpoint = "smartmealtable-db.xxxxx.ap-northeast-2.rds.amazonaws.com:3306"
rds_host = "smartmealtable-db.xxxxx.ap-northeast-2.rds.amazonaws.com"
```

### 서비스 접속 정보
| 서비스 | URL | 인스턴스 | 포트 |
|--------|-----|--------|------|
| API | http://API_IP:8080 | api | 8080 |
| Admin | http://ADMIN_IP:8081 | admin | 8081 |
| Grafana | http://ADMIN_IP:3000 | admin | 3000 |
| Prometheus | http://ADMIN_IP:9090 | admin | 9090 |
| Scheduler | http://BATCH_IP:8082 | batch | 8082 |

---

## ⚠️ 주의사항

### 1. SSH 키 보안
```bash
# SSH 키 권한 설정
chmod 600 smartmealtable-key
chmod 644 smartmealtable-key.pub
```

### 2. DB 패스워드 관리
- ✅ **강력한 패스워드 사용**: 최소 12자, 대소문자, 숫자, 특수문자 포함
- ❌ **공유 금지**: 절대 버전 관리에 저장하지 말 것
- ✅ **환경변수 사용**: Shell history에 남지 않도록 주의

### 3. 보안 그룹 설정 확인
```bash
# 현재 설정 (완전 개방)
- 8080 (API): 0.0.0.0/0 (모두 허용)
- 8081 (Admin): 0.0.0.0/0 (모두 허용)
- 22 (SSH): 0.0.0.0/0 (모두 허용)

# 권장: 프로덕션 환경에서는 제한
- 8080: 로드밸런서 또는 특정 IP만 허용
- 22: 점프 호스트 또는 특정 IP만 허용
```

### 4. 메모리 모니터링
```bash
# 각 인스턴스 메모리 확인
ssh -i smartmealtable-key ubuntu@$API_PUBLIC_IP "free -h"

# Docker 메모리 사용량
ssh -i smartmealtable-key ubuntu@$API_PUBLIC_IP "docker stats"

# 메모리 부족 시 대응
# - API만 t3.small로 업그레이드
# - Batch 작업 최소화
# - Redis 캐시 정책 조정
```

---

## 🔧 배포 후 최적화 (선택사항)

### 1. CloudWatch 로깅 활성화
```bash
# 각 인스턴스에서 CloudWatch Agent 상태 확인
ssh -i smartmealtable-key ubuntu@$API_PUBLIC_IP \
  "sudo systemctl status amazon-cloudwatch-agent"
```

### 2. Auto-scaling 설정 (선택사항)
```bash
# API 서버만 Auto Scaling 그룹에 추가 가능
# - Min: 1, Max: 3
# - CPU 사용률 70% 초과 시 자동 확장
```

### 3. 로드밸런서 추가 (선택사항)
- 현재는 Elastic IP 직접 사용
- 필요시 ALB 추가 가능 (비용 증가)

---

## 🆘 배포 후 문제 해결

### API 서버가 시작되지 않는 경우
```bash
# 1. 로그 확인
ssh -i smartmealtable-key ubuntu@$API_PUBLIC_IP
docker logs $(docker ps -aq --filter "name=api") -f

# 2. RDS 연결 확인
docker exec $(docker ps -aq --filter "name=api") curl -f http://localhost:8080/actuator/health

# 3. 메모리 확인
docker stats
free -h
```

### Redis 연결 오류
```bash
# Redis 상태 확인
ssh -i smartmealtable-key ubuntu@$ADMIN_PUBLIC_IP
docker exec $(docker ps -aq --filter "name=redis") redis-cli ping

# Redis 로그 확인
docker logs $(docker ps -aq --filter "name=redis")
```

### 배포 스크립트 재실행
```bash
# 배포 스크립트 다시 실행
ssh -i smartmealtable-key ubuntu@$API_PUBLIC_IP \
  "cd SmartMealTable-springboot-backend-V2 && \
   export DB_PASSWORD='$DB_PASSWORD' && \
   export RDS_ENDPOINT='$RDS_ENDPOINT' && \
   ./deploy-api.sh"
```

---

## 📝 배포 기록

- **검토 날짜**: 2025-10-23
- **수정 항목**: 6개 (Critical 5개, High 1개)
- **상태**: ✅ 배포 준비 완료

---

**다음 단계**: `terraform apply` 실행

