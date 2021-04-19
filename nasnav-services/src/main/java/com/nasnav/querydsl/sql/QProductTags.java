package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QProductTags is a Querydsl query type for QProductTags
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductTags extends com.querydsl.sql.RelationalPathBase<QProductTags> {

    private static final long serialVersionUID = -962026963;

    public static final QProductTags productTags = new QProductTags("product_tags");

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> tagId = createNumber("tagId", Long.class);

    public final com.querydsl.sql.ForeignKey<QTags> productTagsTagIdFkey = createForeignKey(tagId, "id");

    public final com.querydsl.sql.ForeignKey<QProducts> productTagsProductIdFkey = createForeignKey(productId, "id");

    public QProductTags(String variable) {
        super(QProductTags.class, forVariable(variable), "public", "product_tags");
        addMetadata();
    }

    public QProductTags(String variable, String schema, String table) {
        super(QProductTags.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductTags(String variable, String schema) {
        super(QProductTags.class, forVariable(variable), schema, "product_tags");
        addMetadata();
    }

    public QProductTags(Path<? extends QProductTags> path) {
        super(path.getType(), path.getMetadata(), "public", "product_tags");
        addMetadata();
    }

    public QProductTags(PathMetadata metadata) {
        super(QProductTags.class, metadata, "public", "product_tags");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(tagId, ColumnMetadata.named("tag_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

