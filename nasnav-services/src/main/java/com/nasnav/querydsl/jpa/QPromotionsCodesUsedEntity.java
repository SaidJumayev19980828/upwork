package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.PromotionsCodesUsedEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPromotionsCodesUsedEntity is a Querydsl query type for PromotionsCodesUsedEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPromotionsCodesUsedEntity extends EntityPathBase<PromotionsCodesUsedEntity> {

    private static final long serialVersionUID = -7125143L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPromotionsCodesUsedEntity promotionsCodesUsedEntity = new QPromotionsCodesUsedEntity("promotionsCodesUsedEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPromotionsEntity promotion;

    public final DateTimePath<java.time.LocalDateTime> time = createDateTime("time", java.time.LocalDateTime.class);

    public final QUserEntity user;

    public QPromotionsCodesUsedEntity(String variable) {
        this(PromotionsCodesUsedEntity.class, forVariable(variable), INITS);
    }

    public QPromotionsCodesUsedEntity(Path<? extends PromotionsCodesUsedEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPromotionsCodesUsedEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPromotionsCodesUsedEntity(PathMetadata metadata, PathInits inits) {
        this(PromotionsCodesUsedEntity.class, metadata, inits);
    }

    public QPromotionsCodesUsedEntity(Class<? extends PromotionsCodesUsedEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.promotion = inits.isInitialized("promotion") ? new QPromotionsEntity(forProperty("promotion"), inits.get("promotion")) : null;
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

