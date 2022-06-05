package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.Shop360ProductsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShop360ProductsEntity is a Querydsl query type for Shop360ProductsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShop360ProductsEntity extends EntityPathBase<Shop360ProductsEntity> {

    private static final long serialVersionUID = -960641487L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShop360ProductsEntity shop360ProductsEntity = new QShop360ProductsEntity("shop360ProductsEntity");

    public final QShopFloorsEntity floor;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Float> pitch = createNumber("pitch", Float.class);

    public final QProductEntity productEntity;

    public final NumberPath<Short> published = createNumber("published", Short.class);

    public final QShopScenesEntity scene;

    public final QShopSectionsEntity section;

    public final QShopsEntity shopEntity;

    public final NumberPath<Float> yaw = createNumber("yaw", Float.class);

    public QShop360ProductsEntity(String variable) {
        this(Shop360ProductsEntity.class, forVariable(variable), INITS);
    }

    public QShop360ProductsEntity(Path<? extends Shop360ProductsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShop360ProductsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShop360ProductsEntity(PathMetadata metadata, PathInits inits) {
        this(Shop360ProductsEntity.class, metadata, inits);
    }

    public QShop360ProductsEntity(Class<? extends Shop360ProductsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.floor = inits.isInitialized("floor") ? new QShopFloorsEntity(forProperty("floor"), inits.get("floor")) : null;
        this.productEntity = inits.isInitialized("productEntity") ? new QProductEntity(forProperty("productEntity")) : null;
        this.scene = inits.isInitialized("scene") ? new QShopScenesEntity(forProperty("scene"), inits.get("scene")) : null;
        this.section = inits.isInitialized("section") ? new QShopSectionsEntity(forProperty("section"), inits.get("section")) : null;
        this.shopEntity = inits.isInitialized("shopEntity") ? new QShopsEntity(forProperty("shopEntity"), inits.get("shopEntity")) : null;
    }

}

