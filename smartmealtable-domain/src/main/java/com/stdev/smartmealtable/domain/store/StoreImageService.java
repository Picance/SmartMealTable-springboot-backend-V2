package com.stdev.smartmealtable.domain.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 가게 이미지 도메인 서비스
 * 
 * <p>가게 이미지의 비즈니스 로직을 담당합니다.</p>
 * <ul>
 *   <li>대표 이미지 자동 관리 (isMain 자동 전환)</li>
 *   <li>표시 순서 관리 (displayOrder 자동 할당)</li>
 *   <li>이미지 유효성 검증</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StoreImageService {
    
    private final StoreImageRepository storeImageRepository;
    
    /**
     * 가게 이미지를 생성합니다.
     * 
     * <p>대표 이미지로 설정 시 기존 대표 이미지를 자동으로 해제합니다.</p>
     * <p>displayOrder가 null이면 자동으로 마지막 순서로 할당합니다.</p>
     * 
     * @param storeImage 생성할 이미지
     * @return 생성된 이미지
     */
    public StoreImage createImage(StoreImage storeImage) {
        validateImage(storeImage);
        
        // 대표 이미지로 설정하는 경우 기존 대표 이미지 해제
        if (storeImage.isMain()) {
            unsetExistingMainImage(storeImage.getStoreId());
        }
        
        // displayOrder가 null이면 자동 할당
        StoreImage imageToSave = storeImage;
        if (storeImage.getDisplayOrder() == null) {
            int nextOrder = getNextDisplayOrder(storeImage.getStoreId());
            imageToSave = StoreImage.builder()
                    .storeId(storeImage.getStoreId())
                    .imageUrl(storeImage.getImageUrl())
                    .isMain(storeImage.isMain())
                    .displayOrder(nextOrder)
                    .build();
        }
        
        StoreImage savedImage = storeImageRepository.save(imageToSave);
        log.info("가게 이미지 생성 완료 - storeId: {}, imageId: {}, isMain: {}, displayOrder: {}", 
                savedImage.getStoreId(), savedImage.getStoreImageId(), 
                savedImage.isMain(), savedImage.getDisplayOrder());
        
        return savedImage;
    }
    
    /**
     * 가게 이미지를 수정합니다.
     * 
     * <p>대표 이미지로 변경 시 기존 대표 이미지를 자동으로 해제합니다.</p>
     * 
     * @param storeImageId 이미지 ID
     * @param storeId 가게 ID (권한 검증용)
     * @param imageUrl 새로운 이미지 URL
     * @param isMain 대표 이미지 여부
     * @param displayOrder 표시 순서
     * @return 수정된 이미지
     */
    public StoreImage updateImage(Long storeImageId, Long storeId, String imageUrl, 
                                  boolean isMain, Integer displayOrder) {
        StoreImage existingImage = storeImageRepository.findById(storeImageId);
        
        if (existingImage == null) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다. imageId: " + storeImageId);
        }
        
        if (!existingImage.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("해당 가게의 이미지가 아닙니다. storeId: " + storeId);
        }
        
        // 대표 이미지로 변경하는 경우 기존 대표 이미지 해제
        if (isMain && !existingImage.isMain()) {
            unsetExistingMainImage(storeId);
        }
        
        StoreImage updatedImage = StoreImage.builder()
                .storeImageId(existingImage.getStoreImageId())
                .storeId(existingImage.getStoreId())
                .imageUrl(imageUrl)
                .isMain(isMain)
                .displayOrder(displayOrder)
                .build();
        
        validateImage(updatedImage);
        
        StoreImage savedImage = storeImageRepository.save(updatedImage);
        log.info("가게 이미지 수정 완료 - storeId: {}, imageId: {}, isMain: {}, displayOrder: {}", 
                savedImage.getStoreId(), savedImage.getStoreImageId(), 
                savedImage.isMain(), savedImage.getDisplayOrder());
        
        return savedImage;
    }
    
    /**
     * 가게 이미지를 삭제합니다.
     * 
     * @param storeImageId 이미지 ID
     * @param storeId 가게 ID (권한 검증용)
     */
    public void deleteImage(Long storeImageId, Long storeId) {
        StoreImage existingImage = storeImageRepository.findById(storeImageId);
        
        if (existingImage == null) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다. imageId: " + storeImageId);
        }
        
        if (!existingImage.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("해당 가게의 이미지가 아닙니다. storeId: " + storeId);
        }
        
        // Repository에서는 물리적 삭제 (이미지는 논리적 삭제 불필요)
        storeImageRepository.deleteByStoreId(storeId);
        
        log.info("가게 이미지 삭제 완료 - storeId: {}, imageId: {}", storeId, storeImageId);
    }
    
    /**
     * 가게의 모든 이미지를 조회합니다.
     * 
     * <p>대표 이미지 우선, displayOrder 오름차순으로 정렬됩니다.</p>
     * 
     * @param storeId 가게 ID
     * @return 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<StoreImage> getStoreImages(Long storeId) {
        return storeImageRepository.findByStoreId(storeId);
    }
    
    /**
     * 이미지 ID로 단건 조회합니다.
     * 
     * @param storeImageId 이미지 ID
     * @return 이미지
     */
    @Transactional(readOnly = true)
    public StoreImage getImageById(Long storeImageId) {
        StoreImage image = storeImageRepository.findById(storeImageId);
        if (image == null) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다. imageId: " + storeImageId);
        }
        return image;
    }
    
    /**
     * 가게의 대표 이미지를 조회합니다.
     * 
     * @param storeId 가게 ID
     * @return 대표 이미지 (없으면 첫 번째 이미지)
     */
    @Transactional(readOnly = true)
    public Optional<StoreImage> getMainImage(Long storeId) {
        Optional<StoreImage> mainImage = storeImageRepository.findByStoreIdAndIsMainTrue(storeId);
        
        if (mainImage.isEmpty()) {
            // 대표 이미지가 없으면 첫 번째 이미지 반환
            return storeImageRepository.findFirstByStoreIdOrderByDisplayOrderAsc(storeId);
        }
        
        return mainImage;
    }
    
    /**
     * 기존 대표 이미지를 해제합니다.
     * 
     * @param storeId 가게 ID
     */
    private void unsetExistingMainImage(Long storeId) {
        Optional<StoreImage> existingMain = storeImageRepository.findByStoreIdAndIsMainTrue(storeId);
        
        if (existingMain.isPresent()) {
            StoreImage currentMain = existingMain.get();
            StoreImage updatedMain = StoreImage.builder()
                    .storeImageId(currentMain.getStoreImageId())
                    .storeId(currentMain.getStoreId())
                    .imageUrl(currentMain.getImageUrl())
                    .isMain(false)  // 대표 이미지 해제
                    .displayOrder(currentMain.getDisplayOrder())
                    .build();
            
            storeImageRepository.save(updatedMain);
            log.debug("기존 대표 이미지 해제 - storeId: {}, imageId: {}", storeId, currentMain.getStoreImageId());
        }
    }
    
    /**
     * 다음 displayOrder 값을 계산합니다.
     * 
     * @param storeId 가게 ID
     * @return 다음 순서 번호
     */
    private int getNextDisplayOrder(Long storeId) {
        List<StoreImage> existingImages = storeImageRepository.findByStoreId(storeId);
        
        if (existingImages.isEmpty()) {
            return 1;
        }
        
        return existingImages.stream()
                .map(StoreImage::getDisplayOrder)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
    
    /**
     * 이미지 유효성을 검증합니다.
     * 
     * @param storeImage 검증할 이미지
     */
    private void validateImage(StoreImage storeImage) {
        if (storeImage == null) {
            throw new IllegalArgumentException("이미지 정보가 null입니다.");
        }
        
        if (!storeImage.isValid()) {
            throw new IllegalArgumentException("유효하지 않은 이미지 정보입니다.");
        }
    }
}
