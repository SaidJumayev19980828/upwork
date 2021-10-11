package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductRating;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductRating is a Querydsl query type for ProductRating
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProductRating extends EntityPathBase<ProductRating> {

    private static final long serialVersionUID = -1205391649L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductRating productRating = new QProductRating("productRating");

    public final BooleanPath approved = createBoolean("approved");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> rate = createNumber("rate", Integer.class);

    public final StringPath review = createString("review");

    public final DateTimePath<java.time.LocalDateTime> submissionDate = createDateTime("submissionDate", java.time.LocalDateTime.class);

    public final QUserEntity user;

    public final QProductVariantsEntity variant;

    public QProductRating(String variable) {
        this(ProductRating.class, forVariable(variable), INITS);
    }

    public QProductRating(Path<? extends ProductRating> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductRating(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductRating(PathMetadata metadata, PathInits inits) {
        this(ProductRating.class, metadata, inits);
    }

    public QProductRating(Class<? extends ProductRating> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
        this.variant = inits.isInitialized("variant") ? new QProductVariantsEntity(forProperty("variant"), inits.get("variant")) : null;
    }

}

