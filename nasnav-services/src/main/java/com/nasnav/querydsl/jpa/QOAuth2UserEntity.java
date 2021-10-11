package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OAuth2UserEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOAuth2UserEntity is a Querydsl query type for OAuth2UserEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOAuth2UserEntity extends EntityPathBase<OAuth2UserEntity> {

    private static final long serialVersionUID = -754145802L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOAuth2UserEntity oAuth2UserEntity = new QOAuth2UserEntity("oAuth2UserEntity");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath loginToken = createString("loginToken");

    public final StringPath oAuth2Id = createString("oAuth2Id");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final QOAuth2ProviderEntity provider;

    public final QUserEntity user;

    public QOAuth2UserEntity(String variable) {
        this(OAuth2UserEntity.class, forVariable(variable), INITS);
    }

    public QOAuth2UserEntity(Path<? extends OAuth2UserEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOAuth2UserEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOAuth2UserEntity(PathMetadata metadata, PathInits inits) {
        this(OAuth2UserEntity.class, metadata, inits);
    }

    public QOAuth2UserEntity(Class<? extends OAuth2UserEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.provider = inits.isInitialized("provider") ? new QOAuth2ProviderEntity(forProperty("provider")) : null;
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

