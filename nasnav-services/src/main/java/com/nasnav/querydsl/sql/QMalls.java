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
 * QMalls is a Querydsl query type for QMalls
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMalls extends com.querydsl.sql.RelationalPathBase<QMalls> {

    private static final long serialVersionUID = 141820196;

    public static final QMalls malls = new QMalls("malls");

    public final StringPath address = createString("address");

    public final StringPath area = createString("area");

    public final NumberPath<Long> cityId = createNumber("cityId", Long.class);

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> lat = createNumber("lat", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> lng = createNumber("lng", java.math.BigDecimal.class);

    public final StringPath name = createString("name");

    public final StringPath pArea = createString("pArea");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QMalls> mallsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QCities> railsD3ca29e09aFk = createForeignKey(cityId, "id");

    public final com.querydsl.sql.ForeignKey<QShops> _railsFb68d12dc0Fk = createInvForeignKey(id, "mall_id");

    public QMalls(String variable) {
        super(QMalls.class, forVariable(variable), "public", "malls");
        addMetadata();
    }

    public QMalls(String variable, String schema, String table) {
        super(QMalls.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMalls(String variable, String schema) {
        super(QMalls.class, forVariable(variable), schema, "malls");
        addMetadata();
    }

    public QMalls(Path<? extends QMalls> path) {
        super(path.getType(), path.getMetadata(), "public", "malls");
        addMetadata();
    }

    public QMalls(PathMetadata metadata) {
        super(QMalls.class, metadata, "public", "malls");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(address, ColumnMetadata.named("address").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(area, ColumnMetadata.named("area").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(cityId, ColumnMetadata.named("city_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lat, ColumnMetadata.named("lat").withIndex(9).ofType(Types.NUMERIC).withSize(10).withDigits(6));
        addMetadata(lng, ColumnMetadata.named("lng").withIndex(10).ofType(Types.NUMERIC).withSize(10).withDigits(6));
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pArea, ColumnMetadata.named("p_area").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

