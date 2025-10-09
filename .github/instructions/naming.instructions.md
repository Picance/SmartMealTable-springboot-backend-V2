# naming instruction (용어 사전)

| 분류         | 명칭           | 전체 영문명                | 축약어  | 설명                                                      | 관련 시스템 요소                                                 |
|------------|--------------|-----------------------|------|---------------------------------------------------------|-----------------------------------------------------------|
| **엔티티**    | 회원           | member                |      | 서비스를 이용하는 고객. member, customer 등 유사 용어 대신 member로 통일한다. | member 테이블                                                |
|            | 소셜 계정        | social_account        |      | 고객의 서비스 계정                                              | social_account 테이블                                        |
|            | 회원인증정보       | member_authentication |      | 서비스를 이용하는고객의 신원 확인을 위한 정보                               | member_authentication 테이블                                 |
|            | **음식점 (가게)** | **store**             |      | 음식을 판매하는 장소. SRD의 가게는 음식점을 의미한다.                        | store 테이블                                                 |
|            | 음식           | food                  |      | 판매하는 음식                                                 | food 테이블                                                  |
|            | **카테고리**     | **category**          |      | 음식의 종류를 분류하는 기준 (예: 한식, 중식, 양식)                         | category 테이블                                              |
|            | **리뷰**       | **review**            |      | 음식 또는 음식점에 대한 사용자의 평가.                                  | review 테이블 (추정)                                           |
|            | 지출           | expenditure           |      | 회원의 지출 내역 등록 행위.                                        | expenditure 테이블                                           |
|            | 지출 항목        | expenditure_item      |      | 하나의 지출에 포함된 개별 음식 정보. 지출과 음식의 M:N 관계를 해소하는 연관 엔티티.      | expenditure_item 테이블                                      |
|            | **장바구니**     | **cart**              |      | 지출 내역으로 등록하기 전, 선택한 음식들을 임시로 담아두는 공간.                   | cart 테이블                                                  |
|            | **장바구니 항목**  | **cart_item**         |      | 장바구니에 담긴 개별 음식 정보. 장바구니와 음식의 M:N 관계를 해소하는 연관 엔티티.       | cart_item 테이블                                             |
|            | **예산**       | **budget**            |      | 사용자가 설정하는 식비 목표. 일별, 월별, 식사별 예산으로 관리된다.                 | daily_budget, monthly_budget, meal_budget 테이블             |
|            | **선호**       | **preference**        |      | 사용자가 선호/불호하는 음식 카테고리 정보.                                | preference 테이블                                            |
|            | **즐겨찾기**     | **favorite**          |      | 사용자가 선호하는 음식점을 저장하는 기능 또는 그 목록.                         | favorite 테이블                                              |
|            | 약관           | policy                |      | 회원 가입에 필요한 약관 정보                                        | policy 테이블                                                |
|            | 약관동의         | policy_agreement      |      | 고객과 약관의 M:N 관계를 해소하는 연관 엔티티                             | policy_agreement 테이블                                      |
|            | 주소이력         | address_history       |      | 고객의 주소 이력                                               | address_history 테이블                                       |
|            | **그룹**       | **group**             |      | 사용자의 소속 집단 (예: 학생, 직장인)                                 | group 테이블                                                 |
| **주요 속성**  | 식별자          | identifier            | id   | 데이터를 고유하게 식별하는 번호. [엔티티명]_id 형식 (예: member_id)으로 사용한다.  | member_id, food_id                                        |
|            | 이름, 명        | name                  |      | 사람, 상품 등 대상을 지칭하는 명칭.                                   | member_name, food_name,store_name                         |
|            | **닉네임**      | **nickname**          |      | 서비스 내에서 사용자를 식별하는 별명.                                   | member.nickname                                           |
|            | 가격           | price                 |      | 상품의 현재 판매가 또는 주문 시점의 가격. 문맥에 따라 명확히 구분한다.               | food_price                                                |
|            | 금액           | amount                |      | 금액 예): 총 주문 금액                                          | expenditure_amount                                        |
|            | 재고           | stock                 |      | 음식의 재고                                                  | 재고 수량 (stock_quantity)                                    |
|            | 수량           | quantity              |      | 개수나 양. 재고 quantity                                      | 재고 수량 (stock_quantity) 주문 수량 (order_quantity)             |
|            | 개수, 횟수       | count                 |      | 개별 항목을 하나씩 세는 행위, 이벤트 발생 횟수, 데이터의 총 개수, 인원수 등           | 방문 횟수(visit count) 클릭 수(click count) 직원 수(employee count) |
|            | 주소           | address               | addr | 위치 정보. 회원의 기본 주소(addr)                                  | member.addr                                               |
|            | 비밀번호         | password              |      | 로그인 시 사용하는 비밀번호.                                        | member_authentication.password                            |
|            | 로그인          | login                 |      | 시스템에 접속하는 행위. (예: login_id)                             | member.login_id                                           |
|            | 번호           | number                | no   | 번호                                                      | expenditure_no                                            |
|            | **유형**       | **type**              |      | 데이터의 성격이나 종류를 구분하는 값 (예: 음식 추천 유형, 그룹 유형, 식사 유형)        | member.recommendation_type,group.type                     |
|            | **상태**       | **status**            |      | 데이터의 현재 상태 (예: 영업 상태)                                   | store.status                                              |
|            | **설명**       | **description**       |      | 엔티티나 속성에 대한 부가적인 문자열 정보.                                | food.description                                          |
| **행위/수식어** | 생성           | create                |      | 데이터가 만들어진 시점을 나타낼 때 사용. (예: created_at)                 | created_at                                                |
|            | 수정           | update                |      | 데이터가 변경된 시점을 나타낼 때 사용. (예: updated_at)                  | updated_at                                                |
|            | **알림**       | **notification**      |      | 사용자에게 특정 정보를 전달하는 행위 (예: 푸시 알림).                        | notification_setting                                      |
|            | **추천**       | **recommendation**    |      | 사용자 데이터 기반으로 음식, 음식점을 제안하는 행위.                          | 음식 추천 기능                                                  |
|            | 인가           | Authorization         |      | 사용자가 무엇을 할 수 있는지 결정. Role, ACL, 정책 기반 접근 제어             |                                                           |
|            | 인증           | Authentication        |      | 사용자가 누구인지 확인. 비밀번호 검증, 토큰 확인                            | member_authentication                                     |