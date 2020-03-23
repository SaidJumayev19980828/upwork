package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProductVariants is a Querydsl query type for QProductVariants
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductVariants extends com.querydsl.sql.RelationalPathBase<QProductVariants> {

    private static final long serialVersionUID = 36324514;

    public static final QProductVariants productVariants = new QProductVariants("product_variants");

    public final StringPath barcode = createString("barcode");

    public final StringPath description = createString("description");

    public final StringPath featureSpec = createString("featureSpec");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath pName = createString("pName");

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final NumberPath<Long> subProductIdTemp = createNumber("subProductIdTemp", Long.class);

    public final com.querydsl.sql.PrimaryKey<QProductVariants> productVariantsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QProducts> productVariantsProductIdFkey = createForeignKey(productId, "id");

    public final com.querydsl.sql.ForeignKey<QSubProducts> productVariantsSubProductIdTempFkey = createForeignKey(subProductIdTemp, "id");

    public final com.querydsl.sql.ForeignKey<QStocks> _stocksVariantIdFkey = createInvForeignKey(id, "variant_id");

    public final com.querydsl.sql.ForeignKey<QProductImages> _productImagesVariantIdFkey = createInvForeignKey(id, "variant_id");

    public final com.querydsl.sql.ForeignKey<QProductsExtraAttributes> _variantsExtraAttributesFk = createInvForeignKey(id, "variant_id");

    public QProductVariants(String variable) {
        super(QProductVariants.class, forVariable(variable), "public", "product_variants");
        addMetadata();
    }

    public QProductVariants(String variable, String schema, String table) {
        super(QProductVariants.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductVariants(String variable, String schema) {
        super(QProductVariants.class, forVariable(variable), schema, "product_variants");
        addMetadata();
    }

    public QProductVariants(Path<? extends QProductVariants> path) {
        super(path.getType(), path.getMetadata(), "public", "product_variants");
        addMetadata();
    }

    public QProductVariants(PathMetadata metadata) {
        super(QProductVariants.class, metadata, "public", "product_variants");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(barcode, ColumnMetadata.named("barcode").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(description, ColumnMetadata.named("description").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(featureSpec, ColumnMetadata.named("feature_spec").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(removed, ColumnMetadata.named("removed").withIndex(9).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(subProductIdTemp, ColumnMetadata.named("sub_product_id_temp").withIndex(8).ofType(Types.BIGINT).withSize(19));
    }

}

