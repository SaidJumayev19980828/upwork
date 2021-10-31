package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.BrandsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBrandsEntity is a Querydsl query type for BrandsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBrandsEntity extends EntityPathBase<BrandsEntity> {

    private static final long serialVersionUID = 1413299964L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBrandsEntity brandsEntity = new QBrandsEntity("brandsEntity");

    public final StringPath bannerImage = createString("bannerImage");

    public final NumberPath<Integer> categoryId = createNumber("categoryId", Integer.class);

    public final StringPath coverUrl = createString("coverUrl");

    public final StringPath darkLogo = createString("darkLogo");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logo = createString("logo");

    public final StringPath name = createString("name");

    public final QOrganizationEntity organizationEntity;

    public final StringPath pname = createString("pname");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public QBrandsEntity(String variable) {
        this(BrandsEntity.class, forVariable(variable), INITS);
    }

    public QBrandsEntity(Path<? extends BrandsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBrandsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBrandsEntity(PathMetadata metadata, PathInits inits) {
        this(BrandsEntity.class, metadata, inits);
    }

    public QBrandsEntity(Class<? extends BrandsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
    }

}

