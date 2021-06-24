package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QStocks is a Querydsl query type for QStocks
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QStocks extends com.querydsl.sql.RelationalPathBase<QStocks> {

    private static final long serialVersionUID = -1015490221;

    public static final QStocks stocks = new QStocks("stocks");

    public final NumberPath<Integer> currency = createNumber("currency", Integer.class);

    public final NumberPath<java.math.BigDecimal> discount = createNumber("discount", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final NumberPath<Integer> unitId = createNumber("unitId", Integer.class);

    public final NumberPath<Long> variantId = createNumber("variantId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QStocks> stocksPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShops> railsD8eb88b3bfFk = createForeignKey(shopId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> stocksOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QUnits> stocksUnitIdFkey = createForeignKey(unitId, "id");

    public final com.querydsl.sql.ForeignKey<QProductVariants> stocksVariantIdFkey = createForeignKey(variantId, "id");

    public final com.querydsl.sql.ForeignKey<QBaskets> _basketsStockIdFkey = createInvForeignKey(id, "stock_id");

    public final com.querydsl.sql.ForeignKey<QCartItems> _cartItemsStockIdFkey = createInvForeignKey(id, "stock_id");

    public final com.querydsl.sql.ForeignKey<QProductBundles> _productBundlesBundleStockIdFkey = createInvForeignKey(id, "bundle_stock_id");

    public QStocks(String variable) {
        super(QStocks.class, forVariable(variable), "public", "stocks");
        addMetadata();
    }

    public QStocks(String variable, String schema, String table) {
        super(QStocks.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStocks(String variable, String schema) {
        super(QStocks.class, forVariable(variable), schema, "stocks");
        addMetadata();
    }

    public QStocks(Path<? extends QStocks> path) {
        super(path.getType(), path.getMetadata(), "public", "stocks");
        addMetadata();
    }

    public QStocks(PathMetadata metadata) {
        super(QStocks.class, metadata, "public", "stocks");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(currency, ColumnMetadata.named("currency").withIndex(8).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(discount, ColumnMetadata.named("discount").withIndex(6).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(price, ColumnMetadata.named("price").withIndex(5).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(quantity, ColumnMetadata.named("quantity").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(unitId, ColumnMetadata.named("unit_id").withIndex(9).ofType(Types.INTEGER).withSize(10));
        addMetadata(variantId, ColumnMetadata.named("variant_id").withIndex(7).ofType(Types.BIGINT).withSize(19));
    }

}

