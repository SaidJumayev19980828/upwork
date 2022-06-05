package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.TagsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductEntity is a Querydsl query type for ProductEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProductEntity extends EntityPathBase<ProductEntity> {

    private static final long serialVersionUID = -1565564635L;

    public static final QProductEntity productEntity = new QProductEntity("productEntity");

    public final StringPath barcode = createString("barcode");

    public final NumberPath<Long> brandId = createNumber("brandId", Long.class);

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> creationDate = createDateTime("creationDate", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final BooleanPath hide = createBoolean("hide");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath pname = createString("pname");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Integer> productType = createNumber("productType", Integer.class);

    public final SetPath<ProductVariantsEntity, QProductVariantsEntity> productVariants = this.<ProductVariantsEntity, QProductVariantsEntity>createSet("productVariants", ProductVariantsEntity.class, QProductVariantsEntity.class, PathInits.DIRECT2);

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final BooleanPath search360 = createBoolean("search360");

    public final SetPath<TagsEntity, QTagsEntity> tags = this.<TagsEntity, QTagsEntity>createSet("tags", TagsEntity.class, QTagsEntity.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updateDate = createDateTime("updateDate", java.time.LocalDateTime.class);

    public QProductEntity(String variable) {
        super(ProductEntity.class, forVariable(variable));
    }

    public QProductEntity(Path<? extends ProductEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductEntity(PathMetadata metadata) {
        super(ProductEntity.class, metadata);
    }

}

