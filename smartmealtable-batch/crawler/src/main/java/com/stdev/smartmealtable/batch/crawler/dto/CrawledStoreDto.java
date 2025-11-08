package com.stdev.smartmealtable.batch.crawler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 크롤링 JSON 데이터를 매핑하는 DTO
 * 네이버 플레이스 등의 원본 데이터 구조를 표현
 */
@Getter
@Setter
public class CrawledStoreDto {
    
    /**
     * 외부 시스템의 가게 ID (네이버 플레이스 ID 등)
     */
    private String id;
    
    /**
     * 가게명
     */
    private String name;
    
    /**
     * 카테고리
     */
    private String category;
    
    /**
     * 주소
     */
    private String address;
    
    /**
     * 좌표 정보
     */
    private Coordinates coordinates;
    
    /**
     * 이미지 URL 목록
     */
    private List<String> images;
    
    /**
     * 영업 시간 정보
     */
    @JsonProperty("openingHours")
    private List<OpeningHour> openingHours;
    
    /**
     * 메뉴 정보
     */
    private List<MenuInfo> menus;
    
    /**
     * 메뉴 평균 가격
     */
    @JsonProperty("menu_average")
    private Integer menuAverage;
    
    /**
     * 전화번호
     */
    @JsonProperty("phone_number")
    private String phoneNumber;
    
    /**
     * 리뷰 수
     */
    @JsonProperty("review_count")
    private Integer reviewCount;
    
    /**
     * 좌표 정보
     */
    @Getter
    @Setter
    public static class Coordinates {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
    
    /**
     * 영업 시간 정보
     */
    @Getter
    @Setter
    public static class OpeningHour {
        
        @JsonProperty("dayOfWeek")
        private String dayOfWeek;
        
        private Hours hours;
        
        @Getter
        @Setter
        public static class Hours {
            private String startTime;
            private String endTime;
            private String breakStartTime;
            private String breakEndTime;
            private String lastOrderTime;
        }
    }
    
    /**
     * 메뉴 정보
     */
    @Getter
    @Setter
    public static class MenuInfo {

        private Boolean isMain;
        private String name;
        private String introduce;
        private Integer price;
        private String imgUrl;
    }
}
