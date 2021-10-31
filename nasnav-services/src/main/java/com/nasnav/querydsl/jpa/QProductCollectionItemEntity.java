package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductCollectionItemEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductCollectionItemEntity is a Querydsl query type for ProductCollectionItemEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProductCollectionItemEntity extends EntityPathBase<ProductCollectionItemEntity> {

    private static final long serialVersionUID = 305328086L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductCollectionItemEntity productCollectionItemEntity = new QProductCollectionItemEntity("productCollectionItemEntity");

    public final QProductCollectionEntity collection;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QProductVariantsEntity item;

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public QProductCollectionItemEntity(String variable) {
        this(ProductCollectionItemEntity.class, forVariable(variable), INITS);
    }

    public QProductCollectionItemEntity(Path<? extends ProductCollectionItemEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductCollectionItemEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductCollectionItemEntity(PathMetadata metadata, PathInits inits) {
        this(ProductCollectionItemEntity.class, metadata, inits);
    }

    public QProductCollectionItemEntity(Class<? extends ProductCollectionItemEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.collection = inits.isInitialized("collection") ? new QProductCollectionEntity(forProperty("collection")) : null;
        this.item = inits.isInitialized("item") ? new QProductVariantsEntity(forProperty("item"), inits.get("item")) : null;
    }

}

