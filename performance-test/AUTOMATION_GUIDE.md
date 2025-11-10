# 성능 테스트 자동화 가이드

## 개요

JMeter 성능 테스트 실행 시 테스트 데이터를 자동으로 확인하고 삽입하는 기능이 추가되었습니다.

## 자동화 기능

### 1. 테스트 데이터 자동 확인

`run-test.sh` 스크립트는 테스트 실행 전에 다음을 자동으로 확인합니다:

```bash
# Store 테이블의 데이터 개수 확인
SELECT COUNT(*) FROM store;
```

**조건**:
- Store 테이블의 데이터가 **10개 미만**이면 자동으로 테스트 데이터를 삽입합니다.
- 10개 이상이면 기존 데이터를 사용합니다.

### 2. 테스트 데이터 자동 삽입

데이터가 부족할 경우 자동으로 `test-data.sql`을 실행합니다:

```bash
# 1. 기존 데이터 삭제 (Foreign Key 제약 때문에 순서 중요)
DELETE FROM food;
DELETE FROM store;
DELETE FROM member_group;

# 2. 테스트 데이터 삽입
docker exec -i smartmealtable-mysql mysql -uroot -proot123 smartmealtable < test-data.sql
```

**삽입되는 데이터**:
- Store: 20개 (치킨집, 피자집, 맥도날드, 스타벅스 등)
- Food: 22개 (치킨, 파스타, 떡볶이 등)
- Group: 19개 (서울대학교, 삼성전자 등)

### 3. 데이터 삽입 결과 확인

삽입 후 자동으로 각 테이블의 데이터 개수를 확인하고 출력합니다:

```bash
SELECT COUNT(*) FROM store;    # 20개
SELECT COUNT(*) FROM food;     # 22개
SELECT COUNT(*) FROM member_group;  # 19개
```

## 사용 방법

### 자동 실행 (권장)

```bash
cd performance-test
./run-test.sh
```

