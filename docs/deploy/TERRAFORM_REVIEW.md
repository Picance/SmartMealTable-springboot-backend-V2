# Terraform 배포 검토 보고서

## 🔍 검토 결과 요약

`terraform apply` 실행 시 **여러 심각한 오류**가 발생하여 배포가 **실패**할 것으로 예상됩니다.

**심각도별 문제 개수:**
- 🔴 **치명적(Critical)**: 5개
- 🟠 **높음(High)**: 3개
- 🟡 **중간(Medium)**: 2개

---

## 🔴 CRITICAL 문제점 (즉시 수정 필요)

### 1. **aws_instance.app 리소스 참조 오류**
**위치**: `main.tf` Line 517-524

```terraform
resource "aws_eip" "app" {
  instance = aws_instance.app.id  # ❌ 존재하지 않는 리소스
  # ...
}

output "instance_id" {
  value = aws_instance.app.id  # ❌ 존재하지 않는 리소스
  # ...
}
```

**문제**: `aws_instance.app` 리소스가 정의되지 않음. 실제로 `api`, `admin`, `batch` 리소스만 존재함.

**영향**: 
- `terraform plan` 단계에서 **즉시 실패**
- 배포 전 validation 오류 발생

**수정안**:
```terraform
# 이 부분 제거 (또는 여러 리소스로 분리)
# resource "aws_eip" "app" { ... }
# output "instance_id" { ... }

# 대신 아래 리소스들이 이미 올바르게 정의되어 있음 (Line 953-1020)
resource "aws_eip" "api" { ... }
resource "aws_eip" "admin" { ... }
resource "aws_eip" "batch" { ... }
```

---

### 2. **aws_lb (Load Balancer) 리소스 미정의**
**위치**: `main.tf` Line 848

```terraform
metrics = [
  ["AWS/ApplicationELB", "TargetResponseTime", "LoadBalancer", aws_lb.app.arn_suffix],
  # ❌ aws_lb.app 리소스가 정의되지 않음
]
```

**문제**: CloudWatch 대시보드에서 존재하지 않는 로드밸런서를 참조함.

**영향**:
- CloudWatch 대시보드 생성 시 **오류 발생 가능**
- 로드밸런서가 아예 생성되지 않아 대시보드 메트릭 수집 실패

**현재 아키텍처**: Elastic IP를 직접 사용하는 완전 분리형 (로드밸런서 불필요)

**수정안**:
```terraform
# 옵션 1: 로드밸런서 관련 메트릭 제거
# (현재 완전 분리형 구조에 불필요)

# 옵션 2: 로드밸런서 구현 추가 (권장하지 않음 - 비용 증가)
resource "aws_lb" "app" {
  name               = "smartmealtable-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.ec2.id]
  subnets            = [aws_subnet.public_1.id, aws_subnet.public_2.id]
}
```

---

### 3. **RDS 엔드포인트 출력 형식 오류**
**위치**: `main.tf` Line 902

```terraform
output "rds_endpoint" {
  value = aws_db_instance.smartmealtable.endpoint  # ❌ 틀린 형식
}
```

**문제**: RDS endpoint는 `{host}:{port}` 형식이지만, 배포 스크립트는 이를 정확히 파싱하지 못함.

**현재 출력 형식**: `smartmealtable-db.xxxxx.ap-northeast-2.rds.amazonaws.com:3306`

**docker-compose 환경변수 사용**:
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://${RDS_ENDPOINT}/smartmealtable
# 이 경우 ${RDS_ENDPOINT}에는 host:port 형식이 필요
```

**수정안**:
```terraform
output "rds_endpoint" {
  value = aws_db_instance.smartmealtable.address  # ✅ host만 추출
  description = "RDS database hostname"
}

output "rds_endpoint_full" {
  value = "${aws_db_instance.smartmealtable.address}:${aws_db_instance.smartmealtable.port}"
  description = "RDS database endpoint (host:port)"
}
```

---

### 4. **deploy-admin.sh에서 컨테이너 이름 잘못됨**
**위치**: `deploy-admin.sh` Line 43

```bash
docker exec smartmealtableV2_redis_1 redis-cli ping
# ❌ 컨테이너 이름이 실제와 다를 수 있음
```

**문제**: Docker Compose 생성 컨테이너 이름이 프로젝트 이름과 서비스 이름으로 자동 생성됨.

**실제 컨테이너 이름**: `smartmealtable-admin-1` 또는 다른 형식일 수 있음.

**영향**: 헬스 체크 실패 → 배포 스크립트 오류

**수정안**:
```bash
# 컨테이너 ID 동적으로 가져오기
REDIS_CONTAINER=$(docker ps --filter "name=redis" -q | head -1)
docker exec $REDIS_CONTAINER redis-cli ping
```

---

### 5. **CloudWatch 대시보드의 잘못된 메트릭 참조**
**위치**: `main.tf` Line 831-835

```terraform
metrics = [
  ["AWS/ApplicationELB", ...],  # ❌ 로드밸런서 없음
  ["AWS/ApplicationELB", ...]
]
```

**문제**: 존재하지 않는 리소스를 메트릭으로 참조.

**영향**: Terraform apply 시 경고 또는 오류 가능성

---

## 🟠 HIGH 우선순위 문제

### 6. **SSH 키 페어 관계 설정 오류**
**위치**: `main.tf` Line 714-716

```terraform
resource "aws_key_pair" "smartmealtable" {
  key_name   = "smartmealtable-key"
  public_key = file("${path.module}/smartmealtable-key.pub")
}
```

**문제**: 
- EC2 인스턴스 3개 모두 `key_name = "smartmealtable-key"` 하드코딩
- 하지만 이 key_pair 리소스는 인스턴스 선언 이후에 나타남

**영향**: 리소스 의존성 명시적으로 정의되지 않음. 순서 문제 가능성.

**수정안**:
```terraform
# EC2 인스턴스 정의에서 의존성 추가
resource "aws_instance" "api" {
  # ... 기존 설정
  key_name = aws_key_pair.smartmealtable.key_name
  depends_on = [aws_key_pair.smartmealtable]
}

