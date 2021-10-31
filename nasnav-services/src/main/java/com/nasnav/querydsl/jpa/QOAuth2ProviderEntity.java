package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OAuth2ProviderEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOAuth2ProviderEntity is a Querydsl query type for OAuth2ProviderEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOAuth2ProviderEntity extends EntityPathBase<OAuth2ProviderEntity> {

    private static final long serialVersionUID = -581068900L;

    public static final QOAuth2ProviderEntity oAuth2ProviderEntity = new QOAuth2ProviderEntity("oAuth2ProviderEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath providerName = createString("providerName");

    public QOAuth2ProviderEntity(String variable) {
        super(OAuth2ProviderEntity.class, forVariable(variable));
    }

    public QOAuth2ProviderEntity(Path<? extends OAuth2ProviderEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOAuth2ProviderEntity(PathMetadata metadata) {
        super(OAuth2ProviderEntity.class, metadata);
    }

}

