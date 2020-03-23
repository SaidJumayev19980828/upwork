package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizations is a Querydsl query type for QOrganizations
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizations extends com.querydsl.sql.RelationalPathBase<QOrganizations> {

    private static final long serialVersionUID = 42789093;

    public static final QOrganizations organizations = new QOrganizations("organizations");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logo = createString("logo");

    public final StringPath name = createString("name");

    public final StringPath pName = createString("pName");

    public final NumberPath<Integer> themeId = createNumber("themeId", Integer.class);

    public final StringPath type = createString("type");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QOrganizations> organizationsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QScenes> _railsD232c97110Fk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QProducts> _productsOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QSubscribedUsers> _rails1038aeed15Fk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QShopSections> _rails102545b523Fk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QStocks> _stocksOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QOrganizationImages> _organizationImagesOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QTags> _tagsOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QOrganizationManagers> _rails3c41486b32Fk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QBrands> _brandsOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QOrganizationThemes> _rails66b5304bc3Fk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QWorkTimes> _railsEed183b2b2Fk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QOrganizationDomains> _newtableFk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QIntegrationParam> _integrationParamOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QShopFloors> _rails6333433b00Fk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QProductPositions> _railsFefa61a65aFk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QOauth2Users> _oauth2UsersOrg = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QProductFeatures> _productFeaturesOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QIntegrationEventFailure> _integrationEventFailureOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QRoles> _rolesOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QUsers> _usersOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QFeatures> _rails69ca5ba774Fk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QFiles> _filesOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QEmployeeUsers> _employeeUsersOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QSocialLinks> _rails9b1a7e5d8eFk = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QIntegrationMapping> _integrationMappingOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public final com.querydsl.sql.ForeignKey<QShops> _shopsOrganizationIdFkey = createInvForeignKey(id, "organization_id");

    public QOrganizations(String variable) {
        super(QOrganizations.class, forVariable(variable), "public", "organizations");
        addMetadata();
    }

    public QOrganizations(String variable, String schema, String table) {
        super(QOrganizations.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizations(String variable, String schema) {
        super(QOrganizations.class, forVariable(variable), schema, "organizations");
        addMetadata();
    }

    public QOrganizations(Path<? extends QOrganizations> path) {
        super(path.getType(), path.getMetadata(), "public", "organizations");
        addMetadata();
    }

    public QOrganizations(PathMetadata metadata) {
        super(QOrganizations.class, metadata, "public", "organizations");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(description, ColumnMetadata.named("description").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(themeId, ColumnMetadata.named("theme_id").withIndex(9).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(type, ColumnMetadata.named("type").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

