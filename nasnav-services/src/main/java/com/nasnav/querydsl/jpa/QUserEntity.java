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
 * QUserEntity is a Querydsl query type for UserEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUserEntity extends EntityPathBase<UserEntity> {

    private static final long serialVersionUID = -1884622597L;

    public static final QUserEntity userEntity = new QUserEntity("userEntity");

    public final QBaseUserEntity _super = new QBaseUserEntity(this);

    public final SetPath<AddressesEntity, QAddressesEntity> addresses = this.<AddressesEntity, QAddressesEntity>createSet("addresses", AddressesEntity.class, QAddressesEntity.class, PathInits.DIRECT2);

    //inherited
    public final StringPath authenticationToken = _super.authenticationToken;

    //inherited
    public final StringPath avatar = _super.avatar;

    public final DateTimePath<java.time.LocalDateTime> creationTime = createDateTime("creationTime", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> currentSignInDate = _super.currentSignInDate;

    //inherited
    public final StringPath email = _super.email;

    //inherited
    public final StringPath encryptedPassword = _super.encryptedPassword;

    public final StringPath firstName = createString("firstName");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath image = createString("image");

    public final StringPath lastName = createString("lastName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastSignInDate = _super.lastSignInDate;

    public final StringPath mobile = createString("mobile");

    public final StringPath name = createString("name");

    //inherited
    public final NumberPath<Long> organizationId = _super.organizationId;

    //inherited
    public final StringPath phoneNumber = _super.phoneNumber;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> resetPasswordSentAt = _super.resetPasswordSentAt;

    //inherited
    public final StringPath resetPasswordToken = _super.resetPasswordToken;

    //inherited
    public final NumberPath<Integer> signInCount = _super.signInCount;

    public final SetPath<UserAddressEntity, QUserAddressEntity> userAddresses = this.<UserAddressEntity, QUserAddressEntity>createSet("userAddresses", UserAddressEntity.class, QUserAddressEntity.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Integer> userStatus = _super.userStatus;

    public QUserEntity(String variable) {
        super(UserEntity.class, forVariable(variable));
    }

    public QUserEntity(Path<? extends UserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserEntity(PathMetadata metadata) {
        super(UserEntity.class, metadata);
    }

}