**실행 흐름**:
1. ✅ JMeter 설치 확인
2. ✅ 애플리케이션 상태 확인 (http://localhost:8080)
3. ✅ MySQL 컨테이너 실행 확인
4. ✅ **테스트 데이터 자동 확인**
   - Store < 10개: 자동 삽입 실행
   - Store >= 10개: 기존 데이터 사용
5. ✅ 이전 결과 백업
6. ✅ JMeter 테스트 실행
7. ✅ HTML 리포트 생성

### 수동 실행 (필요 시)

테스트 데이터를 수동으로 다시 삽입하려면:

```bash
# 1. 기존 데이터 삭제
docker exec smartmealtable-mysql mysql -uroot -proot123 smartmealtable -e "DELETE FROM food; DELETE FROM store; DELETE FROM member_group;"

# 2. 테스트 데이터 삽입
docker exec -i smartmealtable-mysql mysql -uroot -proot123 smartmealtable < performance-test/test-data.sql

# 3. 확인
docker exec smartmealtable-mysql mysql -uroot -proot123 smartmealtable -e "SELECT COUNT(*) FROM store; SELECT COUNT(*) FROM food; SELECT COUNT(*) FROM member_group;"
```

## 실행 예시

### Case 1: 데이터가 없을 때

```bash
$ ./run-test.sh

========================================
  JMeter 성능 테스트 실행
========================================
✓ JMeter 설치 확인 완료

📡 애플리케이션 상태 확인 중...
✓ 애플리케이션이 실행 중입니다 (http://localhost:8080)

🗄️  MySQL 연결 및 테스트 데이터 확인 중...
✓ MySQL 컨테이너 실행 중
⚠️  테스트 데이터가 부족합니다 (Store: 0개)
📝 테스트 데이터 삽입 중...
✓ 테스트 데이터 삽입 완료
  - Store: 20개
  - Food: 22개
  - Group: 19개

📊 테스트 설정:
  Base URL: http://localhost:8080
  ...
```

### Case 2: 데이터가 이미 있을 때

```bash
$ ./run-test.sh

========================================
  JMeter 성능 테스트 실행
========================================
✓ JMeter 설치 확인 완료

📡 애플리케이션 상태 확인 중...
✓ 애플리케이션이 실행 중입니다 (http://localhost:8080)

🗄️  MySQL 연결 및 테스트 데이터 확인 중...
✓ MySQL 컨테이너 실행 중
✓ 테스트 데이터 존재 확인 (Store: 20개)

📊 테스트 설정:
  Base URL: http://localhost:8080
  ...
```

## 트러블슈팅

### MySQL 연결 실패

**증상**:
```
❌ MySQL 컨테이너가 실행되지 않았습니다!
```

**해결**:
```bash
cd /path/to/SmartMealTable-springboot-backend-V2
docker-compose -f docker-compose.local.yml up -d smartmealtable-mysql
```

### 테스트 데이터 삽입 실패

**증상**:
```
❌ 테스트 데이터 삽입 실패
```

**해결**:
```bash
# 1. MySQL 로그 확인
docker logs smartmealtable-mysql --tail 50

# 2. 수동 삽입 시도
docker exec -i smartmealtable-mysql mysql -uroot -proot123 smartmealtable < performance-test/test-data.sql

# 3. 에러 메시지 확인
docker exec -i smartmealtable-mysql mysql -uroot -proot123 smartmealtable < performance-test/test-data.sql 2>&1
```

### Foreign Key 제약 에러

**증상**:
```
ERROR 1451 (23000): Cannot delete or update a parent row: a foreign key constraint fails
```

**원인**: 삭제 순서가 잘못됨 (자식 테이블 → 부모 테이블 순서로 삭제해야 함)

**해결**: 스크립트가 자동으로 올바른 순서로 삭제합니다:
```sql
DELETE FROM food;          -- 자식 (store_id FK 참조)
DELETE FROM store;         -- 부모
DELETE FROM member_group;  -- 독립
```

## 커스터마이징

### 자동 삽입 조건 변경

`run-test.sh`의 조건을 수정하여 자동 삽입 조건을 변경할 수 있습니다:

```bash
# 현재: Store < 10개일 때 삽입
if [ -z "$STORE_COUNT" ] || [ "$STORE_COUNT" -lt 10 ]; then

# 변경 예시 1: Store < 20개일 때 삽입
if [ -z "$STORE_COUNT" ] || [ "$STORE_COUNT" -lt 20 ]; then

# 변경 예시 2: Store == 0개일 때만 삽입
if [ -z "$STORE_COUNT" ] || [ "$STORE_COUNT" -eq 0 ]; then
```

### 테스트 데이터 내용 변경

`test-data.sql` 파일을 수정하여 테스트 데이터를 변경할 수 있습니다:

```sql
-- 예시: Store 데이터 추가
INSERT INTO store (name, address, phone_number, store_type, registered_at, latitude, longitude, favorite_count, review_count, view_count) VALUES
('새로운 가게', '서울특별시 강남구 역삼동 1818', '02-1234-9999', 'RESTAURANT', NOW(), 37.4964, 127.0290, 0, 0, 0);
```

## 장점

### 1. 시간 절약
- 매번 수동으로 테스트 데이터를 삽입할 필요 없음
- 테스트 실행 전 사전 준비 시간 단축

### 2. 일관성
- 항상 동일한 테스트 데이터로 테스트 실행
- 테스트 결과 비교가 용이함

### 3. 편의성
- 단일 명령어(`./run-test.sh`)로 모든 준비 완료
- 초보자도 쉽게 사용 가능

### 4. 안정성
- 데이터 존재 여부를 자동으로 확인
- 불필요한 삽입 작업 방지

## 제한사항

### 1. Store 테이블 기준
- 현재는 Store 테이블의 개수만 확인합니다.
- Food, Group 테이블은 별도로 확인하지 않습니다.

### 2. 10개 임계값
- Store 테이블이 10개 미만일 때만 자동 삽입됩니다.
- 정확히 10개 이상이면 삽입하지 않습니다.

### 3. 전체 삭제 후 삽입
- 데이터 삽입 시 기존 데이터를 모두 삭제합니다.
- 기존 데이터를 보존하려면 수동으로 삽입해야 합니다.

## 향후 개선 계획

### Phase 1: 다중 테이블 확인
```bash
# 모든 테이블의 데이터 확인
STORE_COUNT=$(...)
FOOD_COUNT=$(...)
GROUP_COUNT=$(...)

if [ "$STORE_COUNT" -lt 10 ] || [ "$FOOD_COUNT" -lt 10 ] || [ "$GROUP_COUNT" -lt 10 ]; then
    # 자동 삽입
fi
```

### Phase 2: 증분 삽입
```bash
# 기존 데이터를 삭제하지 않고 부족한 만큼만 추가
if [ "$STORE_COUNT" -lt 20 ]; then
    NEEDED=$((20 - STORE_COUNT))
    # $NEEDED 개만 삽입
fi
```

### Phase 3: 환경 변수 지원
```bash
# 자동 삽입 활성화/비활성화
AUTO_INSERT_DATA=true ./run-test.sh
AUTO_INSERT_DATA=false ./run-test.sh

# 임계값 커스터마이징
MIN_STORE_COUNT=20 ./run-test.sh
```

---

**작성일**: 2025-11-10  
**버전**: 1.0.0  
**관련 파일**: `run-test.sh`, `test-data.sql`, `README.md`
