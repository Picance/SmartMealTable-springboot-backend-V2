package com.stdev.smartmealtable.storage.db.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGroupJpaEntity is a Querydsl query type for GroupJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupJpaEntity extends EntityPathBase<GroupJpaEntity> {

    private static final long serialVersionUID = 1374734286L;

    public static final QGroupJpaEntity groupJpaEntity = new QGroupJpaEntity("groupJpaEntity");

    public final StringPath address = createString("address");

    public final StringPath campusType = createString("campusType");

    public final StringPath establishmentDate = createString("establishmentDate");

    public final StringPath establishmentType = createString("establishmentType");

    public final StringPath faxNumber = createString("faxNumber");

    public final NumberPath<Long> groupId = createNumber("groupId", Long.class);

    public final StringPath jibunAddress = createString("jibunAddress");

    public final StringPath name = createString("name");

    public final StringPath nameEn = createString("nameEn");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath postalCode = createString("postalCode");

    public final StringPath regionCode = createString("regionCode");

    public final StringPath regionName = createString("regionName");

    public final StringPath roadAddress = createString("roadAddress");

    public final StringPath schoolType = createString("schoolType");

    public final EnumPath<com.stdev.smartmealtable.domain.member.entity.GroupType> type = createEnum("type", com.stdev.smartmealtable.domain.member.entity.GroupType.class);

    public final StringPath universityType = createString("universityType");

    public final StringPath website = createString("website");

    public QGroupJpaEntity(String variable) {
        super(GroupJpaEntity.class, forVariable(variable));
    }

    public QGroupJpaEntity(Path<? extends GroupJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGroupJpaEntity(PathMetadata metadata) {
        super(GroupJpaEntity.class, metadata);
    }

}

