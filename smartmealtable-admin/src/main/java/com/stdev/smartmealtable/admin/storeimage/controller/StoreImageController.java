package com.stdev.smartmealtable.admin.storeimage.controller;

import com.stdev.smartmealtable.admin.storeimage.controller.request.CreateStoreImageRequest;
import com.stdev.smartmealtable.admin.storeimage.controller.request.UpdateStoreImageRequest;
import com.stdev.smartmealtable.admin.storeimage.controller.response.StoreImageResponse;
import com.stdev.smartmealtable.admin.storeimage.service.StoreImageApplicationService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.domain.store.StoreImage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 가게 이미지 관리 Controller (ADMIN)
 * 
 * <p>관리자가 가게의 이미지를 관리하는 API를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/stores/{storeId}/images")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StoreImageController {
    
    private final StoreImageApplicationService storeImageApplicationService;
    
    /**
     * 가게 이미지 목록 조회
     * 
     * @param storeId 가게 ID
     * @return 이미지 목록 (displayOrder 오름차순, 대표 이미지 우선)
     */
    @GetMapping
    public ApiResponse<List<StoreImageResponse>> getStoreImages(
            @PathVariable @Positive Long storeId
    ) {
        log.info("[ADMIN] GET /api/v1/admin/stores/{}/images", storeId);
        
        List<StoreImage> storeImages = storeImageApplicationService.getStoreImages(storeId);
        
        List<StoreImageResponse> responses = storeImages.stream()
                .map(StoreImageResponse::from)
                .toList();
        
        return ApiResponse.success(responses);
    }
    
    /**
     * 가게 이미지 추가
     * 
     * @param storeId 가게 ID
     * @param request 이미지 추가 요청
     * @return 생성된 이미지 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StoreImageResponse> createStoreImage(
            @PathVariable @Positive Long storeId,
            @RequestBody @Valid CreateStoreImageRequest request
    ) {
        log.info("[ADMIN] POST /api/v1/admin/stores/{}/images - isMain: {}, displayOrder: {}", 
                storeId, request.isMain(), request.displayOrder());
        
        StoreImage storeImage = storeImageApplicationService.createStoreImage(
                storeId,
                request.imageUrl(),
                request.isMain(),
                request.displayOrder()
        );
        
        StoreImageResponse response = StoreImageResponse.from(storeImage);
        return ApiResponse.success(response);
    }
    
    /**
     * 가게 이미지 수정
     * 
     * @param storeId 가게 ID
     * @param storeImageId 이미지 ID
     * @param request 이미지 수정 요청
     * @return 수정된 이미지 정보
     */
    @PutMapping("/{storeImageId}")
    public ApiResponse<StoreImageResponse> updateStoreImage(
            @PathVariable @Positive Long storeId,
            @PathVariable @Positive Long storeImageId,
            @RequestBody @Valid UpdateStoreImageRequest request
    ) {
        log.info("[ADMIN] PUT /api/v1/admin/stores/{}/images/{} - isMain: {}, displayOrder: {}", 
                storeId, storeImageId, request.isMain(), request.displayOrder());
        
        StoreImage storeImage = storeImageApplicationService.updateStoreImage(
                storeId,
                storeImageId,
                request.imageUrl(),
                request.isMain(),
                request.displayOrder()
        );
        
        StoreImageResponse response = StoreImageResponse.from(storeImage);
        return ApiResponse.success(response);
    }
    
    /**
     * 가게 이미지 삭제
     * 
     * @param storeId 가게 ID
     * @param storeImageId 이미지 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{storeImageId}")
    public ResponseEntity<Void> deleteStoreImage(
            @PathVariable @Positive Long storeId,
            @PathVariable @Positive Long storeImageId
    ) {
        log.info("[ADMIN] DELETE /api/v1/admin/stores/{}/images/{}", storeId, storeImageId);
        
        storeImageApplicationService.deleteStoreImage(storeId, storeImageId);
        
        return ResponseEntity.noContent().build();
    }
}

