package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QIntegrationMapping is a Querydsl query type for QIntegrationMapping
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QIntegrationMapping extends com.querydsl.sql.RelationalPathBase<QIntegrationMapping> {

    private static final long serialVersionUID = -570388336;

    public static final QIntegrationMapping integrationMapping = new QIntegrationMapping("integration_mapping");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath localValue = createString("localValue");

    public final NumberPath<Long> mappingType = createNumber("mappingType", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath remoteValue = createString("remoteValue");

    public final com.querydsl.sql.PrimaryKey<QIntegrationMapping> integrationMappingPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QIntegrationMappingType> integrationMappingMappingTypeFkey = createForeignKey(mappingType, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> integrationMappingOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QIntegrationMapping(String variable) {
        super(QIntegrationMapping.class, forVariable(variable), "public", "integration_mapping");
        addMetadata();
    }

    public QIntegrationMapping(String variable, String schema, String table) {
        super(QIntegrationMapping.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QIntegrationMapping(String variable, String schema) {
        super(QIntegrationMapping.class, forVariable(variable), schema, "integration_mapping");
        addMetadata();
    }

    public QIntegrationMapping(Path<? extends QIntegrationMapping> path) {
        super(path.getType(), path.getMetadata(), "public", "integration_mapping");
        addMetadata();
    }

    public QIntegrationMapping(PathMetadata metadata) {
        super(QIntegrationMapping.class, metadata, "public", "integration_mapping");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(localValue, ColumnMetadata.named("local_value").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(mappingType, ColumnMetadata.named("mapping_type").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(remoteValue, ColumnMetadata.named("remote_value").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

