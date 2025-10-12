package com.stdev.smartmealtable.storage.db.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreTemporaryClosureJpaEntity is a Querydsl query type for StoreTemporaryClosureJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreTemporaryClosureJpaEntity extends EntityPathBase<StoreTemporaryClosureJpaEntity> {

    private static final long serialVersionUID = 2042525624L;

    public static final QStoreTemporaryClosureJpaEntity storeTemporaryClosureJpaEntity = new QStoreTemporaryClosureJpaEntity("storeTemporaryClosureJpaEntity");

    public final DatePath<java.time.LocalDate> closureDate = createDate("closureDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final StringPath reason = createString("reason");

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final NumberPath<Long> storeTemporaryClosureId = createNumber("storeTemporaryClosureId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QStoreTemporaryClosureJpaEntity(String variable) {
        super(StoreTemporaryClosureJpaEntity.class, forVariable(variable));
    }

    public QStoreTemporaryClosureJpaEntity(Path<? extends StoreTemporaryClosureJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreTemporaryClosureJpaEntity(PathMetadata metadata) {
        super(StoreTemporaryClosureJpaEntity.class, metadata);
    }

}

