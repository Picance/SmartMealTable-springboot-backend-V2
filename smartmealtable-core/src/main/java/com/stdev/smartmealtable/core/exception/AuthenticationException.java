package com.stdev.smartmealtable.core.exception;

import com.stdev.smartmealtable.core.error.ErrorType;

/**
 * 인증 관련 예외
 * 401 Unauthorized 에러에 사용
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(ErrorType errorType) {
        super(errorType);
    }

    public AuthenticationException(ErrorType errorType, String message) {
        super(errorType, message);
    }

    public AuthenticationException(ErrorType errorType, String message, Throwable cause) {
        super(errorType, message, cause);
    }
}
