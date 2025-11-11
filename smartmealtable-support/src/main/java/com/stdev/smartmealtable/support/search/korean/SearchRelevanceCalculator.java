package com.stdev.smartmealtable.support.search.korean;

/**
 * 검색 결과의 관련성(Relevance) 점수 계산
 *
 * 배달앱처럼 동작하는 검색 정렬 로직:
 * 1. 완전 일치 (모든 글자가 포함된 가장 짧은 항목)
 * 2. 부분 일치 (일부 글자만 포함)
 * 3. 같은 매칭 타입 내에서는 인기도 순
 *
 * 예시:
 * - 검색: "고추" (2자)
 * - "고추" (2/2 일치) → Relevance: 1000
 * - "고추장" (2/3 일치) → Relevance: 500
 * - "매운 고추" (2/4 일치) → Relevance: 400
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
public class SearchRelevanceCalculator {

    private SearchRelevanceCalculator() {
        // Utility class - 인스턴스화 방지
    }

    /**
     * 검색 결과의 관련성 점수 계산
     *
     * 점수 구성:
     * - 매칭 타입 점수 (10000점 단위)
     *   - 완전 일치 (모든 글자 일치): 10000
     *   - 부분 일치: 5000
     * - 길이 보너스 (길이가 짧을수록 높음)
     *   - targetLength가 짧을수록 높은 점수
     * - 인기도 점수 (0~1000)
     *   - 검색 횟수나 좋아요 수
     *
     * @param keyword 검색 키워드
     * @param target 대상 문자열
     * @param popularityScore 인기도 점수 (0~1000, 선택사항)
     * @return 관련성 점수 (높을수록 관련성 높음)
     */
    public static int calculateRelevance(String keyword, String target, int popularityScore) {
        if (keyword == null || keyword.isEmpty() || target == null || target.isEmpty()) {
            return 0;
        }

        // 인기도 점수 정규화 (0~1000)
        int normalizedPopularity = Math.min(Math.max(popularityScore, 0), 1000);

        // 대소문자 무시 검색
        String keywordLower = keyword.toLowerCase();
        String targetLower = target.toLowerCase();

        // 1. 완전 일치 여부 판단
        boolean isExactMatch = isCompleteMatch(keywordLower, targetLower);

        if (isExactMatch) {
            // 완전 일치: 10000 + 길이 보너스 + 인기도
            int lengthBonus = calculateLengthBonus(keyword.length(), target.length());
            return 10000 + lengthBonus + normalizedPopularity;
        } else if (targetLower.contains(keywordLower)) {
            // 부분 일치: 5000 + 길이 보너스 + 인기도
            int lengthBonus = calculateLengthBonus(keyword.length(), target.length());
            return 5000 + lengthBonus + normalizedPopularity;
        }

        // 일치하지 않음
        return 0;
    }

    /**
     * 완전 일치 판단 (모든 글자가 순서대로 포함)
     *
     * 예시:
     * - "고추" in "고추" → true
     * - "고추" in "고추장" → true (모든 글자가 순서대로 포함)
     * - "고추" in "장고추" → true (모든 글자가 순서대로 포함)
     * - "고추" in "고장추" → false (순서가 다름)
     *
     * @param keyword 검색 키워드
     * @param target 대상 문자열
     * @return 완전 일치 여부
     */
    private static boolean isCompleteMatch(String keyword, String target) {
        if (keyword.isEmpty() || target.isEmpty()) {
            return false;
        }

        // 완전 일치: 대상 문자열이 키워드를 연속으로 포함하는지 확인
        // 예: "고추" in "고추장" → true
        // 예: "고추" in "장고추" → true (부분 일치이지만 모든 글자 포함)
        return target.contains(keyword);
    }

    /**
     * 길이 기반 보너스 계산
     *
     * 같은 매칭 타입 내에서 더 짧은 결과에 높은 점수 부여
     * - 키워드 길이와 유사한 길이 → 높은 점수
     * - 키워드보다 훨씬 길면 → 낮은 점수
     *
     * @param keywordLength 키워드 길이
     * @param targetLength 대상 문자열 길이
     * @return 길이 보너스 (0~1000)
     */
    private static int calculateLengthBonus(int keywordLength, int targetLength) {
        // 길이 차이 계산
        int lengthDiff = targetLength - keywordLength;

        // 길이 차이가 작을수록 높은 점수
        // 같은 길이: 1000점
        // 1글자 길어짐: 900점
        // 2글자 길어짐: 800점
        // ...
        // 10글자 이상 길어짐: 0점 이상

        int bonus = Math.max(1000 - (lengthDiff * 100), 0);
        return Math.min(bonus, 1000);
    }

    /**
     * 정규화된 관련성 점수 (0.0 ~ 1.0)
     *
     * @param keyword 검색 키워드
     * @param target 대상 문자열
     * @param popularityScore 인기도 점수 (0~1000)
     * @return 정규화된 점수 (0.0 ~ 1.0)
     */
    public static double calculateNormalizedRelevance(String keyword, String target, int popularityScore) {
        int rawScore = calculateRelevance(keyword, target, popularityScore);
        // 최대 점수: 10000 + 1000(length bonus) + 1000(popularity) = 12000
        return Math.min(rawScore / 12000.0, 1.0);
    }

    /**
     * 디버깅용: 관련성 점수 상세 정보
     */
    public static class RelevanceDetail {
        public final String keyword;
        public final String target;
        public final int matchType; // 0: no match, 1: partial match, 2: complete match
        public final int lengthBonus;
        public final int popularityScore;
        public final int totalScore;

        public RelevanceDetail(String keyword, String target, int popularityScore) {
            this.keyword = keyword;
            this.target = target;

            String keywordLower = keyword.toLowerCase();
            String targetLower = target.toLowerCase();

            if (isCompleteMatch(keywordLower, targetLower)) {
                this.matchType = 2;
            } else if (targetLower.contains(keywordLower)) {
                this.matchType = 1;
            } else {
                this.matchType = 0;
            }

            this.lengthBonus = calculateLengthBonus(keyword.length(), target.length());
            this.popularityScore = Math.min(Math.max(popularityScore, 0), 1000);
            this.totalScore = calculateRelevance(keyword, target, popularityScore);
        }

        @Override
        public String toString() {
            return String.format(
                "RelevanceDetail{keyword='%s', target='%s', matchType=%d, lengthBonus=%d, popularityScore=%d, totalScore=%d}",
                keyword, target, matchType, lengthBonus, popularityScore, totalScore
            );
        }
    }

    /**
     * 상세 분석용 메서드
     */
    public static RelevanceDetail analyzeRelevance(String keyword, String target, int popularityScore) {
        return new RelevanceDetail(keyword, target, popularityScore);
    }
}
