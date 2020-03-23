package com.nasnav.model.querydsl.sql;

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

    private static final long serialVersionUID = 274873794;

    public static final QScenes scenes = new QScenes("scenes");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shopSectionId = createNumber("shopSectionId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QScenes> scenesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShopSections> railsA66b01e057Fk = createForeignKey(shopSectionId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> railsD232c97110Fk = createForeignKey(organizationId, "id");

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
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(image, ColumnMetadata.named("image").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(shopSectionId, ColumnMetadata.named("shop_section_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

