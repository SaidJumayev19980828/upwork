package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTags is a Querydsl query type for QTags
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAddons extends com.querydsl.sql.RelationalPathBase<QAddons> {

    private static final long serialVersionUID = -1;

    public static final QAddons addons = new QAddons("addons");

 



    public final NumberPath<Long> id = createNumber("id", Long.class);

  

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

   
    public final NumberPath<Integer> type = createNumber("type", Integer.class);
    public final NumberPath<Integer> removed = createNumber("removed", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QAddons> addonsPkey = createPrimaryKey(id);


    public final com.querydsl.sql.ForeignKey<QOrganizations> tagsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QProductAddons> _productAddonsAddonIdFkey = createInvForeignKey(id, "addon_id");


    public QAddons(String variable) {
        super(QAddons.class, forVariable(variable), "public", "addons");
        addMetadata();
    }

    public QAddons(String variable, String schema, String table) {
        super(QAddons.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAddons(String variable, String schema) {
        super(QAddons.class, forVariable(variable), schema, "addons");
        addMetadata();
    }

    public QAddons(Path<? extends QAddons> path) {
        super(path.getType(), path.getMetadata(), "public", "addons");
        addMetadata();
    }

    public QAddons(PathMetadata metadata) {
        super(QAddons.class, metadata, "public", "addons");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(removed, ColumnMetadata.named("type").withIndex(2).ofType(Types.INTEGER).withSize(10).notNull());

        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(removed, ColumnMetadata.named("removed").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

