package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.SettingEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettingEntity is a Querydsl query type for SettingEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSettingEntity extends EntityPathBase<SettingEntity> {

    private static final long serialVersionUID = 185095622L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettingEntity settingEntity = new QSettingEntity("settingEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrganizationEntity organization;

    public final StringPath settingName = createString("settingName");

    public final StringPath settingValue = createString("settingValue");

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public QSettingEntity(String variable) {
        this(SettingEntity.class, forVariable(variable), INITS);
    }

    public QSettingEntity(Path<? extends SettingEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettingEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettingEntity(PathMetadata metadata, PathInits inits) {
        this(SettingEntity.class, metadata, inits);
    }

    public QSettingEntity(Class<? extends SettingEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
    }

}

