# MySQL Full-Text Search 개선 작업 완료 보고서

**작업일**: 2025-11-05  
**작업자**: AI Assistant  
**작업 범위**: 검색 성능 개선 (LIKE 쿼리 → Full-Text Search)

---

## 📋 작업 개요

### 문제점
기존 시스템에서는 `%like%` 쿼리를 사용하여 검색을 수행하고 있었습니다.

**주요 문제점:**
1. **성능 문제**: `%keyword%` 패턴은 인덱스를 활용하지 못해 Full Table Scan 발생
2. **검색 품질**: 정확한 단어 매칭 불가, 관련성 점수 없음
3. **한국어 지원**: 부분 검색 시 한글 특성 고려 부족

### 해결 방안
MySQL의 **Full-Text Search**와 **ngram parser**를 활용하여 검색 성능과 품질을 개선했습니다.

---

## 🎯 적용 범위

### 1. DDL 변경
다음 테이블에 Full-Text Index 추가:

#### `member_group` 테이블
```sql
FULLTEXT INDEX ft_group_name (name) WITH PARSER ngram
```

#### `store` 테이블
```sql
FULLTEXT INDEX ft_store_search (name, description) WITH PARSER ngram
```

#### `food` 테이블
```sql
FULLTEXT INDEX ft_food_search (food_name, description) WITH PARSER ngram
```

**특징:**
- `ngram parser` 사용으로 한국어 부분 검색 지원
- 2-gram 토큰화로 "맘스" 검색 시 "맘스터치" 매칭 가능
- `name`과 `description` 모두 검색 대상으로 포함

---

### 2. Repository 개선

#### `StoreJpaRepository`

**신규 메서드 추가:**
```java
/**
 * Full-Text Search를 사용한 가게 검색 (관련성 점수 기반)
 */
Page<StoreJpaEntity> searchByFullText(String keyword, Pageable pageable);
```

**쿼리 특징:**
- `MATCH(name, description) AGAINST(keyword IN NATURAL LANGUAGE MODE)` 사용
- 관련성 점수 기반 정렬 (Relevance Score DESC)
- 삭제된 가게 자동 제외 (`deleted_at IS NULL`)
- 페이징 지원

**자동완성 메서드 개선:**
```java
List<StoreJpaEntity> searchByKeywordForAutocomplete(String keyword, int limit);
```

**개선 사항:**
- Full-Text Search 활용
- 카테고리명 검색은 LIKE 유지 (별도 Full-Text Index 없음)
- 관련성 점수 → 조회수 → 이름 순 정렬

---

#### `GroupJpaRepository`

**기존 메서드 Full-Text Search로 전환:**

```java
/**
 * Full-Text Search를 사용한 그룹 이름 검색
 */
List<GroupJpaEntity> findByNameContaining(String name);

/**
 * 타입과 이름으로 검색 (Full-Text Search, 페이징)
 */
Page<GroupJpaEntity> searchGroups(String type, String name, Pageable pageable);
```

**주요 변경:**
- `LIKE %:name%` → `MATCH(name) AGAINST(:name IN NATURAL LANGUAGE MODE)`
- `GroupType` enum → `String` 파라미터로 변경 (Native Query 호환)
- 관련성 점수 기반 정렬

**GroupRepositoryImpl 수정:**
```java
String typeParam = (type != null) ? type.name() : null;
Page<GroupJpaEntity> jpaPage = jpaRepository.searchGroups(typeParam, name, pageRequest);
```

---

### 3. 테스트 작성

#### `StoreJpaRepositoryFullTextSearchTest`

**테스트 케이스:**
1. ✅ `searchByFullText` 메서드 Page 반환 검증
2. ✅ `searchByKeywordForAutocomplete` 메서드 List 반환 검증
3. ✅ Full-Text Search 쿼리 대상 필드 확인 (name, description)

**특징:**
- Mock 기반 단위 테스트
- Repository 메서드 시그니처 및 반환 타입 검증
- 실제 Full-Text Index는 DDL 적용 후 통합 테스트에서 검증 예정

---

## 📊 기대 효과

### 1. 성능 개선
| 항목 | Before (LIKE) | After (Full-Text) |
|------|---------------|-------------------|
| 인덱스 활용 | ❌ Full Table Scan | ✅ FULLTEXT Index 사용 |
| 검색 속도 | 느림 (데이터 증가 시 선형 증가) | 빠름 (로그 수준 증가) |
| 한국어 지원 | 제한적 | ✅ ngram parser 지원 |

### 2. 검색 품질 개선
- ✅ **관련성 점수**: 검색어와의 관련도가 높은 결과 우선 표시
- ✅ **부분 검색**: "맘스" 검색 시 "맘스터치" 매칭
- ✅ **자연어 검색**: MySQL의 Natural Language Mode 활용

### 3. 확장성
- ✅ 데이터 증가에도 안정적인 성능 유지
- ✅ 추가 언어 지원 용이 (parser 변경만으로 가능)
- ✅ 검색 알고리즘 개선 여지 (Boolean Mode, Query Expansion 등)

