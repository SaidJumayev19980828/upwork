package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ThemeEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QThemeEntity is a Querydsl query type for ThemeEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QThemeEntity extends EntityPathBase<ThemeEntity> {

    private static final long serialVersionUID = -1597915329L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QThemeEntity themeEntity = new QThemeEntity("themeEntity");

    public final org.springframework.data.jpa.domain.QAbstractPersistable _super = new org.springframework.data.jpa.domain.QAbstractPersistable(this);

    public final StringPath defaultSettings = createString("defaultSettings");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath previewImage = createString("previewImage");

    public final QThemeClassEntity themeClassEntity;

    public final StringPath uid = createString("uid");

    public QThemeEntity(String variable) {
        this(ThemeEntity.class, forVariable(variable), INITS);
    }

    public QThemeEntity(Path<? extends ThemeEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QThemeEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QThemeEntity(PathMetadata metadata, PathInits inits) {
        this(ThemeEntity.class, metadata, inits);
    }

    public QThemeEntity(Class<? extends ThemeEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.themeClassEntity = inits.isInitialized("themeClassEntity") ? new QThemeClassEntity(forProperty("themeClassEntity")) : null;
    }

}

