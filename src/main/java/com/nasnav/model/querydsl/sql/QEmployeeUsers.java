package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QEmployeeUsers is a Querydsl query type for QEmployeeUsers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QEmployeeUsers extends com.querydsl.sql.RelationalPathBase<QEmployeeUsers> {

    private static final long serialVersionUID = 113238847;

    public static final QEmployeeUsers employeeUsers = new QEmployeeUsers("employee_users");

    public final StringPath authenticationToken = createString("authenticationToken");

    public final StringPath avatar = createString("avatar");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Integer> createdBy = createNumber("createdBy", Integer.class);

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

    public final NumberPath<Long> organizationManagerId = createNumber("organizationManagerId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final DateTimePath<java.sql.Timestamp> rememberCreatedAt = createDateTime("rememberCreatedAt", java.sql.Timestamp.class);

    public final DateTimePath<java.sql.Timestamp> resetPasswordSentAt = createDateTime("resetPasswordSentAt", java.sql.Timestamp.class);

    public final StringPath resetPasswordToken = createString("resetPasswordToken");

    public final BooleanPath seo = createBoolean("seo");

    public final NumberPath<Integer> serviceType = createNumber("serviceType", Integer.class);

    public final NumberPath<Long> shopId = createNumber("shopId", Long.class);

    public final NumberPath<Integer> signInCount = createNumber("signInCount", Integer.class);

    public final BooleanPath tutorial = createBoolean("tutorial");

    public final StringPath type = createString("type");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QEmployeeUsers> employeeUsersPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> employeeUsersOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QEmployeeUsers(String variable) {
        super(QEmployeeUsers.class, forVariable(variable), "public", "employee_users");
        addMetadata();
    }

    public QEmployeeUsers(String variable, String schema, String table) {
        super(QEmployeeUsers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEmployeeUsers(String variable, String schema) {
        super(QEmployeeUsers.class, forVariable(variable), schema, "employee_users");
        addMetadata();
    }

    public QEmployeeUsers(Path<? extends QEmployeeUsers> path) {
        super(path.getType(), path.getMetadata(), "public", "employee_users");
        addMetadata();
    }

    public QEmployeeUsers(PathMetadata metadata) {
        super(QEmployeeUsers.class, metadata, "public", "employee_users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(authenticationToken, ColumnMetadata.named("authentication_token").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(avatar, ColumnMetadata.named("avatar").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(createdBy, ColumnMetadata.named("created_by").withIndex(21).ofType(Types.INTEGER).withSize(10));
        addMetadata(currentSignInAt, ColumnMetadata.named("current_sign_in_at").withIndex(13).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(currentSignInIp, ColumnMetadata.named("current_sign_in_ip").withIndex(15).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(email, ColumnMetadata.named("email").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(encryptedPassword, ColumnMetadata.named("encrypted_password").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(followingStandards, ColumnMetadata.named("following_standards").withIndex(23).ofType(Types.BIT).withSize(1));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(jobTitle, ColumnMetadata.named("job_title").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(lastSignInAt, ColumnMetadata.named("last_sign_in_at").withIndex(14).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(lastSignInIp, ColumnMetadata.named("last_sign_in_ip").withIndex(16).ofType(Types.OTHER).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(19).ofType(Types.BIGINT).withSize(19));
        addMetadata(organizationManagerId, ColumnMetadata.named("organization_manager_id").withIndex(27).ofType(Types.BIGINT).withSize(19));
        addMetadata(phoneNumber, ColumnMetadata.named("phone_number").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(rememberCreatedAt, ColumnMetadata.named("remember_created_at").withIndex(11).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordSentAt, ColumnMetadata.named("reset_password_sent_at").withIndex(10).ofType(Types.TIMESTAMP).withSize(29).withDigits(6));
        addMetadata(resetPasswordToken, ColumnMetadata.named("reset_password_token").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(seo, ColumnMetadata.named("seo").withIndex(22).ofType(Types.BIT).withSize(1));
        addMetadata(serviceType, ColumnMetadata.named("service_type").withIndex(24).ofType(Types.INTEGER).withSize(10));
        addMetadata(shopId, ColumnMetadata.named("shop_id").withIndex(26).ofType(Types.BIGINT).withSize(19));
        addMetadata(signInCount, ColumnMetadata.named("sign_in_count").withIndex(12).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(tutorial, ColumnMetadata.named("tutorial").withIndex(25).ofType(Types.BIT).withSize(1));
        addMetadata(type, ColumnMetadata.named("type").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

