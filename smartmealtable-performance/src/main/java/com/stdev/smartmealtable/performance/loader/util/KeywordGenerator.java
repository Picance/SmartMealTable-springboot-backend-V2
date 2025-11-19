package com.stdev.smartmealtable.performance.loader.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Generates normalized search keywords for substring matching used in performance data.
 */
public final class KeywordGenerator {

    private static final Pattern NORMALIZE_PATTERN = Pattern.compile("[^0-9a-zA-Z가-힣]");
    private static final int MIN_SUBSTRING_LENGTH = 2;
    private static final int MAX_SUBSTRING_LENGTH = 8;
    private static final int MAX_KEYWORDS_PER_TEXT = 800;
    private static final int MAX_KEYWORDS_PER_PREFIX = 500;
    private static final int KEYWORD_PREFIX_LENGTH = 5;

    private KeywordGenerator() {
    }

    public static List<Keyword> generate(String source) {
        String normalized = normalize(source);
        if (normalized.isEmpty()) {
            return List.of();
        }

        Set<String> substrings = new LinkedHashSet<>();
        java.util.Map<String, Integer> prefixCounts = new java.util.HashMap<>();
        outer:
        for (int start = 0; start < normalized.length(); start++) {
            for (int end = start + 1; end <= normalized.length(); end++) {
                int length = end - start;
                if (length < MIN_SUBSTRING_LENGTH) {
                    continue;
                }
                if (length > MAX_SUBSTRING_LENGTH) {
                    break;
                }
                String candidate = normalized.substring(start, end);
                String prefix = prefix(candidate);
                int count = prefixCounts.getOrDefault(prefix, 0);
                if (count >= MAX_KEYWORDS_PER_PREFIX) {
                    continue;
                }
                substrings.add(candidate);
                prefixCounts.put(prefix, count + 1);
                if (substrings.size() >= MAX_KEYWORDS_PER_TEXT) {
                    break outer;
                }
            }
        }

        return substrings.stream()
                .map(text -> new Keyword(text, prefix(text)))
                .toList();
    }

    private static String normalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String lowered = text.toLowerCase();
        return NORMALIZE_PATTERN.matcher(lowered).replaceAll("");
    }

    private static String prefix(String keyword) {
        if (keyword.isEmpty()) {
            return keyword;
        }
        int end = Math.min(KEYWORD_PREFIX_LENGTH, keyword.length());
        return keyword.substring(0, end);
    }

    public record Keyword(String keyword, String prefix) {
    }
}
