package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ProductCollectionEntity;
import com.nasnav.persistence.ProductCollectionItemEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.TagsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductCollectionEntity is a Querydsl query type for ProductCollectionEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QProductCollectionEntity extends EntityPathBase<ProductCollectionEntity> {

    private static final long serialVersionUID = 1474683619L;

    public static final QProductCollectionEntity productCollectionEntity = new QProductCollectionEntity("productCollectionEntity");

    public final QProductEntity _super = new QProductEntity(this);

    //inherited
    public final StringPath barcode = _super.barcode;

    //inherited
    public final NumberPath<Long> brandId = _super.brandId;

    //inherited
    public final NumberPath<Long> categoryId = _super.categoryId;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> creationDate = _super.creationDate;

    //inherited
    public final StringPath description = _super.description;

    //inherited
    public final BooleanPath hide = _super.hide;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final SetPath<ProductCollectionItemEntity, QProductCollectionItemEntity> items = this.<ProductCollectionItemEntity, QProductCollectionItemEntity>createSet("items", ProductCollectionItemEntity.class, QProductCollectionItemEntity.class, PathInits.DIRECT2);

    //inherited
    public final StringPath name = _super.name;

    //inherited
    public final NumberPath<Long> organizationId = _super.organizationId;

    //inherited
    public final StringPath pname = _super.pname;

    //inherited
    public final NumberPath<Integer> priority = _super.priority;

    //inherited
    public final NumberPath<Integer> productType = _super.productType;

    //inherited
    public final SetPath<ProductVariantsEntity, QProductVariantsEntity> productVariants = _super.productVariants;

    //inherited
    public final NumberPath<Integer> removed = _super.removed;

    //inherited
    public final BooleanPath search360 = _super.search360;

    //inherited
    public final SetPath<TagsEntity, QTagsEntity> tags = _super.tags;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDate = _super.updateDate;

    public QProductCollectionEntity(String variable) {
        super(ProductCollectionEntity.class, forVariable(variable));
    }

    public QProductCollectionEntity(Path<? extends ProductCollectionEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductCollectionEntity(PathMetadata metadata) {
        super(ProductCollectionEntity.class, metadata);
    }

}

