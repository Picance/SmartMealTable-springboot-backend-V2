package com.stdev.smartmealtable.storage.db.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMemberAuthenticationJpaEntity is a Querydsl query type for MemberAuthenticationJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberAuthenticationJpaEntity extends EntityPathBase<MemberAuthenticationJpaEntity> {

    private static final long serialVersionUID = -1286193091L;

    public static final QMemberAuthenticationJpaEntity memberAuthenticationJpaEntity = new QMemberAuthenticationJpaEntity("memberAuthenticationJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Integer> failureCount = createNumber("failureCount", Integer.class);

    public final StringPath hashedPassword = createString("hashedPassword");

    public final NumberPath<Long> memberAuthenticationId = createNumber("memberAuthenticationId", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> passwordChangedAt = createDateTime("passwordChangedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> passwordExpiresAt = createDateTime("passwordExpiresAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public QMemberAuthenticationJpaEntity(String variable) {
        super(MemberAuthenticationJpaEntity.class, forVariable(variable));
    }

    public QMemberAuthenticationJpaEntity(Path<? extends MemberAuthenticationJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberAuthenticationJpaEntity(PathMetadata metadata) {
        super(MemberAuthenticationJpaEntity.class, metadata);
    }

}

