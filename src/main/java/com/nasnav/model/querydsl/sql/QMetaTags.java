package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMetaTags is a Querydsl query type for QMetaTags
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMetaTags extends com.querydsl.sql.RelationalPathBase<QMetaTags> {

    private static final long serialVersionUID = -1809265319;

    public static final QMetaTags metaTags = new QMetaTags("meta_tags");

    public final StringPath content = createString("content");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QMetaTags> metaTagsPkey = createPrimaryKey(id);

    public QMetaTags(String variable) {
        super(QMetaTags.class, forVariable(variable), "public", "meta_tags");
        addMetadata();
    }

    public QMetaTags(String variable, String schema, String table) {
        super(QMetaTags.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMetaTags(String variable, String schema) {
        super(QMetaTags.class, forVariable(variable), schema, "meta_tags");
        addMetadata();
    }

    public QMetaTags(Path<? extends QMetaTags> path) {
        super(path.getType(), path.getMetadata(), "public", "meta_tags");
        addMetadata();
    }

    public QMetaTags(PathMetadata metadata) {
        super(QMetaTags.class, metadata, "public", "meta_tags");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(content, ColumnMetadata.named("content").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(8).ofType(Types.BIGINT).withSize(19));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

