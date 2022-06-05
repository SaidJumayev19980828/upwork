package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.StockUnitEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QStockUnitEntity is a Querydsl query type for StockUnitEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStockUnitEntity extends EntityPathBase<StockUnitEntity> {

    private static final long serialVersionUID = 1619806448L;

    public static final QStockUnitEntity stockUnitEntity = new QStockUnitEntity("stockUnitEntity");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public QStockUnitEntity(String variable) {
        super(StockUnitEntity.class, forVariable(variable));
    }

    public QStockUnitEntity(Path<? extends StockUnitEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStockUnitEntity(PathMetadata metadata) {
        super(StockUnitEntity.class, metadata);
    }

}

