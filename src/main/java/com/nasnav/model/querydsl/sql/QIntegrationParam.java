package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QIntegrationParam is a Querydsl query type for QIntegrationParam
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QIntegrationParam extends com.querydsl.sql.RelationalPathBase<QIntegrationParam> {

    private static final long serialVersionUID = 1018561972;

    public static final QIntegrationParam integrationParam = new QIntegrationParam("integration_param");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> paramType = createNumber("paramType", Long.class);

    public final StringPath paramValue = createString("paramValue");

    public final com.querydsl.sql.PrimaryKey<QIntegrationParam> integrationParamPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QIntegrationParamType> integrationParamParamTypeFkey = createForeignKey(paramType, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> integrationParamOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QIntegrationParam(String variable) {
        super(QIntegrationParam.class, forVariable(variable), "public", "integration_param");
        addMetadata();
    }

    public QIntegrationParam(String variable, String schema, String table) {
        super(QIntegrationParam.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QIntegrationParam(String variable, String schema) {
        super(QIntegrationParam.class, forVariable(variable), schema, "integration_param");
        addMetadata();
    }

    public QIntegrationParam(Path<? extends QIntegrationParam> path) {
        super(path.getType(), path.getMetadata(), "public", "integration_param");
        addMetadata();
    }

    public QIntegrationParam(PathMetadata metadata) {
        super(QIntegrationParam.class, metadata, "public", "integration_param");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(paramType, ColumnMetadata.named("param_type").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(paramValue, ColumnMetadata.named("param_value").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

