package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.StocksEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStocksEntity is a Querydsl query type for StocksEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStocksEntity extends EntityPathBase<StocksEntity> {

    private static final long serialVersionUID = -720485235L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStocksEntity stocksEntity = new QStocksEntity("stocksEntity");

    public final NumberPath<Integer> currency = createNumber("currency", Integer.class);

    public final NumberPath<java.math.BigDecimal> discount = createNumber("discount", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrganizationEntity organizationEntity;

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final QProductVariantsEntity productVariantsEntity;

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final QShopsEntity shopsEntity;

    public final QStockUnitEntity unit;

    public QStocksEntity(String variable) {
        this(StocksEntity.class, forVariable(variable), INITS);
    }

    public QStocksEntity(Path<? extends StocksEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStocksEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStocksEntity(PathMetadata metadata, PathInits inits) {
        this(StocksEntity.class, metadata, inits);
    }

    public QStocksEntity(Class<? extends StocksEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
        this.productVariantsEntity = inits.isInitialized("productVariantsEntity") ? new QProductVariantsEntity(forProperty("productVariantsEntity"), inits.get("productVariantsEntity")) : null;
        this.shopsEntity = inits.isInitialized("shopsEntity") ? new QShopsEntity(forProperty("shopsEntity"), inits.get("shopsEntity")) : null;
        this.unit = inits.isInitialized("unit") ? new QStockUnitEntity(forProperty("unit")) : null;
    }

}

