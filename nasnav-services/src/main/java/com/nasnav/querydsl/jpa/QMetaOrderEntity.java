package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.PromotionsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMetaOrderEntity is a Querydsl query type for MetaOrderEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QMetaOrderEntity extends EntityPathBase<MetaOrderEntity> {

    private static final long serialVersionUID = 837129279L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMetaOrderEntity metaOrderEntity = new QMetaOrderEntity("metaOrderEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> discounts = createNumber("discounts", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> grandTotal = createNumber("grandTotal", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath notes = createString("notes");

    public final QOrganizationEntity organization;

    public final SetPath<PromotionsEntity, QPromotionsEntity> promotions = this.<PromotionsEntity, QPromotionsEntity>createSet("promotions", PromotionsEntity.class, QPromotionsEntity.class, PathInits.DIRECT2);

    public final NumberPath<java.math.BigDecimal> shippingTotal = createNumber("shippingTotal", java.math.BigDecimal.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final SetPath<OrdersEntity, QOrdersEntity> subOrders = this.<OrdersEntity, QOrdersEntity>createSet("subOrders", OrdersEntity.class, QOrdersEntity.class, PathInits.DIRECT2);

    public final NumberPath<java.math.BigDecimal> subTotal = createNumber("subTotal", java.math.BigDecimal.class);

    public final QUserEntity user;

    public QMetaOrderEntity(String variable) {
        this(MetaOrderEntity.class, forVariable(variable), INITS);
    }

    public QMetaOrderEntity(Path<? extends MetaOrderEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMetaOrderEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMetaOrderEntity(PathMetadata metadata, PathInits inits) {
        this(MetaOrderEntity.class, metadata, inits);
    }

    public QMetaOrderEntity(Class<? extends MetaOrderEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

