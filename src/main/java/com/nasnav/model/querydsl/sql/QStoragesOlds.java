package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QStoragesOlds is a Querydsl query type for QStoragesOlds
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QStoragesOlds extends com.querydsl.sql.RelationalPathBase<QStoragesOlds> {

    private static final long serialVersionUID = 152928447;

    public static final QStoragesOlds storagesOlds = new QStoragesOlds("storages_olds");

    public final StringPath barcode = createString("barcode");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final StringPath coverImage = createString("coverImage");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final StringPath ean = createString("ean");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath infoUpdated = createBoolean("infoUpdated");

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Double> price = createNumber("price", Double.class);

    public final SimplePath<String[]> tempColors = createSimple("tempColors", String[].class);

    public final StringPath tempImage = createString("tempImage");

    public final SimplePath<String[]> tempSizes = createSimple("tempSizes", String[].class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final SimplePath<String[]> vrSlider = createSimple("vrSlider", String[].class);

    public final com.querydsl.sql.PrimaryKey<QStoragesOlds> storagesOldsPkey = createPrimaryKey(id);

    public QStoragesOlds(String variable) {
        super(QStoragesOlds.class, forVariable(variable), "public", "storages_olds");
        addMetadata();
    }

    public QStoragesOlds(String variable, String schema, String table) {
        super(QStoragesOlds.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStoragesOlds(String variable, String schema) {
        super(QStoragesOlds.class, forVariable(variable), schema, "storages_olds");
        addMetadata();
    }

    public QStoragesOlds(Path<? extends QStoragesOlds> path) {
        super(path.getType(), path.getMetadata(), "public", "storages_olds");
        addMetadata();
    }

    public QStoragesOlds(PathMetadata metadata) {
        super(QStoragesOlds.class, metadata, "public", "storages_olds");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(barcode, ColumnMetadata.named("barcode").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(brandId, ColumnMetadata.named("brand_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(categoryId, ColumnMetadata.named("category_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(coverImage, ColumnMetadata.named("cover_image").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(10).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(description, ColumnMetadata.named("description").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(ean, ColumnMetadata.named("ean").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(infoUpdated, ColumnMetadata.named("info_updated").withIndex(14).ofType(Types.BIT).withSize(1));
        addMetadata(name, ColumnMetadata.named("name").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(price, ColumnMetadata.named("price").withIndex(6).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(tempColors, ColumnMetadata.named("temp_colors").withIndex(15).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(tempImage, ColumnMetadata.named("temp_image").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(tempSizes, ColumnMetadata.named("temp_sizes").withIndex(16).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(11).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(vrSlider, ColumnMetadata.named("vr_slider").withIndex(17).ofType(Types.ARRAY).withSize(2147483647));
    }

}

