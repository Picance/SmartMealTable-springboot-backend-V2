package com.stdev.smartmealtable.storage.db.preference;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPreferenceJpaEntity is a Querydsl query type for PreferenceJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPreferenceJpaEntity extends EntityPathBase<PreferenceJpaEntity> {

    private static final long serialVersionUID = -1894545400L;

    public static final QPreferenceJpaEntity preferenceJpaEntity = new QPreferenceJpaEntity("preferenceJpaEntity");

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Long> preferenceId = createNumber("preferenceId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> weight = createNumber("weight", Integer.class);

    public QPreferenceJpaEntity(String variable) {
        super(PreferenceJpaEntity.class, forVariable(variable));
    }

    public QPreferenceJpaEntity(Path<? extends PreferenceJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPreferenceJpaEntity(PathMetadata metadata) {
        super(PreferenceJpaEntity.class, metadata);
    }

}

