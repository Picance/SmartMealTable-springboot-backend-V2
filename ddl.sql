-- ================================================================================= --
--                                     회원 관련
-- ================================================================================= --

-- 그룹 테이블
CREATE TABLE member_group (
                          group_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '그룹의 고유 식별자',
                          address       VARCHAR(255)  NULL     COMMENT '그룹의 주소 (예: 학교, 회사 주소)',
                          name          VARCHAR(50)   NOT NULL COMMENT '그룹의 명칭 (예: OO대학교, XX회사)',
                          type          VARCHAR(20)   NOT NULL COMMENT '그룹의 유형 (예: 학교, 회사)',
                          created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                          updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                          PRIMARY KEY (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자의 소속 집단 (예: 학생, 직장인) 정보를 관리하는 테이블';

-- 회원 테이블
CREATE TABLE member (
                        member_id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '회원의 고유 식별자',
                        group_id              BIGINT        NULL     COMMENT '회원이 속한 그룹의 식별자 (논리 FK)',
                        nickname              VARCHAR(50)   NOT NULL COMMENT '서비스 내에서 사용하는 별명',
                        recommendation_type   VARCHAR(20)   NOT NULL COMMENT '사용자 맞춤 음식 추천 유형',
                        created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                        updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                        PRIMARY KEY (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스를 이용하는 고객의 기본 정보를 저장하는 테이블';

-- 회원 인증 테이블
CREATE TABLE member_authentication (
                                       member_authentication_id BIGINT        NOT NULL AUTO_INCREMENT COMMENT '회원 인증 정보의 고유 식별자',
                                       member_id                BIGINT        NOT NULL COMMENT '연동된 회원의 식별자',
                                       email                    VARCHAR(100)  NOT NULL COMMENT '로그인 및 인증에 사용되는 이메일',
                                       hashed_password          VARCHAR(255)  NULL     COMMENT '해시 함수를 통해 암호화된 비밀번호',
                                       failure_count            INTEGER       NOT NULL DEFAULT 0 COMMENT '비밀번호 연속 실패 횟수',
                                       password_changed_at      DATETIME      NULL     COMMENT '마지막 비밀번호 변경 시각 (시스템 자동 기록, 비즈니스 필드)',
                                       password_expires_at      DATETIME      NULL     COMMENT '비밀번호 만료 시각 (정책에 의해 지정, 비즈니스 필드)',
                                       name                     VARCHAR(50)   NULL     COMMENT '회원의 실명',
                                       deleted_at               DATETIME      NULL     COMMENT '회원 탈퇴 시각 (시스템 자동 기록, 비즈니스 필드)',
                                       created_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                       updated_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                       PRIMARY KEY (member_authentication_id),
                                       UNIQUE KEY uq_member_id (member_id),
                                       UNIQUE KEY uq_email (email),
                                       INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원의 서비스 자체 로그인 인증 정보를 저장하는 테이블';

-- 소셜 계정 테이블
CREATE TABLE social_account (
                                social_account_id        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '소셜 계정의 고유 식별자',
                                member_authentication_id BIGINT        NOT NULL COMMENT '연동된 회원 인증 테이블의 식별자',
                                provider                 VARCHAR(20)   NOT NULL COMMENT '소셜 로그인 제공자 (예: Google, Kakao)',
                                provider_id              VARCHAR(255)  NOT NULL COMMENT '소셜 로그인 제공자가 발급하는 고유 ID',
                                token_type               VARCHAR(20)   NULL     COMMENT '토큰의 유형 (예: Bearer)',
                                access_token             VARCHAR(255)  NOT NULL COMMENT 'API 접근을 위한 액세스 토큰',
                                refresh_token            VARCHAR(255)  NULL     COMMENT '액세스 토큰 재발급을 위한 리프레시 토큰',
                                expires_at               DATETIME      NULL     COMMENT '액세스 토큰의 만료 시각 (시스템 자동 기록, 비즈니스 필드)',
                                created_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                updated_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                PRIMARY KEY (social_account_id),
                                INDEX idx_member_authentication_id (member_authentication_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원의 소셜 로그인 정보를 관리하는 테이블';

-- 주소 이력 테이블
CREATE TABLE address_history (
                                 address_history_id    BIGINT         NOT NULL AUTO_INCREMENT COMMENT '주소 이력의 고유 식별자',
                                 member_id             BIGINT         NOT NULL COMMENT '해당 주소를 사용한 회원의 식별자',
                                 lot_number_address    VARCHAR(255)   NULL     COMMENT '지번 기준 주소',
                                 street_name_address   VARCHAR(255)   NOT NULL COMMENT '도로명 기준 주소',
                                 detailed_address      VARCHAR(255)   NULL     COMMENT '건물 호수 등 상세 주소',
                                 latitude              DECIMAL(9,6)   NULL     COMMENT '주소의 위도 정보',
                                 longitude             DECIMAL(9,6)   NULL     COMMENT '주소의 경도 정보',
                                 is_primary            BOOLEAN        NOT NULL COMMENT '기본 배송지 등 메인 주소인지 여부',
                                 address_type          VARCHAR(20)    NULL     COMMENT '주소의 유형 (예: 집, 회사)',
                                 alias                 VARCHAR(50)    NOT NULL COMMENT '주소에 대한 별칭 (예: 우리집)',
                                 registered_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '주소 등록 시각 (시스템 자동 기록, 비즈니스 필드)',
                                 created_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                 updated_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                 PRIMARY KEY (address_history_id),
                                 INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원의 주소 변경 이력을 관리하는 테이블';

-- 약관 테이블
CREATE TABLE policy (
                        policy_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '약관의 고유 식별자',
                        version        VARCHAR(20)   NOT NULL COMMENT '약관의 버전 정보',
                        title          VARCHAR(100)  NOT NULL COMMENT '약관의 제목',
                        description    TEXT          NOT NULL COMMENT '약관의 상세 내용',
                        is_mandatory   BOOLEAN       NOT NULL COMMENT '동의가 필수적인 약관인지 여부',
                        created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                        updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                        PRIMARY KEY (policy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스 이용에 필요한 약관 정보를 관리하는 테이블';

-- 약관 동의 테이블
CREATE TABLE policy_agreement (
                                  policy_agreement_id      BIGINT    NOT NULL AUTO_INCREMENT COMMENT '약관 동의 내역의 고유 식별자',
                                  policy_id                BIGINT    NOT NULL COMMENT '동의한 약관의 식별자',
                                  member_authentication_id BIGINT    NOT NULL COMMENT '약관에 동의한 회원의 인증 식별자',
                                  is_agreed                BOOLEAN   NOT NULL COMMENT '약관 동의 여부',
                                  agreed_at                DATETIME  NOT NULL COMMENT '약관에 동의한 시각 (비즈니스 필드)',
                                  created_at               DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                  updated_at               DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                  PRIMARY KEY (policy_agreement_id),
                                  UNIQUE KEY uq_policy_member_auth (policy_id, member_authentication_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원의 약관 동의 내역을 저장하는 테이블';


-- ================================================================================= --
--                                     가게 관련
-- ================================================================================= --

-- 카테고리 테이블
CREATE TABLE category (
                          category_id   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '카테고리의 고유 식별자',
                          name          VARCHAR(50)   NOT NULL COMMENT '카테고리의 명칭',
                          created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                          updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                          PRIMARY KEY (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='음식의 종류를 분류하는 기준 (예: 한식, 중식) 정보를 관리하는 테이블';

-- 음식점(가게) 테이블
CREATE TABLE store (
    store_id              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '음식점의 고유 식별자',
    category_id           BIGINT         NOT NULL COMMENT '음식점이 속한 음식 카테고리의 식별자',
    seller_id             BIGINT         NULL     COMMENT '판매자 식별자 (논리 FK)',
    name                  VARCHAR(100)   NOT NULL COMMENT '음식점의 상호명',
    address               VARCHAR(200)   NOT NULL COMMENT '도로명 기준 주소',
    lot_number_address    VARCHAR(200)   NULL     COMMENT '지번 기준 주소',
    latitude              DECIMAL(10,7)  NOT NULL COMMENT '주소의 위도 정보',
    longitude             DECIMAL(10,7)  NOT NULL COMMENT '주소의 경도 정보',
    phone_number          VARCHAR(20)    NULL     COMMENT '음식점의 연락처',
    description           TEXT           NULL     COMMENT '음식점 상세 설명',
    average_price         INT            NULL     COMMENT '음식점의 1인당 평균 가격대',
    review_count          INT            NOT NULL DEFAULT 0 COMMENT '음식점에 달린 리뷰의 총 개수',
    view_count            INT            NOT NULL DEFAULT 0 COMMENT '총 조회수 (추천 알고리즘용)',
    favorite_count        INT            NOT NULL DEFAULT 0 COMMENT '즐겨찾기 수',
    store_type            VARCHAR(20)    NOT NULL COMMENT '가게 유형',
    image_url             VARCHAR(500)   NULL     COMMENT '음식점의 대표 이미지 주소',
    registered_at         DATETIME       NOT NULL COMMENT '가게 등록일 (추천 알고리즘의 신규성 점수 계산용, 비즈니스 필드)',
    deleted_at            DATETIME       NULL     COMMENT '삭제 시각 (소프트 삭제용)',
    created_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
    updated_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
    PRIMARY KEY (store_id),
    INDEX idx_category_id (category_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_name (name),
    INDEX idx_review_count (review_count),
    INDEX idx_average_price (average_price),
    INDEX idx_view_count (view_count),
    INDEX idx_store_type (store_type),
    INDEX idx_registered_at (registered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='음식을 판매하는 음식점(가게)의 정보를 관리하는 테이블';

-- 가게 영업시간 테이블
CREATE TABLE store_opening_hour (
                                    store_opening_hour_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '레코드의 고유 식별자',
                                    store_id              BIGINT      NOT NULL COMMENT 'store 테이블을 참조하는 외래키',
                                    day_of_week           VARCHAR(10) NOT NULL COMMENT 'MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY',
                                    open_time             VARCHAR(8)  NULL     COMMENT '영업 시작 시간 (예: ''11:00:00'')',
                                    close_time            VARCHAR(8)  NULL     COMMENT '영업 종료 시간 (예: ''21:00:00'')',
                                    break_start_time      VARCHAR(8)  NULL     COMMENT '브레이크 타임 시작 (없는 경우 NULL)',
                                    break_end_time        VARCHAR(8)  NULL     COMMENT '브레이크 타임 종료 (없는 경우 NULL)',
                                    is_holiday            BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '휴무일 여부',
                                    created_at            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                    updated_at            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                    PRIMARY KEY (store_opening_hour_id),
                                    INDEX idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가게의 요일별 영업 및 휴게 시간 등 상세 정보를 관리하는 테이블';

-- 판매자 테이블
CREATE TABLE seller (
                        seller_id   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '판매자의 고유 식별자',
                        store_id    BIGINT        NOT NULL COMMENT '관리하는 음식점의 식별자',
                        login_id    VARCHAR(50)   NOT NULL COMMENT '판매자 로그인 시 사용하는 ID',
                        password    VARCHAR(255)  NOT NULL COMMENT '암호화하여 저장된 비밀번호',
                        created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                        updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                        PRIMARY KEY (seller_id),
                        UNIQUE KEY uq_store_id (store_id),
                        INDEX idx_login_id (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='음식점(가게)을 관리하는 판매자의 계정 정보를 저장하는 테이블';

-- 가게 조회 이력 테이블
CREATE TABLE food (
                      food_id       BIGINT         NOT NULL AUTO_INCREMENT COMMENT '음식의 고유 식별자',
                      store_id      BIGINT         NOT NULL COMMENT '이 음식을 판매하는 가게의 식별자',
                      category_id   BIGINT         NOT NULL COMMENT '음식 카테고리 식별자',
                      food_name     VARCHAR(100)   NOT NULL COMMENT '음식의 이름',
                      price         INT            NOT NULL COMMENT '음식의 판매 가격',
                      description   VARCHAR(500)   NULL     COMMENT '음식에 대한 상세 설명',
                      image_url     VARCHAR(500)   NULL     COMMENT '음식 이미지 주소',
                      registered_dt DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '음식 등록 시각 (시스템 자동 기록, 신메뉴 표시용, 비즈니스 필드)',
                      created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                      updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                      PRIMARY KEY (food_id),
                      INDEX idx_store_id (store_id),
                      INDEX idx_category_id (category_id),
                      INDEX idx_food_name (food_name),
                      INDEX idx_registered_at (registered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='음식점에서 판매하는 개별 음식의 정보를 저장하는 테이블';

-- 가게 조회 이력 테이블
CREATE TABLE store_view_history (
                                    store_view_history_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '가게 조회 이력의 고유 식별자',
                                    member_id             BIGINT   NOT NULL COMMENT '조회한 회원의 식별자 (논리 FK)',
                                    store_id              BIGINT   NOT NULL COMMENT '조회된 가게의 식별자 (논리 FK)',
                                    viewed_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '조회 시각 (시스템 자동 기록, 비즈니스 필드)',
                                    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                    updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                    PRIMARY KEY (store_view_history_id),
                                    INDEX idx_member_id (member_id),
                                    INDEX idx_store_id (store_id),
                                    INDEX idx_viewed_at (viewed_at),
                                    INDEX idx_store_viewed_at (store_id, viewed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자의 가게 조회 이력을 저장하는 테이블 (추천 알고리즘의 최근 관심도 계산 및 최근 7일 조회수 집계용)';

-- 임시 휴무 테이블
CREATE TABLE store_temporary_closure (
                                         store_temporary_closure_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '임시 휴무의 고유 식별자',
                                         store_id                   BIGINT   NOT NULL COMMENT '임시 휴무 중인 가게의 식별자 (논리 FK)',
                                         closure_date               DATE     NOT NULL COMMENT '휴무 날짜',
                                         start_time                 TIME     NULL     COMMENT '부분 휴무 시작 시간',
                                         end_time                   TIME     NULL     COMMENT '부분 휴무 종료 시간',
                                         reason                     VARCHAR(200) NULL COMMENT '휴무 사유',
                                         registered_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '휴무 정보 등록 시각 (시스템 자동 기록, 최근 휴업 알림용, 비즈니스 필드)',
                                         created_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                         updated_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                         PRIMARY KEY (store_temporary_closure_id),
                                         INDEX idx_store_id (store_id),
                                         INDEX idx_closure_date (closure_date),
                                         INDEX idx_store_closure_date (store_id, closure_date),
                                         INDEX idx_registered_at (registered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가게의 임시 휴무 정보를 관리하는 테이블';


-- ================================================================================= --
--                                   지출 및 장바구니
-- ================================================================================= --

-- 지출 내역 테이블
CREATE TABLE expenditure (
                             expenditure_id    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '지출 내역의 고유 식별자',
                             member_id         BIGINT        NOT NULL COMMENT '지출을 기록한 회원의 식별자',
                             store_id          BIGINT        NULL     COMMENT '지출이 발생한 음식점의 식별자 (논리 FK, 장바구니 또는 수기 입력 지원)',
                             expended_dt       DATETIME      NOT NULL COMMENT '지출이 발생한 날짜와 시간 (비즈니스 필드)',
                             food_category     VARCHAR(50)   NULL     COMMENT '지출한 음식의 주된 카테고리',
                             meal_time         VARCHAR(20)   NULL     COMMENT '식사 시간대 (예: 아침, 점심, 저녁)',
                             discount_price    INT           NOT NULL DEFAULT 0 COMMENT '할인받은 금액',
                             total_expenditure INT           NOT NULL COMMENT '할인을 포함한 최종 지출 금액',
                             created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                             updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                             PRIMARY KEY (expenditure_id),
                             INDEX idx_member_id (member_id),
                             INDEX idx_store_id (store_id),
                             INDEX idx_expended_dt (expended_dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원의 음식 관련 지출 내역을 기록하는 테이블';

-- 지출 항목 테이블
CREATE TABLE expenditure_item (
                                  expenditure_item_id BIGINT        NOT NULL AUTO_INCREMENT COMMENT '지출 항목의 고유 식별자',
                                  expenditure_id      BIGINT        NOT NULL COMMENT '이 항목이 속한 지출 내역의 식별자',
                                  food_id             BIGINT        NULL     COMMENT '주문한 음식의 식별자 (수기 입력 시 NULL 가능)',
                                  food_name           VARCHAR(500)  NULL     COMMENT '음식 이름 (비정규화, 수기 입력 지원)',
                                  order_price         INT           NOT NULL COMMENT '주문 시점의 음식 가격',
                                  order_quantity      INT           NOT NULL COMMENT '주문한 음식의 수량',
                                  created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                  updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                  PRIMARY KEY (expenditure_item_id),
                                  INDEX idx_expenditure_id (expenditure_id),
                                  INDEX idx_food_id (food_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='하나의 지출 내역에 포함된 개별 음식 정보를 저장하는 테이블';

-- 장바구니 테이블
CREATE TABLE cart (
                      cart_id    BIGINT   NOT NULL AUTO_INCREMENT COMMENT '장바구니의 고유 식별자',
                      member_id  BIGINT   NOT NULL COMMENT '장바구니 소유 회원의 식별자',
                      store_id   BIGINT   NOT NULL COMMENT '장바구니에 담긴 음식을 파는 음식점의 식별자',
                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                      updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                      PRIMARY KEY (cart_id),
                      INDEX idx_member_id (member_id),
                      INDEX idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원이 선택한 음식들을 임시로 담아두는 공간';

-- 장바구니 항목 테이블
CREATE TABLE cart_item (
                           cart_item_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '장바구니 항목의 고유 식별자',
                           cart_id      BIGINT   NOT NULL COMMENT '이 항목이 속한 장바구니의 식별자',
                           food_id      BIGINT   NOT NULL COMMENT '장바구니에 담은 음식의 식별자',
                           quantity     INT      NOT NULL COMMENT '장바구니에 담은 음식의 수량',
                           created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                           updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                           PRIMARY KEY (cart_item_id),
                           UNIQUE KEY uq_cart_food (cart_id, food_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='장바구니에 담긴 개별 음식의 정보를 저장하는 테이블';


-- ================================================================================= --
--                                   예산 및 선호도
-- ================================================================================= --

-- 월별 예산 테이블
CREATE TABLE monthly_budget (
                                monthly_budget_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '월별 예산의 고유 식별자',
                                member_id           BIGINT       NOT NULL COMMENT '예산을 설정한 회원의 식별자',
                                monthly_food_budget INT          NOT NULL COMMENT '회원이 설정한 한 달 식비 예산 금액',
                                monthly_used_amount INT          NOT NULL DEFAULT 0 COMMENT '해당 월에 현재까지 사용한 금액',
                                budget_month        VARCHAR(7)   NOT NULL COMMENT '예산이 적용되는 년월 (YYYY-MM)',
                                created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                PRIMARY KEY (monthly_budget_id),
                                INDEX idx_member_id (member_id),
                                INDEX idx_budget_month (budget_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원이 설정한 월별 식비 예산을 관리하는 테이블';

-- 일일 예산 테이블
CREATE TABLE daily_budget (
                              budget_id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '일일 예산의 고유 식별자',
                              member_id         BIGINT   NOT NULL COMMENT '예산을 설정한 회원의 식별자',
                              daily_food_budget INT      NOT NULL COMMENT '회원이 설정한 하루 식비 예산 금액',
                              daily_used_amount INT      NOT NULL DEFAULT 0 COMMENT '해당 일에 현재까지 사용한 금액',
                              budget_date       DATE     NOT NULL COMMENT '예산이 적용되는 날짜',
                              created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                              updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                              PRIMARY KEY (budget_id),
                              INDEX idx_member_id (member_id),
                              INDEX idx_budget_date (budget_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원이 설정한 일일 식비 예산을 관리하는 테이블';

-- 식사 예산 테이블
CREATE TABLE meal_budget (
                             meal_budget_id  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '식사 예산의 고유 식별자',
                             daily_budget_id BIGINT        NOT NULL COMMENT '이 식사 예산이 속한 일일 예산의 식별자',
                             meal_budget     INT           NOT NULL COMMENT '회원이 설정한 한 끼 식비 예산 금액',
                             meal_type       VARCHAR(20)   NOT NULL COMMENT '식사 유형 (예: 아침, 점심, 저녁, 기타)',
                             used_amount     INT           NOT NULL DEFAULT 0 COMMENT '해당 끼니에 사용한 금액',
                             budget_date     DATE          NOT NULL COMMENT '예산이 적용되는 날짜',
                             created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                             updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                             PRIMARY KEY (meal_budget_id),
                             INDEX idx_daily_budget_id (daily_budget_id),
                             INDEX idx_budget_date (budget_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원의 끼니별 식비 예산을 관리하는 테이블';

-- 선호 테이블 (카테고리 기반)
CREATE TABLE preference (
                            preference_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '선호 정보의 고유 식별자',
                            member_id     BIGINT   NOT NULL COMMENT '선호 정보를 설정한 회원의 식별자',
                            category_id   BIGINT   NOT NULL COMMENT '선호/불호하는 음식 카테고리의 식별자',
                            weight        SMALLINT NOT NULL COMMENT '선호 가중치 (좋아요: 100, 보통: 0, 싫어요: -100)',
                            created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                            updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                            PRIMARY KEY (preference_id),
                            UNIQUE KEY uq_member_category (member_id, category_id),
                            INDEX idx_weight (weight),
                            CHECK (weight IN (-100, 0, 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자가 선호 또는 불호하는 음식 카테고리 정보를 저장하는 테이블 (추천 알고리즘의 안정성 점수 계산용)';

-- 개별 음식 선호도 테이블 (REQ-ONBOARD-405)
CREATE TABLE food_preference (
                                 food_preference_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '음식 선호도의 고유 식별자',
                                 member_id          BIGINT   NOT NULL COMMENT '선호 정보를 설정한 회원의 식별자',
                                 food_id            BIGINT   NOT NULL COMMENT '선호하는 개별 음식의 식별자',
                                 is_preferred       BOOLEAN  NOT NULL DEFAULT TRUE COMMENT '선호 여부 (TRUE: 선호, FALSE: 미사용)',
                                 created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                 updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                 PRIMARY KEY (food_preference_id),
                                 UNIQUE KEY uq_member_food (member_id, food_id),
                                 INDEX idx_member_id (member_id),
                                 INDEX idx_food_id (food_id),
                                 INDEX idx_is_preferred (is_preferred)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자가 온보딩 시 이미지 그리드에서 선택한 개별 음식 선호도를 저장하는 테이블 (추천 알고리즘의 세밀한 개인화용)';

-- 즐겨찾기 테이블
CREATE TABLE favorite (
                          favorite_id  BIGINT   NOT NULL AUTO_INCREMENT COMMENT '즐겨찾기의 고유 식별자',
                          store_id     BIGINT   NOT NULL COMMENT '즐겨찾기한 음식점의 식별자',
                          member_id    BIGINT   NOT NULL COMMENT '즐겨찾기를 등록한 회원의 식별자',
                          priority     BIGINT   NOT NULL COMMENT '즐겨찾기 목록에서 가게 순서를 지정하기 위한 칼럼',
                          favorited_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '즐겨찾기로 등록한 시각 (시스템 자동 기록, 비즈니스 필드)',
                          created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                          updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                          PRIMARY KEY (favorite_id),
                          UNIQUE KEY uq_store_member (store_id, member_id),
                          INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자가 선호하는 음식점을 저장 및 관리하는 테이블';

-- 월별 예산 확인 이력 테이블
CREATE TABLE monthly_budget_confirmation (
                                             monthly_budget_confirmation_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '월별 예산 확인의 고유 식별자',
                                             member_id                      BIGINT      NOT NULL COMMENT '회원 ID (FK)',
                                             year                           INT         NOT NULL COMMENT '연도',
                                             month                          INT         NOT NULL COMMENT '월 (1-12)',
                                             action                         VARCHAR(20) NOT NULL COMMENT '사용자 액션 (KEEP: 유지, CHANGE: 변경)',
                                             confirmed_at                   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '확인 시각 (비즈니스 필드)',
                                             created_at                     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                             updated_at                     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
                                             PRIMARY KEY (monthly_budget_confirmation_id),
                                             UNIQUE KEY uq_member_year_month (member_id, year, month),
                                             INDEX idx_member_id (member_id),
                                             INDEX idx_confirmed_at (confirmed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='월별 예산 확인 이력을 저장하는 테이블 (매월 초 예산 확인 모달 처리용)';

-- 사용자 조회 이력 (추천 알고리즘용) - 중복 제거
-- 위의 store_view_history 테이블로 통합됨

-- -- 가게 임시 휴무 정보 테이블
-- CREATE TABLE store_temporary_closure (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '임시 휴무 ID',
--     store_id BIGINT NOT NULL COMMENT '가게 ID (FK)',
--     closure_date DATE NOT NULL COMMENT '휴무 날짜',
--     reason VARCHAR(500) COMMENT '휴무 사유',
--     is_all_day BOOLEAN DEFAULT TRUE COMMENT '종일 휴무 여부',
--     start_time TIME COMMENT '부분 휴무 시작 시간',
--     end_time TIME COMMENT '부분 휴무 종료 시간',
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    
--     INDEX idx_store_id (store_id),
--     INDEX idx_closure_date (closure_date),
--     INDEX idx_store_closure (store_id, closure_date),
--     UNIQUE KEY uk_store_closure_datetime (store_id, closure_date, start_time, end_time)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='가게 임시 휴무 정보';

-- -- 개별 음식 선호도 테이블 추가
-- CREATE TABLE food_preference (
--     food_preference_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '음식 선호도의 고유 식별자',
--     member_id          BIGINT NOT NULL COMMENT '회원 식별자',
--     food_id            BIGINT NOT NULL COMMENT '선호하는 음식의 식별자',
--     is_preferred       BOOLEAN NOT NULL COMMENT '선호 여부',
--     created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     PRIMARY KEY (food_preference_id),
--     UNIQUE KEY uq_member_food (member_id, food_id),
--     INDEX idx_member_id (member_id),
--     INDEX idx_food_id (food_id)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
-- COMMENT='사용자가 선호하는 개별 음식을 저장하는 테이블 (온보딩 REQ-ONBOARD-405)';


-- ================================================================================= --
--                                   알림 및 설정
-- ================================================================================= --

-- 알림 설정 테이블
CREATE TABLE notification_settings (
    notification_settings_id  BIGINT    NOT NULL AUTO_INCREMENT COMMENT '알림 설정의 고유 식별자',
    member_id                 BIGINT    NOT NULL COMMENT '회원 식별자 (논리 FK)',
    push_enabled              BOOLEAN   NOT NULL DEFAULT TRUE COMMENT '전체 푸시 알림 활성화 여부',
    store_notice_enabled      BOOLEAN   NOT NULL DEFAULT TRUE COMMENT '가게 공지 알림 활성화 여부',
    recommendation_enabled    BOOLEAN   NOT NULL DEFAULT TRUE COMMENT '음식점 추천 알림 활성화 여부',
    budget_alert_enabled      BOOLEAN   NOT NULL DEFAULT TRUE COMMENT '예산 알림 활성화 여부',
    password_expiry_alert_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '비밀번호 만료 알림 활성화 여부',
    created_at                DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
    updated_at                DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
    PRIMARY KEY (notification_settings_id),
    UNIQUE KEY uq_member_id (member_id),
    INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원의 알림 설정을 저장하는 테이블';

-- 앱 설정 테이블
CREATE TABLE app_settings (
    app_settings_id    BIGINT    NOT NULL AUTO_INCREMENT COMMENT '앱 설정의 고유 식별자',
    member_id          BIGINT    NOT NULL COMMENT '회원 식별자 (논리 FK)',
    allow_tracking     BOOLEAN   NOT NULL DEFAULT FALSE COMMENT '사용자 추적 허용 여부',
    created_at         DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
    updated_at         DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '감사 필드 (도메인에 노출 안 함)',
    PRIMARY KEY (app_settings_id),
    UNIQUE KEY uq_member_id (member_id),
    INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원의 앱 설정을 저장하는 테이블';