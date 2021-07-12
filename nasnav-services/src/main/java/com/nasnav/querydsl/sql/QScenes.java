package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QScenes is a Querydsl query type for QScenes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QScenes extends com.querydsl.sql.RelationalPathBase<QScenes> {

    private static final long serialVersionUID = -1031477603;

    public static final QScenes scenes = new QScenes("scenes");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final StringPath resized = createString("resized");

    public final NumberPath<Long> shopSectionId = createNumber("shopSectionId", Long.class);

    public final StringPath thumbnail = createString("thumbnail");

    public final com.querydsl.sql.PrimaryKey<QScenes> scenesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShopSections> railsA66b01e057Fk = createForeignKey(shopSectionId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> railsD232c97110Fk = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QShop360Products> _shop360ProductsSceneIdFkey = createInvForeignKey(id, "scene_id");

    public QScenes(String variable) {
        super(QScenes.class, forVariable(variable), "public", "scenes");
        addMetadata();
    }

    public QScenes(String variable, String schema, String table) {
        super(QScenes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QScenes(String variable, String schema) {
        super(QScenes.class, forVariable(variable), schema, "scenes");
        addMetadata();
    }

    public QScenes(Path<? extends QScenes> path) {
        super(path.getType(), path.getMetadata(), "public", "scenes");
        addMetadata();
    }

    public QScenes(PathMetadata metadata) {
        super(QScenes.class, metadata, "public", "scenes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(image, ColumnMetadata.named("image").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(priority, ColumnMetadata.named("priority").withIndex(8).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(resized, ColumnMetadata.named("resized").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(shopSectionId, ColumnMetadata.named("shop_section_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(thumbnail, ColumnMetadata.named("thumbnail").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

