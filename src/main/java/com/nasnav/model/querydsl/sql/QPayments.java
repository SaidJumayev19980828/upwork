package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPayments is a Querydsl query type for QPayments
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPayments extends com.querydsl.sql.RelationalPathBase<QPayments> {

    private static final long serialVersionUID = 23898312;

    public static final QPayments payments = new QPayments("payments");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final NumberPath<Integer> currency = createNumber("currency", Integer.class);

    public final DateTimePath<java.sql.Timestamp> executed = createDateTime("executed", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath object = createString("object");

    public final StringPath operator = createString("operator");

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final StringPath uid = createString("uid");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QPayments> paymentsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrders> paymentsOrderIdFkey = createForeignKey(orderId, "id");

    public QPayments(String variable) {
        super(QPayments.class, forVariable(variable), "public", "payments");
        addMetadata();
    }

    public QPayments(String variable, String schema, String table) {
        super(QPayments.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPayments(String variable, String schema) {
        super(QPayments.class, forVariable(variable), schema, "payments");
        addMetadata();
    }

    public QPayments(Path<? extends QPayments> path) {
        super(path.getType(), path.getMetadata(), "public", "payments");
        addMetadata();
    }

    public QPayments(PathMetadata metadata) {
        super(QPayments.class, metadata, "public", "payments");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(7).ofType(Types.NUMERIC).withSize(10).withDigits(2).notNull());
        addMetadata(currency, ColumnMetadata.named("currency").withIndex(8).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(executed, ColumnMetadata.named("executed").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(object, ColumnMetadata.named("object").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(operator, ColumnMetadata.named("operator").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(orderId, ColumnMetadata.named("order_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(status, ColumnMetadata.named("status").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(uid, ColumnMetadata.named("uid").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
    }

}

