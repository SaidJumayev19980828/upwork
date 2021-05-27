package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAddresses is a Querydsl query type for QAddresses
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAddresses extends com.querydsl.sql.RelationalPathBase<QAddresses> {

    private static final long serialVersionUID = 886110892;

    public static final QAddresses addresses = new QAddresses("addresses");

    public final StringPath addressLine1 = createString("addressLine1");

    public final StringPath addressLine2 = createString("addressLine2");

    public final NumberPath<Long> areaId = createNumber("areaId", Long.class);

    public final StringPath buildingNumber = createString("buildingNumber");

    public final StringPath firstName = createString("firstName");

    public final StringPath flatNumber = createString("flatNumber");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public final NumberPath<java.math.BigInteger> latitude = createNumber("latitude", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> longitude = createNumber("longitude", java.math.BigInteger.class);

    public final StringPath name = createString("name");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath postalCode = createString("postalCode");

    public final NumberPath<Long> subAreaId = createNumber("subAreaId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QAddresses> addressesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QAreas> addressesAreaIdFkey = createForeignKey(areaId, "id");

    public final com.querydsl.sql.ForeignKey<QSubAreas> addressesSubAreaIdFkey = createForeignKey(subAreaId, "id");

    public final com.querydsl.sql.ForeignKey<QOrders> _ordersAddressIdFkey = createInvForeignKey(id, "address_id");

    public final com.querydsl.sql.ForeignKey<QShops> _shopsAddressIdFkey = createInvForeignKey(id, "address_id");

    public final com.querydsl.sql.ForeignKey<QUserAddresses> _userAddressesAddressIdFkey = createInvForeignKey(id, "address_id");

    public QAddresses(String variable) {
        super(QAddresses.class, forVariable(variable), "public", "addresses");
        addMetadata();
    }

    public QAddresses(String variable, String schema, String table) {
        super(QAddresses.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAddresses(String variable, String schema) {
        super(QAddresses.class, forVariable(variable), schema, "addresses");
        addMetadata();
    }

    public QAddresses(Path<? extends QAddresses> path) {
        super(path.getType(), path.getMetadata(), "public", "addresses");
        addMetadata();
    }

    public QAddresses(PathMetadata metadata) {
        super(QAddresses.class, metadata, "public", "addresses");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(addressLine1, ColumnMetadata.named("address_line_1").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(addressLine2, ColumnMetadata.named("address_line_2").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(areaId, ColumnMetadata.named("area_id").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(buildingNumber, ColumnMetadata.named("building_number").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(firstName, ColumnMetadata.named("first_name").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(flatNumber, ColumnMetadata.named("flat_number").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(lastName, ColumnMetadata.named("last_name").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(6).ofType(Types.NUMERIC).withSize(131089));
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(7).ofType(Types.NUMERIC).withSize(131089));
        addMetadata(name, ColumnMetadata.named("name").withIndex(11).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(postalCode, ColumnMetadata.named("postal_code").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(subAreaId, ColumnMetadata.named("sub_area_id").withIndex(14).ofType(Types.BIGINT).withSize(19));
    }

}

