package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OrganizationCartOptimizationEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganizationCartOptimizationEntity is a Querydsl query type for OrganizationCartOptimizationEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganizationCartOptimizationEntity extends EntityPathBase<OrganizationCartOptimizationEntity> {

    private static final long serialVersionUID = -1912000912L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganizationCartOptimizationEntity organizationCartOptimizationEntity = new QOrganizationCartOptimizationEntity("organizationCartOptimizationEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath optimizationStrategy = createString("optimizationStrategy");

    public final QOrganizationEntity organization;

    public final StringPath parameters = createString("parameters");

    public final StringPath shippingServiceId = createString("shippingServiceId");

    public QOrganizationCartOptimizationEntity(String variable) {
        this(OrganizationCartOptimizationEntity.class, forVariable(variable), INITS);
    }

    public QOrganizationCartOptimizationEntity(Path<? extends OrganizationCartOptimizationEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganizationCartOptimizationEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganizationCartOptimizationEntity(PathMetadata metadata, PathInits inits) {
        this(OrganizationCartOptimizationEntity.class, metadata, inits);
    }

    public QOrganizationCartOptimizationEntity(Class<? extends OrganizationCartOptimizationEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
    }

}

