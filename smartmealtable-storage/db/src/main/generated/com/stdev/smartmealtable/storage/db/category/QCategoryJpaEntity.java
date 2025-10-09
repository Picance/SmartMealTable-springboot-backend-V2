package com.stdev.smartmealtable.storage.db.category;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCategoryJpaEntity is a Querydsl query type for CategoryJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategoryJpaEntity extends EntityPathBase<CategoryJpaEntity> {

    private static final long serialVersionUID = -559306776L;

    public static final QCategoryJpaEntity categoryJpaEntity = new QCategoryJpaEntity("categoryJpaEntity");

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QCategoryJpaEntity(String variable) {
        super(CategoryJpaEntity.class, forVariable(variable));
    }

    public QCategoryJpaEntity(Path<? extends CategoryJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCategoryJpaEntity(PathMetadata metadata) {
        super(CategoryJpaEntity.class, metadata);
    }

}

