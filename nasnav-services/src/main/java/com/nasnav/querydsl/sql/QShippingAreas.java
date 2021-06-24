package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShippingAreas is a Querydsl query type for QShippingAreas
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShippingAreas extends com.querydsl.sql.RelationalPathBase<QShippingAreas> {

    private static final long serialVersionUID = 1168226946;

    public static final QShippingAreas shippingAreas = new QShippingAreas("shipping_areas");

    public final NumberPath<Long> areaId = createNumber("areaId", Long.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath providerId = createString("providerId");

    public final StringPath shippingServiceId = createString("shippingServiceId");

    public final com.querydsl.sql.PrimaryKey<QShippingAreas> shippingAreasPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QAreas> shippingAreasAreaIdFkey = createForeignKey(areaId, "id");

    public final com.querydsl.sql.ForeignKey<QShippingService> shippingAreasShippingServiceIdFkey = createForeignKey(shippingServiceId, "id");

    public QShippingAreas(String variable) {
        super(QShippingAreas.class, forVariable(variable), "public", "shipping_areas");
        addMetadata();
    }

    public QShippingAreas(String variable, String schema, String table) {
        super(QShippingAreas.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShippingAreas(String variable, String schema) {
        super(QShippingAreas.class, forVariable(variable), schema, "shipping_areas");
        addMetadata();
    }

    public QShippingAreas(Path<? extends QShippingAreas> path) {
        super(path.getType(), path.getMetadata(), "public", "shipping_areas");
        addMetadata();
    }

    public QShippingAreas(PathMetadata metadata) {
        super(QShippingAreas.class, metadata, "public", "shipping_areas");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaId, ColumnMetadata.named("area_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(providerId, ColumnMetadata.named("provider_id").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(shippingServiceId, ColumnMetadata.named("shipping_service_id").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

