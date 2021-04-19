package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QIntegrationMappingType is a Querydsl query type for QIntegrationMappingType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QIntegrationMappingType extends com.querydsl.sql.RelationalPathBase<QIntegrationMappingType> {

    private static final long serialVersionUID = 707410191;

    public static final QIntegrationMappingType integrationMappingType = new QIntegrationMappingType("integration_mapping_type");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath typeName = createString("typeName");

    public final com.querydsl.sql.PrimaryKey<QIntegrationMappingType> integrationMappingTypePkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QIntegrationMapping> _integrationMappingMappingTypeFkey = createInvForeignKey(id, "mapping_type");

    public QIntegrationMappingType(String variable) {
        super(QIntegrationMappingType.class, forVariable(variable), "public", "integration_mapping_type");
        addMetadata();
    }

    public QIntegrationMappingType(String variable, String schema, String table) {
        super(QIntegrationMappingType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QIntegrationMappingType(String variable, String schema) {
        super(QIntegrationMappingType.class, forVariable(variable), schema, "integration_mapping_type");
        addMetadata();
    }

    public QIntegrationMappingType(Path<? extends QIntegrationMappingType> path) {
        super(path.getType(), path.getMetadata(), "public", "integration_mapping_type");
        addMetadata();
    }

    public QIntegrationMappingType(PathMetadata metadata) {
        super(QIntegrationMappingType.class, metadata, "public", "integration_mapping_type");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(typeName, ColumnMetadata.named("type_name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

