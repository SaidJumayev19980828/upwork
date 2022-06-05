package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.UserSubscriptionEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserSubscriptionEntity is a Querydsl query type for UserSubscriptionEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QUserSubscriptionEntity extends EntityPathBase<UserSubscriptionEntity> {

    private static final long serialVersionUID = 900810872L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserSubscriptionEntity userSubscriptionEntity = new QUserSubscriptionEntity("userSubscriptionEntity");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrganizationEntity organization;

    public final StringPath token = createString("token");

    public QUserSubscriptionEntity(String variable) {
        this(UserSubscriptionEntity.class, forVariable(variable), INITS);
    }

    public QUserSubscriptionEntity(Path<? extends UserSubscriptionEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserSubscriptionEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserSubscriptionEntity(PathMetadata metadata, PathInits inits) {
        this(UserSubscriptionEntity.class, metadata, inits);
    }

    public QUserSubscriptionEntity(Class<? extends UserSubscriptionEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.organization = inits.isInitialized("organization") ? new QOrganizationEntity(forProperty("organization"), inits.get("organization")) : null;
    }

}

