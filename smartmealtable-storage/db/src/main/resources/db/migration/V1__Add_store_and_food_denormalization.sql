-- 지출 내역 API 재설계: 스토어 ID와 음식명 비정규화 추가
-- 목적: 장바구니와 수기 입력 시나리오 모두 지원, 상세 페이지 링크 제공

-- 1. expenditure 테이블: store_id 추가 (논리 FK, nullable)
ALTER TABLE expenditure ADD COLUMN store_id BIGINT NULL AFTER member_id;

-- store_id에 대한 인덱스 추가 (쿼리 성능 최적화)
CREATE INDEX idx_expenditure_store_id ON expenditure(store_id);

-- 2. expenditure_item 테이블: food_id를 nullable로 변경
ALTER TABLE expenditure_item MODIFY COLUMN food_id BIGINT NULL;

-- 3. expenditure_item 테이블: food_name 추가 (비정규화, nullable)
ALTER TABLE expenditure_item ADD COLUMN food_name VARCHAR(500) NULL AFTER food_id;

-- food_id에 대한 인덱스 추가 (쿼리 성능 최적화)
CREATE INDEX idx_expenditure_item_food_id ON expenditure_item(food_id);
