package com.stdev.smartmealtable.storage.db.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 카테고리 Spring Data JPA Repository
 */
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
    
    /**
     * 카테고리 이름으로 조회
     */
    Optional<CategoryJpaEntity> findByName(String name);
}
