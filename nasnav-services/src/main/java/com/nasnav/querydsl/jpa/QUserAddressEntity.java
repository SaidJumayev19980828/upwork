package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.UserAddressEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserAddressEntity is a Querydsl query type for UserAddressEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUserAddressEntity extends EntityPathBase<UserAddressEntity> {

    private static final long serialVersionUID = -1302254369L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserAddressEntity userAddressEntity = new QUserAddressEntity("userAddressEntity");

    public final QAddressesEntity address;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath principal = createBoolean("principal");

    public final QUserEntity user;

    public QUserAddressEntity(String variable) {
        this(UserAddressEntity.class, forVariable(variable), INITS);
    }

    public QUserAddressEntity(Path<? extends UserAddressEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserAddressEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserAddressEntity(PathMetadata metadata, PathInits inits) {
        this(UserAddressEntity.class, metadata, inits);
    }

    public QUserAddressEntity(Class<? extends UserAddressEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new QAddressesEntity(forProperty("address"), inits.get("address")) : null;
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

