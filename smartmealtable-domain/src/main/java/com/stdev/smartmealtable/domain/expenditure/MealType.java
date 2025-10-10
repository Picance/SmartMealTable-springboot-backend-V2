package com.stdev.smartmealtable.domain.expenditure;

/**
 * 식사 시간대를 나타내는 Enum
 */
public enum MealType {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    OTHER("기타");

    private final String description;

    MealType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
