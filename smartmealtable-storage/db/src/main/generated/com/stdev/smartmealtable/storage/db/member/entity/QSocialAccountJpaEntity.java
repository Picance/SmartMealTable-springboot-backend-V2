package com.stdev.smartmealtable.storage.db.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSocialAccountJpaEntity is a Querydsl query type for SocialAccountJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSocialAccountJpaEntity extends EntityPathBase<SocialAccountJpaEntity> {

    private static final long serialVersionUID = 536935021L;

    public static final QSocialAccountJpaEntity socialAccountJpaEntity = new QSocialAccountJpaEntity("socialAccountJpaEntity");

    public final StringPath accessToken = createString("accessToken");

    public final DateTimePath<java.time.LocalDateTime> connectedAt = createDateTime("connectedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> memberAuthenticationId = createNumber("memberAuthenticationId", Long.class);

    public final EnumPath<com.stdev.smartmealtable.domain.member.entity.SocialProvider> provider = createEnum("provider", com.stdev.smartmealtable.domain.member.entity.SocialProvider.class);

    public final StringPath providerId = createString("providerId");

    public final StringPath refreshToken = createString("refreshToken");

    public final NumberPath<Long> socialAccountId = createNumber("socialAccountId", Long.class);

    public final StringPath tokenType = createString("tokenType");

    public QSocialAccountJpaEntity(String variable) {
        super(SocialAccountJpaEntity.class, forVariable(variable));
    }

    public QSocialAccountJpaEntity(Path<? extends SocialAccountJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSocialAccountJpaEntity(PathMetadata metadata) {
        super(SocialAccountJpaEntity.class, metadata);
    }

}

