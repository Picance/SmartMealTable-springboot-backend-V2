package com.stdev.smartmealtable.admin.storeimage.service;

import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.store.StoreImage;
import com.stdev.smartmealtable.domain.store.StoreImageService;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.stdev.smartmealtable.core.error.ErrorType.*;

/**
 * 가게 이미지 관리 Application Service
 * 
 * <p>admin 모듈의 가게 이미지 관리 유즈케이스를 처리합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StoreImageApplicationService {
    
    private final StoreImageService storeImageService;
    private final StoreRepository storeRepository;
    
    /**
     * 가게 이미지를 추가합니다.
     * 
     * @param storeId 가게 ID
     * @param imageUrl 이미지 URL
     * @param isMain 대표 이미지 여부
     * @param displayOrder 표시 순서 (null이면 자동 할당)
     * @return 생성된 이미지
     */
    public StoreImage createStoreImage(Long storeId, String imageUrl, boolean isMain, Integer displayOrder) {
        log.info("[ADMIN] 가게 이미지 추가 요청 - storeId: {}, isMain: {}, displayOrder: {}", 
                storeId, isMain, displayOrder);
        
        // 가게 존재 여부 검증
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        StoreImage storeImage = StoreImage.builder()
                .storeId(storeId)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .displayOrder(displayOrder)
                .build();
        
        return storeImageService.createImage(storeImage);
    }
    
    /**
     * 가게 이미지를 수정합니다.
     * 
     * @param storeId 가게 ID
     * @param storeImageId 이미지 ID
     * @param imageUrl 이미지 URL
     * @param isMain 대표 이미지 여부
     * @param displayOrder 표시 순서
     * @return 수정된 이미지
     */
    public StoreImage updateStoreImage(Long storeId, Long storeImageId, 
                                       String imageUrl, boolean isMain, Integer displayOrder) {
        log.info("[ADMIN] 가게 이미지 수정 요청 - storeId: {}, imageId: {}, isMain: {}, displayOrder: {}", 
                storeId, storeImageId, isMain, displayOrder);
        
        // 가게 존재 여부 검증
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        return storeImageService.updateImage(storeImageId, storeId, imageUrl, isMain, displayOrder);
    }
    
    /**
     * 가게 이미지를 삭제합니다.
     * 
     * @param storeId 가게 ID
     * @param storeImageId 이미지 ID
     */
    public void deleteStoreImage(Long storeId, Long storeImageId) {
        log.info("[ADMIN] 가게 이미지 삭제 요청 - storeId: {}, imageId: {}", storeId, storeImageId);
        
        storeImageService.deleteImage(storeImageId, storeId);
    }
    
    /**
     * 가게 이미지 목록 조회
     * 
     * @param storeId 가게 ID
     * @return 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<StoreImage> getStoreImages(Long storeId) {
        // 가게 존재 확인
        storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        return storeImageService.getStoreImages(storeId);
    }
}
