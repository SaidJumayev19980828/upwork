package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProductImages is a Querydsl query type for QProductImages
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductImages extends com.querydsl.sql.RelationalPathBase<QProductImages> {

    private static final long serialVersionUID = 1402697297;

    public static final QProductImages productImages = new QProductImages("product_images");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final StringPath uri = createString("uri");

    public final NumberPath<Long> variantId = createNumber("variantId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QProductImages> productImagesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QProducts> productImagesProductIdFkey = createForeignKey(productId, "id");

    public final com.querydsl.sql.ForeignKey<QFiles> productImagesUriFkey = createForeignKey(uri, "url");

    public final com.querydsl.sql.ForeignKey<QProductVariants> productImagesVariantIdFkey = createForeignKey(variantId, "id");

    public QProductImages(String variable) {
        super(QProductImages.class, forVariable(variable), "public", "product_images");
        addMetadata();
    }

    public QProductImages(String variable, String schema, String table) {
        super(QProductImages.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductImages(String variable, String schema) {
        super(QProductImages.class, forVariable(variable), schema, "product_images");
        addMetadata();
    }

    public QProductImages(Path<? extends QProductImages> path) {
        super(path.getType(), path.getMetadata(), "public", "product_images");
        addMetadata();
    }

    public QProductImages(PathMetadata metadata) {
        super(QProductImages.class, metadata, "public", "product_images");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(priority, ColumnMetadata.named("priority").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(type, ColumnMetadata.named("type").withIndex(4).ofType(Types.INTEGER).withSize(10));
        addMetadata(uri, ColumnMetadata.named("uri").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(variantId, ColumnMetadata.named("variant_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
    }

}

