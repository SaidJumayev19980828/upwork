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
 * QSections is a Querydsl query type for QSections
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSections extends com.querydsl.sql.RelationalPathBase<QSections> {

    private static final long serialVersionUID = -410847287;

    public static final QSections sections = new QSections("sections");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath mobileJsonData = createString("mobileJsonData");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shop360Id = createNumber("shop360Id", Long.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final StringPath webJsonData = createString("webJsonData");

    public final com.querydsl.sql.PrimaryKey<QSections> sectionsPkey = createPrimaryKey(id);

    public QSections(String variable) {
        super(QSections.class, forVariable(variable), "public", "sections");
        addMetadata();
    }

    public QSections(String variable, String schema, String table) {
        super(QSections.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSections(String variable, String schema) {
        super(QSections.class, forVariable(variable), schema, "sections");
        addMetadata();
    }

    public QSections(Path<? extends QSections> path) {
        super(path.getType(), path.getMetadata(), "public", "sections");
        addMetadata();
    }

    public QSections(PathMetadata metadata) {
        super(QSections.class, metadata, "public", "sections");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(image, ColumnMetadata.named("image").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(mobileJsonData, ColumnMetadata.named("mobile_json_data").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(9).ofType(Types.BIGINT).withSize(19));
        addMetadata(shop360Id, ColumnMetadata.named("shop360_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(title, ColumnMetadata.named("title").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(webJsonData, ColumnMetadata.named("web_json_data").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

