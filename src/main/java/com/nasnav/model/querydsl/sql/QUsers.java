package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QUsers is a Querydsl query type for QUsers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUsers extends com.querydsl.sql.RelationalPathBase<QUsers> {

    private static final long serialVersionUID = 149738061;

    public static final QUsers users = new QUsers("users");

    public final StringPath address = createString("address");

    public final StringPath authenticationToken = createString("authenticationToken");

    public final StringPath avatar = createString("avatar");

    public final StringPath birthDate = createString("birthDate");

    public final StringPath city = createString("city");

    public final StringPath country = createString("country");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> currentSignInAt = createDateTime("currentSignInAt", java.sql.Timestamp.class);

    public final SimplePath<Object> currentSignInIp = createSimple("currentSignInIp", Object.class);

    public final StringPath email = createString("email");

    public final StringPath encryptedPassword = createString("encryptedPassword");

    public final StringPath firebaseToken = createString("firebaseToken");

    public final NumberPath<Integer> flatNumber = createNumber("flatNumber", Integer.class);

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final DateTimePath<java.sql.Timestamp> lastSignInAt = createDateTime("lastSignInAt", java.sql.Timestamp.class);

    public final SimplePath<Object> lastSignInIp = createSimple("lastSignInIp", Object.class);

    public final StringPath mobile = createString("mobile");

    public final DateTimePath<java.sql.Timestamp> oauthExpiresAt = createDateTime("oauthExpiresAt", java.sql.Timestamp.class);

    public final StringPath oauthToken = createString("oauthToken");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath postCode = createString("postCode");

    public final StringPath provider = createString("provider");

    public final DateTimePath<java.sql.Timestamp> rememberCreatedAt = createDateTime("rememberCreatedAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> resetPasswordSentAt = createDateTime("resetPasswordSentAt", java.sql.Timestamp.class);

    public final StringPath resetPasswordToken = createString("resetPasswordToken");

    public final NumberPath<Integer> signInCount = createNumber("signInCount", Integer.class);

    public final StringPath uid = createString("uid");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final StringPath userName = createString("userName");

    public final com.querydsl.sql.PrimaryKey<QUsers> usersPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> usersOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QPockets> _railsFc49c61db2Fk = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QOrders> _railsF868b47f6aFk = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QOauth2Users> _oauth2UsersNasnavUser = createInvForeignKey(id, "nasnav_user_id");

    public QUsers(String variable) {
        super(QUsers.class, forVariable(variable), "public", "users");
        addMetadata();
    }

    public QUsers(String variable, String schema, String table) {
        super(QUsers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUsers(String variable, String schema) {
        super(QUsers.class, forVariable(variable), schema, "users");
        addMetadata();
    }

    public QUsers(Path<? extends QUsers> path) {
        super(path.getType(), path.getMetadata(), "public", "users");
        addMetadata();
    }

    public QUsers(PathMetadata metadata) {
        super(QUsers.class, metadata, "public", "users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(address, ColumnMetadata.named("address").withIndex(22).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(authenticationToken, ColumnMetadata.named("authentication_token").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(avatar, ColumnMetadata.named("avatar").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(birthDate, ColumnMetadata.named("birth_date").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(city, ColumnMetadata.named("city").withIndex(24).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(country, ColumnMetadata.named("country").withIndex(23).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(12).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(currentSignInAt, ColumnMetadata.named("current_sign_in_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(currentSignInIp, ColumnMetadata.named("current_sign_in_ip").withIndex(10).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(email, ColumnMetadata.named("email").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(encryptedPassword, ColumnMetadata.named("encrypted_password").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(firebaseToken, ColumnMetadata.named("firebase_token").withIndex(21).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(flatNumber, ColumnMetadata.named("flat_number").withIndex(27).ofType(Types.INTEGER).withSize(10));
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(image, ColumnMetadata.named("image").withIndex(28).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(lastSignInAt, ColumnMetadata.named("last_sign_in_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(lastSignInIp, ColumnMetadata.named("last_sign_in_ip").withIndex(11).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(mobile, ColumnMetadata.named("mobile").withIndex(32).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(oauthExpiresAt, ColumnMetadata.named("oauth_expires_at").withIndex(30).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(oauthToken, ColumnMetadata.named("oauth_token").withIndex(29).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(31).ofType(Types.BIGINT).withSize(19));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(25).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(postCode, ColumnMetadata.named("post_code").withIndex(26).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(provider, ColumnMetadata.named("provider").withIndex(19).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(rememberCreatedAt, ColumnMetadata.named("remember_created_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordSentAt, ColumnMetadata.named("reset_password_sent_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordToken, ColumnMetadata.named("reset_password_token").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(signInCount, ColumnMetadata.named("sign_in_count").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(uid, ColumnMetadata.named("uid").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(13).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(userName, ColumnMetadata.named("user_name").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

