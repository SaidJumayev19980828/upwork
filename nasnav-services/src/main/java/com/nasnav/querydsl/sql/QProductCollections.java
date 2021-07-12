package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProductCollections is a Querydsl query type for QProductCollections
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductCollections extends com.querydsl.sql.RelationalPathBase<QProductCollections> {

    private static final long serialVersionUID = 1479192924;

    public static final QProductCollections productCollections = new QProductCollections("product_collections");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> variantId = createNumber("variantId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QProductCollections> productCollectionsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QProducts> productCollectionProdIdFk = createForeignKey(productId, "id");

    public final com.querydsl.sql.ForeignKey<QProductVariants> productCollectionVarIdFk = createForeignKey(variantId, "id");

    public final com.querydsl.sql.ForeignKey<QProducts> productCollectionsProductIdFkey = createForeignKey(productId, "id");

    public final com.querydsl.sql.ForeignKey<QProductVariants> productCollectionsVariantIdFkey = createForeignKey(variantId, "id");

    public QProductCollections(String variable) {
        super(QProductCollections.class, forVariable(variable), "public", "product_collections");
        addMetadata();
    }

    public QProductCollections(String variable, String schema, String table) {
        super(QProductCollections.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductCollections(String variable, String schema) {
        super(QProductCollections.class, forVariable(variable), schema, "product_collections");
        addMetadata();
    }

    public QProductCollections(Path<? extends QProductCollections> path) {
        super(path.getType(), path.getMetadata(), "public", "product_collections");
        addMetadata();
    }

    public QProductCollections(PathMetadata metadata) {
        super(QProductCollections.class, metadata, "public", "product_collections");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(priority, ColumnMetadata.named("priority").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(variantId, ColumnMetadata.named("variant_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

