package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.IntegrationMappingTypeEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QIntegrationMappingTypeEntity is a Querydsl query type for IntegrationMappingTypeEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QIntegrationMappingTypeEntity extends EntityPathBase<IntegrationMappingTypeEntity> {

    private static final long serialVersionUID = -1312891932L;

    public static final QIntegrationMappingTypeEntity integrationMappingTypeEntity = new QIntegrationMappingTypeEntity("integrationMappingTypeEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath typeName = createString("typeName");

    public QIntegrationMappingTypeEntity(String variable) {
        super(IntegrationMappingTypeEntity.class, forVariable(variable));
    }

    public QIntegrationMappingTypeEntity(Path<? extends IntegrationMappingTypeEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIntegrationMappingTypeEntity(PathMetadata metadata) {
        super(IntegrationMappingTypeEntity.class, metadata);
    }

}

