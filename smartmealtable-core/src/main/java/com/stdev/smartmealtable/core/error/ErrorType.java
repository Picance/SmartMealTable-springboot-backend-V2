package com.stdev.smartmealtable.core.error;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 에러 타입 정의
 * 각 에러는 HTTP 상태, 에러 코드, 메시지, 로그 레벨을 가짐
 */
public enum ErrorType {

    // ==================== 인증 관련 에러 (401) ====================
    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.E401,
            "유효하지 않은 토큰입니다.",
            LogLevel.WARN
    ),
    
    EXPIRED_TOKEN(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.E401,
            "만료된 토큰입니다.",
            LogLevel.WARN
    ),
    
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.E401,
            "이메일 또는 비밀번호가 일치하지 않습니다.",
            LogLevel.WARN
    ),
    
    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.E401,
            "유효하지 않은 Refresh Token입니다.",
            LogLevel.WARN
    ),
    
    INVALID_CURRENT_PASSWORD(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.E401,
            "현재 비밀번호가 일치하지 않습니다.",
            LogLevel.WARN
    ),
    
    EXPIRED_AUTHORIZATION_CODE(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.E401,
            "소셜 로그인 인증 코드가 만료되었습니다.",
            LogLevel.WARN
    ),

    // ==================== 권한 관련 에러 (403) ====================
    ACCOUNT_LOCKED(
            HttpStatus.FORBIDDEN,
            ErrorCode.E403,
            "계정이 잠겼습니다. 비밀번호를 5회 이상 틀렸습니다.",
            LogLevel.WARN
    ),
    
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            ErrorCode.E403,
            "접근 권한이 없습니다.",
            LogLevel.WARN
    ),

    // ==================== 리소스 없음 (404) ====================
    MEMBER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 회원입니다.",
            LogLevel.WARN
    ),
    
    EMAIL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 이메일입니다.",
            LogLevel.WARN
    ),
    
    GROUP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 그룹입니다.",
            LogLevel.WARN
    ),
    
    POLICY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 약관입니다.",
            LogLevel.WARN
    ),
    
    CATEGORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 카테고리입니다.",
            LogLevel.WARN
    ),
    
    FOOD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 음식입니다.",
            LogLevel.WARN
    ),
    
    STORE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 가게입니다.",
            LogLevel.WARN
    ),
    
    EXPENDITURE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 지출 내역입니다.",
            LogLevel.WARN
    ),
    
    MONTHLY_BUDGET_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "해당 월의 예산 정보를 찾을 수 없습니다.",
            LogLevel.WARN
    ),
    
    DAILY_BUDGET_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "해당 날짜의 예산 정보를 찾을 수 없습니다.",
            LogLevel.WARN
    ),
    
    ADDRESS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            ErrorCode.E404,
            "존재하지 않는 주소입니다.",
            LogLevel.WARN
    ),

    // ==================== 충돌 (409) ====================
    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            ErrorCode.E409,
            "이미 사용 중인 이메일입니다.",
            LogLevel.WARN
    ),
    
    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            ErrorCode.E409,
            "이미 사용 중인 닉네임입니다.",
            LogLevel.WARN
    ),
    
    DUPLICATE_SOCIAL_ACCOUNT(
            HttpStatus.CONFLICT,
            ErrorCode.E409,
            "이미 연동된 소셜 계정입니다.",
            LogLevel.WARN
    ),
    
    LAST_LOGIN_METHOD(
            HttpStatus.CONFLICT,
            ErrorCode.E409,
            "유일한 로그인 수단입니다. 연동 해제하려면 먼저 비밀번호를 설정해주세요.",
            LogLevel.WARN
    ),
    
    CANNOT_DELETE_LAST_PRIMARY_ADDRESS(
            HttpStatus.CONFLICT,
            ErrorCode.E409,
            "기본 주소는 다른 주소가 있을 때만 삭제할 수 있습니다.",
            LogLevel.WARN
    ),
    
    FORBIDDEN_ACCESS(
            HttpStatus.FORBIDDEN,
            ErrorCode.E403,
            "다른 사용자의 리소스에 접근할 수 없습니다.",
            LogLevel.WARN
    ),

    // ==================== 유효성 검증 실패 (422) ====================
    INVALID_INPUT(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ErrorCode.E422,
            "입력 값이 유효하지 않습니다.",
            LogLevel.WARN
    ),
    
    INVALID_PASSWORD_FORMAT(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ErrorCode.E422,
            "비밀번호는 8-20자, 영문/숫자/특수문자 조합이어야 합니다.",
            LogLevel.WARN
    ),
    
    INVALID_EMAIL_FORMAT(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ErrorCode.E422,
            "이메일 형식이 올바르지 않습니다.",
            LogLevel.WARN
    ),
    
    INVALID_BUDGET(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ErrorCode.E422,
            "예산 설정이 유효하지 않습니다. 각 식사 예산의 합계는 일일 예산과 일치해야 합니다.",
            LogLevel.WARN
    ),
    
    REQUIRED_POLICY_NOT_AGREED(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ErrorCode.E422,
            "필수 약관에 동의해야 합니다.",
            LogLevel.WARN
    ),
    
    SMS_PARSING_FAILED(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ErrorCode.E422,
            "SMS 메시지 파싱에 실패했습니다. 지원하지 않는 형식입니다.",
            LogLevel.WARN
    ),

    // ==================== 잘못된 요청 (400) ====================
    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E400,
            "잘못된 요청입니다.",
            LogLevel.WARN
    ),
    
    INVALID_DATE_FORMAT(
            HttpStatus.BAD_REQUEST,
            ErrorCode.E400,
            "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)",
            LogLevel.WARN
    ),

    // ==================== 서버 오류 (500) ====================
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.E500,
            "서버 내부 오류가 발생했습니다.",
            LogLevel.ERROR
    ),

    // ==================== 서비스 이용 불가 (503) ====================
    EXTERNAL_API_ERROR(
            HttpStatus.SERVICE_UNAVAILABLE,
            ErrorCode.E503,
            "외부 API 호출에 실패했습니다. 잠시 후 다시 시도해주세요.",
            LogLevel.ERROR
    ),
    
    SOCIAL_LOGIN_SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            ErrorCode.E503,
            "소셜 로그인 서비스를 일시적으로 이용할 수 없습니다.",
            LogLevel.ERROR
    );

    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;
    private final String message;
    private final LogLevel logLevel;

    ErrorType(HttpStatus httpStatus, ErrorCode errorCode, String message, LogLevel logLevel) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
        this.logLevel = logLevel;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }
}
