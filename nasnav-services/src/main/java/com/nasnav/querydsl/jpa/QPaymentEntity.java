package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.PaymentEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentEntity is a Querydsl query type for PaymentEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPaymentEntity extends EntityPathBase<PaymentEntity> {

    private static final long serialVersionUID = -1177724676L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentEntity paymentEntity = new QPaymentEntity("paymentEntity");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final EnumPath<com.nasnav.enumerations.TransactionCurrency> currency = createEnum("currency", com.nasnav.enumerations.TransactionCurrency.class);

    public final DateTimePath<java.util.Date> executed = createDateTime("executed", java.util.Date.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> metaOrderId = createNumber("metaOrderId", Long.class);

    public final StringPath object = createString("object");

    public final StringPath operator = createString("operator");

    public final QOrdersEntity ordersEntity;

    public final NumberPath<Integer> orgPaymentId = createNumber("orgPaymentId", Integer.class);

    public final StringPath sessionId = createString("sessionId");

    public final EnumPath<com.nasnav.enumerations.PaymentStatus> status = createEnum("status", com.nasnav.enumerations.PaymentStatus.class);

    public final StringPath uid = createString("uid");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPaymentEntity(String variable) {
        this(PaymentEntity.class, forVariable(variable), INITS);
    }

    public QPaymentEntity(Path<? extends PaymentEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentEntity(PathMetadata metadata, PathInits inits) {
        this(PaymentEntity.class, metadata, inits);
    }

    public QPaymentEntity(Class<? extends PaymentEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.ordersEntity = inits.isInitialized("ordersEntity") ? new QOrdersEntity(forProperty("ordersEntity"), inits.get("ordersEntity")) : null;
    }

}

