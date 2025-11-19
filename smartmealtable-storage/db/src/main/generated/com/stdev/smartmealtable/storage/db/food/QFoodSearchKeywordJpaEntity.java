package com.stdev.smartmealtable.storage.db.food;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFoodSearchKeywordJpaEntity is a Querydsl query type for FoodSearchKeywordJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFoodSearchKeywordJpaEntity extends EntityPathBase<FoodSearchKeywordJpaEntity> {

    private static final long serialVersionUID = -1530645133L;

    public static final QFoodSearchKeywordJpaEntity foodSearchKeywordJpaEntity = new QFoodSearchKeywordJpaEntity("foodSearchKeywordJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> foodId = createNumber("foodId", Long.class);

    public final NumberPath<Long> foodSearchKeywordId = createNumber("foodSearchKeywordId", Long.class);

    public final StringPath keyword = createString("keyword");

    public final StringPath keywordPrefix = createString("keywordPrefix");

    public final EnumPath<FoodSearchKeywordType> keywordType = createEnum("keywordType", FoodSearchKeywordType.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QFoodSearchKeywordJpaEntity(String variable) {
        super(FoodSearchKeywordJpaEntity.class, forVariable(variable));
    }

    public QFoodSearchKeywordJpaEntity(Path<? extends FoodSearchKeywordJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFoodSearchKeywordJpaEntity(PathMetadata metadata) {
        super(FoodSearchKeywordJpaEntity.class, metadata);
    }

}

