package com.nasnav.model.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductCollections extends com.querydsl.sql.RelationalPathBase<QProductCollections> {
    private static final long serialVersionUID = 1979041395;

    public static final QProductCollections productCollections = new QProductCollections("product_collections");

    public final NumberPath<Long> variantId = createNumber("variantId", Long.class);

    public final NumberPath<Long> productId= createNumber("productId", Long.class);


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
        addMetadata(variantId, ColumnMetadata.named("variant_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

