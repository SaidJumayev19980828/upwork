package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.EmployeeUserEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmployeeUserEntity is a Querydsl query type for EmployeeUserEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QEmployeeUserEntity extends EntityPathBase<EmployeeUserEntity> {

    private static final long serialVersionUID = -1262226103L;

    public static final QEmployeeUserEntity employeeUserEntity = new QEmployeeUserEntity("employeeUserEntity");

    public final QBaseUserEntity _super = new QBaseUserEntity(this);

    //inherited
    public final StringPath authenticationToken = _super.authenticationToken;

    public final StringPath avatar = createString("avatar");

    public final NumberPath<Integer> createdBy = createNumber("createdBy", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> currentSignInDate = _super.currentSignInDate;

    //inherited
    public final StringPath email = _super.email;

    //inherited
    public final StringPath encryptedPassword = _super.encryptedPassword;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastSignInDate = _super.lastSignInDate;

    public final StringPath name = createString("name");

    //inherited
    public final NumberPath<Long> organizationId = _super.organizationId;

    public final NumberPath<Long> organizationManagerId = createNumber("organizationManagerId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final DateTimePath<java.time.LocalDateTime> rememberCreatedAt = createDateTime("rememberCreatedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> resetPasswordSentAt = _super.resetPasswordSentAt;

    //inherited
    public final StringPath resetPasswordToken = _super.resetPasswordToken;

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    //inherited
    public final NumberPath<Integer> signInCount = _super.signInCount;

    //inherited
    public final NumberPath<Integer> userStatus = _super.userStatus;

    public QEmployeeUserEntity(String variable) {
        super(EmployeeUserEntity.class, forVariable(variable));
    }

    public QEmployeeUserEntity(Path<? extends EmployeeUserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmployeeUserEntity(PathMetadata metadata) {
        super(EmployeeUserEntity.class, metadata);
    }

}

