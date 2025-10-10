package com.stdev.smartmealtable.storage.db.expenditure;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExpenditureItemJpaEntity is a Querydsl query type for ExpenditureItemJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExpenditureItemJpaEntity extends EntityPathBase<ExpenditureItemJpaEntity> {

    private static final long serialVersionUID = -1700336909L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExpenditureItemJpaEntity expenditureItemJpaEntity = new QExpenditureItemJpaEntity("expenditureItemJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QExpenditureJpaEntity expenditure;

    public final StringPath foodName = createString("foodName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QExpenditureItemJpaEntity(String variable) {
        this(ExpenditureItemJpaEntity.class, forVariable(variable), INITS);
    }

    public QExpenditureItemJpaEntity(Path<? extends ExpenditureItemJpaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExpenditureItemJpaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExpenditureItemJpaEntity(PathMetadata metadata, PathInits inits) {
        this(ExpenditureItemJpaEntity.class, metadata, inits);
    }

    public QExpenditureItemJpaEntity(Class<? extends ExpenditureItemJpaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.expenditure = inits.isInitialized("expenditure") ? new QExpenditureJpaEntity(forProperty("expenditure")) : null;
    }

}

