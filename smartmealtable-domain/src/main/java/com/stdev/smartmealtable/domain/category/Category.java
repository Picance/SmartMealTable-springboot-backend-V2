package com.stdev.smartmealtable.domain.category;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음식 카테고리 도메인 엔티티 (예: 한식, 중식, 일식)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    private Long categoryId;
    private String name;

    /**
     * JPA Entity에서 Domain Entity로 변환 시 사용 (reconstitute 패턴)
     */
    public static Category reconstitute(Long categoryId, String name) {
        Category category = new Category();
        category.categoryId = categoryId;
        category.name = name;
        return category;
    }

    /**
     * 새로운 카테고리 생성 (테스트 및 초기화 시 사용)
     */
    public static Category create(String name) {
        Category category = new Category();
        category.name = name;
        return category;
    }
}
