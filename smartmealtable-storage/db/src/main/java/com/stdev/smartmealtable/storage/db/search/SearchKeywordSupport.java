package com.stdev.smartmealtable.storage.db.search;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 공통 검색 키워드 생성/정규화 유틸리티.
 * <p>
 * - 특수문자 제거 및 소문자 변환
 * - 부분 문자열 키워드 생성 (최대 길이 제한)
 * - Prefix 길이 고정 (인덱스 필터용)
 */
public final class SearchKeywordSupport {

    private static final Pattern NORMALIZE_PATTERN = Pattern.compile("[^0-9a-zA-Z가-힣]");
    private static final int MAX_SUBSTRING_LENGTH = 20;
    private static final int MAX_KEYWORDS_PER_TEXT = 800;

    /** Prefix 인덱스에 사용할 최대 길이 */
    public static final int KEYWORD_PREFIX_LENGTH = 5;

    private SearchKeywordSupport() {
    }

    /**
     * 검색용 문자열 정규화 (소문자 + 특수문자 제거).
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String lowered = input.toLowerCase();
        return NORMALIZE_PATTERN.matcher(lowered).replaceAll("");
    }

    /**
     * 부분 문자열 기반 검색 키워드 생성.
     */
    public static List<SearchKeyword> generateKeywords(String source) {
        String normalized = normalize(source);
        if (normalized.isEmpty()) {
            return List.of();
        }

        Set<String> candidates = new LinkedHashSet<>();
        outer:
        for (int start = 0; start < normalized.length(); start++) {
            for (int end = start + 1; end <= normalized.length(); end++) {
                if (end - start > MAX_SUBSTRING_LENGTH) {
                    break;
                }
                candidates.add(normalized.substring(start, end));
                if (candidates.size() >= MAX_KEYWORDS_PER_TEXT) {
                    break outer;
                }
            }
        }

        return candidates.stream()
                .map(keyword -> new SearchKeyword(keyword, buildKeywordPrefix(keyword)))
                .toList();
    }

    /**
     * 검색 시 사용할 Prefix (정규화된 문자열의 앞부분).
     */
    public static String buildQueryPrefix(String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isEmpty()) {
            return "";
        }
        return buildKeywordPrefix(normalizedKeyword);
    }

    private static String buildKeywordPrefix(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return "";
        }
        int endIndex = Math.min(keyword.length(), KEYWORD_PREFIX_LENGTH);
        return keyword.substring(0, endIndex);
    }

    /**
     * 검색 키워드 + Prefix 페어.
     */
    public record SearchKeyword(String keyword, String keywordPrefix) {
    }
}
