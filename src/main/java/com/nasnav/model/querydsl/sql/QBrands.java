package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QBrands is a Querydsl query type for QBrands
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QBrands extends com.querydsl.sql.RelationalPathBase<QBrands> {

    private static final long serialVersionUID = -198088153;

    public static final QBrands brands = new QBrands("brands");

    public final StringPath bannerImage = createString("bannerImage");

    public final SimplePath<String[]> categories = createSimple("categories", String[].class);

    public final NumberPath<Integer> categoryId = createNumber("categoryId", Integer.class);

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath darkLogo = createString("darkLogo");

    public final StringPath description = createString("description");

    public final StringPath displayName = createString("displayName");

    public final StringPath facebook = createString("facebook");

    public final BooleanPath followingStandards = createBoolean("followingStandards");

    public final BooleanPath fri = createBoolean("fri");

    public final TimePath<java.sql.Time> from = createTime("from", java.sql.Time.class);

    public final StringPath googlePlus = createString("googlePlus");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath instagram = createString("instagram");

    public final StringPath logo = createString("logo");

    public final BooleanPath mon = createBoolean("mon");

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Long> organizationManagerId = createNumber("organizationManagerId", Long.class);

    public final SimplePath<String[]> phoneNumbers = createSimple("phoneNumbers", String[].class);

    public final StringPath pinterest = createString("pinterest");

    public final StringPath pName = createString("pName");

    public final BooleanPath sat = createBoolean("sat");

    public final StringPath snapchat = createString("snapchat");

    public final BooleanPath stockManagement = createBoolean("stockManagement");

    public final BooleanPath sun = createBoolean("sun");

    public final BooleanPath thu = createBoolean("thu");

    public final TimePath<java.sql.Time> to = createTime("to", java.sql.Time.class);

    public final BooleanPath tue = createBoolean("tue");

    public final StringPath twitter = createString("twitter");

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final SimplePath<String[]> websites = createSimple("websites", String[].class);

    public final BooleanPath wed = createBoolean("wed");

    public final StringPath youtube = createString("youtube");

    public final com.querydsl.sql.PrimaryKey<QBrands> brandsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizationManagers> rails7665b5107fFk = createForeignKey(organizationManagerId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> brandsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QProductsOlds> _railsF3b4d49caaFk = createInvForeignKey(id, "brand_id");

    public final com.querydsl.sql.ForeignKey<QShops> _shopsBrandIdFkey = createInvForeignKey(id, "brand_id");

    public QBrands(String variable) {
        super(QBrands.class, forVariable(variable), "public", "brands");
        addMetadata();
    }

    public QBrands(String variable, String schema, String table) {
        super(QBrands.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QBrands(String variable, String schema) {
        super(QBrands.class, forVariable(variable), schema, "brands");
        addMetadata();
    }

    public QBrands(Path<? extends QBrands> path) {
        super(path.getType(), path.getMetadata(), "public", "brands");
        addMetadata();
    }

    public QBrands(PathMetadata metadata) {
        super(QBrands.class, metadata, "public", "brands");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(bannerImage, ColumnMetadata.named("banner_image").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(categories, ColumnMetadata.named("categories").withIndex(11).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(categoryId, ColumnMetadata.named("category_id").withIndex(2).ofType(Types.INTEGER).withSize(10));
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(darkLogo, ColumnMetadata.named("dark_logo").withIndex(33).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(description, ColumnMetadata.named("description").withIndex(16).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(displayName, ColumnMetadata.named("display_name").withIndex(32).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(facebook, ColumnMetadata.named("facebook").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(followingStandards, ColumnMetadata.named("following_standards").withIndex(10).ofType(Types.BIT).withSize(1));
        addMetadata(fri, ColumnMetadata.named("fri").withIndex(27).ofType(Types.BIT).withSize(1));
        addMetadata(from, ColumnMetadata.named("from").withIndex(28).ofType(Types.TIME).withSize(15).withDigits(6));
        addMetadata(googlePlus, ColumnMetadata.named("google_plus").withIndex(20).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(instagram, ColumnMetadata.named("instagram").withIndex(14).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(mon, ColumnMetadata.named("mon").withIndex(23).ofType(Types.BIT).withSize(1));
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(8).ofType(Types.BIGINT).withSize(19));
        addMetadata(organizationManagerId, ColumnMetadata.named("organization_manager_id").withIndex(34).ofType(Types.BIGINT).withSize(19));
        addMetadata(phoneNumbers, ColumnMetadata.named("phone_numbers").withIndex(31).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(pinterest, ColumnMetadata.named("pinterest").withIndex(17).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(sat, ColumnMetadata.named("sat").withIndex(21).ofType(Types.BIT).withSize(1));
        addMetadata(snapchat, ColumnMetadata.named("snapchat").withIndex(19).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(stockManagement, ColumnMetadata.named("stock_management").withIndex(15).ofType(Types.BIT).withSize(1));
        addMetadata(sun, ColumnMetadata.named("sun").withIndex(22).ofType(Types.BIT).withSize(1));
        addMetadata(thu, ColumnMetadata.named("thu").withIndex(26).ofType(Types.BIT).withSize(1));
        addMetadata(to, ColumnMetadata.named("to").withIndex(29).ofType(Types.TIME).withSize(15).withDigits(6));
        addMetadata(tue, ColumnMetadata.named("tue").withIndex(24).ofType(Types.BIT).withSize(1));
        addMetadata(twitter, ColumnMetadata.named("twitter").withIndex(13).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(7).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(websites, ColumnMetadata.named("websites").withIndex(30).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(wed, ColumnMetadata.named("wed").withIndex(25).ofType(Types.BIT).withSize(1));
        addMetadata(youtube, ColumnMetadata.named("youtube").withIndex(18).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

