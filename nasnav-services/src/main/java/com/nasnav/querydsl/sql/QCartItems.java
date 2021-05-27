package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QCartItems is a Querydsl query type for QCartItems
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QCartItems extends com.querydsl.sql.RelationalPathBase<QCartItems> {

    private static final long serialVersionUID = 204271498;

    public static final QCartItems cartItems = new QCartItems("cart_items");

    public final StringPath additionalData = createString("additionalData");

    public final StringPath coverImage = createString("coverImage");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> isWishlist = createNumber("isWishlist", Integer.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> stockId = createNumber("stockId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath variantFeatures = createString("variantFeatures");

    public final com.querydsl.sql.PrimaryKey<QCartItems> cartItemsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QStocks> cartItemsStockIdFkey = createForeignKey(stockId, "id");

    public final com.querydsl.sql.ForeignKey<QUsers> cartItemsUserIdFkey = createForeignKey(userId, "id");

    public QCartItems(String variable) {
        super(QCartItems.class, forVariable(variable), "public", "cart_items");
        addMetadata();
    }

    public QCartItems(String variable, String schema, String table) {
        super(QCartItems.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QCartItems(String variable, String schema) {
        super(QCartItems.class, forVariable(variable), schema, "cart_items");
        addMetadata();
    }

    public QCartItems(Path<? extends QCartItems> path) {
        super(path.getType(), path.getMetadata(), "public", "cart_items");
        addMetadata();
    }

    public QCartItems(PathMetadata metadata) {
        super(QCartItems.class, metadata, "public", "cart_items");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(additionalData, ColumnMetadata.named("additional_data").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(coverImage, ColumnMetadata.named("cover_image").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(isWishlist, ColumnMetadata.named("is_wishlist").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(quantity, ColumnMetadata.named("quantity").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(stockId, ColumnMetadata.named("stock_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(variantFeatures, ColumnMetadata.named("variant_features").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

