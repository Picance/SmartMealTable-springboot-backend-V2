package com.stdev.smartmealtable.domain.store;

import com.stdev.smartmealtable.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.stdev.smartmealtable.core.error.ErrorType.*;

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
    private final StoreRepository storeRepository;
    
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
        
        // Store 존재 여부 검증
        storeRepository.findById(storeImage.getStoreId())
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND));
        
        // 가게의 첫 번째 이미지인 경우 자동으로 대표 이미지로 설정
        List<StoreImage> existingImages = storeImageRepository.findByStoreId(storeImage.getStoreId());
        boolean isFirstImage = existingImages.isEmpty();
        boolean shouldBeMain = isFirstImage || storeImage.isMain();
        
        // 대표 이미지로 설정하는 경우 기존 대표 이미지 해제
        if (shouldBeMain) {
            unsetExistingMainImage(storeImage.getStoreId());
        }
        
        // displayOrder가 null이면 자동 할당
        Integer displayOrder = storeImage.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = getNextDisplayOrder(storeImage.getStoreId());
        }
        
        // 이미지 저장
        StoreImage imageToSave = StoreImage.builder()
                .storeId(storeImage.getStoreId())
                .imageUrl(storeImage.getImageUrl())
                .isMain(shouldBeMain)
                .displayOrder(displayOrder)
                .build();
        
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
        StoreImage existingImage = storeImageRepository.findById(storeImageId)
                .orElseThrow(() -> new BusinessException(STORE_IMAGE_NOT_FOUND));
        
        if (!existingImage.getStoreId().equals(storeId)) {
            throw new BusinessException(STORE_IMAGE_NOT_FOUND);
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
        StoreImage existingImage = storeImageRepository.findById(storeImageId)
                .orElseThrow(() -> new BusinessException(STORE_IMAGE_NOT_FOUND));
        
        if (!existingImage.getStoreId().equals(storeId)) {
            throw new BusinessException(STORE_IMAGE_NOT_FOUND);
        }
        
        boolean wasMainImage = existingImage.isMain();
        
        // 개별 이미지만 삭제 (deleteById 사용)
        storeImageRepository.deleteById(storeImageId);
        
        // 대표 이미지를 삭제한 경우, 다음 이미지를 대표로 설정
        if (wasMainImage) {
            promoteNextImageToMain(storeId);
        }
        
        log.info("가게 이미지 삭제 완료 - storeId: {}, imageId: {}, wasMain: {}", storeId, storeImageId, wasMainImage);
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
        return storeImageRepository.findById(storeImageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지입니다. imageId: " + storeImageId));
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
     * 다음 이미지를 대표 이미지로 승격합니다.
     * 
     * <p>displayOrder가 가장 작은 이미지를 대표 이미지로 설정합니다.</p>
     * 
     * @param storeId 가게 ID
     */
    private void promoteNextImageToMain(Long storeId) {
        List<StoreImage> remainingImages = storeImageRepository.findByStoreId(storeId);
        
        if (remainingImages.isEmpty()) {
            log.debug("남은 이미지가 없어 대표 이미지 승격을 건너뜁니다 - storeId: {}", storeId);
            return;
        }
        
        // displayOrder가 가장 작은 이미지를 찾아 대표 이미지로 설정
        StoreImage nextMainImage = remainingImages.stream()
                .min((a, b) -> Integer.compare(
                        a.getDisplayOrder() != null ? a.getDisplayOrder() : Integer.MAX_VALUE,
                        b.getDisplayOrder() != null ? b.getDisplayOrder() : Integer.MAX_VALUE
                ))
                .orElseThrow();
        
        StoreImage promotedImage = StoreImage.builder()
                .storeImageId(nextMainImage.getStoreImageId())
                .storeId(nextMainImage.getStoreId())
                .imageUrl(nextMainImage.getImageUrl())
                .isMain(true)  // 대표 이미지로 승격
                .displayOrder(nextMainImage.getDisplayOrder())
                .build();
        
        storeImageRepository.save(promotedImage);
        log.info("다음 이미지를 대표 이미지로 승격 - storeId: {}, imageId: {}, displayOrder: {}", 
                storeId, promotedImage.getStoreImageId(), promotedImage.getDisplayOrder());
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
                .filter(Objects::nonNull)
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
