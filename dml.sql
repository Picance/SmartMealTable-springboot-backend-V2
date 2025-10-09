-- =TAIN (변수 초기화)
-- 이 스크립트를 여러 번 실행할 경우를 대비해 변수를 초기화합니다.
SET @group_id_company = NULL;
SET @group_id_school = NULL;
SET @category_id_korean = NULL;
SET @category_id_chinese = NULL;
SET @category_id_western = NULL;
SET @category_id_japanese = NULL;
SET @category_id_cafe = NULL;
SET @policy_id_service = NULL;
SET @policy_id_privacy = NULL;
SET @policy_id_marketing = NULL;
SET @member_id_kim = NULL;
SET @auth_id_kim = NULL;
SET @member_id_park = NULL;
SET @auth_id_park = NULL;
SET @store_id_gukbap = NULL;
SET @store_id_banjeom = NULL;
SET @store_id_cafe = NULL;
SET @store_id_pasta = NULL;

-- ================================================================================= --
--                                   기본 데이터 설정
-- ================================================================================= --

-- 그룹 데이터 추가
INSERT INTO `groups` (address, name, type) VALUES ('서울특별시 강남구 테헤란로 123', '스마트컴퍼니', '회사');
SET @group_id_company = LAST_INSERT_ID();
INSERT INTO `groups` (address, name, type) VALUES ('서울특별시 서대문구 이화여대길 52', '알뜰대학교', '학교');
SET @group_id_school = LAST_INSERT_ID();

-- 카테고리 데이터 추가
INSERT INTO category (name) VALUES ('한식');
SET @category_id_korean = LAST_INSERT_ID();
INSERT INTO category (name) VALUES ('중식');
SET @category_id_chinese = LAST_INSERT_ID();
INSERT INTO category (name) VALUES ('양식');
SET @category_id_western = LAST_INSERT_ID();
INSERT INTO category (name) VALUES ('일식');
SET @category_id_japanese = LAST_INSERT_ID();
INSERT INTO category (name) VALUES ('카페');
SET @category_id_cafe = LAST_INSERT_ID();

-- 약관 데이터 추가
INSERT INTO policy (version, title, description, is_mandatory) VALUES ('1.0.0', '서비스 이용 약관', '알뜰식탁 서비스 이용에 관한 약관입니다...', 1);
SET @policy_id_service = LAST_INSERT_ID();
INSERT INTO policy (version, title, description, is_mandatory) VALUES ('1.0.0', '개인정보 수집 및 이용 동의', '서비스 제공을 위해 개인정보를 수집 및 이용합니다...', 1);
SET @policy_id_privacy = LAST_INSERT_ID();
INSERT INTO policy (version, title, description, is_mandatory) VALUES ('1.0.0', '마케팅 정보 수신 동의', '이벤트 및 프로모션 정보를 푸시 알림으로 보내드립니다.', 0);
SET @policy_id_marketing = LAST_INSERT_ID();


-- ================================================================================= --
--                           사용자 2명 가입 및 프로필 설정
-- ================================================================================= --

-- 사용자 1: 김알뜰 (이메일 가입)
INSERT INTO member (group_id, nickname, recommendation_type) VALUES (@group_id_company, '김알뜰', '절약형');
SET @member_id_kim = LAST_INSERT_ID();

INSERT INTO member_authentication (member_id, email, hashed_password, name) VALUES (@member_id_kim, 'kim@example.com', 'hashed_password_placeholder_123', '김알뜰');
SET @auth_id_kim = LAST_INSERT_ID();

-- 사용자 2: 박지출 (소셜 가입)
INSERT INTO member (group_id, nickname, recommendation_type) VALUES (NULL, '박지출', '균형형');
SET @member_id_park = LAST_INSERT_ID();

INSERT INTO member_authentication (member_id, email, name) VALUES (@member_id_park, 'park@gmail.com', '박지출');
SET @auth_id_park = LAST_INSERT_ID();

