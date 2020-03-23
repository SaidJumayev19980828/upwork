package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTags is a Querydsl query type for QTags
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTags extends com.querydsl.sql.RelationalPathBase<QTags> {

    private static final long serialVersionUID = 1805898548;

    public static final QTags tags = new QTags("tags");

    public final StringPath alias = createString("alias");

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final NumberPath<Integer> graphId = createNumber("graphId", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath metadata = createString("metadata");

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath pName = createString("pName");

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QTags> tagsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QCategories> tagsCategoryIdFkey = createForeignKey(categoryId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> tagsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QProductTags> _productTagsTagIdFkey = createInvForeignKey(id, "tag_id");

    public final com.querydsl.sql.ForeignKey<QTagGraphNodes> _tagGraphNodesFk = createInvForeignKey(id, "tag_id");

    public QTags(String variable) {
        super(QTags.class, forVariable(variable), "public", "tags");
        addMetadata();
    }

    public QTags(String variable, String schema, String table) {
        super(QTags.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTags(String variable, String schema) {
        super(QTags.class, forVariable(variable), schema, "tags");
        addMetadata();
    }

    public QTags(Path<? extends QTags> path) {
        super(path.getType(), path.getMetadata(), "public", "tags");
        addMetadata();
    }

    public QTags(PathMetadata metadata) {
        super(QTags.class, metadata, "public", "tags");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alias, ColumnMetadata.named("alias").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(categoryId, ColumnMetadata.named("category_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(graphId, ColumnMetadata.named("graph_id").withIndex(9).ofType(Types.INTEGER).withSize(10));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(metadata, ColumnMetadata.named("metadata").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(8).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(removed, ColumnMetadata.named("removed").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

