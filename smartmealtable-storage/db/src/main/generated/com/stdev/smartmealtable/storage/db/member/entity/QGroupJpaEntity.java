package com.stdev.smartmealtable.storage.db.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupJpaEntity is a Querydsl query type for GroupJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupJpaEntity extends EntityPathBase<GroupJpaEntity> {

    private static final long serialVersionUID = 1374734286L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupJpaEntity groupJpaEntity = new QGroupJpaEntity("groupJpaEntity");

    public final com.stdev.smartmealtable.storage.db.common.vo.QAddressEmbeddable address;

    public final StringPath campusType = createString("campusType");

    public final StringPath establishmentDate = createString("establishmentDate");

    public final StringPath establishmentType = createString("establishmentType");

    public final StringPath faxNumber = createString("faxNumber");

    public final NumberPath<Long> groupId = createNumber("groupId", Long.class);

    public final StringPath name = createString("name");

    public final StringPath nameEn = createString("nameEn");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath postalCode = createString("postalCode");

    public final StringPath regionCode = createString("regionCode");

    public final StringPath regionName = createString("regionName");

    public final StringPath schoolType = createString("schoolType");

    public final EnumPath<com.stdev.smartmealtable.domain.member.entity.GroupType> type = createEnum("type", com.stdev.smartmealtable.domain.member.entity.GroupType.class);

    public final StringPath universityType = createString("universityType");

    public final StringPath website = createString("website");

    public QGroupJpaEntity(String variable) {
        this(GroupJpaEntity.class, forVariable(variable), INITS);
    }

    public QGroupJpaEntity(Path<? extends GroupJpaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupJpaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupJpaEntity(PathMetadata metadata, PathInits inits) {
        this(GroupJpaEntity.class, metadata, inits);
    }

    public QGroupJpaEntity(Class<? extends GroupJpaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new com.stdev.smartmealtable.storage.db.common.vo.QAddressEmbeddable(forProperty("address")) : null;
    }

}

