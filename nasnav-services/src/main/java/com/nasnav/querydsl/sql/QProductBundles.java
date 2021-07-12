package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProductBundles is a Querydsl query type for QProductBundles
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductBundles extends com.querydsl.sql.RelationalPathBase<QProductBundles> {

    private static final long serialVersionUID = -1142659208;

    public static final QProductBundles productBundles = new QProductBundles("product_bundles");

    public final NumberPath<Long> bundleStockId = createNumber("bundleStockId", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final com.querydsl.sql.ForeignKey<QStocks> productBundlesBundleStockIdFkey = createForeignKey(bundleStockId, "id");

    public final com.querydsl.sql.ForeignKey<QProducts> productBundlesProductIdFkey = createForeignKey(productId, "id");

    public QProductBundles(String variable) {
        super(QProductBundles.class, forVariable(variable), "public", "product_bundles");
        addMetadata();
    }

    public QProductBundles(String variable, String schema, String table) {
        super(QProductBundles.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductBundles(String variable, String schema) {
        super(QProductBundles.class, forVariable(variable), schema, "product_bundles");
        addMetadata();
    }

    public QProductBundles(Path<? extends QProductBundles> path) {
        super(path.getType(), path.getMetadata(), "public", "product_bundles");
        addMetadata();
    }

    public QProductBundles(PathMetadata metadata) {
        super(QProductBundles.class, metadata, "public", "product_bundles");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(bundleStockId, ColumnMetadata.named("bundle_stock_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

