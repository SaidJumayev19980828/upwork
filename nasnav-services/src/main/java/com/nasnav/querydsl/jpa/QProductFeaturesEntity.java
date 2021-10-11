package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductFeaturesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductFeaturesEntity is a Querydsl query type for ProductFeaturesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProductFeaturesEntity extends EntityPathBase<ProductFeaturesEntity> {

    private static final long serialVersionUID = 1413082562L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductFeaturesEntity productFeaturesEntity = new QProductFeaturesEntity("productFeaturesEntity");

    public final StringPath description = createString("description");

    public final StringPath extraData = createString("extraData");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final StringPath name = createString("name");

    public final QOrganizationEntity organization;

    public final StringPath pname = createString("pname");

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public QProductFeaturesEntity(String variable) {
        this(ProductFeaturesEntity.class, forVariable(variable), INITS);
    }

    public QProductFeaturesEntity(Path<? extends ProductFeaturesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductFeaturesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductFeaturesEntity(PathMetadata metadata, PathInits inits) {
        this(ProductFeaturesEntity.class, metadata, inits);
    }

    public QProductFeaturesEntity(Class<? extends ProductFeaturesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
    }

}

