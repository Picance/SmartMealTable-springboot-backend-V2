#!/bin/bash

# Selenium 크롤러 테스트 스크립트

echo "========================================"
echo "Selenium 크롤러 테스트"
echo "========================================"
echo ""

cd "$(dirname "$0")/../.."

# 빌드
echo "프로젝트 빌드 중..."
./gradlew :smartmealtable-batch:crawler:compileTestJava --console=plain -q

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패!"
    exit 1
fi

echo "✅ 빌드 완료"
echo ""

# 테스트 실행
echo "크롤러 실행 중..."
echo ""

./gradlew :smartmealtable-batch:crawler:test \
    --tests com.stdev.smartmealtable.batch.crawler.TestSeleniumCrawler \
    --console=plain

echo ""
echo "========================================"
echo "테스트 완료"
echo "========================================"

