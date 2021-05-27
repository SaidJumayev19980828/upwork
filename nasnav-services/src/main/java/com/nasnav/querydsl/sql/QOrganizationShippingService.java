package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationShippingService is a Querydsl query type for QOrganizationShippingService
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationShippingService extends com.querydsl.sql.RelationalPathBase<QOrganizationShippingService> {

    private static final long serialVersionUID = 1843875838;

    public static final QOrganizationShippingService organizationShippingService = new QOrganizationShippingService("organization_shipping_service");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath serviceParameters = createString("serviceParameters");

    public final StringPath shippingServiceId = createString("shippingServiceId");

    public final com.querydsl.sql.PrimaryKey<QOrganizationShippingService> organizationShippingServicePkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> organizationShippingServiceOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QOrganizationShippingService(String variable) {
        super(QOrganizationShippingService.class, forVariable(variable), "public", "organization_shipping_service");
        addMetadata();
    }

    public QOrganizationShippingService(String variable, String schema, String table) {
        super(QOrganizationShippingService.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationShippingService(String variable, String schema) {
        super(QOrganizationShippingService.class, forVariable(variable), schema, "organization_shipping_service");
        addMetadata();
    }

    public QOrganizationShippingService(Path<? extends QOrganizationShippingService> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_shipping_service");
        addMetadata();
    }

    public QOrganizationShippingService(PathMetadata metadata) {
        super(QOrganizationShippingService.class, metadata, "public", "organization_shipping_service");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(serviceParameters, ColumnMetadata.named("service_parameters").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(shippingServiceId, ColumnMetadata.named("shipping_service_id").withIndex(1).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

