package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductImagesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductImagesEntity is a Querydsl query type for ProductImagesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProductImagesEntity extends EntityPathBase<ProductImagesEntity> {

    private static final long serialVersionUID = -310731715L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductImagesEntity productImagesEntity = new QProductImagesEntity("productImagesEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final QProductEntity productEntity;

    public final QProductVariantsEntity productVariantsEntity;

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final StringPath uri = createString("uri");

    public QProductImagesEntity(String variable) {
        this(ProductImagesEntity.class, forVariable(variable), INITS);
    }

    public QProductImagesEntity(Path<? extends ProductImagesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductImagesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductImagesEntity(PathMetadata metadata, PathInits inits) {
        this(ProductImagesEntity.class, metadata, inits);
    }

    public QProductImagesEntity(Class<? extends ProductImagesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.productEntity = inits.isInitialized("productEntity") ? new QProductEntity(forProperty("productEntity")) : null;
        this.productVariantsEntity = inits.isInitialized("productVariantsEntity") ? new QProductVariantsEntity(forProperty("productVariantsEntity"), inits.get("productVariantsEntity")) : null;
    }

}

