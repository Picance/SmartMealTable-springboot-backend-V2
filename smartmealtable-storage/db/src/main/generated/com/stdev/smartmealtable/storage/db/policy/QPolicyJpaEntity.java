package com.stdev.smartmealtable.storage.db.policy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPolicyJpaEntity is a Querydsl query type for PolicyJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPolicyJpaEntity extends EntityPathBase<PolicyJpaEntity> {

    private static final long serialVersionUID = -1368796568L;

    public static final QPolicyJpaEntity policyJpaEntity = new QPolicyJpaEntity("policyJpaEntity");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final BooleanPath isMandatory = createBoolean("isMandatory");

    public final NumberPath<Long> policyId = createNumber("policyId", Long.class);

    public final StringPath title = createString("title");

    public final EnumPath<com.stdev.smartmealtable.domain.policy.entity.PolicyType> type = createEnum("type", com.stdev.smartmealtable.domain.policy.entity.PolicyType.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath version = createString("version");

    public QPolicyJpaEntity(String variable) {
        super(PolicyJpaEntity.class, forVariable(variable));
    }

    public QPolicyJpaEntity(Path<? extends PolicyJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPolicyJpaEntity(PathMetadata metadata) {
        super(PolicyJpaEntity.class, metadata);
    }

}

