package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ThemeClassEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganizationEntity is a Querydsl query type for OrganizationEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganizationEntity extends EntityPathBase<OrganizationEntity> {

    private static final long serialVersionUID = -1265203357L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganizationEntity organizationEntity = new QOrganizationEntity("organizationEntity");

    public final QCountriesEntity country;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> ecommerce = createNumber("ecommerce", Integer.class);

    public final StringPath extraInfo = createString("extraInfo");

    public final StringPath googleToken = createString("googleToken");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> matomoId = createNumber("matomoId", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath pname = createString("pname");

    public final SetPath<ThemeClassEntity, QThemeClassEntity> themeClasses = this.<ThemeClassEntity, QThemeClassEntity>createSet("themeClasses", ThemeClassEntity.class, QThemeClassEntity.class, PathInits.DIRECT2);

    public final NumberPath<Integer> themeId = createNumber("themeId", Integer.class);

    public final StringPath type = createString("type");

    public final NumberPath<Integer> yeshteryState = createNumber("yeshteryState", Integer.class);

    public QOrganizationEntity(String variable) {
        this(OrganizationEntity.class, forVariable(variable), INITS);
    }

    public QOrganizationEntity(Path<? extends OrganizationEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganizationEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganizationEntity(PathMetadata metadata, PathInits inits) {
        this(OrganizationEntity.class, metadata, inits);
    }

    public QOrganizationEntity(Class<? extends OrganizationEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.country = inits.isInitialized("country") ? new QCountriesEntity(forProperty("country")) : null;
    }

}

