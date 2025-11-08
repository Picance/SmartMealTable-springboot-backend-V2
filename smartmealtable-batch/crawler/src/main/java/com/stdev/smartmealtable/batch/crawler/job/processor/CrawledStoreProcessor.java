package com.stdev.smartmealtable.batch.crawler.job.processor;

import com.stdev.smartmealtable.batch.crawler.dto.CrawledStoreDto;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final Map<String, Long> categoryCache = new HashMap<>();

    public CrawledStoreProcessor(StoreRepository storeRepository, CategoryRepository categoryRepository) {
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
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
        // Store 카테고리 ID 조회/생성 (매장 카테고리)
        Long storeCategoryId = resolveStoreCategoryId(dto.getCategory());
        
        return Store.builder()
                .storeId(existing != null ? existing.getStoreId() : null)
                .externalId(dto.getId())
                .name(dto.getName())
                .categoryId(storeCategoryId)
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
        
        // 음식 카테고리는 "음식" 또는 JSON의 카테고리 기반으로 결정
        Long foodCategoryId = resolveFoodCategoryId(dto.getCategory());
        
        for (CrawledStoreDto.MenuInfo menuInfo : dto.getMenus()) {
            Food food = Food.builder()
                    .foodId(null) // 새로 생성
                    .storeId(store.getStoreId()) // Writer에서 실제 storeId로 업데이트 필요
                    .categoryId(foodCategoryId) // 음식 카테고리
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
     * Food 카테고리 ID 조회/생성
     * 음식의 종류를 나타내는 카테고리 (예: 한식, 중식, 음료 등)
     * Store의 카테고리를 기반으로 Food 카테고리 결정
     */
    private Long resolveFoodCategoryId(String storeCategory) {
        // 음식 카테고리 이름을 매장 카테고리에서 유도
        String foodCategoryName = "음식"; // 기본값
        
        if (storeCategory != null && !storeCategory.isEmpty()) {
            // 매장 카테고리를 기반으로 음식 카테고리 결정 (예: "한식점" → "한식")
            if (storeCategory.contains("한식")) {
                foodCategoryName = "한식";
            } else if (storeCategory.contains("중식")) {
                foodCategoryName = "중식";
            } else if (storeCategory.contains("일식")) {
                foodCategoryName = "일식";
            } else if (storeCategory.contains("양식")) {
                foodCategoryName = "양식";
            } else if (storeCategory.contains("카페")) {
                foodCategoryName = "음료";
            } else if (storeCategory.contains("피자")) {
                foodCategoryName = "피자";
            } else if (storeCategory.contains("치킨")) {
                foodCategoryName = "치킨";
            } else {
                foodCategoryName = storeCategory;
            }
        }
        
        Long foodCategoryId = resolveCategoryId(foodCategoryName);
        log.debug("Resolved food category: {} → {} (ID: {})", storeCategory, foodCategoryName, foodCategoryId);
        return foodCategoryId;
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
     * 카테고리가 없으면 자동으로 생성
     */
    private Long resolveCategoryId(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            // 기본 카테고리 ("기타")를 사용
            categoryName = "기타";
        }
        
        // 캐시 확인
        if (categoryCache.containsKey(categoryName)) {
            return categoryCache.get(categoryName);
        }
        
        // DB에서 조회
        var existingCategory = categoryRepository.findByName(categoryName);
        if (existingCategory.isPresent()) {
            Long categoryId = existingCategory.get().getCategoryId();
            categoryCache.put(categoryName, categoryId);
            return categoryId;
        }
        
        // 카테고리가 없으면 새로 생성
        Category newCategory = Category.create(categoryName);
        Category savedCategory = categoryRepository.save(newCategory);
        Long categoryId = savedCategory.getCategoryId();
        
        categoryCache.put(categoryName, categoryId);
        log.info("Created new category: {} (ID: {})", categoryName, categoryId);
        
        return categoryId;
    }
    
    /**
     * Store 카테고리 ID 조회/생성
     * 매장의 종류를 나타내는 카테고리 (예: 한식, 중식, 카페 등)
     */
    private Long resolveStoreCategoryId(String storeCategoryName) {
        if (storeCategoryName == null || storeCategoryName.isEmpty()) {
            storeCategoryName = "기타";
        }
        
        // 캐시 확인
        if (categoryCache.containsKey(storeCategoryName)) {
            Long categoryId = categoryCache.get(storeCategoryName);
            log.debug("Using cached store category: {} (ID: {})", storeCategoryName, categoryId);
            return categoryId;
        }
        
        // DB에서 조회
        var existingCategory = categoryRepository.findByName(storeCategoryName);
        if (existingCategory.isPresent()) {
            Long categoryId = existingCategory.get().getCategoryId();
            categoryCache.put(storeCategoryName, categoryId);
            log.debug("Using existing store category: {} (ID: {})", storeCategoryName, categoryId);
            return categoryId;
        }
        
        // 카테고리가 없으면 새로 생성
        Category newCategory = Category.create(storeCategoryName);
        Category savedCategory = categoryRepository.save(newCategory);
        Long categoryId = savedCategory.getCategoryId();
        
        categoryCache.put(storeCategoryName, categoryId);
        log.info("Created new store category: {} (ID: {})", storeCategoryName, categoryId);
        
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
