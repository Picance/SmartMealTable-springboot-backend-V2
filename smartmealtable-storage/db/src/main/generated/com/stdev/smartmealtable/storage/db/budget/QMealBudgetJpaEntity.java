package com.stdev.smartmealtable.storage.db.budget;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMealBudgetJpaEntity is a Querydsl query type for MealBudgetJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMealBudgetJpaEntity extends EntityPathBase<MealBudgetJpaEntity> {

    private static final long serialVersionUID = -370169851L;

    public static final QMealBudgetJpaEntity mealBudgetJpaEntity = new QMealBudgetJpaEntity("mealBudgetJpaEntity");

    public final DatePath<java.time.LocalDate> budgetDate = createDate("budgetDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> dailyBudgetId = createNumber("dailyBudgetId", Long.class);

    public final NumberPath<Integer> mealBudget = createNumber("mealBudget", Integer.class);

    public final NumberPath<Long> mealBudgetId = createNumber("mealBudgetId", Long.class);

    public final EnumPath<com.stdev.smartmealtable.domain.budget.MealType> mealType = createEnum("mealType", com.stdev.smartmealtable.domain.budget.MealType.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> usedAmount = createNumber("usedAmount", Integer.class);

    public QMealBudgetJpaEntity(String variable) {
        super(MealBudgetJpaEntity.class, forVariable(variable));
    }

    public QMealBudgetJpaEntity(Path<? extends MealBudgetJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMealBudgetJpaEntity(PathMetadata metadata) {
        super(MealBudgetJpaEntity.class, metadata);
    }

}

