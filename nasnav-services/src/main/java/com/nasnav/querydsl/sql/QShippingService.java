package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShippingService is a Querydsl query type for QShippingService
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShippingService extends com.querydsl.sql.RelationalPathBase<QShippingService> {

    private static final long serialVersionUID = 115273905;

    public static final QShippingService shippingService = new QShippingService("shipping_service");

    public final StringPath additionalParameters = createString("additionalParameters");

    public final StringPath id = createString("id");

    public final StringPath serviceParameters = createString("serviceParameters");

    public final com.querydsl.sql.PrimaryKey<QShippingService> shippingServicePkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShippingAreas> _shippingAreasShippingServiceIdFkey = createInvForeignKey(id, "shipping_service_id");

    public QShippingService(String variable) {
        super(QShippingService.class, forVariable(variable), "public", "shipping_service");
        addMetadata();
    }

    public QShippingService(String variable, String schema, String table) {
        super(QShippingService.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShippingService(String variable, String schema) {
        super(QShippingService.class, forVariable(variable), schema, "shipping_service");
        addMetadata();
    }

    public QShippingService(Path<? extends QShippingService> path) {
        super(path.getType(), path.getMetadata(), "public", "shipping_service");
        addMetadata();
    }

    public QShippingService(PathMetadata metadata) {
        super(QShippingService.class, metadata, "public", "shipping_service");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalParameters, ColumnMetadata.named("additional_parameters").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(serviceParameters, ColumnMetadata.named("service_parameters").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

