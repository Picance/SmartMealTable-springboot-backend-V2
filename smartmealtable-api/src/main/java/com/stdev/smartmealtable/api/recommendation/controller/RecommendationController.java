package com.stdev.smartmealtable.api.recommendation.controller;

import com.stcom.smartmealtable.recommendation.domain.model.RecommendationResult;
import com.stcom.smartmealtable.recommendation.domain.model.ScoreDetail;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.api.recommendation.dto.RecommendationRequestDto;
import com.stdev.smartmealtable.api.recommendation.dto.RecommendationResponseDto;
import com.stdev.smartmealtable.api.recommendation.dto.ScoreDetailResponseDto;
import com.stdev.smartmealtable.api.recommendation.dto.UpdateRecommendationTypeRequestDto;
import com.stdev.smartmealtable.api.recommendation.dto.UpdateRecommendationTypeResponseDto;
import com.stdev.smartmealtable.api.recommendation.service.RecommendationApplicationService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.domain.member.entity.Member;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
@Validated  // 메서드 파라미터 validation 활성화
public class RecommendationController {

    private final RecommendationApplicationService recommendationApplicationService;

    /**
     * 추천 목록 조회
     * 
     * <p>사용자 프로필과 현재 위치를 기반으로 개인화된 가게 추천을 제공합니다.</p>
     * 
     * <p>커서 기반 페이징 (무한 스크롤) 또는 오프셋 기반 페이징을 지원합니다:
     * <ul>
     *   <li>커서 기반: lastId, limit 파라미터 사용 (권장)</li>
     *   <li>오프셋 기반: page, size 파라미터 사용 (하위 호환성)</li>
     * </ul>
     * </p>
     * 
     * @param authenticatedUser 인증된 사용자
     * @param latitude 현재 위도 (필수)
     * @param longitude 현재 경도 (필수)
     * @param radius 검색 반경 (km, 기본값: 0.5)
     * @param sortBy 정렬 기준 (기본값: SCORE)
     * @param includeDisliked 불호 음식 포함 여부 (기본값: false)
     * @param openNow 영업 중인 가게만 조회 (기본값: false)
     * @param storeType 가게 타입 필터 (기본값: ALL)
     * @param lastId 마지막 항목의 ID (커서 기반 페이징용)
     * @param limit 조회할 항목 수 (커서 기반 페이징용, 기본값: 20)
     * @param page 페이지 번호 (오프셋 기반 페이징용, 기본값: 0)
     * @param size 페이지 크기 (오프셋 기반 페이징용, 기본값: 20)
     * @return 추천 결과 목록
     */
    @GetMapping
    public ApiResponse<List<RecommendationResponseDto>> getRecommendations(
            @AuthUser AuthenticatedUser authenticatedUser,
            @RequestParam(required = true)
            @NotNull(message = "위도는 필수입니다")
            @DecimalMin(value = "-90.0", message = "위도는 -90 ~ 90 범위여야 합니다")
            @DecimalMax(value = "90.0", message = "위도는 -90 ~ 90 범위여야 합니다")
            BigDecimal latitude,
            @RequestParam(required = true)
            @NotNull(message = "경도는 필수입니다")
            @DecimalMin(value = "-180.0", message = "경도는 -180 ~ 180 범위여야 합니다")
            @DecimalMax(value = "180.0", message = "경도는 -180 ~ 180 범위여야 합니다")
            BigDecimal longitude,
            @RequestParam(required = false, defaultValue = "0.5")
            @DecimalMin(value = "0.1", message = "반경은 0.1km 이상이어야 합니다")
            @DecimalMax(value = "10.0", message = "반경은 10km 이하여야 합니다")
            Double radius,
            @RequestParam(required = false, defaultValue = "SCORE") RecommendationRequestDto.SortBy sortBy,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDisliked,
            @RequestParam(required = false, defaultValue = "false") Boolean openNow,
            @RequestParam(required = false, defaultValue = "ALL") RecommendationRequestDto.StoreTypeFilter storeType,
            // 커서 기반 페이징 파라미터
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "20")
            @Min(value = 1, message = "조회 항목 수는 1 이상이어야 합니다")
            @Max(value = 100, message = "조회 항목 수는 100 이하여야 합니다")
            Integer limit,
            // 오프셋 기반 페이징 파라미터 (하위 호환성)
            @RequestParam(required = false)
            @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
            Integer page,
            @RequestParam(required = false)
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
            Integer size
    ) {
        log.info("추천 목록 조회 API 호출 - memberId: {}, lat: {}, lng: {}, radius: {}, cursor: {}, limit: {}, page: {}, size: {}", 
                authenticatedUser.memberId(), latitude, longitude, radius, lastId, limit, page, size);

        // DTO 생성
        RecommendationRequestDto request = RecommendationRequestDto.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radius(radius)
                .sortBy(sortBy)
                .includeDisliked(includeDisliked)
                .openNow(openNow)
                .storeType(storeType)
                .lastId(lastId)
                .limit(limit)
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .build();

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
            @AuthUser AuthenticatedUser authenticatedUser,
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
            @AuthUser AuthenticatedUser authenticatedUser,
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
