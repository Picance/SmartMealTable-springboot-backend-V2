package com.stdev.smartmealtable.storage.db.food;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFoodPreferenceJpaEntity is a Querydsl query type for FoodPreferenceJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFoodPreferenceJpaEntity extends EntityPathBase<FoodPreferenceJpaEntity> {

    private static final long serialVersionUID = 1651118285L;

    public static final QFoodPreferenceJpaEntity foodPreferenceJpaEntity = new QFoodPreferenceJpaEntity("foodPreferenceJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> foodId = createNumber("foodId", Long.class);

    public final NumberPath<Long> foodPreferenceId = createNumber("foodPreferenceId", Long.class);

    public final BooleanPath isPreferred = createBoolean("isPreferred");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> preferredAt = createDateTime("preferredAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QFoodPreferenceJpaEntity(String variable) {
        super(FoodPreferenceJpaEntity.class, forVariable(variable));
    }

    public QFoodPreferenceJpaEntity(Path<? extends FoodPreferenceJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFoodPreferenceJpaEntity(PathMetadata metadata) {
        super(FoodPreferenceJpaEntity.class, metadata);
    }

}

