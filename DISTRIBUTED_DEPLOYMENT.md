# 완전 분리형 배포 가이드 - t3.micro 3대 구성

## 아키텍처 개요

비용 최적화를 위해 t3.micro (1GB RAM) 인스턴스 3대로 **완전 분리 배포**합니다.
각 인스턴스는 고유한 Elastic IP를 가지며, 포트로 서비스를 구분합니다.

### 인스턴스 구성

```
    인터넷
       │
   ┌───┴───┐
   │사용자  │
   └───┬───┘
       │
┌──────▼──────────────────────────────────────────────────────┐
│                직접 접근 (포트별 구분)                         │
└────┬─────────────────┬─────────────────┬──────────────────┘
     │                 │                 │
┌────▼────┐      ┌────▼────┐      ┌────▼────┐
│  EIP 1  │      │  EIP 2  │      │  EIP 3  │
│:8080    │      │:8081    │      │:8082    │
└────┬────┘      └────┬────┘      └─────────┘
     │                 │
┌────▼────┐      ┌────▼────┐      ┌─────────┐
│인스턴스1 │      │인스턴스2 │      │인스턴스3 │
│API 서버 │      │Admin+기타│      │배치작업 │
└─────────┘      └─────────┘      └─────────┘

┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
│   인스턴스 1       │  │   인스턴스 2       │  │   인스턴스 3       │
│   (API 서버)      │  │ (Admin + Redis)   │  │   (배치 작업)      │
│                   │  │                   │  │                   │
│ - API 모듈        │  │ - Admin 모듈      │  │ - Scheduler       │
│ - Port: 8080      │  │ - Redis          │  │ - Crawler         │
│ - RAM: ~700MB     │  │ - Prometheus     │  │ - Port: 8082      │  -> 필요시에만 실행
│ - EIP: XX.XX.XX.1 │  │ - Grafana        │  │ - RAM: ~800MB     │
│                   │  │ - Port: 8081     │  │ - EIP: XX.XX.XX.3 │
│                   │  │ - RAM: ~900MB    │  │                   │
│                   │  │ - EIP: XX.XX.XX.2 │  │                   │
└───────────────────┘  └───────────────────┘  └───────────────────┘
```

## 메모리 사용량 예상

### 인스턴스 1 (API 서버) - t3.micro (1GB)
- API 애플리케이션: 512MB
- OS + Docker: 400MB
- 여유 공간: 112MB

### 인스턴스 2 (Admin + Redis + 모니터링) - t3.micro (1GB)  
- Admin 애플리케이션: 400MB
- Redis: 100MB
- Prometheus: 200MB (최소 구성)
- Grafana: 150MB (최소 구성)
- OS + Docker: 150MB

### 인스턴스 3 (배치 작업) - t3.micro (1GB)
- Scheduler: 300MB
- Crawler: 400MB (필요시에만 실행)
- OS + Docker: 300MB

## 배포 방법

### 1. 인프라 배포 (Terraform)
```bash
cd /Users/luna/Desktop/WorkSpace/Project/2025/smartmealtableV2

# DB 패스워드 설정
export TF_VAR_db_password="your_secure_password_here"

# Terraform 실행
terraform init
terraform plan
terraform apply
```

### 2. SSH 키 페어 생성 (최초 1회)
```bash
# SSH 키 생성
ssh-keygen -t rsa -b 2048 -f smartmealtable-key

# 공개키를 terraform에서 사용할 수 있도록 위치 확인
ls -la smartmealtable-key*
```

### 3. 각 인스턴스별 배포

#### 🏗️ 파일 구성 확인
배포 전 다음 파일들이 준비되어 있는지 확인:
- `docker-compose.api.yml` - API 서버용
- `docker-compose.admin.yml` - Admin + 모니터링용  
- `docker-compose.batch.yml` - 배치 작업용
- `deploy-api.sh` - API 배포 스크립트
- `deploy-admin.sh` - Admin 배포 스크립트
- `deploy-batch.sh` - 배치 배포 스크립트
- `monitoring/prometheus.yml` - Prometheus 설정
- `monitoring/grafana/provisioning/` - Grafana 설정

### 3. 배포 스크립트

#### deploy-api.sh (인스턴스 1용)
```bash
#!/bin/bash
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export ADMIN_INSTANCE_PRIVATE_IP=$(aws ec2 describe-instances --instance-ids $(terraform output -raw admin_instance_id) --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text)

docker-compose -f docker-compose.api.yml up -d
```

#### deploy-admin.sh (인스턴스 2용)  
```bash
#!/bin/bash
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)

docker-compose -f docker-compose.admin.yml up -d
```

#### deploy-batch.sh (인스턴스 3용)
```bash
#!/bin/bash
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export ADMIN_INSTANCE_PRIVATE_IP=$(aws ec2 describe-instances --instance-ids $(terraform output -raw admin_instance_id) --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text)

# 스케줄러만 상시 실행
docker-compose -f docker-compose.batch.yml up -d scheduler

# 크롤러는 필요시에만 실행
# docker-compose -f docker-compose.batch.yml --profile crawler up -d crawler
```

## 운영 가이드

### 메모리 모니터링
```bash
# 각 인스턴스에서 메모리 사용량 확인
free -h
docker stats

# CloudWatch에서 메모리 사용량 모니터링
# Grafana 대시보드: http://${ADMIN_INSTANCE_IP}:3000
```

### 배치 작업 실행
```bash
# 인스턴스 3에서 크롤러 실행 (필요시)
ssh -i smartmealtable-key.pem ubuntu@${BATCH_INSTANCE_IP}
docker-compose -f docker-compose.batch.yml --profile crawler up -d crawler

# 작업 완료 후 중지
docker-compose -f docker-compose.batch.yml --profile crawler down
```

### 비용 최적화 팁
1. **배치 인스턴스**: 배치 작업이 없을 때는 중지 가능
2. **모니터링**: 개발 환경에서는 Prometheus/Grafana 비활성화 가능  
3. **스케일링**: 트래픽 증가시 API 인스턴스만 t3.small로 업그레이드

### 접속 정보 (완전 분리형)
- **API**: http://${API_PUBLIC_IP}:8080
- **Admin**: http://${ADMIN_PUBLIC_IP}:8081
- **Grafana**: http://${ADMIN_PUBLIC_IP}:3000 (admin/admin123)  
- **Prometheus**: http://${ADMIN_PUBLIC_IP}:9090
- **스케줄러**: http://${BATCH_PUBLIC_IP}:8082 (관리용)

## 월 예상 비용 (서울 리전)
- EC2 t3.micro × 3대: ~$30/월
- RDS db.t3.micro: ~$25/월  
- Elastic IP × 3개: ~$11/월 (연결된 인스턴스는 무료)
- **총 예상 비용: ~$66/월** (ALB 대비 $25 절약!)

## 확장 계획
1. **트래픽 증가시**: API 인스턴스를 t3.small로 업그레이드
2. **고가용성 필요시**: 각 인스턴스를 다중 AZ로 확장
3. **성능 개선 필요시**: RDS를 db.t3.small로 업그레이드