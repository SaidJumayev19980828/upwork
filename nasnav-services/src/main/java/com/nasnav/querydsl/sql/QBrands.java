package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QBrands is a Querydsl query type for QBrands
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QBrands extends com.querydsl.sql.RelationalPathBase<QBrands> {

    private static final long serialVersionUID = -198088153;

    public static final QBrands brands = new QBrands("brands");

    public final StringPath bannerImage = createString("bannerImage");

    public final NumberPath<Integer> categoryId = createNumber("categoryId", Integer.class);

    public final StringPath coverUrl = createString("coverUrl");

    public final StringPath darkLogo = createString("darkLogo");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logo = createString("logo");

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath pName = createString("pName");

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QBrands> brandsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFiles> brandsCoverUrlFkey = createForeignKey(coverUrl, "url");

    public final com.querydsl.sql.ForeignKey<QOrganizations> brandsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QShops> _shopsBrandIdFkey = createInvForeignKey(id, "brand_id");

    public final com.querydsl.sql.ForeignKey<QProducts> _productsBrandIdFkey = createInvForeignKey(id, "brand_id");

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
        addMetadata(categoryId, ColumnMetadata.named("category_id").withIndex(2).ofType(Types.INTEGER).withSize(10));
        addMetadata(coverUrl, ColumnMetadata.named("cover_url").withIndex(11).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(darkLogo, ColumnMetadata.named("dark_logo").withIndex(9).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(description, ColumnMetadata.named("description").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(6).ofType(Types.BIGINT).withSize(19));
        addMetadata(pName, ColumnMetadata.named("p_name").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(priority, ColumnMetadata.named("priority").withIndex(12).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(removed, ColumnMetadata.named("removed").withIndex(10).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

