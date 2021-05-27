package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProductsRelated is a Querydsl query type for QProductsRelated
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductsRelated extends com.querydsl.sql.RelationalPathBase<QProductsRelated> {

    private static final long serialVersionUID = 1482185137;

    public static final QProductsRelated productsRelated = new QProductsRelated("products_related");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> relatedProductId = createNumber("relatedProductId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QProductsRelated> productsRelatedPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QProducts> productsRelatedProductIdFkey = createForeignKey(productId, "id");

    public final com.querydsl.sql.ForeignKey<QProducts> productsRelatedRelatedProductIdFkey = createForeignKey(relatedProductId, "id");

    public QProductsRelated(String variable) {
        super(QProductsRelated.class, forVariable(variable), "public", "products_related");
        addMetadata();
    }

    public QProductsRelated(String variable, String schema, String table) {
        super(QProductsRelated.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductsRelated(String variable, String schema) {
        super(QProductsRelated.class, forVariable(variable), schema, "products_related");
        addMetadata();
    }

    public QProductsRelated(Path<? extends QProductsRelated> path) {
        super(path.getType(), path.getMetadata(), "public", "products_related");
        addMetadata();
    }

    public QProductsRelated(PathMetadata metadata) {
        super(QProductsRelated.class, metadata, "public", "products_related");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(relatedProductId, ColumnMetadata.named("related_product_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

