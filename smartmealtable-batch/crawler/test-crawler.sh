#!/bin/bash

# 크롤러 테스트 실행 스크립트

echo "=========================================="
echo "학식 크롤러 테스트 실행"
echo "=========================================="
echo ""

# 프로젝트 루트로 이동
cd "$(dirname "$0")"/../..

echo "1. 단위 테스트 실행..."
echo ""
./gradlew :smartmealtable-batch:crawler:test --tests "*Test" --info

echo ""
echo "=========================================="
echo "테스트 완료!"
echo "=========================================="
echo ""
echo "테스트 결과는 다음 위치에서 확인할 수 있습니다:"
echo "  - 보고서: smartmealtable-batch/crawler/build/reports/tests/test/index.html"
echo "  - 로그: smartmealtable-batch/crawler/build/test-results/"
echo ""

