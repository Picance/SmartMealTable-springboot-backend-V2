package com.stdev.smartmealtable.admin.store.controller;

import com.stdev.smartmealtable.admin.store.controller.request.*;
import com.stdev.smartmealtable.admin.store.controller.response.*;
import com.stdev.smartmealtable.admin.store.service.StoreApplicationService;
import com.stdev.smartmealtable.admin.store.service.dto.*;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 음식점 관리 API Controller (ADMIN)
 * 
 * <p>관리자가 음식점, 영업시간, 임시 휴무를 관리하는 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/stores")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StoreController {

    private final StoreApplicationService storeApplicationService;

    /**
     * 음식점 목록 조회 (페이징)
     *
     * @param categoryId 카테고리 ID 필터 (선택)
     * @param name 검색할 음식점 이름 (선택)
     * @param storeType 음식점 유형 필터 (선택)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 음식점 목록
     */
    @GetMapping
    public ApiResponse<StoreListResponse> getStores(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) StoreType storeType,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    ) {
        StoreListServiceRequest serviceRequest = StoreListServiceRequest.of(
                categoryId, name, storeType, page, size
        );
        StoreListServiceResponse serviceResponse = storeApplicationService.getStores(serviceRequest);
        
        StoreListResponse response = StoreListResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 음식점 상세 조회
     *
     * @param storeId 음식점 ID
     * @return 음식점 상세 정보
     */
    @GetMapping("/{storeId}")
    public ApiResponse<StoreResponse> getStore(@PathVariable Long storeId) {
        StoreServiceResponse serviceResponse = storeApplicationService.getStore(storeId);
        
        StoreResponse response = StoreResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 음식점 생성
     *
     * @param request 음식점 생성 요청
     * @return 생성된 음식점 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StoreResponse> createStore(@Valid @RequestBody CreateStoreRequest request) {
        CreateStoreServiceRequest serviceRequest = CreateStoreServiceRequest.of(
                request.name(),
                request.categoryId(),
                request.sellerId(),
                request.address(),
                request.lotNumberAddress(),
                request.latitude(),
                request.longitude(),
                request.phoneNumber(),
                request.description(),
                request.averagePrice(),
                request.storeType(),
                request.imageUrl()
        );
        StoreServiceResponse serviceResponse = storeApplicationService.createStore(serviceRequest);
        
        StoreResponse response = StoreResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 음식점 수정
     *
     * @param storeId 음식점 ID
     * @param request 음식점 수정 요청
     * @return 수정된 음식점 정보
     */
    @PutMapping("/{storeId}")
    public ApiResponse<StoreResponse> updateStore(
            @PathVariable Long storeId,
            @Valid @RequestBody UpdateStoreRequest request
    ) {
        UpdateStoreServiceRequest serviceRequest = UpdateStoreServiceRequest.of(
                request.name(),
                request.categoryId(),
                request.address(),
                request.lotNumberAddress(),
                request.latitude(),
                request.longitude(),
                request.phoneNumber(),
                request.description(),
                request.averagePrice(),
                request.storeType(),
                request.imageUrl()
        );
        StoreServiceResponse serviceResponse = storeApplicationService.updateStore(storeId, serviceRequest);
        
        StoreResponse response = StoreResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 음식점 삭제 (논리적 삭제)
     *
     * @param storeId 음식점 ID
     */
    @DeleteMapping("/{storeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStore(@PathVariable Long storeId) {
        storeApplicationService.deleteStore(storeId);
    }

    // ===== 영업시간 관리 =====

    /**
     * 영업시간 추가
     *
     * @param storeId 음식점 ID
     * @param request 영업시간 추가 요청
     * @return 생성된 영업시간 정보
     */
    @PostMapping("/{storeId}/opening-hours")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OpeningHourResponse> addOpeningHour(
            @PathVariable Long storeId,
            @Valid @RequestBody OpeningHourRequest request
    ) {
        OpeningHourServiceRequest serviceRequest = OpeningHourServiceRequest.of(
                request.dayOfWeek(),
                request.openTime(),
                request.closeTime(),
                request.breakStartTime(),
                request.breakEndTime(),
                request.isHoliday()
        );
        OpeningHourServiceResponse serviceResponse = storeApplicationService.addOpeningHour(storeId, serviceRequest);
        
        OpeningHourResponse response = OpeningHourResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 영업시간 수정
     *
     * @param storeId 음식점 ID
     * @param openingHourId 영업시간 ID
     * @param request 영업시간 수정 요청
     * @return 수정된 영업시간 정보
     */
    @PutMapping("/{storeId}/opening-hours/{openingHourId}")
    public ApiResponse<OpeningHourResponse> updateOpeningHour(
            @PathVariable Long storeId,
            @PathVariable Long openingHourId,
            @Valid @RequestBody OpeningHourRequest request
    ) {
        OpeningHourServiceRequest serviceRequest = OpeningHourServiceRequest.of(
                request.dayOfWeek(),
                request.openTime(),
                request.closeTime(),
                request.breakStartTime(),
                request.breakEndTime(),
                request.isHoliday()
        );
        OpeningHourServiceResponse serviceResponse = storeApplicationService.updateOpeningHour(
                storeId, openingHourId, serviceRequest
        );
        
        OpeningHourResponse response = OpeningHourResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 영업시간 삭제
     *
     * @param storeId 음식점 ID
     * @param openingHourId 영업시간 ID
     */
    @DeleteMapping("/{storeId}/opening-hours/{openingHourId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOpeningHour(
            @PathVariable Long storeId,
            @PathVariable Long openingHourId
    ) {
        storeApplicationService.deleteOpeningHour(storeId, openingHourId);
    }

    // ===== 임시 휴무 관리 =====

    /**
     * 임시 휴무 등록
     *
     * @param storeId 음식점 ID
     * @param request 임시 휴무 등록 요청
     * @return 등록된 임시 휴무 정보
     */
    @PostMapping("/{storeId}/temporary-closures")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TemporaryClosureResponse> addTemporaryClosure(
            @PathVariable Long storeId,
            @Valid @RequestBody TemporaryClosureRequest request
    ) {
        TemporaryClosureServiceRequest serviceRequest = TemporaryClosureServiceRequest.of(
                request.closureDate(),
                request.startTime(),
                request.endTime(),
                request.reason()
        );
        TemporaryClosureServiceResponse serviceResponse = storeApplicationService.addTemporaryClosure(
                storeId, serviceRequest
        );
        
        TemporaryClosureResponse response = TemporaryClosureResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 임시 휴무 삭제
     *
     * @param storeId 음식점 ID
     * @param closureId 임시 휴무 ID
     */
    @DeleteMapping("/{storeId}/temporary-closures/{closureId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemporaryClosure(
            @PathVariable Long storeId,
            @PathVariable Long closureId
    ) {
        storeApplicationService.deleteTemporaryClosure(storeId, closureId);
    }
}
