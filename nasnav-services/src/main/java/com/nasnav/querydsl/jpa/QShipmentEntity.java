package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShipmentEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShipmentEntity is a Querydsl query type for ShipmentEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShipmentEntity extends EntityPathBase<ShipmentEntity> {

    private static final long serialVersionUID = 1530601738L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShipmentEntity shipmentEntity = new QShipmentEntity("shipmentEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath externalId = createString("externalId");

    public final DateTimePath<java.time.LocalDateTime> from = createDateTime("from", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath parameters = createString("parameters");

    public final NumberPath<java.math.BigDecimal> shippingFee = createNumber("shippingFee", java.math.BigDecimal.class);

    public final StringPath shippingServiceId = createString("shippingServiceId");

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final QOrdersEntity subOrder;

    public final DateTimePath<java.time.LocalDateTime> to = createDateTime("to", java.time.LocalDateTime.class);

    public final StringPath trackNumber = createString("trackNumber");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QShipmentEntity(String variable) {
        this(ShipmentEntity.class, forVariable(variable), INITS);
    }

    public QShipmentEntity(Path<? extends ShipmentEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShipmentEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShipmentEntity(PathMetadata metadata, PathInits inits) {
        this(ShipmentEntity.class, metadata, inits);
    }

    public QShipmentEntity(Class<? extends ShipmentEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.subOrder = inits.isInitialized("subOrder") ? new QOrdersEntity(forProperty("subOrder"), inits.get("subOrder")) : null;
    }

}

