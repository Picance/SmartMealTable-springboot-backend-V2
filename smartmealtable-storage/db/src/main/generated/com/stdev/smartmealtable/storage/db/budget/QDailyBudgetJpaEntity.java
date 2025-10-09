package com.stdev.smartmealtable.storage.db.budget;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDailyBudgetJpaEntity is a Querydsl query type for DailyBudgetJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDailyBudgetJpaEntity extends EntityPathBase<DailyBudgetJpaEntity> {

    private static final long serialVersionUID = -502873103L;

    public static final QDailyBudgetJpaEntity dailyBudgetJpaEntity = new QDailyBudgetJpaEntity("dailyBudgetJpaEntity");

    public final DatePath<java.time.LocalDate> budgetDate = createDate("budgetDate", java.time.LocalDate.class);

    public final NumberPath<Long> budgetId = createNumber("budgetId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> dailyFoodBudget = createNumber("dailyFoodBudget", Integer.class);

    public final NumberPath<Integer> dailyUsedAmount = createNumber("dailyUsedAmount", Integer.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QDailyBudgetJpaEntity(String variable) {
        super(DailyBudgetJpaEntity.class, forVariable(variable));
    }

    public QDailyBudgetJpaEntity(Path<? extends DailyBudgetJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDailyBudgetJpaEntity(PathMetadata metadata) {
        super(DailyBudgetJpaEntity.class, metadata);
    }

}

