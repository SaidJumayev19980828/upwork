package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.SeoKeywordEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeoKeywordEntity is a Querydsl query type for SeoKeywordEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSeoKeywordEntity extends EntityPathBase<SeoKeywordEntity> {

    private static final long serialVersionUID = 887112060L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeoKeywordEntity seoKeywordEntity = new QSeoKeywordEntity("seoKeywordEntity");

    public final NumberPath<Long> entityId = createNumber("entityId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public final QOrganizationEntity organization;

    public final NumberPath<Integer> typeId = createNumber("typeId", Integer.class);

    public QSeoKeywordEntity(String variable) {
        this(SeoKeywordEntity.class, forVariable(variable), INITS);
    }

    public QSeoKeywordEntity(Path<? extends SeoKeywordEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeoKeywordEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeoKeywordEntity(PathMetadata metadata, PathInits inits) {
        this(SeoKeywordEntity.class, metadata, inits);
    }

    public QSeoKeywordEntity(Class<? extends SeoKeywordEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
    }

}

