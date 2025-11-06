package com.stdev.smartmealtable.storage.db.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreJpaEntity is a Querydsl query type for StoreJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreJpaEntity extends EntityPathBase<StoreJpaEntity> {

    private static final long serialVersionUID = 1639339842L;

    public static final QStoreJpaEntity storeJpaEntity = new QStoreJpaEntity("storeJpaEntity");

    public final StringPath address = createString("address");

    public final NumberPath<Integer> averagePrice = createNumber("averagePrice", Integer.class);

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final StringPath externalId = createString("externalId");

    public final NumberPath<Integer> favoriteCount = createNumber("favoriteCount", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final StringPath lotNumberAddress = createString("lotNumberAddress");

    public final StringPath name = createString("name");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> reviewCount = createNumber("reviewCount", Integer.class);

    public final NumberPath<Long> sellerId = createNumber("sellerId", Long.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final EnumPath<com.stdev.smartmealtable.domain.store.StoreType> storeType = createEnum("storeType", com.stdev.smartmealtable.domain.store.StoreType.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QStoreJpaEntity(String variable) {
        super(StoreJpaEntity.class, forVariable(variable));
    }

    public QStoreJpaEntity(Path<? extends StoreJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreJpaEntity(PathMetadata metadata) {
        super(StoreJpaEntity.class, metadata);
    }

}

