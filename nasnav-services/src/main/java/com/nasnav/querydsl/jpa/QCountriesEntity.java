package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.CountriesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCountriesEntity is a Querydsl query type for CountriesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCountriesEntity extends EntityPathBase<CountriesEntity> {

    private static final long serialVersionUID = 1943008874L;

    public static final QCountriesEntity countriesEntity = new QCountriesEntity("countriesEntity");

    public final SetPath<CitiesEntity, QCitiesEntity> cities = this.<CitiesEntity, QCitiesEntity>createSet("cities", CitiesEntity.class, QCitiesEntity.class, PathInits.DIRECT2);

    public final StringPath currency = createString("currency");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> isoCode = createNumber("isoCode", Integer.class);

    public final StringPath name = createString("name");

    public QCountriesEntity(String variable) {
        super(CountriesEntity.class, forVariable(variable));
    }

    public QCountriesEntity(Path<? extends CountriesEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCountriesEntity(PathMetadata metadata) {
        super(CountriesEntity.class, metadata);
    }

}

