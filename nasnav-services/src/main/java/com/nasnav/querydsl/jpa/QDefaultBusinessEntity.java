package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.DefaultBusinessEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import java.io.Serializable;


/**
 * QDefaultBusinessEntity is a Querydsl query type for DefaultBusinessEntity
 */
@Generated("com.querydsl.codegen.SupertypeSerializer")
public class QDefaultBusinessEntity extends EntityPathBase<DefaultBusinessEntity<? extends Serializable>> {

    private static final long serialVersionUID = 1568419031L;

    public static final QDefaultBusinessEntity defaultBusinessEntity = new QDefaultBusinessEntity("defaultBusinessEntity");

    public final SimplePath<java.io.Serializable> id = createSimple("id", java.io.Serializable.class);

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QDefaultBusinessEntity(String variable) {
        super((Class) DefaultBusinessEntity.class, forVariable(variable));
    }

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QDefaultBusinessEntity(Path<? extends DefaultBusinessEntity> path) {
        super((Class) path.getType(), path.getMetadata());
    }

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QDefaultBusinessEntity(PathMetadata metadata) {
        super((Class) DefaultBusinessEntity.class, metadata);
    }

}

