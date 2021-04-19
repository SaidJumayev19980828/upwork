package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QOrganizationManagers is a Querydsl query type for QOrganizationManagers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationManagers extends com.querydsl.sql.RelationalPathBase<QOrganizationManagers> {

    private static final long serialVersionUID = -1508418092;

    public static final QOrganizationManagers organizationManagers = new QOrganizationManagers("organization_managers");

    public final StringPath authenticationToken = createString("authenticationToken");

    public final StringPath avatar = createString("avatar");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> currentSignInAt = createDateTime("currentSignInAt", java.sql.Timestamp.class);

    public final SimplePath<Object> currentSignInIp = createSimple("currentSignInIp", Object.class);

    public final StringPath email = createString("email");

    public final StringPath encryptedPassword = createString("encryptedPassword");

    public final BooleanPath followingStandards = createBoolean("followingStandards");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobTitle = createString("jobTitle");

    public final DateTimePath<java.sql.Timestamp> lastSignInAt = createDateTime("lastSignInAt", java.sql.Timestamp.class);

    public final SimplePath<Object> lastSignInIp = createSimple("lastSignInIp", Object.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final DateTimePath<java.sql.Timestamp> rememberCreatedAt = createDateTime("rememberCreatedAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> resetPasswordSentAt = createDateTime("resetPasswordSentAt", java.sql.Timestamp.class);

    public final StringPath resetPasswordToken = createString("resetPasswordToken");

    public final BooleanPath seo = createBoolean("seo");

    public final NumberPath<Integer> serviceType = createNumber("serviceType", Integer.class);

    public final NumberPath<Integer> signInCount = createNumber("signInCount", Integer.class);

    public final BooleanPath tutorial = createBoolean("tutorial");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QOrganizationManagers> organizationManagersPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> rails3c41486b32Fk = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QBrands> _rails7665b5107fFk = createInvForeignKey(id, "organization_manager_id");

    public final com.querydsl.sql.ForeignKey<QFacebookPixels> _rails84c2b2df8aFk = createInvForeignKey(id, "organization_manager_id");

    public QOrganizationManagers(String variable) {
        super(QOrganizationManagers.class, forVariable(variable), "public", "organization_managers");
        addMetadata();
    }

    public QOrganizationManagers(String variable, String schema, String table) {
        super(QOrganizationManagers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationManagers(String variable, String schema) {
        super(QOrganizationManagers.class, forVariable(variable), schema, "organization_managers");
        addMetadata();
    }

    public QOrganizationManagers(Path<? extends QOrganizationManagers> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_managers");
        addMetadata();
    }

    public QOrganizationManagers(PathMetadata metadata) {
        super(QOrganizationManagers.class, metadata, "public", "organization_managers");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(authenticationToken, ColumnMetadata.named("authentication_token").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(avatar, ColumnMetadata.named("avatar").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(22).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(currentSignInAt, ColumnMetadata.named("current_sign_in_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(currentSignInIp, ColumnMetadata.named("current_sign_in_ip").withIndex(10).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(email, ColumnMetadata.named("email").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(encryptedPassword, ColumnMetadata.named("encrypted_password").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(followingStandards, ColumnMetadata.named("following_standards").withIndex(17).ofType(Types.BIT).withSize(1));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(jobTitle, ColumnMetadata.named("job_title").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(lastSignInAt, ColumnMetadata.named("last_sign_in_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(lastSignInIp, ColumnMetadata.named("last_sign_in_ip").withIndex(11).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(20).ofType(Types.BIGINT).withSize(19));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(21).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(rememberCreatedAt, ColumnMetadata.named("remember_created_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordSentAt, ColumnMetadata.named("reset_password_sent_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordToken, ColumnMetadata.named("reset_password_token").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(seo, ColumnMetadata.named("seo").withIndex(16).ofType(Types.BIT).withSize(1));
        addMetadata(serviceType, ColumnMetadata.named("service_type").withIndex(19).ofType(Types.INTEGER).withSize(10));
        addMetadata(signInCount, ColumnMetadata.named("sign_in_count").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(tutorial, ColumnMetadata.named("tutorial").withIndex(18).ofType(Types.BIT).withSize(1));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(23).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

