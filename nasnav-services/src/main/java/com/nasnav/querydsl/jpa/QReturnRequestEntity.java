package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ReturnRequestEntity;
import com.nasnav.persistence.ReturnRequestItemEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReturnRequestEntity is a Querydsl query type for ReturnRequestEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QReturnRequestEntity extends EntityPathBase<ReturnRequestEntity> {

    private static final long serialVersionUID = 274861365L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReturnRequestEntity returnRequestEntity = new QReturnRequestEntity("returnRequestEntity");

    public final QEmployeeUserEntity createdByEmployee;

    public final QUserEntity createdByUser;

    public final DateTimePath<java.time.LocalDateTime> createdOn = createDateTime("createdOn", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMetaOrderEntity metaOrder;

    public final SetPath<ReturnRequestItemEntity, QReturnRequestItemEntity> returnedItems = this.<ReturnRequestItemEntity, QReturnRequestItemEntity>createSet("returnedItems", ReturnRequestItemEntity.class, QReturnRequestItemEntity.class, PathInits.DIRECT2);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public QReturnRequestEntity(String variable) {
        this(ReturnRequestEntity.class, forVariable(variable), INITS);
    }

    public QReturnRequestEntity(Path<? extends ReturnRequestEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReturnRequestEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReturnRequestEntity(PathMetadata metadata, PathInits inits) {
        this(ReturnRequestEntity.class, metadata, inits);
    }

    public QReturnRequestEntity(Class<? extends ReturnRequestEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.createdByEmployee = inits.isInitialized("createdByEmployee") ? new QEmployeeUserEntity(forProperty("createdByEmployee")) : null;
        this.createdByUser = inits.isInitialized("createdByUser") ? new QUserEntity(forProperty("createdByUser")) : null;
        this.metaOrder = inits.isInitialized("metaOrder") ? new QMetaOrderEntity(forProperty("metaOrder"), inits.get("metaOrder")) : null;
    }

}

