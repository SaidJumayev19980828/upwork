package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMetaOrdersPromotions is a Querydsl query type for QMetaOrdersPromotions
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMetaOrdersPromotions extends com.querydsl.sql.RelationalPathBase<QMetaOrdersPromotions> {

    private static final long serialVersionUID = 2078049232;

    public static final QMetaOrdersPromotions metaOrdersPromotions = new QMetaOrdersPromotions("meta_orders_promotions");

    public final NumberPath<Long> metaOrder = createNumber("metaOrder", Long.class);

    public final NumberPath<Long> promotion = createNumber("promotion", Long.class);

    public final com.querydsl.sql.ForeignKey<QMetaOrders> metaOrdersPromotionsMetaOrderFkey = createForeignKey(metaOrder, "id");

    public final com.querydsl.sql.ForeignKey<QPromotions> metaOrdersPromotionsPromotionFkey = createForeignKey(promotion, "id");

    public QMetaOrdersPromotions(String variable) {
        super(QMetaOrdersPromotions.class, forVariable(variable), "public", "meta_orders_promotions");
        addMetadata();
    }

    public QMetaOrdersPromotions(String variable, String schema, String table) {
        super(QMetaOrdersPromotions.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMetaOrdersPromotions(String variable, String schema) {
        super(QMetaOrdersPromotions.class, forVariable(variable), schema, "meta_orders_promotions");
        addMetadata();
    }

    public QMetaOrdersPromotions(Path<? extends QMetaOrdersPromotions> path) {
        super(path.getType(), path.getMetadata(), "public", "meta_orders_promotions");
        addMetadata();
    }

    public QMetaOrdersPromotions(PathMetadata metadata) {
        super(QMetaOrdersPromotions.class, metadata, "public", "meta_orders_promotions");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(metaOrder, ColumnMetadata.named("meta_order").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(promotion, ColumnMetadata.named("promotion").withIndex(1).ofType(Types.BIGINT).withSize(19));
    }

}

