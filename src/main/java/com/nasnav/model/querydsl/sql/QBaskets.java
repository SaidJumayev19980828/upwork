package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QBaskets is a Querydsl query type for QBaskets
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QBaskets extends com.querydsl.sql.RelationalPathBase<QBaskets> {

    private static final long serialVersionUID = 1979041394;

    public static final QBaskets baskets = new QBaskets("baskets");

    public final NumberPath<Integer> currency = createNumber("currency", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> quantity = createNumber("quantity", java.math.BigDecimal.class);

    public final NumberPath<Long> stockId = createNumber("stockId", Long.class);

    public final com.querydsl.sql.ForeignKey<QOrders> basketsOrderIdFkey = createForeignKey(orderId, "id");

    public final com.querydsl.sql.ForeignKey<QStocks> basketsStockIdFkey = createForeignKey(stockId, "id");

    public QBaskets(String variable) {
        super(QBaskets.class, forVariable(variable), "public", "baskets");
        addMetadata();
    }

    public QBaskets(String variable, String schema, String table) {
        super(QBaskets.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QBaskets(String variable, String schema) {
        super(QBaskets.class, forVariable(variable), schema, "baskets");
        addMetadata();
    }

    public QBaskets(Path<? extends QBaskets> path) {
        super(path.getType(), path.getMetadata(), "public", "baskets");
        addMetadata();
    }

    public QBaskets(PathMetadata metadata) {
        super(QBaskets.class, metadata, "public", "baskets");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(currency, ColumnMetadata.named("currency").withIndex(6).ofType(Types.INTEGER).withSize(10));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(orderId, ColumnMetadata.named("order_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(price, ColumnMetadata.named("price").withIndex(5).ofType(Types.NUMERIC).withSize(10).withDigits(2).notNull());
        addMetadata(quantity, ColumnMetadata.named("quantity").withIndex(4).ofType(Types.NUMERIC).withSize(10).withDigits(2).notNull());
        addMetadata(stockId, ColumnMetadata.named("stock_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

