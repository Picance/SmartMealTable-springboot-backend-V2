package com.stdev.smartmealtable.storage.db.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMonthlyBudgetConfirmationJpaEntity is a Querydsl query type for MonthlyBudgetConfirmationJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMonthlyBudgetConfirmationJpaEntity extends EntityPathBase<MonthlyBudgetConfirmationJpaEntity> {

    private static final long serialVersionUID = 59766470L;

    public static final QMonthlyBudgetConfirmationJpaEntity monthlyBudgetConfirmationJpaEntity = new QMonthlyBudgetConfirmationJpaEntity("monthlyBudgetConfirmationJpaEntity");

    public final EnumPath<com.stdev.smartmealtable.domain.member.entity.BudgetConfirmAction> action = createEnum("action", com.stdev.smartmealtable.domain.member.entity.BudgetConfirmAction.class);

    public final DateTimePath<java.time.LocalDateTime> confirmedAt = createDateTime("confirmedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Integer> month = createNumber("month", Integer.class);

    public final NumberPath<Long> monthlyBudgetConfirmationId = createNumber("monthlyBudgetConfirmationId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QMonthlyBudgetConfirmationJpaEntity(String variable) {
        super(MonthlyBudgetConfirmationJpaEntity.class, forVariable(variable));
    }

    public QMonthlyBudgetConfirmationJpaEntity(Path<? extends MonthlyBudgetConfirmationJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMonthlyBudgetConfirmationJpaEntity(PathMetadata metadata) {
        super(MonthlyBudgetConfirmationJpaEntity.class, metadata);
    }

}

