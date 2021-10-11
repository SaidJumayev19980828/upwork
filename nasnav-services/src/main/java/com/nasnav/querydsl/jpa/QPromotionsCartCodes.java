package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.PromotionsCartCodes;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPromotionsCartCodes is a Querydsl query type for PromotionsCartCodes
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPromotionsCartCodes extends EntityPathBase<PromotionsCartCodes> {

    private static final long serialVersionUID = 113649609L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPromotionsCartCodes promotionsCartCodes = new QPromotionsCartCodes("promotionsCartCodes");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUserEntity user;

    public QPromotionsCartCodes(String variable) {
        this(PromotionsCartCodes.class, forVariable(variable), INITS);
    }

    public QPromotionsCartCodes(Path<? extends PromotionsCartCodes> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPromotionsCartCodes(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPromotionsCartCodes(PathMetadata metadata, PathInits inits) {
        this(PromotionsCartCodes.class, metadata, inits);
    }

    public QPromotionsCartCodes(Class<? extends PromotionsCartCodes> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

