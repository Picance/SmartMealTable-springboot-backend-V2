package com.stdev.smartmealtable.storage.db.budget;

import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 월별 예산 JPA 엔티티
 */
@Entity
@Table(name = "monthly_budget")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyBudgetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_budget_id")
    private Long monthlyBudgetId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "monthly_food_budget", nullable = false)
    private Integer monthlyFoodBudget;

    @Column(name = "monthly_used_amount", nullable = false)
    private Integer monthlyUsedAmount = 0;

    @Column(name = "budget_month", nullable = false, length = 7)
    private String budgetMonth;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;


    /**
     * Domain → JPA Entity 변환
     */
    public static MonthlyBudgetJpaEntity from(MonthlyBudget domain) {
        MonthlyBudgetJpaEntity entity = new MonthlyBudgetJpaEntity();
        entity.monthlyBudgetId = domain.getMonthlyBudgetId();
        entity.memberId = domain.getMemberId();
        entity.monthlyFoodBudget = domain.getMonthlyFoodBudget();
        entity.monthlyUsedAmount = domain.getMonthlyUsedAmount();
        entity.budgetMonth = domain.getBudgetMonth();
        return entity;
    }

    /**
     * JPA Entity → Domain 변환
     */
    public MonthlyBudget toDomain() {
        return MonthlyBudget.reconstitute(
                this.monthlyBudgetId,
                this.memberId,
                this.monthlyFoodBudget,
                this.monthlyUsedAmount,
                this.budgetMonth
        );
    }

    /**
     * 도메인 변경사항 반영
     */
    public void updateFromDomain(MonthlyBudget domain) {
        this.monthlyFoodBudget = domain.getMonthlyFoodBudget();
        this.monthlyUsedAmount = domain.getMonthlyUsedAmount();
    }
}