# 또는 Terraform이 자동으로 의존성 감지하도록
resource "aws_key_pair" "smartmealtable" {
  key_name   = "smartmealtable-key"
  public_key = file("${path.module}/smartmealtable-key.pub")
}

resource "aws_instance" "api" {
  # ...
  key_name = aws_key_pair.smartmealtable.key_name  # 자동 의존성
}
```

---

### 7. **Public IP 할당 누락**
**위치**: `main.tf` Line 238-244 (api instance)

```terraform
resource "aws_instance" "api" {
  ami           = "ami-0e9bfdb247cc8de84"
  instance_type = "t3.micro"
  subnet_id     = aws_subnet.public_1.id
  # ❌ associate_public_ip_address 미정의
}
```

**문제**: 기본 서브넷 설정 (`map_public_ip_on_launch = true`)에만 의존.

**영향**: 
- 인스턴스 생성 후 즉시 공개 IP 미할당
- Elastic IP 할당 전 SSH 접근 불가

**수정안**:
```terraform
resource "aws_instance" "api" {
  # ... 기존 설정
  associate_public_ip_address = true  # ✅ 명시적 설정
}
```

---

### 8. **subnet_id 할당 오류 - Public/Private 혼동**
**위치**: `main.tf` Line 362 (batch instance)

```terraform
resource "aws_instance" "batch" {
  subnet_id = aws_subnet.public_1.id  # API와 같은 서브넷 사용
}
```

**문제**: Batch 인스턴스도 public_1 서브넷 사용. 배치 작업은 public 서브넷에 있을 필요 없음.

**권장**: 
- API: public_1
- Admin: public_2
- Batch: public_1 (현재 설정) 또는 private 서브넷 (향후 개선)

**현재는 기능하지만 아키텍처 개선 필요**

---

## 🟡 MEDIUM 우선순위 문제

### 9. **terraform.tfvars 파일 미포함**
**문제**: `terraform.tfvars` 파일이 필요하지만 `.gitignore`에 포함될 가능성.

**현재 해결책**:
```bash
export TF_VAR_db_password="your_password"
terraform apply
```

**권장**: 별도 문서 작성 필요

---

### 10. **User Data에서 비동기 실행 문제**
**위치**: `main.tf` Line 266-323 (모든 인스턴스)

```bash
#!/bin/bash
set -e

# Docker 설치
# CloudWatch Agent 설치
# ... 모두 비동기로 실행 가능
```

**문제**: User Data 스크립트는 백그라운드에서 실행될 수 있음.

**현재 해결책**: deploy-all.sh에서 60초 대기 (Line 85)

**권장**: 향상된 헬스 체크 필요

---

## ✅ 현재 올바른 점

1. **VPC/Subnet 설정**: ✅ 올바름
2. **보안 그룹**: ✅ 적절한 inbound/outbound 규칙
3. **RDS 설정**: ✅ db.t3.micro, private 서브넷 사용
4. **IAM 역할**: ✅ SSM, ECR, CloudWatch 권한 포함
5. **Elastic IP**: ✅ 3개 인스턴스용 분리 정의 (일부 제외)

---

## 🔧 추천 수정 순서

| 순서 | 문제 | 심각도 | 예상 소요시간 |
|------|------|--------|------------|
| 1 | aws_instance.app 제거 | 🔴 | 5분 |
| 2 | 로드밸런서 메트릭 제거 | 🔴 | 5분 |
| 3 | RDS 엔드포인트 출력 수정 | 🔴 | 10분 |
| 4 | 배포 스크립트 수정 | 🔴 | 15분 |
| 5 | Key pair 의존성 명시 | 🟠 | 10분 |
| 6 | associate_public_ip 추가 | 🟠 | 5분 |

---

## 📋 최종 점검 사항

배포 전 필수 확인 사항:

- [ ] `aws_instance.app` 참조 모두 제거
- [ ] `aws_lb.app` 참조 제거 또는 리소스 생성
- [ ] RDS endpoint 출력 형식 수정
- [ ] deploy-admin.sh 컨테이너 이름 동적화
- [ ] SSH key pair 의존성 명시화
- [ ] `smartmealtable-key.pub` 파일 존재 확인
- [ ] `TF_VAR_db_password` 환경변수 설정 확인
- [ ] `terraform init` 실행
- [ ] `terraform plan` 오류 없음 확인
- [ ] `terraform apply` 실행

---

## 🚀 다음 단계

1. 위의 Critical 문제 5개 즉시 수정
2. `terraform plan` 검증
3. 소규모 테스트 배포 실행
4. 모니터링 및 로깅 검증

