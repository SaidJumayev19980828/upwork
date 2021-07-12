package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QUnits is a Querydsl query type for QUnits
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUnits extends com.querydsl.sql.RelationalPathBase<QUnits> {

    private static final long serialVersionUID = 800189305;

    public static final QUnits units = new QUnits("units");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QUnits> unitsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QStocks> _stocksUnitIdFkey = createInvForeignKey(id, "unit_id");

    public QUnits(String variable) {
        super(QUnits.class, forVariable(variable), "public", "units");
        addMetadata();
    }

    public QUnits(String variable, String schema, String table) {
        super(QUnits.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUnits(String variable, String schema) {
        super(QUnits.class, forVariable(variable), schema, "units");
        addMetadata();
    }

    public QUnits(Path<? extends QUnits> path) {
        super(path.getType(), path.getMetadata(), "public", "units");
        addMetadata();
    }

    public QUnits(PathMetadata metadata) {
        super(QUnits.class, metadata, "public", "units");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

