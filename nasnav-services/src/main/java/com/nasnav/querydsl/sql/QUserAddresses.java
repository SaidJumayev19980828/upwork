package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QUserAddresses is a Querydsl query type for QUserAddresses
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserAddresses extends com.querydsl.sql.RelationalPathBase<QUserAddresses> {

    private static final long serialVersionUID = -803819999;

    public static final QUserAddresses userAddresses = new QUserAddresses("user_addresses");

    public final NumberPath<Long> addressId = createNumber("addressId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath principal = createBoolean("principal");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QUserAddresses> userAddressesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QAddresses> userAddressesAddressIdFkey = createForeignKey(addressId, "id");

    public final com.querydsl.sql.ForeignKey<QUsers> userAddressesUserIdFkey = createForeignKey(userId, "id");

    public QUserAddresses(String variable) {
        super(QUserAddresses.class, forVariable(variable), "public", "user_addresses");
        addMetadata();
    }

    public QUserAddresses(String variable, String schema, String table) {
        super(QUserAddresses.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUserAddresses(String variable, String schema) {
        super(QUserAddresses.class, forVariable(variable), schema, "user_addresses");
        addMetadata();
    }

    public QUserAddresses(Path<? extends QUserAddresses> path) {
        super(path.getType(), path.getMetadata(), "public", "user_addresses");
        addMetadata();
    }

    public QUserAddresses(PathMetadata metadata) {
        super(QUserAddresses.class, metadata, "public", "user_addresses");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(addressId, ColumnMetadata.named("address_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(principal, ColumnMetadata.named("principal").withIndex(4).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

