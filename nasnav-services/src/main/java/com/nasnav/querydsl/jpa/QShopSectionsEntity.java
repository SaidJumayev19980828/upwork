package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShopScenesEntity;
import com.nasnav.persistence.ShopSectionsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopSectionsEntity is a Querydsl query type for ShopSectionsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShopSectionsEntity extends EntityPathBase<ShopSectionsEntity> {

    private static final long serialVersionUID = 1531531444L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopSectionsEntity shopSectionsEntity = new QShopSectionsEntity("shopSectionsEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath name = createString("name");

    public final QOrganizationEntity organizationEntity;

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final QShopFloorsEntity shopFloorsEntity;

    public final SetPath<ShopScenesEntity, QShopScenesEntity> shopScenes = this.<ShopScenesEntity, QShopScenesEntity>createSet("shopScenes", ShopScenesEntity.class, QShopScenesEntity.class, PathInits.DIRECT2);

    public QShopSectionsEntity(String variable) {
        this(ShopSectionsEntity.class, forVariable(variable), INITS);
    }

    public QShopSectionsEntity(Path<? extends ShopSectionsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopSectionsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopSectionsEntity(PathMetadata metadata, PathInits inits) {
        this(ShopSectionsEntity.class, metadata, inits);
    }

    public QShopSectionsEntity(Class<? extends ShopSectionsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
        this.shopFloorsEntity = inits.isInitialized("shopFloorsEntity") ? new QShopFloorsEntity(forProperty("shopFloorsEntity"), inits.get("shopFloorsEntity")) : null;
    }

}

