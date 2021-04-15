package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QGoogleAnalytics is a Querydsl query type for QGoogleAnalytics
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QGoogleAnalytics extends com.querydsl.sql.RelationalPathBase<QGoogleAnalytics> {

    private static final long serialVersionUID = 564051794;

    public static final QGoogleAnalytics googleAnalytics = new QGoogleAnalytics("google_analytics");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> employeeUserId = createNumber("employeeUserId", Long.class);

    public final BooleanPath general = createBoolean("general");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final StringPath trackingId = createString("trackingId");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QGoogleAnalytics> googleAnalyticsPkey = createPrimaryKey(id);

    public QGoogleAnalytics(String variable) {
        super(QGoogleAnalytics.class, forVariable(variable), "public", "google_analytics");
        addMetadata();
    }

    public QGoogleAnalytics(String variable, String schema, String table) {
        super(QGoogleAnalytics.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QGoogleAnalytics(String variable, String schema) {
        super(QGoogleAnalytics.class, forVariable(variable), schema, "google_analytics");
        addMetadata();
    }

    public QGoogleAnalytics(Path<? extends QGoogleAnalytics> path) {
        super(path.getType(), path.getMetadata(), "public", "google_analytics");
        addMetadata();
    }

    public QGoogleAnalytics(PathMetadata metadata) {
        super(QGoogleAnalytics.class, metadata, "public", "google_analytics");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(employeeUserId, ColumnMetadata.named("employee_user_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(general, ColumnMetadata.named("general").withIndex(6).ofType(Types.BIT).withSize(1));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(trackingId, ColumnMetadata.named("tracking_id").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

