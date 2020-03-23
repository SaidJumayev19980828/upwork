package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QIntegrationParamType is a Querydsl query type for QIntegrationParamType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QIntegrationParamType extends com.querydsl.sql.RelationalPathBase<QIntegrationParamType> {

    private static final long serialVersionUID = 1111232270;

    public static final QIntegrationParamType integrationParamType = new QIntegrationParamType("integration_param_type");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isMandatory = createBoolean("isMandatory");

    public final StringPath typeName = createString("typeName");

    public final com.querydsl.sql.PrimaryKey<QIntegrationParamType> integrationParamTypePkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QIntegrationParam> _integrationParamParamTypeFkey = createInvForeignKey(id, "param_type");

    public QIntegrationParamType(String variable) {
        super(QIntegrationParamType.class, forVariable(variable), "public", "integration_param_type");
        addMetadata();
    }

    public QIntegrationParamType(String variable, String schema, String table) {
        super(QIntegrationParamType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QIntegrationParamType(String variable, String schema) {
        super(QIntegrationParamType.class, forVariable(variable), schema, "integration_param_type");
        addMetadata();
    }

    public QIntegrationParamType(Path<? extends QIntegrationParamType> path) {
        super(path.getType(), path.getMetadata(), "public", "integration_param_type");
        addMetadata();
    }

    public QIntegrationParamType(PathMetadata metadata) {
        super(QIntegrationParamType.class, metadata, "public", "integration_param_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(isMandatory, ColumnMetadata.named("is_mandatory").withIndex(3).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(typeName, ColumnMetadata.named("type_name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

