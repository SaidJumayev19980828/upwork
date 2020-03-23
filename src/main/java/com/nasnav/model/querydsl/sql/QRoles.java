package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRoles is a Querydsl query type for QRoles
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRoles extends com.querydsl.sql.RelationalPathBase<QRoles> {

    private static final long serialVersionUID = 146854658;

    public static final QRoles roles = new QRoles("roles");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QRoles> rolesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> rolesOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QRoles(String variable) {
        super(QRoles.class, forVariable(variable), "public", "roles");
        addMetadata();
    }

    public QRoles(String variable, String schema, String table) {
        super(QRoles.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRoles(String variable, String schema) {
        super(QRoles.class, forVariable(variable), schema, "roles");
        addMetadata();
    }

    public QRoles(Path<? extends QRoles> path) {
        super(path.getType(), path.getMetadata(), "public", "roles");
        addMetadata();
    }

    public QRoles(PathMetadata metadata) {
        super(QRoles.class, metadata, "public", "roles");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

