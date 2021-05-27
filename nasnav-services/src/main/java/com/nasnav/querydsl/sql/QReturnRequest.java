package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QReturnRequest is a Querydsl query type for QReturnRequest
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QReturnRequest extends com.querydsl.sql.RelationalPathBase<QReturnRequest> {

    private static final long serialVersionUID = -783744695;

    public static final QReturnRequest returnRequest = new QReturnRequest("return_request");

    public final NumberPath<Long> createdByEmployee = createNumber("createdByEmployee", Long.class);

    public final NumberPath<Long> createdByUser = createNumber("createdByUser", Long.class);

    public final DateTimePath<java.sql.Timestamp> createdOn = createDateTime("createdOn", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> metaOrderId = createNumber("metaOrderId", Long.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QReturnRequest> returnRequestPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QEmployeeUsers> returnRequestCreatedByEmployeeFkey = createForeignKey(createdByEmployee, "id");

    public final com.querydsl.sql.ForeignKey<QUsers> returnRequestCreatedByUserFkey = createForeignKey(createdByUser, "id");

    public final com.querydsl.sql.ForeignKey<QMetaOrders> returnRequestMetaOrderIdFkey = createForeignKey(metaOrderId, "id");

    public final com.querydsl.sql.ForeignKey<QReturnRequestItem> _returnRequestItemReturnRequestIdFkey = createInvForeignKey(id, "return_request_id");

    public QReturnRequest(String variable) {
        super(QReturnRequest.class, forVariable(variable), "public", "return_request");
        addMetadata();
    }

    public QReturnRequest(String variable, String schema, String table) {
        super(QReturnRequest.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QReturnRequest(String variable, String schema) {
        super(QReturnRequest.class, forVariable(variable), schema, "return_request");
        addMetadata();
    }

    public QReturnRequest(Path<? extends QReturnRequest> path) {
        super(path.getType(), path.getMetadata(), "public", "return_request");
        addMetadata();
    }

    public QReturnRequest(PathMetadata metadata) {
        super(QReturnRequest.class, metadata, "public", "return_request");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdByEmployee, ColumnMetadata.named("created_by_employee").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(createdByUser, ColumnMetadata.named("created_by_user").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(createdOn, ColumnMetadata.named("created_on").withIndex(2).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(metaOrderId, ColumnMetadata.named("meta_order_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(status, ColumnMetadata.named("status").withIndex(6).ofType(Types.INTEGER).withSize(10));
    }

}

