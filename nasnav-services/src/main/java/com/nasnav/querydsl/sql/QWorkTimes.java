package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QWorkTimes is a Querydsl query type for QWorkTimes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWorkTimes extends com.querydsl.sql.RelationalPathBase<QWorkTimes> {

    private static final long serialVersionUID = 1907403898;

    public static final QWorkTimes workTimes = new QWorkTimes("work_times");

    public final DateTimePath<java.sql.Timestamp> closingAt = createDateTime("closingAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final BooleanPath fri = createBoolean("fri");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath mon = createBoolean("mon");

    public final DateTimePath<java.sql.Timestamp> openingAt = createDateTime("openingAt", java.sql.Timestamp.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final BooleanPath sat = createBoolean("sat");

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final BooleanPath sun = createBoolean("sun");

    public final BooleanPath thu = createBoolean("thu");

    public final BooleanPath tue = createBoolean("tue");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final BooleanPath wed = createBoolean("wed");

    public final com.querydsl.sql.PrimaryKey<QWorkTimes> workTimesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> railsEed183b2b2Fk = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QShops> railsC68a0170e4Fk = createForeignKey(shopId, "id");

    public QWorkTimes(String variable) {
        super(QWorkTimes.class, forVariable(variable), "public", "work_times");
        addMetadata();
    }

    public QWorkTimes(String variable, String schema, String table) {
        super(QWorkTimes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWorkTimes(String variable, String schema) {
        super(QWorkTimes.class, forVariable(variable), schema, "work_times");
        addMetadata();
    }

    public QWorkTimes(Path<? extends QWorkTimes> path) {
        super(path.getType(), path.getMetadata(), "public", "work_times");
        addMetadata();
    }

    public QWorkTimes(PathMetadata metadata) {
        super(QWorkTimes.class, metadata, "public", "work_times");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(closingAt, ColumnMetadata.named("closing_at").withIndex(10).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(13).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(fri, ColumnMetadata.named("fri").withIndex(8).ofType(Types.BIT).withSize(1));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(mon, ColumnMetadata.named("mon").withIndex(4).ofType(Types.BIT).withSize(1));
        addMetadata(openingAt, ColumnMetadata.named("opening_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(12).ofType(Types.BIGINT).withSize(19));
        addMetadata(sat, ColumnMetadata.named("sat").withIndex(2).ofType(Types.BIT).withSize(1));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(11).ofType(Types.BIGINT).withSize(19));
        addMetadata(sun, ColumnMetadata.named("sun").withIndex(3).ofType(Types.BIT).withSize(1));
        addMetadata(thu, ColumnMetadata.named("thu").withIndex(7).ofType(Types.BIT).withSize(1));
        addMetadata(tue, ColumnMetadata.named("tue").withIndex(5).ofType(Types.BIT).withSize(1));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(14).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(wed, ColumnMetadata.named("wed").withIndex(6).ofType(Types.BIT).withSize(1));
    }

}

