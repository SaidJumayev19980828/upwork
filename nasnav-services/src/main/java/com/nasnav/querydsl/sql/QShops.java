package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QShops is a Querydsl query type for QShops
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShops extends com.querydsl.sql.RelationalPathBase<QShops> {

    private static final long serialVersionUID = 147572866;

    public static final QShops shops = new QShops("shops");

    public final StringPath address = createString("address");

    public final StringPath area = createString("area");

    public final StringPath banner = createString("banner");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final NumberPath<Integer> buildingId = createNumber("buildingId", Integer.class);

    public final StringPath city = createString("city");

    public final StringPath country = createString("country");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final BooleanPath enableLogo = createBoolean("enableLogo");

    public final StringPath floor = createString("floor");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> lat = createNumber("lat", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> lng = createNumber("lng", java.math.BigDecimal.class);

    public final StringPath logo = createString("logo");

    public final NumberPath<Long> mallId = createNumber("mallId", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath pArea = createString("pArea");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath pName = createString("pName");

    public final StringPath pStreet = createString("pStreet");

    public final NumberPath<Integer> remoteId = createNumber("remoteId", Integer.class);

    public final StringPath street = createString("street");

    public final StringPath streetNumber = createString("streetNumber");

    public final DateTimePath<java.sql.Timestamp> timeFrom = createDateTime("timeFrom", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> timeTo = createDateTime("timeTo", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final StringPath viewImage = createString("viewImage");

    public final SimplePath<String[]> workDays = createSimple("workDays", String[].class);

    public final StringPath workTimes = createString("workTimes");

    public final StringPath zip = createString("zip");

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QShops> shopsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QBrands> shopsBrandIdFkey = createForeignKey(brandId, "id");

    public final com.querydsl.sql.ForeignKey<QMalls> railsFb68d12dc0Fk = createForeignKey(mallId, "id");

    public final com.querydsl.sql.ForeignKey<QBuildings> shopsBuildingIdFkey = createForeignKey(buildingId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> shopsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizationImages> _organizationImagesShopIdFkey = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QStocks> _railsD8eb88b3bfFk = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QShopsOpeningTimes> _shopsOpeningTimesShopIdFkey = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QShop360s> _rails888a1fc9beFk = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QWorkTimes> _railsC68a0170e4Fk = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QSubscribedUsers> _railsF0bd17ab86Fk = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QOrders> _ordersShopIdFkey = createInvForeignKey(id, "shop_id");

    public QShops(String variable) {
        super(QShops.class, forVariable(variable), "public", "shops");
        addMetadata();
    }

    public QShops(String variable, String schema, String table) {
        super(QShops.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShops(String variable, String schema) {
        super(QShops.class, forVariable(variable), schema, "shops");
        addMetadata();
    }

    public QShops(Path<? extends QShops> path) {
        super(path.getType(), path.getMetadata(), "public", "shops");
        addMetadata();
    }

    public QShops(PathMetadata metadata) {
        super(QShops.class, metadata, "public", "shops");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(address, ColumnMetadata.named("address").withIndex(26).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(area, ColumnMetadata.named("area").withIndex(29).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(banner, ColumnMetadata.named("banner").withIndex(27).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(brandId, ColumnMetadata.named("brand_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
        addMetadata(buildingId, ColumnMetadata.named("building_id").withIndex(16).ofType(Types.INTEGER).withSize(10));
        addMetadata(city, ColumnMetadata.named("city").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(country, ColumnMetadata.named("country").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(13).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(enableLogo, ColumnMetadata.named("enable_logo").withIndex(25).ofType(Types.BIT).withSize(1));
        addMetadata(floor, ColumnMetadata.named("floor").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lat, ColumnMetadata.named("lat").withIndex(11).ofType(Types.NUMERIC).withSize(10).withDigits(6));
        addMetadata(lng, ColumnMetadata.named("lng").withIndex(12).ofType(Types.NUMERIC).withSize(10).withDigits(6));
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(24).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(mallId, ColumnMetadata.named("mall_id").withIndex(31).ofType(Types.BIGINT).withSize(19));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(17).ofType(Types.BIGINT).withSize(19));
        addMetadata(pArea, ColumnMetadata.named("p_area").withIndex(30).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(28).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pStreet, ColumnMetadata.named("p_street").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(remoteId, ColumnMetadata.named("remote_id").withIndex(15).ofType(Types.INTEGER).withSize(10));
        addMetadata(street, ColumnMetadata.named("street").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(streetNumber, ColumnMetadata.named("street_number").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(timeFrom, ColumnMetadata.named("time_from").withIndex(21).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(timeTo, ColumnMetadata.named("time_to").withIndex(22).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(14).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(viewImage, ColumnMetadata.named("view_image").withIndex(19).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(workDays, ColumnMetadata.named("work_days").withIndex(23).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(workTimes, ColumnMetadata.named("work_times").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(zip, ColumnMetadata.named("zip").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(removed, ColumnMetadata.named("removed").withIndex(32).ofType(Types.INTEGER).withSize(10));
    }

}

