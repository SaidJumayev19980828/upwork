package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFeatures is a Querydsl query type for QFeatures
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFeatures extends com.querydsl.sql.RelationalPathBase<QFeatures> {

    private static final long serialVersionUID = -1649443368;

    public static final QFeatures features = new QFeatures("features");

    public final BooleanPath beacon = createBoolean("beacon");

    public final BooleanPath campaign = createBoolean("campaign");

    public final BooleanPath cart = createBoolean("cart");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final BooleanPath panorama360 = createBoolean("panorama360");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QFeatures> featuresPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> rails69ca5ba774Fk = createForeignKey(organizationId, "id");

    public QFeatures(String variable) {
        super(QFeatures.class, forVariable(variable), "public", "features");
        addMetadata();
    }

    public QFeatures(String variable, String schema, String table) {
        super(QFeatures.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFeatures(String variable, String schema) {
        super(QFeatures.class, forVariable(variable), schema, "features");
        addMetadata();
    }

    public QFeatures(Path<? extends QFeatures> path) {
        super(path.getType(), path.getMetadata(), "public", "features");
        addMetadata();
    }

    public QFeatures(PathMetadata metadata) {
        super(QFeatures.class, metadata, "public", "features");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(beacon, ColumnMetadata.named("beacon").withIndex(4).ofType(Types.BIT).withSize(1));
        addMetadata(campaign, ColumnMetadata.named("campaign").withIndex(3).ofType(Types.BIT).withSize(1));
        addMetadata(cart, ColumnMetadata.named("cart").withIndex(2).ofType(Types.BIT).withSize(1));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(panorama360, ColumnMetadata.named("panorama360").withIndex(5).ofType(Types.BIT).withSize(1));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

