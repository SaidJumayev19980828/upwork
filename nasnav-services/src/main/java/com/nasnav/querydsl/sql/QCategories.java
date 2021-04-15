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
 * QCategories is a Querydsl query type for QCategories
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QCategories extends com.querydsl.sql.RelationalPathBase<QCategories> {

    private static final long serialVersionUID = 1175053559;

    public static final QCategories categories = new QCategories("categories");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logo = createString("logo");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> parentId = createNumber("parentId", Integer.class);

    public final StringPath pName = createString("pName");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QCategories> categoriesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QCategories> rails82f48f7407Fk = createForeignKey(parentId, "id");

    public final com.querydsl.sql.ForeignKey<QProductsOlds> _railsFb915499a4Fk = createInvForeignKey(id, "category_id");

    public final com.querydsl.sql.ForeignKey<QProducts> _productsCategoryIdFk = createInvForeignKey(id, "category_id");

    public final com.querydsl.sql.ForeignKey<QTags> _tagsCategoryIdFkey = createInvForeignKey(id, "category_id");

    public final com.querydsl.sql.ForeignKey<QCategories> _rails82f48f7407Fk = createInvForeignKey(id, "parent_id");

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
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(parentId, ColumnMetadata.named("parent_id").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

