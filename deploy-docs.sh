#!/bin/bash

##############################################################################
# SmartMealTable API Documentation Deployment Script
# 
# This script automates the process of:
# 1. Running REST Docs tests (with selective test execution)
# 2. Generating HTML documentation
# 3. Copying to docs/ directory for GitHub Pages
#
# Usage:
#   ./deploy-docs.sh                          # Run all RestDocsTest
#   ./deploy-docs.sh --skip-tests             # Skip tests, use existing snippets
#   ./deploy-docs.sh --test-filter "AuthControllerRestDocsTest"  # Specific test
#   ./deploy-docs.sh --test-filter "com.stdev.smartmealtable.api.auth.*"  # Pattern match
#   ./deploy-docs.sh --auto-detect            # Auto-detect modified tests from git
##############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print functions
print_header() {
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "$1"
}

print_usage() {
    echo -e "${BLUE}사용법:${NC}"
    echo "  $0                                           # 모든 RestDocsTest 실행"
    echo "  $0 --skip-tests                              # 테스트 스킵, 기존 snippets 사용"
    echo "  $0 --test-filter 'AuthControllerRestDocsTest'  # 특정 테스트만 실행"
    echo "  $0 --test-filter 'com.stdev.*auth.*'        # 패턴으로 테스트 선택"
    echo "  $0 --auto-detect                             # git에서 수정된 테스트만 실행"
    echo ""
    echo -e "${BLUE}예시:${NC}"
    echo "  # 인증 관련 테스트만 업데이트"
    echo "  $0 --test-filter 'AuthControllerRestDocsTest'"
    echo ""
    echo "  # 여러 테스트 패턴"
    echo "  $0 --test-filter '*AddressControllerRestDocsTest|*AuthControllerRestDocsTest'"
    echo ""
    echo "  # 수정된 테스트만 자동으로 감지"
    echo "  $0 --auto-detect"
}

# Function to get modified test files from git
get_modified_tests() {
    local modified_files
    modified_files=$(git diff --name-only HEAD 2>/dev/null | grep -i "test.*\.java$" || echo "")
    
    if [ -z "$modified_files" ]; then
        print_warning "Git에서 수정된 테스트 파일을 찾을 수 없습니다."
        return 1
    fi
    
    local test_classes=""
    while IFS= read -r file; do
        # 파일 경로에서 클래스 이름 추출 (e.g., src/test/.../AuthControllerTest.java -> AuthControllerTest)
        local class_name=$(basename "$file" .java)
        if [[ "$class_name" == *"RestDocsTest" ]]; then
            test_classes+="$class_name|"
        fi
    done <<< "$modified_files"
    
    if [ -z "$test_classes" ]; then
        print_warning "수정된 RestDocsTest를 찾을 수 없습니다."
        return 1
    fi
    
    # 마지막 '|' 제거
    echo "${test_classes%|}"
}

# Function to show available RestDocsTest files
list_available_tests() {
    print_header "사용 가능한 RestDocsTest 목록"
    find . -path "*/test/java/*" -name "*RestDocsTest.java" -type f | sed 's|.*/||;s|\.java||' | sort
}

# Parse command line arguments
TEST_FILTER=""
SKIP_TESTS=false
AUTO_DETECT=false
SHOW_HELP=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --test-filter)
            TEST_FILTER="$2"
            shift 2
            ;;
        --auto-detect)
            AUTO_DETECT=true
            shift
            ;;
        --list-tests)
            list_available_tests
            exit 0
            ;;
        --help|-h)
            print_usage
            exit 0
            ;;
        *)
            print_error "알 수 없는 옵션: $1"
            print_usage
            exit 1
            ;;
    esac
done

# Main script
print_header "SmartMealTable API Documentation Deployment"

# Check if .env file exists
if [ ! -f .env ]; then
    print_error ".env file not found!"
    print_info "Please copy .env.example to .env and configure it."
    exit 1
fi
print_success ".env file exists"

# Step 1: Clean previous build
print_header "Step 1: 이전 빌드 정리"
./gradlew :smartmealtable-api:clean
print_success "정리 완료"

