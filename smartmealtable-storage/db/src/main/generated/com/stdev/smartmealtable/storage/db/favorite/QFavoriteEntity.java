package com.stdev.smartmealtable.storage.db.favorite;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFavoriteEntity is a Querydsl query type for FavoriteEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFavoriteEntity extends EntityPathBase<FavoriteEntity> {

    private static final long serialVersionUID = 1005882777L;

    public static final QFavoriteEntity favoriteEntity = new QFavoriteEntity("favoriteEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> favoritedAt = createDateTime("favoritedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> favoriteId = createNumber("favoriteId", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Long> priority = createNumber("priority", Long.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QFavoriteEntity(String variable) {
        super(FavoriteEntity.class, forVariable(variable));
    }

    public QFavoriteEntity(Path<? extends FavoriteEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFavoriteEntity(PathMetadata metadata) {
        super(FavoriteEntity.class, metadata);
    }

}

