package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductVariantsEntity is a Querydsl query type for ProductVariantsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProductVariantsEntity extends EntityPathBase<ProductVariantsEntity> {

    private static final long serialVersionUID = -2073704109L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductVariantsEntity productVariantsEntity = new QProductVariantsEntity("productVariantsEntity");

    public final StringPath barcode = createString("barcode");

    public final StringPath description = createString("description");

    public final SetPath<ProductExtraAttributesEntity, QProductExtraAttributesEntity> extraAttributes = this.<ProductExtraAttributesEntity, QProductExtraAttributesEntity>createSet("extraAttributes", ProductExtraAttributesEntity.class, QProductExtraAttributesEntity.class, PathInits.DIRECT2);

    public final StringPath featureSpec = createString("featureSpec");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath pname = createString("pname");

    public final StringPath productCode = createString("productCode");

    public final QProductEntity productEntity;

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final StringPath sku = createString("sku");

    public final SetPath<StocksEntity, QStocksEntity> stocks = this.<StocksEntity, QStocksEntity>createSet("stocks", StocksEntity.class, QStocksEntity.class, PathInits.DIRECT2);

    public final NumberPath<java.math.BigDecimal> weight = createNumber("weight", java.math.BigDecimal.class);

    public QProductVariantsEntity(String variable) {
        this(ProductVariantsEntity.class, forVariable(variable), INITS);
    }

    public QProductVariantsEntity(Path<? extends ProductVariantsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductVariantsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductVariantsEntity(PathMetadata metadata, PathInits inits) {
        this(ProductVariantsEntity.class, metadata, inits);
    }

    public QProductVariantsEntity(Class<? extends ProductVariantsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.productEntity = inits.isInitialized("productEntity") ? new QProductEntity(forProperty("productEntity")) : null;
    }

}

