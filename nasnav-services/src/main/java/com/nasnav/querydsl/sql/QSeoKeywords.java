package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSeoKeywords is a Querydsl query type for QSeoKeywords
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSeoKeywords extends com.querydsl.sql.RelationalPathBase<QSeoKeywords> {

    private static final long serialVersionUID = 1190655921;

    public static final QSeoKeywords seoKeywords = new QSeoKeywords("seo_keywords");

    public final NumberPath<Long> entityId = createNumber("entityId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Integer> typeId = createNumber("typeId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QSeoKeywords> seoKeywordsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> seoKeywordsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QSeoKeywords(String variable) {
        super(QSeoKeywords.class, forVariable(variable), "public", "seo_keywords");
        addMetadata();
    }

    public QSeoKeywords(String variable, String schema, String table) {
        super(QSeoKeywords.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSeoKeywords(String variable, String schema) {
        super(QSeoKeywords.class, forVariable(variable), schema, "seo_keywords");
        addMetadata();
    }

    public QSeoKeywords(Path<? extends QSeoKeywords> path) {
        super(path.getType(), path.getMetadata(), "public", "seo_keywords");
        addMetadata();
    }

    public QSeoKeywords(PathMetadata metadata) {
        super(QSeoKeywords.class, metadata, "public", "seo_keywords");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(entityId, ColumnMetadata.named("entity_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(keyword, ColumnMetadata.named("keyword").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(typeId, ColumnMetadata.named("type_id").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

