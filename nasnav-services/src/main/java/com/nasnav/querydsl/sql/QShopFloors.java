package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShopFloors is a Querydsl query type for QShopFloors
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShopFloors extends com.querydsl.sql.RelationalPathBase<QShopFloors> {

    private static final long serialVersionUID = -1155533133;

    public static final QShopFloors shopFloors = new QShopFloors("shop_floors");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> number = createNumber("number", Integer.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shop360Id = createNumber("shop360Id", Long.class);

    public final com.querydsl.sql.PrimaryKey<QShopFloors> shopFloorsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShop360s> rails34316e0ca5Fk = createForeignKey(shop360Id, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> rails6333433b00Fk = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QShopSections> _railsF2b72e42c7Fk = createInvForeignKey(id, "shop_floor_id");

    public final com.querydsl.sql.ForeignKey<QShop360Products> _shop360ProductsFloorIdFkey = createInvForeignKey(id, "floor_id");

    public QShopFloors(String variable) {
        super(QShopFloors.class, forVariable(variable), "public", "shop_floors");
        addMetadata();
    }

    public QShopFloors(String variable, String schema, String table) {
        super(QShopFloors.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShopFloors(String variable, String schema) {
        super(QShopFloors.class, forVariable(variable), schema, "shop_floors");
        addMetadata();
    }

    public QShopFloors(Path<? extends QShopFloors> path) {
        super(path.getType(), path.getMetadata(), "public", "shop_floors");
        addMetadata();
    }

    public QShopFloors(PathMetadata metadata) {
        super(QShopFloors.class, metadata, "public", "shop_floors");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(number, ColumnMetadata.named("number").withIndex(2).ofType(Types.INTEGER).withSize(10));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(shop360Id, ColumnMetadata.named("shop360_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
    }

}

