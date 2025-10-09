package com.stdev.smartmealtable.core.error;

/**
 * API 에러 코드 정의
 * HTTP 상태 코드와 매핑됨
 */
public enum ErrorCode {

    // 4xx Client Errors
    E400,  // Bad Request - 잘못된 요청
    E401,  // Unauthorized - 인증 실패
    E403,  // Forbidden - 권한 없음
    E404,  // Not Found - 리소스 없음
    E409,  // Conflict - 충돌 (중복 데이터)
    E422,  // Unprocessable Entity - 유효성 검증 실패

    // 5xx Server Errors
    E500,  // Internal Server Error - 내부 서버 오류
    E503   // Service Unavailable - 서비스 이용 불가 (외부 API 오류)
}
