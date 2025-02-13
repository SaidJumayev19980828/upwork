package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QExtraAttributes is a Querydsl query type for QExtraAttributes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QExtraAttributes extends com.querydsl.sql.RelationalPathBase<QExtraAttributes> {

    private static final long serialVersionUID = -759376623;

    public static final QExtraAttributes extraAttributes = new QExtraAttributes("extra_attributes");

    public final StringPath attributeType = createString("attributeType");

    public final StringPath icon = createString("icon");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath keyName = createString("keyName");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QExtraAttributes> extraAttributesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> extraAttributesOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QProductsExtraAttributes> _extraAttributeIdFkey = createInvForeignKey(id, "extra_attribute_id");

    public QExtraAttributes(String variable) {
        super(QExtraAttributes.class, forVariable(variable), "public", "extra_attributes");
        addMetadata();
    }

    public QExtraAttributes(String variable, String schema, String table) {
        super(QExtraAttributes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QExtraAttributes(String variable, String schema) {
        super(QExtraAttributes.class, forVariable(variable), schema, "extra_attributes");
        addMetadata();
    }

    public QExtraAttributes(Path<? extends QExtraAttributes> path) {
        super(path.getType(), path.getMetadata(), "public", "extra_attributes");
        addMetadata();
    }

    public QExtraAttributes(PathMetadata metadata) {
        super(QExtraAttributes.class, metadata, "public", "extra_attributes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attributeType, ColumnMetadata.named("attribute_type").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(icon, ColumnMetadata.named("icon").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(keyName, ColumnMetadata.named("key_name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
    }

}

