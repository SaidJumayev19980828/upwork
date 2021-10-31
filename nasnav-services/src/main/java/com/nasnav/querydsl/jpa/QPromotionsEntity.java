package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.PromotionsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPromotionsEntity is a Querydsl query type for PromotionsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPromotionsEntity extends EntityPathBase<PromotionsEntity> {

    private static final long serialVersionUID = 1344501728L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPromotionsEntity promotionsEntity = new QPromotionsEntity("promotionsEntity");

    public final StringPath code = createString("code");

    public final StringPath constrainsJson = createString("constrainsJson");

    public final QEmployeeUserEntity createdBy;

    public final DateTimePath<java.time.LocalDateTime> createdOn = createDateTime("createdOn", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> dateEnd = createDateTime("dateEnd", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> dateStart = createDateTime("dateStart", java.time.LocalDateTime.class);

    public final StringPath discountJson = createString("discountJson");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath identifier = createString("identifier");

    public final QOrganizationEntity organization;

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<Integer> typeId = createNumber("typeId", Integer.class);

    public final NumberPath<Integer> userRestricted = createNumber("userRestricted", Integer.class);

    public QPromotionsEntity(String variable) {
        this(PromotionsEntity.class, forVariable(variable), INITS);
    }

    public QPromotionsEntity(Path<? extends PromotionsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPromotionsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPromotionsEntity(PathMetadata metadata, PathInits inits) {
        this(PromotionsEntity.class, metadata, inits);
    }

    public QPromotionsEntity(Class<? extends PromotionsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.createdBy = inits.isInitialized("createdBy") ? new QEmployeeUserEntity(forProperty("createdBy")) : null;
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
    }

}

