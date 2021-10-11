package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.SubAreasEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubAreasEntity is a Querydsl query type for SubAreasEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSubAreasEntity extends EntityPathBase<SubAreasEntity> {

    private static final long serialVersionUID = 908317078L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubAreasEntity subAreasEntity = new QSubAreasEntity("subAreasEntity");

    public final QAreasEntity area;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final StringPath name = createString("name");

    public final QOrganizationEntity organization;

    public QSubAreasEntity(String variable) {
        this(SubAreasEntity.class, forVariable(variable), INITS);
    }

    public QSubAreasEntity(Path<? extends SubAreasEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubAreasEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubAreasEntity(PathMetadata metadata, PathInits inits) {
        this(SubAreasEntity.class, metadata, inits);
    }

    public QSubAreasEntity(Class<? extends SubAreasEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.area = inits.isInitialized("area") ? new QAreasEntity(forProperty("area"), inits.get("area")) : null;
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
    }

}

