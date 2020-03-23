package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationImages is a Querydsl query type for QOrganizationImages
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationImages extends com.querydsl.sql.RelationalPathBase<QOrganizationImages> {

    private static final long serialVersionUID = -561250970;

    public static final QOrganizationImages organizationImages = new QOrganizationImages("organization_images");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final StringPath uri = createString("uri");

    public final com.querydsl.sql.PrimaryKey<QOrganizationImages> organizationImagesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFiles> organizationImagesUriFkey = createForeignKey(uri, "url");

    public final com.querydsl.sql.ForeignKey<QOrganizationImageTypes> organizationImagesTypeFk = createForeignKey(type, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> organizationImagesOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QShops> organizationImagesShopIdFkey = createForeignKey(shopId, "id");

    public QOrganizationImages(String variable) {
        super(QOrganizationImages.class, forVariable(variable), "public", "organization_images");
        addMetadata();
    }

    public QOrganizationImages(String variable, String schema, String table) {
        super(QOrganizationImages.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationImages(String variable, String schema) {
        super(QOrganizationImages.class, forVariable(variable), schema, "organization_images");
        addMetadata();
    }

    public QOrganizationImages(Path<? extends QOrganizationImages> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_images");
        addMetadata();
    }

    public QOrganizationImages(PathMetadata metadata) {
        super(QOrganizationImages.class, metadata, "public", "organization_images");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(type, ColumnMetadata.named("type").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(uri, ColumnMetadata.named("uri").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

