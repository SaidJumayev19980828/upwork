package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QAdmins is a Querydsl query type for QAdmins
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAdmins extends com.querydsl.sql.RelationalPathBase<QAdmins> {

    private static final long serialVersionUID = -239293601;

    public static final QAdmins admins = new QAdmins("admins");

    public final StringPath authenticationToken = createString("authenticationToken");

    public final StringPath avatar = createString("avatar");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> currentSignInAt = createDateTime("currentSignInAt", java.sql.Timestamp.class);

    public final SimplePath<Object> currentSignInIp = createSimple("currentSignInIp", Object.class);

    public final StringPath email = createString("email");

    public final StringPath encryptedPassword = createString("encryptedPassword");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath jobTitle = createString("jobTitle");

    public final DateTimePath<java.sql.Timestamp> lastSignInAt = createDateTime("lastSignInAt", java.sql.Timestamp.class);

    public final SimplePath<Object> lastSignInIp = createSimple("lastSignInIp", Object.class);

    public final StringPath name = createString("name");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final DateTimePath<java.sql.Timestamp> rememberCreatedAt = createDateTime("rememberCreatedAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> resetPasswordSentAt = createDateTime("resetPasswordSentAt", java.sql.Timestamp.class);

    public final StringPath resetPasswordToken = createString("resetPasswordToken");

    public final BooleanPath seo = createBoolean("seo");

    public final NumberPath<Integer> signInCount = createNumber("signInCount", Integer.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QAdmins> adminsPkey = createPrimaryKey(id);

    public QAdmins(String variable) {
        super(QAdmins.class, forVariable(variable), "public", "admins");
        addMetadata();
    }

    public QAdmins(String variable, String schema, String table) {
        super(QAdmins.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAdmins(String variable, String schema) {
        super(QAdmins.class, forVariable(variable), schema, "admins");
        addMetadata();
    }

    public QAdmins(Path<? extends QAdmins> path) {
        super(path.getType(), path.getMetadata(), "public", "admins");
        addMetadata();
    }

    public QAdmins(PathMetadata metadata) {
        super(QAdmins.class, metadata, "public", "admins");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(authenticationToken, ColumnMetadata.named("authentication_token").withIndex(19).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(avatar, ColumnMetadata.named("avatar").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(12).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(currentSignInAt, ColumnMetadata.named("current_sign_in_at").withIndex(8).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(currentSignInIp, ColumnMetadata.named("current_sign_in_ip").withIndex(10).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(email, ColumnMetadata.named("email").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(encryptedPassword, ColumnMetadata.named("encrypted_password").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(jobTitle, ColumnMetadata.named("job_title").withIndex(15).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(lastSignInAt, ColumnMetadata.named("last_sign_in_at").withIndex(9).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(lastSignInIp, ColumnMetadata.named("last_sign_in_ip").withIndex(11).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(rememberCreatedAt, ColumnMetadata.named("remember_created_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordSentAt, ColumnMetadata.named("reset_password_sent_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordToken, ColumnMetadata.named("reset_password_token").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(seo, ColumnMetadata.named("seo").withIndex(18).ofType(Types.BIT).withSize(1));
        addMetadata(signInCount, ColumnMetadata.named("sign_in_count").withIndex(7).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(13).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

