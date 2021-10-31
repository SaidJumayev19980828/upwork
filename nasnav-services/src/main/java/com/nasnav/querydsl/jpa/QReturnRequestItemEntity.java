package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ReturnRequestItemEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReturnRequestItemEntity is a Querydsl query type for ReturnRequestItemEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QReturnRequestItemEntity extends EntityPathBase<ReturnRequestItemEntity> {

    private static final long serialVersionUID = -2129813208L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReturnRequestItemEntity returnRequestItemEntity = new QReturnRequestItemEntity("returnRequestItemEntity");

    public final QBasketsEntity basket;

    public final QEmployeeUserEntity createdByEmployee;

    public final QUserEntity createdByUser;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QEmployeeUserEntity receivedBy;

    public final DateTimePath<java.time.LocalDateTime> receivedOn = createDateTime("receivedOn", java.time.LocalDateTime.class);

    public final NumberPath<Integer> receivedQuantity = createNumber("receivedQuantity", Integer.class);

    public final NumberPath<Integer> returnedQuantity = createNumber("returnedQuantity", Integer.class);

    public final QReturnRequestEntity returnRequest;

    public final QReturnShipmentEntity returnShipment;

    public QReturnRequestItemEntity(String variable) {
        this(ReturnRequestItemEntity.class, forVariable(variable), INITS);
    }

    public QReturnRequestItemEntity(Path<? extends ReturnRequestItemEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReturnRequestItemEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReturnRequestItemEntity(PathMetadata metadata, PathInits inits) {
        this(ReturnRequestItemEntity.class, metadata, inits);
    }

    public QReturnRequestItemEntity(Class<? extends ReturnRequestItemEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.basket = inits.isInitialized("basket") ? new QBasketsEntity(forProperty("basket"), inits.get("basket")) : null;
        this.createdByEmployee = inits.isInitialized("createdByEmployee") ? new QEmployeeUserEntity(forProperty("createdByEmployee")) : null;
        this.createdByUser = inits.isInitialized("createdByUser") ? new QUserEntity(forProperty("createdByUser")) : null;
        this.receivedBy = inits.isInitialized("receivedBy") ? new QEmployeeUserEntity(forProperty("receivedBy")) : null;
        this.returnRequest = inits.isInitialized("returnRequest") ? new QReturnRequestEntity(forProperty("returnRequest"), inits.get("returnRequest")) : null;
        this.returnShipment = inits.isInitialized("returnShipment") ? new QReturnShipmentEntity(forProperty("returnShipment")) : null;
    }

}

