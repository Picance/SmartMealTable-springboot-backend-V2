package com.stdev.smartmealtable.storage.db.budget;

import com.stdev.smartmealtable.domain.budget.DailyBudget;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일일 예산 JPA 엔티티
 */
@Entity
@Table(name = "daily_budget")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyBudgetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long budgetId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "daily_food_budget", nullable = false)
    private Integer dailyFoodBudget;

    @Column(name = "daily_used_amount", nullable = false)
    private Integer dailyUsedAmount = 0;

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
    public static DailyBudgetJpaEntity from(DailyBudget domain) {
        DailyBudgetJpaEntity entity = new DailyBudgetJpaEntity();
        entity.budgetId = domain.getBudgetId();
        entity.memberId = domain.getMemberId();
        entity.dailyFoodBudget = domain.getDailyFoodBudget();
        entity.dailyUsedAmount = domain.getDailyUsedAmount();
        entity.budgetDate = domain.getBudgetDate();
        return entity;
    }

    /**
     * JPA Entity → Domain 변환
     */
    public DailyBudget toDomain() {
        return DailyBudget.reconstitute(
                this.budgetId,
                this.memberId,
                this.dailyFoodBudget,
                this.dailyUsedAmount,
                this.budgetDate
        );
    }

    /**
     * 도메인 변경사항 반영
     */
    public void updateFromDomain(DailyBudget domain) {
        this.dailyFoodBudget = domain.getDailyFoodBudget();
        this.dailyUsedAmount = domain.getDailyUsedAmount();
    }
}
