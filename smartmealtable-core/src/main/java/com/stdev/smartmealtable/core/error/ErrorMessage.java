package com.stdev.smartmealtable.core.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * API 에러 메시지 구조
 * 
 * <p>code 필드는 ErrorCode enum 또는 String으로 표현될 수 있습니다.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorMessage(
        @JsonProperty("code") Object code,  // ErrorCode enum 또는 String
        String message,
        Map<String, String> data
) {
    
    /**
     * ErrorCode enum을 사용하는 생성자
     */
    public ErrorMessage(ErrorCode errorCode, String message, Map<String, String> data) {
        this(errorCode.name(), message, data);
    }
    
    /**
     * String 코드를 사용하는 생성자
     */
    public ErrorMessage(String code, String message, Map<String, String> data) {
        this((Object) code, message, data);
    }
    
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
    
    /**
     * 에러 메시지 생성 (String 코드, 상세 정보 없음)
     */
    public static ErrorMessage of(String code, String message) {
        return new ErrorMessage(code, message, null);
    }
    
    /**
     * 에러 메시지 생성 (String 코드, 상세 정보 포함)
     */
    public static ErrorMessage of(String code, String message, Map<String, String> data) {
        return new ErrorMessage(code, message, data);
    }
}
