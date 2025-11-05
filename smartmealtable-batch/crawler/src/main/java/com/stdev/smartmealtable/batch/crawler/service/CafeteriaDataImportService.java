package com.stdev.smartmealtable.batch.crawler.service;

import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.MenuData;
import com.stdev.smartmealtable.batch.crawler.domain.CampusCafeteriaData.RestaurantData;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreOpeningHour;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 학식 데이터를 도메인 객체로 변환하여 DB에 저장하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CafeteriaDataImportService {
    
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final FoodRepository foodRepository;
    
    // 영업시간: 평일 10:00 ~ 19:00
    private static final String OPEN_TIME = "10:00:00";
    private static final String CLOSE_TIME = "19:00:00";
    
    /**
     * 크롤링한 학식 데이터를 데이터베이스에 저장합니다.
     */
    @Transactional
    public void importCafeteriaData(List<CampusCafeteriaData> cafeteriaDataList) {
        log.info("학식 데이터 저장 시작: {} 개 건물", cafeteriaDataList.size());
        
        // 카테고리 ID 맵 생성 (카테고리명 -> 카테고리 ID)
        Map<String, Long> categoryMap = loadCategoryMap();
        
        int totalStores = 0;
        int totalFoods = 0;
        
        for (CampusCafeteriaData cafeteriaData : cafeteriaDataList) {
            log.info("건물 '{}' 데이터 처리 시작", cafeteriaData.getBuildingName());
            
            for (RestaurantData restaurantData : cafeteriaData.getRestaurants()) {
                try {
                    // 카테고리 ID 조회
                    Long categoryId = categoryMap.get(restaurantData.getCategoryName());
                    if (categoryId == null) {
                        log.warn("카테고리 '{}' 를 찾을 수 없습니다. 가게 '{}' 스킵", 
                                restaurantData.getCategoryName(), restaurantData.getName());
                        continue;
                    }
                    
                    // 가게 저장 또는 업데이트
                    Store store = saveOrUpdateStore(
                            cafeteriaData, 
                            restaurantData, 
                            categoryId
                    );
                    
                    // 영업시간 저장
                    saveOpeningHours(store.getStoreId());
                    
                    // 메뉴(음식) 저장
                    int foodCount = saveFoods(store.getStoreId(), categoryId, restaurantData.getMenus());
                    
                    totalStores++;
                    totalFoods += foodCount;
                    
                    log.info("가게 '{}' 저장 완료: {} 개 메뉴", restaurantData.getName(), foodCount);
                    
                } catch (Exception e) {
                    log.error("가게 '{}' 저장 중 오류 발생", restaurantData.getName(), e);
                }
            }
        }
        
        log.info("학식 데이터 저장 완료: {} 개 가게, {} 개 메뉴", totalStores, totalFoods);
    }
    
    /**
     * 카테고리 맵 로드 (카테고리명 -> 카테고리 ID)
     */
    private Map<String, Long> loadCategoryMap() {
        Map<String, Long> categoryMap = new HashMap<>();
        
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            categoryMap.put(category.getName(), category.getCategoryId());
        }
        
        log.info("카테고리 맵 로드 완료: {} 개", categoryMap.size());
        return categoryMap;
    }
    
    /**
     * 가게 저장 또는 업데이트
     * 
     * <p>같은 이름과 주소의 가게가 있으면 업데이트, 없으면 새로 생성</p>
     */
    private Store saveOrUpdateStore(
            CampusCafeteriaData cafeteriaData,
            RestaurantData restaurantData,
            Long categoryId
    ) {
        // 평균 가격 계산
        Integer averagePrice = calculateAveragePrice(restaurantData.getMenus());
        
        // 가게명에 건물명 추가 (구분을 위해)
        String storeName = restaurantData.getName() + " (" + cafeteriaData.getBuildingName() + ")";
        
        // 기존 가게 찾기 (이름과 주소로 검색)
        // 실제로는 더 정교한 검색 로직이 필요할 수 있음
        Optional<Store> existingStore = findExistingStore(storeName, cafeteriaData.getAddress());
        
        Store store;
        if (existingStore.isPresent()) {
            // 기존 가게 업데이트
            store = existingStore.get();
            log.info("기존 가게 업데이트: {}", storeName);
            
            // 업데이트된 정보로 새 Store 객체 생성
            store = Store.builder()
                    .storeId(store.getStoreId())
                    .name(storeName)
                    .categoryId(categoryId)
                    .sellerId(store.getSellerId())
                    .address(cafeteriaData.getAddress())
                    .lotNumberAddress(null)
                    .latitude(cafeteriaData.getLatitude())
                    .longitude(cafeteriaData.getLongitude())
                    .phoneNumber(null)
                    .description(cafeteriaData.getBuildingName() + " 학생 식당")
                    .averagePrice(averagePrice)
                    .reviewCount(store.getReviewCount())
                    .viewCount(store.getViewCount())
                    .favoriteCount(store.getFavoriteCount())
                    .storeType(StoreType.CAMPUS_RESTAURANT)
                    .imageUrl(null)
                    .registeredAt(store.getRegisteredAt())
                    .deletedAt(null)
                    .build();
            
        } else {
            // 새 가게 생성
            log.info("새 가게 생성: {}", storeName);
            
            store = Store.builder()
                    .name(storeName)
                    .categoryId(categoryId)
                    .address(cafeteriaData.getAddress())
                    .latitude(cafeteriaData.getLatitude())
                    .longitude(cafeteriaData.getLongitude())
                    .description(cafeteriaData.getBuildingName() + " 학생 식당")
                    .averagePrice(averagePrice)
                    .reviewCount(0)
                    .viewCount(0)
                    .favoriteCount(0)
                    .storeType(StoreType.CAMPUS_RESTAURANT)
                    .registeredAt(LocalDateTime.now())
                    .build();
        }
        
        return storeRepository.save(store);
    }
    
    /**
     * 기존 가게 찾기
     */
    private Optional<Store> findExistingStore(String name, String address) {
        // 간단한 구현: 이름으로만 검색
        // 실제로는 StoreRepository에 findByNameAndAddress 같은 메서드가 필요
        // 여기서는 새로운 가게로 간주
        return Optional.empty();
    }
    
    /**
     * 평균 가격 계산
     */
    private Integer calculateAveragePrice(List<MenuData> menus) {
        if (menus.isEmpty()) {
            return null;
        }
        
        double average = menus.stream()
                .mapToInt(MenuData::getPrice)
                .average()
                .orElse(0.0);
        
        return (int) Math.round(average);
    }
    
    /**
     * 영업시간 저장 (평일 10:00 ~ 19:00)
     */
    private void saveOpeningHours(Long storeId) {
        // 기존 영업시간 삭제
        List<StoreOpeningHour> existingHours = storeRepository.findOpeningHoursByStoreId(storeId);
        for (StoreOpeningHour hour : existingHours) {
            storeRepository.deleteOpeningHourById(hour.storeOpeningHourId());
        }
        
        // 평일 영업시간 저장 (월~금)
        for (DayOfWeek dayOfWeek : List.of(
                DayOfWeek.MONDAY, 
                DayOfWeek.TUESDAY, 
                DayOfWeek.WEDNESDAY, 
                DayOfWeek.THURSDAY, 
                DayOfWeek.FRIDAY
        )) {
            StoreOpeningHour openingHour = new StoreOpeningHour(
                    null, // storeOpeningHourId (null for new)
                    storeId,
                    dayOfWeek,
                    OPEN_TIME,
                    CLOSE_TIME,
                    null, // breakStartTime
                    null, // breakEndTime
                    false // isHoliday
            );
            
            storeRepository.saveOpeningHour(openingHour);
        }
        
        // 주말 휴무 (토, 일)
        for (DayOfWeek dayOfWeek : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            StoreOpeningHour openingHour = new StoreOpeningHour(
                    null, // storeOpeningHourId (null for new)
                    storeId,
                    dayOfWeek,
                    null, // openTime
                    null, // closeTime
                    null, // breakStartTime
                    null, // breakEndTime
                    true // isHoliday
            );
            
            storeRepository.saveOpeningHour(openingHour);
        }
    }
    
    /**
     * 메뉴(음식) 저장
     * 
     * <p>기존 메뉴는 삭제하고 새로 저장합니다.</p>
     */
    private int saveFoods(Long storeId, Long categoryId, List<MenuData> menus) {
        // 기존 음식 논리 삭제
        List<Food> existingFoods = foodRepository.findByStoreId(storeId);
        for (Food existingFood : existingFoods) {
            foodRepository.softDelete(existingFood.getFoodId());
        }
        
        // 새 음식 저장
        int count = 0;
        for (MenuData menu : menus) {
            try {
                Food food = Food.create(
                        menu.getName(),
                        storeId,
                        categoryId,
                        null, // description
                        null, // imageUrl
                        menu.getPrice()
                );
                
                foodRepository.save(food);
                count++;
                
            } catch (Exception e) {
                log.error("메뉴 '{}' 저장 중 오류 발생", menu.getName(), e);
            }
        }
        
        return count;
    }
}

