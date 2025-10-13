package com.stdev.smartmealtable.api.recommendation.controller;

import com.stcom.smartmealtable.recommendation.domain.model.RecommendationResult;
import com.stcom.smartmealtable.recommendation.domain.model.ScoreDetail;
import com.stdev.smartmealtable.api.common.auth.AuthenticatedUser;
import com.stdev.smartmealtable.api.recommendation.dto.RecommendationRequestDto;
import com.stdev.smartmealtable.api.recommendation.dto.RecommendationResponseDto;
import com.stdev.smartmealtable.api.recommendation.dto.ScoreDetailResponseDto;
import com.stdev.smartmealtable.api.recommendation.dto.UpdateRecommendationTypeRequestDto;
import com.stdev.smartmealtable.api.recommendation.dto.UpdateRecommendationTypeResponseDto;
import com.stdev.smartmealtable.api.recommendation.service.RecommendationApplicationService;
import com.stdev.smartmealtable.core.response.ApiResponse;
import com.stdev.smartmealtable.domain.member.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 추천 시스템 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationApplicationService recommendationApplicationService;

    /**
     * 추천 목록 조회
     * 
     * <p>사용자 프로필과 현재 위치를 기반으로 개인화된 가게 추천을 제공합니다.</p>
     * 
     * @param authenticatedUser 인증된 사용자
     * @param request 추천 요청
     * @return 추천 결과 목록
     */
    @GetMapping
    public ApiResponse<List<RecommendationResponseDto>> getRecommendations(
            AuthenticatedUser authenticatedUser,
            @Valid @ModelAttribute RecommendationRequestDto request
    ) {
        log.info("추천 목록 조회 API 호출 - memberId: {}, request: {}", 
                authenticatedUser.memberId(), request);

        List<RecommendationResult> results = recommendationApplicationService.getRecommendations(
                authenticatedUser.memberId(),
                request
        );

        List<RecommendationResponseDto> response = RecommendationResponseDto.fromList(results);

        return ApiResponse.success(response);
    }

    /**
     * 점수 상세 조회
     * 
     * <p>특정 가게에 대한 추천 점수의 상세 정보를 제공합니다.</p>
     * 
     * @param authenticatedUser 인증된 사용자
     * @param storeId 가게 ID
     * @param latitude 현재 위도 (선택)
     * @param longitude 현재 경도 (선택)
     * @return 점수 상세 정보
     */
    @GetMapping("/{storeId}/score-detail")
    public ApiResponse<ScoreDetailResponseDto> getScoreDetail(
            AuthenticatedUser authenticatedUser,
            @PathVariable Long storeId,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude
    ) {
        log.info("점수 상세 조회 API 호출 - memberId: {}, storeId: {}, lat: {}, lng: {}", 
                authenticatedUser.memberId(), storeId, latitude, longitude);

        ScoreDetailResponseDto response = recommendationApplicationService.getScoreDetailResponse(
                authenticatedUser.memberId(),
                storeId,
                latitude,
                longitude
        );
        
        return ApiResponse.success(response);
    }

    /**
     * 추천 유형 변경
     * 
     * <p>사용자의 추천 유형을 변경합니다 (절약형, 모험형, 균형형).</p>
     * 
     * @param authenticatedUser 인증된 사용자
     * @param request 추천 유형 변경 요청
     * @return 변경 결과
     */
    @PutMapping("/type")
    public ApiResponse<UpdateRecommendationTypeResponseDto> updateRecommendationType(
            AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateRecommendationTypeRequestDto request
    ) {
        log.info("추천 유형 변경 API 호출 - memberId: {}, type: {}", 
                authenticatedUser.memberId(), request.getRecommendationType());

        Member updatedMember = recommendationApplicationService.updateRecommendationType(
                authenticatedUser.memberId(),
                request.getRecommendationType()
        );

        UpdateRecommendationTypeResponseDto response = UpdateRecommendationTypeResponseDto.from(updatedMember);
        
        return ApiResponse.success(response);
    }
}
