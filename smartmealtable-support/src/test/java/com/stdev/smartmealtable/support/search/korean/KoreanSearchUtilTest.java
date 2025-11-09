package com.stdev.smartmealtable.support.search.korean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * KoreanSearchUtil 단위 테스트
 * 
 * 테스트 범위:
 * 1. 초성 추출
 * 2. 초성 매칭
 * 3. 편집 거리 계산
 * 4. 오타 허용 매칭
 * 5. 엣지 케이스 (null, 빈 문자열, 특수문자)
 */
@DisplayName("KoreanSearchUtil 단위 테스트")
class KoreanSearchUtilTest {

    // ==================== 초성 추출 테스트 ====================
    
    @Test
    @DisplayName("초성 추출: 한글만 포함된 문자열")
    void extractChosung_Korean() {
        assertThat(KoreanSearchUtil.extractChosung("서울대학교")).isEqualTo("ㅅㅇㄷㅎㄱ");
        assertThat(KoreanSearchUtil.extractChosung("삼성전자")).isEqualTo("ㅅㅅㅈㅈ");
        assertThat(KoreanSearchUtil.extractChosung("김치찌개")).isEqualTo("ㄱㅊㅉㄱ");
        assertThat(KoreanSearchUtil.extractChosung("고려대학교")).isEqualTo("ㄱㄹㄷㅎㄱ");
    }
    
    @Test
    @DisplayName("초성 추출: 한글 + 영문 혼합")
    void extractChosung_KoreanAndEnglish() {
        assertThat(KoreanSearchUtil.extractChosung("abc서울대")).isEqualTo("ㅅㅇㄷ");
        assertThat(KoreanSearchUtil.extractChosung("서울대ABC")).isEqualTo("ㅅㅇㄷ");
        assertThat(KoreanSearchUtil.extractChosung("서울123대학교")).isEqualTo("ㅅㅇㄷㅎㄱ");
    }
    
    @Test
    @DisplayName("초성 추출: 빈 문자열")
    void extractChosung_Empty() {
        assertThat(KoreanSearchUtil.extractChosung("")).isEmpty();
        assertThat(KoreanSearchUtil.extractChosung(null)).isEmpty();
    }
    
    @Test
    @DisplayName("초성 추출: 한글이 없는 문자열")
    void extractChosung_NoKorean() {
        assertThat(KoreanSearchUtil.extractChosung("abc123")).isEmpty();
        assertThat(KoreanSearchUtil.extractChosung("!@#$%")).isEmpty();
    }

    // ==================== 초성 매칭 테스트 ====================
    
    @Test
    @DisplayName("초성 매칭: 시작 부분 매칭")
    void matchesChosung_StartsWith() {
        assertThat(KoreanSearchUtil.matchesChosung("ㅅㅇㄷ", "서울대학교")).isTrue();
        assertThat(KoreanSearchUtil.matchesChosung("ㄱㅊ", "김치찌개")).isTrue();
    }
    
    @Test
    @DisplayName("초성 매칭: 중간 부분 매칭")
    void matchesChosung_Contains() {
        assertThat(KoreanSearchUtil.matchesChosung("ㄷㅎㄱ", "서울대학교")).isTrue();
        assertThat(KoreanSearchUtil.matchesChosung("ㅊㅉ", "김치찌개")).isTrue();
    }
    
    @Test
    @DisplayName("초성 매칭: 완전 매칭")
    void matchesChosung_FullMatch() {
        assertThat(KoreanSearchUtil.matchesChosung("ㅅㅇㄷㅎㄱ", "서울대학교")).isTrue();
        assertThat(KoreanSearchUtil.matchesChosung("ㄱㅊㅉㄱ", "김치찌개")).isTrue();
    }
    
    @Test
    @DisplayName("초성 매칭: 매칭 실패")
    void matchesChosung_NoMatch() {
        assertThat(KoreanSearchUtil.matchesChosung("ㅅㅇㄷㅎㄱㅅ", "서울대학교")).isFalse(); // 초성이 더 김
        assertThat(KoreanSearchUtil.matchesChosung("ㅅㅅㅅ", "서울대학교")).isFalse(); // 다른 초성
    }
    
    @Test
    @DisplayName("초성 매칭: null 또는 빈 문자열")
    void matchesChosung_NullOrEmpty() {
        assertThat(KoreanSearchUtil.matchesChosung(null, "서울대학교")).isFalse();
        assertThat(KoreanSearchUtil.matchesChosung("", "서울대학교")).isFalse();
    }

    // ==================== 시작 매칭 테스트 ====================
    