# Step 2: Run REST Docs tests (optional - skip if snippets exist)
if [ "$SKIP_TESTS" = false ]; then
    print_header "Step 2: REST Docs 테스트 실행"
    
    # Determine which tests to run
    if [ "$AUTO_DETECT" = true ]; then
        print_info "Git에서 수정된 테스트 감지 중..."
        TEST_FILTER=$(get_modified_tests)
        if [ $? -ne 0 ]; then
            print_warning "수정된 테스트를 자동으로 감지할 수 없습니다. 모든 RestDocsTest를 실행합니다."
            TEST_FILTER="*RestDocsTest"
        fi
    elif [ -z "$TEST_FILTER" ]; then
        TEST_FILTER="*RestDocsTest"
    fi
    
    print_info "실행 대상 테스트: $TEST_FILTER"
    print_warning "테스트 실행 중 (몇 분이 소요될 수 있습니다)..."
    
    # Set environment variables for tests
    export KAKAO_CLIENT_ID=$(grep KAKAO_CLIENT_ID .env | cut -d '=' -f2)
    export KAKAO_REDIRECT_URI=$(grep KAKAO_REDIRECT_URI .env | cut -d '=' -f2)
    export GOOGLE_CLIENT_ID=$(grep GOOGLE_CLIENT_ID .env | cut -d '=' -f2)
    export GOOGLE_CLIENT_SECRET=$(grep GOOGLE_CLIENT_SECRET .env | cut -d '=' -f2)
    export GOOGLE_REDIRECT_URI=$(grep GOOGLE_REDIRECT_URI .env | cut -d '=' -f2)
    export VERTEX_AI_PROJECT_ID=$(grep VERTEX_AI_PROJECT_ID .env | cut -d '=' -f2)
    export VERTEX_AI_MODEL=$(grep VERTEX_AI_MODEL .env | cut -d '=' -f2)
    export VERTEX_AI_TEMPERATURE=$(grep VERTEX_AI_TEMPERATURE .env | cut -d '=' -f2)
    export VERTEX_AI_LOCATION=$(grep VERTEX_AI_LOCATION .env | cut -d '=' -f2)
    
    if ./gradlew :smartmealtable-api:test --tests "$TEST_FILTER"; then
        print_success "REST Docs 테스트 통과"
    else
        print_error "일부 테스트가 실패했습니다. 위의 로그를 확인하세요."
        print_info "테스트를 스킵하려면: $0 --skip-tests"
        exit 1
    fi
else
    print_warning "테스트 스킵 (--skip-tests 플래그 감지됨)"
    print_info "build/generated-snippets 디렉토리가 존재하는지 확인하세요"
fi

# Step 3: Create snippets directory if it doesn't exist
print_header "Step 3: Snippets 디렉토리 확인"
SNIPPETS_DIR="smartmealtable-api/build/generated-snippets"
if [ ! -d "$SNIPPETS_DIR" ]; then
    print_warning "Snippets 디렉토리가 없습니다. 생성 중..."
    mkdir -p "$SNIPPETS_DIR"
    print_success "Snippets 디렉토리 생성 완료"
else
    print_success "Snippets 디렉토리 존재"
fi

# Step 4: Generate AsciiDoc HTML
print_header "Step 4: HTML 문서 생성"
if ./gradlew :smartmealtable-api:asciidoctor; then
    print_success "HTML 문서 생성 완료"
else
    print_error "HTML 문서 생성 실패"
    exit 1
fi

# Step 5: Check if HTML was generated
HTML_SOURCE="smartmealtable-api/build/docs/asciidoc/index.html"
if [ ! -f "$HTML_SOURCE" ]; then
    print_error "생성된 HTML을 찾을 수 없습니다: $HTML_SOURCE"
    exit 1
fi
print_success "생성된 HTML 확인"

# Step 6: Copy to docs directory
print_header "Step 5: docs/ 디렉토리에 복사"
DOCS_DIR="docs"
mkdir -p "$DOCS_DIR"
cp "$HTML_SOURCE" "$DOCS_DIR/api-docs.html"
print_success "api-docs.html을 docs/에 복사"

# Step 7: Generate deployment summary
print_header "Step 6: 배포 요약 생성"
cat > "$DOCS_DIR/DEPLOY_INFO.txt" << EOF
SmartMealTable API 문서 배포
============================

배포 날짜: $(date '+%Y-%m-%d %H:%M:%S')
배포자: $(whoami)
Git 커밋: $(git rev-parse --short HEAD 2>/dev/null || echo "N/A")
Git 브랜치: $(git branch --show-current 2>/dev/null || echo "N/A")

생성된 파일:
- api-docs.html: Spring REST Docs로 생성된 전체 API 문서
- README.md: 문서 인덱스 및 빠른 시작 가이드

다음 단계:
1. 생성된 문서 검토: docs/api-docs.html
2. 변경사항 커밋: git add docs/ && git commit -m "docs: Update API documentation"
3. GitHub에 푸시: git push origin main
4. GitHub Pages 활성화: Settings > Pages > Source: main/docs

자세한 정보는 docs/README.md를 참고하세요.
EOF
print_success "DEPLOY_INFO.txt 생성"

# Final summary
print_header "배포 완료!"
echo -e "${GREEN}문서가 성공적으로 배포되었습니다!${NC}\n"
echo "📄 생성된 파일:"
echo "   - docs/api-docs.html"
echo "   - docs/README.md"
echo "   - docs/DEPLOY_INFO.txt"
echo ""
echo "📝 다음 단계:"
echo "   1. 검토: open docs/api-docs.html"
echo "   2. 커밋: git add docs/ && git commit -m 'docs: Update API documentation'"
echo "   3. 푸시: git push origin main"
echo "   4. GitHub Pages 활성화 (리포지토리 설정에서)"
echo ""
echo -e "${YELLOW}참고: GitHub Pages를 설정하세요 (Settings > Pages > Source: main/docs)${NC}"
