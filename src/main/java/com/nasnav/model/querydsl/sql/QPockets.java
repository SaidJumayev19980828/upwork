package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPockets is a Querydsl query type for QPockets
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPockets extends com.querydsl.sql.RelationalPathBase<QPockets> {

    private static final long serialVersionUID = 1905222818;

    public static final QPockets pockets = new QPockets("pockets");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QPockets> pocketsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QUsers> railsFc49c61db2Fk = createForeignKey(userId, "id");

    public QPockets(String variable) {
        super(QPockets.class, forVariable(variable), "public", "pockets");
        addMetadata();
    }

    public QPockets(String variable, String schema, String table) {
        super(QPockets.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPockets(String variable, String schema) {
        super(QPockets.class, forVariable(variable), schema, "pockets");
        addMetadata();
    }

    public QPockets(Path<? extends QPockets> path) {
        super(path.getType(), path.getMetadata(), "public", "pockets");
        addMetadata();
    }

    public QPockets(PathMetadata metadata) {
        super(QPockets.class, metadata, "public", "pockets");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
    }

}

