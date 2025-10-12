package com.stdev.smartmealtable.storage.db.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreViewHistoryJpaEntity is a Querydsl query type for StoreViewHistoryJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreViewHistoryJpaEntity extends EntityPathBase<StoreViewHistoryJpaEntity> {

    private static final long serialVersionUID = -1029036021L;

    public static final QStoreViewHistoryJpaEntity storeViewHistoryJpaEntity = new QStoreViewHistoryJpaEntity("storeViewHistoryJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final NumberPath<Long> storeViewHistoryId = createNumber("storeViewHistoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> viewedAt = createDateTime("viewedAt", java.time.LocalDateTime.class);

    public QStoreViewHistoryJpaEntity(String variable) {
        super(StoreViewHistoryJpaEntity.class, forVariable(variable));
    }

    public QStoreViewHistoryJpaEntity(Path<? extends StoreViewHistoryJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreViewHistoryJpaEntity(PathMetadata metadata) {
        super(StoreViewHistoryJpaEntity.class, metadata);
    }

}

