package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QUserSubscriptions is a Querydsl query type for QUserSubscriptions
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserSubscriptions extends com.querydsl.sql.RelationalPathBase<QUserSubscriptions> {

    private static final long serialVersionUID = -285235723;

    public static final QUserSubscriptions userSubscriptions = new QUserSubscriptions("user_subscriptions");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath token = createString("token");

    public final com.querydsl.sql.PrimaryKey<QUserSubscriptions> userSubscriptionsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> userSubscriptionsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QUserSubscriptions(String variable) {
        super(QUserSubscriptions.class, forVariable(variable), "public", "user_subscriptions");
        addMetadata();
    }

    public QUserSubscriptions(String variable, String schema, String table) {
        super(QUserSubscriptions.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUserSubscriptions(String variable, String schema) {
        super(QUserSubscriptions.class, forVariable(variable), schema, "user_subscriptions");
        addMetadata();
    }

    public QUserSubscriptions(Path<? extends QUserSubscriptions> path) {
        super(path.getType(), path.getMetadata(), "public", "user_subscriptions");
        addMetadata();
    }

    public QUserSubscriptions(PathMetadata metadata) {
        super(QUserSubscriptions.class, metadata, "public", "user_subscriptions");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(email, ColumnMetadata.named("email").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(token, ColumnMetadata.named("token").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

