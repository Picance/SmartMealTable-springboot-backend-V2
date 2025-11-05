package com.stdev.smartmealtable.admin.common.exception;

import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.error.ErrorCode;
import com.stdev.smartmealtable.core.error.ErrorMessage;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BaseException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * ADMIN 전역 예외 처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * BaseException 처리 (BusinessException 등)
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        log.warn("BaseException: {}", ex.getMessage(), ex);
        
        // ErrorType 이름을 에러 코드로 사용
        ErrorMessage errorMessage = new ErrorMessage(
                ex.getErrorType().name(),  // POLICY_NOT_FOUND, DUPLICATE_POLICY_TITLE 등
                ex.getMessage() != null ? ex.getMessage() : ex.getErrorType().getMessage(),
                ex.getErrorData()
        );
        
        HttpStatus status = determineHttpStatus(ex.getErrorType());
        
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(errorMessage));
    }
    
    /**
     * Validation 예외 처리 (422) - Request Body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        
        FieldError fieldError = ex.getBindingResult().getFieldError();
        
        String message = "입력값 검증에 실패했습니다.";
        Map<String, String> errorData = new HashMap<>();
        
        if (fieldError != null) {
            errorData.put("field", fieldError.getField());
            errorData.put("reason", fieldError.getDefaultMessage());
            message = fieldError.getDefaultMessage();
        }
        
        ErrorMessage errorMessage = new ErrorMessage(
                ErrorCode.E422,
                message,
                errorData
        );
        
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(errorMessage));
    }
    
    /**
     * Constraint Violation 예외 처리 (400) - Query Parameters
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint Violation: {}", ex.getMessage());
        
        String message = "요청 파라미터가 유효하지 않습니다.";
        
        ConstraintViolation<?> violation = ex.getConstraintViolations().iterator().next();
        if (violation != null) {
            message = violation.getMessage();
        }
        
        ErrorMessage errorMessage = ErrorMessage.of(
                ErrorCode.E400,
                message
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }
    
    /**
     * 필수 Request Parameter 누락 처리 (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex
    ) {
        log.warn("Missing request parameter: {}", ex.getMessage());
        
        String message = String.format("필수 파라미터가 누락되었습니다: %s", ex.getParameterName());
        
        ErrorMessage errorMessage = ErrorMessage.of(
                ErrorCode.E400,
                message
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }
    
    /**
     * IllegalArgumentException 처리 (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage(), ex);
        
        ErrorMessage errorMessage = ErrorMessage.of(
                ErrorCode.E400,
                ex.getMessage()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }
    
    /**
     * 리소스를 찾을 수 없음 처리 (404)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorMessage errorMessage = ErrorMessage.of(
                ErrorCode.E404,
                "요청하신 리소스를 찾을 수 없습니다."
        );
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(errorMessage));
    }
    
    /**
     * 기타 예외 처리 (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorMessage errorMessage = ErrorMessage.of(
                ErrorCode.E500,
                "서버 내부 오류가 발생했습니다."
        );
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorMessage));
    }
    
    /**
     * ErrorType에 따른 HTTP Status 결정
     */
    private HttpStatus determineHttpStatus(ErrorType errorType) {
        ErrorCode code = errorType.getErrorCode();
        
        return switch (code) {
            case E400 -> HttpStatus.BAD_REQUEST;
            case E401 -> HttpStatus.UNAUTHORIZED;
            case E403 -> HttpStatus.FORBIDDEN;
            case E404 -> HttpStatus.NOT_FOUND;
            case E409 -> HttpStatus.CONFLICT;
            case E422 -> HttpStatus.UNPROCESSABLE_ENTITY;
            case E503 -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
