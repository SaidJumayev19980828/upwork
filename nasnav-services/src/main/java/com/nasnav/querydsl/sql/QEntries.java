package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QEntries is a Querydsl query type for QEntries
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QEntries extends com.querydsl.sql.RelationalPathBase<QEntries> {

    private static final long serialVersionUID = 719899541;

    public static final QEntries entries = new QEntries("entries");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final NumberPath<Long> pocketId = createNumber("pocketId", Long.class);

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final NumberPath<Long> subProductId = createNumber("subProductId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QEntries> entriesPkey = createPrimaryKey(id);

    public QEntries(String variable) {
        super(QEntries.class, forVariable(variable), "public", "entries");
        addMetadata();
    }

    public QEntries(String variable, String schema, String table) {
        super(QEntries.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEntries(String variable, String schema) {
        super(QEntries.class, forVariable(variable), schema, "entries");
        addMetadata();
    }

    public QEntries(Path<? extends QEntries> path) {
        super(path.getType(), path.getMetadata(), "public", "entries");
        addMetadata();
    }

    public QEntries(PathMetadata metadata) {
        super(QEntries.class, metadata, "public", "entries");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(orderId, ColumnMetadata.named("order_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(pocketId, ColumnMetadata.named("pocket_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(price, ColumnMetadata.named("price").withIndex(2).ofType(Types.NUMERIC).withSize(10).withDigits(2));
        addMetadata(quantity, ColumnMetadata.named("quantity").withIndex(7).ofType(Types.INTEGER).withSize(10));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(subProductId, ColumnMetadata.named("sub_product_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

