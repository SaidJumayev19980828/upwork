package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.SocialEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSocialEntity is a Querydsl query type for SocialEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QSocialEntity extends EntityPathBase<SocialEntity> {

    private static final long serialVersionUID = 1906881501L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSocialEntity socialEntity = new QSocialEntity("socialEntity");

    public final StringPath facebook = createString("facebook");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath instagram = createString("instagram");

    public final StringPath linkedin = createString("linkedin");

    public final QOrganizationEntity organizationEntity;

    public final StringPath pinterest = createString("pinterest");

    public final StringPath twitter = createString("twitter");

    public final StringPath youtube = createString("youtube");

    public QSocialEntity(String variable) {
        this(SocialEntity.class, forVariable(variable), INITS);
    }

    public QSocialEntity(Path<? extends SocialEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSocialEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSocialEntity(PathMetadata metadata, PathInits inits) {
        this(SocialEntity.class, metadata, inits);
    }

    public QSocialEntity(Class<? extends SocialEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
    }

}

