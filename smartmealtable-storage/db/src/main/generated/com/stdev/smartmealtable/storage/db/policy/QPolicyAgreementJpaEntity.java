package com.stdev.smartmealtable.storage.db.policy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPolicyAgreementJpaEntity is a Querydsl query type for PolicyAgreementJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPolicyAgreementJpaEntity extends EntityPathBase<PolicyAgreementJpaEntity> {

    private static final long serialVersionUID = 1760780586L;

    public static final QPolicyAgreementJpaEntity policyAgreementJpaEntity = new QPolicyAgreementJpaEntity("policyAgreementJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> agreedAt = createDateTime("agreedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final BooleanPath isAgreed = createBoolean("isAgreed");

    public final NumberPath<Long> memberAuthenticationId = createNumber("memberAuthenticationId", Long.class);

    public final NumberPath<Long> policyAgreementId = createNumber("policyAgreementId", Long.class);

    public final NumberPath<Long> policyId = createNumber("policyId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QPolicyAgreementJpaEntity(String variable) {
        super(PolicyAgreementJpaEntity.class, forVariable(variable));
    }

    public QPolicyAgreementJpaEntity(Path<? extends PolicyAgreementJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPolicyAgreementJpaEntity(PathMetadata metadata) {
        super(PolicyAgreementJpaEntity.class, metadata);
    }

}

