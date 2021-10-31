package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ReturnRequestItemEntity;
import com.nasnav.persistence.ReturnShipmentEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReturnShipmentEntity is a Querydsl query type for ReturnShipmentEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QReturnShipmentEntity extends EntityPathBase<ReturnShipmentEntity> {

    private static final long serialVersionUID = 176599194L;

    public static final QReturnShipmentEntity returnShipmentEntity = new QReturnShipmentEntity("returnShipmentEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath externalId = createString("externalId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ReturnRequestItemEntity, QReturnRequestItemEntity> returnRequestItems = this.<ReturnRequestItemEntity, QReturnRequestItemEntity>createList("returnRequestItems", ReturnRequestItemEntity.class, QReturnRequestItemEntity.class, PathInits.DIRECT2);

    public final StringPath shippingServiceId = createString("shippingServiceId");

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final StringPath trackNumber = createString("trackNumber");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QReturnShipmentEntity(String variable) {
        super(ReturnShipmentEntity.class, forVariable(variable));
    }

    public QReturnShipmentEntity(Path<? extends ReturnShipmentEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReturnShipmentEntity(PathMetadata metadata) {
        super(ReturnShipmentEntity.class, metadata);
    }

}

