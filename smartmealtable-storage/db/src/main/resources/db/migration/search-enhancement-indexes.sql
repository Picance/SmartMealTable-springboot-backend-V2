-- ===========================================================================================
-- 검색 기능 강화를 위한 DB 인덱스 생성 스크립트
-- 작성일: 2025-11-09
-- 목적: Group 검색 성능 최적화
-- ===========================================================================================

-- ==================== Group 테이블 인덱스 ====================

-- 1. 이름 Prefix 검색용 인덱스
-- LIKE 'prefix%' 형태의 검색에 B-Tree 인덱스 활용 가능
-- 최대 10자까지만 인덱싱하여 공간 절약
CREATE INDEX IF NOT EXISTS idx_group_name_prefix 
ON member_group(name(10));

-- 2. 타입 + 이름 복합 인덱스
-- 타입별 그룹을 이름으로 검색할 때 효율적
CREATE INDEX IF NOT EXISTS idx_group_type_name_prefix 
ON member_group(type, name(10));

-- ==================== 인덱스 생성 확인 ====================

-- 생성된 인덱스 확인
SHOW INDEX FROM member_group WHERE Key_name LIKE 'idx_group%';

-- ==================== 인덱스 사용 테스트 ====================

-- Prefix 검색 쿼리 실행 계획 확인
EXPLAIN 
SELECT * 
FROM member_group 
WHERE name LIKE '서울%'
ORDER BY name
LIMIT 10;

-- 타입 + 이름 검색 쿼리 실행 계획 확인
EXPLAIN 
SELECT * 
FROM member_group 
WHERE type = 'UNIVERSITY' 
  AND name LIKE '서울%'
ORDER BY name
LIMIT 10;

-- ==================== 성능 비교 ====================

-- 인덱스 사용 전후 성능 비교
-- (인덱스 사용 전) Full Table Scan
-- EXPLAIN SELECT * FROM member_group WHERE name LIKE '%서울%';

-- (인덱스 사용 후) Index Range Scan
-- EXPLAIN SELECT * FROM member_group WHERE name LIKE '서울%';

-- ==================== 주의사항 ====================
-- 1. LIKE '%keyword%' 형태는 인덱스를 사용할 수 없음
-- 2. LIKE 'keyword%' 형태만 인덱스 활용 가능
-- 3. name(10)은 최대 10바이트까지만 인덱싱
--    - 한글 1글자 = 3바이트 (UTF-8)
--    - 약 3~4글자의 한글까지 인덱싱
-- 4. 인덱스 공간: 약 500KB (5만 건 기준)