INSERT INTO social_account (member_authentication_id, provider, provider_id, access_token) VALUES (@auth_id_park, 'Google', 'google_provider_id_456', 'google_access_token_placeholder');

-- 주소 데이터 추가
INSERT INTO address_history (member_id, lot_number_address, street_name_address, detailed_address, latitude, longitude, is_primary, address_type, alias) VALUES
                                                                                                                                                             (@member_id_kim, '서울 강남구 역삼동 123-45', '서울특별시 강남구 테헤란로 123', 'A동 101호', 37.50449, 127.0489, 0, '회사', '우리 회사'),
                                                                                                                                                             (@member_id_kim, '서울 서초구 반포동 555-1', '서울특별시 서초구 신반포로 55', '101동 202호', 37.5015, 127.0047, 1, '집', '우리 집'),
                                                                                                                                                             (@member_id_park, '서울 마포구 합정동 987-6', '서울특별시 마포구 월드컵로 10', '오피스텔 303호', 37.5513, 126.9109, 1, '집', '내 자취방');

-- 약관 동의 데이터 추가
INSERT INTO policy_agreement (policy_id, member_authentication_id, is_agreed, agreed_dt) VALUES
                                                                                             (@policy_id_service, @auth_id_kim, 1, NOW()),
                                                                                             (@policy_id_privacy, @auth_id_kim, 1, NOW()),
                                                                                             (@policy_id_marketing, @auth_id_kim, 0, NOW()),
                                                                                             (@policy_id_service, @auth_id_park, 1, NOW()),
                                                                                             (@policy_id_privacy, @auth_id_park, 1, NOW()),
                                                                                             (@policy_id_marketing, @auth_id_park, 1, NOW());


-- ================================================================================= --
--                             가게 및 메뉴 데이터 설정
-- ================================================================================= --

-- 가게 데이터 추가
INSERT INTO store (category_id, store_name, average_price, lot_number_address, street_name_address, detailed_address, latitude, longitude, phone_number) VALUES
    (@category_id_korean, '든든한 국밥', 9000, '서울 강남구 역삼동 123-1', '서울특별시 강남구 테헤란로 120', '1층', 37.5040, 127.0480, '02-111-1111');
SET @store_id_gukbap = LAST_INSERT_ID();

INSERT INTO store (category_id, store_name, average_price, lot_number_address, street_name_address, detailed_address, latitude, longitude, phone_number) VALUES
    (@category_id_chinese, '빠른반점', 8000, '서울 강남구 역삼동 456-1', '서울특별시 강남구 테헤란로 150', '2층', 37.5055, 127.0495, '02-222-2222');
SET @store_id_banjeom = LAST_INSERT_ID();

INSERT INTO store (category_id, store_name, average_price, lot_number_address, street_name_address, detailed_address, latitude, longitude, phone_number) VALUES
    (@category_id_cafe, '카페 안식', 5500, '서울 서초구 반포동 555-2', '서울특별시 서초구 신반포로 56', '', 37.5018, 127.0050, '02-333-3333');
SET @store_id_cafe = LAST_INSERT_ID();

INSERT INTO store (category_id, store_name, average_price, lot_number_address, street_name_address, detailed_address, latitude, longitude, phone_number) VALUES
    (@category_id_western, '파스타 피오레', 18000, '서울 마포구 합정동 987-10', '서울특별시 마포구 월드컵로 12', '', 37.5510, 126.9100, '02-444-4444');
SET @store_id_pasta = LAST_INSERT_ID();


-- 가게 영업시간 데이터 추가
INSERT INTO store_opening_hour (store_id, day_of_week, start_time, end_time, break_start_time, break_end_time, last_order_time) VALUES
    (@store_id_gukbap, 'EVERYDAY', '09:00:00', '21:00:00', '15:00:00', '16:30:00', '20:30:00');
