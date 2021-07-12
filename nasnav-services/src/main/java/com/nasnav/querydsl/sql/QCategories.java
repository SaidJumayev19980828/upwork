package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QCategories is a Querydsl query type for QCategories
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QCategories extends com.querydsl.sql.RelationalPathBase<QCategories> {

    private static final long serialVersionUID = 1655089234;

    public static final QCategories categories = new QCategories("categories");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logo = createString("logo");

    public final StringPath name = createString("name");

    public final NumberPath<Long> parentId = createNumber("parentId", Long.class);

    public final StringPath pName = createString("pName");

    public final com.querydsl.sql.PrimaryKey<QCategories> categoriesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QCategories> rails82f48f7407Fk = createForeignKey(parentId, "id");

    public final com.querydsl.sql.ForeignKey<QCategories> _rails82f48f7407Fk = createInvForeignKey(id, "parent_id");

    public final com.querydsl.sql.ForeignKey<QProducts> _productsCategoryIdFk = createInvForeignKey(id, "category_id");

    public final com.querydsl.sql.ForeignKey<QTags> _tagsCategoryIdFkey = createInvForeignKey(id, "category_id");

    public QCategories(String variable) {
        super(QCategories.class, forVariable(variable), "public", "categories");
        addMetadata();
    }

    public QCategories(String variable, String schema, String table) {
        super(QCategories.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QCategories(String variable, String schema) {
        super(QCategories.class, forVariable(variable), schema, "categories");
        addMetadata();
    }

    public QCategories(Path<? extends QCategories> path) {
        super(path.getType(), path.getMetadata(), "public", "categories");
        addMetadata();
    }

    public QCategories(PathMetadata metadata) {
        super(QCategories.class, metadata, "public", "categories");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(parentId, ColumnMetadata.named("parent_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

