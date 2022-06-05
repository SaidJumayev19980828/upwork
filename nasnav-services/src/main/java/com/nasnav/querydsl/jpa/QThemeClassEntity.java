package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ThemeClassEntity;
import com.nasnav.persistence.ThemeEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QThemeClassEntity is a Querydsl query type for ThemeClassEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QThemeClassEntity extends EntityPathBase<ThemeClassEntity> {

    private static final long serialVersionUID = 2096913823L;

    public static final QThemeClassEntity themeClassEntity = new QThemeClassEntity("themeClassEntity");

    public final org.springframework.data.jpa.domain.QAbstractPersistable _super = new org.springframework.data.jpa.domain.QAbstractPersistable(this);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final SetPath<ThemeEntity, QThemeEntity> themes = this.<ThemeEntity, QThemeEntity>createSet("themes", ThemeEntity.class, QThemeEntity.class, PathInits.DIRECT2);

    public QThemeClassEntity(String variable) {
        super(ThemeClassEntity.class, forVariable(variable));
    }

    public QThemeClassEntity(Path<? extends ThemeClassEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QThemeClassEntity(PathMetadata metadata) {
        super(ThemeClassEntity.class, metadata);
    }

}

