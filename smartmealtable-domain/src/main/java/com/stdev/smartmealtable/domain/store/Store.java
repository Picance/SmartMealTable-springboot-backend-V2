package com.stdev.smartmealtable.domain.store;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 음식점(가게) 도메인 엔티티
 * 음식을 판매하는 음식점의 정보를 관리합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Store {
    
    /**
     * 가게 고유 식별자
     */
    private Long storeId;
    
    /**
     * 가게명
     */
    private String name;
    
    /**
     * 카테고리 ID (외부 참조)
     */
    private Long categoryId;
    
    /**
     * 판매자 ID (외부 참조)
     */
    private Long sellerId;
    
    /**
     * 도로명 주소
     */
    private String address;
    
    /**
     * 지번 주소
     */
    private String lotNumberAddress;
    
    /**
     * 위도
     */
    private BigDecimal latitude;
    
    /**
     * 경도
     */
    private BigDecimal longitude;
    
    /**
     * 전화번호
     */
    private String phoneNumber;
    
    /**
     * 가게 설명
     */
    private String description;
    
    /**
     * 평균 가격
     */
    private Integer averagePrice;
    
    /**
     * 리뷰 수
     */
    private Integer reviewCount;
    
    /**
     * 조회수
     */
    private Integer viewCount;
    
    /**
     * 즐겨찾기 수
     */
    private Integer favoriteCount;
    
    /**
     * 가게 유형 (학생식당, 일반음식점)
     */
    private StoreType storeType;
    
    /**
     * 대표 이미지 URL
     */
    private String imageUrl;
    
    /**
     * 가게 등록일시 (비즈니스적 의미가 있는 시간)
     */
    private LocalDateTime registeredAt;
    
    /**
     * 논리 삭제 일시
     */
    private LocalDateTime deletedAt;
    
    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    /**
     * 학생 식당 여부 확인
     */
    public boolean isCampusRestaurant() {
        return storeType == StoreType.CAMPUS_RESTAURANT;
    }
    
    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /**
     * 새로운 가게 생성 (테스트 및 초기화 시 사용)
     */
    public static Store create(
            String name,
            Long categoryId,
            String address,
            String lotNumberAddress,
            BigDecimal latitude,
            BigDecimal longitude,
            String phoneNumber,
            String description,
            Integer averagePrice,
            Integer reviewCount,
            Integer viewCount,
            Integer favoriteCount,
            StoreType storeType
    ) {
        return Store.builder()
                .name(name)
                .categoryId(categoryId)
                .address(address)
                .lotNumberAddress(lotNumberAddress)
                .latitude(latitude)
                .longitude(longitude)
                .phoneNumber(phoneNumber)
                .description(description)
                .averagePrice(averagePrice)
                .reviewCount(reviewCount != null ? reviewCount : 0)
                .viewCount(viewCount != null ? viewCount : 0)
                .favoriteCount(favoriteCount != null ? favoriteCount : 0)
                .storeType(storeType)
                .registeredAt(LocalDateTime.now())
                .build();
    }
}
