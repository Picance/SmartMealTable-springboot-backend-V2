package com.stdev.smartmealtable.storage.db.category;

import com.stdev.smartmealtable.domain.category.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 카테고리 JPA Entity
 */
@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA Entity → Domain 변환
     */
    public Category toDomain() {
        return Category.reconstitute(this.categoryId, this.name);
    }
}
