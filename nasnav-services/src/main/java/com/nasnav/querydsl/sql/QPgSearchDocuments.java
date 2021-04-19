package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QPgSearchDocuments is a Querydsl query type for QPgSearchDocuments
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPgSearchDocuments extends com.querydsl.sql.RelationalPathBase<QPgSearchDocuments> {

    private static final long serialVersionUID = 2041449406;

    public static final QPgSearchDocuments pgSearchDocuments = new QPgSearchDocuments("pg_search_documents");

    public final StringPath content = createString("content");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> searchableId = createNumber("searchableId", Integer.class);

    public final StringPath searchableType = createString("searchableType");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QPgSearchDocuments> pgSearchDocumentsPkey = createPrimaryKey(id);

    public QPgSearchDocuments(String variable) {
        super(QPgSearchDocuments.class, forVariable(variable), "public", "pg_search_documents");
        addMetadata();
    }

    public QPgSearchDocuments(String variable, String schema, String table) {
        super(QPgSearchDocuments.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPgSearchDocuments(String variable, String schema) {
        super(QPgSearchDocuments.class, forVariable(variable), schema, "pg_search_documents");
        addMetadata();
    }

    public QPgSearchDocuments(Path<? extends QPgSearchDocuments> path) {
        super(path.getType(), path.getMetadata(), "public", "pg_search_documents");
        addMetadata();
    }

    public QPgSearchDocuments(PathMetadata metadata) {
        super(QPgSearchDocuments.class, metadata, "public", "pg_search_documents");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(content, ColumnMetadata.named("content").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(searchableId, ColumnMetadata.named("searchable_id").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(searchableType, ColumnMetadata.named("searchable_type").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

