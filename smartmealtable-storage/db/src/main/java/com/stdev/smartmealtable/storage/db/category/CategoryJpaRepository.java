package com.stdev.smartmealtable.storage.db.category;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 카테고리 Spring Data JPA Repository
 */
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
}
