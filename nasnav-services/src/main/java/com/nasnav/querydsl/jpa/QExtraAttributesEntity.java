package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ExtraAttributesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QExtraAttributesEntity is a Querydsl query type for ExtraAttributesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QExtraAttributesEntity extends EntityPathBase<ExtraAttributesEntity> {

    private static final long serialVersionUID = 1503585469L;

    public static final QExtraAttributesEntity extraAttributesEntity = new QExtraAttributesEntity("extraAttributesEntity");

    public final StringPath iconUrl = createString("iconUrl");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath type = createString("type");

    public QExtraAttributesEntity(String variable) {
        super(ExtraAttributesEntity.class, forVariable(variable));
    }

    public QExtraAttributesEntity(Path<? extends ExtraAttributesEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExtraAttributesEntity(PathMetadata metadata) {
        super(ExtraAttributesEntity.class, metadata);
    }

}

