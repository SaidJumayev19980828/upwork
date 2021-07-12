package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationDomains is a Querydsl query type for QOrganizationDomains
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationDomains extends com.querydsl.sql.RelationalPathBase<QOrganizationDomains> {

    private static final long serialVersionUID = 145652230;

    public static final QOrganizationDomains organizationDomains = new QOrganizationDomains("organization_domains");

    public final NumberPath<Integer> canonical = createNumber("canonical", Integer.class);

    public final StringPath domain = createString("domain");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath subdir = createString("subdir");

    public final com.querydsl.sql.PrimaryKey<QOrganizationDomains> organizationDomainsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> organizationDomainsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QOrganizationDomains(String variable) {
        super(QOrganizationDomains.class, forVariable(variable), "public", "organization_domains");
        addMetadata();
    }

    public QOrganizationDomains(String variable, String schema, String table) {
        super(QOrganizationDomains.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationDomains(String variable, String schema) {
        super(QOrganizationDomains.class, forVariable(variable), schema, "organization_domains");
        addMetadata();
    }

    public QOrganizationDomains(Path<? extends QOrganizationDomains> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_domains");
        addMetadata();
    }

    public QOrganizationDomains(PathMetadata metadata) {
        super(QOrganizationDomains.class, metadata, "public", "organization_domains");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(canonical, ColumnMetadata.named("canonical").withIndex(5).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(domain, ColumnMetadata.named("domain").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(subdir, ColumnMetadata.named("subdir").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

