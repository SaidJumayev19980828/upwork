package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPromotionsCodesUsed is a Querydsl query type for QPromotionsCodesUsed
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPromotionsCodesUsed extends com.querydsl.sql.RelationalPathBase<QPromotionsCodesUsed> {

    private static final long serialVersionUID = 395011389;

    public static final QPromotionsCodesUsed promotionsCodesUsed = new QPromotionsCodesUsed("promotions_codes_used");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> promotionId = createNumber("promotionId", Long.class);

    public final DateTimePath<java.sql.Timestamp> time = createDateTime("time", java.sql.Timestamp.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QPromotionsCodesUsed> promotionsCodesUsedPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QPromotions> promotionsCodesUsedPromotionIdFkey = createForeignKey(promotionId, "id");

    public final com.querydsl.sql.ForeignKey<QUsers> promotionsCodesUsedUserIdFkey = createForeignKey(userId, "id");

    public QPromotionsCodesUsed(String variable) {
        super(QPromotionsCodesUsed.class, forVariable(variable), "public", "promotions_codes_used");
        addMetadata();
    }

    public QPromotionsCodesUsed(String variable, String schema, String table) {
        super(QPromotionsCodesUsed.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPromotionsCodesUsed(String variable, String schema) {
        super(QPromotionsCodesUsed.class, forVariable(variable), schema, "promotions_codes_used");
        addMetadata();
    }

    public QPromotionsCodesUsed(Path<? extends QPromotionsCodesUsed> path) {
        super(path.getType(), path.getMetadata(), "public", "promotions_codes_used");
        addMetadata();
    }

    public QPromotionsCodesUsed(PathMetadata metadata) {
        super(QPromotionsCodesUsed.class, metadata, "public", "promotions_codes_used");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(promotionId, ColumnMetadata.named("promotion_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(time, ColumnMetadata.named("time").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

