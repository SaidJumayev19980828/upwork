package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSeoPageTitles is a Querydsl query type for QSeoPageTitles
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSeoPageTitles extends com.querydsl.sql.RelationalPathBase<QSeoPageTitles> {

    private static final long serialVersionUID = 32819212;

    public static final QSeoPageTitles seoPageTitles = new QSeoPageTitles("seo_page_titles");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QSeoPageTitles> seoPageTitlesPkey = createPrimaryKey(id);

    public QSeoPageTitles(String variable) {
        super(QSeoPageTitles.class, forVariable(variable), "public", "seo_page_titles");
        addMetadata();
    }

    public QSeoPageTitles(String variable, String schema, String table) {
        super(QSeoPageTitles.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSeoPageTitles(String variable, String schema) {
        super(QSeoPageTitles.class, forVariable(variable), schema, "seo_page_titles");
        addMetadata();
    }

    public QSeoPageTitles(Path<? extends QSeoPageTitles> path) {
        super(path.getType(), path.getMetadata(), "public", "seo_page_titles");
        addMetadata();
    }

    public QSeoPageTitles(PathMetadata metadata) {
        super(QSeoPageTitles.class, metadata, "public", "seo_page_titles");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(title, ColumnMetadata.named("title").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

