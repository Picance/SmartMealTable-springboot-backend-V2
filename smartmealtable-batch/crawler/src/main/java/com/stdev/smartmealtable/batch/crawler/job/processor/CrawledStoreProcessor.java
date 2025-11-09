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
        // CSV 형식의 카테고리를 분리해서 처리
        List<Long> storeCategoryIds = resolveStoreCategoryIds(dto.getCategory());
        
        return Store.builder()
                .storeId(existing != null ? existing.getStoreId() : null)
                .externalId(dto.getId())
                .name(dto.getName())
                .categoryIds(storeCategoryIds)
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
     * Processor 단계에서는 storeId를 설정하지 않음 (Writer에서 저장 후 설정)
     */
    private List<Food> convertToFoods(CrawledStoreDto dto, Store store) {
        if (dto.getMenus() == null) {
            log.warn("Store has no menus list: {}", dto.getName());
            return new ArrayList<>();
        }
        
        if (dto.getMenus().isEmpty()) {
            log.warn("Store has empty menus list: {}", dto.getName());
            return new ArrayList<>();
        }
        
        List<Food> foods = new ArrayList<>();
        int displayOrder = 0;
        int validCount = 0;
        int invalidCount = 0;
        
        // 가게 카테고리를 CSV 분리 (음식 카테고리 매칭에 사용)
        List<String> storeCategoryNames = splitCategories(dto.getCategory());
        
        log.info("Processing {} menus for store: {} (storeId: {}), store categories: {}", 
                dto.getMenus().size(), dto.getName(), store.getStoreId(), storeCategoryNames);
        
        for (CrawledStoreDto.MenuInfo menuInfo : dto.getMenus()) {
            // 음식별로 적합한 카테고리 결정 (음식 이름 기반 매칭)
            Long foodCategoryId = resolveFoodCategoryForMenu(menuInfo.getName(), storeCategoryNames);
            
            // storeId는 null로 둠 - Writer에서 저장된 storeId를 설정할 예정
            Food food = Food.builder()
                    .foodId(null) // 새로 생성
                    .storeId(null) // Writer에서 실제 storeId로 설정
                    .categoryId(foodCategoryId) // 음식 카테고리
                    .foodName(menuInfo.getName())
                    .description(menuInfo.getIntroduce())
                    .price(menuInfo.getPrice())  // 크롤러용 개별 가격
                    .averagePrice(null) // 추천 시스템용 (배치에서는 설정 안 함)
                    .imageUrl(menuInfo.getImgUrl())
                    .isMain(menuInfo.getIsMain() != null && menuInfo.getIsMain())
                    .displayOrder(displayOrder++)
                    .registeredDt(null) // DB DEFAULT
                    .deletedAt(null)
                    .build();

            // Processor 단계에서는 storeId가 없어도 기본 검증만 수행
            // (foodName, price, categoryId가 있으면 유효한 것으로 봄)
            if (isValidFoodForProcessing(food)) {
                foods.add(food);
                validCount++;
                log.debug("✓ Valid food: name='{}', price={}", 
                         food.getFoodName(), food.getPrice());
            } else {
                invalidCount++;
                log.warn("✗ Invalid food: name='{}', price={}, categoryId={}, reason: {}",
                         food.getFoodName(), food.getPrice(), food.getCategoryId(),
                         getInvalidReasons(food));
            }
        }
        
        log.info("Menu conversion result for store '{}': valid={}, invalid={}, total={}", 
                dto.getName(), validCount, invalidCount, dto.getMenus().size());
        
        return foods;
    }
    
    /**
     * Processor 단계에서 음식이 기본적으로 유효한지 검증
     * (storeId는 아직 설정되지 않았으므로 제외)
     */
    private boolean isValidFoodForProcessing(Food food) {
        return food.getFoodName() != null && !food.getFoodName().trim().isEmpty()
            && (food.getPrice() != null || food.getAveragePrice() != null)
            && (food.getPrice() == null || food.getPrice() >= 0)
            && (food.getAveragePrice() == null || food.getAveragePrice() >= 0)
            && food.getCategoryId() != null;
    }
    
    /**
     * 음식이 유효하지 않은 이유를 분석하는 헬퍼 메서드
     * (Processor 단계에서는 storeId가 없으므로 제외)
     */
    private String getInvalidReasons(Food food) {
        StringBuilder reasons = new StringBuilder();
        
        if (food.getFoodName() == null || food.getFoodName().trim().isEmpty()) {
            reasons.append("[No name]");
        }
        if (food.getPrice() == null && food.getAveragePrice() == null) {
            reasons.append("[No price/averagePrice]");
        }
        if (food.getPrice() != null && food.getPrice() < 0) {
            reasons.append("[Negative price]");
        }
        if (food.getCategoryId() == null) {
            reasons.append("[No categoryId]");
        }
        
        return reasons.length() > 0 ? reasons.toString() : "[Unknown reason]";
    }
    
    /**
     * Food 카테고리 ID 조회/생성
     * 음식의 종류를 나타내는 카테고리 (예: 한식, 중식, 음료 등)
     * Store의 카테고리를 기반으로 Food 카테고리 결정
     */
    /**
     * 음식 이름과 가게 카테고리 리스트를 기반으로 적합한 음식 카테고리 결정
     * 
     * 로직:
     * 1. 가게 카테고리가 1개인 경우 → 해당 카테고리 사용
     * 2. 가게 카테고리가 여러 개인 경우:
     *    - 음식 이름에 카테고리 키워드가 포함되면 해당 카테고리 선택
     *    - 매칭되는 것이 없으면 "알수없음" 카테고리 사용
     * 
     * 예: 가게 카테고리 ["곱창", "막창", "양"], 음식 이름 "곱창전골" → "곱창" 카테고리
     * 
     * @param foodName 음식 이름
     * @param storeCategoryNames 가게 카테고리 리스트 (이미 CSV 분리됨)
     * @return 음식 카테고리 ID
     */
    private Long resolveFoodCategoryForMenu(String foodName, List<String> storeCategoryNames) {
        String selectedCategoryName;
        
        // 카테고리가 없으면 "알수없음"
        if (storeCategoryNames == null || storeCategoryNames.isEmpty()) {
            selectedCategoryName = "알수없음";
            log.debug("No store categories available. Using '알수없음' for food: {}", foodName);
        }
        // 카테고리가 1개면 그대로 사용
        else if (storeCategoryNames.size() == 1) {
            selectedCategoryName = storeCategoryNames.get(0);
            log.debug("Single store category '{}' applied to food: {}", selectedCategoryName, foodName);
        }
        // 카테고리가 여러 개면 음식 이름 기반 매칭
        else {
            selectedCategoryName = matchCategoryByFoodName(foodName, storeCategoryNames);
            log.debug("Matched category '{}' for food '{}' from categories: {}", 
                    selectedCategoryName, foodName, storeCategoryNames);
        }
        
        Long categoryId = resolveCategoryId(selectedCategoryName);
        log.debug("Resolved food category: '{}' → ID: {}", selectedCategoryName, categoryId);
        return categoryId;
    }
    
    /**
     * 음식 이름에서 가게 카테고리 키워드를 찾아 매칭
     * 
     * 예: 
     * - 음식 이름 "곱창전골", 카테고리 ["곱창", "막창", "양"] → "곱창"
     * - 음식 이름 "제육볶음", 카테고리 ["곱창", "막창", "양"] → "알수없음"
     * 
     * @param foodName 음식 이름
     * @param storeCategoryNames 가게 카테고리 리스트
     * @return 매칭된 카테고리명 (매칭 실패 시 "알수없음")
     */
    private String matchCategoryByFoodName(String foodName, List<String> storeCategoryNames) {
        if (foodName == null || foodName.isEmpty()) {
            log.warn("Empty food name. Using '알수없음' category.");
            return "알수없음";
        }
        
        // 음식 이름에 카테고리 키워드가 포함되어 있는지 확인
        for (String categoryName : storeCategoryNames) {
            if (categoryName != null && !categoryName.isEmpty() && foodName.contains(categoryName)) {
                log.debug("✅ Keyword match: '{}' found in food name '{}'", categoryName, foodName);
                return categoryName;
            }
        }
        
        // 매칭 실패
        log.debug("❌ No keyword match for food '{}' in categories: {}. Using '알수없음'", 
                foodName, storeCategoryNames);
        return "알수없음";
    }
    
    /**
     * 이미지 정보 변환
     * Processor 단계에서는 storeId를 설정하지 않음 (Writer에서 저장 후 설정)
     */
    private List<StoreImage> convertToImages(CrawledStoreDto dto, Store store) {
        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            log.debug("Store has no images: {}", dto.getName());
            return new ArrayList<>();
        }
        
        List<StoreImage> images = new ArrayList<>();
        int displayOrder = 0;
        
        log.debug("Processing {} images for store: {}", dto.getImages().size(), dto.getName());
        
        for (String imageUrl : dto.getImages()) {
            // storeId는 null로 둠 - Writer에서 저장된 storeId를 설정할 예정
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                log.warn("Skipping empty image URL for store: {}", dto.getName());
                continue;
            }
            
            StoreImage image = StoreImage.builder()
                    .storeImageId(null)
                    .storeId(null) // Writer에서 실제 storeId로 설정
                    .imageUrl(imageUrl)
                    .isMain(displayOrder == 0) // 첫 번째 이미지를 대표 이미지로
                    .displayOrder(displayOrder++)
                    .build();
            
            images.add(image);
            log.debug("  Image {}: {} (isMain={})", displayOrder, imageUrl.substring(0, Math.min(50, imageUrl.length())) + "...", image.isMain());
        }
        
        log.info("Image conversion result for store '{}': total={}", dto.getName(), images.size());
        return images;
    }
    
    /**
     * 영업시간 정보 변환
     * Processor 단계에서는 storeId를 설정하지 않음 (Writer에서 저장 후 설정)
     */
    private List<StoreOpeningHour> convertToOpeningHours(CrawledStoreDto dto, Store store) {
        if (dto.getOpeningHours() == null || dto.getOpeningHours().isEmpty()) {
            log.debug("Store has no opening hours: {}", dto.getName());
            return new ArrayList<>();
        }
        
        List<StoreOpeningHour> openingHours = new ArrayList<>();
        
        log.debug("Processing {} opening hours for store: {}", dto.getOpeningHours().size(), dto.getName());
        
        for (CrawledStoreDto.OpeningHour oh : dto.getOpeningHours()) {
            DayOfWeek dayOfWeek = convertDayOfWeek(oh.getDayOfWeek());
            if (dayOfWeek == null || oh.getHours() == null) {
                log.warn("Skipping invalid opening hour for store {}: dayOfWeek={}", dto.getName(), oh.getDayOfWeek());
                continue;
            }
            
            StoreOpeningHour openingHour = new StoreOpeningHour(
                    null, // storeOpeningHourId
                    null, // Writer에서 실제 storeId 설정
                    dayOfWeek,
                    oh.getHours().getStartTime(),
                    oh.getHours().getEndTime(),
                    oh.getHours().getBreakStartTime(),
                    oh.getHours().getBreakEndTime(),
                    false // 영업시간이 있으면 휴무일 아님
            );
            
            openingHours.add(openingHour);
            log.debug("  {}: {} ~ {}", dayOfWeek, oh.getHours().getStartTime(), oh.getHours().getEndTime());
        }
        
        log.info("Opening hours conversion result for store '{}': total={}", dto.getName(), openingHours.size());
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
     * Store 카테고리 ID 조회/생성 (여러 개)
     * 매장의 종류를 나타내는 카테고리 (예: 한식, 중식, 카페 등)
     * CSV 형식 카테고리를 분리해서 각각 조회/생성
     * 예: "한식,일식" → [한식_카테고리_ID, 일식_카테고리_ID]
     */
    private List<Long> resolveStoreCategoryIds(String storeCategoryNames) {
        List<Long> categoryIds = new ArrayList<>();
        
        if (storeCategoryNames == null || storeCategoryNames.isEmpty()) {
            // 기본 카테고리 ("기타")를 사용
            categoryIds.add(resolveCategoryId("기타"));
            log.warn("No categories provided. Using default category '기타'.");
            return categoryIds;
        }
        
        // CSV 형식 카테고리 분리
        List<String> categoryNameList = splitCategories(storeCategoryNames);
        
        if (categoryNameList.isEmpty()) {
            // 분리 후 빈 결과면 기본 카테고리 사용
            log.warn("No valid categories found after splitting '{}'. Using default category '기타'.", storeCategoryNames);
            categoryIds.add(resolveCategoryId("기타"));
            return categoryIds;
        }
        
        // 각 카테고리 ID 조회/생성
        for (String categoryName : categoryNameList) {
            Long categoryId = resolveCategoryId(categoryName);
            categoryIds.add(categoryId);
            log.debug("Resolved store category: {} (ID: {})", categoryName, categoryId);
        }
        
        log.info("✅ Successfully resolved store categories: '{}' → {} categories (IDs: {})", 
                storeCategoryNames, categoryNameList.size(), categoryIds);
        
        return categoryIds;
    }
    
    /**
     * CSV 형식의 카테고리 문자열을 개별 카테고리로 분리
     * 
     * 예시:
     * - "한식,일식" → ["한식", "일식"]
     * - "한식, 일식" → ["한식", "일식"] (공백 제거)
     * - "한식" → ["한식"] (단일 카테고리)
     * - "맥주,호프" → ["맥주", "호프"]
     * - "" → [] (빈 문자열)
     * 
     * @param categoriesString CSV 형식의 카테고리 문자열
     * @return 분리된 카테고리 이름 리스트 (공백 제거됨)
     */
    private List<String> splitCategories(String categoriesString) {
        if (categoriesString == null || categoriesString.isBlank()) {
            return new ArrayList<>();
        }
        
        List<String> categories = new ArrayList<>();
        
        // 콤마로 분리
        String[] parts = categoriesString.split(",");
        
        for (String part : parts) {
            String trimmed = part.trim();
            
            // 빈 문자열 제외
            if (trimmed.isEmpty()) {
                continue;
            }
            
            categories.add(trimmed);
            log.debug("Split category: '{}'", trimmed);
        }
        
        if (categories.isEmpty()) {
            log.warn("No valid categories found after splitting: '{}'", categoriesString);
        } else if (categories.size() > 1) {
            log.info("Split {} categories from: '{}'", categories.size(), categoriesString);
        }
        
        return categories;
    }
    
    /**
     * Store 카테고리 ID 조회/생성 (단일)
     * 매장의 종류를 나타내는 카테고리 (예: 한식, 중식, 카페 등)
     * 
     * 주의: 카테고리명에 콤마가 포함되어 있으면 자동으로 분리합니다.
     * 예: "맥주,호프" → ["맥주", "호프"] 로 분리 후 각각 처리
     */
    private Long resolveStoreCategoryId(String storeCategoryName) {
        if (storeCategoryName == null || storeCategoryName.isEmpty()) {
            storeCategoryName = "기타";
        }
        
        // ⚠️ 카테고리명에 콤마가 포함되어 있으면 경고 로그 기록
        // (이는 splitCategories() 메서드가 먼저 분리해야 하는 상황)
        if (storeCategoryName.contains(",")) {
            log.warn("⚠️ Category name contains comma and was not split: '{}'. " +
                    "This should have been split by splitCategories() method. " +
                    "Using first category only.", storeCategoryName);
            
            // 첫 번째 카테고리만 추출
            String firstCategory = storeCategoryName.split(",")[0].trim();
            storeCategoryName = firstCategory.isEmpty() ? "기타" : firstCategory;
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
