package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.IntegrationParamTypeEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QIntegrationParamTypeEntity is a Querydsl query type for IntegrationParamTypeEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QIntegrationParamTypeEntity extends EntityPathBase<IntegrationParamTypeEntity> {

    private static final long serialVersionUID = -378186973L;

    public static final QIntegrationParamTypeEntity integrationParamTypeEntity = new QIntegrationParamTypeEntity("integrationParamTypeEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isMandatory = createBoolean("isMandatory");

    public final StringPath typeName = createString("typeName");

    public QIntegrationParamTypeEntity(String variable) {
        super(IntegrationParamTypeEntity.class, forVariable(variable));
    }

    public QIntegrationParamTypeEntity(Path<? extends IntegrationParamTypeEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIntegrationParamTypeEntity(PathMetadata metadata) {
        super(IntegrationParamTypeEntity.class, metadata);
    }

}

