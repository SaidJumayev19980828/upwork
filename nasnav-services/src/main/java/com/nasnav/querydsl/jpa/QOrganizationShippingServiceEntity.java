package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OrganizationShippingServiceEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganizationShippingServiceEntity is a Querydsl query type for OrganizationShippingServiceEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganizationShippingServiceEntity extends EntityPathBase<OrganizationShippingServiceEntity> {

    private static final long serialVersionUID = 2102420586L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganizationShippingServiceEntity organizationShippingServiceEntity = new QOrganizationShippingServiceEntity("organizationShippingServiceEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrganizationEntity organization;

    public final StringPath serviceId = createString("serviceId");

    public final StringPath serviceParameters = createString("serviceParameters");

    public QOrganizationShippingServiceEntity(String variable) {
        this(OrganizationShippingServiceEntity.class, forVariable(variable), INITS);
    }

    public QOrganizationShippingServiceEntity(Path<? extends OrganizationShippingServiceEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganizationShippingServiceEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganizationShippingServiceEntity(PathMetadata metadata, PathInits inits) {
        this(OrganizationShippingServiceEntity.class, metadata, inits);
    }

    public QOrganizationShippingServiceEntity(Class<? extends OrganizationShippingServiceEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
    }

}

