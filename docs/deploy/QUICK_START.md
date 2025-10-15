# SmartMealTable - 빠른 시작 가이드

## 📚 API 문서 배포 (신규)

### API 문서 자동 생성 및 배포
```bash
# 1. API 문서 생성 (REST Docs)
./deploy-docs.sh

# 2. GitHub Pages에 배포
git add docs/
git commit -m "docs: Update API documentation"
git push origin main

# 3. GitHub Pages 활성화
# Settings > Pages > Source: main/docs
```

**문서 위치**:
- **로컬**: `docs/api-docs.html` (브라우저에서 열기)
- **온라인**: `https://picance.github.io/SmartMealTable-springboot-backend-V2/`

**자세한 내용**: [DEPLOYMENT_READINESS_REPORT.md](./DEPLOYMENT_READINESS_REPORT.md)

---

## 🏠 로컬 개발 환경

### 원클릭 로컬 실행
```bash
# 모든 서비스 한 번에 실행
./local-dev.sh start

# 또는 간단히
./local-dev.sh
```

### 개별 서비스 관리
```bash
./local-dev.sh stop      # 모든 서비스 중지
./local-dev.sh restart   # 재시작
./local-dev.sh build     # 이미지 다시 빌드
./local-dev.sh logs      # 로그 확인
./local-dev.sh status    # 상태 확인
./local-dev.sh crawler   # 크롤러 실행
./local-dev.sh clean     # 완전 삭제
```

### 로컬 접속 URL
- **API 서버**: http://localhost:8080
- **Admin 서버**: http://localhost:8081  
- **스케줄러**: http://localhost:8082
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **MySQL**: localhost:3306 (smartmeal_user/smartmeal123)
- **Redis**: localhost:6379

---

## ☁️ AWS 클라우드 배포

### 한 번에 배포하기
```bash
# 1. DB 패스워드 설정
export DB_PASSWORD="your_secure_password_here"

# 2. 전체 배포 실행
./deploy-all.sh
```

## 📁 주요 파일 구성

```bash
├── main.tf                     # Terraform 인프라 정의 (완전 분리형)
├── docker-compose.api.yml      # API 서버 컨테이너 설정
├── docker-compose.admin.yml    # Admin + Redis + 모니터링 설정  
├── docker-compose.batch.yml    # 스케줄러 + 크롤러 설정
├── deploy-all.sh              # 🔥 원클릭 전체 배포 스크립트
├── deploy-api.sh              # API 서버 개별 배포
├── deploy-admin.sh            # Admin 시스템 개별 배포  
├── deploy-batch.sh            # 배치 시스템 개별 배포
└── monitoring/
    ├── prometheus.yml         # Prometheus 설정
    └── grafana/provisioning/ # Grafana 자동 설정
```

## 💰 비용 정보

- **EC2 t3.micro × 3대**: ~$30/월
- **RDS db.t3.micro**: ~$25/월  
- **Elastic IP × 3개**: ~$11/월
- **총 예상 비용**: ~$66/월

## 🔧 개별 서비스 관리

### API 서버만 재배포
```bash
ssh -i smartmealtable-key ubuntu@$(terraform output -raw api_public_ip)
./deploy-api.sh
```

### 크롤러 실행 (필요시)
```bash
ssh -i smartmealtable-key ubuntu@$(terraform output -raw batch_public_ip)
docker-compose -f docker-compose.batch.yml --profile crawler up -d crawler
```

### 모니터링 접속
- **Grafana**: http://[ADMIN_IP]:3000 (admin/admin123)
- **Prometheus**: http://[ADMIN_IP]:9090

## 📋 상세 가이드

전체 상세 가이드는 [DISTRIBUTED_DEPLOYMENT.md](DISTRIBUTED_DEPLOYMENT.md)를 참고하세요.

## ⚠️ 주의사항

1. **SSH 키**: `smartmealtable-key` 파일은 안전하게 보관하세요
2. **DB 패스워드**: 강력한 패스워드를 사용하세요  
3. **보안 그룹**: 필요한 포트만 열어두세요
4. **메모리 모니터링**: 각 인스턴스 메모리 사용량을 주기적으로 확인하세요

## 🆘 문제 해결

### 메모리 부족시
```bash
# 각 인스턴스에서 확인
free -h
docker stats
```

### 서비스 재시작
```bash
docker-compose -f docker-compose.[service].yml restart
```

### 로그 확인  
```bash
docker-compose -f docker-compose.[service].yml logs -f
```