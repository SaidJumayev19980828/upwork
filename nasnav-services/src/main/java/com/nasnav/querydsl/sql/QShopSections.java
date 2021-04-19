package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QShopSections is a Querydsl query type for QShopSections
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShopSections extends com.querydsl.sql.RelationalPathBase<QShopSections> {

    private static final long serialVersionUID = -898655393;

    public static final QShopSections shopSections = new QShopSections("shop_sections");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath mobileJsonData = createString("mobileJsonData");

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shopFloorId = createNumber("shopFloorId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final StringPath webJsonData = createString("webJsonData");

    public final com.querydsl.sql.PrimaryKey<QShopSections> shopSectionsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> rails102545b523Fk = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QShopFloors> railsF2b72e42c7Fk = createForeignKey(shopFloorId, "id");

    public final com.querydsl.sql.ForeignKey<QScenes> _railsA66b01e057Fk = createInvForeignKey(id, "shop_section_id");

    public QShopSections(String variable) {
        super(QShopSections.class, forVariable(variable), "public", "shop_sections");
        addMetadata();
    }

    public QShopSections(String variable, String schema, String table) {
        super(QShopSections.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShopSections(String variable, String schema) {
        super(QShopSections.class, forVariable(variable), schema, "shop_sections");
        addMetadata();
    }

    public QShopSections(Path<? extends QShopSections> path) {
        super(path.getType(), path.getMetadata(), "public", "shop_sections");
        addMetadata();
    }

    public QShopSections(PathMetadata metadata) {
        super(QShopSections.class, metadata, "public", "shop_sections");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(image, ColumnMetadata.named("image").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(mobileJsonData, ColumnMetadata.named("mobile_json_data").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(shopFloorId, ColumnMetadata.named("shop_floor_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(webJsonData, ColumnMetadata.named("web_json_data").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

