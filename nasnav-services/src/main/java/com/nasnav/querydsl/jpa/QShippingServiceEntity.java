package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShippingServiceEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QShippingServiceEntity is a Querydsl query type for ShippingServiceEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShippingServiceEntity extends EntityPathBase<ShippingServiceEntity> {

    private static final long serialVersionUID = -999962531L;

    public static final QShippingServiceEntity shippingServiceEntity = new QShippingServiceEntity("shippingServiceEntity");

    public final StringPath addtionalParameters = createString("addtionalParameters");

    public final StringPath id = createString("id");

    public final StringPath serviceParameters = createString("serviceParameters");

    public QShippingServiceEntity(String variable) {
        super(ShippingServiceEntity.class, forVariable(variable));
    }

    public QShippingServiceEntity(Path<? extends ShippingServiceEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QShippingServiceEntity(PathMetadata metadata) {
        super(ShippingServiceEntity.class, metadata);
    }

}

