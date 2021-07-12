package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProductFeatures is a Querydsl query type for QProductFeatures
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductFeatures extends com.querydsl.sql.RelationalPathBase<QProductFeatures> {

    private static final long serialVersionUID = -58702378;

    public static final QProductFeatures productFeatures = new QProductFeatures("product_features");

    public final StringPath description = createString("description");

    public final StringPath extraData = createString("extraData");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> level = createNumber("level", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath pName = createString("pName");

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QProductFeatures> productFeaturesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> productFeaturesOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QProductFeatures(String variable) {
        super(QProductFeatures.class, forVariable(variable), "public", "product_features");
        addMetadata();
    }

    public QProductFeatures(String variable, String schema, String table) {
        super(QProductFeatures.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductFeatures(String variable, String schema) {
        super(QProductFeatures.class, forVariable(variable), schema, "product_features");
        addMetadata();
    }

    public QProductFeatures(Path<? extends QProductFeatures> path) {
        super(path.getType(), path.getMetadata(), "public", "product_features");
        addMetadata();
    }

    public QProductFeatures(PathMetadata metadata) {
        super(QProductFeatures.class, metadata, "public", "product_features");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(description, ColumnMetadata.named("description").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(extraData, ColumnMetadata.named("extra_data").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(level, ColumnMetadata.named("level").withIndex(6).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(type, ColumnMetadata.named("type").withIndex(8).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

