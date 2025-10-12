package com.stdev.smartmealtable.api.store.controller;

import com.stdev.smartmealtable.api.store.dto.StoreAutocompleteResponse;
import com.stdev.smartmealtable.api.store.dto.StoreDetailResponse;
import com.stdev.smartmealtable.api.store.dto.StoreListRequest;
import com.stdev.smartmealtable.api.store.dto.StoreListResponse;
import com.stdev.smartmealtable.api.store.service.StoreService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 가게 관리 API Controller
 * - 가게 목록 조회 (위치 기반 필터링, 정렬)
 * - 가게 상세 조회 (조회 이력 기록, 조회수 증가)
 * - 가게 자동완성 검색
 */
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StoreController {
    
    private final StoreService storeService;
    
    /**
     * 가게 목록 조회
     * GET /api/v1/stores
     *
     * @param user 인증된 사용자 (기본 주소 조회용)
     * @param keyword 검색어 (가게명, 카테고리명)
     * @param radius 검색 반경 (km, 기본값: 3.0)
     * @param categoryId 카테고리 ID 필터
     * @param isOpen 영업 중인 가게만 조회 여부
     * @param storeType 가게 유형 필터 (STUDENT_CAFETERIA, GENERAL_RESTAURANT)
     * @param sortBy 정렬 기준 (distance, reviewCount, viewCount, averagePrice)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 가게 목록 및 페이징 정보
     */
    @GetMapping
    public ApiResponse<StoreListResponse> getStores(
            @AuthUser AuthenticatedUser user,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Min(0) @Max(50) Double radius,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isOpen,
            @RequestParam(required = false) StoreType storeType,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size
    ) {
        log.info("가게 목록 조회 API 호출 - memberId: {}, keyword: {}, radius: {}, categoryId: {}, isOpen: {}, storeType: {}, sortBy: {}, page: {}, size: {}",
                user.memberId(), keyword, radius, categoryId, isOpen, storeType, sortBy, page, size);
        
        StoreListRequest request = new StoreListRequest(
                keyword,
                radius,
                categoryId,
                isOpen,
                storeType,
                sortBy,
                page,
                size
        );
        
        StoreListResponse response = storeService.getStores(user.memberId(), request);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 가게 상세 조회
     * GET /api/v1/stores/{storeId}
     *
     * @param user 인증된 사용자
     * @param storeId 조회할 가게 ID
     * @return 가게 상세 정보 (영업시간, 임시 휴무 등 포함)
     */
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponse> getStoreDetail(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long storeId
    ) {
        log.info("가게 상세 조회 API 호출 - memberId: {}, storeId: {}", user.memberId(), storeId);
        
        StoreDetailResponse response = storeService.getStoreDetail(user.memberId(), storeId);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 가게 자동완성 검색
     * GET /api/v1/stores/autocomplete
     *
     * @param keyword 검색어
     * @param limit 조회 개수 (기본값: 10)
     * @return 자동완성 검색 결과 목록
     */
    @GetMapping("/autocomplete")
    public ApiResponse<StoreAutocompleteListResponse> autocomplete(
            @RequestParam String keyword,
            @RequestParam(required = false) @Min(1) @Max(20) Integer limit
    ) {
        log.info("가게 자동완성 검색 API 호출 - keyword: {}, limit: {}", keyword, limit);
        
        List<StoreAutocompleteResponse> stores = storeService.autocomplete(keyword, limit);
        
        return ApiResponse.success(new StoreAutocompleteListResponse(stores));
    }
    
    /**
     * 자동완성 검색 응답 Wrapper
     */
    public record StoreAutocompleteListResponse(
            List<StoreAutocompleteResponse> stores
    ) {
    }
}
