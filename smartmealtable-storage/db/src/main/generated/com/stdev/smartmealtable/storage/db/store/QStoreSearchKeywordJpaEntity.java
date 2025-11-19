package com.stdev.smartmealtable.storage.db.store;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreSearchKeywordJpaEntity is a Querydsl query type for StoreSearchKeywordJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreSearchKeywordJpaEntity extends EntityPathBase<StoreSearchKeywordJpaEntity> {

    private static final long serialVersionUID = 1604607065L;

    public static final QStoreSearchKeywordJpaEntity storeSearchKeywordJpaEntity = new QStoreSearchKeywordJpaEntity("storeSearchKeywordJpaEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath keyword = createString("keyword");

    public final StringPath keywordPrefix = createString("keywordPrefix");

    public final EnumPath<StoreSearchKeywordType> keywordType = createEnum("keywordType", StoreSearchKeywordType.class);

    public final NumberPath<Long> storeId = createNumber("storeId", Long.class);

    public final NumberPath<Long> storeSearchKeywordId = createNumber("storeSearchKeywordId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QStoreSearchKeywordJpaEntity(String variable) {
        super(StoreSearchKeywordJpaEntity.class, forVariable(variable));
    }

    public QStoreSearchKeywordJpaEntity(Path<? extends StoreSearchKeywordJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreSearchKeywordJpaEntity(PathMetadata metadata) {
        super(StoreSearchKeywordJpaEntity.class, metadata);
    }

}

