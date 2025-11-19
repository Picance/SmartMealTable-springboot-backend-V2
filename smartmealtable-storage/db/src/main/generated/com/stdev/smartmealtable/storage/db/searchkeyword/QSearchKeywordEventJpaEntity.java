package com.stdev.smartmealtable.storage.db.searchkeyword;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSearchKeywordEventJpaEntity is a Querydsl query type for SearchKeywordEventJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchKeywordEventJpaEntity extends EntityPathBase<SearchKeywordEventJpaEntity> {

    private static final long serialVersionUID = -1788089696L;

    public static final QSearchKeywordEventJpaEntity searchKeywordEventJpaEntity = new QSearchKeywordEventJpaEntity("searchKeywordEventJpaEntity");

    public final NumberPath<Long> clickedFoodId = createNumber("clickedFoodId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final StringPath normalizedKeyword = createString("normalizedKeyword");

    public final StringPath rawKeyword = createString("rawKeyword");

    public final NumberPath<Long> searchKeywordEventId = createNumber("searchKeywordEventId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QSearchKeywordEventJpaEntity(String variable) {
        super(SearchKeywordEventJpaEntity.class, forVariable(variable));
    }

    public QSearchKeywordEventJpaEntity(Path<? extends SearchKeywordEventJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSearchKeywordEventJpaEntity(PathMetadata metadata) {
        super(SearchKeywordEventJpaEntity.class, metadata);
    }

}

