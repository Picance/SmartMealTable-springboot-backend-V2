package com.stdev.smartmealtable.storage.db.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreImageJpaEntity is a Querydsl query type for StoreImageJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreImageJpaEntity extends EntityPathBase<StoreImageJpaEntity> {

    private static final long serialVersionUID = -317992097L;

    public static final QStoreImageJpaEntity storeImageJpaEntity = new QStoreImageJpaEntity("storeImageJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isMain = createBoolean("isMain");

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final NumberPath<Long> storeImageId = createNumber("storeImageId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QStoreImageJpaEntity(String variable) {
        super(StoreImageJpaEntity.class, forVariable(variable));
    }

    public QStoreImageJpaEntity(Path<? extends StoreImageJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreImageJpaEntity(PathMetadata metadata) {
        super(StoreImageJpaEntity.class, metadata);
    }

}

