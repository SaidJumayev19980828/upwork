package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUnits extends com.querydsl.sql.RelationalPathBase<QUnits> {

    private static final long serialVersionUID = -198088154;

    public static final QUnits units = new QUnits("units");

    public final StringPath name = createString("name");

    public final NumberPath<Long> id = createNumber("id", Long.class);

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
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }
}
