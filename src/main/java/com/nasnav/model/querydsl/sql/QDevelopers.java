package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDevelopers is a Querydsl query type for QDevelopers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDevelopers extends com.querydsl.sql.RelationalPathBase<QDevelopers> {

    private static final long serialVersionUID = 1672392900;

    public static final QDevelopers developers = new QDevelopers("developers");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QDevelopers> developersPkey = createPrimaryKey(id);

    public QDevelopers(String variable) {
        super(QDevelopers.class, forVariable(variable), "public", "developers");
        addMetadata();
    }

    public QDevelopers(String variable, String schema, String table) {
        super(QDevelopers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDevelopers(String variable, String schema) {
        super(QDevelopers.class, forVariable(variable), schema, "developers");
        addMetadata();
    }

    public QDevelopers(Path<? extends QDevelopers> path) {
        super(path.getType(), path.getMetadata(), "public", "developers");
        addMetadata();
    }

    public QDevelopers(PathMetadata metadata) {
        super(QDevelopers.class, metadata, "public", "developers");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(password, ColumnMetadata.named("password").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

