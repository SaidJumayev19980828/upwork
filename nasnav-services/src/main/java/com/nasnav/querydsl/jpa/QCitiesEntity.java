package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.CitiesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCitiesEntity is a Querydsl query type for CitiesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCitiesEntity extends EntityPathBase<CitiesEntity> {

    private static final long serialVersionUID = -1008532807L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCitiesEntity citiesEntity = new QCitiesEntity("citiesEntity");

    public final SetPath<AreasEntity, QAreasEntity> areas = this.<AreasEntity, QAreasEntity>createSet("areas", AreasEntity.class, QAreasEntity.class, PathInits.DIRECT2);

    public final QCountriesEntity countriesEntity;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public QCitiesEntity(String variable) {
        this(CitiesEntity.class, forVariable(variable), INITS);
    }

    public QCitiesEntity(Path<? extends CitiesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCitiesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCitiesEntity(PathMetadata metadata, PathInits inits) {
        this(CitiesEntity.class, metadata, inits);
    }

    public QCitiesEntity(Class<? extends CitiesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.countriesEntity = inits.isInitialized("countriesEntity") ? new QCountriesEntity(forProperty("countriesEntity")) : null;
    }

}

