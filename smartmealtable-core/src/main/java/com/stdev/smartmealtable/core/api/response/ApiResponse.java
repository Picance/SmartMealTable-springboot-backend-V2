package com.stdev.smartmealtable.core.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.stdev.smartmealtable.core.error.ErrorMessage;

/**
 * API 공통 응답 구조
 * 
 * @param <T> 응답 데이터 타입
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        ResultType result,
        T data,
        ErrorMessage error
) {
    
    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }
    
    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }
    
    /**
     * 에러 응답
     */
    public static <T> ApiResponse<T> error(ErrorMessage errorMessage) {
        return new ApiResponse<>(ResultType.ERROR, null, errorMessage);
    }
}
