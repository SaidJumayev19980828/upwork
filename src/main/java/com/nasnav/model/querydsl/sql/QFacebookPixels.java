package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFacebookPixels is a Querydsl query type for QFacebookPixels
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFacebookPixels extends com.querydsl.sql.RelationalPathBase<QFacebookPixels> {

    private static final long serialVersionUID = 527241838;

    public static final QFacebookPixels facebookPixels = new QFacebookPixels("facebook_pixels");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final BooleanPath general = createBoolean("general");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> organizationManagerId = createNumber("organizationManagerId", Long.class);

    public final StringPath pixelId = createString("pixelId");

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QFacebookPixels> facebookPixelsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizationManagers> rails84c2b2df8aFk = createForeignKey(organizationManagerId, "id");

    public QFacebookPixels(String variable) {
        super(QFacebookPixels.class, forVariable(variable), "public", "facebook_pixels");
        addMetadata();
    }

    public QFacebookPixels(String variable, String schema, String table) {
        super(QFacebookPixels.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFacebookPixels(String variable, String schema) {
        super(QFacebookPixels.class, forVariable(variable), schema, "facebook_pixels");
        addMetadata();
    }

    public QFacebookPixels(Path<? extends QFacebookPixels> path) {
        super(path.getType(), path.getMetadata(), "public", "facebook_pixels");
        addMetadata();
    }

    public QFacebookPixels(PathMetadata metadata) {
        super(QFacebookPixels.class, metadata, "public", "facebook_pixels");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(general, ColumnMetadata.named("general").withIndex(4).ofType(Types.BIT).withSize(1));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(7).ofType(Types.BIGINT).withSize(19));
        addMetadata(organizationManagerId, ColumnMetadata.named("organization_manager_id").withIndex(8).ofType(Types.BIGINT).withSize(19));
        addMetadata(pixelId, ColumnMetadata.named("pixel_id").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

