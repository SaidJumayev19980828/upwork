package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.RelatedProductsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRelatedProductsEntity is a Querydsl query type for RelatedProductsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QRelatedProductsEntity extends EntityPathBase<RelatedProductsEntity> {

    private static final long serialVersionUID = 992909285L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRelatedProductsEntity relatedProductsEntity = new QRelatedProductsEntity("relatedProductsEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QProductEntity product;

    public final QProductEntity relatedProduct;

    public QRelatedProductsEntity(String variable) {
        this(RelatedProductsEntity.class, forVariable(variable), INITS);
    }

    public QRelatedProductsEntity(Path<? extends RelatedProductsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRelatedProductsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRelatedProductsEntity(PathMetadata metadata, PathInits inits) {
        this(RelatedProductsEntity.class, metadata, inits);
    }

    public QRelatedProductsEntity(Class<? extends RelatedProductsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProductEntity(forProperty("product")) : null;
        this.relatedProduct = inits.isInitialized("relatedProduct") ? new QProductEntity(forProperty("relatedProduct")) : null;
    }

}

