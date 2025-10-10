package com.stdev.smartmealtable.storage.db.expenditure;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExpenditureJpaEntity is a Querydsl query type for ExpenditureJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExpenditureJpaEntity extends EntityPathBase<ExpenditureJpaEntity> {

    private static final long serialVersionUID = -299281210L;

    public static final QExpenditureJpaEntity expenditureJpaEntity = new QExpenditureJpaEntity("expenditureJpaEntity");

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath deleted = createBoolean("deleted");

    public final DatePath<java.time.LocalDate> expendedDate = createDate("expendedDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> expendedTime = createTime("expendedTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ExpenditureItemJpaEntity, QExpenditureItemJpaEntity> items = this.<ExpenditureItemJpaEntity, QExpenditureItemJpaEntity>createList("items", ExpenditureItemJpaEntity.class, QExpenditureItemJpaEntity.class, PathInits.DIRECT2);

    public final EnumPath<com.stdev.smartmealtable.domain.expenditure.MealType> mealType = createEnum("mealType", com.stdev.smartmealtable.domain.expenditure.MealType.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final StringPath memo = createString("memo");

    public final StringPath storeName = createString("storeName");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QExpenditureJpaEntity(String variable) {
        super(ExpenditureJpaEntity.class, forVariable(variable));
    }

    public QExpenditureJpaEntity(Path<? extends ExpenditureJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExpenditureJpaEntity(PathMetadata metadata) {
        super(ExpenditureJpaEntity.class, metadata);
    }

}

