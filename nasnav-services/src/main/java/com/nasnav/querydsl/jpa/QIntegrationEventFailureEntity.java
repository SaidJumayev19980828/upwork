package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.IntegrationEventFailureEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QIntegrationEventFailureEntity is a Querydsl query type for IntegrationEventFailureEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QIntegrationEventFailureEntity extends EntityPathBase<IntegrationEventFailureEntity> {

    private static final long serialVersionUID = 766807034L;

    public static final QIntegrationEventFailureEntity integrationEventFailureEntity = new QIntegrationEventFailureEntity("integrationEventFailureEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath eventData = createString("eventData");

    public final StringPath eventType = createString("eventType");

    public final StringPath fallbackException = createString("fallbackException");

    public final StringPath handleException = createString("handleException");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public QIntegrationEventFailureEntity(String variable) {
        super(IntegrationEventFailureEntity.class, forVariable(variable));
    }

    public QIntegrationEventFailureEntity(Path<? extends IntegrationEventFailureEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIntegrationEventFailureEntity(PathMetadata metadata) {
        super(IntegrationEventFailureEntity.class, metadata);
    }

}

