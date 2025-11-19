package com.stdev.smartmealtable.storage.db.search;

import java.util.regex.Pattern;

/**
 * 공통 검색 키워드 생성/정규화 유틸리티.
 * <p>
 * - 특수문자 제거 및 소문자 변환
 */
public final class SearchKeywordSupport {

    private static final Pattern NORMALIZE_PATTERN = Pattern.compile("[^0-9a-zA-Z가-힣]");

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

}
