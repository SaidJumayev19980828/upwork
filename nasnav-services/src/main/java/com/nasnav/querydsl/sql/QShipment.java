package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShipment is a Querydsl query type for QShipment
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShipment extends com.querydsl.sql.RelationalPathBase<QShipment> {

    private static final long serialVersionUID = 1146612048;

    public static final QShipment shipment = new QShipment("shipment");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> deliveryFrom = createDateTime("deliveryFrom", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> deliveryUntil = createDateTime("deliveryUntil", java.sql.Timestamp.class);

    public final StringPath externalId = createString("externalId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath parameters = createString("parameters");

    public final NumberPath<java.math.BigDecimal> shippingFee = createNumber("shippingFee", java.math.BigDecimal.class);

    public final StringPath shippingServiceId = createString("shippingServiceId");

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<Long> subOrderId = createNumber("subOrderId", Long.class);

    public final StringPath trackNumber = createString("trackNumber");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QShipment> shipmentPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrders> shipmentSubOrderIdFkey = createForeignKey(subOrderId, "id");

    public QShipment(String variable) {
        super(QShipment.class, forVariable(variable), "public", "shipment");
        addMetadata();
    }

    public QShipment(String variable, String schema, String table) {
        super(QShipment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShipment(String variable, String schema) {
        super(QShipment.class, forVariable(variable), schema, "shipment");
        addMetadata();
    }

    public QShipment(Path<? extends QShipment> path) {
        super(path.getType(), path.getMetadata(), "public", "shipment");
        addMetadata();
    }

    public QShipment(PathMetadata metadata) {
        super(QShipment.class, metadata, "public", "shipment");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(deliveryFrom, ColumnMetadata.named("delivery_from").withIndex(11).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(deliveryUntil, ColumnMetadata.named("delivery_until").withIndex(12).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(externalId, ColumnMetadata.named("external_id").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(parameters, ColumnMetadata.named("parameters").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(shippingFee, ColumnMetadata.named("shipping_fee").withIndex(10).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(shippingServiceId, ColumnMetadata.named("shipping_service_id").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(subOrderId, ColumnMetadata.named("sub_order_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(trackNumber, ColumnMetadata.named("track_number").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
    }

}

