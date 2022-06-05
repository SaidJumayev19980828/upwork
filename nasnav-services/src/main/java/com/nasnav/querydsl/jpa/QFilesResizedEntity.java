package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.FilesResizedEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFilesResizedEntity is a Querydsl query type for FilesResizedEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QFilesResizedEntity extends EntityPathBase<FilesResizedEntity> {

    private static final long serialVersionUID = -798060183L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFilesResizedEntity filesResizedEntity = new QFilesResizedEntity("filesResizedEntity");

    public final NumberPath<Integer> height = createNumber("height", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final QFileEntity originalFile;

    public final NumberPath<Integer> width = createNumber("width", Integer.class);

    public QFilesResizedEntity(String variable) {
        this(FilesResizedEntity.class, forVariable(variable), INITS);
    }

    public QFilesResizedEntity(Path<? extends FilesResizedEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFilesResizedEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFilesResizedEntity(PathMetadata metadata, PathInits inits) {
        this(FilesResizedEntity.class, metadata, inits);
    }

    public QFilesResizedEntity(Class<? extends FilesResizedEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.originalFile = inits.isInitialized("originalFile") ? new QFileEntity(forProperty("originalFile"), inits.get("originalFile")) : null;
    }

}

