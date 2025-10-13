package com.stdev.smartmealtable.core.response;

import lombok.Getter;

/**
 * API 응답 공통 포맷
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
public class ApiResponse<T> {
    
    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;
    
    private ApiResponse(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }
    
    /**
     * 성공 응답 생성 (메시지 포함)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }
    
    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, null, message, errorCode);
    }
}
