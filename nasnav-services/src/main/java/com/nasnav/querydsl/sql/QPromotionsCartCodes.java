package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPromotionsCartCodes is a Querydsl query type for QPromotionsCartCodes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPromotionsCartCodes extends com.querydsl.sql.RelationalPathBase<QPromotionsCartCodes> {

    private static final long serialVersionUID = 1691488544;

    public static final QPromotionsCartCodes promotionsCartCodes = new QPromotionsCartCodes("promotions_cart_codes");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QPromotionsCartCodes> promotionsCartCodesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QUsers> promotionsCartCodesUserIdFkey = createForeignKey(userId, "id");

    public QPromotionsCartCodes(String variable) {
        super(QPromotionsCartCodes.class, forVariable(variable), "public", "promotions_cart_codes");
        addMetadata();
    }

    public QPromotionsCartCodes(String variable, String schema, String table) {
        super(QPromotionsCartCodes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPromotionsCartCodes(String variable, String schema) {
        super(QPromotionsCartCodes.class, forVariable(variable), schema, "promotions_cart_codes");
        addMetadata();
    }

    public QPromotionsCartCodes(Path<? extends QPromotionsCartCodes> path) {
        super(path.getType(), path.getMetadata(), "public", "promotions_cart_codes");
        addMetadata();
    }

    public QPromotionsCartCodes(PathMetadata metadata) {
        super(QPromotionsCartCodes.class, metadata, "public", "promotions_cart_codes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

