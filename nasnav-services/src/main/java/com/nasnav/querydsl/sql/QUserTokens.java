package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QUserTokens is a Querydsl query type for QUserTokens
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserTokens extends com.querydsl.sql.RelationalPathBase<QUserTokens> {

    private static final long serialVersionUID = 1974500219;

    public static final QUserTokens userTokens = new QUserTokens("user_tokens");

    public final NumberPath<Long> employeeUserId = createNumber("employeeUserId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath token = createString("token");

    public final DateTimePath<java.sql.Timestamp> updateTime = createDateTime("updateTime", java.sql.Timestamp.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QUserTokens> userTokensPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QEmployeeUsers> userTokensEmployeeUserIdFkey = createForeignKey(employeeUserId, "id");

    public final com.querydsl.sql.ForeignKey<QUsers> userTokensUserIdFkey = createForeignKey(userId, "id");

    public QUserTokens(String variable) {
        super(QUserTokens.class, forVariable(variable), "public", "user_tokens");
        addMetadata();
    }

    public QUserTokens(String variable, String schema, String table) {
        super(QUserTokens.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUserTokens(String variable, String schema) {
        super(QUserTokens.class, forVariable(variable), schema, "user_tokens");
        addMetadata();
    }

    public QUserTokens(Path<? extends QUserTokens> path) {
        super(path.getType(), path.getMetadata(), "public", "user_tokens");
        addMetadata();
    }

    public QUserTokens(PathMetadata metadata) {
        super(QUserTokens.class, metadata, "public", "user_tokens");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(employeeUserId, ColumnMetadata.named("employee_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(token, ColumnMetadata.named("token").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(updateTime, ColumnMetadata.named("update_time").withIndex(3).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
    }

}

