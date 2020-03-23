package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRoleEmployeeUsers is a Querydsl query type for QRoleEmployeeUsers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRoleEmployeeUsers extends com.querydsl.sql.RelationalPathBase<QRoleEmployeeUsers> {

    private static final long serialVersionUID = 1433491433;

    public static final QRoleEmployeeUsers roleEmployeeUsers = new QRoleEmployeeUsers("role_employee_users");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> employeeUserId = createNumber("employeeUserId", Long.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> roleId = createNumber("roleId", Integer.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRoleEmployeeUsers> roleEmployeeUsersPkey = createPrimaryKey(id);

    public QRoleEmployeeUsers(String variable) {
        super(QRoleEmployeeUsers.class, forVariable(variable), "public", "role_employee_users");
        addMetadata();
    }

    public QRoleEmployeeUsers(String variable, String schema, String table) {
        super(QRoleEmployeeUsers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRoleEmployeeUsers(String variable, String schema) {
        super(QRoleEmployeeUsers.class, forVariable(variable), schema, "role_employee_users");
        addMetadata();
    }

    public QRoleEmployeeUsers(Path<? extends QRoleEmployeeUsers> path) {
        super(path.getType(), path.getMetadata(), "public", "role_employee_users");
        addMetadata();
    }

    public QRoleEmployeeUsers(PathMetadata metadata) {
        super(QRoleEmployeeUsers.class, metadata, "public", "role_employee_users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(employeeUserId, ColumnMetadata.named("employee_user_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(roleId, ColumnMetadata.named("role_id").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