---

## 🚀 배포 가이드

### 1. DDL 적용
```sql
-- Production DB에 적용
ALTER TABLE member_group 
ADD FULLTEXT INDEX ft_group_name (name) WITH PARSER ngram;

ALTER TABLE store 
ADD FULLTEXT INDEX ft_store_search (name, description) WITH PARSER ngram;

ALTER TABLE food 
ADD FULLTEXT INDEX ft_food_search (food_name, description) WITH PARSER ngram;
```

**주의사항:**
- Index 생성 시간: 테이블 크기에 따라 수분~수십 분 소요 가능
- Online Index 생성 지원 (서비스 중단 불필요)
- 디스크 공간 추가 필요 (테이블 크기의 약 30-50%)

### 2. 설정 확인
```sql
-- MySQL Full-Text Search 설정 확인
SHOW VARIABLES LIKE 'ngram_token_size';  -- 기본값: 2 (권장)
SHOW VARIABLES LIKE 'ft_min_word_len';   -- 기본값: 4
```

### 3. 검증 쿼리
```sql
-- Full-Text Search 동작 확인
SELECT name, MATCH(name, description) AGAINST('치킨' IN NATURAL LANGUAGE MODE) as score
FROM store
WHERE MATCH(name, description) AGAINST('치킨' IN NATURAL LANGUAGE MODE)
ORDER BY score DESC
LIMIT 10;
```

---

## 📝 추가 개선 사항 (향후 고려)

### 1. Food 검색 Full-Text 적용
현재 `food` 테이블에 Full-Text Index는 추가되었으나, Repository 메서드는 아직 적용되지 않았습니다.

**적용 대상:**
- `FoodJpaRepository` (존재 시)
- 온보딩 음식 검색 API

### 2. 하이브리드 검색 전략
```java
// 1. Full-Text Search 시도
Page<Store> results = fullTextSearch(keyword);

// 2. 결과가 없으면 LIKE 검색으로 Fallback
if (results.isEmpty()) {
    results = likeSearch("%" + keyword + "%");
}
```

### 3. 검색어 전처리 유틸리티
```java
public class SearchQueryPreprocessor {
    public static String normalize(String keyword) {
        return keyword
            .trim()
            .toLowerCase()
            .replaceAll("\\s+", " ")
            .replaceAll("[^a-zA-Z0-9가-힣\\s]", "");
    }
}
```

### 4. Boolean Mode 활용 (고급 검색)
```sql
-- AND 검색
MATCH(name) AGAINST('+치킨 +강남' IN BOOLEAN MODE)

-- NOT 검색
MATCH(name) AGAINST('+치킨 -후라이드' IN BOOLEAN MODE)
```

---

## 🔍 모니터링 포인트

### 1. 검색 성능
```sql
-- 느린 쿼리 확인
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';
```

### 2. Index 상태
```sql
-- Index 사용률 확인
SHOW INDEX FROM store;
ANALYZE TABLE store;
```

### 3. 검색 품질
- 검색 결과 관련성 피드백 수집
- 검색어별 클릭률 분석
- 검색 결과 없음(No Results) 비율 추적

---

## ✅ 완료 항목

- [x] DDL에 Full-Text Index 추가 (store, food, member_group)
- [x] StoreJpaRepository Full-Text Search 메서드 구현
- [x] GroupJpaRepository Full-Text Search 전환
- [x] 단위 테스트 작성 (StoreJpaRepositoryFullTextSearchTest)
- [x] GroupRepositoryImpl 수정 (타입 파라미터 변환)
- [x] 빌드 및 컴파일 검증

---

## 📌 참고 자료

### MySQL Full-Text Search 문서
- [MySQL 8.0 Full-Text Search](https://dev.mysql.com/doc/refman/8.0/en/fulltext-search.html)
- [ngram Full-Text Parser](https://dev.mysql.com/doc/refman/8.0/en/fulltext-search-ngram.html)

### 관련 파일
- `ddl.sql` - Full-Text Index 정의
- `StoreJpaRepository.java` - 가게 검색 Repository
- `GroupJpaRepository.java` - 그룹 검색 Repository
- `StoreJpaRepositoryFullTextSearchTest.java` - 테스트

---

## 🎉 결론

MySQL Full-Text Search 도입으로 **검색 성능**과 **검색 품질**을 모두 개선했습니다.

**핵심 개선 사항:**
1. ⚡ **성능 향상**: Full Table Scan → FULLTEXT Index 활용
2. 🎯 **검색 품질**: 관련성 점수 기반 정렬
3. 🇰🇷 **한국어 지원**: ngram parser로 부분 검색 최적화
4. 📈 **확장성**: 데이터 증가에도 안정적인 성능

**다음 단계:**
- DDL 프로덕션 적용
- 통합 테스트 (실제 데이터)
- 성능 모니터링 및 튜닝
