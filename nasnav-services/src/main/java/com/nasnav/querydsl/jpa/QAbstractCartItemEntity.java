package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.AbstractCartItemEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAbstractCartItemEntity is a Querydsl query type for AbstractCartItemEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAbstractCartItemEntity extends EntityPathBase<AbstractCartItemEntity> {

    private static final long serialVersionUID = 2019882693L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAbstractCartItemEntity abstractCartItemEntity = new QAbstractCartItemEntity("abstractCartItemEntity");

    public final StringPath additionalData = createString("additionalData");

    public final StringPath coverImage = createString("coverImage");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final QStocksEntity stock;

    public final QUserEntity user;

    public final StringPath variantFeatures = createString("variantFeatures");

    public QAbstractCartItemEntity(String variable) {
        this(AbstractCartItemEntity.class, forVariable(variable), INITS);
    }

    public QAbstractCartItemEntity(Path<? extends AbstractCartItemEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAbstractCartItemEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAbstractCartItemEntity(PathMetadata metadata, PathInits inits) {
        this(AbstractCartItemEntity.class, metadata, inits);
    }

    public QAbstractCartItemEntity(Class<? extends AbstractCartItemEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.stock = inits.isInitialized("stock") ? new QStocksEntity(forProperty("stock"), inits.get("stock")) : null;
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

