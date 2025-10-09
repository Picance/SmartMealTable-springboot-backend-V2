package com.stdev.smartmealtable.core.exception;

import com.stdev.smartmealtable.core.error.ErrorType;

/**
 * 권한 관련 예외
 * 403 Forbidden 에러에 사용
 */
public class AuthorizationException extends BaseException {

    public AuthorizationException(ErrorType errorType) {
        super(errorType);
    }

    public AuthorizationException(ErrorType errorType, String message) {
        super(errorType, message);
    }
}
