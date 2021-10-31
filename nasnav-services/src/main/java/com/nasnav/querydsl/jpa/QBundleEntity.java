package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.BundleEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.TagsEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBundleEntity is a Querydsl query type for BundleEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBundleEntity extends EntityPathBase<BundleEntity> {

    private static final long serialVersionUID = 174844210L;

    public static final QBundleEntity bundleEntity = new QBundleEntity("bundleEntity");

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

    public final SetPath<StocksEntity, QStocksEntity> items = this.<StocksEntity, QStocksEntity>createSet("items", StocksEntity.class, QStocksEntity.class, PathInits.DIRECT2);

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

    public QBundleEntity(String variable) {
        super(BundleEntity.class, forVariable(variable));
    }

    public QBundleEntity(Path<? extends BundleEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBundleEntity(PathMetadata metadata) {
        super(BundleEntity.class, metadata);
    }

}

