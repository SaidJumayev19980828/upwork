package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShopsOpeningTimes is a Querydsl query type for QShopsOpeningTimes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShopsOpeningTimes extends com.querydsl.sql.RelationalPathBase<QShopsOpeningTimes> {

    private static final long serialVersionUID = 2133103285;

    public static final QShopsOpeningTimes shopsOpeningTimes = new QShopsOpeningTimes("shops_opening_times");

    public final TimePath<java.sql.Time> closes = createTime("closes", java.sql.Time.class);

    public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final TimePath<java.sql.Time> opens = createTime("opens", java.sql.Time.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final DatePath<java.sql.Date> validFrom = createDate("validFrom", java.sql.Date.class);

    public final DatePath<java.sql.Date> validThrough = createDate("validThrough", java.sql.Date.class);

    public final com.querydsl.sql.PrimaryKey<QShopsOpeningTimes> shopsOpeningTimesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShops> shopsOpeningTimesShopIdFkey = createForeignKey(shopId, "id");

    public QShopsOpeningTimes(String variable) {
        super(QShopsOpeningTimes.class, forVariable(variable), "public", "shops_opening_times");
        addMetadata();
    }

    public QShopsOpeningTimes(String variable, String schema, String table) {
        super(QShopsOpeningTimes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShopsOpeningTimes(String variable, String schema) {
        super(QShopsOpeningTimes.class, forVariable(variable), schema, "shops_opening_times");
        addMetadata();
    }

    public QShopsOpeningTimes(Path<? extends QShopsOpeningTimes> path) {
        super(path.getType(), path.getMetadata(), "public", "shops_opening_times");
        addMetadata();
    }

    public QShopsOpeningTimes(PathMetadata metadata) {
        super(QShopsOpeningTimes.class, metadata, "public", "shops_opening_times");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(closes, ColumnMetadata.named("closes").withIndex(4).ofType(Types.TIME).withSize(15).withDigits(6).notNull());
        addMetadata(dayOfWeek, ColumnMetadata.named("day_of_week").withIndex(2).ofType(Types.INTEGER).withSize(10));
        addMetadata(id, ColumnMetadata.named("id").withIndex(7).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(opens, ColumnMetadata.named("opens").withIndex(3).ofType(Types.TIME).withSize(15).withDigits(6).notNull());
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(validFrom, ColumnMetadata.named("valid_from").withIndex(5).ofType(Types.DATE).withSize(13));
        addMetadata(validThrough, ColumnMetadata.named("valid_through").withIndex(6).ofType(Types.DATE).withSize(13));
    }

}

