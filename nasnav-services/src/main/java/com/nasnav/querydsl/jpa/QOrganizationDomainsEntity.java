package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OrganizationDomainsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganizationDomainsEntity is a Querydsl query type for OrganizationDomainsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganizationDomainsEntity extends EntityPathBase<OrganizationDomainsEntity> {

    private static final long serialVersionUID = -807673486L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganizationDomainsEntity organizationDomainsEntity = new QOrganizationDomainsEntity("organizationDomainsEntity");

    public final StringPath domain = createString("domain");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrganizationEntity organizationEntity;

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final StringPath subdir = createString("subdir");

    public QOrganizationDomainsEntity(String variable) {
        this(OrganizationDomainsEntity.class, forVariable(variable), INITS);
    }

    public QOrganizationDomainsEntity(Path<? extends OrganizationDomainsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganizationDomainsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganizationDomainsEntity(PathMetadata metadata, PathInits inits) {
        this(OrganizationDomainsEntity.class, metadata, inits);
    }

    public QOrganizationDomainsEntity(Class<? extends OrganizationDomainsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
    }

}

