package com.stdev.smartmealtable.storage.db.food;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFoodJpaEntity is a Querydsl query type for FoodJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFoodJpaEntity extends EntityPathBase<FoodJpaEntity> {

    private static final long serialVersionUID = -737559576L;

    public static final QFoodJpaEntity foodJpaEntity = new QFoodJpaEntity("foodJpaEntity");

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final NumberPath<Long> foodId = createNumber("foodId", Long.class);

    public final StringPath foodName = createString("foodName");

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isMain = createBoolean("isMain");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> registeredDt = createDateTime("registeredDt", java.time.LocalDateTime.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QFoodJpaEntity(String variable) {
        super(FoodJpaEntity.class, forVariable(variable));
    }

    public QFoodJpaEntity(Path<? extends FoodJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFoodJpaEntity(PathMetadata metadata) {
        super(FoodJpaEntity.class, metadata);
    }

}

