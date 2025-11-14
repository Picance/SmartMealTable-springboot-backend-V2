package com.stdev.smartmealtable.performance.loader.util;

import lombok.experimental.UtilityClass;

/**
 * Helper that centralizes how synthetic rows are labeled so we can fetch/delete them later.
 */
@UtilityClass
public class RunTag {

    private static final String PREFIX = "PERF";

    public static String memberNickname(String runId, long sequence) {
        return String.format("%s_%s_MEM_%d", PREFIX, runId, sequence);
    }

    public static String memberPattern(String runId) {
        return String.format("%s_%s_MEM_%%", PREFIX, runId);
    }

    public static String storeExternalId(String runId, long sequence) {
        return String.format("%s_%s_STORE_%d", PREFIX, runId, sequence);
    }

    public static String storePattern(String runId) {
        return String.format("%s_%s_STORE_%%", PREFIX, runId);
    }
    
    public static String categoryName(String runId, long sequence) {
        return String.format("%s_%s_CATEGORY_%d", PREFIX, runId, sequence);
    }
    
    public static String categoryPattern(String runId) {
        return String.format("%s_%s_CATEGORY_%%", PREFIX, runId);
    }

    public static String foodName(String runId, long storeId, int menuIndex) {
        return String.format("%s_%s_FOOD_%d_%d", PREFIX, runId, storeId, menuIndex);
    }

    public static String foodPattern(String runId) {
        return String.format("%s_%s_FOOD_%%", PREFIX, runId);
    }

    public static String expenditureMemo(String runId, long sequence) {
        return String.format("%s_%s_EXP_%d", PREFIX, runId, sequence);
    }

    public static String expenditurePattern(String runId) {
        return String.format("%s_%s_EXP_%%", PREFIX, runId);
    }

    /**
     * Extracts the trailing number from a tagged identifier (e.g. PERF_20250101_STORE_42 → 42).
     */
    public static long extractSequence(String taggedValue) {
        if (taggedValue == null || taggedValue.isBlank()) {
            return -1;
        }
        int lastUnderscore = taggedValue.lastIndexOf('_');
        if (lastUnderscore == -1 || lastUnderscore == taggedValue.length() - 1) {
            return -1;
        }
        try {
            return Long.parseLong(taggedValue.substring(lastUnderscore + 1));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
