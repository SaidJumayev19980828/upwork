package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSubscribedUsers is a Querydsl query type for QSubscribedUsers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSubscribedUsers extends com.querydsl.sql.RelationalPathBase<QSubscribedUsers> {

    private static final long serialVersionUID = 512814035;

    public static final QSubscribedUsers subscribedUsers = new QSubscribedUsers("subscribed_users");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QSubscribedUsers> subscribedUsersPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShops> railsF0bd17ab86Fk = createForeignKey(shopId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> rails1038aeed15Fk = createForeignKey(organizationId, "id");

    public QSubscribedUsers(String variable) {
        super(QSubscribedUsers.class, forVariable(variable), "public", "subscribed_users");
        addMetadata();
    }

    public QSubscribedUsers(String variable, String schema, String table) {
        super(QSubscribedUsers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSubscribedUsers(String variable, String schema) {
        super(QSubscribedUsers.class, forVariable(variable), schema, "subscribed_users");
        addMetadata();
    }

    public QSubscribedUsers(Path<? extends QSubscribedUsers> path) {
        super(path.getType(), path.getMetadata(), "public", "subscribed_users");
        addMetadata();
    }

    public QSubscribedUsers(PathMetadata metadata) {
        super(QSubscribedUsers.class, metadata, "public", "subscribed_users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(email, ColumnMetadata.named("email").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

