# 지출 내역 API 재설계 - 최종 요약

**작성일**: 2025-10-23  
**상태**: 설계 완료 ✅

---

## 📋 핵심 요약 (3분 컷)

### 문제점
```
현재 API 구조:
- 장바구니에서 지출 등록 시 storeId 정보 손실 ❌
- foodId 필수 → 수기 입력 시 무리하게 foodId 처리 ❌
- 음식명 비정규화 안 함 ❌
```

### 해결책
```
개선된 API 구조:
✅ POST /api/v1/expenditures/from-cart (장바구니 전용)
   - storeId + foodId 명시적 포함
   - 음식명 비정규화 저장

✅ POST /api/v1/expenditures (수기 입력, 기존 유지)
   - foodId = NULL 허용
   - 음식명만으로 등록 가능
```

### 장점
```
1. API 의도 명확화: 두 시나리오 분리
2. 데이터 완전성: storeId, foodName 저장
3. UX 개선: hasFoodLink, hasStoreLink로 상세 페이지 링크 가능
4. 확장성: 향후 분석/추천 기능 강화 용이
```

---

## 🎯 핵심 변경사항

### 1️⃣ 스키마 (3개 변경)

| 테이블 | 변경 | 이유 |
|--------|------|------|
| expenditure | +store_id (논리 FK) | 가게 상세 페이지 링크 |
| expenditure_item | -food_id (NOT NULL → nullable) | 수기 입력 지원 |
| expenditure_item | +food_name (비정규화) | 향후 활용 + 쿼리 최적화 |

### 2️⃣ 도메인 모델 (4개 메서드)

```java
// Expenditure
✅ createFromCart(storeId, ...)        // 장바구니
✅ createFromManualInput(...)          // 수기 입력

// ExpenditureItem
✅ createFromFood(foodId, foodName, ...)           // 음식 제공
✅ createFromManualInput(foodName, ...)            // 음식명만
```

### 3️⃣ API 엔드포인트 (1개 추가)

```
기존 유지: POST /api/v1/expenditures
신규 추가: POST /api/v1/expenditures/from-cart
```

### 4️⃣ DTO (3개 추가/변경)

- `CreateExpenditureFromCartRequest` (신규)
- `CreateExpenditureResponse` (수정: storeId, hasStoreLink)
- `ExpenditureItemResponse` (수정: foodName, hasFoodLink)

---

## 📂 제공 문서

### 1. API_REDESIGN_EXPENDITURE.md ⭐ 필독
```
- 전체 설계 방향
- 스키마 다이어그램
- DTO 상세
- 팩토리 메서드
- 사용 예시
→ 개발자용 완벽한 레퍼런스
```

### 2. API_REDESIGN_IMPLEMENTATION_GUIDE.md 🛠️
```
- 단계별 구현 순서 (7단계)
- 복사/붙여넣기 가능한 코드
- 테스트 코드 예시
- 마이그레이션 SQL
- 체크리스트
→ 실제 구현 시 이 문서 따라하기
```

### 3. API_REDESIGN_BEFORE_AFTER.md 📊
```
- Before/After 비교
- 시나리오별 API 호출 예시
- 클라이언트 영향도
- FAQ
→ 이해 관계자용 설명 자료
```

---

## ⏱️ 예상 구현 시간

| 단계 | 작업 | 소요 시간 |
|------|------|---------|
| 1 | Domain 엔티티 수정 | 1시간 |
| 2 | JPA 엔티티 수정 | 1시간 |
| 3 | 마이그레이션 스크립트 | 30분 |
| 4 | DTO 작성 | 1.5시간 |
| 5 | Service 구현 | 1시간 |
| 6 | Controller 추가 | 30분 |
| 7 | 테스트 작성 | 2시간 |
| **총합** | | **7.5시간** |

---

## ✅ 배포 체크리스트

### 구현 전
- [ ] 팀 회의로 설계 검토
- [ ] 테스트 DB에서 스키마 변경 검증
- [ ] 기존 API 호환성 테스트 계획 수립

### 구현 중
- [ ] Domain 엔티티 구현 및 단위 테스트
- [ ] JPA 엔티티 구현 및 테스트
- [ ] DTO 구현
- [ ] Service 구현
- [ ] Controller 구현
- [ ] 통합 테스트 (양 시나리오)

