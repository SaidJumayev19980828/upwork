package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPaymentRefunds is a Querydsl query type for QPaymentRefunds
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPaymentRefunds extends com.querydsl.sql.RelationalPathBase<QPaymentRefunds> {

    private static final long serialVersionUID = -1154311605;

    public static final QPaymentRefunds paymentRefunds = new QPaymentRefunds("payment_refunds");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final NumberPath<Integer> currency = createNumber("currency", Integer.class);

    public final DateTimePath<java.sql.Timestamp> executed = createDateTime("executed", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath object = createString("object");

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final StringPath uid = createString("uid");

    public final com.querydsl.sql.PrimaryKey<QPaymentRefunds> paymentRefundsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QPayments> paymentRefundsPaymentIdFkey = createForeignKey(paymentId, "id");

    public QPaymentRefunds(String variable) {
        super(QPaymentRefunds.class, forVariable(variable), "public", "payment_refunds");
        addMetadata();
    }

    public QPaymentRefunds(String variable, String schema, String table) {
        super(QPaymentRefunds.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPaymentRefunds(String variable, String schema) {
        super(QPaymentRefunds.class, forVariable(variable), schema, "payment_refunds");
        addMetadata();
    }

    public QPaymentRefunds(Path<? extends QPaymentRefunds> path) {
        super(path.getType(), path.getMetadata(), "public", "payment_refunds");
        addMetadata();
    }

    public QPaymentRefunds(PathMetadata metadata) {
        super(QPaymentRefunds.class, metadata, "public", "payment_refunds");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(5).ofType(Types.NUMERIC).withSize(10).withDigits(2).notNull());
        addMetadata(currency, ColumnMetadata.named("currency").withIndex(6).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(executed, ColumnMetadata.named("executed").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(object, ColumnMetadata.named("object").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(paymentId, ColumnMetadata.named("payment_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(uid, ColumnMetadata.named("uid").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

