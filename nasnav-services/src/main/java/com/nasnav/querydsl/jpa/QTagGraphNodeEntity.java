package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.TagGraphNodeEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTagGraphNodeEntity is a Querydsl query type for TagGraphNodeEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTagGraphNodeEntity extends EntityPathBase<TagGraphNodeEntity> {

    private static final long serialVersionUID = -864184858L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTagGraphNodeEntity tagGraphNodeEntity = new QTagGraphNodeEntity("tagGraphNodeEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final QTagsEntity tag;

    public QTagGraphNodeEntity(String variable) {
        this(TagGraphNodeEntity.class, forVariable(variable), INITS);
    }

    public QTagGraphNodeEntity(Path<? extends TagGraphNodeEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTagGraphNodeEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTagGraphNodeEntity(PathMetadata metadata, PathInits inits) {
        this(TagGraphNodeEntity.class, metadata, inits);
    }

    public QTagGraphNodeEntity(Class<? extends TagGraphNodeEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.tag = inits.isInitialized("tag") ? new QTagsEntity(forProperty("tag"), inits.get("tag")) : null;
    }

}

