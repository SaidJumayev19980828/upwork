package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OrganizationImagesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganizationImagesEntity is a Querydsl query type for OrganizationImagesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganizationImagesEntity extends EntityPathBase<OrganizationImagesEntity> {

    private static final long serialVersionUID = -1297114117L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganizationImagesEntity organizationImagesEntity = new QOrganizationImagesEntity("organizationImagesEntity");

    public final org.springframework.data.jpa.domain.QAbstractPersistable _super = new org.springframework.data.jpa.domain.QAbstractPersistable(this);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrganizationEntity organizationEntity;

    public final QShopsEntity shopsEntity;

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final StringPath uri = createString("uri");

    public QOrganizationImagesEntity(String variable) {
        this(OrganizationImagesEntity.class, forVariable(variable), INITS);
    }

    public QOrganizationImagesEntity(Path<? extends OrganizationImagesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganizationImagesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganizationImagesEntity(PathMetadata metadata, PathInits inits) {
        this(OrganizationImagesEntity.class, metadata, inits);
    }

    public QOrganizationImagesEntity(Class<? extends OrganizationImagesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
        this.shopsEntity = inits.isInitialized("shopsEntity") ? new QShopsEntity(forProperty("shopsEntity"), inits.get("shopsEntity")) : null;
    }

}

