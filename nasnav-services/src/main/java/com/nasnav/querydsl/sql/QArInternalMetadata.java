package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QArInternalMetadata is a Querydsl query type for QArInternalMetadata
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QArInternalMetadata extends com.querydsl.sql.RelationalPathBase<QArInternalMetadata> {

    private static final long serialVersionUID = 1592195896;

    public static final QArInternalMetadata arInternalMetadata = new QArInternalMetadata("ar_internal_metadata");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath key = createString("key");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final StringPath value = createString("value");

    public final com.querydsl.sql.PrimaryKey<QArInternalMetadata> arInternalMetadataPkey = createPrimaryKey(key);

    public QArInternalMetadata(String variable) {
        super(QArInternalMetadata.class, forVariable(variable), "public", "ar_internal_metadata");
        addMetadata();
    }

    public QArInternalMetadata(String variable, String schema, String table) {
        super(QArInternalMetadata.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QArInternalMetadata(String variable, String schema) {
        super(QArInternalMetadata.class, forVariable(variable), schema, "ar_internal_metadata");
        addMetadata();
    }

    public QArInternalMetadata(Path<? extends QArInternalMetadata> path) {
        super(path.getType(), path.getMetadata(), "public", "ar_internal_metadata");
        addMetadata();
    }

    public QArInternalMetadata(PathMetadata metadata) {
        super(QArInternalMetadata.class, metadata, "public", "ar_internal_metadata");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(key, ColumnMetadata.named("key").withIndex(1).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(value, ColumnMetadata.named("value").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

