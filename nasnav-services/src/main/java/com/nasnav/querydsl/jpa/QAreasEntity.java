package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.AreasEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAreasEntity is a Querydsl query type for AreasEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAreasEntity extends EntityPathBase<AreasEntity> {

    private static final long serialVersionUID = 615516476L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAreasEntity areasEntity = new QAreasEntity("areasEntity");

    public final QCitiesEntity citiesEntity;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public QAreasEntity(String variable) {
        this(AreasEntity.class, forVariable(variable), INITS);
    }

    public QAreasEntity(Path<? extends AreasEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAreasEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAreasEntity(PathMetadata metadata, PathInits inits) {
        this(AreasEntity.class, metadata, inits);
    }

    public QAreasEntity(Class<? extends AreasEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.citiesEntity = inits.isInitialized("citiesEntity") ? new QCitiesEntity(forProperty("citiesEntity"), inits.get("citiesEntity")) : null;
    }

}

