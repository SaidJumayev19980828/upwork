package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductExtraAttributesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductExtraAttributesEntity is a Querydsl query type for ProductExtraAttributesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProductExtraAttributesEntity extends EntityPathBase<ProductExtraAttributesEntity> {

    private static final long serialVersionUID = -955405624L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductExtraAttributesEntity productExtraAttributesEntity = new QProductExtraAttributesEntity("productExtraAttributesEntity");

    public final QExtraAttributesEntity extraAttribute;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath value = createString("value");

    public final QProductVariantsEntity variant;

    public QProductExtraAttributesEntity(String variable) {
        this(ProductExtraAttributesEntity.class, forVariable(variable), INITS);
    }

    public QProductExtraAttributesEntity(Path<? extends ProductExtraAttributesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductExtraAttributesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductExtraAttributesEntity(PathMetadata metadata, PathInits inits) {
        this(ProductExtraAttributesEntity.class, metadata, inits);
    }

    public QProductExtraAttributesEntity(Class<? extends ProductExtraAttributesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.extraAttribute = inits.isInitialized("extraAttribute") ? new QExtraAttributesEntity(forProperty("extraAttribute")) : null;
        this.variant = inits.isInitialized("variant") ? new QProductVariantsEntity(forProperty("variant"), inits.get("variant")) : null;
    }

}

