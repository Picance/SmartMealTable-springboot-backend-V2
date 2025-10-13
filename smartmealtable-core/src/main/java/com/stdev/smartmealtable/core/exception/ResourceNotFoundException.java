package com.stdev.smartmealtable.core.exception;

import com.stdev.smartmealtable.core.error.ErrorType;

import java.util.Map;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외 (404)
 * 회원, 가게, 지출내역, 예산 등 엔티티를 찾지 못했을 때 사용
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(ErrorType errorType) {
        super(errorType);
    }

    public ResourceNotFoundException(ErrorType errorType, Map<String, String> errorData) {
        super(errorType, errorData);
    }

    public ResourceNotFoundException(ErrorType errorType, String message) {
        super(errorType, message);
    }

    public ResourceNotFoundException(ErrorType errorType, String message, Throwable cause) {
        super(errorType, message, cause);
    }
}
