package com.stdev.smartmealtable.storage.db.member.entity;

import com.stdev.smartmealtable.domain.member.entity.BudgetConfirmAction;
import com.stdev.smartmealtable.domain.member.entity.MonthlyBudgetConfirmation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 월별 예산 확인 이력 JPA 엔티티
 * Domain Entity <-> JPA Entity 변환
 */
@Entity
@Table(
    name = "monthly_budget_confirmation",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_member_year_month",
            columnNames = {"member_id", "year", "month"}
        )
    },
    indexes = {
        @Index(name = "idx_member_id", columnList = "member_id"),
        @Index(name = "idx_confirmed_at", columnList = "confirmed_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyBudgetConfirmationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_budget_confirmation_id")
    private Long monthlyBudgetConfirmationId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private BudgetConfirmAction action;

    @Column(name = "confirmed_at", nullable = false)
    private LocalDateTime confirmedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Domain -> JPA Entity 변환
     */
    public static MonthlyBudgetConfirmationJpaEntity from(MonthlyBudgetConfirmation domain) {
        MonthlyBudgetConfirmationJpaEntity entity = new MonthlyBudgetConfirmationJpaEntity();
        entity.monthlyBudgetConfirmationId = domain.getMonthlyBudgetConfirmationId();
        entity.memberId = domain.getMemberId();
        entity.year = domain.getYear();
        entity.month = domain.getMonth();
        entity.action = domain.getAction();
        entity.confirmedAt = domain.getConfirmedAt();
        return entity;
    }

    /**
     * JPA Entity -> Domain 변환
     */
    public MonthlyBudgetConfirmation toDomain() {
        return MonthlyBudgetConfirmation.reconstitute(
                this.monthlyBudgetConfirmationId,
                this.memberId,
                this.year,
                this.month,
                this.action,
                this.confirmedAt
        );
    }
}
