package com.stdev.smartmealtable.storage.db.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreOpeningHourJpaEntity is a Querydsl query type for StoreOpeningHourJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreOpeningHourJpaEntity extends EntityPathBase<StoreOpeningHourJpaEntity> {

    private static final long serialVersionUID = -2017114754L;

    public static final QStoreOpeningHourJpaEntity storeOpeningHourJpaEntity = new QStoreOpeningHourJpaEntity("storeOpeningHourJpaEntity");

    public final StringPath breakEndTime = createString("breakEndTime");

    public final StringPath breakStartTime = createString("breakStartTime");

    public final StringPath closeTime = createString("closeTime");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final EnumPath<java.time.DayOfWeek> dayOfWeek = createEnum("dayOfWeek", java.time.DayOfWeek.class);

    public final BooleanPath isHoliday = createBoolean("isHoliday");

    public final StringPath openTime = createString("openTime");

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final NumberPath<Long> storeOpeningHourId = createNumber("storeOpeningHourId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QStoreOpeningHourJpaEntity(String variable) {
        super(StoreOpeningHourJpaEntity.class, forVariable(variable));
    }

    public QStoreOpeningHourJpaEntity(Path<? extends StoreOpeningHourJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreOpeningHourJpaEntity(PathMetadata metadata) {
        super(StoreOpeningHourJpaEntity.class, metadata);
    }

}

