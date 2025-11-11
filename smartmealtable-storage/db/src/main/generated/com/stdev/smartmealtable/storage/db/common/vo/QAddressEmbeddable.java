package com.stdev.smartmealtable.storage.db.common.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAddressEmbeddable is a Querydsl query type for AddressEmbeddable
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QAddressEmbeddable extends BeanPath<AddressEmbeddable> {

    private static final long serialVersionUID = -51123729L;

    public static final QAddressEmbeddable addressEmbeddable = new QAddressEmbeddable("addressEmbeddable");

    public final EnumPath<com.stdev.smartmealtable.domain.common.vo.AddressType> addressType = createEnum("addressType", com.stdev.smartmealtable.domain.common.vo.AddressType.class);

    public final StringPath alias = createString("alias");

    public final StringPath detailedAddress = createString("detailedAddress");

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath lotNumberAddress = createString("lotNumberAddress");

    public final StringPath streetNameAddress = createString("streetNameAddress");

    public QAddressEmbeddable(String variable) {
        super(AddressEmbeddable.class, forVariable(variable));
    }

    public QAddressEmbeddable(Path<? extends AddressEmbeddable> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAddressEmbeddable(PathMetadata metadata) {
        super(AddressEmbeddable.class, metadata);
    }

}

