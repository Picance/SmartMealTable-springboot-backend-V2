package com.stdev.smartmealtable.storage.db.budget;

import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 식사 예산 JPA 엔티티
 */
@Entity
@Table(name = "meal_budget")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealBudgetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_budget_id")
    private Long mealBudgetId;

    @Column(name = "daily_budget_id", nullable = false)
    private Long dailyBudgetId;

    @Column(name = "meal_budget", nullable = false)
    private Integer mealBudget;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(name = "used_amount", nullable = false)
    private Integer usedAmount = 0;

    @Column(name = "budget_date", nullable = false)
    private LocalDate budgetDate;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Domain → JPA Entity 변환
     */
    public static MealBudgetJpaEntity from(MealBudget domain) {
        MealBudgetJpaEntity entity = new MealBudgetJpaEntity();
        entity.mealBudgetId = domain.getMealBudgetId();
        entity.dailyBudgetId = domain.getDailyBudgetId();
        entity.mealBudget = domain.getMealBudget();
        entity.mealType = domain.getMealType();
        entity.usedAmount = domain.getUsedAmount();
        entity.budgetDate = domain.getBudgetDate();
        return entity;
    }

    /**
     * JPA Entity → Domain 변환
     */
    public MealBudget toDomain() {
        return MealBudget.reconstitute(
                this.mealBudgetId,
                this.dailyBudgetId,
                this.mealBudget,
                this.mealType,
                this.usedAmount,
                this.budgetDate
        );
    }

    /**
     * 도메인 변경사항 반영
     */
    public void updateFromDomain(MealBudget domain) {
        this.mealBudget = domain.getMealBudget();
        this.usedAmount = domain.getUsedAmount();
    }
}
