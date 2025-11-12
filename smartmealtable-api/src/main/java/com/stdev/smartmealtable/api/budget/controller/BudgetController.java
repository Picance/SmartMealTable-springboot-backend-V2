package com.stdev.smartmealtable.api.budget.controller;

import com.stdev.smartmealtable.api.budget.controller.dto.DailyBudgetQueryResponse;
import com.stdev.smartmealtable.api.budget.controller.dto.MonthlyBudgetQueryResponse;
import com.stdev.smartmealtable.api.budget.controller.request.UpdateBudgetRequest;
import com.stdev.smartmealtable.api.budget.controller.request.UpdateDailyBudgetRequest;
import com.stdev.smartmealtable.api.budget.controller.response.UpdateBudgetResponse;
import com.stdev.smartmealtable.api.budget.controller.response.UpdateDailyBudgetResponse;
import com.stdev.smartmealtable.api.budget.service.DailyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.MonthlyBudgetQueryService;
import com.stdev.smartmealtable.api.budget.service.UpdateBudgetService;
import com.stdev.smartmealtable.api.budget.service.UpdateBudgetServiceRequest;
import com.stdev.smartmealtable.api.budget.service.UpdateBudgetServiceResponse;
import com.stdev.smartmealtable.api.budget.service.UpdateDailyBudgetService;
import com.stdev.smartmealtable.api.budget.service.UpdateDailyBudgetServiceResponse;
import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceRequest;
import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
    private final UpdateBudgetService updateBudgetService;
    private final UpdateDailyBudgetService updateDailyBudgetService;

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

    /**
     * 월별 예산 수정
     * - 월별 예산과 일일 예산을 수정합니다.
     * - 해당 월의 모든 일일 예산이 함께 업데이트됩니다.
     *
     * @param user    인증된 사용자
     * @param request 예산 수정 요청 정보
     * @return 수정된 예산 정보
     */
    @PutMapping
    public ApiResponse<UpdateBudgetResponse> updateBudget(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {
        UpdateBudgetServiceRequest serviceRequest = new UpdateBudgetServiceRequest(
                request.getMonthlyFoodBudget(),
                request.getDailyFoodBudget()
        );

        UpdateBudgetServiceResponse serviceResponse = updateBudgetService.updateBudget(user.memberId(), serviceRequest);
        UpdateBudgetResponse response = new UpdateBudgetResponse(serviceResponse);

        return ApiResponse.success(response);
    }

    /**
     * 일별 예산 수정
     * - 특정 날짜의 예산을 수정합니다.
     * - applyForward가 true이면 해당 날짜 이후의 모든 예산도 함께 수정됩니다.
     *
     * @param user    인증된 사용자
     * @param date    수정할 날짜 (YYYY-MM-DD 형식)
     * @param request 예산 수정 요청 정보
     * @return 수정된 예산 정보
     */
    @PutMapping("/daily/{date}")
    public ApiResponse<UpdateDailyBudgetResponse> updateDailyBudget(
            @AuthUser AuthenticatedUser user,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody UpdateDailyBudgetRequest request
    ) {
        UpdateDailyBudgetServiceResponse serviceResponse = updateDailyBudgetService.updateDailyBudget(
                user.memberId(),
                date,
                request.getDailyBudget(),
                request.getApplyForward()
        );

        UpdateDailyBudgetResponse response = new UpdateDailyBudgetResponse(serviceResponse);
        return ApiResponse.success(response);
    }
}
