package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationPayments is a Querydsl query type for QOrganizationPayments
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationPayments extends com.querydsl.sql.RelationalPathBase<QOrganizationPayments> {

    private static final long serialVersionUID = -249002341;

    public static final QOrganizationPayments organizationPayments = new QOrganizationPayments("organization_payments");

    public final StringPath account = createString("account");

    public final StringPath gateway = createString("gateway");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QOrganizationPayments> organizationPaymentsPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> organizationPaymentsFk = createForeignKey(organizationId, "id");

    public QOrganizationPayments(String variable) {
        super(QOrganizationPayments.class, forVariable(variable), "public", "organization_payments");
        addMetadata();
    }

    public QOrganizationPayments(String variable, String schema, String table) {
        super(QOrganizationPayments.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationPayments(String variable, String schema) {
        super(QOrganizationPayments.class, forVariable(variable), schema, "organization_payments");
        addMetadata();
    }

    public QOrganizationPayments(Path<? extends QOrganizationPayments> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_payments");
        addMetadata();
    }

    public QOrganizationPayments(PathMetadata metadata) {
        super(QOrganizationPayments.class, metadata, "public", "organization_payments");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(account, ColumnMetadata.named("account").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(gateway, ColumnMetadata.named("gateway").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

