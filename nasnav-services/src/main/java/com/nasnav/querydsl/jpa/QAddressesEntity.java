package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.UserAddressEntity;
import com.nasnav.persistence.UserEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAddressesEntity is a Querydsl query type for AddressesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAddressesEntity extends EntityPathBase<AddressesEntity> {

    private static final long serialVersionUID = 1496575192L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAddressesEntity addressesEntity = new QAddressesEntity("addressesEntity");

    public final StringPath addressLine1 = createString("addressLine1");

    public final StringPath addressLine2 = createString("addressLine2");

    public final QAreasEntity areasEntity;

    public final StringPath buildingNumber = createString("buildingNumber");

    public final StringPath firstName = createString("firstName");

    public final StringPath flatNumber = createString("flatNumber");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath postalCode = createString("postalCode");

    public final QSubAreasEntity subAreasEntity;

    public final SetPath<UserAddressEntity, QUserAddressEntity> userAddresses = this.<UserAddressEntity, QUserAddressEntity>createSet("userAddresses", UserAddressEntity.class, QUserAddressEntity.class, PathInits.DIRECT2);

    public final SetPath<UserEntity, QUserEntity> users = this.<UserEntity, QUserEntity>createSet("users", UserEntity.class, QUserEntity.class, PathInits.DIRECT2);

    public QAddressesEntity(String variable) {
        this(AddressesEntity.class, forVariable(variable), INITS);
    }

    public QAddressesEntity(Path<? extends AddressesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAddressesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAddressesEntity(PathMetadata metadata, PathInits inits) {
        this(AddressesEntity.class, metadata, inits);
    }

    public QAddressesEntity(Class<? extends AddressesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.areasEntity = inits.isInitialized("areasEntity") ? new QAreasEntity(forProperty("areasEntity"), inits.get("areasEntity")) : null;
        this.subAreasEntity = inits.isInitialized("subAreasEntity") ? new QSubAreasEntity(forProperty("subAreasEntity"), inits.get("subAreasEntity")) : null;
    }

}

