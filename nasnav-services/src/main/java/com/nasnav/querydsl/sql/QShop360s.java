package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShop360s is a Querydsl query type for QShop360s
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShop360s extends com.querydsl.sql.RelationalPathBase<QShop360s> {

    private static final long serialVersionUID = 1316611986;

    public static final QShop360s shop360s = new QShop360s("shop360s");

    public final BooleanPath deleted = createBoolean("deleted");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mobileJsonData = createString("mobileJsonData");

    public final StringPath previewJsonData = createString("previewJsonData");

    public final BooleanPath published = createBoolean("published");

    public final StringPath sceneName = createString("sceneName");

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final StringPath url = createString("url");

    public final StringPath webJsonData = createString("webJsonData");

    public final com.querydsl.sql.PrimaryKey<QShop360s> shop360sPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShops> rails888a1fc9beFk = createForeignKey(shopId, "id");

    public final com.querydsl.sql.ForeignKey<QShopFloors> _rails34316e0ca5Fk = createInvForeignKey(id, "shop360_id");

    public final com.querydsl.sql.ForeignKey<QProductPositions> _rails7a3b031e76Fk = createInvForeignKey(id, "shop360_id");

    public final com.querydsl.sql.ForeignKey<QSections> _sectionsShop360IdFkey = createInvForeignKey(id, "shop360_id");

    public QShop360s(String variable) {
        super(QShop360s.class, forVariable(variable), "public", "shop360s");
        addMetadata();
    }

    public QShop360s(String variable, String schema, String table) {
        super(QShop360s.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShop360s(String variable, String schema) {
        super(QShop360s.class, forVariable(variable), schema, "shop360s");
        addMetadata();
    }

    public QShop360s(Path<? extends QShop360s> path) {
        super(path.getType(), path.getMetadata(), "public", "shop360s");
        addMetadata();
    }

    public QShop360s(PathMetadata metadata) {
        super(QShop360s.class, metadata, "public", "shop360s");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(deleted, ColumnMetadata.named("deleted").withIndex(9).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(mobileJsonData, ColumnMetadata.named("mobile_json_data").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(previewJsonData, ColumnMetadata.named("preview_json_data").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(published, ColumnMetadata.named("published").withIndex(7).ofType(Types.BIT).withSize(1));
        addMetadata(sceneName, ColumnMetadata.named("scene_name").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(url, ColumnMetadata.named("url").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(webJsonData, ColumnMetadata.named("web_json_data").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

