package com.stdev.smartmealtable.admin.food.service;

import com.stdev.smartmealtable.admin.food.service.dto.*;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodPageResult;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.stdev.smartmealtable.core.error.ErrorType.*;

/**
 * 음식 관리 Application Service (ADMIN)
 * 
 * <p>트랜잭션 관리와 유즈케이스에 집중합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FoodApplicationService {

    private final FoodRepository foodRepository;

    /**
     * 음식 목록 조회 (페이징)
     */
    public FoodListServiceResponse getFoods(FoodListServiceRequest request) {
        log.info("[ADMIN] 음식 목록 조회 - categoryId: {}, storeId: {}, name: {}, page: {}, size: {}", 
                request.categoryId(), request.storeId(), request.name(), request.page(), request.size());
        
        FoodPageResult pageResult = foodRepository.adminSearch(
                request.categoryId(),
                request.storeId(),
                request.name(),
                request.page(),
                request.size()
        );
        
        log.info("[ADMIN] 음식 목록 조회 완료 - 총 {}개, {}페이지", 
                pageResult.totalElements(), pageResult.totalPages());
        
        return FoodListServiceResponse.from(pageResult);
    }

    /**
     * 음식 상세 조회
     */
    public FoodServiceResponse getFood(Long foodId) {
        log.info("[ADMIN] 음식 상세 조회 - foodId: {}", foodId);
        
        Food food = foodRepository.findByIdAndDeletedAtIsNull(foodId)
                .orElseThrow(() -> new BusinessException(FOOD_NOT_FOUND));
        
        log.info("[ADMIN] 음식 상세 조회 완료 - name: {}", food.getFoodName());
        
        return FoodServiceResponse.from(food);
    }

    /**
     * 음식 생성 - v2.0
     * 
     * <p>isMain, displayOrder 필드를 포함합니다.</p>
     */
    @Transactional
    public FoodServiceResponse createFood(CreateFoodServiceRequest request) {
        log.info("[ADMIN] 음식 생성 요청 - name: {}, storeId: {}, isMain: {}, displayOrder: {}", 
                request.foodName(), request.storeId(), request.isMain(), request.displayOrder());
        
        Food food = Food.create(
                request.foodName(),
                request.storeId(),
                request.categoryId(),
                request.description(),
                request.imageUrl(),
                request.averagePrice(),
                request.isMain(),
                request.displayOrder()
        );
        
        Food savedFood = foodRepository.save(food);
        
        log.info("[ADMIN] 음식 생성 완료 - foodId: {}, name: {}, isMain: {}, displayOrder: {}", 
                savedFood.getFoodId(), savedFood.getFoodName(), savedFood.getIsMain(), savedFood.getDisplayOrder());
        
        return FoodServiceResponse.from(savedFood);
    }

    /**
     * 음식 수정 - v2.0
     * 
     * <p>isMain, displayOrder 필드를 포함합니다.</p>
     */
    @Transactional
    public FoodServiceResponse updateFood(Long foodId, UpdateFoodServiceRequest request) {
        log.info("[ADMIN] 음식 수정 요청 - foodId: {}, name: {}, isMain: {}, displayOrder: {}", 
                foodId, request.foodName(), request.isMain(), request.displayOrder());
        
        Food existingFood = foodRepository.findByIdAndDeletedAtIsNull(foodId)
                .orElseThrow(() -> new BusinessException(FOOD_NOT_FOUND));
        
        // Food 엔티티는 불변이므로 새로운 객체 생성
        Food updatedFood = Food.reconstituteWithMainAndOrder(
                existingFood.getFoodId(),
                request.foodName(),
                existingFood.getStoreId(), // storeId는 수정하지 않음
                request.categoryId(),
                request.description(),
                request.imageUrl(),
                request.averagePrice(),
                request.isMain(),
                request.displayOrder()
        );
        
        Food savedFood = foodRepository.save(updatedFood);
        
        log.info("[ADMIN] 음식 수정 완료 - foodId: {}, name: {}, isMain: {}, displayOrder: {}", 
                savedFood.getFoodId(), savedFood.getFoodName(), savedFood.getIsMain(), savedFood.getDisplayOrder());
        
        return FoodServiceResponse.from(savedFood);
    }

    /**
     * 음식 삭제 (논리적 삭제)
     */
    @Transactional
    public void deleteFood(Long foodId) {
        log.info("[ADMIN] 음식 삭제 요청 - foodId: {}", foodId);
        
        Food food = foodRepository.findByIdAndDeletedAtIsNull(foodId)
                .orElseThrow(() -> new BusinessException(FOOD_NOT_FOUND));
        
        // TODO: 삭제 전 참조 체크 (favorites, cart_items, expenditure_items)
        // 현재는 soft delete만 수행
        
        foodRepository.softDelete(foodId);
        
        log.info("[ADMIN] 음식 삭제 완료 - foodId: {}, name: {}", 
                food.getFoodId(), food.getFoodName());
    }
}
