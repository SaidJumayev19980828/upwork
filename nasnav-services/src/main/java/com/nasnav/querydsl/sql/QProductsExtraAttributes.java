package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProductsExtraAttributes is a Querydsl query type for QProductsExtraAttributes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductsExtraAttributes extends com.querydsl.sql.RelationalPathBase<QProductsExtraAttributes> {

    private static final long serialVersionUID = 784801037;

    public static final QProductsExtraAttributes productsExtraAttributes = new QProductsExtraAttributes("products_extra_attributes");

    public final NumberPath<Integer> extraAttributeId = createNumber("extraAttributeId", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath value = createString("value");

    public final NumberPath<Long> variantId = createNumber("variantId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QProductsExtraAttributes> productsExtraAttributesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QExtraAttributes> extraAttributeIdFkey = createForeignKey(extraAttributeId, "id");

    public final com.querydsl.sql.ForeignKey<QProductVariants> productsExtraAttributesVariantIdFkey = createForeignKey(variantId, "id");

    public QProductsExtraAttributes(String variable) {
        super(QProductsExtraAttributes.class, forVariable(variable), "public", "products_extra_attributes");
        addMetadata();
    }

    public QProductsExtraAttributes(String variable, String schema, String table) {
        super(QProductsExtraAttributes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductsExtraAttributes(String variable, String schema) {
        super(QProductsExtraAttributes.class, forVariable(variable), schema, "products_extra_attributes");
        addMetadata();
    }

    public QProductsExtraAttributes(Path<? extends QProductsExtraAttributes> path) {
        super(path.getType(), path.getMetadata(), "public", "products_extra_attributes");
        addMetadata();
    }

    public QProductsExtraAttributes(PathMetadata metadata) {
        super(QProductsExtraAttributes.class, metadata, "public", "products_extra_attributes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(extraAttributeId, ColumnMetadata.named("extra_attribute_id").withIndex(2).ofType(Types.INTEGER).withSize(10));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(value, ColumnMetadata.named("value").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(variantId, ColumnMetadata.named("variant_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

