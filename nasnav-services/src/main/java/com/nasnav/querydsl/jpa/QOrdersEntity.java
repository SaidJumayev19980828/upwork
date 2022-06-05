package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.OrdersEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrdersEntity is a Querydsl query type for OrdersEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrdersEntity extends EntityPathBase<OrdersEntity> {

    private static final long serialVersionUID = 218596693L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrdersEntity ordersEntity = new QOrdersEntity("ordersEntity");

    public final QAddressesEntity addressEntity;

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final SetPath<BasketsEntity, QBasketsEntity> basketsEntity = this.<BasketsEntity, QBasketsEntity>createSet("basketsEntity", BasketsEntity.class, QBasketsEntity.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> creationDate = createDateTime("creationDate", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deliveryDate = createDateTime("deliveryDate", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> discounts = createNumber("discounts", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMetaOrderEntity metaOrder;

    public final StringPath name = createString("name");

    public final QOrganizationEntity organizationEntity;

    public final QPaymentEntity paymentEntity;

    public final NumberPath<Integer> paymentStatus = createNumber("paymentStatus", Integer.class);

    public final QShipmentEntity shipment;

    public final QShopsEntity shopsEntity;

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<java.math.BigDecimal> total = createNumber("total", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> updateDate = createDateTime("updateDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QOrdersEntity(String variable) {
        this(OrdersEntity.class, forVariable(variable), INITS);
    }

    public QOrdersEntity(Path<? extends OrdersEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrdersEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrdersEntity(PathMetadata metadata, PathInits inits) {
        this(OrdersEntity.class, metadata, inits);
    }

    public QOrdersEntity(Class<? extends OrdersEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.addressEntity = inits.isInitialized("addressEntity") ? new QAddressesEntity(forProperty("addressEntity"), inits.get("addressEntity")) : null;
        this.metaOrder = inits.isInitialized("metaOrder") ? new QMetaOrderEntity(forProperty("metaOrder"), inits.get("metaOrder")) : null;
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
        this.paymentEntity = inits.isInitialized("paymentEntity") ? new QPaymentEntity(forProperty("paymentEntity"), inits.get("paymentEntity")) : null;
        this.shipment = inits.isInitialized("shipment") ? new QShipmentEntity(forProperty("shipment"), inits.get("shipment")) : null;
        this.shopsEntity = inits.isInitialized("shopsEntity") ? new QShopsEntity(forProperty("shopsEntity"), inits.get("shopsEntity")) : null;
    }

}

