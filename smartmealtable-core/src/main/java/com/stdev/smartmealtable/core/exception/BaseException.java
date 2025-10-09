package com.stdev.smartmealtable.core.exception;

import com.stdev.smartmealtable.core.error.ErrorType;

import java.util.Map;

/**
 * 비즈니스 로직 처리 중 발생하는 모든 예외의 기본 클래스
 */
public class BaseException extends RuntimeException {

    private final ErrorType errorType;
    private final Map<String, String> errorData;

    public BaseException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.errorData = null;
    }

    public BaseException(ErrorType errorType, Map<String, String> errorData) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.errorData = errorData;
    }

    public BaseException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.errorData = null;
    }

    public BaseException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errorData = null;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public Map<String, String> getErrorData() {
        return errorData;
    }
}
