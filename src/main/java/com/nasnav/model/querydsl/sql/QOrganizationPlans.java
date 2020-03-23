package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationPlans is a Querydsl query type for QOrganizationPlans
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationPlans extends com.querydsl.sql.RelationalPathBase<QOrganizationPlans> {

    private static final long serialVersionUID = -1397143108;

    public static final QOrganizationPlans organizationPlans = new QOrganizationPlans("organization_plans");

    public final StringPath country = createString("country");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<java.math.BigInteger> price = createNumber("price", java.math.BigInteger.class);

    public final NumberPath<Integer> storesCount = createNumber("storesCount", Integer.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QOrganizationPlans> organizationPlansPkey = createPrimaryKey(id);

    public QOrganizationPlans(String variable) {
        super(QOrganizationPlans.class, forVariable(variable), "public", "organization_plans");
        addMetadata();
    }

    public QOrganizationPlans(String variable, String schema, String table) {
        super(QOrganizationPlans.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationPlans(String variable, String schema) {
        super(QOrganizationPlans.class, forVariable(variable), schema, "organization_plans");
        addMetadata();
    }

    public QOrganizationPlans(Path<? extends QOrganizationPlans> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_plans");
        addMetadata();
    }

    public QOrganizationPlans(PathMetadata metadata) {
        super(QOrganizationPlans.class, metadata, "public", "organization_plans");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(country, ColumnMetadata.named("country").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(price, ColumnMetadata.named("price").withIndex(4).ofType(Types.NUMERIC).withSize(131089));
        addMetadata(storesCount, ColumnMetadata.named("stores_count").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

