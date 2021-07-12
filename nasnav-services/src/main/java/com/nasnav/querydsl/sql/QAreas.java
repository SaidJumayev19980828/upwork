package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAreas is a Querydsl query type for QAreas
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAreas extends com.querydsl.sql.RelationalPathBase<QAreas> {

    private static final long serialVersionUID = 781833616;

    public static final QAreas areas = new QAreas("areas");

    public final NumberPath<Long> cityId = createNumber("cityId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QAreas> areasPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QCities> areasCityIdFkey = createForeignKey(cityId, "id");

    public final com.querydsl.sql.ForeignKey<QAddresses> _addressesAreaIdFkey = createInvForeignKey(id, "area_id");

    public final com.querydsl.sql.ForeignKey<QShippingAreas> _shippingAreasAreaIdFkey = createInvForeignKey(id, "area_id");

    public final com.querydsl.sql.ForeignKey<QSubAreas> _subAreasAreaIdFkey = createInvForeignKey(id, "area_id");

    public QAreas(String variable) {
        super(QAreas.class, forVariable(variable), "public", "areas");
        addMetadata();
    }

    public QAreas(String variable, String schema, String table) {
        super(QAreas.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAreas(String variable, String schema) {
        super(QAreas.class, forVariable(variable), schema, "areas");
        addMetadata();
    }

    public QAreas(Path<? extends QAreas> path) {
        super(path.getType(), path.getMetadata(), "public", "areas");
        addMetadata();
    }

    public QAreas(PathMetadata metadata) {
        super(QAreas.class, metadata, "public", "areas");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(cityId, ColumnMetadata.named("city_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

