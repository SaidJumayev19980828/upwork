package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.IntegrationParamEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIntegrationParamEntity is a Querydsl query type for IntegrationParamEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QIntegrationParamEntity extends EntityPathBase<IntegrationParamEntity> {

    private static final long serialVersionUID = -432058807L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIntegrationParamEntity integrationParamEntity = new QIntegrationParamEntity("integrationParamEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath paramValue = createString("paramValue");

    public final QIntegrationParamTypeEntity type;

    public QIntegrationParamEntity(String variable) {
        this(IntegrationParamEntity.class, forVariable(variable), INITS);
    }

    public QIntegrationParamEntity(Path<? extends IntegrationParamEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIntegrationParamEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIntegrationParamEntity(PathMetadata metadata, PathInits inits) {
        this(IntegrationParamEntity.class, metadata, inits);
    }

    public QIntegrationParamEntity(Class<? extends IntegrationParamEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.type = inits.isInitialized("type") ? new QIntegrationParamTypeEntity(forProperty("type")) : null;
    }

}

