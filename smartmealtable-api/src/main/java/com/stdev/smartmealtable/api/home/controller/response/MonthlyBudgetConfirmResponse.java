package com.stdev.smartmealtable.api.home.controller.response;

import com.stdev.smartmealtable.api.home.service.dto.MonthlyBudgetConfirmServiceResponse;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 월별 예산 확인 처리 응답 DTO (Controller Layer)
 */
public record MonthlyBudgetConfirmResponse(
        Integer year,
        Integer month,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        LocalDateTime confirmedAt,
        
        Integer monthlyBudget
) {
    
    /**
     * Service Response를 Controller Response로 변환
     */
    public static MonthlyBudgetConfirmResponse from(MonthlyBudgetConfirmServiceResponse serviceResponse) {
        return new MonthlyBudgetConfirmResponse(
                serviceResponse.year(),
                serviceResponse.month(),
                serviceResponse.confirmedAt(),
                serviceResponse.monthlyBudget()
        );
    }
}
