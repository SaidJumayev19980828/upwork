package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QProductsOlds is a Querydsl query type for QProductsOlds
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductsOlds extends com.querydsl.sql.RelationalPathBase<QProductsOlds> {

    private static final long serialVersionUID = 270032587;

    public static final QProductsOlds productsOlds = new QProductsOlds("products_olds");

    public final StringPath barcode = createString("barcode");

    public final NumberPath<Integer> brandId = createNumber("brandId", Integer.class);

    public final NumberPath<Integer> categoryId = createNumber("categoryId", Integer.class);

    public final StringPath coverImage = createString("coverImage");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath description = createString("description");

    public final NumberPath<Double> discount = createNumber("discount", Double.class);

    public final StringPath ean = createString("ean");

    public final StringPath fittingRoom = createString("fittingRoom");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> number = createNumber("number", Integer.class);

    public final NumberPath<Integer> organizationId = createNumber("organizationId", Integer.class);

    public final StringPath pName = createString("pName");

    public final NumberPath<Double> price = createNumber("price", Double.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final SimplePath<String[]> tempColors = createSimple("tempColors", String[].class);

    public final StringPath tempImage = createString("tempImage");

    public final SimplePath<String[]> tempSizes = createSimple("tempSizes", String[].class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final BooleanPath updatedOnline = createBoolean("updatedOnline");

    public final StringPath viewImage = createString("viewImage");

    public final SimplePath<String[]> vrSlider = createSimple("vrSlider", String[].class);

    public final com.querydsl.sql.PrimaryKey<QProductsOlds> productsOldsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QBrands> railsF3b4d49caaFk = createForeignKey(brandId, "id");

    public final com.querydsl.sql.ForeignKey<QCategories> railsFb915499a4Fk = createForeignKey(categoryId, "id");

    public QProductsOlds(String variable) {
        super(QProductsOlds.class, forVariable(variable), "public", "products_olds");
        addMetadata();
    }

    public QProductsOlds(String variable, String schema, String table) {
        super(QProductsOlds.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductsOlds(String variable, String schema) {
        super(QProductsOlds.class, forVariable(variable), schema, "products_olds");
        addMetadata();
    }

    public QProductsOlds(Path<? extends QProductsOlds> path) {
        super(path.getType(), path.getMetadata(), "public", "products_olds");
        addMetadata();
    }

    public QProductsOlds(PathMetadata metadata) {
        super(QProductsOlds.class, metadata, "public", "products_olds");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(barcode, ColumnMetadata.named("barcode").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(brandId, ColumnMetadata.named("brand_id").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(categoryId, ColumnMetadata.named("category_id").withIndex(2).ofType(Types.INTEGER).withSize(10));
        addMetadata(coverImage, ColumnMetadata.named("cover_image").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(description, ColumnMetadata.named("description").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(discount, ColumnMetadata.named("discount").withIndex(5).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(ean, ColumnMetadata.named("ean").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(fittingRoom, ColumnMetadata.named("fitting_room").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(number, ColumnMetadata.named("number").withIndex(12).ofType(Types.INTEGER).withSize(10));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(14).ofType(Types.INTEGER).withSize(10));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(price, ColumnMetadata.named("price").withIndex(4).ofType(Types.DOUBLE).withSize(17).withDigits(17));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(19).ofType(Types.BIGINT).withSize(19));
        addMetadata(tempColors, ColumnMetadata.named("temp_colors").withIndex(22).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(tempImage, ColumnMetadata.named("temp_image").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(tempSizes, ColumnMetadata.named("temp_sizes").withIndex(23).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(updatedOnline, ColumnMetadata.named("updated_online").withIndex(21).ofType(Types.BIT).withSize(1));
        addMetadata(viewImage, ColumnMetadata.named("view_image").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(vrSlider, ColumnMetadata.named("vr_slider").withIndex(11).ofType(Types.ARRAY).withSize(2147483647));
    }

}

