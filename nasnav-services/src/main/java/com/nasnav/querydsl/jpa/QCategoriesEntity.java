package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.CategoriesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QCategoriesEntity is a Querydsl query type for CategoriesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QCategoriesEntity extends EntityPathBase<CategoriesEntity> {

    private static final long serialVersionUID = 695272652L;

    public static final QCategoriesEntity categoriesEntity = new QCategoriesEntity("categoriesEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logo = createString("logo");

    public final StringPath name = createString("name");

    public final NumberPath<Long> parentId = createNumber("parentId", Long.class);

    public final StringPath pname = createString("pname");

    public QCategoriesEntity(String variable) {
        super(CategoriesEntity.class, forVariable(variable));
    }

    public QCategoriesEntity(Path<? extends CategoriesEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCategoriesEntity(PathMetadata metadata) {
        super(CategoriesEntity.class, metadata);
    }

}