    @Test
    @DisplayName("초성 시작 매칭: 성공")
    void startsWithChosung_Success() {
        assertThat(KoreanSearchUtil.startsWithChosung("ㅅㅇ", "서울대학교")).isTrue();
        assertThat(KoreanSearchUtil.startsWithChosung("ㄱㅊ", "김치찌개")).isTrue();
    }
    
    @Test
    @DisplayName("초성 시작 매칭: 실패 (중간 매칭)")
    void startsWithChosung_Fail() {
        assertThat(KoreanSearchUtil.startsWithChosung("ㄷㅎ", "서울대학교")).isFalse(); // 중간에는 있지만 시작 아님
        assertThat(KoreanSearchUtil.startsWithChosung("ㅉㄱ", "김치찌개")).isFalse();
    }

    // ==================== 한글 확인 테스트 ====================
    
    @ParameterizedTest
    @CsvSource({
        "가, true",
        "힣, true",
        "서, true",
        "a, false",
        "1, false",
        "!, false"
    })
    @DisplayName("한글 문자 확인")
    void isKorean(char ch, boolean expected) {
        assertThat(KoreanSearchUtil.isKorean(ch)).isEqualTo(expected);
    }

    // ==================== 초성 문자열 확인 테스트 ====================
    
    @Test
    @DisplayName("초성 문자열 확인: 초성만 포함")
    void isChosung_OnlyChosung() {
        assertThat(KoreanSearchUtil.isChosung("ㅅㅇㄷㅎㄱ")).isTrue();
        assertThat(KoreanSearchUtil.isChosung("ㄱㅊㅉㄱ")).isTrue();
        assertThat(KoreanSearchUtil.isChosung("ㅎ")).isTrue();
    }
    
    @Test
    @DisplayName("초성 문자열 확인: 초성 아님")
    void isChosung_NotChosung() {
        assertThat(KoreanSearchUtil.isChosung("서울대")).isFalse(); // 한글
        assertThat(KoreanSearchUtil.isChosung("abc")).isFalse(); // 영문
        assertThat(KoreanSearchUtil.isChosung("ㅅㅇㄷabc")).isFalse(); // 초성 + 영문
    }
    
    @Test
    @DisplayName("초성 문자열 확인: null 또는 빈 문자열")
    void isChosung_NullOrEmpty() {
        assertThat(KoreanSearchUtil.isChosung(null)).isFalse();
        assertThat(KoreanSearchUtil.isChosung("")).isFalse();
    }

    // ==================== 편집 거리 계산 테스트 ====================
    
    @Test
    @DisplayName("편집 거리: 완전 일치")
    void calculateEditDistance_Identical() {
        assertThat(KoreanSearchUtil.calculateEditDistance("서울대학교", "서울대학교")).isEqualTo(0);
        assertThat(KoreanSearchUtil.calculateEditDistance("김치찌개", "김치찌개")).isEqualTo(0);
    }
    
    @ParameterizedTest
    @CsvSource({
        "서울대, 서울대학교, 2",     // 2글자 추가
        "고려대, 고려대학교, 2",     // 2글자 추가
        "김치, 김지, 1",            // 1글자 대체
        "카레, 커리, 2",            // 2글자 대체
        "abc, def, 3"              // 3글자 대체
    })
    @DisplayName("편집 거리: 다양한 케이스")
    void calculateEditDistance_Various(String s1, String s2, int expected) {
        assertThat(KoreanSearchUtil.calculateEditDistance(s1, s2)).isEqualTo(expected);
    }
    
    @Test
    @DisplayName("편집 거리: 빈 문자열")
    void calculateEditDistance_Empty() {
        assertThat(KoreanSearchUtil.calculateEditDistance("", "서울대")).isEqualTo(3);
        assertThat(KoreanSearchUtil.calculateEditDistance("서울대", "")).isEqualTo(3);
        assertThat(KoreanSearchUtil.calculateEditDistance("", "")).isEqualTo(0);
    }
    
