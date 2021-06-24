package com.nasnav.querydsl.sql;

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

    private static final long serialVersionUID = 800334354;

    public static final QUsers users = new QUsers("users");

    public final StringPath address = createString("address");

    public final StringPath authenticationToken = createString("authenticationToken");

    public final StringPath avatar = createString("avatar");

    public final StringPath birthDate = createString("birthDate");

    public final DateTimePath<java.sql.Timestamp> currentSignInAt = createDateTime("currentSignInAt", java.sql.Timestamp.class);

    public final SimplePath<Object> currentSignInIp = createSimple("currentSignInIp", Object.class);

    public final StringPath email = createString("email");

    public final StringPath encryptedPassword = createString("encryptedPassword");

    public final StringPath firstName = createString("firstName");

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath lastName = createString("lastName");

    public final DateTimePath<java.sql.Timestamp> lastSignInAt = createDateTime("lastSignInAt", java.sql.Timestamp.class);

    public final SimplePath<Object> lastSignInIp = createSimple("lastSignInIp", Object.class);

    public final StringPath mobile = createString("mobile");

    public final DateTimePath<java.sql.Timestamp> oauthExpiresAt = createDateTime("oauthExpiresAt", java.sql.Timestamp.class);

    public final StringPath oauthToken = createString("oauthToken");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath postCode = createString("postCode");

    public final DateTimePath<java.sql.Timestamp> rememberCreatedAt = createDateTime("rememberCreatedAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> resetPasswordSentAt = createDateTime("resetPasswordSentAt", java.sql.Timestamp.class);

    public final StringPath resetPasswordToken = createString("resetPasswordToken");

    public final NumberPath<Integer> signInCount = createNumber("signInCount", Integer.class);

    public final StringPath userName = createString("userName");

    public final NumberPath<Integer> userStatus = createNumber("userStatus", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QUsers> usersPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> usersOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QCartItems> _cartItemsUserIdFkey = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QOrders> _railsF868b47f6aFk = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QOauth2Users> _oauth2UsersNasnavUserIdFkey = createInvForeignKey(id, "nasnav_user_id");

    public final com.querydsl.sql.ForeignKey<QPayments> _paymentsUserIdFkey = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QProductRatings> _productRatingsUserIdFkey = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QPromotionsCartCodes> _promotionsCartCodesUserIdFkey = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QPromotionsCodesUsed> _promotionsCodesUsedUserIdFkey = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QReturnRequest> _returnRequestCreatedByUserFkey = createInvForeignKey(id, "created_by_user");

    public final com.querydsl.sql.ForeignKey<QReturnRequestItem> _returnRequestItemCreatedByUserFkey = createInvForeignKey(id, "created_by_user");

    public final com.querydsl.sql.ForeignKey<QUserAddresses> _userAddressesUserIdFkey = createInvForeignKey(id, "user_id");

    public final com.querydsl.sql.ForeignKey<QUserTokens> _userTokensUserIdFkey = createInvForeignKey(id, "user_id");

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
        addMetadata(address, ColumnMetadata.named("address").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(authenticationToken, ColumnMetadata.named("authentication_token").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(avatar, ColumnMetadata.named("avatar").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(birthDate, ColumnMetadata.named("birth_date").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(currentSignInAt, ColumnMetadata.named("current_sign_in_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(currentSignInIp, ColumnMetadata.named("current_sign_in_ip").withIndex(10).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(email, ColumnMetadata.named("email").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(encryptedPassword, ColumnMetadata.named("encrypted_password").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(firstName, ColumnMetadata.named("first_name").withIndex(26).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(image, ColumnMetadata.named("image").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(lastName, ColumnMetadata.named("last_name").withIndex(27).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(lastSignInAt, ColumnMetadata.named("last_sign_in_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(lastSignInIp, ColumnMetadata.named("last_sign_in_ip").withIndex(11).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(mobile, ColumnMetadata.named("mobile").withIndex(24).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(oauthExpiresAt, ColumnMetadata.named("oauth_expires_at").withIndex(22).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(oauthToken, ColumnMetadata.named("oauth_token").withIndex(21).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(23).ofType(Types.BIGINT).withSize(19));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(postCode, ColumnMetadata.named("post_code").withIndex(19).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(rememberCreatedAt, ColumnMetadata.named("remember_created_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordSentAt, ColumnMetadata.named("reset_password_sent_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordToken, ColumnMetadata.named("reset_password_token").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(signInCount, ColumnMetadata.named("sign_in_count").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(userName, ColumnMetadata.named("user_name").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(userStatus, ColumnMetadata.named("user_status").withIndex(25).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

