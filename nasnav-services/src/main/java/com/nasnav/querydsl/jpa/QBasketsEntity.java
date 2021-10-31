package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.BasketsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBasketsEntity is a Querydsl query type for BasketsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBasketsEntity extends EntityPathBase<BasketsEntity> {

    private static final long serialVersionUID = -2095792861L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBasketsEntity basketsEntity = new QBasketsEntity("basketsEntity");

    public final NumberPath<Integer> currency = createNumber("currency", Integer.class);

    public final NumberPath<java.math.BigDecimal> discount = createNumber("discount", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath itemData = createString("itemData");

    public final QOrdersEntity ordersEntity;

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> quantity = createNumber("quantity", java.math.BigDecimal.class);

    public final QStocksEntity stocksEntity;

    public QBasketsEntity(String variable) {
        this(BasketsEntity.class, forVariable(variable), INITS);
    }

    public QBasketsEntity(Path<? extends BasketsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBasketsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBasketsEntity(PathMetadata metadata, PathInits inits) {
        this(BasketsEntity.class, metadata, inits);
    }

    public QBasketsEntity(Class<? extends BasketsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.ordersEntity = inits.isInitialized("ordersEntity") ? new QOrdersEntity(forProperty("ordersEntity"), inits.get("ordersEntity")) : null;
        this.stocksEntity = inits.isInitialized("stocksEntity") ? new QStocksEntity(forProperty("stocksEntity"), inits.get("stocksEntity")) : null;
    }

}

