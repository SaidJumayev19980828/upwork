package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.PaymentRefundEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentRefundEntity is a Querydsl query type for PaymentRefundEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPaymentRefundEntity extends EntityPathBase<PaymentRefundEntity> {

    private static final long serialVersionUID = 403878868L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentRefundEntity paymentRefundEntity = new QPaymentRefundEntity("paymentRefundEntity");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final NumberPath<Integer> currency = createNumber("currency", Integer.class);

    public final DateTimePath<java.util.Date> executed = createDateTime("executed", java.util.Date.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath object = createString("object");

    public final QPaymentEntity paymentEntity;

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final StringPath uid = createString("uid");

    public QPaymentRefundEntity(String variable) {
        this(PaymentRefundEntity.class, forVariable(variable), INITS);
    }

    public QPaymentRefundEntity(Path<? extends PaymentRefundEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentRefundEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentRefundEntity(PathMetadata metadata, PathInits inits) {
        this(PaymentRefundEntity.class, metadata, inits);
    }

    public QPaymentRefundEntity(Class<? extends PaymentRefundEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.paymentEntity = inits.isInitialized("paymentEntity") ? new QPaymentEntity(forProperty("paymentEntity"), inits.get("paymentEntity")) : null;
    }

}

