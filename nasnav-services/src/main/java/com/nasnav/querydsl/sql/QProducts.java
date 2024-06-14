package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProducts is a Querydsl query type for QProducts
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProducts extends com.querydsl.sql.RelationalPathBase<QProducts> {

    private static final long serialVersionUID = 659179802;

    public static final QProducts products = new QProducts("products");

    public final StringPath barcode = createString("barcode");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final BooleanPath hide = createBoolean("hide");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath pName = createString("pName");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Integer> productType = createNumber("productType", Integer.class);

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final BooleanPath search360 = createBoolean("search360");

    public final NumberPath<Long> modelId = createNumber("modelId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QProducts> productsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QBrands> productsBrandIdFkey = createForeignKey(brandId, "id");

    public final com.querydsl.sql.ForeignKey<QCategories> productsCategoryIdFk = createForeignKey(categoryId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> productsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QProductBundles> _productBundlesProductIdFkey = createInvForeignKey(id, "product_id");

    public final com.querydsl.sql.ForeignKey<QProductCollections> _productCollectionProdIdFk = createInvForeignKey(id, "product_id");

    public final com.querydsl.sql.ForeignKey<QProductCollections> _productCollectionsProductIdFkey = createInvForeignKey(id, "product_id");

    public final com.querydsl.sql.ForeignKey<QProductImages> _productImagesProductIdFkey = createInvForeignKey(id, "product_id");

    public final com.querydsl.sql.ForeignKey<QProductTags> _productTagsProductIdFkey = createInvForeignKey(id, "product_id");

    public final com.querydsl.sql.ForeignKey<QProductVariants> _productVariantsProductIdFkey = createInvForeignKey(id, "product_id");

    public final com.querydsl.sql.ForeignKey<QProductsRelated> _productsRelatedProductIdFkey = createInvForeignKey(id, "product_id");

    public final com.querydsl.sql.ForeignKey<QProductsRelated> _productsRelatedRelatedProductIdFkey = createInvForeignKey(id, "related_product_id");

    public final com.querydsl.sql.ForeignKey<QShop360Products> _shop360ProductsProductIdFkey = createInvForeignKey(id, "product_id");
    
    public final com.querydsl.sql.ForeignKey<QProductAddons> _productAddonsProductIdFkey = createInvForeignKey(id, "product_id");


    public QProducts(String variable) {
        super(QProducts.class, forVariable(variable), "public", "products");
        addMetadata();
    }

    public QProducts(String variable, String schema, String table) {
        super(QProducts.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProducts(String variable, String schema) {
        super(QProducts.class, forVariable(variable), schema, "products");
        addMetadata();
    }

    public QProducts(Path<? extends QProducts> path) {
        super(path.getType(), path.getMetadata(), "public", "products");
        addMetadata();
    }

    public QProducts(PathMetadata metadata) {
        super(QProducts.class, metadata, "public", "products");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(barcode, ColumnMetadata.named("barcode").withIndex(11).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(brandId, ColumnMetadata.named("brand_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(categoryId, ColumnMetadata.named("category_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(description, ColumnMetadata.named("description").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(hide, ColumnMetadata.named("hide").withIndex(10).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(priority, ColumnMetadata.named("priority").withIndex(15).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(productType, ColumnMetadata.named("product_type").withIndex(12).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(removed, ColumnMetadata.named("removed").withIndex(13).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(search360, ColumnMetadata.named("search_360").withIndex(14).ofType(Types.BIT).withSize(1));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(modelId, ColumnMetadata.named("model_id").withIndex(16).ofType(Types.BIGINT).withSize(19));
    }

}

