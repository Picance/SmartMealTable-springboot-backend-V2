package com.stdev.smartmealtable.domain.store;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가게 이미지 도메인 엔티티
 * 가게의 사진 정보를 관리합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreImage {
    
    /**
     * 이미지 고유 식별자
     */
    private Long storeImageId;
    
    /**
     * 가게 ID (외부 참조)
     */
    private Long storeId;
    
    /**
     * 이미지 URL
     */
    private String imageUrl;
    
    /**
     * 대표 이미지 여부
     */
    private boolean isMain;
    
    /**
     * 표시 순서
     */
    private Integer displayOrder;
    
    /**
     * 새로운 가게 이미지 생성
     * 
     * @param storeId 가게 ID
     * @param imageUrl 이미지 URL
     * @param isMain 대표 이미지 여부
     * @param displayOrder 표시 순서
     * @return 생성된 StoreImage 객체
     */
    public static StoreImage create(Long storeId, String imageUrl, boolean isMain, Integer displayOrder) {
        return StoreImage.builder()
                .storeId(storeId)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .displayOrder(displayOrder)
                .build();
    }
    
    /**
     * 이미지가 유효한지 검증
     */
    public boolean isValid() {
        return imageUrl != null && !imageUrl.trim().isEmpty()
            && storeId != null;
    }
    
    /**
     * 대표 이미지인지 확인
     */
    public boolean isMainImage() {
        return isMain;
    }
}
