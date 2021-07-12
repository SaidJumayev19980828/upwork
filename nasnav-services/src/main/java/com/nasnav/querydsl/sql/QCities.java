package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QCities is a Querydsl query type for QCities
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QCities extends com.querydsl.sql.RelationalPathBase<QCities> {

    private static final long serialVersionUID = -1483560833;

    public static final QCities cities = new QCities("cities");

    public final NumberPath<Long> countryId = createNumber("countryId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QCities> citiesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QCountries> rails996e05be41Fk = createForeignKey(countryId, "id");

    public final com.querydsl.sql.ForeignKey<QAreas> _areasCityIdFkey = createInvForeignKey(id, "city_id");

    public QCities(String variable) {
        super(QCities.class, forVariable(variable), "public", "cities");
        addMetadata();
    }

    public QCities(String variable, String schema, String table) {
        super(QCities.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QCities(String variable, String schema) {
        super(QCities.class, forVariable(variable), schema, "cities");
        addMetadata();
    }

    public QCities(Path<? extends QCities> path) {
        super(path.getType(), path.getMetadata(), "public", "cities");
        addMetadata();
    }

    public QCities(PathMetadata metadata) {
        super(QCities.class, metadata, "public", "cities");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(countryId, ColumnMetadata.named("country_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

