package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QReturnShipment is a Querydsl query type for QReturnShipment
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QReturnShipment extends com.querydsl.sql.RelationalPathBase<QReturnShipment> {

    private static final long serialVersionUID = 1350646304;

    public static final QReturnShipment returnShipment = new QReturnShipment("return_shipment");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath externalId = createString("externalId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath shippingServiceId = createString("shippingServiceId");

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final StringPath trackNumber = createString("trackNumber");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QReturnShipment> returnShipmentPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QReturnRequestItem> _returnRequestItemReturnShipmentIdFkey = createInvForeignKey(id, "return_shipment_id");

    public QReturnShipment(String variable) {
        super(QReturnShipment.class, forVariable(variable), "public", "return_shipment");
        addMetadata();
    }

    public QReturnShipment(String variable, String schema, String table) {
        super(QReturnShipment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QReturnShipment(String variable, String schema) {
        super(QReturnShipment.class, forVariable(variable), schema, "return_shipment");
        addMetadata();
    }

    public QReturnShipment(Path<? extends QReturnShipment> path) {
        super(path.getType(), path.getMetadata(), "public", "return_shipment");
        addMetadata();
    }

    public QReturnShipment(PathMetadata metadata) {
        super(QReturnShipment.class, metadata, "public", "return_shipment");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(2).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(externalId, ColumnMetadata.named("external_id").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(shippingServiceId, ColumnMetadata.named("shipping_service_id").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(trackNumber, ColumnMetadata.named("track_number").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
    }

}

