package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShopThreeSixtyEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopThreeSixtyEntity is a Querydsl query type for ShopThreeSixtyEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShopThreeSixtyEntity extends EntityPathBase<ShopThreeSixtyEntity> {

    private static final long serialVersionUID = -1608999569L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopThreeSixtyEntity shopThreeSixtyEntity = new QShopThreeSixtyEntity("shopThreeSixtyEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mobileJsonData = createString("mobileJsonData");

    public final StringPath previewJsonData = createString("previewJsonData");

    public final BooleanPath published = createBoolean("published");

    public final StringPath sceneName = createString("sceneName");

    public final QShopsEntity shopsEntity;

    public final StringPath url = createString("url");

    public final StringPath webJsonData = createString("webJsonData");

    public QShopThreeSixtyEntity(String variable) {
        this(ShopThreeSixtyEntity.class, forVariable(variable), INITS);
    }

    public QShopThreeSixtyEntity(Path<? extends ShopThreeSixtyEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopThreeSixtyEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopThreeSixtyEntity(PathMetadata metadata, PathInits inits) {
        this(ShopThreeSixtyEntity.class, metadata, inits);
    }

    public QShopThreeSixtyEntity(Class<? extends ShopThreeSixtyEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shopsEntity = inits.isInitialized("shopsEntity") ? new QShopsEntity(forProperty("shopsEntity"), inits.get("shopsEntity")) : null;
    }

}

