package com.stdev.smartmealtable.admin.statistics.controller;

import com.stdev.smartmealtable.admin.statistics.dto.response.ExpenditureStatisticsResponse;
import com.stdev.smartmealtable.admin.statistics.dto.response.StoreStatisticsResponse;
import com.stdev.smartmealtable.admin.statistics.dto.response.UserStatisticsResponse;
import com.stdev.smartmealtable.admin.statistics.service.StatisticsApplicationService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통계 조회 Controller
 */
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsApplicationService statisticsApplicationService;

    /**
     * 사용자 통계 조회
     *
     * @return 사용자 통계 정보
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics() {
        var statistics = statisticsApplicationService.getUserStatistics();
        var response = UserStatisticsResponse.from(statistics);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 지출 통계 조회
     *
     * @return 지출 통계 정보
     */
    @GetMapping("/expenditures")
    public ResponseEntity<ApiResponse<ExpenditureStatisticsResponse>> getExpenditureStatistics() {
        var statistics = statisticsApplicationService.getExpenditureStatistics();
        var response = ExpenditureStatisticsResponse.from(statistics);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 음식점 통계 조회
     *
     * @return 음식점 통계 정보
     */
    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<StoreStatisticsResponse>> getStoreStatistics() {
        var statistics = statisticsApplicationService.getStoreStatistics();
        var response = StoreStatisticsResponse.from(statistics);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
