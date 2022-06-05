package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShopFloorsEntity;
import com.nasnav.persistence.ShopSectionsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopFloorsEntity is a Querydsl query type for ShopFloorsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShopFloorsEntity extends EntityPathBase<ShopFloorsEntity> {

    private static final long serialVersionUID = -1267448211L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopFloorsEntity shopFloorsEntity = new QShopFloorsEntity("shopFloorsEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> number = createNumber("number", Integer.class);

    public final QOrganizationEntity organizationEntity;

    public final SetPath<ShopSectionsEntity, QShopSectionsEntity> shopSections = this.<ShopSectionsEntity, QShopSectionsEntity>createSet("shopSections", ShopSectionsEntity.class, QShopSectionsEntity.class, PathInits.DIRECT2);

    public final QShopThreeSixtyEntity shopThreeSixtyEntity;

    public QShopFloorsEntity(String variable) {
        this(ShopFloorsEntity.class, forVariable(variable), INITS);
    }

    public QShopFloorsEntity(Path<? extends ShopFloorsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopFloorsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopFloorsEntity(PathMetadata metadata, PathInits inits) {
        this(ShopFloorsEntity.class, metadata, inits);
    }

    public QShopFloorsEntity(Class<? extends ShopFloorsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
        this.shopThreeSixtyEntity = inits.isInitialized("shopThreeSixtyEntity") ? new QShopThreeSixtyEntity(forProperty("shopThreeSixtyEntity"), inits.get("shopThreeSixtyEntity")) : null;
    }

}

