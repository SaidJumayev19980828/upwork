package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QReturnRequestItem is a Querydsl query type for QReturnRequestItem
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QReturnRequestItem extends com.querydsl.sql.RelationalPathBase<QReturnRequestItem> {

    private static final long serialVersionUID = 386409468;

    public static final QReturnRequestItem returnRequestItem = new QReturnRequestItem("return_request_item");

    public final NumberPath<Long> createdByEmployee = createNumber("createdByEmployee", Long.class);

    public final NumberPath<Long> createdByUser = createNumber("createdByUser", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> orderItemId = createNumber("orderItemId", Long.class);

    public final NumberPath<Long> receivedBy = createNumber("receivedBy", Long.class);

    public final DateTimePath<java.sql.Timestamp> receivedOn = createDateTime("receivedOn", java.sql.Timestamp.class);

    public final NumberPath<Integer> receivedQuantity = createNumber("receivedQuantity", Integer.class);

    public final NumberPath<Integer> returnedQuantity = createNumber("returnedQuantity", Integer.class);

    public final NumberPath<Long> returnRequestId = createNumber("returnRequestId", Long.class);

    public final NumberPath<Long> returnShipmentId = createNumber("returnShipmentId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QReturnRequestItem> returnRequestItemPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QEmployeeUsers> returnRequestItemCreatedByEmployeeFkey = createForeignKey(createdByEmployee, "id");

    public final com.querydsl.sql.ForeignKey<QUsers> returnRequestItemCreatedByUserFkey = createForeignKey(createdByUser, "id");

    public final com.querydsl.sql.ForeignKey<QBaskets> returnRequestItemOrderItemIdFkey = createForeignKey(orderItemId, "id");

    public final com.querydsl.sql.ForeignKey<QEmployeeUsers> returnRequestItemReceivedByFkey = createForeignKey(receivedBy, "id");

    public final com.querydsl.sql.ForeignKey<QReturnRequest> returnRequestItemReturnRequestIdFkey = createForeignKey(returnRequestId, "id");

    public final com.querydsl.sql.ForeignKey<QReturnShipment> returnRequestItemReturnShipmentIdFkey = createForeignKey(returnShipmentId, "id");

    public QReturnRequestItem(String variable) {
        super(QReturnRequestItem.class, forVariable(variable), "public", "return_request_item");
        addMetadata();
    }

    public QReturnRequestItem(String variable, String schema, String table) {
        super(QReturnRequestItem.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QReturnRequestItem(String variable, String schema) {
        super(QReturnRequestItem.class, forVariable(variable), schema, "return_request_item");
        addMetadata();
    }

    public QReturnRequestItem(Path<? extends QReturnRequestItem> path) {
        super(path.getType(), path.getMetadata(), "public", "return_request_item");
        addMetadata();
    }

    public QReturnRequestItem(PathMetadata metadata) {
        super(QReturnRequestItem.class, metadata, "public", "return_request_item");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdByEmployee, ColumnMetadata.named("created_by_employee").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(createdByUser, ColumnMetadata.named("created_by_user").withIndex(8).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(orderItemId, ColumnMetadata.named("order_item_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(receivedBy, ColumnMetadata.named("received_by").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(receivedOn, ColumnMetadata.named("received_on").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(receivedQuantity, ColumnMetadata.named("received_quantity").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(returnedQuantity, ColumnMetadata.named("returned_quantity").withIndex(4).ofType(Types.INTEGER).withSize(10));
        addMetadata(returnRequestId, ColumnMetadata.named("return_request_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(returnShipmentId, ColumnMetadata.named("return_shipment_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
    }

}

