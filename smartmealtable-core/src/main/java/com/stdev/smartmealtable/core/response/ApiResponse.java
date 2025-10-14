package com.stdev.smartmealtable.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * API 응답 공통 포맷
 * 
 * <p>API 스펙에 따른 표준 응답 구조:</p>
 * <pre>
 * {
 *   "result": "SUCCESS" | "ERROR",
 *   "data": { ... },       // 성공 시
 *   "error": {             // 실패 시
 *     "code": "E400",
 *     "message": "..."
 *   }
 * }
 * </pre>
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final String result;
    private final T data;
    private final ErrorResponse error;
    
    private ApiResponse(String result, T data, ErrorResponse error) {
        this.result = result;
        this.data = data;
        this.error = error;
    }
    
    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", data, null);
    }
    
    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>("ERROR", null, new ErrorResponse(code, message));
    }
    
    /**
     * 에러 응답 내부 객체
     */
    @Getter
    public static class ErrorResponse {
        private final String code;
        private final String message;
        
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
