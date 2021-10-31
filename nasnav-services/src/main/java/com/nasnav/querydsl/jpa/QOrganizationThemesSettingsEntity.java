package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OrganizationThemesSettingsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganizationThemesSettingsEntity is a Querydsl query type for OrganizationThemesSettingsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganizationThemesSettingsEntity extends EntityPathBase<OrganizationThemesSettingsEntity> {

    private static final long serialVersionUID = -769011760L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganizationThemesSettingsEntity organizationThemesSettingsEntity = new QOrganizationThemesSettingsEntity("organizationThemesSettingsEntity");

    public final org.springframework.data.jpa.domain.QAbstractPersistable _super = new org.springframework.data.jpa.domain.QAbstractPersistable(this);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final QOrganizationEntity organizationEntity;

    public final StringPath settings = createString("settings");

    public final QThemeEntity theme;

    public QOrganizationThemesSettingsEntity(String variable) {
        this(OrganizationThemesSettingsEntity.class, forVariable(variable), INITS);
    }

    public QOrganizationThemesSettingsEntity(Path<? extends OrganizationThemesSettingsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganizationThemesSettingsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganizationThemesSettingsEntity(PathMetadata metadata, PathInits inits) {
        this(OrganizationThemesSettingsEntity.class, metadata, inits);
    }

    public QOrganizationThemesSettingsEntity(Class<? extends OrganizationThemesSettingsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
        this.theme = inits.isInitialized("theme") ? new QThemeEntity(forProperty("theme"), inits.get("theme")) : null;
    }

}

