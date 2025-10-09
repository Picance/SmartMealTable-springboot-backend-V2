package com.stdev.smartmealtable.domain.budget;

/**
 * 식사 유형을 정의하는 enum
 */
public enum MealType {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    ETC("기타");

    private final String description;

    MealType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
