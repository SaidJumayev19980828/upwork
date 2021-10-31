package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShopsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopsEntity is a Querydsl query type for ShopsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShopsEntity extends EntityPathBase<ShopsEntity> {

    private static final long serialVersionUID = -222026061L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopsEntity shopsEntity = new QShopsEntity("shopsEntity");

    public final QAddressesEntity addressesEntity;

    public final StringPath banner = createString("banner");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final StringPath darkLogo = createString("darkLogo");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> isWarehouse = createNumber("isWarehouse", Integer.class);

    public final StringPath logo = createString("logo");

    public final StringPath name = createString("name");

    public final QOrganizationEntity organizationEntity;

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath placeId = createString("placeId");

    public final StringPath pname = createString("pname");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public QShopsEntity(String variable) {
        this(ShopsEntity.class, forVariable(variable), INITS);
    }

    public QShopsEntity(Path<? extends ShopsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopsEntity(PathMetadata metadata, PathInits inits) {
        this(ShopsEntity.class, metadata, inits);
    }

    public QShopsEntity(Class<? extends ShopsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.addressesEntity = inits.isInitialized("addressesEntity") ? new QAddressesEntity(forProperty("addressesEntity"), inits.get("addressesEntity")) : null;
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
    }

}

