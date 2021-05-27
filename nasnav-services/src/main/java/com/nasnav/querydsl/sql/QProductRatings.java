package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProductRatings is a Querydsl query type for QProductRatings
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductRatings extends com.querydsl.sql.RelationalPathBase<QProductRatings> {

    private static final long serialVersionUID = -394393155;

    public static final QProductRatings productRatings = new QProductRatings("product_ratings");

    public final BooleanPath approved = createBoolean("approved");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> rate = createNumber("rate", Integer.class);

    public final StringPath review = createString("review");

    public final DateTimePath<java.sql.Timestamp> submissionDate = createDateTime("submissionDate", java.sql.Timestamp.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final NumberPath<Long> variantId = createNumber("variantId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QProductRatings> productRatingsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QUsers> productRatingsUserIdFkey = createForeignKey(userId, "id");

    public final com.querydsl.sql.ForeignKey<QProductVariants> productRatingsVariantIdFkey = createForeignKey(variantId, "id");

    public QProductRatings(String variable) {
        super(QProductRatings.class, forVariable(variable), "public", "product_ratings");
        addMetadata();
    }

    public QProductRatings(String variable, String schema, String table) {
        super(QProductRatings.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductRatings(String variable, String schema) {
        super(QProductRatings.class, forVariable(variable), schema, "product_ratings");
        addMetadata();
    }

    public QProductRatings(Path<? extends QProductRatings> path) {
        super(path.getType(), path.getMetadata(), "public", "product_ratings");
        addMetadata();
    }

    public QProductRatings(PathMetadata metadata) {
        super(QProductRatings.class, metadata, "public", "product_ratings");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(approved, ColumnMetadata.named("approved").withIndex(7).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(rate, ColumnMetadata.named("rate").withIndex(4).ofType(Types.INTEGER).withSize(10));
        addMetadata(review, ColumnMetadata.named("review").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(submissionDate, ColumnMetadata.named("submission_date").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(variantId, ColumnMetadata.named("variant_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

