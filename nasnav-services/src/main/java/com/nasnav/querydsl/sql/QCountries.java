package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QCountries is a Querydsl query type for QCountries
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QCountries extends com.querydsl.sql.RelationalPathBase<QCountries> {

    private static final long serialVersionUID = -2114964359;

    public static final QCountries countries = new QCountries("countries");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QCountries> countriesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QCities> _rails996e05be41Fk = createInvForeignKey(id, "country_id");

    public QCountries(String variable) {
        super(QCountries.class, forVariable(variable), "public", "countries");
        addMetadata();
    }

    public QCountries(String variable, String schema, String table) {
        super(QCountries.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QCountries(String variable, String schema) {
        super(QCountries.class, forVariable(variable), schema, "countries");
        addMetadata();
    }

    public QCountries(Path<? extends QCountries> path) {
        super(path.getType(), path.getMetadata(), "public", "countries");
        addMetadata();
    }

    public QCountries(PathMetadata metadata) {
        super(QCountries.class, metadata, "public", "countries");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

