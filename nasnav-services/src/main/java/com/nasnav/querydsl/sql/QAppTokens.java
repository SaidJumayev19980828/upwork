package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QAppTokens is a Querydsl query type for QAppTokens
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAppTokens extends com.querydsl.sql.RelationalPathBase<QAppTokens> {

    private static final long serialVersionUID = -1943951136;

    public static final QAppTokens appTokens = new QAppTokens("app_tokens");

    public final BooleanPath active = createBoolean("active");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> platform = createNumber("platform", Integer.class);

    public final StringPath title = createString("title");

    public final StringPath token = createString("token");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QAppTokens> appTokensPkey = createPrimaryKey(id);

    public QAppTokens(String variable) {
        super(QAppTokens.class, forVariable(variable), "public", "app_tokens");
        addMetadata();
    }

    public QAppTokens(String variable, String schema, String table) {
        super(QAppTokens.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAppTokens(String variable, String schema) {
        super(QAppTokens.class, forVariable(variable), schema, "app_tokens");
        addMetadata();
    }

    public QAppTokens(Path<? extends QAppTokens> path) {
        super(path.getType(), path.getMetadata(), "public", "app_tokens");
        addMetadata();
    }

    public QAppTokens(PathMetadata metadata) {
        super(QAppTokens.class, metadata, "public", "app_tokens");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(active, ColumnMetadata.named("active").withIndex(2).ofType(Types.BIT).withSize(1));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(platform, ColumnMetadata.named("platform").withIndex(7).ofType(Types.INTEGER).withSize(10));
        addMetadata(title, ColumnMetadata.named("title").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(token, ColumnMetadata.named("token").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

