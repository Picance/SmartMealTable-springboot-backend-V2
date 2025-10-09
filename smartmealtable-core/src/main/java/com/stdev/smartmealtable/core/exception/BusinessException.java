package com.stdev.smartmealtable.core.exception;

import com.stdev.smartmealtable.core.error.ErrorType;

import java.util.Map;

/**
 * 일반적인 비즈니스 로직 예외
 * 404, 409, 422 등에 사용
 */
public class BusinessException extends BaseException {

    public BusinessException(ErrorType errorType) {
        super(errorType);
    }

    public BusinessException(ErrorType errorType, Map<String, String> errorData) {
        super(errorType, errorData);
    }

    public BusinessException(ErrorType errorType, String message) {
        super(errorType, message);
    }

    public BusinessException(ErrorType errorType, String message, Throwable cause) {
        super(errorType, message, cause);
    }
}
