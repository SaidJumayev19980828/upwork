package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShop360Products is a Querydsl query type for QShop360Products
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShop360Products extends com.querydsl.sql.RelationalPathBase<QShop360Products> {

    private static final long serialVersionUID = -1288774267;

    public static final QShop360Products shop360Products = new QShop360Products("shop360_products");

    public final NumberPath<Long> floorId = createNumber("floorId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Float> pitch = createNumber("pitch", Float.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Short> published = createNumber("published", Short.class);

    public final NumberPath<Long> sceneId = createNumber("sceneId", Long.class);

    public final NumberPath<Long> sectionId = createNumber("sectionId", Long.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final NumberPath<Float> yaw = createNumber("yaw", Float.class);

    public final com.querydsl.sql.PrimaryKey<QShop360Products> shop360ProductsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShopFloors> shop360ProductsFloorIdFkey = createForeignKey(floorId, "id");

    public final com.querydsl.sql.ForeignKey<QProducts> shop360ProductsProductIdFkey = createForeignKey(productId, "id");

    public final com.querydsl.sql.ForeignKey<QScenes> shop360ProductsSceneIdFkey = createForeignKey(sceneId, "id");

    public final com.querydsl.sql.ForeignKey<QShopSections> shop360ProductsSectionIdFkey = createForeignKey(sectionId, "id");

    public final com.querydsl.sql.ForeignKey<QShops> shop360ProductsShopIdFkey = createForeignKey(shopId, "id");

    public QShop360Products(String variable) {
        super(QShop360Products.class, forVariable(variable), "public", "shop360_products");
        addMetadata();
    }

    public QShop360Products(String variable, String schema, String table) {
        super(QShop360Products.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShop360Products(String variable, String schema) {
        super(QShop360Products.class, forVariable(variable), schema, "shop360_products");
        addMetadata();
    }

    public QShop360Products(Path<? extends QShop360Products> path) {
        super(path.getType(), path.getMetadata(), "public", "shop360_products");
        addMetadata();
    }

    public QShop360Products(PathMetadata metadata) {
        super(QShop360Products.class, metadata, "public", "shop360_products");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(floorId, ColumnMetadata.named("floor_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(9).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(pitch, ColumnMetadata.named("pitch").withIndex(6).ofType(Types.REAL).withSize(8).withDigits(8));
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(published, ColumnMetadata.named("published").withIndex(8).ofType(Types.SMALLINT).withSize(5).notNull());
        addMetadata(sceneId, ColumnMetadata.named("scene_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(sectionId, ColumnMetadata.named("section_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(yaw, ColumnMetadata.named("yaw").withIndex(7).ofType(Types.REAL).withSize(8).withDigits(8));
    }

}

