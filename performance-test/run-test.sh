#!/bin/bash

# JMeter 성능 테스트 실행 스크립트

set -e

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 현재 스크립트 디렉터리
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  JMeter 성능 테스트 실행${NC}"
echo -e "${GREEN}========================================${NC}"

# JMeter 설치 확인
if ! command -v jmeter &> /dev/null; then
    echo -e "${RED}❌ JMeter가 설치되지 않았습니다.${NC}"
    echo -e "${YELLOW}설치 방법:${NC}"
    echo "  macOS: brew install jmeter"
    echo "  Linux: apt-get install jmeter 또는 yum install jmeter"
    echo "  수동 설치: https://jmeter.apache.org/download_jmeter.cgi"
    exit 1
fi

echo -e "${GREEN}✓ JMeter 설치 확인 완료${NC}"
jmeter -v | head -1

# 애플리케이션 실행 확인
echo -e "\n${YELLOW}📡 애플리케이션 상태 확인 중...${NC}"
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 애플리케이션이 실행 중입니다 (http://localhost:8080)${NC}"
else
    echo -e "${RED}❌ 애플리케이션이 실행되지 않았습니다!${NC}"
    echo -e "${YELLOW}애플리케이션을 먼저 실행해주세요:${NC}"
    echo "  cd $PROJECT_ROOT"
    echo "  docker-compose -f docker-compose.local.yml up -d"
    echo "  ./gradlew :smartmealtable-api:bootRun"
    exit 1
fi

# MySQL 연결 확인 및 테스트 데이터 삽입
echo -e "\n${YELLOW}🗄️  MySQL 연결 및 테스트 데이터 확인 중...${NC}"
if docker ps | grep -q smartmealtable-mysql; then
    echo -e "${GREEN}✓ MySQL 컨테이너 실행 중${NC}"
    
    # 테스트 데이터 존재 여부 확인
    STORE_COUNT=$(docker exec smartmealtable-mysql mysql -uroot -proot123 smartmealtable -se "SELECT COUNT(*) FROM store;" 2>/dev/null)
    
    if [ -z "$STORE_COUNT" ] || [ "$STORE_COUNT" -lt 10 ]; then
        echo -e "${YELLOW}⚠️  테스트 데이터가 부족합니다 (Store: $STORE_COUNT개)${NC}"
        echo -e "${YELLOW}📝 테스트 데이터 삽입 중...${NC}"
        
        # 기존 데이터 삭제 (Foreign Key 제약 때문에 순서 중요)
        docker exec smartmealtable-mysql mysql -uroot -proot123 smartmealtable -e "DELETE FROM food; DELETE FROM store; DELETE FROM member_group;" 2>/dev/null || true
        
        # 테스트 데이터 삽입
        if docker exec -i smartmealtable-mysql mysql -uroot -proot123 smartmealtable < "$SCRIPT_DIR/test-data.sql" 2>/dev/null; then
            echo -e "${GREEN}✓ 테스트 데이터 삽입 완료${NC}"
            
            # 삽입된 데이터 확인
            STORE_COUNT=$(docker exec smartmealtable-mysql mysql -uroot -proot123 smartmealtable -se "SELECT COUNT(*) FROM store;" 2>/dev/null)
            FOOD_COUNT=$(docker exec smartmealtable-mysql mysql -uroot -proot123 smartmealtable -se "SELECT COUNT(*) FROM food;" 2>/dev/null)
            GROUP_COUNT=$(docker exec smartmealtable-mysql mysql -uroot -proot123 smartmealtable -se "SELECT COUNT(*) FROM member_group;" 2>/dev/null)
            echo -e "${GREEN}  - Store: ${STORE_COUNT}개${NC}"
            echo -e "${GREEN}  - Food: ${FOOD_COUNT}개${NC}"
            echo -e "${GREEN}  - Group: ${GROUP_COUNT}개${NC}"
        else
            echo -e "${RED}❌ 테스트 데이터 삽입 실패${NC}"
            echo -e "${YELLOW}수동으로 삽입해주세요:${NC}"
            echo "  docker exec -i smartmealtable-mysql mysql -uroot -proot123 smartmealtable < $SCRIPT_DIR/test-data.sql"
            exit 1
        fi
    else
        echo -e "${GREEN}✓ 테스트 데이터 존재 확인 (Store: ${STORE_COUNT}개)${NC}"
    fi
else
    echo -e "${RED}❌ MySQL 컨테이너가 실행되지 않았습니다!${NC}"
    echo -e "${YELLOW}MySQL을 먼저 실행해주세요:${NC}"
    echo "  cd $PROJECT_ROOT"
    echo "  docker-compose -f docker-compose.local.yml up -d smartmealtable-mysql"
    exit 1
fi

# 결과 디렉터리 생성
RESULTS_DIR="$SCRIPT_DIR/results"
mkdir -p "$RESULTS_DIR"

# 이전 결과 백업
if [ -f "$RESULTS_DIR/test-results.jtl" ] || [ -f "$RESULTS_DIR/summary-report.csv" ]; then
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    echo -e "${YELLOW}📦 이전 테스트 결과 백업 중...${NC}"
    mkdir -p "$RESULTS_DIR/backup/backup_${TIMESTAMP}"
    mv "$RESULTS_DIR"/*.jtl "$RESULTS_DIR/backup/backup_${TIMESTAMP}/" 2>/dev/null || true
    mv "$RESULTS_DIR"/*.csv "$RESULTS_DIR/backup/backup_${TIMESTAMP}/" 2>/dev/null || true
    rm -rf "$RESULTS_DIR/html-report" 2>/dev/null || true
    echo -e "${GREEN}✓ 백업 완료: $RESULTS_DIR/backup/backup_${TIMESTAMP}/${NC}"
fi

# 테스트 파라미터 설정
BASE_URL="${BASE_URL:-http://localhost:8080}"
RAMP_UP_TIME="${RAMP_UP_TIME:-10}"
TEST_DURATION="${TEST_DURATION:-120}"

echo -e "\n${GREEN}📊 테스트 설정:${NC}"
echo "  Base URL: $BASE_URL"
echo "  Ramp-up Time: ${RAMP_UP_TIME}초"
echo "  Test Duration: ${TEST_DURATION}초"
echo "  동시 사용자: 300명 (Store: 100, Food: 100, Group: 100)"

# JMeter 실행
echo -e "\n${GREEN}🚀 JMeter 테스트 시작...${NC}"
cd "$SCRIPT_DIR/jmeter"

jmeter -n -t autocomplete-performance-test.jmx \
  -JBASE_URL="$BASE_URL" \
  -JRAMP_UP_TIME="$RAMP_UP_TIME" \
  -JTEST_DURATION="$TEST_DURATION" \
  -l "$RESULTS_DIR/test-results.jtl" \
  -e -o "$RESULTS_DIR/html-report"

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  ✅ 테스트 완료!${NC}"
echo -e "${GREEN}========================================${NC}"

# 결과 요약
echo -e "\n${GREEN}📈 테스트 결과 위치:${NC}"
echo "  JTL 파일: $RESULTS_DIR/test-results.jtl"
echo "  HTML 리포트: $RESULTS_DIR/html-report/index.html"
echo "  요약 리포트: $RESULTS_DIR/summary-report.csv"

# 간단한 통계 출력
if [ -f "$RESULTS_DIR/test-results.jtl" ]; then
    echo -e "\n${GREEN}📊 간단한 통계:${NC}"
    TOTAL_REQUESTS=$(wc -l < "$RESULTS_DIR/test-results.jtl" | tr -d ' ')
    echo "  총 요청 수: $((TOTAL_REQUESTS - 1))"
    
    # 성공률 계산
    SUCCESS_COUNT=$(grep -c ",200," "$RESULTS_DIR/test-results.jtl" || echo "0")
    SUCCESS_RATE=$(echo "scale=2; $SUCCESS_COUNT * 100 / ($TOTAL_REQUESTS - 1)" | bc)
    echo "  성공률: ${SUCCESS_RATE}%"
fi

echo -e "\n${YELLOW}💡 HTML 리포트 보기:${NC}"
echo "  open $RESULTS_DIR/html-report/index.html"

echo -e "\n${GREEN}완료!${NC}"
