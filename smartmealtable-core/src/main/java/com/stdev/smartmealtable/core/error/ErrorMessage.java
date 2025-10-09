package com.stdev.smartmealtable.core.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * API 에러 메시지 구조
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorMessage(
        ErrorCode code,
        String message,
        Map<String, String> data
) {
    
    /**
     * 에러 메시지 생성 (상세 정보 없음)
     */
    public static ErrorMessage of(ErrorCode code, String message) {
        return new ErrorMessage(code, message, null);
    }
    
    /**
     * 에러 메시지 생성 (상세 정보 포함)
     */
    public static ErrorMessage of(ErrorCode code, String message, Map<String, String> data) {
        return new ErrorMessage(code, message, data);
    }
}
