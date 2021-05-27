package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPromotions is a Querydsl query type for QPromotions
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPromotions extends com.querydsl.sql.RelationalPathBase<QPromotions> {

    private static final long serialVersionUID = 1352792678;

    public static final QPromotions promotions = new QPromotions("promotions");

    public final StringPath code = createString("code");

    public final StringPath constrains = createString("constrains");

    public final NumberPath<Long> createdBy = createNumber("createdBy", Long.class);

    public final DateTimePath<java.sql.Timestamp> createdOn = createDateTime("createdOn", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> dateEnd = createDateTime("dateEnd", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> dateStart = createDateTime("dateStart", java.sql.Timestamp.class);

    public final StringPath discount = createString("discount");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath identifier = createString("identifier");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<Integer> typeId = createNumber("typeId", Integer.class);

    public final NumberPath<Integer> userRestricted = createNumber("userRestricted", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QPromotions> promotionsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QEmployeeUsers> promotionsCreatedByFkey = createForeignKey(createdBy, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> promotionsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QMetaOrdersPromotions> _metaOrdersPromotionsPromotionFkey = createInvForeignKey(id, "promotion");

    public final com.querydsl.sql.ForeignKey<QOrders> _ordersPromotionFkey = createInvForeignKey(id, "promotion");

    public final com.querydsl.sql.ForeignKey<QPromotionsCodesUsed> _promotionsCodesUsedPromotionIdFkey = createInvForeignKey(id, "promotion_id");

    public QPromotions(String variable) {
        super(QPromotions.class, forVariable(variable), "public", "promotions");
        addMetadata();
    }

    public QPromotions(String variable, String schema, String table) {
        super(QPromotions.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPromotions(String variable, String schema) {
        super(QPromotions.class, forVariable(variable), schema, "promotions");
        addMetadata();
    }

    public QPromotions(Path<? extends QPromotions> path) {
        super(path.getType(), path.getMetadata(), "public", "promotions");
        addMetadata();
    }

    public QPromotions(PathMetadata metadata) {
        super(QPromotions.class, metadata, "public", "promotions");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("code").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(constrains, ColumnMetadata.named("constrains").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdBy, ColumnMetadata.named("created_by").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(createdOn, ColumnMetadata.named("created_on").withIndex(12).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(dateEnd, ColumnMetadata.named("date_end").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(dateStart, ColumnMetadata.named("date_start").withIndex(4).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(discount, ColumnMetadata.named("discount").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(identifier, ColumnMetadata.named("identifier").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(priority, ColumnMetadata.named("priority").withIndex(14).ofType(Types.INTEGER).withSize(10));
        addMetadata(status, ColumnMetadata.named("status").withIndex(6).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(typeId, ColumnMetadata.named("type_id").withIndex(13).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(userRestricted, ColumnMetadata.named("user_restricted").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

