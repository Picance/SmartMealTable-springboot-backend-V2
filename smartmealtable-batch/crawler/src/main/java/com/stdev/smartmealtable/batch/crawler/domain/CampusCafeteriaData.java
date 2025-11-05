package com.stdev.smartmealtable.batch.crawler.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 학식당 크롤링 데이터 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusCafeteriaData {
    
    /**
     * 건물명 (예: ST: Table, ST: Dining)
     */
    private String buildingName;
    
    /**
     * 건물 주소
     */
    private String address;
    
    /**
     * 위도
     */
    private BigDecimal latitude;
    
    /**
     * 경도
     */
    private BigDecimal longitude;
    
    /**
     * 가게 목록
     */
    private List<RestaurantData> restaurants;
    
    /**
     * 가게 데이터
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantData {
        /**
         * 가게명
         */
        private String name;
        
        /**
         * 카테고리명 (한식, 중식, 일식, 양식, 아시안)
         */
        private String categoryName;
        
        /**
         * 메뉴 목록
         */
        private List<MenuData> menus;
    }
    
    /**
     * 메뉴 데이터
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuData {
        /**
         * 메뉴명
         */
        private String name;
        
        /**
         * 가격
         */
        private Integer price;
    }
}

