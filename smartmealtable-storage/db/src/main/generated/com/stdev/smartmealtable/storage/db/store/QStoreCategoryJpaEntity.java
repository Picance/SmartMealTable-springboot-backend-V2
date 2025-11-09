package com.stdev.smartmealtable.storage.db.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreCategoryJpaEntity is a Querydsl query type for StoreCategoryJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreCategoryJpaEntity extends EntityPathBase<StoreCategoryJpaEntity> {

    private static final long serialVersionUID = 1485018084L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreCategoryJpaEntity storeCategoryJpaEntity = new QStoreCategoryJpaEntity("storeCategoryJpaEntity");

    public final com.stdev.smartmealtable.storage.db.category.QCategoryJpaEntity category;

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final QStoreJpaEntity store;

    public final NumberPath<Long> storeCategoryId = createNumber("storeCategoryId", Long.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QStoreCategoryJpaEntity(String variable) {
        this(StoreCategoryJpaEntity.class, forVariable(variable), INITS);
    }

    public QStoreCategoryJpaEntity(Path<? extends StoreCategoryJpaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreCategoryJpaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreCategoryJpaEntity(PathMetadata metadata, PathInits inits) {
        this(StoreCategoryJpaEntity.class, metadata, inits);
    }

    public QStoreCategoryJpaEntity(Class<? extends StoreCategoryJpaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.stdev.smartmealtable.storage.db.category.QCategoryJpaEntity(forProperty("category")) : null;
        this.store = inits.isInitialized("store") ? new QStoreJpaEntity(forProperty("store")) : null;
    }

}