INSERT INTO store_opening_hour (store_id, day_of_week, start_time, end_time, last_order_time) VALUES
                                                                                                  (@store_id_banjeom, 'MONDAY', '11:00:00', '20:00:00', '19:30:00'),
                                                                                                  (@store_id_banjeom, 'TUESDAY', '11:00:00', '20:00:00', '19:30:00'),
                                                                                                  (@store_id_banjeom, 'WEDNESDAY', '11:00:00', '20:00:00', '19:30:00'),
                                                                                                  (@store_id_banjeom, 'THURSDAY', '11:00:00', '20:00:00', '19:30:00'),
                                                                                                  (@store_id_banjeom, 'FRIDAY', '11:00:00', '21:00:00', '20:30:00'),
                                                                                                  (@store_id_banjeom, 'SATURDAY', '11:00:00', '21:00:00', '20:30:00');
INSERT INTO store_opening_hour (store_id, day_of_week, start_time, end_time) VALUES
                                                                                 (@store_id_cafe, 'MONDAY', '08:00:00', '22:00:00'),
                                                                                 (@store_id_cafe, 'TUESDAY', '08:00:00', '22:00:00'),
                                                                                 (@store_id_cafe, 'WEDNESDAY', '08:00:00', '22:00:00'),
                                                                                 (@store_id_cafe, 'THURSDAY', '08:00:00', '22:00:00'),
                                                                                 (@store_id_cafe, 'FRIDAY', '08:00:00', '22:00:00'),
                                                                                 (@store_id_cafe, 'SATURDAY', '10:00:00', '22:00:00');

-- 판매자 데이터 추가 (든든한 국밥 가게 주인)
INSERT INTO seller (store_id, login_id, password) VALUES (@store_id_gukbap, 'seller_denden', 'seller_password_placeholder');

-- 음식 데이터 추가
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_gukbap, '돼지국밥', 9000, '진하고 구수한 국물의 돼지국밥');
SET @food_id_gukbap1 = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_gukbap, '순대국밥', 9000, '속이 꽉 찬 순대가 들어간 순대국밥');
SET @food_id_gukbap2 = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_banjeom, '짜장면', 7000, '남녀노소 누구나 좋아하는 짜장면');
SET @food_id_jjajang = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_banjeom, '짬뽕', 8000, '얼큰하고 시원한 국물의 짬뽕');
SET @food_id_jjamppong = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_banjeom, '탕수육 (소)', 18000, '바삭하고 달콤한 탕수육');
SET @food_id_tangsuyuk = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_cafe, '아메리카노', 4500, '깊고 진한 풍미의 아메리카노');
SET @food_id_americano = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_cafe, '카페라떼', 5000, '부드러운 우유와 에스프레소의 조화');
SET @food_id_latte = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_cafe, '치즈케이크', 6500, '꾸덕하고 진한 뉴욕 치즈케이크');
SET @food_id_cheesecake = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_pasta, '봉골레 파스타', 17000, '신선한 조개로 맛을 낸 오일 파스타');
SET @food_id_vongole = LAST_INSERT_ID();
INSERT INTO food (store_id, food_name, price, description) VALUES
    (@store_id_pasta, '마르게리따 피자', 20000, '토마토, 바질, 치즈가 올라간 기본 피자');
SET @food_id_pizza = LAST_INSERT_ID();


-- ================================================================================= --
--                                사용자 활동 데이터
-- ================================================================================= --

-- 선호/불호 데이터 추가
INSERT INTO preference (member_id, category_id, weight) VALUES
                                                            (@member_id_kim, @category_id_korean, 10),
                                                            (@member_id_kim, @category_id_western, -10),
                                                            (@member_id_park, @category_id_western, 10),
                                                            (@member_id_park, @category_id_japanese, 5);

-- 즐겨찾기 데이터 추가
INSERT INTO favorite (store_id, member_id, priority) VALUES
                                                         (@store_id_gukbap, @member_id_kim, 1),
                                                         (@store_id_banjeom, @member_id_park, 2),
                                                         (@store_id_pasta, @member_id_park, 1);

