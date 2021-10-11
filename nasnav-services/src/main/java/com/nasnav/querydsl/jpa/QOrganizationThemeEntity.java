package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OrganizationThemeEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrganizationThemeEntity is a Querydsl query type for OrganizationThemeEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganizationThemeEntity extends EntityPathBase<OrganizationThemeEntity> {

    private static final long serialVersionUID = 1133549452L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrganizationThemeEntity organizationThemeEntity = new QOrganizationThemeEntity("organizationThemeEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath firstColor = createString("firstColor");

    public final BooleanPath firstSection = createBoolean("firstSection");

    public final StringPath firstSectionImage = createString("firstSectionImage");

    public final NumberPath<Integer> firstSectionProduct = createNumber("firstSectionProduct", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logo = createString("logo");

    public final QOrganizationEntity organizationEntity;

    public final StringPath secondColor = createString("secondColor");

    public final BooleanPath secondSection = createBoolean("secondSection");

    public final StringPath secondSectionImage = createString("secondSectionImage");

    public final NumberPath<Integer> secondSectionProduct = createNumber("secondSectionProduct", Integer.class);

    public final BooleanPath sliderBody = createBoolean("sliderBody");

    public final StringPath sliderHeader = createString("sliderHeader");

    public final ArrayPath<String[], String> sliderImages = createArray("sliderImages", String[].class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QOrganizationThemeEntity(String variable) {
        this(OrganizationThemeEntity.class, forVariable(variable), INITS);
    }

    public QOrganizationThemeEntity(Path<? extends OrganizationThemeEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrganizationThemeEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrganizationThemeEntity(PathMetadata metadata, PathInits inits) {
        this(OrganizationThemeEntity.class, metadata, inits);
    }

    public QOrganizationThemeEntity(Class<? extends OrganizationThemeEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
    }

}

