package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShippingAreaEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShippingAreaEntity is a Querydsl query type for ShippingAreaEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShippingAreaEntity extends EntityPathBase<ShippingAreaEntity> {

    private static final long serialVersionUID = 1497888651L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShippingAreaEntity shippingAreaEntity = new QShippingAreaEntity("shippingAreaEntity");

    public final QAreasEntity area;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath providerId = createString("providerId");

    public final QShippingServiceEntity shippingService;

    public QShippingAreaEntity(String variable) {
        this(ShippingAreaEntity.class, forVariable(variable), INITS);
    }

    public QShippingAreaEntity(Path<? extends ShippingAreaEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShippingAreaEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShippingAreaEntity(PathMetadata metadata, PathInits inits) {
        this(ShippingAreaEntity.class, metadata, inits);
    }

    public QShippingAreaEntity(Class<? extends ShippingAreaEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.area = inits.isInitialized("area") ? new QAreasEntity(forProperty("area"), inits.get("area")) : null;
        this.shippingService = inits.isInitialized("shippingService") ? new QShippingServiceEntity(forProperty("shippingService")) : null;
    }

}

