package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMetaOrders is a Querydsl query type for QMetaOrders
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMetaOrders extends com.querydsl.sql.RelationalPathBase<QMetaOrders> {

    private static final long serialVersionUID = 1110273920;

    public static final QMetaOrders metaOrders = new QMetaOrders("meta_orders");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<java.math.BigDecimal> discounts = createNumber("discounts", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> grandTotal = createNumber("grandTotal", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath notes = createString("notes");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<java.math.BigDecimal> shippingTotal = createNumber("shippingTotal", java.math.BigDecimal.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<java.math.BigDecimal> subTotal = createNumber("subTotal", java.math.BigDecimal.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QMetaOrders> metaOrdersPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QMetaOrdersPromotions> _metaOrdersPromotionsMetaOrderFkey = createInvForeignKey(id, "meta_order");

    public final com.querydsl.sql.ForeignKey<QOrders> _ordersMetaOrderIdFkey = createInvForeignKey(id, "meta_order_id");

    public final com.querydsl.sql.ForeignKey<QPayments> _paymentsMetaOrderIdFkey = createInvForeignKey(id, "meta_order_id");

    public final com.querydsl.sql.ForeignKey<QReturnRequest> _returnRequestMetaOrderIdFkey = createInvForeignKey(id, "meta_order_id");

    public QMetaOrders(String variable) {
        super(QMetaOrders.class, forVariable(variable), "public", "meta_orders");
        addMetadata();
    }

    public QMetaOrders(String variable, String schema, String table) {
        super(QMetaOrders.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMetaOrders(String variable, String schema) {
        super(QMetaOrders.class, forVariable(variable), schema, "meta_orders");
        addMetadata();
    }

    public QMetaOrders(Path<? extends QMetaOrders> path) {
        super(path.getType(), path.getMetadata(), "public", "meta_orders");
        addMetadata();
    }

    public QMetaOrders(PathMetadata metadata) {
        super(QMetaOrders.class, metadata, "public", "meta_orders");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(2).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(discounts, ColumnMetadata.named("discounts").withIndex(9).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(grandTotal, ColumnMetadata.named("grand_total").withIndex(8).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(notes, ColumnMetadata.named("notes").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(shippingTotal, ColumnMetadata.named("shipping_total").withIndex(7).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(status, ColumnMetadata.named("status").withIndex(5).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(subTotal, ColumnMetadata.named("sub_total").withIndex(6).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
    }

}

