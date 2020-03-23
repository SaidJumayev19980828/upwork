package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPowers is a Querydsl query type for QPowers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPowers extends com.querydsl.sql.RelationalPathBase<QPowers> {

    private static final long serialVersionUID = 200596585;

    public static final QPowers powers = new QPowers("powers");

    public final BooleanPath branchAnalytics = createBoolean("branchAnalytics");

    public final BooleanPath branchStorage = createBoolean("branchStorage");

    public final BooleanPath clientsProfile = createBoolean("clientsProfile");

    public final BooleanPath createCampaign = createBoolean("createCampaign");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> employeeUserId = createNumber("employeeUserId", Long.class);

    public final BooleanPath homeDelivery = createBoolean("homeDelivery");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath moneyRedeem = createBoolean("moneyRedeem");

    public final BooleanPath navboxScan = createBoolean("navboxScan");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final BooleanPath productRedeem = createBoolean("productRedeem");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QPowers> powersPkey = createPrimaryKey(id);

    public QPowers(String variable) {
        super(QPowers.class, forVariable(variable), "public", "powers");
        addMetadata();
    }

    public QPowers(String variable, String schema, String table) {
        super(QPowers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPowers(String variable, String schema) {
        super(QPowers.class, forVariable(variable), schema, "powers");
        addMetadata();
    }

    public QPowers(Path<? extends QPowers> path) {
        super(path.getType(), path.getMetadata(), "public", "powers");
        addMetadata();
    }

    public QPowers(PathMetadata metadata) {
        super(QPowers.class, metadata, "public", "powers");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(branchAnalytics, ColumnMetadata.named("branch_analytics").withIndex(6).ofType(Types.BIT).withSize(1));
        addMetadata(branchStorage, ColumnMetadata.named("branch_storage").withIndex(8).ofType(Types.BIT).withSize(1));
        addMetadata(clientsProfile, ColumnMetadata.named("clients_profile").withIndex(5).ofType(Types.BIT).withSize(1));
        addMetadata(createCampaign, ColumnMetadata.named("create_campaign").withIndex(7).ofType(Types.BIT).withSize(1));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(12).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(employeeUserId, ColumnMetadata.named("employee_user_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(homeDelivery, ColumnMetadata.named("home_delivery").withIndex(9).ofType(Types.BIT).withSize(1));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(moneyRedeem, ColumnMetadata.named("money_redeem").withIndex(2).ofType(Types.BIT).withSize(1));
        addMetadata(navboxScan, ColumnMetadata.named("navbox_scan").withIndex(4).ofType(Types.BIT).withSize(1));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(10).ofType(Types.BIGINT).withSize(19));
        addMetadata(productRedeem, ColumnMetadata.named("product_redeem").withIndex(3).ofType(Types.BIT).withSize(1));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(13).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

