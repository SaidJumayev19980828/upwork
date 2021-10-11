package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShopScenesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopScenesEntity is a Querydsl query type for ShopScenesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShopScenesEntity extends EntityPathBase<ShopScenesEntity> {

    private static final long serialVersionUID = 1994211821L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopScenesEntity shopScenesEntity = new QShopScenesEntity("shopScenesEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath name = createString("name");

    public final QOrganizationEntity organizationEntity;

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final StringPath resized = createString("resized");

    public final QShopSectionsEntity shopSectionsEntity;

    public final StringPath thumbnail = createString("thumbnail");

    public QShopScenesEntity(String variable) {
        this(ShopScenesEntity.class, forVariable(variable), INITS);
    }

    public QShopScenesEntity(Path<? extends ShopScenesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopScenesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopScenesEntity(PathMetadata metadata, PathInits inits) {
        this(ShopScenesEntity.class, metadata, inits);
    }

    public QShopScenesEntity(Class<? extends ShopScenesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
        this.shopSectionsEntity = inits.isInitialized("shopSectionsEntity") ? new QShopSectionsEntity(forProperty("shopSectionsEntity"), inits.get("shopSectionsEntity")) : null;
    }

}

