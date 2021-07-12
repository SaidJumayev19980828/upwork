package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganiztionCartOptimization is a Querydsl query type for QOrganiztionCartOptimization
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganiztionCartOptimization extends com.querydsl.sql.RelationalPathBase<QOrganiztionCartOptimization> {

    private static final long serialVersionUID = -435881363;

    public static final QOrganiztionCartOptimization organiztionCartOptimization = new QOrganiztionCartOptimization("organiztion_cart_optimization");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath optimizationParameters = createString("optimizationParameters");

    public final StringPath optimizationStrategy = createString("optimizationStrategy");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath shippingServiceId = createString("shippingServiceId");

    public final com.querydsl.sql.PrimaryKey<QOrganiztionCartOptimization> organiztionCartOptimizationPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> organiztionCartOptimizationOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QOrganiztionCartOptimization(String variable) {
        super(QOrganiztionCartOptimization.class, forVariable(variable), "public", "organiztion_cart_optimization");
        addMetadata();
    }

    public QOrganiztionCartOptimization(String variable, String schema, String table) {
        super(QOrganiztionCartOptimization.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganiztionCartOptimization(String variable, String schema) {
        super(QOrganiztionCartOptimization.class, forVariable(variable), schema, "organiztion_cart_optimization");
        addMetadata();
    }

    public QOrganiztionCartOptimization(Path<? extends QOrganiztionCartOptimization> path) {
        super(path.getType(), path.getMetadata(), "public", "organiztion_cart_optimization");
        addMetadata();
    }

    public QOrganiztionCartOptimization(PathMetadata metadata) {
        super(QOrganiztionCartOptimization.class, metadata, "public", "organiztion_cart_optimization");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(optimizationParameters, ColumnMetadata.named("optimization_parameters").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(optimizationStrategy, ColumnMetadata.named("optimization_strategy").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(shippingServiceId, ColumnMetadata.named("shipping_service_id").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

