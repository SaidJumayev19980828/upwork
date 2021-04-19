package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QSchemaMigrations is a Querydsl query type for QSchemaMigrations
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSchemaMigrations extends com.querydsl.sql.RelationalPathBase<QSchemaMigrations> {

    private static final long serialVersionUID = 715280769;

    public static final QSchemaMigrations schemaMigrations = new QSchemaMigrations("schema_migrations");

    public final StringPath version = createString("version");

    public QSchemaMigrations(String variable) {
        super(QSchemaMigrations.class, forVariable(variable), "public", "schema_migrations");
        addMetadata();
    }

    public QSchemaMigrations(String variable, String schema, String table) {
        super(QSchemaMigrations.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSchemaMigrations(String variable, String schema) {
        super(QSchemaMigrations.class, forVariable(variable), schema, "schema_migrations");
        addMetadata();
    }

    public QSchemaMigrations(Path<? extends QSchemaMigrations> path) {
        super(path.getType(), path.getMetadata(), "public", "schema_migrations");
        addMetadata();
    }

    public QSchemaMigrations(PathMetadata metadata) {
        super(QSchemaMigrations.class, metadata, "public", "schema_migrations");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(version, ColumnMetadata.named("version").withIndex(1).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

