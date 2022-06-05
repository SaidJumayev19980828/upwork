package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.WishlistItemEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWishlistItemEntity is a Querydsl query type for WishlistItemEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QWishlistItemEntity extends EntityPathBase<WishlistItemEntity> {

    private static final long serialVersionUID = 827198696L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWishlistItemEntity wishlistItemEntity = new QWishlistItemEntity("wishlistItemEntity");

    public final QAbstractCartItemEntity _super;

    //inherited
    public final StringPath additionalData;

    //inherited
    public final StringPath coverImage;

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final NumberPath<Integer> quantity;

    // inherited
    public final QStocksEntity stock;

    // inherited
    public final QUserEntity user;

    //inherited
    public final StringPath variantFeatures;

    public QWishlistItemEntity(String variable) {
        this(WishlistItemEntity.class, forVariable(variable), INITS);
    }

    public QWishlistItemEntity(Path<? extends WishlistItemEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWishlistItemEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWishlistItemEntity(PathMetadata metadata, PathInits inits) {
        this(WishlistItemEntity.class, metadata, inits);
    }

    public QWishlistItemEntity(Class<? extends WishlistItemEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QAbstractCartItemEntity(type, metadata, inits);
        this.additionalData = _super.additionalData;
        this.coverImage = _super.coverImage;
        this.id = _super.id;
        this.quantity = _super.quantity;
        this.stock = _super.stock;
        this.user = _super.user;
        this.variantFeatures = _super.variantFeatures;
    }

}

