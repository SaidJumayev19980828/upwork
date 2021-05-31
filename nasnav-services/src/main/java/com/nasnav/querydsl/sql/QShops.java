package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShops is a Querydsl query type for QShops
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShops extends com.querydsl.sql.RelationalPathBase<QShops> {

    private static final long serialVersionUID = 798169159;

    public static final QShops shops = new QShops("shops");

    public final NumberPath<Long> addressId = createNumber("addressId", Long.class);

    public final StringPath banner = createString("banner");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final BooleanPath enableLogo = createBoolean("enableLogo");

    public final StringPath googlePlaceId = createString("googlePlaceId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> isWarehouse = createNumber("isWarehouse", Integer.class);

    public final StringPath logo = createString("logo");

    public final StringPath darkLogo = createString("dark_logo");

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath pName = createString("pName");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final SimplePath<String[]> workDays = createSimple("workDays", String[].class);

    public final com.querydsl.sql.PrimaryKey<QShops> shopsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QAddresses> shopsAddressIdFkey = createForeignKey(addressId, "id");

    public final com.querydsl.sql.ForeignKey<QBrands> shopsBrandIdFkey = createForeignKey(brandId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> shopsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QEmployeeUsers> _employeeUsersShopIdFkey = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QShop360s> _rails888a1fc9beFk = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QStocks> _railsD8eb88b3bfFk = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QOrders> _ordersShopIdFkey = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QOrganizationImages> _organizationImagesShopIdFkey = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QShop360Products> _shop360ProductsShopIdFkey = createInvForeignKey(id, "shop_id");

    public final com.querydsl.sql.ForeignKey<QShopsOpeningTimes> _shopsOpeningTimesShopIdFkey = createInvForeignKey(id, "shop_id");

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
        addMetadata(addressId, ColumnMetadata.named("address_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(banner, ColumnMetadata.named("banner").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(brandId, ColumnMetadata.named("brand_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(enableLogo, ColumnMetadata.named("enable_logo").withIndex(9).ofType(Types.BIT).withSize(1));
        addMetadata(googlePlaceId, ColumnMetadata.named("google_place_id").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(isWarehouse, ColumnMetadata.named("is_warehouse").withIndex(15).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(logo, ColumnMetadata.named("dark_logo").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(11).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(priority, ColumnMetadata.named("priority").withIndex(16).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(removed, ColumnMetadata.named("removed").withIndex(13).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(workDays, ColumnMetadata.named("work_days").withIndex(7).ofType(Types.ARRAY).withSize(2147483647));
    }

}

