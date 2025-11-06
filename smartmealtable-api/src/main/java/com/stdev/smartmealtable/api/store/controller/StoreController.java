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
     * <p>커서 기반 페이징 및 오프셋 기반 페이징을 모두 지원합니다.</p>
     * <ul>
     *   <li><strong>커서 기반 (무한 스크롤)</strong>: lastId + limit 사용
     *     <ul>
     *       <li>첫 요청: lastId 생략</li>
     *       <li>다음 페이지: 이전 응답의 lastId 사용</li>
     *       <li>응답에 hasMore=false면 더 이상 데이터 없음</li>
     *     </ul>
     *   </li>
     *   <li><strong>오프셋 기반 (기존 방식)</strong>: page + size 사용 (하위 호환성)</li>
     * </ul>
     *
     * @param user 인증된 사용자 (기본 주소 조회용)
     * @param keyword 검색어 (가게명, 카테고리명)
     * @param radius 검색 반경 (km, 기본값: 3.0)
     * @param categoryId 카테고리 ID 필터
     * @param isOpen 영업 중인 가게만 조회 여부
     * @param storeType 가게 유형 필터 (STUDENT_CAFETERIA, GENERAL_RESTAURANT)
     * @param sortBy 정렬 기준 (distance, reviewCount, viewCount, averagePrice)
     * @param lastId 커서 (이전 응답의 마지막 가게 ID, null이면 처음부터 조회)
     * @param limit 커서 조회 개수 (기본값: 20, 1-100)
     * @param page 페이지 번호 (0부터 시작, 기본값: 0) - 오프셋 모드용
     * @param size 페이지 크기 (기본값: 20) - 오프셋 모드용
     * @return 가게 목록 및 페이징 정보 (hasMore, lastId 포함)
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
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size
    ) {
        log.info("가게 목록 조회 API 호출 - memberId: {}, keyword: {}, radius: {}, categoryId: {}, isOpen: {}, storeType: {}, sortBy: {}, lastId: {}, limit: {}, page: {}, size: {}",
                user.memberId(), keyword, radius, categoryId, isOpen, storeType, sortBy, lastId, limit, page, size);
        
        StoreListRequest request = new StoreListRequest(
                keyword,
                radius,
                categoryId,
                isOpen,
                storeType,
                sortBy,
                lastId,
                limit,
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
     * @return 가게 상세 정보 (영업시간, 임시 휴무, 이미지, 메뉴 등 포함)
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
     * 가게별 메뉴 목록 조회
     * GET /api/v1/stores/{storeId}/foods
     *
     * @param storeId 가게 ID
     * @param sort 정렬 기준 (displayOrder,asc/desc, price,asc/desc, registeredDt,desc, isMain,desc)
     * @return 가게의 메뉴 목록
     */
    @GetMapping("/{storeId}/foods")
    public ApiResponse<com.stdev.smartmealtable.api.store.dto.GetStoreFoodsResponse> getStoreFoods(
            @PathVariable Long storeId,
            @RequestParam(required = false, defaultValue = "displayOrder,asc") String sort
    ) {
        log.info("가게별 메뉴 목록 조회 API 호출 - storeId: {}, sort: {}", storeId, sort);
        
        var response = storeService.getStoreFoods(storeId, sort);
        
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