    @Test
    @DisplayName("편집 거리: null 입력 시 예외")
    void calculateEditDistance_NullThrowsException() {
        assertThatThrownBy(() -> KoreanSearchUtil.calculateEditDistance(null, "서울대"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("입력 문자열은 null일 수 없습니다");
        
        assertThatThrownBy(() -> KoreanSearchUtil.calculateEditDistance("서울대", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("입력 문자열은 null일 수 없습니다");
    }
    
    @Test
    @DisplayName("편집 거리: 성능 테스트 (1만 번 실행)")
    void calculateEditDistance_Performance() {
        int iterations = 10_000;
        long start = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            KoreanSearchUtil.calculateEditDistance("서울대학교", "서울시립대학교");
        }
        
        long avgTime = (System.nanoTime() - start) / iterations;
        
        // 평균 실행 시간이 100μs(마이크로초) 이하여야 함
        assertThat(avgTime).isLessThan(100_000); // 100μs = 100,000 나노초
        
        System.out.printf("✅ 편집 거리 계산 평균 실행 시간: %.2f μs%n", avgTime / 1000.0);
    }

    // ==================== 오타 허용 매칭 테스트 ====================
    
    @Test
    @DisplayName("오타 허용 매칭: 완전 일치")
    void matchesWithTypoTolerance_ExactMatch() {
        assertThat(KoreanSearchUtil.matchesWithTypoTolerance("서울대학교", "서울대학교", 2)).isTrue();
    }
    
    @Test
    @DisplayName("오타 허용 매칭: 부분 일치")
    void matchesWithTypoTolerance_Contains() {
        assertThat(KoreanSearchUtil.matchesWithTypoTolerance("서울대", "서울대학교", 2)).isTrue();
    }
    
    @Test
    @DisplayName("오타 허용 매칭: 편집 거리 이내")
    void matchesWithTypoTolerance_WithinDistance() {
        assertThat(KoreanSearchUtil.matchesWithTypoTolerance("서울대학", "서울대학교", 2)).isTrue(); // 거리 1
        assertThat(KoreanSearchUtil.matchesWithTypoTolerance("서울", "서울대학교", 2)).isTrue(); // 거리 3이지만 부분 매칭
    }
    
    @Test
    @DisplayName("오타 허용 매칭: 편집 거리 초과")
    void matchesWithTypoTolerance_ExceedsDistance() {
        assertThat(KoreanSearchUtil.matchesWithTypoTolerance("abc", "xyz", 2)).isFalse(); // 거리 3
    }
    
    @Test
    @DisplayName("오타 허용 매칭: null 입력")
    void matchesWithTypoTolerance_Null() {
        assertThat(KoreanSearchUtil.matchesWithTypoTolerance(null, "서울대학교", 2)).isFalse();
        assertThat(KoreanSearchUtil.matchesWithTypoTolerance("서울대", null, 2)).isFalse();
    }

    // ==================== 통합 시나리오 테스트 ====================
    
    @Test
    @DisplayName("통합 시나리오: 대학교 검색")
    void integrationTest_UniversitySearch() {
        String[] universities = {"서울대학교", "서울시립대학교", "고려대학교", "연세대학교"};
        
        // 1. 초성 검색: "ㅅㅇㄷ" → "서울대학교"만 매칭 (ㅅㅇㄷㅎㄱ)
        //    "서울시립대학교"는 ㅅㅇㅅㄹㄷㅎㄱ 이므로 매칭 안됨 (중간에 ㅅㄹ이 있음)
        assertThat(universities)
            .filteredOn(name -> KoreanSearchUtil.matchesChosung("ㅅㅇㄷ", name))
            .containsExactly("서울대학교");
        
        // 2. 초성 검색: "ㅅㅇ" → "서울대학교", "서울시립대학교" 모두 매칭
        assertThat(universities)
            .filteredOn(name -> KoreanSearchUtil.startsWithChosung("ㅅㅇ", name))
            .containsExactlyInAnyOrder("서울대학교", "서울시립대학교");
        
        // 3. 시작 매칭: "서울"
        assertThat(universities)
            .filteredOn(name -> name.startsWith("서울"))
            .containsExactlyInAnyOrder("서울대학교", "서울시립대학교");
        
        // 4. 초성 시작 매칭: "ㄱㄹ"
        assertThat(universities)
            .filteredOn(name -> KoreanSearchUtil.startsWithChosung("ㄱㄹ", name))
            .containsExactly("고려대학교");
    }
    
    @Test
    @DisplayName("통합 시나리오: 음식 검색")
    void integrationTest_FoodSearch() {
        String[] foods = {"김치찌개", "된장찌개", "김치볶음밥", "김치전"};
        
        // 1. 초성 검색: "ㄱㅊ"
        assertThat(foods)
            .filteredOn(name -> KoreanSearchUtil.matchesChosung("ㄱㅊ", name))
            .containsExactlyInAnyOrder("김치찌개", "김치볶음밥", "김치전");
        
        // 2. 부분 매칭: "찌개"
        assertThat(foods)
            .filteredOn(name -> name.contains("찌개"))
            .containsExactlyInAnyOrder("김치찌개", "된장찌개");
        
        // 3. 초성 완전 매칭: "ㄱㅊㅉㄱ"
        assertThat(foods)
            .filteredOn(name -> KoreanSearchUtil.extractChosung(name).equals("ㄱㅊㅉㄱ"))
            .containsExactly("김치찌개");
    }
}
