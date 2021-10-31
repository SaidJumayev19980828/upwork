package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.UserTokensEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserTokensEntity is a Querydsl query type for UserTokensEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUserTokensEntity extends EntityPathBase<UserTokensEntity> {

    private static final long serialVersionUID = 1299191093L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserTokensEntity userTokensEntity = new QUserTokensEntity("userTokensEntity");

    public final QEmployeeUserEntity employeeUserEntity;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath token = createString("token");

    public final DateTimePath<java.time.LocalDateTime> updateTime = createDateTime("updateTime", java.time.LocalDateTime.class);

    public final QUserEntity userEntity;

    public QUserTokensEntity(String variable) {
        this(UserTokensEntity.class, forVariable(variable), INITS);
    }

    public QUserTokensEntity(Path<? extends UserTokensEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserTokensEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserTokensEntity(PathMetadata metadata, PathInits inits) {
        this(UserTokensEntity.class, metadata, inits);
    }

    public QUserTokensEntity(Class<? extends UserTokensEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.employeeUserEntity = inits.isInitialized("employeeUserEntity") ? new QEmployeeUserEntity(forProperty("employeeUserEntity")) : null;
        this.userEntity = inits.isInitialized("userEntity") ? new QUserEntity(forProperty("userEntity")) : null;
    }

}

