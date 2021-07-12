package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QCountries is a Querydsl query type for QCountries
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QCountries extends com.querydsl.sql.RelationalPathBase<QCountries> {

    private static final long serialVersionUID = 1364203966;

    public static final QCountries countries = new QCountries("countries");

    public final StringPath currency = createString("currency");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> isoCode = createNumber("isoCode", Integer.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QCountries> countriesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QCities> _rails996e05be41Fk = createInvForeignKey(id, "country_id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> _organizationsCurrencyIsoFkey = createInvForeignKey(isoCode, "currency_iso");

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
        addMetadata(currency, ColumnMetadata.named("currency").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(isoCode, ColumnMetadata.named("iso_code").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

