package com.stdev.smartmealtable.core.exception;

import com.stdev.smartmealtable.core.error.ErrorType;

/**
 * 외부 API 호출 실패 예외
 * 503 Service Unavailable 에러에 사용
 */
public class ExternalServiceException extends BaseException {

    public ExternalServiceException(ErrorType errorType) {
        super(errorType);
    }

    public ExternalServiceException(ErrorType errorType, String message, Throwable cause) {
        super(errorType, message, cause);
    }
}
