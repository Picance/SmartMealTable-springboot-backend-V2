package com.stdev.smartmealtable.storage.db.budget;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMonthlyBudgetJpaEntity is a Querydsl query type for MonthlyBudgetJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMonthlyBudgetJpaEntity extends EntityPathBase<MonthlyBudgetJpaEntity> {

    private static final long serialVersionUID = 1294979901L;

    public static final QMonthlyBudgetJpaEntity monthlyBudgetJpaEntity = new QMonthlyBudgetJpaEntity("monthlyBudgetJpaEntity");

    public final StringPath budgetMonth = createString("budgetMonth");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Long> monthlyBudgetId = createNumber("monthlyBudgetId", Long.class);

    public final NumberPath<Integer> monthlyFoodBudget = createNumber("monthlyFoodBudget", Integer.class);

    public final NumberPath<Integer> monthlyUsedAmount = createNumber("monthlyUsedAmount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QMonthlyBudgetJpaEntity(String variable) {
        super(MonthlyBudgetJpaEntity.class, forVariable(variable));
    }

    public QMonthlyBudgetJpaEntity(Path<? extends MonthlyBudgetJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMonthlyBudgetJpaEntity(PathMetadata metadata) {
        super(MonthlyBudgetJpaEntity.class, metadata);
    }

}

