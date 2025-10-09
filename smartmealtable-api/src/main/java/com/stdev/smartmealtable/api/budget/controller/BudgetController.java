package com.stdev.smartmealtable.api.budget.controller;

import com.stdev.smartmealtable.api.budget.controller.dto.DailyBudgetQueryResponse;
import com.stdev.smartmealtable.api.budget.controller.dto.MonthlyBudgetQueryResponse;
import com.stdev.smartmealtable.api.budget.service.DailyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.MonthlyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceRequest;
import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

/**
 * 예산 관리 API Controller
 */
@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Validated
public class BudgetController {

    private final MonthlyBudgetQueryService monthlyBudgetQueryService;
    private final DailyBudgetQueryService dailyBudgetQueryService;

    /**
     * 월별 예산 조회
     *
     * @param user  인증된 사용자
     * @param year  조회할 연도
     * @param month 조회할 월 (1-12)
     * @return 월별 예산 정보
     */
    @GetMapping("/monthly")
    public ApiResponse<MonthlyBudgetQueryResponse> getMonthlyBudget(
            @AuthUser AuthenticatedUser user,
            @RequestParam @Min(2000) @Max(2100) Integer year,
            @RequestParam @Min(1) @Max(12) Integer month
    ) {
        MonthlyBudgetQueryServiceRequest request = new MonthlyBudgetQueryServiceRequest(
                user.memberId(),
                year,
                month
        );

        MonthlyBudgetQueryServiceResponse serviceResponse = monthlyBudgetQueryService.getMonthlyBudget(request);
        MonthlyBudgetQueryResponse response = MonthlyBudgetQueryResponse.from(serviceResponse);

        return ApiResponse.success(response);
    }

    /**
     * 일별 예산 조회
     *
     * @param user 인증된 사용자
     * @param date 조회할 날짜 (YYYY-MM-DD 형식)
     * @return 일별 예산 정보 (끼니별 포함)
     */
    @GetMapping("/daily")
    public ApiResponse<DailyBudgetQueryResponse> getDailyBudget(
            @AuthUser AuthenticatedUser user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyBudgetQueryServiceResponse serviceResponse = dailyBudgetQueryService.getDailyBudget(
                user.memberId(),
                date
        );
        DailyBudgetQueryResponse response = DailyBudgetQueryResponse.from(serviceResponse);

        return ApiResponse.success(response);
    }
}
