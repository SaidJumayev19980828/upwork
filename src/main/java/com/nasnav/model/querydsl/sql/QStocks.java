package com.nasnav.model.querydsl.sql;

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

    private static final long serialVersionUID = 290861176;

    public static final QStocks stocks = new QStocks("stocks");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> currency = createNumber("currency", Integer.class);

    public final NumberPath<java.math.BigDecimal> discount = createNumber("discount", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final NumberPath<Long> variantId = createNumber("variantId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QStocks> stocksPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QProductVariants> stocksVariantIdFkey = createForeignKey(variantId, "id");

    public final com.querydsl.sql.ForeignKey<QShops> railsD8eb88b3bfFk = createForeignKey(shopId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> stocksOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QBaskets> _basketsStockIdFkey = createInvForeignKey(id, "stock_id");

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
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(currency, ColumnMetadata.named("currency").withIndex(11).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(discount, ColumnMetadata.named("discount").withIndex(9).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(location, ColumnMetadata.named("location").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(price, ColumnMetadata.named("price").withIndex(8).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(quantity, ColumnMetadata.named("quantity").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(variantId, ColumnMetadata.named("variant_id").withIndex(10).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

