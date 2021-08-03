package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QVariantFeatureValues is a Querydsl query type for QVariantFeatureValues
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QVariantFeatureValues extends com.querydsl.sql.RelationalPathBase<QVariantFeatureValues> {

    private static final long serialVersionUID = -1230575127;

    public static final QVariantFeatureValues variantFeatureValues = new QVariantFeatureValues("variant_feature_values");

    public final NumberPath<Integer> featureId = createNumber("featureId", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath value = createString("value");

    public final NumberPath<Long> variantId = createNumber("variantId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QVariantFeatureValues> variantFeatureValuesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QProductVariants> variantFeatureValuesVariantIdFkey = createForeignKey(variantId, "id");

    public final com.querydsl.sql.ForeignKey<QProductFeatures> variantFeatureValuesFeatureIdFkey = createForeignKey(featureId, "id");

    public QVariantFeatureValues(String variable) {
        super(QVariantFeatureValues.class, forVariable(variable), "public", "variant_feature_values");
        addMetadata();
    }

    public QVariantFeatureValues(String variable, String schema, String table) {
        super(QVariantFeatureValues.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QVariantFeatureValues(String variable, String schema) {
        super(QVariantFeatureValues.class, forVariable(variable), schema, "variant_feature_values");
        addMetadata();
    }

    public QVariantFeatureValues(Path<? extends QVariantFeatureValues> path) {
        super(path.getType(), path.getMetadata(), "public", "variant_feature_values");
        addMetadata();
    }

    public QVariantFeatureValues(PathMetadata metadata) {
        super(QVariantFeatureValues.class, metadata, "public", "variant_feature_values");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(featureId, ColumnMetadata.named("feature_id").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(value, ColumnMetadata.named("value").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(variantId, ColumnMetadata.named("variant_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

