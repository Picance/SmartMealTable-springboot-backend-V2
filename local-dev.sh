#!/bin/bash
# 로컬 개발 환경 실행 스크립트

set -e

echo "🚀 SmartMealTable 로컬 개발 환경 시작"
echo "======================================="

# .env 파일 로드
if [ -f .env ]; then
    echo "📋 .env 파일 로드 중..."
    set -a
    source .env
    set +a
    echo "✅ 환경 변수 로드 완료"
else
    echo "⚠️  .env 파일을 찾을 수 없습니다. 기본 설정으로 실행됩니다."
fi

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 도움말 함수
show_help() {
    echo "사용법: $0 [옵션]"
    echo ""
    echo "옵션:"
    echo "  start     모든 서비스 시작 (기본값)"
    echo "  stop      모든 서비스 중지"
    echo "  restart   모든 서비스 재시작"
    echo "  build     Gradle 빌드 및 Docker 이미지 다시 빌드"
    echo "  logs      서비스 로그 확인"
    echo "  status    서비스 상태 확인"
    echo "  crawler   크롤러 실행"
    echo "  clean     볼륨 포함 완전 삭제"
    echo "  help      이 도움말 표시"
}

# 서비스 상태 확인
check_services() {
    echo -e "${YELLOW}📊 서비스 상태 확인 중...${NC}"
    
    # 컨테이너 상태
    echo "=== 컨테이너 상태 ==="
    docker-compose -f docker-compose.local.yml ps
    
    echo ""
    echo "=== 서비스 헬스체크 ==="
    
    services=("mysql:3306" "redis:6379" "api:8080" "admin:8081" "scheduler:8082" "prometheus:9090" "grafana:3000")
    
    for service in "${services[@]}"; do
        name=${service%:*}
        port=${service#*:}
        
        if nc -z localhost $port 2>/dev/null; then
            echo -e "${GREEN}✅ $name (포트 $port) - 정상${NC}"
        else
            echo -e "${RED}❌ $name (포트 $port) - 응답 없음${NC}"
        fi
    done
}

# 서비스 시작
start_services() {
    echo -e "${YELLOW}🚀 서비스 시작 중...${NC}"
    
    # 로그 디렉토리 생성
    mkdir -p logs
    
    # 서비스 시작
    docker-compose -f docker-compose.local.yml up -d
    
    echo -e "${YELLOW}⏳ 서비스 초기화 대기 중... (30초)${NC}"
    sleep 30
    
    check_services
    
    echo ""
    echo -e "${GREEN}✅ 로컬 개발 환경이 시작되었습니다!${NC}"
    echo ""
    echo "🌐 접속 URL:"
    echo "  • API 서버: http://localhost:8080"
    echo "  • Admin 서버: http://localhost:8081"
    echo "  • 스케줄러: http://localhost:8082"
    echo "  • Grafana: http://localhost:3000 (admin/admin123)"
    echo "  • Prometheus: http://localhost:9090"
    echo ""
    echo "🗄️  데이터베이스:"
    echo "  • MySQL: localhost:3306 (smartmeal_user/smartmeal123)"
    echo "  • Redis: localhost:6379"
}

# 서비스 중지
stop_services() {
    echo -e "${YELLOW}🛑 서비스 중지 중...${NC}"
    docker-compose -f docker-compose.local.yml down
    echo -e "${GREEN}✅ 모든 서비스가 중지되었습니다.${NC}"
}

# 서비스 재시작
restart_services() {
    echo -e "${YELLOW}🔄 서비스 재시작 중...${NC}"
    stop_services
    start_services
}

# 이미지 빌드
build_images() {
    echo -e "${YELLOW}🔨 Docker 이미지 빌드 중...${NC}"
    # 먼저 Gradle 빌드 실행 (테스트는 기본적으로 건너뜀)
    # RUN_TESTS=true 환경변수 설정 시 테스트를 실행합니다.
    if [ -x "./gradlew" ]; then
        echo -e "${YELLOW}⚙️  Gradle wrapper로 빌드 실행 (테스트 ${GREEN}${RUN_TESTS:+실행}${RUN_TESTS:+'가능'}${NC}${YELLOW})...${NC}"
        if [ "${RUN_TESTS}" = "true" ]; then
            ./gradlew build
        else
            ./gradlew build -x test
        fi
    else
        echo -e "${YELLOW}⚙️  gradlew가 없거나 실행 불가. 전역 gradle을 사용하여 빌드 시도합니다...${NC}"
        if command -v gradle >/dev/null 2>&1; then
            if [ "${RUN_TESTS}" = "true" ]; then
                gradle build
            else
                gradle build -x test
            fi
        else
            echo -e "${RED}❌ gradlew 또는 gradle을 찾을 수 없습니다. Gradle 빌드를 건너뜁니다.${NC}"
        fi
    fi

    # Docker 이미지 빌드
    docker-compose -f docker-compose.local.yml build --no-cache
    echo -e "${GREEN}✅ 이미지 빌드가 완료되었습니다.${NC}"
}

# 로그 확인
show_logs() {
    echo -e "${YELLOW}📝 서비스 로그 확인${NC}"
    docker-compose -f docker-compose.local.yml logs -f
}

# 크롤러 실행
run_crawler() {
    echo -e "${YELLOW}🕷️  크롤러 실행 중...${NC}"
    docker-compose -f docker-compose.local.yml --profile crawler up -d crawler
    echo -e "${GREEN}✅ 크롤러가 시작되었습니다.${NC}"
    echo "로그 확인: docker-compose -f docker-compose.local.yml logs -f crawler"
}

# 완전 삭제 (볼륨 포함)
clean_all() {
    echo -e "${RED}⚠️  모든 데이터를 삭제합니다. 계속하시겠습니까? (y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo -e "${YELLOW}🧹 완전 삭제 중...${NC}"
        docker-compose -f docker-compose.local.yml down -v --remove-orphans
        docker system prune -f
        echo -e "${GREEN}✅ 완전 삭제가 완료되었습니다.${NC}"
    else
        echo "취소되었습니다."
    fi
}

# 메인 로직
case "${1:-start}" in
    "start")
        start_services
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        restart_services
        ;;
    "build")
        build_images
        ;;
    "logs")
        show_logs
        ;;
    "status")
        check_services
        ;;
    "crawler")
        run_crawler
        ;;
    "clean")
        clean_all
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        echo -e "${RED}❌ 알 수 없는 옵션: $1${NC}"
        show_help
        exit 1
        ;;
esac