package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSections is a Querydsl query type for QSections
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSections extends com.querydsl.sql.RelationalPathBase<QSections> {

    private static final long serialVersionUID = -1684089372;

    public static final QSections sections = new QSections("sections");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shop360Id = createNumber("shop360Id", Long.class);

    public final StringPath title = createString("title");

    public final com.querydsl.sql.PrimaryKey<QSections> sectionsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> sectionsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QShop360s> sectionsShop360IdFkey = createForeignKey(shop360Id, "id");

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
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(image, ColumnMetadata.named("image").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(shop360Id, ColumnMetadata.named("shop360_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(title, ColumnMetadata.named("title").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

