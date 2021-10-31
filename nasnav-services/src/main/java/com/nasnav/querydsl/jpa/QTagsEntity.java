package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.TagsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTagsEntity is a Querydsl query type for TagsEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QTagsEntity extends EntityPathBase<TagsEntity> {

    private static final long serialVersionUID = 859457225L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTagsEntity tagsEntity = new QTagsEntity("tagsEntity");

    public final org.springframework.data.jpa.domain.QAbstractPersistable _super = new org.springframework.data.jpa.domain.QAbstractPersistable(this);

    public final StringPath alias = createString("alias");

    public final QCategoriesEntity categoriesEntity;

    public final NumberPath<Integer> graphId = createNumber("graphId", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath metadata = createString("metadata");

    public final StringPath name = createString("name");

    public final QOrganizationEntity organizationEntity;

    public final StringPath pname = createString("pname");

    public final SetPath<ProductEntity, QProductEntity> products = this.<ProductEntity, QProductEntity>createSet("products", ProductEntity.class, QProductEntity.class, PathInits.DIRECT2);

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public QTagsEntity(String variable) {
        this(TagsEntity.class, forVariable(variable), INITS);
    }

    public QTagsEntity(Path<? extends TagsEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTagsEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTagsEntity(PathMetadata metadata, PathInits inits) {
        this(TagsEntity.class, metadata, inits);
    }

    public QTagsEntity(Class<? extends TagsEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.categoriesEntity = inits.isInitialized("categoriesEntity") ? new QCategoriesEntity(forProperty("categoriesEntity")) : null;
        this.organizationEntity = inits.isInitialized("organizationEntity") ? new QOrganizationEntity(forProperty("organizationEntity"), inits.get("organizationEntity")) : null;
    }

}

