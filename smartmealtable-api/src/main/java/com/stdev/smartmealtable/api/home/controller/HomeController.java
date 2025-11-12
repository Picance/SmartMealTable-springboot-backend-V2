package com.stdev.smartmealtable.api.home.controller;

import com.stdev.smartmealtable.api.home.controller.request.MonthlyBudgetConfirmRequest;
import com.stdev.smartmealtable.api.home.controller.response.MonthlyBudgetConfirmResponse;
import com.stdev.smartmealtable.api.home.controller.response.OnboardingStatusResponse;
import com.stdev.smartmealtable.api.home.service.HomeDashboardQueryService;
import com.stdev.smartmealtable.api.home.service.MonthlyBudgetConfirmService;
import com.stdev.smartmealtable.api.home.service.OnboardingStatusQueryService;
import com.stdev.smartmealtable.api.home.service.dto.HomeDashboardServiceResponse;
import com.stdev.smartmealtable.api.home.service.dto.MonthlyBudgetConfirmServiceResponse;
import com.stdev.smartmealtable.api.home.service.dto.OnboardingStatusServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 홈 화면 API Controller
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class HomeController {

    private final HomeDashboardQueryService homeDashboardQueryService;
    private final OnboardingStatusQueryService onboardingStatusQueryService;
    private final MonthlyBudgetConfirmService monthlyBudgetConfirmService;

    /**
     * 홈 대시보드 조회
     *
     * 선택적으로 사용자의 현재 위치 좌표를 전달할 수 있습니다.
     * 위치 정보가 있으면 해당 위치 기준으로 거리순 추천이 제공됩니다.
     *
     * @param user 인증된 사용자
     * @param latitude 사용자 위도 (선택사항)
     * @param longitude 사용자 경도 (선택사항)
     * @return 홈 대시보드 정보
     *
     * @example GET /api/v1/home/dashboard
     * @example GET /api/v1/home/dashboard?latitude=37.4979&longitude=127.0276
     */
    @GetMapping("/home/dashboard")
    public ApiResponse<HomeDashboardServiceResponse> getHomeDashboard(
            @AuthUser AuthenticatedUser user,
            @RequestParam(name = "latitude", required = false) Double latitude,
            @RequestParam(name = "longitude", required = false) Double longitude
    ) {
        HomeDashboardServiceResponse response;

        if (latitude != null && longitude != null) {
            response = homeDashboardQueryService.getHomeDashboardWithLocation(
                    user.memberId(),
                    latitude,
                    longitude
            );
        } else {
            response = homeDashboardQueryService.getHomeDashboard(user.memberId());
        }

        return ApiResponse.success(response);
    }

    /**
     * 온보딩 상태 조회
     *
     * @param user 인증된 사용자
     * @return 온보딩 상태 정보
     */
    @GetMapping("/members/me/onboarding-status")
    public ApiResponse<OnboardingStatusResponse> getOnboardingStatus(
            @AuthUser AuthenticatedUser user
    ) {
        OnboardingStatusServiceResponse serviceResponse = onboardingStatusQueryService.getOnboardingStatus(user.memberId());
        OnboardingStatusResponse response = OnboardingStatusResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 월별 예산 확인 처리
     *
     * @param user    인증된 사용자
     * @param request 월별 예산 확인 요청
     * @return 월별 예산 확인 처리 결과
     */
    @PostMapping("/members/me/monthly-budget-confirmed")
    public ApiResponse<MonthlyBudgetConfirmResponse> confirmMonthlyBudget(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody MonthlyBudgetConfirmRequest request
    ) {
        MonthlyBudgetConfirmServiceResponse serviceResponse = monthlyBudgetConfirmService.confirmMonthlyBudget(
                user.memberId(),
                request.year(),
                request.month(),
                request.action()
        );
        
        MonthlyBudgetConfirmResponse response = MonthlyBudgetConfirmResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }
}
