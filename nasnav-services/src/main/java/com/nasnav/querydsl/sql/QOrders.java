package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QOrders is a Querydsl query type for QOrders
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrders extends com.querydsl.sql.RelationalPathBase<QOrders> {

    private static final long serialVersionUID = 174171968;

    public static final QOrders orders = new QOrders("orders");

    public final StringPath address = createString("address");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final StringPath basket = createString("basket");

    public final SimplePath<String[]> cancelationReasons = createSimple("cancelationReasons", String[].class);

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> dateDelivery = createDateTime("dateDelivery", java.sql.Timestamp.class);

    public final StringPath driverName = createString("driverName");

    public final StringPath email = createString("email");

    public final BooleanPath equipped = createBoolean("equipped");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final NumberPath<Integer> paymentStatus = createNumber("paymentStatus", Integer.class);

    public final NumberPath<Integer> paymentType = createNumber("paymentType", Integer.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QOrders> ordersPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QUsers> railsF868b47f6aFk = createForeignKey(userId, "id");

    public final com.querydsl.sql.ForeignKey<QShops> ordersShopIdFkey = createForeignKey(shopId, "id");

    public final com.querydsl.sql.ForeignKey<QBaskets> _basketsOrderIdFkey = createInvForeignKey(id, "order_id");

    public final com.querydsl.sql.ForeignKey<QPayments> _paymentsOrderIdFkey = createInvForeignKey(id, "order_id");

    public QOrders(String variable) {
        super(QOrders.class, forVariable(variable), "public", "orders");
        addMetadata();
    }

    public QOrders(String variable, String schema, String table) {
        super(QOrders.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrders(String variable, String schema) {
        super(QOrders.class, forVariable(variable), schema, "orders");
        addMetadata();
    }

    public QOrders(Path<? extends QOrders> path) {
        super(path.getType(), path.getMetadata(), "public", "orders");
        addMetadata();
    }

    public QOrders(PathMetadata metadata) {
        super(QOrders.class, metadata, "public", "orders");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(address, ColumnMetadata.named("address").withIndex(2).ofType(Types.VARCHAR).withSize(150));
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(16).ofType(Types.NUMERIC).withSize(10).withDigits(2).notNull());
        addMetadata(basket, ColumnMetadata.named("basket").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(cancelationReasons, ColumnMetadata.named("cancelation_reasons").withIndex(12).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(dateDelivery, ColumnMetadata.named("date_delivery").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(driverName, ColumnMetadata.named("driver_name").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(email, ColumnMetadata.named("email").withIndex(3).ofType(Types.VARCHAR).withSize(40));
        addMetadata(equipped, ColumnMetadata.named("equipped").withIndex(17).ofType(Types.BIT).withSize(1));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(4).ofType(Types.VARCHAR).withSize(40));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
        addMetadata(paymentId, ColumnMetadata.named("payment_id").withIndex(19).ofType(Types.BIGINT).withSize(19));
        addMetadata(paymentStatus, ColumnMetadata.named("payment_status").withIndex(18).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(paymentType, ColumnMetadata.named("payment_type").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(13).ofType(Types.BIGINT).withSize(19));
        addMetadata(status, ColumnMetadata.named("status").withIndex(11).ofType(Types.INTEGER).withSize(10));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
    }

}

