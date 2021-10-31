package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.RoleEmployeeUser;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QRoleEmployeeUser is a Querydsl query type for RoleEmployeeUser
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QRoleEmployeeUser extends EntityPathBase<RoleEmployeeUser> {

    private static final long serialVersionUID = 1663502044L;

    public static final QRoleEmployeeUser roleEmployeeUser = new QRoleEmployeeUser("roleEmployeeUser");

    public final QDefaultBusinessEntity _super = new QDefaultBusinessEntity(this);

    public final NumberPath<Long> employeeUserId = createNumber("employeeUserId", Long.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> roleId = createNumber("roleId", Integer.class);

    public QRoleEmployeeUser(String variable) {
        super(RoleEmployeeUser.class, forVariable(variable));
    }

    public QRoleEmployeeUser(Path<? extends RoleEmployeeUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoleEmployeeUser(PathMetadata metadata) {
        super(RoleEmployeeUser.class, metadata);
    }

}

