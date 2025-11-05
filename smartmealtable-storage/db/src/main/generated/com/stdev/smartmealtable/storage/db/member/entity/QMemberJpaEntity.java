package com.stdev.smartmealtable.storage.db.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMemberJpaEntity is a Querydsl query type for MemberJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberJpaEntity extends EntityPathBase<MemberJpaEntity> {

    private static final long serialVersionUID = 2087409685L;

    public static final QMemberJpaEntity memberJpaEntity = new QMemberJpaEntity("memberJpaEntity");

    public final NumberPath<Long> groupId = createNumber("groupId", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<com.stdev.smartmealtable.domain.member.entity.RecommendationType> recommendationType = createEnum("recommendationType", com.stdev.smartmealtable.domain.member.entity.RecommendationType.class);

    public QMemberJpaEntity(String variable) {
        super(MemberJpaEntity.class, forVariable(variable));
    }

    public QMemberJpaEntity(Path<? extends MemberJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberJpaEntity(PathMetadata metadata) {
        super(MemberJpaEntity.class, metadata);
    }

}

