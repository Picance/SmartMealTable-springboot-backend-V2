package com.stdev.smartmealtable.storage.db.settings.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAppSettingsJpaEntity is a Querydsl query type for AppSettingsJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAppSettingsJpaEntity extends EntityPathBase<AppSettingsJpaEntity> {

    private static final long serialVersionUID = -1197276334L;

    public static final QAppSettingsJpaEntity appSettingsJpaEntity = new QAppSettingsJpaEntity("appSettingsJpaEntity");

    public final BooleanPath allowTracking = createBoolean("allowTracking");

    public final NumberPath<Long> appSettingsId = createNumber("appSettingsId", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public QAppSettingsJpaEntity(String variable) {
        super(AppSettingsJpaEntity.class, forVariable(variable));
    }

    public QAppSettingsJpaEntity(Path<? extends AppSettingsJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAppSettingsJpaEntity(PathMetadata metadata) {
        super(AppSettingsJpaEntity.class, metadata);
    }

}

