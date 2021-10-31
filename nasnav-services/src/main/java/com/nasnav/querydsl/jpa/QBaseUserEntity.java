package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.BaseUserEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseUserEntity is a Querydsl query type for BaseUserEntity
 */
@Generated("com.querydsl.codegen.SupertypeSerializer")
public class QBaseUserEntity extends EntityPathBase<BaseUserEntity> {

    private static final long serialVersionUID = -517159892L;

    public static final QBaseUserEntity baseUserEntity = new QBaseUserEntity("baseUserEntity");

    public final QDefaultBusinessEntity _super = new QDefaultBusinessEntity(this);

    public final StringPath authenticationToken = createString("authenticationToken");

    public final StringPath avatar = createString("avatar");

    public final DateTimePath<java.time.LocalDateTime> currentSignInDate = createDateTime("currentSignInDate", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final StringPath encryptedPassword = createString("encryptedPassword");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastSignInDate = createDateTime("lastSignInDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final DateTimePath<java.time.LocalDateTime> resetPasswordSentAt = createDateTime("resetPasswordSentAt", java.time.LocalDateTime.class);

    public final StringPath resetPasswordToken = createString("resetPasswordToken");

    public final NumberPath<Integer> signInCount = createNumber("signInCount", Integer.class);

    public final NumberPath<Integer> userStatus = createNumber("userStatus", Integer.class);

    public QBaseUserEntity(String variable) {
        super(BaseUserEntity.class, forVariable(variable));
    }

    public QBaseUserEntity(Path<? extends BaseUserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseUserEntity(PathMetadata metadata) {
        super(BaseUserEntity.class, metadata);
    }

}

