package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QManagers is a Querydsl query type for QManagers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QManagers extends com.querydsl.sql.RelationalPathBase<QManagers> {

    private static final long serialVersionUID = -1235517439;

    public static final QManagers managers = new QManagers("managers");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QManagers> managersPkey = createPrimaryKey(id);

    public QManagers(String variable) {
        super(QManagers.class, forVariable(variable), "public", "managers");
        addMetadata();
    }

    public QManagers(String variable, String schema, String table) {
        super(QManagers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QManagers(String variable, String schema) {
        super(QManagers.class, forVariable(variable), schema, "managers");
        addMetadata();
    }

    public QManagers(Path<? extends QManagers> path) {
        super(path.getType(), path.getMetadata(), "public", "managers");
        addMetadata();
    }

    public QManagers(PathMetadata metadata) {
        super(QManagers.class, metadata, "public", "managers");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(2).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

