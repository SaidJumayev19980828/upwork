package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSubProducts is a Querydsl query type for QSubProducts
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSubProducts extends com.querydsl.sql.RelationalPathBase<QSubProducts> {

    private static final long serialVersionUID = 840364105;

    public static final QSubProducts subProducts = new QSubProducts("sub_products");

    public final StringPath barcode = createString("barcode");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final StringPath color = createString("color");

    public final StringPath coverImage = createString("coverImage");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Double> discount = createNumber("discount", Double.class);

    public final SimplePath<String[]> gallery = createSimple("gallery", String[].class);

    public final NumberPath<Integer> galleryIndex = createNumber("galleryIndex", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath itemId = createString("itemId");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final BooleanPath popup = createBoolean("popup");

    public final NumberPath<Double> price = createNumber("price", Double.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final StringPath size = createString("size");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QSubProducts> subProductsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QProductVariants> _productVariantsSubProductIdTempFkey = createInvForeignKey(id, "sub_product_id_temp");

    public QSubProducts(String variable) {
        super(QSubProducts.class, forVariable(variable), "public", "sub_products");
        addMetadata();
    }

    public QSubProducts(String variable, String schema, String table) {
        super(QSubProducts.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSubProducts(String variable, String schema) {
        super(QSubProducts.class, forVariable(variable), schema, "sub_products");
        addMetadata();
    }

    public QSubProducts(Path<? extends QSubProducts> path) {
        super(path.getType(), path.getMetadata(), "public", "sub_products");
        addMetadata();
    }

    public QSubProducts(PathMetadata metadata) {
        super(QSubProducts.class, metadata, "public", "sub_products");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(barcode, ColumnMetadata.named("barcode").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(brandId, ColumnMetadata.named("brand_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(color, ColumnMetadata.named("color").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(coverImage, ColumnMetadata.named("cover_image").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(discount, ColumnMetadata.named("discount").withIndex(5).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(gallery, ColumnMetadata.named("gallery").withIndex(13).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(galleryIndex, ColumnMetadata.named("gallery_index").withIndex(14).ofType(Types.INTEGER).withSize(10));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(itemId, ColumnMetadata.named("item_id").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(popup, ColumnMetadata.named("popup").withIndex(15).ofType(Types.BIT).withSize(1));
        addMetadata(price, ColumnMetadata.named("price").withIndex(4).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(quantity, ColumnMetadata.named("quantity").withIndex(8).ofType(Types.INTEGER).withSize(10));
        addMetadata(size, ColumnMetadata.named("size").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(10).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

