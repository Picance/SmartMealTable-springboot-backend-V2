package com.stdev.smartmealtable.storage.db.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreCategoryJpaEntity is a Querydsl query type for StoreCategoryJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreCategoryJpaEntity extends EntityPathBase<StoreCategoryJpaEntity> {

    private static final long serialVersionUID = 1485018084L;

    public static final QStoreCategoryJpaEntity storeCategoryJpaEntity = new QStoreCategoryJpaEntity("storeCategoryJpaEntity");

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final NumberPath<Long> storeCategoryId = createNumber("storeCategoryId", Long.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QStoreCategoryJpaEntity(String variable) {
        super(StoreCategoryJpaEntity.class, forVariable(variable));
    }

    public QStoreCategoryJpaEntity(Path<? extends StoreCategoryJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreCategoryJpaEntity(PathMetadata metadata) {
        super(StoreCategoryJpaEntity.class, metadata);
    }

}

