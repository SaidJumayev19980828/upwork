package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QBuildings is a Querydsl query type for QBuildings
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QBuildings extends com.querydsl.sql.RelationalPathBase<QBuildings> {

    private static final long serialVersionUID = -572989948;

    public static final QBuildings buildings = new QBuildings("buildings");

    public final StringPath address = createString("address");

    public final StringPath area = createString("area");

    public final StringPath banner = createString("banner");

    public final StringPath buildingType = createString("buildingType");

    public final TimePath<java.sql.Time> closeAt = createTime("closeAt", java.sql.Time.class);

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final NumberPath<Integer> floorsCount = createNumber("floorsCount", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<java.math.BigDecimal> latitude = createNumber("latitude", java.math.BigDecimal.class);

    public final StringPath logo = createString("logo");

    public final NumberPath<java.math.BigDecimal> longitude = createNumber("longitude", java.math.BigDecimal.class);

    public final StringPath name = createString("name");

    public final TimePath<java.sql.Time> openAt = createTime("openAt", java.sql.Time.class);

    public final NumberPath<Integer> organizationId = createNumber("organizationId", Integer.class);

    public final StringPath pName = createString("pName");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QBuildings> buildingsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShops> _shopsBuildingIdFkey = createInvForeignKey(id, "building_id");

    public QBuildings(String variable) {
        super(QBuildings.class, forVariable(variable), "public", "buildings");
        addMetadata();
    }

    public QBuildings(String variable, String schema, String table) {
        super(QBuildings.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QBuildings(String variable, String schema) {
        super(QBuildings.class, forVariable(variable), schema, "buildings");
        addMetadata();
    }

    public QBuildings(Path<? extends QBuildings> path) {
        super(path.getType(), path.getMetadata(), "public", "buildings");
        addMetadata();
    }

    public QBuildings(PathMetadata metadata) {
        super(QBuildings.class, metadata, "public", "buildings");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(address, ColumnMetadata.named("address").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(area, ColumnMetadata.named("area").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(banner, ColumnMetadata.named("banner").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(buildingType, ColumnMetadata.named("building_type").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(closeAt, ColumnMetadata.named("close_at").withIndex(11).ofType(Types.TIME).withSize(15).withDigits(6));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(description, ColumnMetadata.named("description").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(floorsCount, ColumnMetadata.named("floors_count").withIndex(16).ofType(Types.INTEGER).withSize(10));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(5).ofType(Types.NUMERIC).withSize(10).withDigits(6));
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(4).ofType(Types.NUMERIC).withSize(10).withDigits(6));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(openAt, ColumnMetadata.named("open_at").withIndex(10).ofType(Types.TIME).withSize(15).withDigits(6));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(13).ofType(Types.INTEGER).withSize(10));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