### 구현 후
- [ ] 전체 통합 테스트 통과
- [ ] REST Docs 업데이트
- [ ] 기존 API 호환성 최종 검증
- [ ] Code Review (팀원)

### 배포
- [ ] DB 마이그레이션 (테스트 → 실제)
- [ ] 신규 API 배포
- [ ] 모니터링 설정
- [ ] 롤백 계획 대기

---

## 🔄 호환성 및 롤백

### ✅ 기존 API 호환성
```
❌ Breaking Change 없음
✅ 기존 POST /api/v1/expenditures 그대로 사용 가능
✅ 모든 GET/PUT/DELETE 완벽 호환
```

### 🚨 긴급 롤백 (필요시)
```sql
-- 1단계: DB 롤백
ALTER TABLE expenditure DROP COLUMN store_id;
ALTER TABLE expenditure_item MODIFY food_id BIGINT NOT NULL;
ALTER TABLE expenditure_item DROP COLUMN food_name;

-- 2단계: 코드 롤백
git revert <commit>

-- 3단계: 재배포
```

---

## 📈 향후 확장 가능성

### Phase 2 (향후)
```
1. 음식명 기반 자동 분류 (ML)
2. 지출 트렌드 분석 (storeId 활용)
3. 맞춤 추천 알고리즘 개선 (foodName 비정규화 활용)
4. 영수증 자동 파싱 (foodName 명시적 저장)
```

### 설계의 확장성
```
✅ 논리 FK 구조 → 향후 물리 FK로 전환 용이
✅ foodName 비정규화 → 역정규화 기능 구현 가능
✅ hasFoodLink, hasStoreLink → 프론트 렌더링 로직 독립적
```

---

## 🚀 즉시 실행 항목

### 이 주 (Week 1)
```
1. ✅ 이 문서 완독 (30분)
2. ✅ API_REDESIGN_EXPENDITURE.md 상세 검토 (1시간)
3. ✅ 팀 회의로 설계 승인 (30분)
4. ✅ 마이그레이션 스크립트 테스트 DB 검증 (1시간)
```

### 다음 주 (Week 2)
```
1. 구현 시작 (API_REDESIGN_IMPLEMENTATION_GUIDE.md 따라하기)
2. 일일 진행 상황 공유
3. 완료 후 Code Review
```

---

## 📞 문의 및 개선

### 이 설계에 대해 질문이 있으면?
1. API_REDESIGN_BEFORE_AFTER.md의 FAQ 확인
2. 팀 회의에서 논의
3. 아래 연락처로 질문

### 개선 제안
```
설계 중 놓친 부분이 있으면:
1. GitHub Issue 생성
2. Pull Request로 문서 수정
3. 팀과 논의 후 반영
```

---

## 📚 참고 자료

| 문서 | 용도 | 독자 |
|------|------|------|
| API_REDESIGN_EXPENDITURE.md | 완전한 명세서 | 개발자 |
| API_REDESIGN_IMPLEMENTATION_GUIDE.md | 구현 가이드 | 개발자 |
| API_REDESIGN_BEFORE_AFTER.md | 이해관계자 설명 | PM, QA, Frontend |
| API_SPECIFICATION.md | 전체 API 명세 | 모두 |

---

## ✨ 최종 정리

### 이 개선의 핵심
```
"두 가지 입력 시나리오 (장바구니 vs 수기)를
명확히 분리하고, 각 시나리오의 데이터를 완전히 저장하며,
향후 확장 가능한 구조로 설계"
```

### 얻는 것
```
1. API 명확성 ↑
2. 데이터 품질 ↑
3. 확장성 ↑
4. 사용자 경험 ↑
```

### 비용
```
구현 시간: 7.5시간
DB 변경: 3개 칼럼
API 추가: 1개 엔드포인트
Breaking Change: 0
```

---

## 🎉 다음 단계

```
1단계: ✅ 설계 검토 (완료)
       ↓
2단계: 팀 회의 승인
       ↓
3단계: 구현 (GUIDE 문서 따라)
       ↓
4단계: 테스트 및 Review
       ↓
5단계: 배포 및 모니터링
       ↓
6단계: 문서화 완료
```

**준비 완료! 이제 구현만 남았습니다. 🚀**

