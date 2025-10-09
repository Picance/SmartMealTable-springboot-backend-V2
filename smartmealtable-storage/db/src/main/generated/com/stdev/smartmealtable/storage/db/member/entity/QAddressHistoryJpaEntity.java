package com.stdev.smartmealtable.storage.db.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAddressHistoryJpaEntity is a Querydsl query type for AddressHistoryJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAddressHistoryJpaEntity extends EntityPathBase<AddressHistoryJpaEntity> {

    private static final long serialVersionUID = -1576300945L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAddressHistoryJpaEntity addressHistoryJpaEntity = new QAddressHistoryJpaEntity("addressHistoryJpaEntity");

    public final com.stdev.smartmealtable.storage.db.common.vo.QAddressEmbeddable address;

    public final NumberPath<Long> addressHistoryId = createNumber("addressHistoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath isPrimary = createBoolean("isPrimary");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QAddressHistoryJpaEntity(String variable) {
        this(AddressHistoryJpaEntity.class, forVariable(variable), INITS);
    }

    public QAddressHistoryJpaEntity(Path<? extends AddressHistoryJpaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAddressHistoryJpaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAddressHistoryJpaEntity(PathMetadata metadata, PathInits inits) {
        this(AddressHistoryJpaEntity.class, metadata, inits);
    }

    public QAddressHistoryJpaEntity(Class<? extends AddressHistoryJpaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new com.stdev.smartmealtable.storage.db.common.vo.QAddressEmbeddable(forProperty("address")) : null;
    }

}

