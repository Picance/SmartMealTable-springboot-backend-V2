package com.stdev.smartmealtable.batch.crawler.job.writer;

import com.stdev.smartmealtable.batch.crawler.job.processor.CrawledStoreProcessor.ProcessedStoreData;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import com.stdev.smartmealtable.domain.store.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 처리된 가게 데이터를 DB에 저장하는 Writer
 * Store, Food, StoreImage, StoreOpeningHour를 모두 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreDataWriter implements ItemWriter<ProcessedStoreData> {
    
    private final StoreRepository storeRepository;
    private final FoodRepository foodRepository;
    private final StoreImageRepository storeImageRepository;
    private final StoreOpeningHourRepository openingHourRepository;
    
    @Override
    @Transactional
    public void write(Chunk<? extends ProcessedStoreData> chunk) throws Exception {
        log.info("Writing {} stores to database", chunk.size());
        
        for (ProcessedStoreData data : chunk) {
            try {
                writeStoreData(data);
            } catch (Exception e) {
                log.error("Failed to write store data: {}", data.getStore().getName(), e);
                throw e;
            }
        }
    }
    
    /**
     * 가게 데이터 및 관련 엔티티 저장
     */
    private void writeStoreData(ProcessedStoreData data) {
        // 1. Store 저장 (Upsert)
        Store savedStore = storeRepository.save(data.getStore());
        Long storeId = savedStore.getStoreId();
        
        log.debug("Saved store: {} (ID: {}, externalId: {})", 
                savedStore.getName(), storeId, savedStore.getExternalId());
        
        // 2. 기존 음식, 이미지, 영업시간 삭제 (새로 추가하기 위해)
        deleteExistingRelations(storeId);
        
        // 3. 음식 저장
        saveFoods(storeId, data.getFoods());
        
        // 4. 이미지 저장
        saveImages(storeId, data.getImages());
        
        // 5. 영업시간 저장
        saveOpeningHours(storeId, data.getOpeningHours());
    }
    
    /**
     * 기존 관련 엔티티 삭제
     */
    private void deleteExistingRelations(Long storeId) {
        try {
            foodRepository.deleteByStoreId(storeId);
            storeImageRepository.deleteByStoreId(storeId);
            openingHourRepository.deleteByStoreId(storeId);
            log.debug("Deleted existing relations for store ID: {}", storeId);
        } catch (Exception e) {
            log.warn("Failed to delete existing relations for store ID: {}", storeId, e);
            // 기존 데이터가 없을 수 있으므로 예외 무시하고 계속 진행
        }
    }
    
    /**
     * 음식(메뉴) 저장
     */
    private void saveFoods(Long storeId, java.util.List<Food> foods) {
        if (foods == null || foods.isEmpty()) {
            return;
        }
        
        for (Food food : foods) {
            Food foodToSave = Food.builder()
                    .foodId(null)
                    .storeId(storeId) // 실제 저장된 storeId 설정
                    .categoryId(food.getCategoryId())
                    .foodName(food.getFoodName())
                    .description(food.getDescription())
                    .price(food.getPrice())
                    .imageUrl(food.getImageUrl())
                    .isMain(food.getIsMain() != null && food.getIsMain())
                    .displayOrder(food.getDisplayOrder())
                    .registeredDt(null) // DB DEFAULT
                    .deletedAt(null)
                    .build();
            
            foodRepository.save(foodToSave);
        }
        
        log.debug("Saved {} foods for store ID: {}", foods.size(), storeId);
    }
    
    /**
     * 이미지 저장
     */
    private void saveImages(Long storeId, java.util.List<StoreImage> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        
        for (StoreImage image : images) {
            StoreImage imageToSave = StoreImage.builder()
                    .storeImageId(null)
                    .storeId(storeId) // 실제 저장된 storeId 설정
                    .imageUrl(image.getImageUrl())
                    .isMain(image.isMain())
                    .displayOrder(image.getDisplayOrder())
                    .build();
            
            storeImageRepository.save(imageToSave);
        }
        
        log.debug("Saved {} images for store ID: {}", images.size(), storeId);
    }
    
    /**
     * 영업시간 저장
     */
    private void saveOpeningHours(Long storeId, java.util.List<StoreOpeningHour> openingHours) {
        if (openingHours == null || openingHours.isEmpty()) {
            return;
        }
        
        for (StoreOpeningHour oh : openingHours) {
            StoreOpeningHour ohToSave = new StoreOpeningHour(
                    null,
                    storeId, // 실제 저장된 storeId 설정
                    oh.dayOfWeek(),
                    oh.openTime(),
                    oh.closeTime(),
                    oh.breakStartTime(),
                    oh.breakEndTime(),
                    oh.isHoliday()
            );
            
            openingHourRepository.save(ohToSave);
        }
        
        log.debug("Saved {} opening hours for store ID: {}", openingHours.size(), storeId);
    }
}