-- 예산 데이터 추가
-- 김알뜰의 예산
INSERT INTO monthly_budget (member_id, monthly_food_budget, budget_month) VALUES (@member_id_kim, 400000, '2025-10');
INSERT INTO daily_budget (member_id, daily_food_budget, budget_date) VALUES (@member_id_kim, 15000, '2025-10-07');
SET @daily_budget_id_kim = LAST_INSERT_ID();
INSERT INTO meal_budget (daily_budget_id, meal_budget, meal_type, budget_date) VALUES
                                                                                   (@daily_budget_id_kim, 8000, '점심', '2025-10-07'),
                                                                                   (@daily_budget_id_kim, 7000, '저녁', '2025-10-07');
-- 박지출의 예산
INSERT INTO monthly_budget (member_id, monthly_food_budget, budget_month) VALUES (@member_id_park, 600000, '2025-10');
INSERT INTO daily_budget (member_id, daily_food_budget, budget_date) VALUES (@member_id_park, 25000, '2025-10-07');
SET @daily_budget_id_park = LAST_INSERT_ID();
INSERT INTO meal_budget (daily_budget_id, meal_budget, meal_type, budget_date) VALUES
                                                                                   (@daily_budget_id_park, 10000, '점심', '2025-10-07'),
                                                                                   (@daily_budget_id_park, 15000, '저녁', '2025-10-07');


-- 지출 내역 데이터 추가
-- 김알뜰의 지출 1 (든든한 국밥에서 점심)
INSERT INTO expenditure (member_id, store_id, expended_dt, food_category, meal_time, total_expenditure) VALUES
    (@member_id_kim, @store_id_gukbap, '2025-10-07 12:30:00', '한식', '점심', 9000);
SET @expenditure_id_1 = LAST_INSERT_ID();
INSERT INTO expenditure_item (expenditure_id, food_id, order_price, order_quantity) VALUES
    (@expenditure_id_1, @food_id_gukbap1, 9000, 1);

-- 김알뜰의 지출 2 (카페 안식에서 커피)
INSERT INTO expenditure (member_id, store_id, expended_dt, food_category, meal_time, total_expenditure) VALUES
    (@member_id_kim, @store_id_cafe, '2025-10-07 13:10:00', '카페', '기타', 5000);
SET @expenditure_id_2 = LAST_INSERT_ID();
INSERT INTO expenditure_item (expenditure_id, food_id, order_price, order_quantity) VALUES
    (@expenditure_id_2, @food_id_latte, 5000, 1);

-- 박지출의 지출 1 (빠른반점에서 저녁)
INSERT INTO expenditure (member_id, store_id, expended_dt, food_category, meal_time, discount_price, total_expenditure) VALUES
    (@member_id_park, @store_id_banjeom, '2025-10-06 19:00:00', '중식', '저녁', 1000, 24000);
SET @expenditure_id_3 = LAST_INSERT_ID();
INSERT INTO expenditure_item (expenditure_id, food_id, order_price, order_quantity) VALUES
                                                                                        (@expenditure_id_3, @food_id_jjajang, 7000, 1),
                                                                                        (@expenditure_id_3, @food_id_jjamppong, 8000, 1),
                                                                                        (@expenditure_id_3, @food_id_tangsuyuk, 10000, 1); -- 주문 시점 가격이 10000원이었다고 가정


-- 장바구니 데이터 추가 (김알뜰이 빠른반점 음식을 담아둠)
INSERT INTO cart (member_id, store_id) VALUES (@member_id_kim, @store_id_banjeom);
SET @cart_id_kim = LAST_INSERT_ID();
INSERT INTO cart_item (cart_id, food_id, quantity) VALUES
                                                       (@cart_id_kim, @food_id_jjajang, 2), -- 짜장면 2개
                                                       (@cart_id_kim, @food_id_tangsuyuk, 1); -- 탕수육 1개