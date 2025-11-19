package com.stdev.smartmealtable.storage.db.food;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodSearchKeywordJpaRepository extends JpaRepository<FoodSearchKeywordJpaEntity, Long> {

    void deleteByFoodId(Long foodId);

    void deleteByStoreId(Long storeId);
}
