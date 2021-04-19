package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QPermissionRoles is a Querydsl query type for QPermissionRoles
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPermissionRoles extends com.querydsl.sql.RelationalPathBase<QPermissionRoles> {

    private static final long serialVersionUID = -494562253;

    public static final QPermissionRoles permissionRoles = new QPermissionRoles("permission_roles");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> permissionId = createNumber("permissionId", Integer.class);

    public final NumberPath<Integer> roleId = createNumber("roleId", Integer.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QPermissionRoles> permissionRolesPkey = createPrimaryKey(id);

    public QPermissionRoles(String variable) {
        super(QPermissionRoles.class, forVariable(variable), "public", "permission_roles");
        addMetadata();
    }

    public QPermissionRoles(String variable, String schema, String table) {
        super(QPermissionRoles.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPermissionRoles(String variable, String schema) {
        super(QPermissionRoles.class, forVariable(variable), schema, "permission_roles");
        addMetadata();
    }

    public QPermissionRoles(Path<? extends QPermissionRoles> path) {
        super(path.getType(), path.getMetadata(), "public", "permission_roles");
        addMetadata();
    }

    public QPermissionRoles(PathMetadata metadata) {
        super(QPermissionRoles.class, metadata, "public", "permission_roles");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(permissionId, ColumnMetadata.named("permission_id").withIndex(2).ofType(Types.INTEGER).withSize(10));
        addMetadata(roleId, ColumnMetadata.named("role_id").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

