package com.stdev.smartmealtable.batch.crawler.job.processor;

import com.stdev.smartmealtable.batch.crawler.dto.CrawledStoreDto;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.store.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 크롤링 데이터를 도메인 객체로 변환하는 Processor
 * 기존 데이터는 업데이트, 신규 데이터는 생성 (Upsert 로직)
 */
@Slf4j
@Component
public class CrawledStoreProcessor implements ItemProcessor<CrawledStoreDto, CrawledStoreProcessor.ProcessedStoreData> {
    
    private final StoreRepository storeRepository;
    private final Map<String, Long> categoryCache = new HashMap<>();
    
    public CrawledStoreProcessor(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }
    
    @Override
    public ProcessedStoreData process(CrawledStoreDto item) throws Exception {
        log.debug("Processing crawled store: {} (externalId: {})", item.getName(), item.getId());
        
        try {
            // 1. 기존 가게 조회 (externalId 기준)
            Store existingStore = storeRepository.findByExternalId(item.getId()).orElse(null);
            
            // 2. 가게 정보 변환
            Store store = convertToStore(item, existingStore);
            
            // 3. 음식 정보 변환
            List<Food> foods = convertToFoods(item, store);
            
            // 4. 이미지 정보 변환
            List<StoreImage> images = convertToImages(item, store);
            
            // 5. 영업시간 정보 변환
            List<StoreOpeningHour> openingHours = convertToOpeningHours(item, store);
            
            return new ProcessedStoreData(store, foods, images, openingHours);
            
        } catch (Exception e) {
            log.error("Failed to process store: {} (externalId: {})", item.getName(), item.getId(), e);
            throw e;
        }
    }
    
    /**
     * 크롤링 데이터 → Store 도메인 객체 변환
     */
    private Store convertToStore(CrawledStoreDto dto, Store existing) {
        // 카테고리 ID 조회 (간단화를 위해 임시로 하드코딩, 실제로는 Category 조회 필요)
        Long categoryId = resolveCategoryId(dto.getCategory());
        
        return Store.builder()
                .storeId(existing != null ? existing.getStoreId() : null)
                .externalId(dto.getId())
                .name(dto.getName())
                .categoryId(categoryId)
                .sellerId(null) // 크롤링 데이터에는 판매자 정보 없음
                .address(dto.getAddress())
                .lotNumberAddress(null) // 크롤링 데이터에는 지번주소 없음
                .latitude(dto.getCoordinates() != null ? dto.getCoordinates().getLatitude() : null)
                .longitude(dto.getCoordinates() != null ? dto.getCoordinates().getLongitude() : null)
                .phoneNumber(dto.getPhoneNumber())
                .description(null) // 크롤링 데이터에는 설명 없음
                .averagePrice(dto.getMenuAverage())
                .reviewCount(dto.getReviewCount() != null ? dto.getReviewCount() : 0)
                .viewCount(existing != null ? existing.getViewCount() : 0)
                .favoriteCount(existing != null ? existing.getFavoriteCount() : 0)
                .storeType(StoreType.RESTAURANT) // 기본값: 일반 음식점
                .imageUrl(getMainImageUrl(dto.getImages()))
                .registeredAt(existing != null ? existing.getRegisteredAt() : LocalDateTime.now())
                .deletedAt(null)
                .build();
    }
    
