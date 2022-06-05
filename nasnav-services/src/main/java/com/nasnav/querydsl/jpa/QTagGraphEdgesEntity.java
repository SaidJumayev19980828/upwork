package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.TagGraphEdgesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTagGraphEdgesEntity is a Querydsl query type for TagGraphEdgesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTagGraphEdgesEntity extends EntityPathBase<TagGraphEdgesEntity> {

    private static final long serialVersionUID = -1536872L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTagGraphEdgesEntity tagGraphEdgesEntity = new QTagGraphEdgesEntity("tagGraphEdgesEntity");

    public final QTagGraphNodeEntity child;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QTagGraphNodeEntity parent;

    public QTagGraphEdgesEntity(String variable) {
        this(TagGraphEdgesEntity.class, forVariable(variable), INITS);
    }

    public QTagGraphEdgesEntity(Path<? extends TagGraphEdgesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTagGraphEdgesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTagGraphEdgesEntity(PathMetadata metadata, PathInits inits) {
        this(TagGraphEdgesEntity.class, metadata, inits);
    }

    public QTagGraphEdgesEntity(Class<? extends TagGraphEdgesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.child = inits.isInitialized("child") ? new QTagGraphNodeEntity(forProperty("child"), inits.get("child")) : null;
        this.parent = inits.isInitialized("parent") ? new QTagGraphNodeEntity(forProperty("parent"), inits.get("parent")) : null;
    }

}

