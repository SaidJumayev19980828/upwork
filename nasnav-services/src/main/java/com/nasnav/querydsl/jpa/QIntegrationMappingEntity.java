package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.IntegrationMappingEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIntegrationMappingEntity is a Querydsl query type for IntegrationMappingEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QIntegrationMappingEntity extends EntityPathBase<IntegrationMappingEntity> {

    private static final long serialVersionUID = 1046830986L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIntegrationMappingEntity integrationMappingEntity = new QIntegrationMappingEntity("integrationMappingEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath localValue = createString("localValue");

    public final QIntegrationMappingTypeEntity mappingType;

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath remoteValue = createString("remoteValue");

    public QIntegrationMappingEntity(String variable) {
        this(IntegrationMappingEntity.class, forVariable(variable), INITS);
    }

    public QIntegrationMappingEntity(Path<? extends IntegrationMappingEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIntegrationMappingEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIntegrationMappingEntity(PathMetadata metadata, PathInits inits) {
        this(IntegrationMappingEntity.class, metadata, inits);
    }

    public QIntegrationMappingEntity(Class<? extends IntegrationMappingEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.mappingType = inits.isInitialized("mappingType") ? new QIntegrationMappingTypeEntity(forProperty("mappingType")) : null;
    }

}

