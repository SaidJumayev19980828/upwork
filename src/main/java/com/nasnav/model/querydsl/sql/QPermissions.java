package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPermissions is a Querydsl query type for QPermissions
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPermissions extends com.querydsl.sql.RelationalPathBase<QPermissions> {

    private static final long serialVersionUID = 1663316233;

    public static final QPermissions permissions = new QPermissions("permissions");

    public final StringPath action = createString("action");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath targetModelName = createString("targetModelName");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QPermissions> permissionsPkey = createPrimaryKey(id);

    public QPermissions(String variable) {
        super(QPermissions.class, forVariable(variable), "public", "permissions");
        addMetadata();
    }

    public QPermissions(String variable, String schema, String table) {
        super(QPermissions.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPermissions(String variable, String schema) {
        super(QPermissions.class, forVariable(variable), schema, "permissions");
        addMetadata();
    }

    public QPermissions(Path<? extends QPermissions> path) {
        super(path.getType(), path.getMetadata(), "public", "permissions");
        addMetadata();
    }

    public QPermissions(PathMetadata metadata) {
        super(QPermissions.class, metadata, "public", "permissions");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(action, ColumnMetadata.named("action").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(targetModelName, ColumnMetadata.named("target_model_name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

