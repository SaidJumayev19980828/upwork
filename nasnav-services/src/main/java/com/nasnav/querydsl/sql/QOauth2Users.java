package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOauth2Users is a Querydsl query type for QOauth2Users
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOauth2Users extends com.querydsl.sql.RelationalPathBase<QOauth2Users> {

    private static final long serialVersionUID = 1654084311;

    public static final QOauth2Users oauth2Users = new QOauth2Users("oauth2_users");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath loginToken = createString("loginToken");

    public final NumberPath<Long> nasnavUserId = createNumber("nasnavUserId", Long.class);

    public final StringPath oauth2Id = createString("oauth2Id");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> providerId = createNumber("providerId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QOauth2Users> oauth2UsersPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QUsers> oauth2UsersNasnavUserIdFkey = createForeignKey(nasnavUserId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> oauth2UsersOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QOauth2Providers> oauth2UsersProviderIdFkey = createForeignKey(providerId, "id");

    public QOauth2Users(String variable) {
        super(QOauth2Users.class, forVariable(variable), "public", "oauth2_users");
        addMetadata();
    }

    public QOauth2Users(String variable, String schema, String table) {
        super(QOauth2Users.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOauth2Users(String variable, String schema) {
        super(QOauth2Users.class, forVariable(variable), schema, "oauth2_users");
        addMetadata();
    }

    public QOauth2Users(Path<? extends QOauth2Users> path) {
        super(path.getType(), path.getMetadata(), "public", "oauth2_users");
        addMetadata();
    }

    public QOauth2Users(PathMetadata metadata) {
        super(QOauth2Users.class, metadata, "public", "oauth2_users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(email, ColumnMetadata.named("email").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(loginToken, ColumnMetadata.named("login_token").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(nasnavUserId, ColumnMetadata.named("nasnav_user_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(oauth2Id, ColumnMetadata.named("oauth2_id").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(6).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(providerId, ColumnMetadata.named("provider_id").withIndex(7).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