    /**
     * 음식(메뉴) 정보 변환
     */
    private List<Food> convertToFoods(CrawledStoreDto dto, Store store) {
        if (dto.getMenus() == null || dto.getMenus().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Food> foods = new ArrayList<>();
        int displayOrder = 0;
        
        // 기본 카테고리 ID (크롤링 데이터에는 음식 카테고리 없음, 임시로 1L 사용)
        Long defaultFoodCategoryId = 1L;
        
        for (CrawledStoreDto.MenuInfo menuInfo : dto.getMenus()) {
            Food food = Food.builder()
                    .foodId(null) // 새로 생성
                    .storeId(store.getStoreId()) // Writer에서 실제 storeId로 업데이트 필요
                    .categoryId(defaultFoodCategoryId) // 음식 카테고리 (임시)
                    .foodName(menuInfo.getName())
                    .description(menuInfo.getIntroduce())
                    .price(menuInfo.getPrice())
                    .imageUrl(menuInfo.getImgUrl())
                    .isMain(menuInfo.getIsMain() != null && menuInfo.getIsMain())
                    .displayOrder(displayOrder++)
                    .registeredDt(null) // DB DEFAULT
                    .deletedAt(null)
                    .build();
            
            if (food.isValid()) {
                foods.add(food);
            }
        }
        
        return foods;
    }
    
    /**
     * 이미지 정보 변환
     */
    private List<StoreImage> convertToImages(CrawledStoreDto dto, Store store) {
        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StoreImage> images = new ArrayList<>();
        int displayOrder = 0;
        
        for (String imageUrl : dto.getImages()) {
            StoreImage image = StoreImage.builder()
                    .storeImageId(null)
                    .storeId(store.getStoreId()) // Writer에서 실제 storeId로 업데이트 필요
                    .imageUrl(imageUrl)
                    .isMain(displayOrder == 0) // 첫 번째 이미지를 대표 이미지로
                    .displayOrder(displayOrder++)
                    .build();
            
            if (image.isValid()) {
                images.add(image);
            }
        }
        
        return images;
    }
    
    /**
     * 영업시간 정보 변환
     */
    private List<StoreOpeningHour> convertToOpeningHours(CrawledStoreDto dto, Store store) {
        if (dto.getOpeningHours() == null || dto.getOpeningHours().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StoreOpeningHour> openingHours = new ArrayList<>();
        
        for (CrawledStoreDto.OpeningHour oh : dto.getOpeningHours()) {
            DayOfWeek dayOfWeek = convertDayOfWeek(oh.getDayOfWeek());
            if (dayOfWeek == null || oh.getHours() == null) {
                continue;
            }
            
            StoreOpeningHour openingHour = new StoreOpeningHour(
                    null, // storeOpeningHourId
                    store.getStoreId(), // Writer에서 실제 storeId로 업데이트 필요
                    dayOfWeek,
                    oh.getHours().getStartTime(),
                    oh.getHours().getEndTime(),
                    oh.getHours().getBreakStartTime(),
                    oh.getHours().getBreakEndTime(),
                    false // 영업시간이 있으면 휴무일 아님
            );
            
            openingHours.add(openingHour);
        }
        
        return openingHours;
    }
    
    /**
     * 카테고리 이름으로 카테고리 ID 조회
     * TODO: CategoryRepository를 통해 실제 조회하도록 개선
     */
    private Long resolveCategoryId(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return 1L; // 기본 카테고리
        }
        
        // 캐시 확인
        if (categoryCache.containsKey(categoryName)) {
            return categoryCache.get(categoryName);
        }
        
        // 임시 매핑 (실제로는 DB에서 조회)
        Long categoryId = 1L; // 기본값
        categoryCache.put(categoryName, categoryId);
        
        return categoryId;
    }
    
    /**
     * 대표 이미지 URL 추출
     */
    private String getMainImageUrl(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0);
    }
    
    /**
     * 요일 문자열을 DayOfWeek로 변환
     */
    private DayOfWeek convertDayOfWeek(String dayStr) {
        if (dayStr == null) {
            return null;
        }
        
        return switch (dayStr) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            case "토" -> DayOfWeek.SATURDAY;
            case "일" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }
    
    /**
     * 처리된 가게 데이터 (Store + 관련 엔티티들)
     */
    @Getter
    public static class ProcessedStoreData {
        private final Store store;
        private final List<Food> foods;
        private final List<StoreImage> images;
        private final List<StoreOpeningHour> openingHours;
        
        public ProcessedStoreData(Store store, List<Food> foods, 
                                  List<StoreImage> images, List<StoreOpeningHour> openingHours) {
            this.store = store;
            this.foods = foods;
            this.images = images;
            this.openingHours = openingHours;
        }
    }
}
