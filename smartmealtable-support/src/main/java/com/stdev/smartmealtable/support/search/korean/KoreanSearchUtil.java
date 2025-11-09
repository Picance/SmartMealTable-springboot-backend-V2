package com.stdev.smartmealtable.support.search.korean;

/**
 * 한글 검색 유틸리티
 * 
 * 기능:
 * 1. 한글 초성 추출 (예: "서울대학교" → "ㅅㅇㄷㅎㄱ")
 * 2. 초성 매칭 검증 (예: "ㅅㅇㄷ"가 "서울대학교"와 매칭되는지 확인)
 * 3. 편집 거리 계산 (Levenshtein Distance)
 * 
 * @author SmartMealTable Team
 * @since 2025-11-09
 */
public class KoreanSearchUtil {

    // 한글 유니코드 범위: 0xAC00 ~ 0xD7A3
    private static final int KOREAN_UNICODE_START = 0xAC00; // '가'
    private static final int KOREAN_UNICODE_END = 0xD7A3;   // '힣'
    
    // 초성 19개
    private static final char[] CHOSUNG = {
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    
    // 한글 음절 구성 상수
    private static final int CHOSUNG_COUNT = 19;
    private static final int JUNGSUNG_COUNT = 21;
    private static final int JONGSUNG_COUNT = 28;
    private static final int JUNGSUNG_JONGSUNG_COUNT = JUNGSUNG_COUNT * JONGSUNG_COUNT; // 588

    private KoreanSearchUtil() {
        // Utility class - 인스턴스화 방지
    }

    /**
     * 한글 문자열에서 초성만 추출
     * 
     * 예시:
     * - "서울대학교" → "ㅅㅇㄷㅎㄱ"
     * - "김치찌개" → "ㄱㅊㅉㄱ"
     * - "abc가나다" → "ㄱㄴㄷ" (영문/숫자는 무시)
     * 
     * @param text 원본 텍스트
     * @return 초성 문자열 (한글이 아닌 문자는 제외)
     */
    public static String extractChosung(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        StringBuilder chosung = new StringBuilder();
        
        for (char ch : text.toCharArray()) {
            if (isKorean(ch)) {
                int unicode = ch - KOREAN_UNICODE_START;
                int chosungIndex = unicode / JUNGSUNG_JONGSUNG_COUNT;
                chosung.append(CHOSUNG[chosungIndex]);
            }
            // 한글이 아닌 문자는 무시 (영문, 숫자, 공백 등)
        }
        
        return chosung.toString();
    }

    /**
     * 입력한 초성이 대상 문자열의 초성과 매칭되는지 확인
     * 
     * 예시:
     * - matchesChosung("ㅅㅇㄷ", "서울대학교") → true (시작 부분 매칭)
     * - matchesChosung("ㄷㅎㄱ", "서울대학교") → true (중간 부분 매칭)
     * - matchesChosung("ㅅㅇㄷㅎㄱ", "서울대학교") → true (완전 매칭)
     * - matchesChosung("ㅅㅇㄷㅎㄱㅅ", "서울대학교") → false (초성이 더 김)
     * 
     * @param chosungQuery 검색 초성 (예: "ㅅㅇㄷ")
     * @param targetText 대상 텍스트 (예: "서울대학교")
     * @return 매칭 여부
     */
    public static boolean matchesChosung(String chosungQuery, String targetText) {
        if (chosungQuery == null || chosungQuery.isEmpty()) {
            return false;
        }
        
        String targetChosung = extractChosung(targetText);
        
        // 부분 매칭 지원: targetChosung이 chosungQuery를 포함하면 true
        return targetChosung.contains(chosungQuery);
    }

    /**
     * 두 문자열이 초성으로 시작하는지 확인
     * 
     * 예시:
     * - startsWithChosung("ㅅㅇ", "서울대학교") → true
     * - startsWithChosung("ㄷㅎ", "서울대학교") → false
     * 
     * @param chosungQuery 검색 초성
     * @param targetText 대상 텍스트
     * @return 시작 매칭 여부
     */
    public static boolean startsWithChosung(String chosungQuery, String targetText) {
        if (chosungQuery == null || chosungQuery.isEmpty()) {
            return false;
        }
        
        String targetChosung = extractChosung(targetText);
        return targetChosung.startsWith(chosungQuery);
    }

    /**
     * 문자가 한글인지 확인
     * 
     * @param ch 확인할 문자
     * @return 한글 여부
     */
    public static boolean isKorean(char ch) {
        return ch >= KOREAN_UNICODE_START && ch <= KOREAN_UNICODE_END;
    }

    /**
     * 문자열이 초성으로만 구성되어 있는지 확인
     * 
     * @param text 확인할 문자열
     * @return 초성 여부
     */
    public static boolean isChosung(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (char ch : text.toCharArray()) {
            boolean isChosungChar = false;
            for (char chosung : CHOSUNG) {
                if (ch == chosung) {
                    isChosungChar = true;
                    break;
                }
            }
            if (!isChosungChar) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Levenshtein Distance (편집 거리) 계산
     * 
     * 두 문자열 간의 유사도를 측정. 값이 작을수록 유사함.
     * 
     * 예시:
     * - calculateEditDistance("서울대", "서울대학교") → 2 (2글자 추가)
     * - calculateEditDistance("고려대", "고려대학교") → 2
     * - calculateEditDistance("김치", "김지") → 1 (1글자 대체)
     * 
     * @param s1 문자열 1
     * @param s2 문자열 2
     * @return 편집 거리 (0 = 완전 동일)
     */
    public static int calculateEditDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("입력 문자열은 null일 수 없습니다");
        }
        
        if (s1.equals(s2)) {
            return 0;
        }
        
        int len1 = s1.length();
        int len2 = s2.length();
        
        if (len1 == 0) return len2;
        if (len2 == 0) return len1;
        
        // DP 테이블 초기화
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        // DP 테이블 채우기
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                
                dp[i][j] = Math.min(
                    Math.min(
                        dp[i - 1][j] + 1,     // 삭제
                        dp[i][j - 1] + 1      // 삽입
                    ),
                    dp[i - 1][j - 1] + cost   // 대체
                );
            }
        }
        
        return dp[len1][len2];
    }

    /**
     * 오타 허용 매칭 (편집 거리 기반)
     * 
     * @param query 검색어
     * @param target 대상 문자열
     * @param maxDistance 허용 편집 거리 (기본값: 2)
     * @return 매칭 여부
     */
    public static boolean matchesWithTypoTolerance(String query, String target, int maxDistance) {
        if (query == null || target == null) {
            return false;
        }
        
        // 완전 일치
        if (target.contains(query)) {
            return true;
        }
        
        // 편집 거리 계산
        int distance = calculateEditDistance(query, target);
        return distance <= maxDistance;
    }
}
